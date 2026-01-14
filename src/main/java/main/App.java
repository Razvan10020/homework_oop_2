package main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.Role;
import fileio.ActionInput;
import fileio.UserInput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * main.App represents the main application logic that processes input commands,
 * generates outputs, and writes them to a file
 */
public class App {
    private App() {
    }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";

    private static final ObjectWriter WRITER =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static void main(String[] args) {
        run("input/in_04_test_assign.json", "out/out_04_test_assign.json");
    }

    /**
     * Runs the application: reads commands from an input file,
     * processes them, generates results, and writes them to an output file
     *
     * @param inputPath path to the input file containing commands
     * @param outputPath path to the file where results should be written
     */
    public static void run(final String inputPath, final String outputPath) {
        //keep 'outputs' variable name to be used for writing
        List<ObjectNode> outputs = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        /*
            TODO 1 :
            Load initial user data and commands. we strongly recommend using jackson library.
            you can use the reading from hw1 as a reference.
            however you can use some of the more advanced features of
            jackson library, available here: https://www.baeldung.com/jackson-annotations
        */

        //reading and loading the users
        UserManger userManger = new UserManger();

        try {
            List<UserInput> usersInput = mapper.readValue(
                    new File(INPUT_USERS_FIELD),
                    new TypeReference<>() {
                    }
            );
            userManger.loadUsers(usersInput);

        } catch (IOException e) {
            System.err.println("Eroare la încărcarea bazei de date de utilizatori: " + e.getMessage());
            return;
        }


        //reading the input of the tests
        List<ActionInput> actionInputs = new ArrayList<>();

        try {
            actionInputs = mapper.readValue(
                    new File(inputPath),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException e) {
            System.err.println("Nu s-a putut citi fișierul de acțiuni: " + e.getMessage());
        }

        // TODO 2: process commands.
        TicketManager ticketManager = new TicketManager();

        for (ActionInput actionInput : actionInputs) {
            try {
                ObjectNode response = mapper.createObjectNode();
                response.put("command", actionInput.getCommand());
                response.put("username", actionInput.getUsername());
                response.put("timestamp", actionInput.getTimestamp());

                int addRepoToOut = 1;

                switch (actionInput.getCommand()) {
                    case "viewTickets" -> {
                        ticketManager.ViewTicket(actionInput, userManger,response, mapper);
                    }
                    case "reportTicket" -> {
                        String error = ticketManager.reportTicket(actionInput, userManger, response);
                        if (error != null) {
                            response.put("error", error);
                        }
                        else  {
                            addRepoToOut = 0;
                        }
                    }
                    case "lostInvestors" -> {
                        Role role = userManger.getRole(actionInput.getUsername());
                        if (!Role.MANAGER.equals(role)) {
                            response.put("error", "Only managers can execute this command.");
                        }
                        else {
                            addRepoToOut = 0;
                        }
                    }
                    case "createMilestone" -> {
                        String error = ticketManager.createMilestone(actionInput, userManger, response);
                        if (error != null) {
                            response.put("error", error);
                        }
                        else  {
                            addRepoToOut = 0;
                        }
                    }
                    case "viewMilestones" -> {
                        ticketManager.ViewMilestones(actionInput, userManger,response, mapper);
                    }
                    case "assignTicket" -> {
                        String error = ticketManager.assignTicket(actionInput, userManger, response);
                        if (error != null) {
                            response.put("error", error);
                        }
                        else  {
                            addRepoToOut = 0;
                        }
                    }
                    case "undoAssignTicket" -> {
                        String error = ticketManager.undoAssignTicket(actionInput, userManger, response);
                        if (error != null) {
                            response.put("error", error);
                        }
                        else  {
                            addRepoToOut = 0;
                        }
                    }
                    case "viewAssignedTickets" -> {
                        ticketManager.ViewAssignedTickets(actionInput, userManger,response, mapper);
                    }
                    case "addComment" -> {
                        String error = ticketManager.addComment(actionInput, userManger, response);
                        if (error != null) {
                            response.put("error", error);
                        }
                        else  {
                            addRepoToOut = 0;
                        }
                    }
                    case "undoAddComment" -> {
                        String error = ticketManager.undoAddComment(actionInput, userManger, response);
                        if (error != null) {
                            response.put("error", error);
                        }
                        else  {
                            addRepoToOut = 0;
                        }
                    }
                    default -> System.out.println("Comandă necunoscută: " + actionInput.getCommand());
                }

                if (addRepoToOut > 0) {
                    outputs.add(response);
                }

            } catch (Exception e) {
                System.err.println("Eroare la procesarea comenzii " + actionInput.getCommand() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // TODO 3: create objectnodes for output, add them to outputs list.

        // DO NOT CHANGE THIS SECTION IN ANY WAY
        try {
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            WRITER.withDefaultPrettyPrinter().writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("error writing to output file: " + e.getMessage());
        }
    }
}
