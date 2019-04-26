package process.filelist;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import filters.FilenameFilterImages;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import process.ApplicationContext;
import process.ApplicationContext.Events;
import process.ApplicationContext.Parameters;
import process.dto.FileListEntry;
import storages.IconStorage;
import storages.IconStorage.Icons;
import utils.FileUtils;

public class FileListNode {
    private ApplicationContext applicationContext;

    @FXML
    private ListView<FileListEntry> filesListView;
    @FXML
    private TextField currentFolderTextField;

    private File currentFolder;
    private String currentFileName;

    public Node init(ApplicationContext applicationContext) throws IOException {
        this.applicationContext = applicationContext;

        Parent node = FileUtils.loadFXML(this);
        // TODO Rewrite it better way, maybe extract to nested or new file
        filesListView.setCellFactory(new Callback<ListView<FileListEntry>, ListCell<FileListEntry>>() {
            @Override
            public ListCell<FileListEntry> call(ListView<FileListEntry> param) {
                return new ListCell<FileListEntry>() {
                    @Override
                    protected void updateItem(FileListEntry item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            Icons icon = item != null && item.isSaved() ? Icons.CHECKED_16 : Icons.TRANSPARENT_16;
                            Image image = IconStorage.getIcon(icon);
                            setGraphic(new ImageView(image));
                            setText(item.getFilename());
                        }
                    }
                };
            }
        });

        filesListView.getSelectionModel().selectedItemProperty().addListener(event -> {
            applicationContext.fireEvent(Events.WorkFileSelected, filesListView.getSelectionModel().getSelectedItem());
        });

        applicationContext.addEventListener(Events.WorkFolderChanged, newFolder -> handleChangeWorkFolder(newFolder));
        applicationContext.addEventListener(Events.WorkFolderRefresh, e -> handleRefreshFileList());
        applicationContext.addEventListener(Events.WorkFileSelected, value -> handleSelectFile(value));
        applicationContext.addEventListener(Events.WorkFileSelectNext, e -> handleSelectNextFile());

        return node;
    }

    @FXML
    private void selectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Folder with Images");
        if (currentFolder != null) {
            directoryChooser.setInitialDirectory(currentFolder);
        }
        File newFolder = directoryChooser.showDialog(null);

        if (newFolder != null && newFolder.exists() && newFolder.isDirectory()) {
            applicationContext.fireEvent(Events.WorkFolderChanged, newFolder);
        }
    }

    private void handleChangeWorkFolder(Object value) {
        File newFolder = (File) value;
        currentFolder = newFolder;
        applicationContext.setParameterValue(Parameters.StartFolder, currentFolder.getAbsolutePath());
        handleRefreshFileList();
    }

    @FXML
    private void handleRefreshFileList() {
        if (currentFolder == null || !currentFolder.exists() || !currentFolder.isDirectory()) {
            return;
        }

        currentFolderTextField.setText(currentFolder.getAbsolutePath());

        // Forming new list of FileListEntry for filesListView
        String[] filenames = currentFolder.list(new FilenameFilterImages());
        List<FileListEntry> fileEntries = Arrays.stream(filenames)
                .map(filename -> {
                    boolean saved = applicationContext.getSelectionBoundaries(currentFolder, filename) != null;
                    FileListEntry fileListEntry = new FileListEntry(filename, saved);
                    return fileListEntry;
                }).collect(Collectors.toList());
        ObservableList<FileListEntry> items = FXCollections.observableArrayList(fileEntries);

        // Looking for old selected item in the new list
        FileListEntry newSelectedItem = items.stream()
                .filter(item -> item.getFilename().equals(currentFileName))
                .findFirst().orElse(null);

        // Restoring the selection of the old file in the new list
        filesListView.setItems(items);
        if (newSelectedItem != null) {
            filesListView.getSelectionModel().select(newSelectedItem);
        }
    }

    private void handleSelectFile(Object value) {
        if (value == null) {
            return;
        }

        FileListEntry fileListEntry = (FileListEntry) value;
        String newWorkFile = fileListEntry.getFilename();
        // TODO More checks that we get real image file, not a directory or so
        if (newWorkFile != null && !newWorkFile.isEmpty()) {
            currentFileName = newWorkFile;
        }
    }

    private void handleSelectNextFile() {
        filesListView.getSelectionModel().selectNext();

        int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
        int scrollTo = selectedIndex >= 10 ? selectedIndex - 10 : 0;
        filesListView.scrollTo(scrollTo);
    }
}
