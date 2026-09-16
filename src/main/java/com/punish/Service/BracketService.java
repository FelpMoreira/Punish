package com.punish.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.punish.Model.Match;
import com.punish.Model.Player;

public class BracketService {

    MatchService matchService;

    public BracketService(MatchService matchService) {
        this.matchService = matchService;
    }
    
    private int calcularTamanhoBracket(int numJogadores){
        int tamanho = 1;
        while (tamanho < numJogadores) {
            tamanho = tamanho * 2;
        }
        return tamanho;
    }
    public List<Match> gerarBracket(Long tournament_id, List<Player> players){
        Collections.shuffle(players);
        int tamanho_bracket = calcularTamanhoBracket(players.size());
        int byes = tamanho_bracket - players.size();
        List<Player> slots = new ArrayList<>();
        int playerIdx = 0;
        int nullsLeft = byes;
        for (int i = 0; i < tamanho_bracket; i++) {
            if (i % 2 == 1 && nullsLeft > 0) {
                slots.add(null);
                nullsLeft--;
            } else {
                slots.add(players.get(playerIdx++));
            }
        }
        int numRodadas = (int) (Math.log(tamanho_bracket) / Math.log(2));
        List<Match> matches = new ArrayList<>();
        for(int r = 1; r <= numRodadas; r++){
            int divisor = (int) Math.pow(2, r);
            int partidas_nessa_rodada = tamanho_bracket/divisor;
            for(int p = 0; p < partidas_nessa_rodada; p++){
                Match match = new Match();
                match.setFk_tournament_id(tournament_id);
                match.setRound_number(r);
                match.setMatch_number(p);
                match.setBracket_type("WINNERS");
                match.setStatus("WAITING");
                Match matchSalvo = matchService.criar(match);
                matches.add(matchSalvo);
            }
        }
        for (Match match : matches) {
            Match prox = matches.stream()
                         .filter(m -> m.getRound_number() == match.getRound_number() + 1 && m.getMatch_number() == match.getMatch_number()/2)
                         .findFirst()
                         .orElse(null);
            if (prox != null) {
                match.setFk_next_match_win_id(prox.getId());
                matchService.atualizarNextMatchWin(match.getFk_next_match_win_id(), match.getId());
            }
        }

        List<Match> primeiraRodada = matches.stream()
                                     .filter(m -> m.getRound_number() == 1)
                                     .toList();
        int indice = 0;

        for (Match match : primeiraRodada) {
            Player p1 = slots.get(indice++);
            Player p2 = slots.get(indice++);

            if (p1 != null && p2 != null) {
                matchService.atualizarPlayer2(match.getId(), p2.getId());
                matchService.atualizarStatus("READY", match.getId());
                matchService.atualizarPlayer1(match.getId(), p1.getId());
            } else if (p1 == null){
                matchService.atualizarPlayer2(match.getId(), p2.getId());
                matchService.atualizarVencedor(match.getId(), p2.getId(), 0, 0);
                matchService.atualizarStatus("FINISHED", match.getId());
                Match prox = matches.stream()
                             .filter(m -> m.getId().equals(match.getFk_next_match_win_id()))
                             .findFirst()
                             .orElse(null);
                if (prox.getFk_player1_id() == null) {
                    matchService.atualizarPlayer2(prox.getId(), p2.getId());
                    prox.setFk_player2_id(p2.getId());
                } else {
                    matchService.atualizarPlayer1(prox.getId(), p2.getId());
                    prox.setFk_player1_id(p2.getId());
                }
            } else if (p2 == null){
                matchService.atualizarPlayer1(match.getId(), p1.getId());
                matchService.atualizarVencedor(match.getId(), p1.getId(), 0,0);
                matchService.atualizarStatus("FINISHED", match.getId());
                Match prox = matches.stream()
                             .filter(m -> m.getId().equals(match.getFk_next_match_win_id()))
                             .findFirst()
                             .orElse(null);
                if (prox.getFk_player1_id() == null) {
                    matchService.atualizarPlayer1(prox.getId(), p1.getId());
                    prox.setFk_player1_id(p1.getId());
                } else {
                    matchService.atualizarPlayer2(prox.getId(), p1.getId());
                    prox.setFk_player2_id(p1.getId());
                }
            } else {
                matchService.atualizarStatus("FINISHED", match.getId());
            }
        }

        List<Match> roundsAcima = matches.stream()
            .sorted(Comparator.comparingInt(Match::getRound_number))
            .toList();

        for (Match match : roundsAcima) {
            if (match.getRound_number() == 1) continue;
            Long p1 = match.getFk_player1_id();
            Long p2 = match.getFk_player2_id();
            Long winner = match.getFk_winner_id();

            if (winner != null) continue;
            if (p1 != null && p2 != null) {
                matchService.atualizarStatus("READY", match.getId());
            }
        }

        return matches;
    }

