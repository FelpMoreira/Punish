package com.punish.Controller;

import java.util.List;

import com.punish.Model.Match;
import com.punish.Model.ResultadoRequest;
import com.punish.Service.MatchService;

import io.javalin.Javalin;

public class MatchController {
    MatchService matchService = new MatchService();

    public void matchRoutes(Javalin app){
        app.get("/tournaments/{id}/matches", ctx -> {
            Long tournament_id = Long.parseLong(ctx.pathParam("id"));
            List<Match> m = matchService.buscarPorTournament(tournament_id);
            ctx.json(m);
        });

        app.get("/matches/{id}", ctx -> {
            Long match_id = Long.parseLong(ctx.pathParam("id"));
            Match m = matchService.buscarPorId(match_id);
            ctx.json(m);
        });

        app.patch("/matches/{id}/result", ctx -> {
            Long match_id = Long.parseLong(ctx.pathParam("id"));
            ResultadoRequest body = ctx.bodyAsClass(ResultadoRequest.class);
            Match matchAtualizada = matchService.registrarResultado(match_id, body.fk_winner_id(), body.score_player1(), body.score_player2());
            ctx.json(matchAtualizada);
        });

        app.patch("/matches/{id}/start", ctx -> {
            Long match_id = Long.parseLong(ctx.pathParam("id"));
            Match m = matchService.iniciarPartida(match_id);
            ctx.json(m);
        });
    }
}
