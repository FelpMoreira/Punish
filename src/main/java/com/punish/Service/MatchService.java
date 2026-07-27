package com.punish.Service;

import java.util.ArrayList;
import java.util.List;

import com.punish.Exception.ConflictException;
import com.punish.Exception.NotFoundException;
import com.punish.Exception.ValidationException;
import com.punish.Model.Match;
import com.punish.Repository.MatchRepository;
import com.punish.Repository.TournamentRepository;

public class MatchService {
    MatchRepository matchRepository = new MatchRepository();
    TournamentRepository tournamentRepository = new TournamentRepository();

    public Match criar(Match match){
        return matchRepository.criar(match);
    }

    public List<Match> iniciarRodada(Long fk_tournament_id, int fk_round_number){
        List<Match> all = matchRepository.buscarPorTournament(fk_tournament_id);
        List<Match> started = new ArrayList<>();
        for (Match m : all) {
            if (m.getRound_number() == fk_round_number
                    && "READY".equals(m.getStatus())
                    && m.getFk_player1_id() != null
                    && m.getFk_player2_id() != null) {
                matchRepository.atualizarStatus("IN_PROGRESS", m.getId());
                started.add(matchRepository.buscarPorId(m.getId()));
            }
        }
        return started;
    }

    public Match buscarPorId(Long id){
        Match m = matchRepository.buscarPorId(id);
        if (m == null) {
            throw new NotFoundException("Partida não encontrada");
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
        if (m == null) throw new NotFoundException("Partida não encontrada");
        if (!"READY".equals(m.getStatus())) throw new ConflictException("Partida não pronta");
        if (m.getFk_player1_id() == null || m.getFk_player2_id() == null) throw new ConflictException("Partida não tem 2 jogadores");
        matchRepository.atualizarStatus("IN_PROGRESS", id);
        return matchRepository.buscarPorId(id);
    }

    public Match registrarResultado(Long id, Long fk_winner_id, Integer score_player1, Integer score_player2){
        if(score_player1 != null && score_player1 < 0) throw new ValidationException("Placar não pode ser negativo");
        if(score_player2 != null && score_player2 < 0) throw new ValidationException("Placar não pode ser negativo");
        Match m = matchRepository.buscarPorId(id);
        if (m == null) throw new NotFoundException("Partida não encontrada");
        if (!"READY".equals(m.getStatus()) && !"IN_PROGRESS".equals(m.getStatus())){
            throw new ConflictException("Partida não está em andamento");
        }
        if (fk_winner_id == null) {
            throw new ValidationException("Vencedor não informado");
        }
        if (!fk_winner_id.equals(m.getFk_player1_id()) && !fk_winner_id.equals(m.getFk_player2_id())) {
            throw new ValidationException("Vencedor inválido");
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
            throw new ConflictException("Não existe vaga nessa partida");
        }

        Match nextAtualizada = matchRepository.buscarPorId(m.getFk_next_match_win_id());
        if (nextAtualizada.getFk_player1_id() != null && nextAtualizada.getFk_player2_id() != null) {
            matchRepository.atualizarStatus("READY", nextAtualizada.getId());
        }
        
        return matchRepository.buscarPorId(id);
    }

    public void delatarPorTournament(Long id){
        matchRepository.deletarPorTournament(id);
    }
}