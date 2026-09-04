package com.punish.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import com.punish.Config.JwtConfig;
import com.punish.Exception.ConflictException;
import com.punish.Exception.NotFoundException;
import com.punish.Exception.ValidationException;
import com.punish.Model.Player;
import com.punish.Model.RefreshToken;
import com.punish.Repository.PlayerRepository;
import com.punish.Repository.RefreshTokenRepository;

import io.javalin.http.UnauthorizedResponse;

public class AuthService {
    private PlayerRepository playerRepository = new PlayerRepository();
    private RefreshTokenRepository refreshTokenRepository = new RefreshTokenRepository();
    private static final long REFRESH_EXPIRATION_MS = 604800000L;

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

    public Map<String, String> login(String email, String password){
        if(email == null || email.isBlank()) throw new ValidationException("Email é obrigatório");
        if(password == null || password.isBlank()) throw new ValidationException("Senha é obrigatoria");
        Player p = playerRepository.buscarPorEmail(email);
        if(p == null || !BCrypt.checkpw(password, p.getPassword_hash())) throw new NotFoundException("Email ou senha inválidos");
        String token = JwtConfig.generateToken(p.getId(), p.getRole());
        String refreshToken = criarRefreshToken(p.getId());
        return Map.of("token", token, "refreshToken", refreshToken, "email", email);
    }

    public String criarRefreshToken(Long playerId){
        String token = UUID.randomUUID().toString();
        Timestamp expiraEm = Timestamp.from(Instant.now().plusMillis(REFRESH_EXPIRATION_MS));
        refreshTokenRepository.salvar(playerId, token, expiraEm);
        return token;
    }

    public Map<String,String> refresh(String refreshToken){
        RefreshToken rt = refreshTokenRepository.buscarPorToken(refreshToken);
        if(rt == null || rt.getRevogado() || rt.getExpira_em().before(Timestamp.from(Instant.now()))) throw new UnauthorizedResponse("Refresh token inválido");
        refreshTokenRepository.revogar(refreshToken);
        Player p = playerRepository.buscarPorId(rt.getFk_player_id());
        if(p == null) throw new UnauthorizedResponse("Refresh token inválido");
        String novoToken = JwtConfig.generateToken(p.getId(), p.getRole());
        String novoRefresh = criarRefreshToken(p.getId());
        return Map.of("token", novoToken, "refreshToken", novoRefresh);
    }

    public void logout(String refreshToken){
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.revogar(refreshToken);
        }
    }
}
