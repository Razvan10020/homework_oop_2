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

/**
 * Clasa responsabila pentru gestionarea utilizatorilor din sistem.
 */
public class UserManger {
    private final Map<String, User> users = new HashMap<>();

    /**
     * Incarca o lista de utilizatori in mapa interna.
     * @param inputUsers lista de date de intrare pentru utilizatori.
     */
    public void loadUsers(final List<UserInput> inputUsers) {
        if (inputUsers == null) {
            return;
        }

        for (UserInput input : inputUsers) {
            User user;
            if (input instanceof ManagerInput) {
                user = new Manager(input);
            } else if (input instanceof DeveloperInput) {
                user = new Developer(input);
            } else {
                user = new User(input);
            }
            users.put(input.getUsername(), user);
        }
    }

    /**
     * Verifica daca un utilizator exista in sistem.
     * @param userName numele utilizatorului cautat.
     * @return true daca utilizatorul exista, false altfel.
     */
    public boolean userExists(final String userName) {
        return users.containsKey(userName);
    }

    /**
     * Returneaza obiectul User asociat numelui.
     * @param userName numele utilizatorului.
     * @return obiectul User.
     */
    public User getUser(final String userName) {
        return users.get(userName);
    }

    /**
     * Obtine rolul unui utilizator specificat
     * @param username numele utilizatorului
     * @return rolul utilizatorului (MANAGER, DEVELOPE
     */
    public Role getRole(final String username) {
        User user = users.get(username);
        if (user == null) {
            return null;
        }
        return user.getRole();
    }
}
