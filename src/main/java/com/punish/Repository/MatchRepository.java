package com.punish.Repository;

import java.util.List;

import org.jdbi.v3.core.Jdbi;

import com.punish.Config.Database;
import com.punish.Model.Match;

public class MatchRepository {
    
    Jdbi jdbi = Database.getJdbi();

    public Match criar(Match match){
        Long id = jdbi.withHandle(handle -> 
            handle.createUpdate("""
                INSERT INTO matches (fk_tournament_id, fk_player1_id, fk_player2_id, bracket_type, round_number, match_number, fk_next_match_win_id, fk_next_match_lose_id, status) VALUES (:fk_tournament_id, :fk_player1_id, :fk_player2_id, :bracket_type, :round_number, :match_number, :fk_next_match_win_id, :fk_next_match_lose_id, :status)
            """)
            .bindBean(match)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .findOne()
            .orElse(null)
        );
        return buscarPorId(id);
    }

    public Match buscarPorId(long id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT * FROM matches WHERE id = :id")
            .bind("id", id)
            .mapToBean(Match.class)
            .findOne()
            .orElse(null)
        );
    }

    public List<Match> buscarPorTournament(long fk_tournament_id){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT * FROM matches WHERE fk_tournament_id = :tid")
            .bind("tid", fk_tournament_id)
            .mapToBean(Match.class)
            .list()
        );
    }

    public List<Match> buscarComFiltros(Long playerId, Long tournamentId, int page, int size) {
        int offset = (page - 1) * size;
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                SELECT * FROM matches WHERE 1=1
                AND (:playerId IS NULL OR fk_player1_id = :playerId OR fk_player2_id = :playerId)
                AND (:tournamentId IS NULL OR fk_tournament_id = :tournamentId)
                LIMIT :size OFFSET :offset
            """)
            .bind("playerId", playerId)
            .bind("tournamentId", tournamentId)
            .bind("size", size)
            .bind("offset", offset)
            .mapToBean(Match.class)
            .list()
        );
    }

    public long contarTotal(long playerId, long tournamentId){
        return jdbi.withHandle(handle -> 
            handle.createQuery("""
                SELECT COUNT(*) FROM matches WHERE 1=1
                AND (:playerId IS NULL OR fk_player1_id = :playerId OR fk_player2_id = :playerId)
                AND (:tournamentId IS NULL OR fk_tournament_id = :tournamentId)
            """)
            .bind("playerId", playerId)
            .bind("tournamentId", tournamentId)
            .mapTo(Long.class)
            .findOne()
            .orElse(0L)
        );
    }

    public void deletarPorTournament(Long fk_tournament_id){
        jdbi.withHandle(handle ->
            handle.createUpdate("DELETE FROM matches WHERE fk_tournament_id = :tid")
            .bind("tid", fk_tournament_id)
            .execute()
        );
    }

    public void atualizarVencedor(long id, Long fk_winner_id, Integer score_player1, Integer score_player2){
        jdbi.withHandle(handle -> 
            handle.createUpdate("UPDATE matches SET fk_winner_id = :winnerId, score_player1 = :score1, score_player2 = :score2, status = 'FINISHED' WHERE id = :id")
            .bind("winnerId", fk_winner_id)
            .bind("score1", score_player1)
            .bind("score2", score_player2)
            .bind("id", id)
            .execute()
        );
    }

    public void atualizarNextMatchWin(Long fk_next_match_win_id, Long id){
        jdbi.withHandle(handle -> 
            handle.createUpdate("UPDATE matches SET fk_next_match_win_id = :fk_next_match_win_id WHERE id = :id")
            .bind("fk_next_match_win_id", fk_next_match_win_id)
            .bind("id", id)
            .execute()
        );
    }

    public void atualizarStatus(String status, Long id){
        jdbi.withHandle(handle -> 
            handle.createUpdate("UPDATE matches SET status = :status WHERE id = :id")
            .bind("status", status)
            .bind("id", id)
            .execute()
        );
    }

    public long contarPorStatus(String status){
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM matches WHERE status = :status")
                .bind("status", status)
                .mapTo(Long.class)
                .findOne()
                .orElse(0L)
        );
    }

    public void atualizarPlayer1(long id, Long fk_player1_id){
        jdbi.withHandle(handle ->
            handle.createUpdate("UPDATE matches SET fk_player1_id = :pid1 WHERE id = :id")
            .bind("pid1", fk_player1_id)
            .bind("id", id)
            .execute()
        );
    }

    public void atualizarPlayer2(long id, Long fk_player2_id){
        jdbi.withHandle(handle ->
            handle.createUpdate("UPDATE matches SET fk_player2_id = :pid2 WHERE id = :id")
            .bind("pid2", fk_player2_id)
            .bind("id", id)
            .execute()
        );
    }
}
