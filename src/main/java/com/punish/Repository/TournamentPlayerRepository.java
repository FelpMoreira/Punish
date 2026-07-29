package com.punish.Repository;

import java.util.List;

import org.jdbi.v3.core.Jdbi;

import com.punish.Config.Database;
import com.punish.Model.Player;
import com.punish.Model.TournamentPlayer;

public class TournamentPlayerRepository {
    Jdbi jdbi = Database.getJdbi();
    
    public TournamentPlayer criarTournamentPlayer(Long fk_tournament_id, Long fk_player_id){
        Long id = jdbi.withHandle(handle -> {
            return handle.createUpdate("INSERT INTO tournament_player (fk_tournament_id, fk_player_id) VALUES (:fk_tournament_id, :fk_player_id)")
                .bind("fk_tournament_id", fk_tournament_id)
                .bind("fk_player_id", fk_player_id)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .findOne()
                .orElse(null);
        });
        return buscarPorId(id);
    }

    public void deletarTournamentPlayer(Long fk_tournament_id, Long fk_player_id){
        jdbi.withHandle(handle -> {
            return handle.createUpdate("DELETE FROM tournament_player WHERE fk_tournament_id = :fk_tournament_id AND fk_player_id = :fk_player_id")
            .bind("fk_tournament_id", fk_tournament_id)
            .bind("fk_player_id", fk_player_id)
            .execute();
        });
    }

    public TournamentPlayer buscarPorId(Long id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT * FROM tournament_player WHERE id = :id")
            .bind("id", id)
            .mapToBean(TournamentPlayer.class)
            .findOne()
            .orElse(null)
        );
    }

    public List<TournamentPlayer> buscarPlayerPorTournament(Long fk_tournament_id){
        return jdbi.withHandle(handle -> {
            return handle.createQuery("SELECT * FROM tournament_player WHERE fk_tournament_id = :fk_tournament_id")
            .bind("fk_tournament_id", fk_tournament_id)
            .mapToBean(TournamentPlayer.class)
            .list();
        });
    }

    public boolean existe(long fk_tournament_id, long fk_player_id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT COUNT(*) > 0  FROM tournament_player WHERE fk_tournament_id = :tid AND fk_player_id = :pid")
                .bind("tid", fk_tournament_id)
                .bind("pid", fk_player_id)
                .mapTo(Boolean.class)
                .findOne()
                .orElse(false)
        );
    }

    public List<Player> buscarPlayerDoTournament(Long fk_tournament_id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT p.id, p.nickname FROM player p INNER JOIN tournament_player tp ON p.id = tp.fk_player_id WHERE tp.fk_tournament_id = :tid")
            .bind("tid", fk_tournament_id)
            .mapToBean(Player.class)
            .list()
        );
    }

    public int contarPorTournament(long fk_tournament_id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT COUNT(*) FROM tournament_player WHERE fk_tournament_id = :tid")
            .bind("tid", fk_tournament_id)
            .mapTo(Integer.class)
            .findOne()
            .orElse(0)
        );
    }

    public int contarPorPlayer(Long fk_player_id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT COUNT(*) FROM tournament_player WHERE fk_player_id = :pid")
            .bind("pid", fk_player_id)
            .mapTo(Integer.class)
            .findOne()
            .orElse(0)
        );
    }

    public void atualizarSeed(long fk_tournament_id, long fk_player_id, int seed){
        jdbi.withHandle(handle -> 
            handle.createUpdate("UPDATE tournament_player SET seed = :seed WHERE fk_tournament_id = :tid AND fk_player_id = :pid")
            .bind("tid", fk_tournament_id)
            .bind("pid", fk_player_id)
            .bind("seed", seed)
            .execute()
        );
    }

}