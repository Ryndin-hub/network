package jsonClasses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class PlaceDescriptionText {
    private String text;
}

@Getter
@Setter
class PlaceDescriptionDescr {
    private String Descr;
}

@Getter
@Setter
public class PlaceWithDescription {
    private PlaceDescriptionText wikipedia_extracts;
    private PlaceDescriptionDescr info;
    public String getText(){
        return wikipedia_extracts.getText();
    }
    public String getDescr(){
        return info.getDescr();
    }
}
