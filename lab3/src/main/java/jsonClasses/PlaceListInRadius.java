package jsonClasses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlaceListInRadius {
    private @JsonProperty(" ") List<PlaceInRadius> placeListInRadius;
}

