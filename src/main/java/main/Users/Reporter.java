package main.Users;

import enums.Role;
import fileio.UserInput.UserInput;

public class Reporter extends User{

    public Reporter(UserInput userInput) {
        super(userInput.getUsername(),
                userInput.getEmail(),
                userInput.getRole());
    }
}
