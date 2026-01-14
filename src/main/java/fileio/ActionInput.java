package fileio;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import fileio.ActionComsIn.AddCommentInput;
import fileio.ActionComsIn.AssignTicketInput;
import fileio.ActionComsIn.FilterInput;
import fileio.ActionComsIn.MilestoneInput;
import fileio.TicketInput.ParamsInput;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ActionInput {
    private String command;
    private String username;
    private String timestamp;

    // 1. Păstrăm ParamsInput pentru comenzile care vin cu obiectul "params" în JSON
    private ParamsInput params;

    // 2. Colectăm toate restul câmpurilor de la root (name, dueDate, tickets etc.)
    private Map<String, Object> otherProps = new HashMap<>();

    private FilterInput filters;

    /**
     * Metoda folosita pentru a adauga diferite campuri în mapa
     * @param value Valoarea asociată cheii.
     */
    @JsonAnySetter
    public void addOther(final String key, final Object value) {
        otherProps.put(key, value);
    }

    // --- LOGICA DE CONVERSIE ---
    /**
     * In functie de comanda schimba ce parametri trec si pot fi alesi
     *
     */
    public ParamsInput asParams() {
        // Dacă comanda e createMilestone, ignorăm ParamsInput
        if ("createMilestone".equals(command)) {
            return null;
        }

        // Dacă datele au venit în interiorul obiectului "params" { ... }
        if (this.params != null) {
            return this.params;
        }

        // Dacă e reportTicket dar datele sunt la root (flat)
        if ("reportTicket".equals(command) && !otherProps.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(otherProps, ParamsInput.class);
        }
        return null;
    }

    /**
     * In functie de comanda schimba ce parametri trec si pot fi alesi
     *
     */
    public MilestoneInput asMilestone() {
        // Dacă nu e comanda de milestone, nu încercăm conversia
        if (!"createMilestone".equals(command)) {
            return null;
        }

        // Convertim Map-ul "otherProps" (unde s-au dus name, dueDate etc.) în MilestoneInput
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.convertValue(otherProps, MilestoneInput.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * In functie de comanda schimba ce parametri trec si pot fi alesi
     * @return
     */
    public AssignTicketInput asAssignTicket() {
        if (!"assignTicket".equals(command) && !"undoAssignTicket".equals(command)
            && !"changeStatus".equals(command) && !"undoChangeStatus".equals(command)) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.convertValue(otherProps, AssignTicketInput.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * In functie de comanda schimba ce parametri trec si pot fi alesi
     *
     */
    public AddCommentInput asAddComment() {
        if (!"addComment".equals(command)) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.convertValue(otherProps, AddCommentInput.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * In functie de comanda schimba ce parametri trec si pot fi alesi
     *
     */
    public AddCommentInput asUndoAddComment() {
        if (!"undoAddComment".equals(command)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.convertValue(otherProps, AddCommentInput.class);
        } catch (Exception e) {
            return null;
        }
    }

    public FilterInput asFilter() {
        if (!"search".equals(command)) {
            return null;
        }
        if (filters != null) {
            return this.filters;
        }
    }
}
