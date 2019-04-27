package process.processing;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import process.ApplicationContext;
import process.processing.actions.ActionsNode;
import process.processing.preprocessing.PreprocessingNode;
import utils.FileUtils;

public class ProcessingNode {
    @FXML
    private GridPane processingTabGridPane;

    public Node init(ApplicationContext applicationContext) throws IOException {
        Parent node = FileUtils.loadFXML(this);

        ActionsNode actions = new ActionsNode();
        Node actionsNode = actions.init(applicationContext);
        processingTabGridPane.getChildren().add(actionsNode);

        PreprocessingNode reordering = new PreprocessingNode();
        Node reorderingNode = reordering.init(applicationContext);
        GridPane.setRowIndex(reorderingNode, 1);
        processingTabGridPane.getChildren().add(reorderingNode);

        return node;
    }
}
