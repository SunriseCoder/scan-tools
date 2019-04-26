package process.reordering;

import java.io.File;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.util.Callback;
import process.ApplicationContext;
import process.reordering.methods.AbstractReorderingTask;
import process.reordering.methods.Reordering4PagesOn1SheetFromMiddle;
import utils.FileUtils;

public class PageReorderingNode {
    private ApplicationContext applicationContext;

    @FXML
    private ComboBox<ReorderingMethods> methodComboBox;
    @FXML
    private ProgressBar progressBar;

    public Node init(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;
        Parent rootNode = FileUtils.loadFXML(this);
        return rootNode;
    }

    public void initialize() {
        methodComboBox.setCellFactory(new ReorderingMethodCellFactory());
        methodComboBox.setButtonCell(new ReorderingMethodsListCell());

        ReorderingMethods[] values = ReorderingMethods.values();
        ObservableList<ReorderingMethods> items = FXCollections.observableArrayList(values);
        methodComboBox.setItems(items);
        methodComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void startProcessing() throws Exception {
        ReorderingMethods method = methodComboBox.getSelectionModel().getSelectedItem();
        if (method == null) {
            return;
        }

        File inputFolder = applicationContext.getWorkFolder();
        File outputFolder = new File(inputFolder, "reordered");
        outputFolder.mkdir();

        AbstractReorderingTask task = method.cl.newInstance();
        task.setApplicationContext(applicationContext);
        task.setInputFolder(inputFolder);
        task.setOutputFolder(outputFolder);
        task.setProgressBar(progressBar);
        Thread thread = new Thread(task);
        thread.start();
    }

    public static enum ReorderingMethods {
        Method4PagesOn1SheetFromMiddle("4 pages, from middle", Reordering4PagesOn1SheetFromMiddle.class);

        private String text;
        private Class<? extends AbstractReorderingTask> cl;

        private ReorderingMethods(String text, Class<? extends AbstractReorderingTask> cl) {
            this.text = text;
            this.cl = cl;
        }

        public String getText() {
            return text;
        }

        public Class<? extends AbstractReorderingTask> getCl() {
            return cl;
        }
    }

    private static class ReorderingMethodCellFactory
            implements Callback<ListView<ReorderingMethods>, ListCell<ReorderingMethods>> {
        @Override
        public ListCell<ReorderingMethods> call(ListView<ReorderingMethods> param) {
            return new ReorderingMethodsListCell();
        }
    }

    private static class ReorderingMethodsListCell extends ListCell<ReorderingMethods> {
        @Override
        protected void updateItem(ReorderingMethods item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? null : item.getText());
        }
    }
}
