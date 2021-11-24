package jsonClasses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlaceList {
    private @JsonProperty("hits") List<Place> placeList;
}
