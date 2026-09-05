package com.punish.Controller;

import java.util.List;
import java.util.Map;

import com.punish.Model.Player;
import com.punish.Repository.PlayerRepository;

import io.javalin.Javalin;

public class AdminController {
    PlayerRepository playerRepository = new PlayerRepository();

    public void adminRoutes(Javalin app){
        app.get("/admin/player", ctx -> {
            List<Player> players = playerRepository.listarTodos();
            ctx.json(players);
        });

        app.put("/admin/players/{id}/role", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String role = (String) body.get("role");

            if (role == null || role.isBlank()) {
                ctx.status(400).json(Map.of("error", "Role inválida"));
                return;
            }
            Player p = playerRepository.buscarPorId(id);
            if (p == null) {
                ctx.status(404).json(Map.of("error", "Jogador não encontrado"));
                return;
            }
            playerRepository.atualiazrRole(id, role);
            ctx.status(204);
        });
    }
}
