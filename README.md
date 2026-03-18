# Cricket Backend

Spring Boot backend for the cricket scoring project, migrated from the legacy Java console codebase.

## Current Scope

Implemented APIs:
- auth signup/login
- teams and players
- single match creation and scoring
- innings start
- scoreboard and undo
- series creation and listing
- tournament creation and listing
- tournament points table
- audience dashboard

Legacy integration:
- single-match innings scoring is still driven through the legacy `MatchSimulation` engine via [`LegacySimulationBridgeService`](src/main/kotlin/com/cricket/cricketbackend/service/LegacySimulationBridgeService.kt)

## Tech Stack

- Java 21
- Kotlin
- Spring Boot
- Spring Data JPA
- MySQL

## Run

Set environment variables or use the defaults in `application.properties`.

```bash
./mvnw spring-boot:run
```

Build:

```bash
./mvnw -DskipTests compile
```

## Configuration

Recommended environment variables:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`

Example values are provided in `.env.example`.

## Important Limitation

The backend is not fully production ready yet.

Main blocker:
- the legacy scoring engine still uses static mutable state, so concurrent live match scoring is unsafe

Other incomplete areas:
- restart-safe recovery of live innings state
- full saved/resume lifecycle parity
- playoff/final lifecycle automation
- full integration test coverage

## API Summary

See [`docs/API_EXAMPLES.md`](docs/API_EXAMPLES.md) for example requests.

Main routes:

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `GET /api/v1/teams`
- `POST /api/v1/teams`
- `GET /api/v1/teams/{teamId}/players`
- `POST /api/v1/teams/{teamId}/players`
- `GET /api/v1/matches`
- `POST /api/v1/matches/single`
- `GET /api/v1/matches/{matchId}`
- `POST /api/v1/matches/{matchId}/innings/{inningsNo}/start`
- `POST /api/v1/matches/{matchId}/ball`
- `GET /api/v1/matches/{matchId}/scoreboard`
- `POST /api/v1/matches/{matchId}/undo-last-ball`
- `POST /api/v1/series`
- `GET /api/v1/series`
- `GET /api/v1/series/{seriesId}`
- `POST /api/v1/tournaments`
- `GET /api/v1/tournaments`
- `GET /api/v1/tournaments/{tournamentId}`
- `GET /api/v1/tournaments/{tournamentId}/points-table`
- `GET /api/v1/audience/dashboard`
