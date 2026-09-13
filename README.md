# carfinder-nlq

A backend service that searches a vehicle catalogue using natural language - "Show SUVs under ₹15L",
"Diesel automatic cars below 80k km" — by having an LLM turn the query into a structured filter, then
translating that filter into an Elasticsearch `bool` + `function_score` query.

See [DESIGN.md](DESIGN.md) for the architecture rationale and tradeoffs.

## Prerequisites

- Java 17+
- Maven (or use the bundled `./mvnw` wrapper)
- Docker (for Elasticsearch)
- A free Gemini API key from [Google AI Studio](https://aistudio.google.com/apikey)

## Setup

```bash
docker-compose up -d
export GEMINI_API_KEY=your-key-here
export SEED_AUTO_LOAD=true   # first run only: auto-creates the index and bulk-loads seed data
./mvnw spring-boot:run
```

On startup, if `SEED_AUTO_LOAD=true` and the `vehicles` index doesn't exist yet, the app creates it
from `seed-data/mapping.json` and bulk-loads `seed-data/vehicles.ndjson` (1200 documents). Subsequent
runs can drop the env var — the loader is a no-op once the index exists.

To (re)generate the seed data yourself instead of using the committed NDJSON file:

```bash
./mvnw exec:java
```

This overwrites `seed-data/vehicles.ndjson` with a freshly randomized (but reproducible — fixed seed)
catalogue of ~1200 realistic Indian-market used-car listings.

## Configuration

| Env var           | Default                  | Purpose                                   |
|--------------------|---------------------------|--------------------------------------------|
| `GEMINI_API_KEY`   | (required)                | Gemini API key for NL-to-filter parsing     |
| `GEMINI_MODEL`     | `gemini-3.6-flash`        | Gemini model id (bump here if Google deprecates it again) |
| `ES_HOST`          | `localhost`               | Elasticsearch host                          |
| `ES_PORT`          | `9200`                    | Elasticsearch port                          |
| `SEED_AUTO_LOAD`   | `false`                   | Auto-create index + bulk-load on startup    |

## API

### `POST /api/search`

**Request**

```json
{
  "query": "Show SUVs under 15L",
  "page": 0,
  "size": 10
}
```

`page` (default `0`) and `size` (default `20`, max `100`) are optional.

**Response**

```json
{
  "query": "Show SUVs under 15L",
  "appliedFilters": {
    "priceMin": null,
    "priceMax": 1500000,
    "bodyType": "suv",
    "fuelType": null,
    "transmission": null,
    "kmDrivenMin": null,
    "kmDrivenMax": null,
    "yearMin": null,
    "yearMax": null,
    "safetyRatingMin": null,
    "brand": null,
    "model": null
  },
  "results": [
    {
      "id": "df5e478e-0e7f-4ad7-ab60-76adc8f78a79",
      "brand": "Kia",
      "model": "Seltos",
      "variant": "VX Diesel MT",
      "price": 749366,
      "bodyType": "suv",
      "fuelType": "diesel",
      "transmission": "manual",
      "kmDriven": 48992,
      "year": 2022,
      "safetyRating": 5,
      "city": "Pune",
      "listedDate": "2026-09-10",
      "score": 6.99
    }
  ],
  "page": 0,
  "size": 10,
  "totalHits": 628
}
```

**Errors** — a consistent JSON error body on all failure paths:

```json
{ "timestamp": "...", "status": 422, "error": "Unprocessable Entity", "message": "..." }
```

| Status | Cause                                              |
|--------|-----------------------------------------------------|
| 400    | Request validation failure (e.g. blank `query`)     |
| 422    | The LLM call failed or returned an unparseable filter |
| 502    | Elasticsearch query execution failed                |

### Example queries

```bash
curl -X POST localhost:8080/api/search -H 'Content-Type: application/json' \
  -d '{"query": "Show SUVs under 15L"}'

curl -X POST localhost:8080/api/search -H 'Content-Type: application/json' \
  -d '{"query": "Diesel automatic cars below 80k km"}'

curl -X POST localhost:8080/api/search -H 'Content-Type: application/json' \
  -d '{"query": "Family cars with high safety ratings"}'
```

## Running tests

```bash
./mvnw test
```

JUnit5 throughout, covering the query builder (bool + function_score assembly), the LLM response
parsing/mapping logic, the service orchestration, and the controller (MockMvc).

## Repo layout

```
src/main/java/com/carfinder/nlq/
  config/     Elasticsearch + Gemini client beans
  controller/ REST endpoint
  dto/        Request/response/filter records
  exception/  Custom exceptions + @RestControllerAdvice
  json/       Shared ObjectMapper config (snake_case, ISO dates)
  llm/        NL query parser (interface + Gemini implementation)
  model/      Elasticsearch document shape
  search/     Query builder + scoring strategies + ES repository
  seed/       Seed data generator + startup loader
  service/    Search orchestration
seed-data/    mapping.json + vehicles.ndjson (committed, regeneratable)
```
