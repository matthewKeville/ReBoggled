import React from 'react';
import { useRouteLoaderData } from "react-router-dom";

import LobbyUserDisplay from "../lobby/LobbyUserDisplay.jsx";


export default function PlayerList({lobby}) {

  const { userInfo } = useRouteLoaderData("root");

  if (lobby == null) {
    return
  }

  const isOwner = (lobby.owner.id == userInfo.id);

  /* compute a badge based on the lobby context */
  function getContextBadge(player) {
     if ( player.id == lobby.owner.id ) {
       return "👑"
     } else {
       return ""
     }
  }

  return (

    <div className="players-flex thick-blue-border">
      {
        lobby.users.map( (player) => {
          return (
            <LobbyUserDisplay 
              player={player} 
              lobby={lobby} 
              contextBadge={getContextBadge(player)} 
              isOwner={isOwner} 
              isSelf={player.username == userInfo.username}
              key={player.id}
            />
          )
        })
      }
    </div>
  )

}
