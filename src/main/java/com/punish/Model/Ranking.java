package com.punish.Model;

public class Ranking {
    private Long player_id;
    private String nickname;
    private int placement;

    public Ranking (Long player_id, String nickname, int placement) {
        this.player_id = player_id;
        this.nickname = nickname;
        this.placement = placement;
    }

    public Long getPlayer_id() { return player_id; }
    public void setPlayer_id(Long player_id) { this.player_id = player_id; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getPlacement() { return placement; }
    public void setPlacement(int placement) { this.placement = placement; }
}
