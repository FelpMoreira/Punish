package com.punish.Repository;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.core.Jdbi;

import com.punish.Config.Database;
import com.punish.Model.Player;

public class PlayerRepository {
    Jdbi jdbi = Database.getJdbi();
    public Player criarPlayer(String nickname){
        Long id = jdbi.withHandle(handle -> {
            return handle.createUpdate("INSERT INTO player (nickname) VALUES (:nickname)")
            .bind("nickname", nickname)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Long.class)
            .findOne()
            .orElse(null);
        });
        return buscarPorId(id);
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

    public void deletar(Long id){
        jdbi.withHandle(handle ->
            handle.createUpdate("DELETE FROM player WHERE id = :id")
            .bind("id", id)
            .execute()
        );
    }
}
