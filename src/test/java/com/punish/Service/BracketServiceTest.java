package com.punish.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.punish.Model.Match;
import com.punish.Model.Player;

@ExtendWith(MockitoExtension.class)
public class BracketServiceTest {
    @Mock
    MatchService matchService;
    
    @InjectMocks
    BracketService bracketService;

    private void stubCriacao(List<Match> criados) {
        AtomicLong counter = new AtomicLong(0);
        when(matchService.criar(any())).thenAnswer(invocation -> {
            Match m = invocation.getArgument(0);
            m.setId(counter.incrementAndGet());
            criados.add(m);
            return m;
        });
    }
    
    @Test
    void deveCalcularTotalDeMatchesCom4Jogadores(){
        Long tournamentId = 1L;
        // arrange
        List<Player> players = new ArrayList<>(
            List.of(
                new Player(1L),
                new Player(2L),
                new Player(3L),
                new Player(4L)
            )
        );

        AtomicLong counter = new AtomicLong(0);

        when(matchService.criar(any())).thenAnswer(invocation -> {
            Match m = invocation.getArgument(0);
            m.setId(counter.incrementAndGet());
            return m;
        });

        // act
        List<Match> matches = bracketService.gerarBracket(tournamentId, players);

        // assert
        assertThat(matches).hasSize(3);
    }

    @Test
    void deveGerar7MatchesCom2ByesSeTiver6Jogadores(){
        Long tournamentId = 1L;
        // arrange
        List<Player> players = new ArrayList<>(
            List.of(
                new Player(1L),
                new Player(2L),
                new Player(3L),
                new Player(4L),
                new Player(5L),
                new Player(6L)
            )
        );

        AtomicLong counter = new AtomicLong(0);

        when(matchService.criar(any())).thenAnswer(invocation ->{
            Match m = invocation.getArgument(0);
            m.setId(counter.incrementAndGet());
            return m;
        });

        // act
        List<Match> matches = bracketService.gerarBracket(tournamentId, players);

        // assert
        assertThat(matches).hasSize(7);
    }

    @Test
    void deveGerar14MatchesPara6JogadoresEmDoubleElim(){
        List<Player> players = jogadores(6);
        List<Match> criados = new ArrayList<>();
        stubCriacao(criados);
        when(matchService.buscarPorTournament(anyLong())).thenReturn(criados);

        List<Match> matches = bracketService.gerarBracketDoubleElimination(1L, players);

        assertThat(matches).hasSize(14);
        assertThat(matches.stream().filter(m -> "WINNERS".equals(m.getBracket_type()))).hasSize(7);
        assertThat(matches.stream().filter(m -> "LOSERS".equals(m.getBracket_type()))).hasSize(6);
        assertThat(matches.stream().filter(m -> "GRAND_FINAL".equals(m.getBracket_type()))).hasSize(1);
    }

    @Test
    void devePropagarOsDoisByesParaASegundaRodadaSemSobrescrever(){
        List<Player> players = jogadores(6);
        List<Match> criados = new ArrayList<>();
        stubCriacao(criados);
        when(matchService.buscarPorTournament(anyLong())).thenReturn(criados);

        List<Match> matches = bracketService.gerarBracketDoubleElimination(1L, players);

        Match r2 = matches.stream()
            .filter(m -> "WINNERS".equals(m.getBracket_type()) && m.getRound_number() == 2 && m.getMatch_number() == 0)
            .findFirst().orElseThrow();

        assertThat(r2.getFk_player1_id()).isNotNull();
        assertThat(r2.getFk_player2_id()).isNotNull();
        assertThat(r2.getFk_player1_id()).isNotEqualTo(r2.getFk_player2_id());
    }

    @Test 
    void deveAplicarWalkoverNosDoisByesDaPrimeiraRodada() {
        List<Player> players = jogadores(6);
        List<Match> criados = new ArrayList<>();
        stubCriacao(criados);
        when(matchService.buscarPorTournament(anyLong())).thenReturn(criados);

        bracketService.gerarBracketDoubleElimination(1L, players);

        verify(matchService, times(2)).atualizarVencedor(anyLong(), anyLong(), eq(0), eq(0));
    }

    private List<Player> jogadores(int n) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < n; i++) players.add(new Player((long) i));
        return players;
    }
}
