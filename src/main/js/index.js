import React, { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import {
  createBrowserRouter,
  RouterProvider
} from "react-router-dom";

import Lobbies, {loader as lobbiesLoader }  from "./Lobbies.jsx";
import Lobby, {loader as lobbyLoader }      from "./Lobby.jsx";
import ErrorPage from "./Error.jsx";
import Root from "./Root.jsx";

async function rootLoader({params}) {
  console.log("loading user data")
  const userInfoResponse = await fetch("/api/user/info");
  const userInfo = await userInfoResponse.json()
  if ( userInfo == null ) {
    console.log("unable to load user data")
  }
  return { userInfo };
}

const router = createBrowserRouter([
  {
    path: "/",
    element: <Root />,
    loader: rootLoader,
    errorElement: <ErrorPage />,
    children: [
      {
        path: "lobby",
        element: <Lobbies />,
        loader: lobbiesLoader
      },
      {
        path: "lobby/:lobbyId",
        element: <Lobby />,
        loader: lobbyLoader
      }
    ]
  },
]);

const root = createRoot(document.getElementById("root"));

root.render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
