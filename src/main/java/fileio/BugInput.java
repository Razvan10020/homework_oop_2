package fileio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import enums.Frequency;
import enums.Severity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class BugInput extends ParamsInput {
    private String expectedBehavior;
    private String actualBehavior;
    private Frequency frequency;
    private Severity severity;
    //optional variables
    private String environment;
    private String errorCode;
}
