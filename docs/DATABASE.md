## 🗃️ Model de Dados

### `player`

| Campo | Tipo | Notas |
|-------|------|-------|
| `id` | `BIGSERIAL PK` | |
| `nickname` | `VARCHAR(50) UNIQUE` | |
| `email` | `VARCHAR(100) UNIQUE` | adicionado no auth |
| `password_hash` | `VARCHAR(255)` | BCrypt; oculto no JSON (`@JsonIgnore`) |
| `role` | `VARCHAR(20)` | `PLAYER`/`ORGANIZER`/`ADMIN` |

### `tournament`

| Campo | Tipo | Notas |
|-------|------|-------|
| `id` | `BIGSERIAL PK` | |
| `name` | `VARCHAR(100)` | |
| `game` | `VARCHAR(50)` | |
| `status` | `VARCHAR(20)` | `CREATED`/`STARTED`/`FINISHED` |
| `fk_winner_id` | `BIGINT` → player | campeão |
| `fk_owner` | `BIGINT` → player | dono do torneio (roadmap 4.2) |
| `criado_em` | `TIMESTAMP` | DEFAULT now() |

### `tournament_player`

| Campo | Tipo | Notas |
|-------|------|-------|
| `id` | `BIGSERIAL PK` | |
| `fk_tournament_id` | `BIGINT` → tournament | ON DELETE CASCADE, UNIQUE com player |
| `fk_player_id` | `BIGINT` → player | ON DELETE CASCADE |
| `seed` | `INT` | |

### `matches`

| Campo | Tipo | Notas |
|-------|------|-------|
| `id` | `BIGSERIAL PK` | |
| `fk_tournament_id` | `BIGINT` → tournament | ON DELETE CASCADE |
| `fk_player1_id` / `fk_player2_id` | `BIGINT` → player | nullable (byes) |
| `fk_winner_id` | `BIGINT` → player | |
| `score_player1` / `score_player2` | `INT` | |
| `bracket_type` | `VARCHAR(20)` | `WINNERS` (no Single Elim) |
| `round_number` | `INT` | 1 = primeira rodada |
| `match_number` | `INT` | |
| `fk_next_match_win_id` | `BIGINT` → matches | próxima partida |
| `fk_next_match_lose_id` | `BIGINT` → matches | não usado no Single Elim |
| `status` | `VARCHAR(20)` | `WAITING`/`READY`/`IN_PROGRESS`/`FINISHED` |