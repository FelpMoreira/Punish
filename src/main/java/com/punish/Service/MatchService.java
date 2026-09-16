package com.punish.Service;

import java.util.ArrayList;
import java.util.List;

import com.punish.Exception.ConflictException;
import com.punish.Exception.NotFoundException;
import com.punish.Exception.ValidationException;
import com.punish.Model.Match;
import com.punish.Model.PaginaResult;
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

    public PaginaResult<Match> buscarHistorico(Long playerId, Long tournamentId, int page, int size) {
        List<Match> data = matchRepository.buscarComFiltros(playerId, tournamentId, page, size);
        long total = matchRepository.contarTotal(playerId, tournamentId);
        return new PaginaResult<>(data, total, page, size);
    }

    public void atualizarNextMatchWin(Long fk_next_match_win_id, Long id){
        matchRepository.atualizarNextMatchWin(fk_next_match_win_id, id);
    }

    public void atualizarNextMatchLose(Long fk_next_match_lose_id, Long id){
        matchRepository.atualizarNextMatchLose(fk_next_match_lose_id, id);
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
        if (score_player1 != null && score_player1 < 0) throw new ValidationException("Placar não pode ser negativo");
        if (score_player2 != null && score_player2 < 0) throw new ValidationException("Placar não pode ser negativo");
        Match m = matchRepository.buscarPorId(id);
        if (m == null) throw new NotFoundException("Partida não encontrada");
        if (!"READY".equals(m.getStatus()) && !"IN_PROGRESS".equals(m.getStatus())) throw new ConflictException("Partida em andamento");
        if (fk_winner_id == null) throw new ValidationException("Vencedor não informado");
        if (!fk_winner_id.equals(m.getFk_player1_id()) && !fk_winner_id.equals(m.getFk_player2_id())) throw new ValidationException("Vencedor inválido");
        matchRepository.atualizarVencedor(id, fk_winner_id, score_player1, score_player2);

        if ("GRAND_FINAL".equals(m.getBracket_type()) && m.getFk_next_match_win_id() == null) {
            List<Match> all = matchRepository.buscarPorTournament(m.getFk_tournament_id());
            Match lbFinal = all.stream()
                .filter(x -> "LOSERS".equals(x.getBracket_type())
                        && m.getId().equals(x.getFk_next_match_win_id()))
                .findFirst().orElse(null);
            if (lbFinal != null) {
                if (fk_winner_id.equals(lbFinal.getFk_winner_id())) {
                    Match gf2 = new Match();
                    gf2.setFk_tournament_id(m.getFk_tournament_id());
                    gf2.setRound_number(m.getRound_number() + 1);
                    gf2.setMatch_number(0);
                    gf2.setBracket_type("GRAND_FINAL");
                    gf2.setStatus("WAITING");
                    gf2.setFk_player1_id(m.getFk_player1_id());
                    gf2.setFk_player2_id(m.getFk_player2_id());
                    gf2 = matchRepository.criar(gf2);
                    matchRepository.atualizarStatus("READY", gf2.getId());
                    matchRepository.atualizarNextMatchWin(gf2.getId(), m.getId());
                    matchRepository.atualizarNextMatchLose(gf2.getId(), m.getId());
                } else {
                    tournamentRepository.atualizarCampeao(m.getFk_tournament_id(), fk_winner_id);
                    tournamentRepository.atualizarStatus(m.getFk_tournament_id(), "FINISHED");
                }
                return matchRepository.buscarPorId(id);
            }   
            tournamentRepository.atualizarCampeao(m.getFk_tournament_id(), fk_winner_id);
            tournamentRepository.atualizarStatus(m.getFk_tournament_id(), "FINISHED");
            return matchRepository.buscarPorId(id);
        }
        Long nextMatchId = m.getFk_next_match_win_id();
        if (nextMatchId == null) {
            tournamentRepository.atualizarCampeao(m.getFk_tournament_id(), fk_winner_id);
            tournamentRepository.atualizarStatus(m.getFk_tournament_id(), "FINISHED");
            return matchRepository.buscarPorId(id);
        }
        colocarJogador(nextMatchId, fk_winner_id);

        Long loserId = fk_winner_id.equals(m.getFk_player1_id()) ? m.getFk_player2_id() : m.getFk_player1_id();
        if (m.getfk_next_match_lose_id() != null) {
            colocarJogador(m.getfk_next_match_lose_id(), loserId);
        }

        conferirSeCompletou(nextMatchId, m.getFk_tournament_id());
        conferirSeCompletou(m.getfk_next_match_lose_id(), m.getFk_tournament_id());

        return matchRepository.buscarPorId(id);
    }

    public void colocarJogador(Long alvoId, Long playerId) {
        Match alvo = matchRepository.buscarPorId(alvoId);
        if (alvo.getFk_player1_id() == null) {
            matchRepository.atualizarPlayer1(alvoId, playerId);
        } else if (alvo.getFk_player2_id() == null) {
            matchRepository.atualizarPlayer2(alvoId, playerId);
        } else {
            throw new ConflictException("Não existe vaga nessa partida");
        }
        Match alvoAtualizada = matchRepository.buscarPorId(alvoId);
        if (alvoAtualizada.getFk_player1_id() != null && alvoAtualizada.getFk_player2_id() != null) {
            matchRepository.atualizarStatus("READY", alvoAtualizada.getId());
        }
    }

    public void conferirSeCompletou(Long alvoId, Long tournamenteId){
        if (alvoId == null) return;
        Match alvo = matchRepository.buscarPorId(alvoId);
        // GF nunca sofre W/O automatico
        if (alvo == null || "GRAND_FINAL".equals(alvo.getBracket_type())) return;

        List<Match> all = matchRepository.buscarPorTournament(tournamenteId);
        boolean todasFontes = all.stream()
            .filter(x -> alvo.getId().equals(x.getFk_next_match_win_id())
                      || alvo.getId().equals(x.getfk_next_match_lose_id()))
            .allMatch(x -> "FINISHED".equals(x.getStatus()));
        if (!todasFontes) return; // ainda vai chegar jogando

        Long p1 = alvo.getFk_player1_id();
        Long p2 = alvo.getFk_player2_id();

        if (p1 != null && p2 != null) { 
            atualizarStatus("READY", alvo.getId()); 
            return; 
        }
        if (p1 != null || p2 != null) { // 1 jogador -> W/O, promove e desce a "cascata"
            Long winner = p1 != null ? p1 : p2;
            atualizarVencedor(alvo.getId(), winner, 0, 0);
            if (alvo.getFk_next_match_win_id() != null) {
                colocarJogador(alvo.getFk_next_match_win_id(), winner);
                conferirSeCompletou(alvo.getFk_next_match_win_id(), tournamenteId);
            }
            return;
        }
        matchRepository.atualizarStatus("FINISHED", alvo.getId());
        if (alvo.getFk_next_match_win_id() != null) {
            conferirSeCompletou(alvo.getFk_next_match_win_id(), tournamenteId);
        }
    }

    public void deletarPorTournament(Long id){
        matchRepository.deletarPorTournament(id);
    }
}