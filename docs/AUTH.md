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