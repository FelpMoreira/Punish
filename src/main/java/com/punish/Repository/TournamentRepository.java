package com.punish.Repository;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.core.Jdbi;

import com.punish.Config.Database;
import com.punish.Model.Tournament;

public class TournamentRepository {
    Jdbi jdbi = Database.getJdbi();
    public Tournament criarTournament(String name, String game){ 
        Long id = jdbi.withHandle(handle -> {
            return handle.createUpdate("""
                INSERT INTO tournament (name, game) VALUES (:name, :game)
            """)
            .bind("name", name)
            .bind("game", game)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .findOne()
            .orElse(null);
        });
        return buscarPorId(id);
    }
    
    public List<Tournament> buscarTodosOsTorneios(){
        return jdbi.withHandle(handle -> {
            return handle.createQuery("SELECT * FROM tournament")
            .mapToBean(Tournament.class)
            .list();
        });
    }

    public Tournament buscarPorId(Long id){
        Tournament tournament = jdbi.withHandle(handle -> {
            Optional<Tournament> result = handle.createQuery("SELECT * FROM tournament WHERE id = :id")
            .bind("id", id)
            .mapToBean(Tournament.class)
            .findOne();
            return result.orElse(null);
        });
        return tournament;
    }

    public List<Tournament> buscarComFiltros(String name, String game, String status){
        return jdbi.withHandle(handle -> {
            return handle.createQuery(("""
            SELECT * FROM tournament WHERE 1=1
            AND (:name IS NULL OR name ILIKE '%' || :name || '%')
            AND (:game IS NULL OR game ILIKE '%' || :game || '%')
            AND (:status IS NULL OR status = :status::tournament_status)
            """))
            .bind("name", name)
            .bind("game", game)
            .bind("status", status)
            .mapToBean(Tournament.class)
            .list();
        });
    }

    public List<Tournament> buscarComPaginacao(String name, String game, String status, int page, int size){
        int offset = (page - 1) * size;
        return jdbi.withHandle(handle -> {
            return handle.createQuery("""
                SELECT * FROM tournament WHERE 1=1
                AND (:name IS NULL OR :name ILIKE '%' || :name || '%')
                AND (:game IS NULL OR :game ILIKE '%' || :game || '%')
                AND (:status IS NULL OR status = :status)
                LIMIT :size OFFSET :offset
            """)
            .bind("name", name)
            .bind("game", game)
            .bind("status", status)
            .bind("size", size)
            .bind("offset", offset)
            .mapToBean(Tournament.class)
            .list();
        });
    }

    public long contarTotal(String name, String game, String status){
        return jdbi.withHandle(handle ->
            handle.createQuery("""
                    SELECT COUNT (*) FROM tournament WHERE 1=1
                    AND (:name IS NULL OR :name ILIKE '%' || :name || '%')
                    AND (:game IS NULL OR :game ILIKE '%' || :game || '%')
                    AND (:status IS NULL OR status = :status)
                    """)
                .bind("name", name)
                .bind("game", game)
                .bind("status", status)
                .mapTo(Long.class)
                .findOne()
                .orElse(0L)
        );
    }

    public List<Tournament> buscarPorNome(String name){
        return jdbi.withHandle(handle -> {
            return handle.createQuery("SELECT * FROM tournament WHERE name = :name")
                .bind("name", name)
                .mapToBean(Tournament.class)
                .list();
        });
    }

    public Tournament atualizarCampeao(Long id, Long fk_winner_id){
        jdbi.withHandle(handle -> {
            return handle.createUpdate("UPDATE tournament SET fk_winner_id = :fk_winner_id WHERE id = :id")
            .bind("fk_winner_id", fk_winner_id)
            .bind("id", id)
            .execute();
        });
        Tournament tournament = buscarPorId(id);
        return tournament;
    }

    public Tournament atualizarTournament(Long id, String name, String game){
        jdbi.withHandle(handle -> {
            return handle.createUpdate("UPDATE tournament SET name = :name, game = :game WHERE id = :id")
            .bind("name", name)
            .bind("game", game)
            .bind("id", id)
            .execute();
        });
        Tournament tournament = buscarPorId(id);
        return tournament;
    }

    public Tournament atualizarStatus(Long id, String status){
        jdbi.withHandle(handle -> {
            return handle.createUpdate("UPDATE tournament SET status = :status WHERE id = :id")
            .bind("status", status)
            .bind("id", id)
            .execute();
        });
        Tournament tournament = buscarPorId(id);
        return tournament;
    }

    public void limparCampeao(Long id){
        jdbi.withHandle(handle -> 
            handle.createUpdate("UPDATE tournament SET fk_winner_id = NULL WHERE id = :id")
            .bind("id", id)
            .execute()
        );
    }

    public void deletar(Long id){
        jdbi.withHandle(handle -> {
            return handle.createUpdate("DELETE FROM tournament WHERE id = :id")
            .bind("id", id)
            .execute();
        });
    }
}
