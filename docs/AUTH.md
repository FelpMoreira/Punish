## 🔐 Autenticação & Roles

- **User = Player** (mesma entidade `player`). O usuário cadastrado É um jogador.
- **Roles**: `PLAYER` (padrão), `ORGANIZER`, `ADMIN`.
- **Cadastro**: apenas um `nickname` + `email` + `password`. Senha ≥ 6 caracteres, hasheada com **BCrypt**.
- **Login**: `POST /auth/login` → retorna `{ "token": <JWT>, "refreshToken": <uuid>, "email": ... }`. O access token é um JWT e expira em **24h**; o refresh token é um UUID persistido no banco (`refresh_token`) por **7 dias**.
- **Refresh**: `POST /auth/refresh` troca um refresh válido por um novo par (rotação: o token usado é revogado a cada uso).
- **Logout**: `POST /auth/logout` revoga o refresh token no servidor antes de limpar o cliente.
- **Authorization**: header `Authorization: Bearer <token>`.
- **JWT claims**: `subject` = userId, `claim("role")` = role.
- **Rotas públicas**: `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, e **todas as GET**.
- **Rotas protegidas**: qualquer outra. `needsOrganizer` bloqueia (403) usuários sem role `ORGANIZER`/`ADMIN` em rotas de escrita.

### Endpoints de Auth

| Método | Rota | Corpo | Resposta |
|--------|------|-------|----------|
| POST | `/auth/register` | `{ nickname, email, password }` | `201` Player (sem password_hash) |
| POST | `/auth/login` | `{ email, password }` | `200` `{ token, refreshToken, email }` |
| POST | `/auth/refresh` | `{ refreshToken }` | `200` `{ token, refreshToken }` |
| POST | `/auth/logout` | `{ refreshToken }` | `204` |

Erros: `400` Validação, `409` email/nickname duplicado, `401` credenciais inválidas / token ausente/inválido / refresh inválido, `403` sem permissão.