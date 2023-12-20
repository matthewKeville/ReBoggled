package com.keville.ReBoggled.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.keville.ReBoggled.model.game.GameSettings;
import com.keville.ReBoggled.model.lobby.Lobby;

public class LobbyViewDTO {

    public Integer id;
    public String name;
    public Integer capacity;
    public Boolean isPrivate;
    public GameSettings gameSettings;
    public LocalDateTime lastModifiedDate;

    public LobbyUserDTO owner;
    public List<LobbyUserDTO> users;

    public LobbyViewDTO(Lobby lobby) {
      this.id = lobby.id;
      this.name = lobby.name;
      this.capacity = lobby.capacity;
      this.isPrivate = lobby.isPrivate;
      this.gameSettings = lobby.gameSettings;
      this.lastModifiedDate = lobby.lastModifiedDate;
    }

}
