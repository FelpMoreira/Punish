package com.punish.Controller;

import java.util.List;

import com.punish.Model.Tournament;
import com.punish.Service.TournamentService;

import com.punish.Model.Match;
import com.punish.Model.Player;
import com.punish.Service.BracketService;
import com.punish.Service.MatchService;
import com.punish.Service.PlayerService;

import io.javalin.Javalin;

public class TournamentController {
    TournamentService tournamentService = new TournamentService();
    PlayerService playerService = new PlayerService();
    BracketService bracketService = new BracketService(new MatchService());

    public void tournamentRoutes(Javalin app){
        app.get("/tournaments", ctx -> {
            List<Tournament> tournaments = tournamentService.buscarTodosOsTorneios();
            ctx.json(tournaments);
        });

        app.get("/tournaments/{id}", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Tournament t = tournamentService.buscarPorId(id);
            ctx.status(201).json(t);
        });

        app.post("/tournaments", ctx -> {
            Tournament body = ctx.bodyAsClass(Tournament.class);
            Tournament t = tournamentService.criarTournament(
                body.getName(),
                body.getGame()
            );
            ctx.status(201).json(t);
        });

        app.put("/tournaments/{id}", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Tournament body = ctx.bodyAsClass(Tournament.class);
            Tournament t = tournamentService.atualizarTournament(
                id,
                body.getName(),
                body.getGame()
            );
            ctx.status(201).json(t);
        });

        app.post("/tournaments/{id}/start", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            tournamentService.start(id);
            ctx.status(204);
        });

        app.post("/tournaments/{id}/finish", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            tournamentService.finish(id);
            ctx.status(204);
        });

        app.post("/tournaments/{id}/generate", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            tournamentService.start(id);
            List<Player> players = playerService.buscarPlayersDoTournament(id);
            List<Match> matches = bracketService.gerarBracket(id, players);
            ctx.status(201).json(matches);
        });

        app.delete("/tournaments/{id}", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            tournamentService.deletar(id);
            ctx.status(204);
        });
    }
}
