package process.forms.editor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import process.context.ApplicationContext;
import process.services.BookService;
import utils.FileUtils;

@Component
public class ParseForm {
    @FXML
    private SplitPane parseSplitPane;

    @FXML
    private Button transformButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button deleteButton;

    @FXML
    private TextArea sourceField;
    @FXML
    private TextArea transformationField;
    @FXML
    private TextArea transformedField;
    @FXML
    private TextArea correctField;

    public Node createUI(ApplicationContext applicationContext) throws IOException {
        Parent root = FileUtils.loadFXML(this);

        return root;
    }
}
