package com.punish.Repository;

import java.util.List;

import org.jdbi.v3.core.Jdbi;

import com.punish.Config.Database;
import com.punish.Model.TournamentRequest;

public class TournamentRequestRepository {
    Jdbi jdbi = Database.getJdbi();

    public TournamentRequest criar(Long fk_tournament_id, Long fk_player_id){
        jdbi.withHandle(handle -> {
            return handle.createUpdate("INSERT INTO tournament_request (fk_tournament_id, fk_player_id) VALUES (:fk_tournament_id, :fk_player_id)")
                .bind("fk_tournament_id", fk_tournament_id)
                .bind("fk_player_id", fk_player_id)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .findOne()
                .orElse(null);
        });
        return buscar(fk_tournament_id, fk_player_id);
    }

    public TournamentRequest buscar(Long fk_tournament_id, Long fk_player_id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT * FROM tournament_request WHERE fk_tournament_id = :fk_tournament_id AND fk_player_id = :fk_player_id")
                .bind("fk_tournament_id", fk_tournament_id)
                .bind("fk_player_id", fk_player_id)
                .mapToBean(TournamentRequest.class)
                .findOne()
                .orElse(null)
        );
    }

    public List<TournamentRequest> buscarPorTournament(Long fk_tournament_id, String status){
        String sql = "SELECT * FROM tournament_request WHERE fk_tournament_id = :fk_tournament_id";
        if (status != null) sql += " AND status = :status";
        String finalSql = sql;
        return jdbi.withHandle(handle -> {
            var query = handle.createQuery(finalSql)
                .bind("fk_tournament_id", fk_tournament_id);
            if(status != null) query.bind("status", status);
            return query.mapToBean(TournamentRequest.class).list();
        });
    }

    public void atualizarStatus(Long id, String status){
        jdbi.withHandle(handle ->
            handle.createUpdate("UPDATE tournament_request")
            .bind("id", id)
            .bind("status", status)
            .execute()
        );
    }

    public boolean exite(Long fk_tournament_id, Long fk_player_id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT COUNT(*) > 0 FROM tournament_request WHERE fk_tournament_id = :tid AND fk_player_id = :pid")
            .bind("tid", fk_tournament_id)
            .bind("pid", fk_player_id)
            .mapTo(Boolean.class)
            .findOne()
            .orElse(null)
        );
    }
}
