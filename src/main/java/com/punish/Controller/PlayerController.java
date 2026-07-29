package com.punish.Controller;

import java.util.List;
import java.util.Map;

import com.punish.Model.Player;
import com.punish.Model.PlayerStats;
import com.punish.Service.PlayerService;

import io.javalin.Javalin;

public class PlayerController {
    PlayerService playerService = new PlayerService();

    public void playerRoutes(Javalin app){
        app.get("/players", ctx -> {
            String nickname = ctx.pathParam("nickname");
            List<Player> players = playerService.buscarNicknameComFiltros(nickname);
            ctx.json(players);
        });

        app.post("/players", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            Player p = playerService.criarPlayer(body.get("nickname"));
            ctx.status(201).json(p);
        });

        app.get("/players/{id}", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Player p = playerService.buscarPorId(id);
            ctx.json(p);
        });

        app.get("/players/{id}/stats", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            PlayerStats stats = playerService.buscarStats(id);
            ctx.json(stats);
        });

        app.post("/tournaments/{id}/players", ctx -> {
            Long tournament_id = Long.parseLong(ctx.pathParam("id"));
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Long playerId = ((Number) body.get("playerId")).longValue();
            playerService.adicionarAoTournament(tournament_id, playerId);
            ctx.status(204);
        });

        app.get("/tournaments/{id}/players", ctx -> {
            Long tournament_id = Long.parseLong(ctx.pathParam("id"));
            List<Player> p = playerService.buscarPlayersDoTournament(tournament_id);
            ctx.json(p);
        });

        app.delete("/tournaments/{id}/players/{player_id}", ctx -> {
            Long tournament_id = Long.parseLong(ctx.pathParam("id"));
            Long player_id = Long.parseLong(ctx.pathParam("player_id"));
            playerService.removerPlayersDoTournament(tournament_id, player_id);
            ctx.status(204);
        });

        app.delete("/players/{id}", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            playerService.deletarPlayer(id);
            ctx.status(204);
        });
    }
}
