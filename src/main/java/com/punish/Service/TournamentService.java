package com.punish.Service;

import java.util.List;

import com.punish.Exception.ConflictException;
import com.punish.Exception.NotFoundException;
import com.punish.Exception.ValidationException;
import com.punish.Model.Tournament;
import com.punish.Model.Enums.TournamentStatus;
import com.punish.Repository.TournamentRepository;

public class TournamentService {
    TournamentRepository tournamentRepository = new TournamentRepository();

    public Tournament criarTournament(String name, String game){
        if(name == null || name.isBlank()) throw new ValidationException("Nome é obrigatório");
        if(name.length() > 100) throw new ValidationException("Nome muito longo");
        if(game == null || game.isBlank()) throw new ValidationException("Jogo é obrigatório");
        if(game.length() > 50) throw new ValidationException("Nome do jogo muito longo");
        return tournamentRepository.criarTournament(name, game);
    }

    public List<Tournament> buscarTodosOsTorneios(){
        return tournamentRepository.buscarTodosOsTorneios();
    }

    public Tournament buscarPorId(Long id){
        Tournament t = tournamentRepository.buscarPorId(id);
        if (t == null) throw new NotFoundException("Torneio não encontrado");
        return t;
    }

    public List<Tournament> buscarPorNome(String name){
        return tournamentRepository.buscarPorNome(name);
    }

    public Tournament atualizarTournament(Long id, String name, String game){
        Tournament t = buscarPorId(id);
        if (t.getStatus() == TournamentStatus.FINISHED) {
            throw new ConflictException("Torneio ja foi encerrado");
        }
        return tournamentRepository.atualizarTournament(id, name, game);
    }
    
    public Tournament atualizarStatus(Long id, String status){
        buscarPorId(id);
        return tournamentRepository.atualizarStatus(id, status);
    }

    public void start(Long id) {
        Tournament t = buscarPorId(id);
        if (t.getStatus() != TournamentStatus.CREATED) {
            throw new ConflictException("Torneio não pode ser iniciado");
        }
        tournamentRepository.atualizarStatus(id, "STARTED");
    }

    public void finish(Long id) {
        Tournament t = buscarPorId(id);
        if (t.getStatus() != TournamentStatus.STARTED) {
            throw new ConflictException("Torneio não pode ser iniciado");
        }
        tournamentRepository.atualizarStatus(id, "FINISHED");
    }

    public void deletar(Long id){
        buscarPorId(id);
        tournamentRepository.deletar(id);
    }
}
