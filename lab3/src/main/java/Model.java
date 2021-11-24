import lombok.SneakyThrows;
import java.io.IOException;
import java.util.List;
import jsonClasses.*;

public class Model {
    View view;
    List<Place> currentPlaceList;
    List<PlaceInRadius> currentPlaceInRadiusList;
    APIParser apiParser = new APIParser();

    private class FindDescription extends Thread{
        String xid;
        View view;
        @SneakyThrows
        public void run(){
            PlaceWithDescription placeWithDescription = apiParser.placeDescriptionByXid(xid);
            if (null != placeWithDescription.getInfo() && null != placeWithDescription.getDescr()) {
                view.updateDescription(xid, (placeWithDescription.getDescr()));
            } else if (null != placeWithDescription.getWikipedia_extracts() && null != placeWithDescription.getText()) {
                view.updateDescription(xid, (placeWithDescription.getText()));
            } else view.updateDescription(xid,"отсутствует");
        }
    }

    private class FindWeather extends Thread{
        Position position;
        View view;
        @SneakyThrows
        public void run(){
            PlaceWithWeather place = apiParser.getWeatherByPosition(position);
            view.updateWeather(place);
        }
    }

    public void findPlacesInRadius(int index) throws IOException {
        List<PlaceInRadius> placeInRadiusList = apiParser.getPlacesInRadius(currentPlaceList.get(index).getPosition());
        placeInRadiusList.removeIf(place -> place.getName().equals(""));
        currentPlaceInRadiusList = placeInRadiusList;
        view.setPlaces(placeInRadiusList);
        FindWeather findWeather = new FindWeather();
        findWeather.view = view;
        findWeather.position = currentPlaceList.get(index).getPosition();
        findWeather.start();
        for (PlaceInRadius place : placeInRadiusList){
            FindDescription findDescription = new FindDescription();
            findDescription.view = view;
            findDescription.xid = place.getXid();
            findDescription.start();
        }
    }

    public void findPlaces(String placeName) throws IOException {
        List<Place> placeList = apiParser.getPlacesByName(placeName);
        currentPlaceList = placeList;
        view.updateComboBox(placeList);
    }

    public Model(View _view){
        view = _view;
    }
}
