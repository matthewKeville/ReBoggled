import React, { useState, useEffect, } from 'react';
import {  useLoaderData, useRouteLoaderData } from "react-router-dom";

import PreGame from "/src/main/js/components/game/preGame/PreGame.jsx";
import Game from "/src/main/js/components/game/game/Game.jsx";
import PostGame from "/src/main/js/components/game/postgame/PostGame.jsx";

import { GetLobbySummary } from "/src/main/js/services/LobbyService.ts";

export async function loader({params}) {
  console.log("loading lobby " + params.lobbyId)
  return params.lobbyId
}

export default function Lobby() {

  const { userInfo } = useRouteLoaderData("root");
  const lobbyId = useLoaderData();

  const [lobby,setLobby] = useState(null)
  const [lobbyState,setLobbyState] = useState(null)


  async function onGameEnd() {
    setLobbyState("postgame");
  }

  async function computeLobbyState(lobby) {
    setLobbyState( lobby.gameActive ? "game" : "pregame" )
  }

  useEffect(() => {

    let loadInitialLobby = async () => {
      let serviceResponse = await GetLobbySummary(lobbyId)
      let lobbyData = serviceResponse.data
      computeLobbyState(lobbyData)
      setLobby(lobbyData)
    }
    loadInitialLobby()
    console.log("after load initial")

    // SSE

    const evtSource = new EventSource("/api/lobby/"+lobbyId+"/summary/sse")
    evtSource.addEventListener("lobby_change", (e) => {
      let data = JSON.parse(e.data)
      console.log("lobby change recieved");
      console.log(data)
      computeLobbyState(data)
      setLobby(data)
    });
    console.log("after sse")
    return () => {
      console.log("closing the event source") 
      evtSource.close()
    }

  },[]);

  if (lobby == null) {
    return (<></>)
  }

  if ( lobbyState == "pregame" ) {
    return ( <PreGame lobby={lobby} onReturnToPostGame={() => {setLobbyState("postgame")}}/> )
  } else if (lobbyState == "game") {
    return ( <Game gameId={lobby.gameId} onGameEnd={onGameEnd}/> )
  } else if (lobbyState == "postgame") {
    return ( <PostGame lobby={lobby} onReturnToLobby={() => {setLobbyState("pregame")}}/> )
  } else {
    return (<></>)
  }

}
