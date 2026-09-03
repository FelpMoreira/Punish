package com.punish.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.punish.Exception.ConflictException;
import com.punish.Exception.NotFoundException;
import com.punish.Exception.ValidationException;
import com.punish.Model.Tournament;
import com.punish.Model.TournamentInvite;
import com.punish.Model.TournamentRequest;
import com.punish.Model.Enums.TournamentStatus;
import com.punish.Repository.TournamentInviteRepository;
import com.punish.Repository.TournamentRequestRepository;

public class InviteService {
    private TournamentService tournamentService = new TournamentService();
    private PlayerService playerService = new PlayerService();
    private TournamentInviteRepository inviteRepository = new TournamentInviteRepository();
    private TournamentRequestRepository requestRepository = new TournamentRequestRepository();

    public TournamentInvite criarConvite(Long tournamentId, Timestamp expiraEm, Integer usosMax){
        Tournament t = tournamentService.buscarPorId(tournamentId);
        if (t.getStatus() != TournamentStatus.CREATED) throw new ConflictException("Torneio não está aberto para inscrição");
        if (expiraEm != null && expiraEm.before(Timestamp.from(Instant.now()))) throw new ValidationException("Data de expiração no passado");
        if (usosMax != null && usosMax < 1) throw new ValidationException("Limite de uso deve ser no mínimo 1");
        String codigo = UUID.randomUUID().toString();
        return inviteRepository.criar(tournamentId, codigo, expiraEm, usosMax);
    }

    public TournamentInvite buscarPorCodigo(String codigo){
        TournamentInvite invite = inviteRepository.buscarPorCodigo(codigo);
        if (invite == null) throw new NotFoundException("Código não encontrado");
        return invite;
    }

    public List<TournamentInvite> listarConvites(Long tournamentId){
        tournamentService.buscarPorId(tournamentId);
        return inviteRepository.buscarPorTournament(tournamentId);
    }

    public void deletar(Long inviteId){
        inviteRepository.deletar(inviteId);
    }

    public void entrarPorLink(String codigo, Long userId){
        TournamentInvite invite = buscarPorCodigo(codigo);
        if (invite.getExpira_em() != null && invite.getExpira_em().before(Timestamp.from(Instant.now()))) throw new ConflictException("Convite expirado");
        if (invite.getUsos_max() != null && invite.getUsos() >= invite.getUsos_max()) throw new ConflictException("Covite esgotou o limite de usos");
        playerService.adicionarAoTournament(invite.getFk_tournament_id(), userId);
        inviteRepository.incrementarUsos(invite.getId());
    }

    // pedidos de entrada

    public TournamentRequest solicitarEntrada(Long tournamentId, Long userId){
        Tournament t = tournamentService.buscarPorId(tournamentId);
        if (t.getStatus() != TournamentStatus.CREATED) throw new ConflictException("Torneio não está aberto para incrição");
        if (requestRepository.exite(tournamentId, userId)) throw new ConflictException("Pedido já enviado");
        return requestRepository.criar(tournamentId, userId); 
    }

    public List<TournamentRequest> listarPedidos(Long tournamentId){
        tournamentService.buscarPorId(tournamentId);
        return requestRepository.buscarPorTournament(tournamentId, "PENDING");
    }

    public void aceitarPedido(Long tournamentId, Long playerId){
        TournamentRequest request = requestRepository.buscar(tournamentId, playerId);
        if (request == null || !"PENDING".equals(request.getStatus())) throw new ConflictException("Pedido não encontrado");
        playerService.adicionarAoTournament(tournamentId, playerId);
        requestRepository.atualizarStatus(request.getId(), "ACCEPTED");
    }

    public void rejeitarPedido(Long tournamentId, Long playerId){
        TournamentRequest request = requestRepository.buscar(tournamentId, playerId);
        if (request == null || !"PENDING".equals(request.getStatus())) throw new ConflictException("Pedido não encontrado");
        requestRepository.atualizarStatus(request.getId(), "REJECTED");
    }
}
