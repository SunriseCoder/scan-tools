package process.processing;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import process.ApplicationContext;
import process.reordering.PageReorderingNode;
import utils.FileUtils;

public class ProcessingNode {
    @FXML
    private GridPane processingTabGridPane;

    public Node init(ApplicationContext applicationContext) throws IOException {
        Parent node = FileUtils.loadFXML(this);

        PageReorderingNode reordering = new PageReorderingNode();
        Node reorderingNode = reordering.init(applicationContext);
        processingTabGridPane.getChildren().add(reorderingNode);

        return node;
    }
}
