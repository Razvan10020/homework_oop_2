package fileio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import enums.BusinessPriority;
import enums.ExpertiseArea;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BugInput.class, name = "BUG"),
        @JsonSubTypes.Type(value = FeatureRequestInput.class, name = "FEATURE_REQUEST"),
        @JsonSubTypes.Type(value = UiFeedbackInput.class, name = "UI_FEEDBACK")
})
public class ParamsInput {
    protected String type;
    protected String title;
    protected BusinessPriority businessPriority;
    protected ExpertiseArea expertiseArea;
    protected String description;
    protected String reportedBy;
}
