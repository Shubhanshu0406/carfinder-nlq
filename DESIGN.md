# Design

## Overview

The request has two distinct jobs: understand what the user asked for, and find/rank vehicles that
match it. The code is split along that line.

```
Client
  │  POST /api/search { query, page, size }
  ▼
SearchController
  ▼
SearchServiceImpl
  ├─► NlQueryParser (GeminiNlQueryParser) ──► VehicleFilter
  │       parses free text into a structured filter, via Gemini
  ├─► VehicleQueryBuilder ──► Elasticsearch query
  │       turns VehicleFilter into a bool + function_score query
  └─► VehicleSearchRepository ──► Elasticsearch ──► ranked VehicleResults
          executes the query, maps hits back to DTOs
  ▼
SearchResponse { query, appliedFilters, results, page, size, totalHits }
```

`SearchServiceImpl` just calls those three collaborators in order; it owns no logic of its own. Parsing, query-building, and execution are three different concerns with three different failure modes, so each gets its own class and its own test.

## Data store: Elasticsearch, no relation DB

There's no write path for listings here, no create/update/delete, no transactions. The whole job is "search a catalogue," which is what Elasticsearch is for. Adding MySQL alongside it would mean a repository layer, migrations, and a sync process whose only job is keeping two stores in agreement - pure overhead when only one of them is ever queried.

## Natural language → structured filter

`NlQueryParser` is an interface: `parse(String) -> VehicleFilter`. `VehicleFilter` is a flat record — price range, body type, fuel type, transmission, km-driven range, year range, safety-rating floor, brand, model — every field nullable, meaning the user didn't mention it.

`GeminiNlQueryParser` is the one implementation. It calls Gemini's `generateContent` endpoint directly over `RestClient` (no provider SDK), with `responseMimeType: application/json` and a `responseSchema`
(`GeminiFilterSchema`) that constrains the model to the filter shape. A prompt template (`prompts/nl-to-filter-prompt.txt`) carries the rules the model can't infer on its own — unit conversions ("15L" → 1,500,000), valid enum values, what "high safety rating" or "less driven" means — plus worked examples, which keep it from wrapping the answer in prose.

One rule needed more than static text: "new"/"newer" only means something relative to today, and an LLM has no reliable notion of the current date, especially past its training cutoff. So the prompt carries a `{{current_year}}` placeholder that `GeminiNlQueryParser` resolves against the JVM clock before sending — the model is handed the real number instead of guessing it.

Turning the model's raw JSON into a `VehicleFilter` is its own class, `FilterJsonMapper` — pure JSON-to-object mapping, no HTTP, testable with plain strings. It also lowercases body type, fuel type, and transmission, since those are small fixed enums and the model doesn't always echo the exact casing it was given.

Anything that fails here — malformed response, unreachable model, unexpected shape — raises `LlmParsingException`, mapped centrally to a `422`: a query the model couldn't handle is a client-facing outcome, not a server fault.

## Filter → query, and ranking

`VehicleQueryBuilder` does two distinct things, kept deliberately separate.

**Filtering** (`buildBoolQuery`): one `bool.filter` clause per set field — `range` for price, km-driven, year, and the safety-rating floor; `term` for body type, fuel type, transmission, brand, model. All in filter context, not query context — these are exact constraints with no relevance of their own.

**Ranking**: three `ScoringStrategy` beans (`RecencyBoostStrategy`, `SafetyRatingBoostStrategy`, `LowMileageBoostStrategy`), each one `function_score` function — gaussian decay on `listed_date`, linear boost on `safety_rating`, reciprocal boost on `km_driven`. Spring collects every `ScoringStrategy` bean into the list `VehicleQueryBuilder` is built with, so a new ranking signal is a new class, not a change to the builder.

One detail worth flagging: `boost_mode` is `replace`, not the more common `multiply`. The bool query is filter-only, so it never produces a relevance score of its own — filter context always scores zero. `multiply` against that zero would erase every ranking signal; `replace` makes the combined function score the entire ranking signal, which is the actual point of using function_score here.

## Data model and seed data

The mapping (`seed-data/mapping.json`) follows the brief: `id, brand, model, variant, price, body_type, fuel_type, transmission, km_driven, year, safety_rating, city, listed_date`. `price`/`km_driven` are numeric ranges; `listed_date` is a plain `yyyy-MM-dd` date for the recency decay; everything filtered by exact value is `keyword`.

Every `keyword` field carries a custom `lowercase_normalizer`. The model lowercases enum-like fields reliably, but brand/model names aren't a fixed enum, so their casing isn't guaranteed. The normalizer makes `term` filtering case-insensitive at the index level (applied to both stored and query values automatically), while `_source` keeps original casing ("Hyundai") for display.

`SeedDataGenerator` (run via `./mvnw exec:java`) draws from a static catalog of real Indian-market brand/model combinations and randomizes year, mileage, price, safety rating, city, and listing recency around a realistic base, with a fixed seed for reproducibility — output is `_bulk`-ready NDJSON. `SeedLoader`, a `CommandLineRunner` gated by `app.seed.auto-load`, creates the index from `mapping.json` and bulk-loads that NDJSON on startup if the index doesn't exist yet.

One shared detail: `VehicleDocument` is camelCase, but the mapping and NDJSON need snake_case. Rather than annotate every field, one `VehicleJsonMapper` factory configures a single `ObjectMapper` (snake_case, ISO dates), reused by the generator, the loader, and the Elasticsearch client's own mapper — so all three serialize identically instead of drifting apart.

## Error handling

Every failure maps to a specific exception, and one `GlobalExceptionHandler` turns them into a consistent JSON body (`timestamp`, `status`, `error`, `message`):

- Request validation failure (blank query, size out of range) → `400`
- `LlmParsingException` (model call failed, or unparseable response) → `422`
- `SearchExecutionException` (Elasticsearch failed) → `502`

## Testing

Each layer is tested at the level where it's cheapest and most precise:

- `VehicleQueryBuilderTest` — every filter field, combinations, and the function_score assembly, no ES involved. This is the core of the assignment, so it's the most thoroughly covered.
- `FilterJsonMapperTest` — LLM response parsing/mapping, plain strings, no network.
- `GeminiNlQueryParserTest` — mocks `RestClient`: success, malformed shape, transport failure, and that the current-year placeholder actually resolves.
- `SearchServiceImplTest` — orchestration order and arguments, all collaborators mocked.
- `SearchControllerTest` — `MockMvc` against a mocked `SearchService`: status codes and error mapping.

No automated test runs against a live Elasticsearch instance — the brief accepts an isolated query-builder test as the fallback here. The actual query shape (and the `boost_mode` choice above) was verified by hand against a running ES container during development.

## Tradeoffs

- **No live-ES integration test** — an isolated query-builder test stands in for it.
- **Offset-based pagination only.** Fine at this scale; would need `search_after` for deep pagination.
- **No fuzzy/synonym matching** on brand/model (e.g. "Merc" won't resolve to "Mercedes") — the model is trusted to normalize names it recognizes.
- **Single ES node, security disabled** in `docker-compose.yml` — local dev only, not a production topology.

## What's next

- A Testcontainers-backed integration test against a live index.
- Fuzzy/synonym matching on brand and model.
- Caching identical NL queries to cut LLM latency and cost.
- A confidence signal from parsing, so an ambiguous query can prompt for clarification instead of failing outright.
