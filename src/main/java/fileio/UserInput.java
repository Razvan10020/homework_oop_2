package fileio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import enums.Role;
import fileio.UserInputP.DeveloperInput;
import fileio.UserInputP.ManagerInput;
import fileio.UserInputP.ReporterInput;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "role",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeveloperInput.class, name = "DEVELOPER"),
        @JsonSubTypes.Type(value = ManagerInput.class, name = "MANAGER"),
        @JsonSubTypes.Type(value = ReporterInput.class, name = "REPORTER")
})
public class UserInput {
    private String username;
    private Role role;
    private String email;
}
