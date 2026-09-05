package com.punish.Repository;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.core.Jdbi;

import com.punish.Config.Database;
import com.punish.Model.Player;

public class PlayerRepository {
    Jdbi jdbi = Database.getJdbi();
    public Player criarPlayer(String nickname, String email, String password_hash){
        Long id = jdbi.withHandle(handle -> {
            return handle.createUpdate("INSERT INTO player (nickname, email, password_hash) VALUES (:nickname, :email, :password_hash)")
            .bind("nickname", nickname)
            .bind("email", email)
            .bind("password_hash", password_hash)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .findOne()
            .orElse(null);
        });
        return buscarPorId(id);
    }

    public List<Player> listarTodos(){
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT * FROM player ORDER BY nickname")
            .mapToBean(Player.class)
            .list()
        ) ;
    }

    public void atualizarRole(Long id, String role){
        jdbi.withHandle(handle ->
            handle.createUpdate("UPDATE player SET role = :role WHERE id = :id")
            .bind("role", role)
            .bind("id", id)
            .execute()
        );
    }

    public Player buscarPorId(Long id){
        Player player = jdbi.withHandle(handle -> {
            Optional<Player> result = handle.createQuery("SELECT * FROM player WHERE id = :id")
            .bind("id", id)
            .mapToBean(Player.class)
            .findOne();
            return result.orElse(null);
        });
        return player;
    }

    public Player buscarPorEmail(String email) {
        return jdbi.withHandle(handle -> {
            Optional<Player> result = handle.createQuery("SELECT * FROM player WHERE email = :email")
            .bind("email", email)
            .mapToBean(Player.class)
            .findOne();
            return result.orElse(null); 
        });
    }

    public List<Player> buscarComPaginacao(String nickname, int page, int size){
        int offset = (page - 1) * size;
        return jdbi.withHandle(handle -> {
            return handle.createQuery("""
                SELECT * FROM player
                WHERE (:nickname IS NULL OR :nickname ILIKE '%' || :nickname || '%')
                LIMIT :size OFFSET :offset
            """)
            .bind("nickname", nickname)
            .bind("size", size)
            .bind("offset", offset)
            .mapToBean(Player.class)
            .list();
        });
    }

    public long contarTotal(String nickname){
        return jdbi.withHandle(handle -> 
            handle.createQuery("""
                SELECT COUNT(*) FROM player
                WHERE (:nickname IS NULL OR :nickname ILIKE '%' || :nickname || '%')
            """)
            .bind("nickname", nickname)
            .mapTo(Long.class)
            .findOne()
            .orElse(0L)
        );
    }

    public List<Player> buscarPorNicknameLike(String nickname){
        return jdbi.withHandle(handle -> {
            return handle.createQuery("SELECT * FROM player WHERE nickname ILIKE '%' || :nickname || '%'")
            .bind("nickname", nickname)
            .mapToBean(Player.class)
            .list();
        });
    }

    public List<Player> buscarPorNickname(String nickname){
        return jdbi.withHandle(handle -> {
            return handle.createQuery("SELECT * FROM player WHERE nickname = :nickname")
                .bind("nickname", nickname)
                .mapToBean(Player.class)
                .list();
        });
    }

    public List<Player> buscarTodosOsPlayers(){
        return jdbi.withHandle(handle -> {
            return handle.createQuery("SELECT * FROM player")
            .mapToBean(Player.class)
            .list();
        });
    }

    public long contarPlayers(){
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT COUNT(*) FROM player")
                .mapTo(Long.class)
                .findOne()
                .orElse(0L)
        );
    }

    public long contarVitorias(Long playerId){
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT COUNT(*) FROM matches WHERE fk_winner_id = :pid")
            .bind("pid", playerId)
            .mapTo(Long.class)
            .findOne()
            .orElse(0l)
        );
    }

    public long contarPartidas(Long playerId){
        return jdbi.withHandle(handle -> 
            handle.createQuery("""
                    SELECT COUNT(*) FROM matches
                    WHERE (fk_player1_id = :pid OR fk_player2_id = :pid)
                    AND fk_winner_id IS NOT NULL
                    """)
                .bind("pid", playerId)
                .mapTo(Long.class)
                .findOne()
                .orElse(0L)
        );
    }

    public void deletar(Long id){
        jdbi.withHandle(handle ->
            handle.createUpdate("DELETE FROM player WHERE id = :id")
            .bind("id", id)
            .execute()
        );
    }
}
