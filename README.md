# Punish 🎮🏆

> Sistema de gerenciamento de torneios de jogos de luta — Single Elimination, autenticação JWT, e bracket automático.

## Sobre

**Punish** é um sistema (backend Java + frontend React) para criar e gerenciar torneios de fighting games (Tekken, Street Fighter, Guilty Gear, Smash, etc.) com chaveamento **Single Elimination**, registro de resultados, ranking, paginação e histórico de partidas.


**Repositórios**
- Backend: `Punish/` (este repositório)
- Frontend: `Punish-Web/` (React 19 + Vite + Tailwind v4)

---

## 🏗️ Estrutura do Projeto

```
com.punish
├── App.java                      # Entrypoint: Javalin + CORS + exception handlers + rotas
├── Config/
│   ├── Database.java             # Singleton JDBI + HikariCP + mappers de enum
│   ├── JwtConfig.java            # Geração/validação de JWT (jjwt) com jwt.secret
│   └── TournamentStatusMapper.java
├── Controller/
│   ├── AuthController.java       # POST /auth/register, /auth/login
│   ├── DashboardController.java  # GET /dashboard (contagens)
│   ├── MatchController.java      # matches, histórico paginado, start, result, start-round
│   ├── PlayerController.java     # players (listar, stats, adicionar/remover de torneio)
│   └── TournamentController.java # CRUD torneio + generate + recalculate + ranking
├── Exception/
│   ├── ConflictException.java    # → HTTP 409
│   ├── NotFoundException.java    # → HTTP 404
│   └── ValidationException.java  # → HTTP 400
├── Middleware/
│   └── AuthMiddleware.java       # Valida JWT, rotas públicas/protegidas, role ORGANIZER/ADMIN
├── Model/
│   ├── Enums/
│   │   ├── BracketType.java      # WINNERS, LOSERS, GRAND_FINAL
│   │   ├── MatchStatus.java      # WAITING, READY, IN_PROGRESS, FINISHED
│   │   ├── TournamentStatus.java # CREATED, STARTED, FINISHED
│   │   └── UserRole.java         # PLAYER, ORGANIZER, ADMIN
│   ├── Match.java
│   ├── PaginaResult.java         # Envelope de paginação {data, total, page, size}
│   ├── Player.java               # id, nickname, email, password_hash (@JsonIgnore), role
│   ├── PlayerStats.java
│   ├── Ranking.java
│   ├── ResultadoRequest.java     # DTO corpo de POST /matches/{id}/result
│   ├── Tournament.java           # id, name, game, fk_winner_id, fk_owner, status, criado_em
│   └── TournamentPlayer.java
├── Repository/
│   ├── MatchRepository.java
│   ├── PlayerRepository.java
│   ├── TournamentPlayerRepository.java
│   └── TournamentRepository.java
└── Service/
    ├── AuthService.java          # register (BCrypt + validações) + login (JWT)
    ├── BracketService.java       # Geração automática do bracket
    ├── MatchService.java         # registrarResultado, iniciarPartida/Rodada, buscarHistorico
    ├── PlayerService.java
    ├── RankingService.java       # Cálculo de colocação (eliminação)
    └── TournamentService.java
```

---

## 📦 Dependências (`pom.xml`)

| Grupo | Artefato | Versão | Uso |
|-------|----------|--------|-----|
| `io.javalin` | `javalin` | 6.7.0 | HTTP framework |
| `com.fasterxml.jackson.core` | `jackson-databind` | 2.20.0 | JSON |
| `org.postgresql` | `postgresql` | 42.7.7 | Banco |
| `com.zaxxer` | `HikariCP` | 6.3.0 | Pool de conexões |
| `org.jdbi` | `jdbi3-core` | 3.45.0 | SQL/DB |
| `io.jsonwebtoken` | `jjwt` (impl+api+jackson) | 0.12.x | JWT |
| `org.mindrot` | `jbcrypt` | 0.4 | Hash de senha |
| `org.junit.jupiter` / `org.assertj` | — | 5.13.4 / 3.27.3 | Testes |

---

## 🚀 Executando

### Banco (PostgreSQL)

```bash
# Criar banco
createdb punish_db

# Aplicar schema (inclui ALTERs de auth e fk_owner)
psql -U felp -h localhost -d punish_db -f schema.sql
```

### Backend

```bash
mvn spring-boot:run   # ou mvn compile exec:java conforme setup
# Sobe em http://localhost:7000
```

Configuração em `src/main/resources/application.properties`:

```
db.url=jdbc:postgresql://localhost:5432/punish_db
db.user=felp
db.password=admin
jwt.secret=<chave-do-jwt>
```

