package com.punish.Service;

import java.security.PublicKey;
import java.util.List;

import com.punish.Model.Match;
import com.punish.Repository.MatchRepository;
import com.punish.Repository.TournamentRepository;

public class MatchService {
    MatchRepository matchRepository = new MatchRepository();
    TournamentRepository tournamentRepository = new TournamentRepository();

    public Match criar(Match match){
        return matchRepository.criar(match);
    }

    public Match buscarPorId(Long id){
        Match m = matchRepository.buscarPorId(id);
        if (m == null) {
            throw new RuntimeException("Partida não encontrada");
        }
        return m;
    }

    public List<Match> buscarPorTournament(long fk_tournament_id){
        return matchRepository.buscarPorTournament(fk_tournament_id);
    }

    public void atualizarNextMatchWin(Long fk_next_match_win_id, Long id){
        matchRepository.atualizarNextMatchWin(fk_next_match_win_id, id);
    }

    public void atualizarStatus(String status, Long id){
        matchRepository.atualizarStatus(status, id);
    }

    public void atualizarPlayer1(Long id, Long fk_player1_id){
        buscarPorId(id);
        matchRepository.atualizarPlayer1(id, fk_player1_id);
    }

    public void atualizarPlayer2(Long id, Long fk_player2_id){
        buscarPorId(id);
        matchRepository.atualizarPlayer2(id, fk_player2_id);
    }

    public void atualizarVencedor(long id, Long fk_winner_id, Integer score_player1, Integer score_player2){
        buscarPorId(id);
        matchRepository.atualizarVencedor(id, fk_winner_id, score_player1, score_player2);
    }

    public Match iniciarPartida(Long id){
        Match m = matchRepository.buscarPorId(id);
        if (m == null) throw new RuntimeException("Partida não encontrada");
        if (!"READY".equals(m.getStatus())) throw new RuntimeException("Partida não pronta");
        if (m.getFk_player1_id() == null || m.getFk_player2_id() == null) throw new RuntimeException("Partida não tem 2 jogadores");
        matchRepository.atualizarStatus("IN_PROGRESS", id);
        return matchRepository.buscarPorId(id);
    }

    public Match registrarResultado(Long id, Long fk_winner_id, Integer score_player1, Integer score_player2){
        Match m = matchRepository.buscarPorId(id);
        if (m == null) throw new RuntimeException("Partida não encontrada");
        if (!"READY".equals(m.getStatus()) && !"IN_PROGRESS".equals(m.getStatus())){
            throw new RuntimeException("Partida não está em andamento");
        }
        if (fk_winner_id == null) {
            throw new RuntimeException("Vencedor não informado");
        }
        if (!fk_winner_id.equals(m.getFk_player1_id()) && !fk_winner_id.equals(m.getFk_player2_id())) {
            throw new RuntimeException("Vencedor inválido");
        }
        matchRepository.atualizarVencedor(id, fk_winner_id, score_player1, score_player2);
        Long nextMatchId = m.getFk_next_match_win_id();
        Match next_match = null;
        if (nextMatchId != null) {
            next_match = matchRepository.buscarPorId(nextMatchId);
        }
        if (next_match == null) {
            tournamentRepository.atualizarCampeao(m.getFk_tournament_id(), fk_winner_id);
            tournamentRepository.atualizarStatus(m.getFk_tournament_id(), "FINISHED");
            return matchRepository.buscarPorId(id);
        }
        if (next_match.getFk_player1_id() == null) {
            matchRepository.atualizarPlayer1(m.getFk_next_match_win_id(), fk_winner_id);
        } else if (next_match.getFk_player2_id() == null) {
            matchRepository.atualizarPlayer2(m.getFk_next_match_win_id(), fk_winner_id);
        } else {
            throw new RuntimeException("Não existe vaga nessa partida");
        }

        Match nextAtualizada = matchRepository.buscarPorId(m.getFk_next_match_win_id());
        if (nextAtualizada.getFk_player1_id() != null && nextAtualizada.getFk_player2_id() != null) {
            matchRepository.atualizarStatus("READY", nextAtualizada.getId());
        }
        
        return matchRepository.buscarPorId(id);
    }
}