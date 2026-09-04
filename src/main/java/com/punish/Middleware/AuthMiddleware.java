package com.punish.Middleware;

import com.punish.Config.JwtConfig;

import io.javalin.Javalin;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
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
                throw new UnauthorizedResponse("Token não fornecido");
            }
            try {
                Claims claims = JwtConfig.parseToken(auth.substring(7));
                ctx.attribute("userId", Long.parseLong(claims.getSubject()));
                ctx.attribute("userRole", claims.get("role", String.class));
            } catch (Exception e) {
                throw new UnauthorizedResponse("Token inválido");
            }

            // verificar role
            String role = ctx.attribute("userRole");
            if (needsOrganizer(path, method) && !"ORGANIZER".equals(role) && !"ADMIN".equals(role)) {
                throw new ForbiddenResponse("Sem permissão");
            } 
        });

    }

    private static boolean isPublic(String path, String method){
        if ("POST".equals(method) && (path.equals("/auth/register")
            || path.equals("/auth/login")
            || path.equals("/auth/refresh")
            || path.equals("/auth/logout"))) return true;
        if ("GET".equals(method)) return true;
        return false;
    }

    private static boolean needsOrganizer(String path, String method){
        // deleção global de player: exige ORGANIZER/ADMIN
        if ("DELETE".equals(method) && path.matches("/players/\\d+")) return true;
        return false;
    }
}
