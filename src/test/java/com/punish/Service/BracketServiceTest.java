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
import static org.mockito.Mockito.when;

import com.punish.Model.Match;
import com.punish.Model.Player;

@ExtendWith(MockitoExtension.class)
public class BracketServiceTest {
    @Mock
    MatchService matchService;
    
    @InjectMocks
    BracketService bracketService;
    
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
}
