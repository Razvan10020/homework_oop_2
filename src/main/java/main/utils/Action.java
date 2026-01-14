package main.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "milestone", "from", "to", "by",
        "timestamp", "action"
})
public class Action {
    private String milestone;
    private String by;
    private String timestamp;
    private enums.Action action;
    private Status from;
    private Status to;
}
