package com.punish.Model;

import java.sql.Timestamp;

public class TournamentRequest {
    private Long id;
    private Long fk_tournament_id;
    private Long fk_player_id;
    private String status;
    private Timestamp criado_em;

    public TournamentRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFk_tournament_id() { return fk_tournament_id; }
    public void setFk_tournament_id(Long fk_tournament_id) { this.fk_tournament_id = fk_tournament_id; }

    public Long getFk_player_id() { return fk_player_id; }
    public void setFk_player_id(Long fk_player_id) { this.fk_player_id = fk_player_id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCriado_em() { return criado_em; }
    public void setCriado_em(Timestamp criado_em) { this.criado_em = criado_em; }
    
}
