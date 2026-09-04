## 📡 API — Endpoints

### Torneios

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/tournaments` | Listar. Query: `name`, `game`, `status`. Paginação: `page`+`size` → `PaginaResult` |
| GET | `/tournaments/{id}` | Buscar por id |
| POST | `/tournaments` | Criar `{ name, game }` → `201` |
| PUT | `/tournaments/{id}` | Atualizar `{ name, game }` → `201` |
| DELETE | `/tournaments/{id}` | Deletar → `204` |
| POST | `/tournaments/{id}/start` | Iniciar (só se `CREATED`) → `204` |
| POST | `/tournaments/{id}/finish` | Finalizar (só se `STARTED`) → `204` |
| POST | `/tournaments/{id}/generate` | Gera bracket (chama `start`) → `201` lista de matches |
| POST | `/tournaments/{id}/recalculate` | Resetar + gerar de novo → `204` |
| GET | `/tournaments/{id}/ranking` | Ranking por colocação |

### Players

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/players` | Listar. Query: `nickname`. Paginação: `page`+`size` |
| GET | `/players/{id}` | Buscar |
| GET | `/players/{id}/stats` | Stats do player (torneios/vitórias/derrotas/winrate) |
| POST | `/tournaments/{id}/players` | Adicionar player `{ playerId }` → `204` |
| GET | `/tournaments/{id}/players` | Players do torneio |
| DELETE | `/tournaments/{id}/players/{playerId}` | Remover player do torneio → `204` |
| DELETE | `/players/{id}` | Deletar player → `204` |

> `POST /players` foi **removido** — cadastro agora é via `/auth/register`.

### Partidas

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/tournaments/{id}/matches` | Partidas de um torneio |
| GET | `/matches` | Histórico. Query: `playerId`, `tournamentId`, `page`, `size` |
| GET | `/matches/{id}` | Buscar partida |
| PATCH | `/matches/{id}/start` | Marcar `IN_PROGRESS` (só se `READY` + 2 players) |
| PATCH | `/matches/{id}/result` | Registrar resultado (ver DTO abaixo) |
| PATCH | `/tournaments/{id}/matches/start-round` | Iniciar rodada `{ round }` |

**`POST /matches/{id}/result` — corpo** (`ResultadoRequest`):
```json
{ "fk_winner_id": 1, "score_player1": 3, "score_player2": 1 }
```

Regras: winner deve ser player1 ou player2; scores ≥ 0; só `READY`/`IN_PROGRESS`. Ao registrar resultado, o vencedor é **automaticamente promovido** para `fk_next_match_win_id`. Se não houver next match, o torneio é finalizado e o campeão salvo.

### Dashboard

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/dashboard` | `{ totalPlayers, matchesPlayed, upcomingMatches }` |