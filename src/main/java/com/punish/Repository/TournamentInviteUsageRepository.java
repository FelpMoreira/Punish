package com.punish.Repository;

import java.util.List;

import org.jdbi.v3.core.Jdbi;

import com.punish.Config.Database;
import com.punish.Model.TournamentInviteUsage;

public class TournamentInviteUsageRepository {
    Jdbi jdbi = Database.getJdbi();

    public void salvar(Long inviteId, Long playerId){
        jdbi.withHandle(handle -> 
            handle.createUpdate("INSERT INTO tournament_invite_usage (fk_invite_id, fk_player_id) VALUES (:fk_invite_id, :fk_player_id)")
            .bind("fk_player_id", playerId)
            .bind("inviteId", inviteId)
            .execute()
        );
    }

    public List<TournamentInviteUsage> buscarPorInvite(Long inviteId){
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM tournament_invite_usage WHERE fk_invite_id = :fk_invite_id ORDER BY usado_em DESC")
            .bind("fk_invite_id", inviteId)
            .mapToBean(TournamentInviteUsage.class)
            .list()
        );
    }
}
