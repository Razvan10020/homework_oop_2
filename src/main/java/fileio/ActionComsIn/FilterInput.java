package fileio.ActionComsIn;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import enums.BusinessPriority;
import enums.ExpertiseArea;
import enums.SearchType;
import enums.Seniority;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class FilterInput {
    private BusinessPriority businessPriority;
    private String type;
    private String createdAt;
    private String createdBefore;
    private String createdAfter;
    private Boolean availableForAssignment;

    //filters for managers
    //all of the above
    private List<String> keywords;
    private ExpertiseArea expertiseArea;
    private Seniority seniority;
    private Integer performanceScoreAbove;
    private Integer performanceScoreBelow;
    private SearchType searchType;

    public boolean isDeveloperSearch() {
        if (SearchType.DEVELOPER.equals(searchType)) {
            return true;
        }
        return false;
    }
}
