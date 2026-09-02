package com.punish.Model;

import java.security.Timestamp;

public class TournamentInvite {
    private Long id;
    private Long fk_tournament_id;
    private String codigo;
    private Timestamp criado_em;
    private Timestamp expira_em;
    private Integer usos_max;
    private Integer usos;

    public TournamentInvite() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFk_tournament_id() { return fk_tournament_id; }
    public void setFk_tournament_id(Long fk_tournament_id) { this.fk_tournament_id = fk_tournament_id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo;}

    public Timestamp getCriado_em() { return criado_em; }
    public void setCriado_em(Timestamp criado_em) { this.criado_em = criado_em; }

    public Timestamp getExpira_em() { return expira_em; }
    public void setExpira_em(Timestamp expira_em) { this.expira_em = expira_em; }

    public Integer getUsos_max() { return usos_max; }
    public void setUsos_max(Integer usos_max) { this.usos_max = usos_max; }

    public Integer getUsos() { return usos; }
    public void setUsos(Integer usos) { this.usos = usos; }

}
