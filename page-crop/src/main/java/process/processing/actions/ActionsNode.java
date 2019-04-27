package process.processing.actions;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import process.ApplicationContext;
import process.ApplicationContext.Events;
import utils.FileUtils;

public class ActionsNode {
    private ApplicationContext applicationContext;

    @FXML
    private ToggleButton sensorToggleButton;

    public Node init(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        Parent rootNode = FileUtils.loadFXML(this);
        return rootNode;
    }

    @FXML
    private void handleCenterImage() {
        applicationContext.fireEvent(Events.CenterImage, null);
    }

    @FXML
    private void handleSaveImage() {
        applicationContext.fireEvent(Events.SaveImage, null);
    }

    @FXML
    private void handleToggleSensor() {
        boolean selected = sensorToggleButton.isSelected();
        applicationContext.fireEvent(Events.SensorControl, selected);
    }
}
