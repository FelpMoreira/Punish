## 📡 API — Endpoints

Base: `https://punish.fly.dev` — autenticação via header `Authorization: Bearer <token>` (exceto rotas públicas).
Referência usada pelo app mobile (APK).

### Autenticação

| Método | Rota | Corpo/Descrição | Resposta |
|--------|------|-----------------|----------|
| POST | `/auth/register` | `{ nickname, email, password }` → `201` | Player |
| POST | `/auth/login` | `{ email, password }` | `{ token, refreshToken, email }` |
| POST | `/auth/refresh` | `{ refreshToken }` → renova o par | `{ token, refreshToken }` |
| POST | `/auth/logout` | `{ refreshToken }` → revoga → `204` | — |

> O `token` (JWT) carrega a `role` do player — após mudança de role é preciso **relogar**.
> Rotas públicas: `register`, `login`, `refresh`, `logout`.

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
| GET | `/dashboard` | `{ totalPlayers, matchesPlayed, upcomingMatches }` |

### Players

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/players` | Listar. Query: `nickname`. Paginação: `page`+`size` |
| GET | `/players/{id}` | Buscar |
| GET | `/players/{id}/stats` | Stats do player (torneios/vitórias/derrotas/winrate) |
| POST | `/tournaments/{id}/players` | Adicionar player `{ playerId }` → `204` |
| GET | `/tournaments/{id}/players` | Players do torneio |
| DELETE | `/tournaments/{id}/players/{playerId}` | Remover player do torneio → `204` |
| DELETE | `/players/{id}` | Deletar player → `204` (exige ORGANIZER/ADMIN) |

> `POST /players` foi **removido** — cadastro agora é via `/auth/register`.

### Admin (somente ADMIN)

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/admin/players` | Listar todos os usuários → `[{ id, nickname, email, role }]` |
| PUT | `/admin/players/{id}/role` | Mudar role. Body: `{ "role": "PLAYER"\|"ORGANIZER"\|"ADMIN" }` → `204` |

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

### Invites (convites & pedidos)

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/tournaments/{id}/invite` | Criar convite `{ expiraEm?, usosMax? }` → `201` |
| GET | `/tournaments/{id}/invite` | Listar convites + usos detalhados (dono) |
| DELETE | `/tournaments/{id}/invite/{invite_id}` | Revogar convite → `204` |
| GET | `/invites/{codigo}` | Info pública de um convite → `{ codigo, usosMax, usos, expiraEm, tournamentId, tournamentName }` |
| POST | `/invites/{codigo}/join` | Entrar no torneio via link (qualquer logado) → `204` |
| POST | `/tournaments/{id}/requests` | Solicitar entrada → `201` |
| GET | `/tournaments/{id}/requests` | Listar pedidos (dono) |
| POST | `/tournaments/{id}/requests/{player_id}/accept` | Aceitar pedido → `204` |
| POST | `/tournaments/{id}/requests/{player_id}/reject` | Rejeitar pedido → `204` |
