package fileio.TicketInput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class ActionInput {
    private String command;
    private String username;
    private String timestamp;
    private ParamsInput params;
}
