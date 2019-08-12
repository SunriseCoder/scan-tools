package process.forms;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import process.context.ApplicationContext;
import utils.FileUtils;

public class ContentTreeForm {
    private ApplicationContext applicationContext;

    @FXML
    private TreeView<String> treeView;

    public Node createUI(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;

        Parent root = FileUtils.loadFXML(this);

        TreeItem<String> rootItem = new TreeItem<>("Root");
        treeView.setRoot(rootItem);

        return root;
    }
}
