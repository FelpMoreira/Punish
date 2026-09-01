package com.punish.Model;

import java.sql.Timestamp;

import com.punish.Model.Enums.TournamentStatus;

public class Tournament {
    private Long id;
    private String name;
    private String game;
    private Long fk_winner_id;
    private Long fk_owner;
    private TournamentStatus status;
    private Timestamp criado_em;

    public Tournament () {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public Long getFk_winner_id() { return fk_winner_id; }
    public void setFk_winner_id(Long fk_winner_id) { this.fk_winner_id = fk_winner_id; }

    public Long getFk_owner() { return fk_owner; }
    public void setFk_owner(Long fk_owner) { this.fk_owner = fk_owner; }
    
    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }

    public Timestamp getCriado_em() { return criado_em; }
    public void setCriado_em(Timestamp criado_em) { this.criado_em = criado_em; }
}