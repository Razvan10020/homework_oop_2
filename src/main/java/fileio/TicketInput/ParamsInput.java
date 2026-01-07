package fileio.TicketInput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import enums.BusinessPriority;
import enums.ExpertiseArea;
// ATENȚIE: Verifică dacă mai folosești ActionParams.
// Dacă ai scos-o din ActionInput, o poți scoate și de aici.
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY, // Schimbat în EXISTING_PROPERTY
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BugInput.class, name = "BUG"),
        @JsonSubTypes.Type(value = FeatureRequestInput.class, name = "FEATURE_REQUEST"),
        @JsonSubTypes.Type(value = UiFeedbackInput.class, name = "UI_FEEDBACK")
})
public class ParamsInput { // Am scos "implements ActionParams" pentru a evita conflictele de pachete
    protected String type;
    protected String title;
    protected BusinessPriority businessPriority;
    protected ExpertiseArea expertiseArea;
    protected String description;
    protected String reportedBy;
}