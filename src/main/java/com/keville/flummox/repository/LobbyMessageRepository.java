
package com.keville.flummox.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import com.keville.flummox.model.lobby.LobbyMessage;

public interface LobbyMessageRepository extends CrudRepository<LobbyMessage, Integer> {

  @Query("""
  SELECT  * FROM lobby_message as LOB 
      where LOB.LOBBY = :lobbyId
  """)
  Iterable<LobbyMessage> findByLobby(Integer lobbyId);

}
