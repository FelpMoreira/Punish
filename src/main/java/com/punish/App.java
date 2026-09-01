package com.punish;

import java.util.Map;

import com.punish.Controller.AuthController;
import com.punish.Controller.DashboardController;
import com.punish.Controller.MatchController;
import com.punish.Controller.PlayerController;
import com.punish.Controller.TournamentController;
import com.punish.Exception.ConflictException;
import com.punish.Exception.NotFoundException;
import com.punish.Exception.ValidationException;
import com.punish.Middleware.AuthMiddleware;

import io.javalin.Javalin;

public class App 
{
    public static void main( String[] args )
    {
        Javalin javalin = Javalin.create( config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    // rule.allowHost("https://punish.vercel.app")
                    rule.anyHost();
                });
            });
        }).start(7000);

        javalin.exception(NotFoundException.class, (e, ctx) -> {
            ctx.status(404).json(Map.of("error ", e.getMessage()));
        });

        javalin.exception(ValidationException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("error ", e.getMessage()));
        });

        javalin.exception(ConflictException.class, (e, ctx) -> {
            ctx.status(409).json(Map.of("error ", e.getMessage()));
        });

        javalin.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Erro interno do servidor"));
        });

        AuthMiddleware.register(javalin);
        new DashboardController().dashboardRoutes(javalin);
        new MatchController().matchRoutes(javalin);
        new PlayerController().playerRoutes(javalin);
        new TournamentController().tournamentRoutes(javalin);
        new AuthController().authRoutes(javalin);

    }
}
