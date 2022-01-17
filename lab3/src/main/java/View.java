import lombok.SneakyThrows;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jsonClasses.*;

public class View {
    private JFrame frame;
    private JPanel content;
    private JComboBox<String> comboBox;
    private JTextArea text;
    private Model model;
    private List<PlaceWithDescriptionView> placeWithDescriptionsList = new ArrayList<>();
    private Weather weather = new Weather();

    public void updateText(){
        String txt = "";
        if ("" != weather.sky){
            txt += "Погода: " + weather.temperature + " ℃, " + weather.sky + "\n" + "\n";
        }
        if (placeWithDescriptionsList.isEmpty()){
            txt += "Поблизости ничего не найдено";
            text.setText(txt);
            return;
        }
        for (PlaceWithDescriptionView place : placeWithDescriptionsList){
            txt += place.name + "\n";
            if ("" != place.description) txt += "Описание: " + place.description + "\n";
            txt += "\n";
        }
        text.setText(txt);
    }

    public void updateWeather(PlaceWithWeather place){
        weather.temperature = place.getTemp();
        weather.sky = Translator.translateWeather(place.getWeather());
        updateText();
    }

    public void updateDescription(String xid, String description){
        for (PlaceWithDescriptionView place : placeWithDescriptionsList){
            if (place.xid == xid){
                place.description = description;
                updateText();
            }
        }
    }

    public void setPlaces(List<PlaceInRadius> placeInRadiusList){
        placeWithDescriptionsList.clear();
        weather.sky = "";
        for (PlaceInRadius placeInRadius : placeInRadiusList){
            PlaceWithDescriptionView newPlace = new PlaceWithDescriptionView();
            newPlace.name = placeInRadius.getName();
            newPlace.description = "";
            newPlace.xid = placeInRadius.getXid();
            newPlace.position = placeInRadius.getPosition();
            placeWithDescriptionsList.add(newPlace);
        }
        updateText();
    }

    public void updateComboBox(List<Place> placeList){
        comboBox.removeAllItems();
        for (Place place : placeList){
            comboBox.addItem(place.toString());
        }
    }

    class ComboBoxListener implements ActionListener {
        private void enterPressed() throws IOException {
            model.findPlaces((String)comboBox.getSelectedItem());
        }

        private void placeSelected() throws IOException {
            model.findPlacesInRadius(comboBox.getSelectedIndex());
        }

        @SneakyThrows
        public void actionPerformed(ActionEvent e) {
            if (0 == e.getModifiers() && "comboBoxEdited" == e.getActionCommand()) enterPressed();
            else if (16 == e.getModifiers() && "comboBoxChanged" == e.getActionCommand()) placeSelected();
        }
    }

    public View() throws IOException {
        frame = new JFrame("lab 3");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,800);
        frame.setResizable(false);

        model = new Model(this);

        content = new JPanel(new FlowLayout(FlowLayout.LEFT));

        comboBox = new JComboBox<String>();
        comboBox.setEditable(true);
        comboBox.setPreferredSize(new Dimension(570,30));
        comboBox.addActionListener(new ComboBoxListener());
        content.add(comboBox);

        text = new JTextArea(45,56);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setEditable(false);
        content.add(text);

        JScrollPane scroll = new JScrollPane(text);
        content.add(scroll);

        frame.setContentPane(content);
        frame.setVisible(true);
    }
}
