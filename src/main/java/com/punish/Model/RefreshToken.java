package com.punish.Model;

import java.sql.Timestamp;

public class RefreshToken {
    private Long id;
    private Long fk_player_id;
    private String token;
    private Timestamp criado_em;
    private Timestamp expira_em;
    private Boolean revogado;

    public RefreshToken() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFk_player_id() { return fk_player_id; }
    public void setFk_player_id(Long fk_player_id) { this.fk_player_id = fk_player_id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Timestamp getCriado_em() { return criado_em; }
    public void setCriado_em(Timestamp criado_em) { this.criado_em = criado_em; }

    public Timestamp getExpira_em() { return expira_em; }
    public void setExpira_em(Timestamp expira_em) { this.expira_em = expira_em; }

    public Boolean getRevogado() { return revogado; }
    public void setRevogado(Boolean revogado) { this.revogado = revogado; }

}
