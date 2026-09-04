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