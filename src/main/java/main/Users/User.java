package main.Users;

import enums.Role;
import fileio.UserInput.UserInput;
import lombok.Data;

@Data
public class User {
    private String username;
    private String email;
    private Role role;

    public User(String username,
                String email,
                Role role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public User(UserInput input) {
        this.username = input.getUsername();
        this.email = input.getEmail();
        this.role = input.getRole();
    }
}
