package com.punish.Service;

import org.mindrot.jbcrypt.BCrypt;

import com.punish.Config.JwtConfig;
import com.punish.Exception.ConflictException;
import com.punish.Exception.NotFoundException;
import com.punish.Exception.ValidationException;
import com.punish.Model.Player;
import com.punish.Repository.PlayerRepository;

public class AuthService {
    private PlayerRepository playerRepository = new PlayerRepository();

    public Player register(String nickname, String email, String password){
        if(nickname == null || nickname.isBlank()) throw new ValidationException("Nickname é obrigatório");
        if(nickname.length() > 50) throw new ValidationException("Nickname muito longo");
        if(email == null || email.isBlank()) throw new ValidationException("Email é obrigatório");
        if(playerRepository.buscarPorEmail(email) != null) throw new ConflictException("Email já cadastrado");
        if(playerRepository.buscarPorNickname(nickname).size() > 0) throw new ConflictException("Nickname já cadastrado");
        if(password == null || password.length() < 6) throw new ValidationException("Senha deve ter no minimo 6 caracteres");

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        return playerRepository.criarPlayer(nickname, email, passwordHash);
    }

    public String login(String email, String password){
        if(email == null || email.isBlank()) throw new ValidationException("Email é obrigatório");
        if(password == null || password.isBlank()) throw new ValidationException("Senha é obrigatória");
        Player p = playerRepository.buscarPorEmail(email);
        if (p == null || !BCrypt.checkpw(password, p.getPassword_hash())) {
            throw new NotFoundException("Email ou senha inválidos");
        }
        return JwtConfig.generateToken(p.getId(), p.getRole());
    }
}
