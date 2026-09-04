package com.punish.Controller;

import java.util.Map;

import com.punish.Model.Player;
import com.punish.Service.AuthService;

import io.javalin.Javalin;

public class AuthController {
    private AuthService authService = new AuthService();

    public void authRoutes(Javalin app){
        app.post("/auth/register", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            Player p = authService.register(body.get("nickname"), body.get("email"), body.get("password"));
            ctx.status(201).json(p);
        });

        app.post("/auth/login", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            Map<String, String> result = authService.login(body.get("email"), body.get("password"));
            ctx.json(result);
        });

        app.post("/auth/refresh", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            Map<String, String> result = authService.refresh(body.get("refreshToken"));
            ctx.json(result);
        });

        app.post("/auth/logout", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            authService.logout(body.get("refreshToken"));
            ctx.status(204);
        });
    }
}
