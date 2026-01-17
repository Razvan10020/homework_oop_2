package main.Users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.Role;
import fileio.UserInput;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class User {
    private String username;
    private String email;
    private Role role;
    @JsonIgnore
    private List<String> notifications = new ArrayList<>();

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

    public void setNotifications(String s) {
        notifications.add(s);
    }
}
