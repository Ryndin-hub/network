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

    private void findDescription(String xid, View view) throws IOException {
        PlaceWithDescription placeWithDescription = apiParser.placeDescriptionByXid(xid);
        if (null != placeWithDescription.getInfo() && null != placeWithDescription.getDescr()) {
            view.updateDescription(xid, (placeWithDescription.getDescr()));
        } else if (null != placeWithDescription.getWikipedia_extracts() && null != placeWithDescription.getText()) {
            view.updateDescription(xid, (placeWithDescription.getText()));
        } else view.updateDescription(xid,"отсутствует");
    }

    private void findWeather(Position position, View view){
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
                    findWeather(currentPlaceList.get(index).getPosition(),view);
                    for (PlaceInRadius place : placeInRadiusList){
                        try {
                            findDescription(place.getXid(),view);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
