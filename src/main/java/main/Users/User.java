package main.Users;

import enums.Role;
import fileio.UserInput;
import lombok.Data;

@Data
public class User {
    private String username;
    private String email;
    private Role role;

    public User(final String username,
                final String email,
                final Role role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public User(final UserInput input) {
        this.username = input.getUsername();
        this.email = input.getEmail();
        this.role = input.getRole();
    }
}
