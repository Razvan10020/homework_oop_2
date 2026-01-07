package main.Users;

import fileio.UserInput;

public class Reporter extends User{

    public Reporter(UserInput userInput) {
        super(userInput.getUsername(),
                userInput.getEmail(),
                userInput.getRole());
    }
}
