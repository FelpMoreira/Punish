package com.punish.Repository;

import java.sql.Timestamp;
import java.util.List;

import org.jdbi.v3.core.Jdbi;

import com.punish.Config.Database;
import com.punish.Model.TournamentInvite;

public class TournamentInviteRepository {
    Jdbi jdbi = Database.getJdbi();

    public TournamentInvite criar(Long fk_tounament_id, String codigo, Timestamp expira_em, Integer usos_max) {
        Long id = jdbi.withHandle(handle -> {
            return handle.createUpdate("INSERT INTO tournament_invite (fk_tournament_id, codigo, expira_em, usos_max) VALUES (:fk_tournament_id, :codigo, :expira_em, :usos_max)")
            .bind("fk_tournament_id", fk_tounament_id)
            .bind("codigo", codigo)
            .bind("expira_em", expira_em)
            .bind("usos_max", usos_max)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .findOne()
            .orElse(null); 
        });
        return buscarPorId(id);
    }

    public TournamentInvite buscarPorId(Long id){
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM tournament_invite WHERE id = :id")
            .bind("id", id)
            .mapToBean(TournamentInvite.class)
            .findOne()
            .orElse(null)
        );
    }

    public TournamentInvite buscarPorCodigo(String codigo) {
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT * FROM tournament_invite WHERE codigo = :codigo")
            .bind("codigo", codigo)
            .mapToBean(TournamentInvite.class)
            .findOne()
            .orElse(null)
        );
    }

    public List<TournamentInvite> buscarPorTournament(Long fk_tournament_id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT * FROM tournament_invite WHERE fk_tournament_id = :fk_tournament_id ORDER BY criado_em DESC")
            .bind("fk_tournament_id", fk_tournament_id)
            .mapToBean(TournamentInvite.class)
            .list()
        );
    }

    public void incrementarUsos(Long id){
        jdbi.withHandle(handle -> 
            handle.createUpdate("UPDATE tournament_invite SET usos = usos + 1 WHERE id = :id")
            .bind("id", id)
            .execute()
        );
    }

    public void deletar(Long id){
        jdbi.withHandle(handle -> 
            handle.createUpdate("DELETE FROM tournament_invite WHERE id = :id")
            .bind("id", id)
            .execute()
        );
    }
}
