package main.Users;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeveloperRepartition {
    private String developer;
    private List<Integer> assignedTickets = new ArrayList<>();

    public DeveloperRepartition(final String developer) {
        this.developer = developer;
    }

    public DeveloperRepartition(final String developer, final int ticketId) {
        this.developer = developer;
        this.assignedTickets.add(ticketId);
    }
}
