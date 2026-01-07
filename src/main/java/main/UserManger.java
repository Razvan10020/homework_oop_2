package main;

import enums.Role;
import fileio.UserInputP.DeveloperInput;
import fileio.UserInputP.ManagerInput;
import fileio.UserInput;
import main.Users.Developer;
import main.Users.Manager;
import main.Users.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManger {
    private final Map<String, User> users = new HashMap<>();

    public void loadUsers(List<UserInput> inputUsers) {
        if (inputUsers == null) return;

        for (UserInput input : inputUsers) {
            User user;
            if(input instanceof ManagerInput) {
                user = new Manager(input);
            }
            else if(input instanceof DeveloperInput) {
               user = new Developer(input);
            }
            else {
                user = new User(input);
            }
            users.put(input.getUsername(), user);
        }
    }

    public boolean userExists(String userName) {
        return users.containsKey(userName);
    }

    public User getUser(String userName) {
        return users.get(userName);
    }

    public Role getRole(String username) {
        return users.get(username).getRole();
    }
}
