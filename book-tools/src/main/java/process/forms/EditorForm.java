package process.forms;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import process.context.ApplicationContext;
import utils.FileUtils;

public class EditorForm {
    private ApplicationContext applicationContext;

    @FXML
    private TextArea textArea;

    public Node createUI(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;

        Parent root = FileUtils.loadFXML(this);

        return root;
    }
}
