package com.punish.Model;

import java.sql.Timestamp;

public class TournamentInviteUsage {
    private Long id;
    private Long fk_invite_id;
    private Long fk_player_id;
    private Timestamp usado_em;

    public TournamentInviteUsage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFk_invite_id() { return fk_invite_id; }
    public void setFk_invite_id(Long fk_invite_id) { this.fk_invite_id = fk_invite_id; }

    public Long getFk_player_id() { return fk_player_id; }
    public void setFk_player_id(Long fk_player_id) { this.fk_player_id = fk_player_id; }

    public Timestamp getUsado_em() { return usado_em; }
    public void setUsado_em(Timestamp usado_em) { this.usado_em = usado_em; }
    
}
