import lombok.SneakyThrows;
import java.io.IOException;
import java.util.List;
import jsonClasses.*;
import java.util.concurrent.CompletableFuture;

public class Model {
    private View view;
    private List<Place> currentPlaceList;
    private List<PlaceInRadius> currentPlaceInRadiusList;
    private APIParser apiParser = new APIParser();

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
            CompletableFuture.supplyAsync(() -> {
                try {
                    return apiParser.getWeatherByPosition(position);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            })
                    .thenAcceptAsync(place -> {
                        view.updateWeather(place);
                    });
        }
    }

    public void findPlacesInRadius(int index){
        CompletableFuture.supplyAsync(() -> {
            try {
                return apiParser.getPlacesInRadius(currentPlaceList.get(index).getPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        })
                .thenAcceptAsync(placeInRadiusList -> {
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
                });
    }

    public void findPlaces(String placeName){
        CompletableFuture.supplyAsync(() -> {
            try {
                return apiParser.getPlacesByName(placeName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        })
                .thenAcceptAsync(placeList -> {
                    if (placeList == null) return;
                    currentPlaceList = placeList;
                    view.updateComboBox(placeList);
                });
    }

    public Model(View _view) throws IOException {
        view = _view;
    }
}
