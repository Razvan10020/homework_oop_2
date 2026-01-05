package fileio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import enums.BusinessValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class UiFeedbackInput {
    private String uiElement;
    private BusinessValue businessValue;
    private String usabilityScore;
    private String screenshotUrl;
    private String suggestedFix;
}
