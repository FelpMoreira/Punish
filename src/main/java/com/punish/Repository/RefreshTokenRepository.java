package com.punish.Repository;

import java.sql.Timestamp;

import org.jdbi.v3.core.Jdbi;

import com.punish.Config.Database;
import com.punish.Model.RefreshToken;

public class RefreshTokenRepository {
    Jdbi jdbi = Database.getJdbi();

    public void salvar(Long playerId, String token, Timestamp expiraEm){
        jdbi.withHandle(handle -> 
            handle.createUpdate("INSERT INTO refresh_token (fk_player_id, token, expira_em) VALUES (:fk_player_id, :token, :expira_em)")
            .bind("fk_player_id", playerId)
            .bind("token", token)
            .bind("expira_em", expiraEm)
            .execute()
        );
    }

    public RefreshToken buscarPorToken(String token){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT * FROM refresh_token WHERE token = :token")
            .bind("token", token)
            .mapToBean(RefreshToken.class)
            .findOne()
            .orElse(null)
        );
    }

    public void revogar(String token) {
        jdbi.withHandle(handle -> 
            handle.createUpdate("UPDATE refresh_token SET revogado = TRUE WHERE token = :token")
            .bind("token", token)
            .execute()
        );
    }
}
