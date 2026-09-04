-- ============================================================
-- Schema: Punish
-- Descrição: Sistema de gerenciamento de torneios de fighting games
-- Banco: PostgreSQL
-- ============================================================

-- Jogador
CREATE TABLE player (
    id       BIGSERIAL    PRIMARY KEY,
    nickname VARCHAR(50)  NOT NULL UNIQUE
);

-- Torneio
CREATE TABLE tournament (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    game       VARCHAR(50)  NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'CREATED',
    fk_winner_id          BIGINT      REFERENCES player(id),
    criado_em  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Relação N:N torneio-jogador
CREATE TABLE tournament_player (
    id                BIGSERIAL PRIMARY KEY,
    fk_tournament_id  BIGINT    NOT NULL REFERENCES tournament(id) ON DELETE CASCADE,
    fk_player_id      BIGINT    NOT NULL REFERENCES player(id) ON DELETE CASCADE,
    seed              INT,
    UNIQUE(fk_tournament_id, fk_player_id)
);

-- Partida
CREATE TABLE matches (
    id                    BIGSERIAL   PRIMARY KEY,
    fk_tournament_id      BIGINT      NOT NULL REFERENCES tournament(id) ON DELETE CASCADE,
    fk_player1_id         BIGINT      REFERENCES player(id),
    fk_player2_id         BIGINT      REFERENCES player(id),
    fk_winner_id          BIGINT      REFERENCES player(id),
    score_player1         INT,
    score_player2         INT,
    bracket_type          VARCHAR(20) NOT NULL,
    round_number          INT         NOT NULL,
    match_number          INT,
    fk_next_match_win_id  BIGINT      REFERENCES matches(id),
    fk_next_match_lose_id BIGINT      REFERENCES matches(id),
    status                VARCHAR(20) NOT NULL DEFAULT 'WAITING'
);

-- Convite (link do organizador)
CREATE TABLE tournament_invite (
    id               BIGSERIAL    PRIMARY KEY,
    fk_tournament_id BIGINT       NOT NULL REFERENCES tournament(id) ON DELETE CASCADE,
    codigo           VARCHAR(36)  NOT NULL UNIQUE,
    criado_em        TIMESTAMP    NOT NULL DEFAULT NOW(),
    expira_em        TIMESTAMP,
    usos_max         INT,
    usos             INT          NOT NULL DEFAULT 0
);


-- Pedido de entrada
CREATE TABLE tournament_request (
    id               BIGSERIAL    PRIMARY KEY,
    fk_tournament_id BIGINT       NOT NULL REFERENCES tournament(id) ON DELETE CASCADE,
    fk_player_id     BIGINT       NOT NULL REFERENCES player(id) ON DELETE CASCADE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    criado_em        TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(fk_tournament_id, fk_player_id)
);

CREATE TABLE refresh_token (
    id               BIGSERIAL    PRIMARY KEY,
    fk_player_id     BIGINT       NOT NULL REFERENCES player(id) ON DELETE CASCADE,
    token            VARCHAR(255) NOT NULL UNIQUE,
    criado_em        TIMESTAMP    NOT NULL DEFAULT NOW(),
    expira_em        TIMESTAMP    NOT NULL,
    revogado         BOOLEAN      NOT NULL DEFAULT FALSE
);

-- ============================================================
-- Índices
-- ============================================================
CREATE INDEX idx_tp_tournament ON tournament_player(fk_tournament_id);
CREATE INDEX idx_tp_player    ON tournament_player(fk_player_id);
CREATE INDEX idx_m_tournament ON matches(fk_tournament_id);
CREATE INDEX idx_m_player1    ON matches(fk_player1_id);
CREATE INDEX idx_m_player2    ON matches(fk_player2_id);
CREATE INDEX idx_m_winner     ON matches(fk_winner_id);
CREATE INDEX idx_m_next_win   ON matches(fk_next_match_win_id);
CREATE INDEX idx_m_next_lose  ON matches(fk_next_match_lose_id);
CREATE INDEX idx_inv_tournament ON tournament_invite(fk_tournament_id);
CREATE INDEX idx_req_tournament ON tournament_request(fk_tournament_id);
CREATE INDEX idx_req_player     ON tournament_request(fk_player_id);


-- ============================================================
-- Inserts opcionais (dados de exemplo)
-- ============================================================
INSERT INTO tournament (name, game) VALUES ('Tekken IFCE 2026', 'Tekken 7');
INSERT INTO player (nickname) VALUES ('Felp'), ('Leandro'), ('Isaias'), ('Pedro'),
                                     ('Joao'), ('Maria'), ('Jose'), ('Ana');

ALTER TABLE player ADD COLUMN IF NOT EXISTS email VARCHAR(100) UNIQUE;
ALTER TABLE player ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE player ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'PLAYER';
ALTER TABLE tournament ADD COLUMN IF NOT EXISTS fk_owner BIGINT REFERENCES player(id);