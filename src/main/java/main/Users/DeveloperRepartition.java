package main.Users;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeveloperRepartition {
    private String developer;
    private List<Integer> assignedTickets = new ArrayList<>();

    public DeveloperRepartition(String developer) {
        this.developer = developer;
    }
}
