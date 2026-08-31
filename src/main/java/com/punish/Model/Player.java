package com.punish.Model;

public class Player {
    private Long id;
    private String nickname;
    private String email;
    private String password_hash;
    private String role;

    public Player () {}

    public Player (Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPassword_hash() { return password_hash; }
    public void setPassword_hash(String password_hash) { this.password_hash = password_hash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

}