    public List<Match> gerarBracketDoubleElimination(Long tournament_id, List<Player> players) {
        Collections.shuffle(players);
        int tamanho_bracket = calcularTamanhoBracket(players.size());
        int byes = tamanho_bracket - players.size();
        int numRodadas = (int) (Math.log(tamanho_bracket) / Math.log(2));
        List<Player> slots = new ArrayList<>();
        int playerIdx = 0;
        int nullsLeft = byes;
        for (int i = 0; i < tamanho_bracket; i++) {
            if (i % 2 == 1 && nullsLeft > 0) {
                slots.add(null);
                nullsLeft--;
            } else {
                slots.add(players.get(playerIdx++));
            }
        }

        List<List<Match>> wb = new ArrayList<>();
        for (int r = 1; r <= numRodadas; r++) {
            List<Match> rodada = new ArrayList<>();
            int partidas = tamanho_bracket / (int) Math.pow(2, r);
            for (int p = 0; p < partidas; p++) {
                Match match = new Match();
                match.setFk_tournament_id(tournament_id);
                match.setRound_number(r);
                match.setMatch_number(p);
                match.setBracket_type("WINNERS");
                match.setStatus("WAITING");
                rodada.add(matchService.criar(match));
            }
            wb.add(rodada);
        }

        int lbRodadas = 2 * numRodadas - 2;
        List<List<Match>> lb = new ArrayList<>();
        for (int l = 1; l <= lbRodadas; l++){
            List<Match> rodada = new ArrayList<>();
            int partidas = (int) (Math.pow(2, numRodadas -1 ) / Math.pow(2, (l + 1) / 2));
            for (int p = 0; p < partidas; p++) {
                Match match = new Match();
                match.setFk_tournament_id(tournament_id);
                match.setRound_number(l);
                match.setMatch_number(p);
                match.setBracket_type("LOSERS");
                match.setStatus("WAITING");
                rodada.add(matchService.criar(match));
            }
            lb.add(rodada);
        }

        Match gf = new Match();
        gf.setFk_tournament_id(tournament_id);
        gf.setRound_number(numRodadas + 1);
        gf.setMatch_number(0);
        gf.setBracket_type("GRAND_FINAL");
        gf.setStatus("WAITING");
        gf = matchService.criar(gf);

        for (int r = 1; r <= numRodadas; r++) {
            for (int p = 0; p < wb.get(r - 1).size(); p++) {
                Long alvo = (r == numRodadas) ? gf.getId() : wb.get(r).get(p / 2).getId();
                matchService.atualizarNextMatchWin(alvo, wb.get(r - 1).get(p).getId());
            }
        }

        for (int l = 1; l <= lbRodadas; l++) {
            for (int p = 0; p < lb.get(l - 1).size(); p++) {
                Long alvo;
                if (l == lbRodadas) {
                    alvo = gf.getId();
                } else {
                    int prox = (l % 2 == 1) ? p : p / 2;
                    alvo = lb.get(l).get(prox).getId();
                }
                matchService.atualizarNextMatchWin(alvo, lb.get(l - 1).get(p).getId());
            }
        }
        
        for (int p = 0; p < wb.get(0).size(); p++) {
            matchService.atualizarNextMatchLose(lb.get(0).get(p / 2).getId(), wb.get(0).get(p).getId());
        }
        for (int r = 2; r <= numRodadas; r++) {
            for (int p = 0; p < wb.get(r - 1).size(); p++) {
                matchService.atualizarNextMatchLose(lb.get(2 * r - 3).get(p).getId(), wb.get(r - 1).get(p).getId());
            }
        }

        int indice = 0;
        for (Match match : wb.get(0)) {
            Player p1 = slots.get(indice++);
            Player p2 = slots.get(indice++);
            if (p1 != null && p2 != null) {
                matchService.atualizarPlayer2(match.getId(), p2.getId());
                matchService.atualizarStatus("READY", match.getId());
                matchService.atualizarPlayer1(match.getId(), p1.getId());
            } else if (p1 == null) {
                matchService.atualizarPlayer2(match.getId(), p2.getId());
                matchService.atualizarVencedor(match.getId(), p2.getId(), 0, 0);
                Match prox = wb.get(1).get(match.getMatch_number() / 2);
                if (prox.getFk_player1_id() == null) matchService.atualizarPlayer1(prox.getId(), p2.getId());
                else matchService.atualizarPlayer2(prox.getId(), p2.getId());
            } else if (p2 == null) {
                matchService.atualizarPlayer1(match.getId(), p1.getId());
                matchService.atualizarVencedor(match.getId(), p1.getId(), 0, 0);
                Match prox = wb.get(1).get(match.getMatch_number() / 2);
                if (prox.getFk_player1_id() == null) matchService.atualizarPlayer1(prox.getId(), p1.getId());
                else matchService.atualizarPlayer2(prox.getId(), p1.getId());
            } else {
                matchService.atualizarStatus("FINISHED", match.getId());
            }
        }

        for (Match match : matchService.buscarPorTournament(tournament_id)) {
            if ("WINNERS".equals(match.getBracket_type())
                    && match.getRound_number() > 1
                    && match.getFk_winner_id() == null
                    && match.getFk_player1_id() != null
                    && match.getFk_player2_id() != null) {
                matchService.atualizarStatus("READY", match.getId());
            }
        }

        for (List<Match> rodada : lb) {
            for (Match match : rodada) {
                matchService.conferirSeCompletou(match.getId(), tournament_id);
            }
        }

        return matchService.buscarPorTournament(tournament_id);
    }
}