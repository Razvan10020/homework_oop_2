package fileio.ActionComsIn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import enums.BusinessPriority;
import enums.ExpertiseArea;
import enums.Seniority;
import enums.Status;
import main.Commands.Comment;
import main.utils.Views;

import java.util.ArrayList;
import java.util.List;

public class FilterInput {
    private BusinessPriority businessPriority;
    private String type;
    private String createdAt;
    private String createdBefore;
    private String createdAfter;
    private String availableForAssignment;

    //filters for managers
    //all of the above
    private List<String> keywords;
    private ExpertiseArea expertiseArea;
    private Seniority seniority;
    private int performanceScoreAbove;
    private int performanceScoreBelow;
}
