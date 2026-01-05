package fileio.TicketInput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import enums.BusinessValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class UiFeedbackInput extends ParamsInput {
    private String uiElementId;
    private BusinessValue businessValue;
    private String usabilityScore;
    private String screenshotUrl;
    private String suggestedFix;
}