**⚠️ `Database.getDataSource()` lê de `application.properties`** (não de env vars). O `jwt.secret` precisa ter tamanho suficiente para HMAC-SHA (>= 256 bits).

### Frontend

```bash
cd punish-frontend && npm install && npm run dev
```

---

## 🔐 Autenticação & Roles

- **User = Player** (mesma entidade `player`). O usuário cadastrado É um jogador.
- **Roles**: `PLAYER` (padrão), `ORGANIZER`, `ADMIN`.
- **Cadastro**: apenas um `nickname` + `email` + `password`. Senha ≥ 6 caracteres, hasheada com **BCrypt**.
- **Login**: `POST /auth/login` → retorna `{ "token": <JWT>, "email": ... }`. O token expira em **24h**.
- **Authorization**: header `Authorization: Bearer <token>`.
- **JWT claims**: `subject` = userId, `claim("role")` = role.
- **Rotas públicas**: `POST /auth/register`, `POST /auth/login`, e **todas as GET**.
- **Rotas protegidas**: qualquer outra. `needsOrganizer` bloqueia (403) usuários sem role `ORGANIZER`/`ADMIN` em rotas de escrita (em refinamento, ver roadmap).

### Endpoints de Auth

| Método | Rota | Corpo | Resposta |
|--------|------|-------|----------|
| POST | `/auth/register` | `{ nickname, email, password }` | `201` Player (sem password_hash) |
| POST | `/auth/login` | `{ email, password }` | `200` `{ token, email }` |

Erros: `400` Validação, `409` email/nickname duplicado, `401` credenciais inválidas / token ausente/inválido, `403` sem permissão.

---

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

---

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

---

## 🧠 Bracket (Single Elimination)

`BracketService.gerarBracket(tournamentId, players)`:

1. **Shuffle** nos players (sem seed na geração atual)
2. Calcula próxima potência de 2 (4, 8, 16...) como tamanho do bracket
3. Insere **byes** (slots `null`) alternadamente
4. Cria todas as partidas (rodadas de 1 a N), `bracket_type=WINNERS`, status `WAITING`
5. Conecta `fk_next_match_win_id` (partida `p` da rodada `r` → partida `p/2` da rodada `r+1`)
6. Preenche primeira rodada; partidas com bye viram `FINISHED` e promovem o adversário
7. Marca como `READY` as partidas seguintes que já têm 2 players

O avanço automático após resultado é feito pelo `MatchService.registrarResultado` (promove winner pro `fk_next_match_win_id`, e finaliza o torneio na última partida).

**Limite atual**: máxima de **16 jogadores** por torneio (validação em `PlayerService.adicionarAoTournament`).

---

## 📜 Regras de Negócio

- Não pode adicionar player a torneio não `CREATED`; torneio cheio no 16º player
- Não pode remover player de torneio não `CREATED`
- `start` só em `CREATED`; `finish` só em `STARTED`
- Torneio `FINISHED` não pode ser editado nem recalculado
- Não pode gerar bracket com torneio errado / partidas sem 2 jogadores rodando
- `registrarResultado`: winner deve ser player1/player2; placar não-negativo; partida deve estar `READY` ou `IN_PROGRESS`
- `iniciarPartida`: partida `READY` + 2 jogadores
- Cadastro: email e nickname únicos; senha ≥ 6 caracteres
- Atualização direta de `status` em `TournamentService.atualizarStatus` não valida transições (usado internamente)

---

## 🧪 Testes

- `src/test/java/com/punish/Service/BracketServiceTest.java` — testes do bracket
- `src/test/java/com/punish/AppTest.java` — smoke test

```bash
mvn test
```

---

## 🗺️ Roadmap

| # | Fase | Status |
|---|------|--------|
| 1 | Histórico de partidas (GET /matches + paginação + RANKING) | ✅ feito |
| 2 | Auth com JWT + Roles | 🔄 em progresso |
| 3 | Convites (link do organizador + solicitações) | ⏳ |
| 4 | Round Robin | ⏳ |
| 5 | Sistema Suíço | ⏳ |
| 6 | Pool Stage + Bracket | ⏳ |
| 7 | Double Elimination | ⏳ |

**Passo em andamento — 4.2 `fk_owner` + permissões**: ver `PASSO_4.2.md` na raiz para o plano detalhado (adicionar dono ao torneio, `verificarDono`, refinar `AuthMiddleware.needsOrganizer`).

---

Frontend: [FelpMoreira/Punish-Web](https://github.com/FelpMoreira/Punish-Web)