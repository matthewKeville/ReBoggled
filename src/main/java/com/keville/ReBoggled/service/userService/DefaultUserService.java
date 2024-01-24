package com.keville.ReBoggled.service.userService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

import com.keville.ReBoggled.model.user.User;
import com.keville.ReBoggled.repository.UserRepository;

@Component
public class DefaultUserService implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private UserRepository users;

    public DefaultUserService(
        @Autowired UserRepository users) {
      this.users = users;
    }

    public User getUser(int id) {
      Optional<User> optUser = users.findById(id);
      if ( optUser.isPresent() ) {
        return users.findById(id).get();
      }
      return null;
    }

    public User getUserByUsername(String username) throws UsernameNotFoundException {
      Optional<User> optUser = users.findByUsername(username);
      if ( ! optUser.isPresent() ) {
        throw new UsernameNotFoundException("can't locate user " + username);
      }
      return optUser.get();
    }

    public void addLobby(User user) {
      users.save(user);
    }

    @Override
    public User createUser(User user) {

      User newUser = (User) user;
      return users.save(newUser);

    }

    //UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      Optional<User> user = users.findByUsername(username);
      if ( user.isEmpty() ) {
        throw new UsernameNotFoundException("can't locate user " + username);
      }
      return user.get();
    }

}
