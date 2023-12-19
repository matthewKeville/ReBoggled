package com.keville.ReBoggled;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import com.keville.ReBoggled.model.game.BoardSize;
import com.keville.ReBoggled.model.game.BoardTopology;
import com.keville.ReBoggled.model.game.FindRule;
import com.keville.ReBoggled.model.game.GameSettings;
import com.keville.ReBoggled.model.lobby.Lobby;
import com.keville.ReBoggled.model.user.User;
import com.keville.ReBoggled.repository.LobbyRepository;
import com.keville.ReBoggled.repository.UserRepository;
import com.keville.ReBoggled.service.LobbyService;
import com.keville.ReBoggled.service.exceptions.LobbyServiceException;

@SpringBootApplication
public class ReBoggledApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ReBoggledApplication.class);
  private static boolean skipCreateDevData = true;

  @Autowired
  private LobbyService lobbyService;

  public static void main(String[] args) {
    SpringApplication.run(ReBoggledApplication.class, args);
  }

  @Bean
  public UserDetailsManager users(DataSource dataSource) {
    JdbcUserDetailsManager users = new JdbcUserDetailsManager (dataSource);
    return users;
  }

  @Order(value=0)
  @Bean
  CommandLineRunner processArgs() {
    return args -> {

      LOG.info("Parsing Arguments");

      for ( String arg : args ) {

        String[] argParts = arg.split("=");

        if (argParts.length != 2) {
          LOG.warn("invalid argument : " + arg);
          continue;
        }

        try {

          String prop = argParts[0];
          String value = argParts[1];

          if ( prop.equals("--create-dev-data") ) {
            skipCreateDevData = !Boolean.parseBoolean(value);
          }

        } catch (Exception e)  {

          LOG.error("error processing argument " + arg);
          continue;

        }

      }

    };
  }

  @Order(value=2)
  @Bean
  CommandLineRunner createDevData(
    @Autowired LobbyRepository lobbies,
    @Autowired UserRepository users,
    @Autowired UserDetailsManager userDetailsManager
    ) {

    return args -> {

      if ( skipCreateDevData ) {
        LOG.info("skipping creation of dev data");
        return;
      }

      // Development User Auth

      UserDetails mattDetails = org.springframework.security.core.userdetails.User.builder()
        .username("matt@email.com")
        .password("{noop}test") //use no op password encoder
        .roles("SA")
        .authorities("read")
        .build();

      UserDetails aliceDetails = org.springframework.security.core.userdetails.User.builder()
        .username("alice@email.com")
        .password("{noop}guest") //use no op password encoder
        .roles("user")
        .authorities("read")
        .build();

      UserDetails bobDetails = org.springframework.security.core.userdetails.User.builder()
        .username("bob@email.com")
        .password("{noop}guest") //use no op password encoder
        .roles("user")
        .authorities("read")
        .build();

      UserDetails charlieDetails = org.springframework.security.core.userdetails.User.builder()
        .username("charlie@email.com")
        .password("{noop}guest") //use no op password encoder
        .roles("user")
        .authorities("read")
        .build();

      UserDetails danDetails = org.springframework.security.core.userdetails.User.builder()
        .username("dan@email.com")
        .password("{noop}guest") //use no op password encoder
        .roles("user")
        .authorities("read")
        .build();

      UserDetails meDetails = org.springframework.security.core.userdetails.User.builder()
        .username("emily@email.com")
        .password("{noop}guest") //use no op password encoder
        .roles("user")
        .authorities("read")
        .build();
  
  
      userDetailsManager.createUser(mattDetails);
      userDetailsManager.createUser(aliceDetails);
      userDetailsManager.createUser(bobDetails);
      userDetailsManager.createUser(charlieDetails);
      userDetailsManager.createUser(danDetails);

      // Development User Data
      //
      try {

      User matt = users.save(User.createUser("matt@email.com", "fake"));
      AggregateReference<User, Integer> mattRef = AggregateReference.to(matt.id);

      User alice = users.save(User.createUser("alice@email.com", "alice"));
      User bob = users.save(User.createUser("bob@email.com", "bob42"));

      AggregateReference<User, Integer> bobRef = AggregateReference.to(bob.id);
      User charlie = users.save(User.createUser("charlie@email.com", "bigCharles"));
      User dan = users.save(User.createUser("dan@email.com", "thePipesArePlaying"));
      User emily = users.save(User.createUser("emily@email.com", "empemjem"));

      // Development Lobby Data

      //if we add users to any private lobby we throw here TBD

      //Lobby secret = new Lobby("Secret Dungeon", 4, false, mattRef);
      Lobby secret = lobbyService.createNew(matt.id);
      lobbyService.addUserToLobby(alice.id, secret.id);

      GameSettings gameSettings = new GameSettings(BoardSize.FIVE, BoardTopology.CYLINDER, FindRule.UNIQUE, 120);
      Lobby roomA = lobbies.save(new Lobby("Room A", 2, false, AggregateReference.to(charlie.id), gameSettings));
      lobbyService.addUserToLobby(charlie.id, roomA.id);
      lobbyService.addUserToLobby(dan.id, roomA.id);

      lobbies.save(new Lobby("The Purple Lounge", 12, false, bobRef));

      Lobby single = lobbies.save(new Lobby("The Single", 1, false, AggregateReference.to(emily.id)));
      lobbyService.addUserToLobby(emily.id, single.id);

      } catch ( LobbyServiceException lse) {
        LOG.error(lse.getMessage());
      }

    };
  }

  static AggregateReference<User, Integer> ARof(User user) {
    return AggregateReference.to(user.id);
  }

}
