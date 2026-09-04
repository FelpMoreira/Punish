# Punish

Sistema de gerenciamento de torneios de jogos de luta — Single Elimination, autenticação JWT, e bracket automático.

**Punish** é um sistema (backend Java + frontend React) para criar e gerenciar torneios de fighting games (Tekken, Street Fighter, Guilty Gear, Smash, etc.) com chaveamento **Single Elimination**, registro de resultados, ranking, paginação e histórico de partidas.

**Repositórios**
- Backend: `Punish/` (este repositório)
- Frontend: `Punish-Web/` (React 19 + Vite + Tailwind v4)

## Features

- JWT Auth (cadastro/login + roles)
- Single Elimination com bracket automático
- Ranking e histórico de partidas
- Paginação e filtros de busca
- Convites por link e solicitações de entrada
- Dashboard com contagens

## Documentação

| Doc | Conteúdo |
|-----|----------|
| [Arquitetura](docs/ARCHITECTURE.md) | Estrutura do projeto, dependências, regras de negócio |
| [API](docs/API.md) | Endpoints e contratos |
| [Banco de dados](docs/DATABASE.md) | Modelo de dados |
| [Bracket](docs/BRACKET.md) | Algoritmo Single Elimination |
| [Autenticação](docs/AUTH.md) | JWT, roles e endpoints de auth |
| [Roadmap](docs/ROADMAP.md) | Fases do projeto |
| [Deploy](docs/DEPLOY.md) | Planejamento de publicação (futuro) |

## Como rodar

### Banco (PostgreSQL)

```bash
# Criar banco
createdb punish_db

# Aplicar schema (inclui ALTERs de auth e fk_owner)
psql -U felp -h localhost -d punish_db -f schema.sql
```

### Backend

```bash
mvn exec:java
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

## Testes

- `src/test/java/com/punish/Service/BracketServiceTest.java` — testes do bracket
- `src/test/java/com/punish/AppTest.java` — smoke test

```bash
mvn test
```

Frontend: [FelpMoreira/Punish-Web](https://github.com/FelpMoreira/Punish-Web)