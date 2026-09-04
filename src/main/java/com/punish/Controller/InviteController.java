package com.punish.Controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.punish.Model.Tournament;
import com.punish.Model.TournamentInvite;
import com.punish.Model.TournamentRequest;
import com.punish.Service.InviteService;
import com.punish.Service.TournamentService;

import io.javalin.Javalin;

public class InviteController {
    InviteService inviteService = new InviteService();
    TournamentService tournamentService = new TournamentService();

    public  void inviteRoutes(Javalin app){

        // convites (dono)

        app.post("/tournaments/{id}/invite", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Long userId = ctx.attribute("userId");
            String userRole = ctx.attribute("userRole");
            tournamentService.verificarDono(id, userId, userRole);

            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String expiraEmStr = body.get("expiraEm") != null ? body.get("expiraEm").toString() : null;
            Timestamp expiraEm = expiraEmStr != null ? Timestamp.valueOf(LocalDateTime.parse(expiraEmStr)) : null;
            String usosMaxStr = body.get("usosMax") != null ? body.get("usosMax").toString() : null;
            Integer usosMax = usosMaxStr != null ? Integer.parseInt(usosMaxStr) : null;

            TournamentInvite invite = inviteService.criarConvite(id, expiraEm, usosMax);
            ctx.status(201).json(invite);
        });

        app.get("/tournaments/{id}/invite", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Long userId = ctx.attribute("userId");
            String userRole = ctx.attribute("userRole");
            tournamentService.verificarDono(id, userId, userRole);

            List<TournamentInvite> invites = inviteService.listarConvites(id);
            ctx.json(invites);
        });

        app.delete("/tournaments/{id}/invite/{invite_id}", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Long inviteId = Long.parseLong(ctx.pathParam("invite_id"));
            Long userId = ctx.attribute("userId");
            String userRole = ctx.attribute("userRole");
            tournamentService.verificarDono(id, userId, userRole);

            inviteService.revogarConvite(inviteId);
            ctx.status(204);
        });

        // join por link (qualquer logado)
        app.post("/invites/{codigo}/join", ctx -> {
            String codigo = ctx.pathParam("codigo");
            Long userId = ctx.attribute("userId");

            inviteService.entrarPorLink(codigo, userId);
            ctx.status(204);
        });

        // pedidos de entrada
        app.post("/tournaments/{id}/requests", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Long userId = ctx.attribute("userId");

            TournamentRequest request = inviteService.solicitarEntrada(id, userId);
            ctx.status(201).json(request);
        });

        app.get("/tournaments/{id}/requests", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Long userId = ctx.attribute("userId");
            String userRole = ctx.attribute("userRole");
            tournamentService.verificarDono(id, userId, userRole);

            List<TournamentRequest> requests = inviteService.listarPedidos(id);
            ctx.json(requests);
        });

        app.post("/tournaments/{id}/requests/{player_id}/accept", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Long playerId = Long.parseLong(ctx.pathParam("player_id"));
            Long userId = ctx.attribute("userId");
            String userRole = ctx.attribute("userRole");
            tournamentService.verificarDono(id, userId, userRole);

            inviteService.aceitarPedido(id, playerId);
            ctx.status(204);
        });

        app.post("/tournaments/{id}/requests/{player_id}/reject", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Long playerId = Long.parseLong(ctx.pathParam("player_id"));
            Long userId = ctx.attribute("userId");
            String userRole = ctx.attribute("userRole");
            tournamentService.verificarDono(id, userId, userRole);

            inviteService.rejeitarPedido(id, playerId);
            ctx.status(204);
        });

        app.get("/invites/{codigo}", ctx -> {
            String codigo = ctx.pathParam("codigo");
            TournamentInvite invite = inviteService.buscarPorCodigo(codigo);
            Tournament t = tournamentService.buscarPorId(invite.getFk_tournament_id());

            Map<String, Object> resp = new HashMap<>();
            resp.put("codigo", invite.getCodigo());
            resp.put("usosMax", invite.getUsos_max());
            resp.put("usos", invite.getUsos());
            resp.put("expiraEm", invite.getExpira_em());
            resp.put("tournamentId", t.getId());
            resp.put("tournamentName", t.getName());
            resp.put("tournamentStatus", t.getStatus().toString());
            ctx.json(resp);
        });
    }
}