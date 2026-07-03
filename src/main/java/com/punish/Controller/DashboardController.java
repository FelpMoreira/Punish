package com.punish.Controller;

import java.util.Map;

import com.punish.Repository.MatchRepository;
import com.punish.Repository.PlayerRepository;

import io.javalin.Javalin;

public class DashboardController {
    PlayerRepository playerRepository = new PlayerRepository();
    MatchRepository matchRepository = new MatchRepository();

    public void dashboardRoutes(Javalin app){
        app.get("/dashboard", ctx -> {
            long totalPlayers = playerRepository.contarPlayers();
            long matchesPlayed = matchRepository.contarPorStatus("FINISHED");
            long upcomingMatches = matchRepository.contarPorStatus("READY");
            ctx.json(Map.of(
                "totalPlayers", totalPlayers,
                "matchesPlayed", matchesPlayed,
                "upcomingMatches", upcomingMatches
            ));
        });
    }
}
