package com.punish.Service;

import java.util.ArrayList;
import java.util.List;

import com.punish.Model.Match;
import com.punish.Model.Player;
import com.punish.Model.Ranking;
import com.punish.Model.Tournament;
import com.punish.Repository.MatchRepository;
import com.punish.Repository.PlayerRepository;
import com.punish.Repository.TournamentRepository;

public class RankingService {
    private MatchRepository matchRepository = new MatchRepository();
    private TournamentRepository tournamentRepository = new TournamentRepository();
    private PlayerRepository playerRepository = new PlayerRepository();

    public List<Ranking> calcularRanking(Long tournament_id){
        Tournament t = tournamentRepository.buscarPorId(tournament_id);
        List<Match> m = matchRepository.buscarPorTournament(tournament_id);
        int totalRodadas = 0;
        List<Ranking> rankingList = new ArrayList<>();

        for (Match match : m) {
            if (match.getRound_number() > totalRodadas) {
                totalRodadas = match.getRound_number();
            }
        }

        for (Match match : m) {

            if (!"FINISHED".equals(match.getStatus())) continue;
            if (match.getFk_winner_id() == null) continue;

            Long perdedorId;
            if (match.getFk_winner_id().equals(match.getFk_player1_id())) {
                perdedorId = match.getFk_player2_id();
            } else {
                perdedorId = match.getFk_player1_id();
            }

            if (perdedorId == null) continue;

            int placement = (int) Math.pow(2, totalRodadas - match.getRound_number()) + 1;
            rankingList.add(new Ranking(perdedorId, null, placement));

        }

        if (t.getFk_winner_id() != null) {
            rankingList.add(new Ranking(t.getFk_winner_id(), null, 1));
        }

        for (Ranking r : rankingList) {
            Player p = playerRepository.buscarPorId(r.getPlayer_id());
            if (p != null) r.setNickname(p.getNickname());
        }

        rankingList.sort((a, b) -> Integer.compare(a.getPlacement(), b.getPlacement()));

        return rankingList;
    }
}
