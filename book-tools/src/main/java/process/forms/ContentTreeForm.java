package process.forms;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import process.context.ApplicationContext;
import process.context.ApplicationEvents;
import process.entities.BookElementEntity;
import process.services.BookElementService;
import utils.FileUtils;

@Component
public class ContentTreeForm {
    private ApplicationContext applicationContext;

    @Autowired
    private BookElementService bookService;

    @FXML
    private TreeView<BookElementEntity> treeView;

    private BookElementEntity rootEntity;

    public ContentTreeForm() {
        rootEntity = new BookElementEntity();
        rootEntity.setTitle("Root");
    }

    public Node createUI(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;

        Parent root = FileUtils.loadFXML(this);

        treeView.setCellFactory(c -> new ContentTreeCell());
        treeView.getSelectionModel().selectedItemProperty().addListener(e -> treeViewSelectionChanged(e));
        treeView.setShowRoot(false);

        refreshTree();

        return root;
    }

    @FXML
    private void refreshTree() {
        List<BookElementEntity> entities = bookService.getRootElements();
        List<TreeItem<BookElementEntity>> items = processElementsRecursively(entities);

        TreeItem<BookElementEntity> rootItem = new TreeItem<>(rootEntity);
        rootItem.getChildren().addAll(items);

        treeView.setRoot(rootItem);
    }

    private void treeViewSelectionChanged(Observable e) {
        TreeItem<BookElementEntity> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            BookElementEntity selectedBookElement = selectedItem.getValue();
            applicationContext.fireEvent(ApplicationEvents.CurrentBookElementChanged, selectedBookElement);
        }
    }

    private List<TreeItem<BookElementEntity>> processElementsRecursively(List<BookElementEntity> entities) {
        List<TreeItem<BookElementEntity>> items = entities.parallelStream()
                .map(entity -> {
                    TreeItem<BookElementEntity> item = new TreeItem<>(entity);
                    List<TreeItem<BookElementEntity>> childItems = processElementsRecursively(entity.getChildren());
                    item.getChildren().addAll(childItems);
                    return item;
                }).collect(Collectors.toList());
        return items;
    }

    private class ContentTreeCell extends TreeCell<BookElementEntity> {
        @Override
        protected void updateItem(BookElementEntity item, boolean empty) {
            super.updateItem(item, empty);

            VBox node = null;
            if (!empty) {
                node = new VBox();

                // Book Element Title Label
                Label timeLabel = new Label(item.getTitle());
                timeLabel.setTextFill(Color.BLUE);
                node.getChildren().add(timeLabel);
            }
            setGraphic(node);
        }
    }
}
