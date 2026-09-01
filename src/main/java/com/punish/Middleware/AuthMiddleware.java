package com.punish.Middleware;

import java.util.Map;

import com.punish.Config.JwtConfig;

import io.javalin.Javalin;
import io.jsonwebtoken.Claims;

public class AuthMiddleware {
    public static void register(Javalin app) {
        app.beforeMatched(ctx -> {
            String path = ctx.path();
            String method = ctx.method().name();

            // rotas publicas
            if (isPublic(path, method)) return;

            // extrair token
            String auth = ctx.header("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                ctx.status(401).json(Map.of("error", "Token não fornecido"));
                return;
            }
            try {
                Claims claims = JwtConfig.parseToken(auth.substring(7));
                ctx.attribute("userId", Long.parseLong(claims.getSubject()));
                ctx.attribute("userRole", claims.get("role", String.class));
            } catch (Exception e) {
                ctx.status(401).json(Map.of("error", "Token inválido"));
                return;
            }

            // verificar role
            String role = ctx.attribute("userRole");
            if (needsOrganizer(path, method) && !"ORGANIZER".equals(role) && !"ADMIN".equals(role)) {
                ctx.status(403).json(Map.of("error", "Sem permissão"));
            } 
        });

    }

    private static boolean isPublic(String path, String method){
        if ("POST".equals(method) && (path.equals("/auth/register") || path.equals("/auth/login"))) return true;
        if ("GET".equals(method)) return true;
        return false;
    }

    private static boolean needsOrganizer(String path, String method){
        // POST/PUT/DELET de torneios e gerenciamentos de player
        return true; // simplificado (vai ser refinado na implementação)
    }
}
