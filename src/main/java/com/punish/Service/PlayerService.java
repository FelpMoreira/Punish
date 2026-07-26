package com.punish.Service;

import java.util.List;

import com.punish.Exception.ConflictException;
import com.punish.Exception.NotFoundException;
import com.punish.Exception.ValidationException;
import com.punish.Model.Player;
import com.punish.Model.Tournament;
import com.punish.Model.Enums.TournamentStatus;
import com.punish.Repository.PlayerRepository;
import com.punish.Repository.TournamentPlayerRepository;

public class PlayerService {
    private TournamentService tournamentService = new TournamentService();
    private TournamentPlayerRepository tournamentPlayerRepository = new TournamentPlayerRepository();
    PlayerRepository playerRepository = new PlayerRepository();

    public Player criarPlayer(String nickname){
        if(nickname == null || nickname.isBlank()) throw new ValidationException("Nickname é obrigatório");
        if(nickname.length() > 50) throw new ValidationException("Nickname muito longo");
        return playerRepository.criarPlayer(nickname);
    }

    public Player buscarPorId(Long id){
        Player p = playerRepository.buscarPorId(id);
        if (p == null) throw new NotFoundException("Player não encontrado");
        return p;
    }

    public List<Player> buscarPorNickname(String nickname){
        return playerRepository.buscarPorNickname(nickname);
    }

    public List<Player> buscarTodosOsPlayers(){
        return playerRepository.buscarTodosOsPlayers();
    }

    public void adicionarAoTournament(Long tournament_id, Long player_id){
        Tournament t = tournamentService.buscarPorId(tournament_id);
        if (t.getStatus() != TournamentStatus.CREATED) {
            throw new ConflictException("Torneio não está aberto para inscrição");
        }
        buscarPorId(player_id);
        if (tournamentPlayerRepository.existe(tournament_id, player_id)){
            throw new ConflictException("Jogador já está no torneio");
        }
        if (tournamentPlayerRepository.contarPorTournament(tournament_id) >= 16) {
            throw new ConflictException("Torneio cheio");
        }
        tournamentPlayerRepository.criarTournamentPlayer(tournament_id, player_id);
    }

    public void removerPlayersDoTournament(Long tournament_id, Long player_id){
        Tournament t = tournamentService.buscarPorId(tournament_id);
        if (t.getStatus() != TournamentStatus.CREATED) {
            throw new ConflictException("Não é possivel remover players");
        }
        tournamentPlayerRepository.deletarTournamentPlayer(tournament_id, player_id);
    }

    public List<Player> buscarPlayersDoTournament(Long tournament_id){
        return tournamentPlayerRepository.buscarPlayerDoTournament (tournament_id);
    }

    public void deletarPlayer(Long id){
        buscarPorId(id);
        playerRepository.deletar(id);
    }
}
