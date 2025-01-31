package com.keville.flummox.DTO;

public class RegisterUserRequestDTO {

    private String username;
    private String email;
    private String password;
    private String passwordConfirmation;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getPasswordConfirmation() {
      return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
      this.passwordConfirmation = passwordConfirmation;
    }

}
