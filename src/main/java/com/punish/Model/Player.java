package com.punish.Model;

public class Player {
    private Long id;
    private String nickname;

    public Player () {}

    public Player (Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
