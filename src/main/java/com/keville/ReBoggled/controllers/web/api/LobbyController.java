package com.keville.ReBoggled.controllers.web.api;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.keville.ReBoggled.DTO.LobbyNewMessageDTO;
import com.keville.ReBoggled.DTO.LobbySummaryDTO;
import com.keville.ReBoggled.DTO.LobbyUpdateDTO;
import com.keville.ReBoggled.sse.LobbyMessageSseDispatcher;
import com.keville.ReBoggled.sse.LobbySseDispatcher;
import com.keville.ReBoggled.sse.context.LobbyContext;
import com.keville.ReBoggled.sse.context.LobbyMessageContext;
import com.keville.ReBoggled.model.lobby.LobbyUpdate;
import com.keville.ReBoggled.model.lobby.Lobby;
import com.keville.ReBoggled.model.user.User;
import com.keville.ReBoggled.service.lobbyService.LobbyService;
import com.keville.ReBoggled.service.lobbyService.LobbyServiceException;
import com.keville.ReBoggled.service.userService.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

  public static final String baseRoute = "/api/lobby";
  private static final Logger LOG = LoggerFactory.getLogger(LobbyController.class);

  private LobbyService lobbyService;
  private UserService userService;

  private LobbySseDispatcher lobbySseDispatcher;
  private LobbyMessageSseDispatcher lobbyMessageSseDispatcher;

  public LobbyController(@Autowired LobbyService lobbyService,
      @Autowired UserService userService,
      @Autowired LobbySseDispatcher lobbySseDispatcher,
      @Autowired LobbyMessageSseDispatcher lobbyMessageSseDispatcher) {

    this.lobbyService = lobbyService;
    this.userService = userService;
    this.lobbySseDispatcher = lobbySseDispatcher;
    this.lobbyMessageSseDispatcher = lobbyMessageSseDispatcher;
  }

  @GetMapping("")
  public ResponseEntity<?> getLobbies(HttpSession session) {

    Iterable<Lobby> lobbies = lobbyService.getLobbies();
    return new ResponseEntity<Iterable<Lobby>>(lobbies,HttpStatus.OK);

  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getLobby(
      @PathVariable("id") Integer id,
      @Autowired HttpSession session) {

    try {  
      Lobby lobby = lobbyService.getLobby(id);
      return new ResponseEntity<Lobby>(lobby,HttpStatus.OK);
    } catch (LobbyServiceException e) {
      return handleLobbyServiceException(e);
    }

  }

  //TODO : Check if the user participates in the lobby

  @GetMapping("/{id}/messages/sse")
  public SseEmitter getLobbyMessageSSE(
      @PathVariable("id") Integer id,
      @Autowired HttpSession session) {

    Integer userId = (Integer) session.getAttribute("userId");

    LobbyMessageContext context = new LobbyMessageContext(userId,id);
    return lobbyMessageSseDispatcher.register(context);

  }

  //TODO : Check if the user participates in the lobby

  @GetMapping("/{id}/summary/sse")
  public SseEmitter getLobbySSE(
      @PathVariable("id") Integer id,
      @Autowired HttpSession session) {

    Integer userId = (Integer) session.getAttribute("userId");

    LobbyContext context = new LobbyContext(userId,id);
    return lobbySseDispatcher.register(context);

  }

  @PostMapping("/{id}/messages")
    public ResponseEntity<?> addLobbyMessage(
        @PathVariable("id") Integer id,
        @Valid @RequestBody LobbyNewMessageDTO lobbyNewMessageDTO,
        @Autowired HttpSession session,
        @Autowired BindingResult bindingResult) {

      if ( bindingResult.hasErrors() ) {
          LOG.info(String.format("Invalid Request Body"));
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WRONG_BODY");
      }

      Integer userId = (Integer) session.getAttribute("userId");

      try {

        Lobby lobby = lobbyService.addMessageToLobby(lobbyNewMessageDTO,id,userId);
        return new ResponseEntity<Lobby>(lobby,HttpStatus.OK);

      } catch (LobbyServiceException e) {
        return handleLobbyServiceException(e);
      }

    }

  @GetMapping("/summary")
  public ResponseEntity<?> getLobbyViews(HttpSession session,
      @RequestParam(required = false, name = "publicOnly") boolean publicOnly
      ) {

    try { 
    Iterable<LobbySummaryDTO> lobbies = lobbyService.getLobbySummaryDTOs();
    return new ResponseEntity<Iterable<LobbySummaryDTO>>(lobbies,HttpStatus.OK);
    } catch (LobbyServiceException e) {
      return handleLobbyServiceException(e);
    }

  }

  @GetMapping("/{id}/summary")
  public ResponseEntity<?> getLobbyView(@PathVariable("id") Integer id,
      HttpSession session) {

    try { 
      LobbySummaryDTO lobby = lobbyService.getLobbySummaryDTO(id);
      return new ResponseEntity<LobbySummaryDTO>(lobby,HttpStatus.OK);
    } catch (LobbyServiceException e) {
      return handleLobbyServiceException(e);
    }

  }

  @GetMapping("/invite")
  public ResponseEntity<?> getInvite(HttpSession session) {

    try { 

      Integer userId = (Integer) session.getAttribute("userId");

      if ( userId == null ) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
      }
      
      String inviteLink = lobbyService.getUserInviteLink(userId);
      return new ResponseEntity<String>(inviteLink,HttpStatus.OK);

    } catch (LobbyServiceException e) {
      return handleLobbyServiceException(e);
    }

  }

  @PostMapping("/{id}/join")
  public ResponseEntity<?> joinLobby (
      @PathVariable("id") Integer id,
      @Autowired HttpSession session,
      @RequestParam(required = false, name = "token") String token
      ) {

    Integer userId = (Integer) session.getAttribute("userId");
    if (userId == null) {
      LOG.warn("unable to identify the userId of the current Session");
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    try {
      Lobby lobby = lobbyService.addUserToLobby(userId, id, token == null ? Optional.empty() : Optional.of(token));
      return new ResponseEntity<Lobby>(lobby,HttpStatus.OK);
    } catch (LobbyServiceException e)  {
      handleLobbyServiceException(e);
      return ResponseEntity.internalServerError().build();
    }

  }

  @PostMapping("/{id}/kick/{userId}")
  public ResponseEntity<?> kickPlayer(
      @PathVariable("id") Integer id,
      @PathVariable("userId") Integer userId, //To be kicked
      @Autowired HttpSession session) {

    try {
      Integer requesterId = (Integer) session.getAttribute("userId");
      verifyLobbyOwner(id,requesterId);
      Optional<Lobby> lobbyOpt = lobbyService.removeUserFromLobby(userId, id,true);
      if ( lobbyOpt.isPresent() ) {
        return new ResponseEntity<Lobby>(lobbyOpt.get(),HttpStatus.OK);
      }
      return ResponseEntity.ok().build();
    } catch (LobbyServiceException e) {
      return handleLobbyServiceException(e);
    }

  }

  @PostMapping("/{id}/promote/{userId}")
  public ResponseEntity<?> promotePlayer(
      @PathVariable("id") Integer id,
      @PathVariable("userId") Integer userId, //To be kicked
      @Autowired HttpSession session) {

    try {
      Integer requesterId = (Integer) session.getAttribute("userId");
      verifyLobbyOwner(id,requesterId);
      Lobby lobby = lobbyService.transferLobbyOwnership(id,userId);
      return new ResponseEntity<Lobby>(lobby,HttpStatus.OK);
    } catch (LobbyServiceException e) {
      return handleLobbyServiceException(e);
    }

  }

  @PostMapping("/{id}/leave")
  public ResponseEntity<?> leaveLobby(
      @PathVariable("id") Integer id,
      @Autowired HttpSession session) {

    Integer userId = (Integer) session.getAttribute("userId");

    try { 
      Optional<Lobby> lobby = lobbyService.removeUserFromLobby(userId,id,false);
      return new ResponseEntity<Lobby>(lobby.isPresent() ? lobby.get() : null,HttpStatus.OK);
    } catch (LobbyServiceException e) {
      return handleLobbyServiceException(e);
    }

  }

  @PostMapping("/{id}/update")
    public ResponseEntity<?> updateLobby(
        @PathVariable("id") Integer id,
        @Valid @RequestBody LobbyUpdateDTO lobbyUpdateDTO,
        @Autowired HttpSession session,
        @Autowired BindingResult bindingResult) {

      if ( bindingResult.hasErrors() ) {
          LOG.info(String.format("Invalid Request Body"));
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WRONG_BODY");
      }

      try {

        Integer userId = (Integer) session.getAttribute("userId");
        verifyLobbyOwner(id,userId);

        //Map DTO to Domain 
        LobbyUpdate lobbyUpdate= new LobbyUpdate(id,lobbyUpdateDTO);

        Lobby lobby = lobbyService.update(lobbyUpdate);
        return new ResponseEntity<Lobby>(lobby,HttpStatus.OK);

      } catch (LobbyServiceException e) {
        return handleLobbyServiceException(e);
      }

    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startGame(
        @PathVariable("id") Integer id,
        @Autowired HttpSession session ) {

      try {

        Integer userId = (Integer) session.getAttribute("userId");
        verifyLobbyOwner(id,userId);

        Lobby lobby = lobbyService.startGame(id);
        return new ResponseEntity<Lobby>(lobby,HttpStatus.OK);

      } catch (LobbyServiceException e) {
        return handleLobbyServiceException(e);
      }

    }

  @PostMapping("/create")
    public ResponseEntity<?>  createLobby(
        @Autowired HttpSession session) {

      Integer userId = (Integer) session.getAttribute("userId");
      User user = userService.getUser(userId);

      try {
        Lobby lobby = lobbyService.createNew(userId);
        return new ResponseEntity<Lobby>(lobby,HttpStatus.CREATED);
      } catch (LobbyServiceException e) {
        return handleLobbyServiceException(e);
      }

    }

  @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLobby(
        @PathVariable("id") Integer id,
        @Autowired HttpSession session) {

      try {
        Integer userId = (Integer) session.getAttribute("userId");
        verifyLobbyOwner(id,userId);

        lobbyService.delete(id);
        return ResponseEntity.ok().build();
      } catch (LobbyServiceException e) {
        return handleLobbyServiceException(e);
      }

    }


  /*
  I really wanted to allow my controller routes to throw LobbServiceExceptions
  and transform them into rethrown ResponseStatusException so I can filter
  and modify what errors get sent to the client without engineering a custom
  error response. However, this can't work the way I want. The default mechanism
  to handle ResponseStatusException (ResponseStatusExceptionHandler) listens on scope
  of controller methods. 

  A method of class with @ExceptionHandler exists outside the scope, that is
  a ResponseStatusException is thrown inside a custom @ExceptionHandler does not
  get caught by the default mechanism.

  I could write my own custom errror response using ResponseEntity, however
  I want to leverage springs (more so boot) builtin error response. So we
  opt for try catch and explict handler invocation.

  We could also find a way to register the default implementation 
  @ResponseStatusExceptionHandler as a @ExceptionHandler in the proper scope
  but currently I am fed up with this task am moving on with the above solution.

  FIXME : This is very hacky 

  The way I have controller methods returning
  a ResponseEntity<?> , but if they fail they catch and return 
  handleLobbyServiceException(e) with the false expectation of ever getting
  a ResponseEntity<?> when this method will always throw and be handled by 
  springs default ResponseStatusException handler.
  */
  public ResponseEntity<?> handleLobbyServiceException(LobbyServiceException e) {

    switch (e.error) {
      case LOBBY_FULL:
        throw new ResponseStatusException(HttpStatus.CONFLICT, "LOBBY_IS_FULL");
      case LOBBY_PRIVATE:
        throw new ResponseStatusException(HttpStatus.CONFLICT, "LOBBY_IS_PRIVATE");
      case GUEST_NOT_ALLOWED:
        throw new ResponseStatusException(HttpStatus.CONFLICT, "GUEST_NOT_ALLOWED");
      case USER_NOT_IN_LOBBY:
        throw new ResponseStatusException(HttpStatus.CONFLICT, "NOT_IN_LOBBY");
      case CAPACITY_SHORTENING:
        throw new ResponseStatusException(HttpStatus.CONFLICT, "CAPACITY_SHORTENING_CONFLICT");
      case LOBBY_NOT_FOUND:
        throw new ResponseStatusException(HttpStatus.CONFLICT, "LOBBY_NOT_FOUND");
      case ERROR:
      default:
        LOG.error("unexpected Lobby Service Error : " + e.getMessage());
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

  }

  //  TODO : (Authorization) does not belong in the controller ...
  private void verifyLobbyOwner(Integer lobbyId,Integer userId) throws LobbyServiceException {

      Integer ownerId = lobbyService.getLobbyOwnerId(lobbyId);

      if ( ownerId == null ) {
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
      }

      if ( !ownerId.equals(userId) ) {
          LOG.info(String.format("Non Lobby Owner %d is trying to manipulate lobby %d",userId,lobbyId));
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT_AUTHORIZED");
      }

  }

}
