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
            String token = authService.login(body.get("email"), body.get("password"));
            ctx.json(Map.of("token", token, "email", body.get("email")));
        });
    }
}
