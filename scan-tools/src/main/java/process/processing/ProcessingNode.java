package process.processing;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import process.context.ApplicationContext;
import process.context.ApplicationEvents;
import process.processing.actions.ActionsNode;
import process.processing.prepare.PrepareNode;
import process.processing.render.RenderNode;
import utils.FileUtils;
import utils.ThreadUtils;

public class ProcessingNode {
    private ApplicationContext applicationContext;

    @FXML
    private GridPane processingTabGridPane;

    @FXML
    private Button saveButton;

    public Node init(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;

        Parent node = FileUtils.loadFXML(this);

        applicationContext.addEventListener(ApplicationEvents.SensorControl, value -> saveButton.setVisible((boolean) value));

        ActionsNode actions = new ActionsNode();
        Node actionsNode = actions.init(applicationContext);
        processingTabGridPane.getChildren().add(actionsNode);

        PrepareNode reorder = new PrepareNode();
        Node reorderNode = reorder.init(applicationContext);
        GridPane.setRowIndex(reorderNode, 1);
        processingTabGridPane.getChildren().add(reorderNode);

        RenderNode render = new RenderNode();
        Node renderNode = render.init(applicationContext);
        GridPane.setRowIndex(renderNode, 2);
        processingTabGridPane.getChildren().add(renderNode);

        return node;
    }

    @FXML
    private void handleSave() {
        saveButton.setDisable(true);
        applicationContext.fireEvent(ApplicationEvents.SaveImage, null);
        ThreadUtils.runLaterAfterSleep(3000, () -> saveButton.setDisable(false));
    }
}
