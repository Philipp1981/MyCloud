package com.geekbrains.mycloud.client;


import com.geekbrains.mycloud.common.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.*;

public class Controller implements Initializable {


    public static TreeMap<String, Long> map;
    @FXML
    TextField textField;
    @FXML
    VBox clientPanel;
    @FXML
    VBox serverPanel;
    @FXML
    HBox mainHBox;
    @FXML
    StackPane mainStackPane;
    @FXML
    ListView<String> myListView;
    @FXML
    ListView<String> serverListView;
    @FXML
    ListView<String> serverSizeListView;
    @FXML
    Label serverStorage;
    @FXML
    Label clientStorage;
    @FXML
    Button btn;
    Desktop desktop = Desktop.getDesktop();
    FileChooser fileChooser = new FileChooser();
    List<String> sl = new ArrayList<>();
    String userLog = UserLogin.getLogin();
    String clientPath = "client_storage/" + userLog + "/";
    double dragDeltaX, dragDeltaY;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Files.createDirectories(Paths.get(clientPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObservableList<String> list = FXCollections.observableArrayList();
        initializeWindowDragAndDropLabel();
        initializeDragAndDropLabel();
        Network.start();   //запускаем соединение с сервером   + usLogin+"/"
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();  //прослушиваем сетевое соединение на предмет получения сообщений типа AbstractMessage
                    if (am instanceof FileMessage) {       //если получаем сообщение типа FileMessage
                        FileMessage fm = (FileMessage) am;  // кастуем его к типу FileMessage (наследник AbstractMessage )
                        // в хранилище клиента создаем фвйл-копию полученного при получении FileMessage файла

                        Files.write(Paths.get(clientPath + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList(); //обновляем файллист хранилища клиента
                    }

                    if (am instanceof RefreshSrvFileListMessage) {
                        RefreshSrvFileListMessage refreshSrvFileListMessage = (RefreshSrvFileListMessage) am;
                        map = refreshSrvFileListMessage.getFindFiles();
                        for (Map.Entry<String, Long> entry : map.entrySet()) {
                            serverListView.getItems().add(entry.getKey());
                            serverSizeListView.getItems().add(entry.getValue().toString() + " byte");
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } finally {
                Network.stop(); //в любом случае закрываем соединение при закрытии
            }
        });
        t.setDaemon(true);
        t.start();
        clientStorage.setText("CLIENT STORAGE: " + userLog);
        refreshLocalFilesList();
        try {
            refreshServerStorage();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    // при нажатии кнопки Load File, если textField содержит текст, посылаем сообщение типа FileRequest серверу
    //с запросом на посылку с сервера файла, находящегося в хранилище сервера
    public void pressOnDownloadBtn(ActionEvent actionEvent) throws IOException {
        myListView.getItems().clear();
        if (textField.getLength() > 0) {
            Network.sendMsg(new FileRequest(textField.getText()));

            if (!Files.exists(Paths.get(clientPath + textField.getText()))) {
                System.out.println("File:  " + textField.getText() + "  - add to client storage");
            } else
                System.out.println("File:  " + textField.getText() + "  - already exist on client storage");
            textField.clear();
        }
    }

    public void refreshLocalFilesList() {   //обновление списка файлов в хранилище клиента
        if (Platform.isFxApplicationThread()) {
            try {
                Files.list(Paths.get(clientPath)).map(p -> p.getFileName().toString()).forEach(o -> myListView.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    Files.list(Paths.get(clientPath)).map(p -> p.getFileName().toString()).forEach(o -> myListView.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

//    public void refreshServerFilesList() {   //обновление списка файлов в хранилище клиента
//
//        RefreshMessage refreshMessage = new RefreshMessage("refresh");
//        Network.sendRefreshMessage(refreshMessage);
//
//        Thread serverFilesListener = new Thread(() -> {
//
//                    for (Map.Entry<String, Long> entry : map.entrySet()) {
//                    serverListView.getItems().clear();
//                    serverListView.getItems().add(entry.getKey());
//                    serverSizeListView.getItems().clear();
//                    serverSizeListView.getItems().add(entry.getValue().toString());}
//
//        });
//        serverFilesListener.setDaemon(true);
//        serverFilesListener.start();
//
//    }
//
//    public void refreshServerSizeFilesList() {   //обновление списка файлов в хранилище клиента
//
//
////        if (Platform.isFxApplicationThread()) {
////            try {
////                serverSizeListView.getItems().clear();
////                Files.list(Paths.get("server_storage/")).map(p -> p.getFileName().toString()).forEach(o -> serverSizeListView.getItems().add(o.length()+" bytes"));
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        } else {
////            Platform.runLater(() -> {
////                try {
////                    Files.list(Paths.get("server_storage/")).map(p -> p.getFileName().toString()).forEach(o ->serverSizeListView.getItems().add(o.length()+" bytes"));
////                    serverSizeListView.getItems().clear();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            });
////        }
//    }

    public void refreshServerStorage() {
        try {
            serverListView.getItems().clear();
            serverSizeListView.getItems().clear();
            RefreshMessage refreshMessage = new RefreshMessage("refresh");
            Network.sendRefreshMessage(refreshMessage);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    // добавление файла с машины клиента в хранилище сервера при нажатии кнопки add Files
    public void addFiles(ActionEvent event) throws Exception {

        Stage stage = new Stage();
        File file = fileChooser.showOpenDialog(stage);

        FileMessage fmsg = new FileMessage(Paths.get(file.getAbsolutePath()));
        Network.sendMsg(fmsg);
        try {
            refreshServerStorage();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    // добавление файла с машины клиента в хранилище сервера (посредством перетаскивания файла в окно DragAndDrop)
    public void initializeDragAndDropLabel() {
        serverListView.getItems().clear();
        serverPanel.setOnDragOver(event -> {
            if (event.getGestureSource() != serverListView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        serverPanel.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File o : db.getFiles()) {
                    sl.add(o.getName());
//                    for (String a : sl) {
//                        long count = o.length();
//                        serverListView.getItems().add(a);
//                        refreshServerStorage();
//                        serverSizeListView.getItems().add(count + "  bytes");
//                        refreshServerSizeFilesList();
////                        if (!Files.exists(Paths.get("server_storage/" + o.getName()))) {
////                            System.out.println(o.getPath() + "  : " + count + "  bytes");
////                        }else System.out.println("File:  "+o.getName() + "  - already exist on server storage");
//                    }
                    FileMessage fmsg1 = null;
                    try {
                        fmsg1 = new FileMessage(Paths.get(o.getAbsolutePath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Network.sendMsg(fmsg1);
                    try {
                        refreshServerStorage();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                    serverListView.setOnMousePressed(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            if (mouseEvent.getClickCount() == 2) {
                                openFile(o);
                            }
                        }
                    });
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

    }

    public void initializeWindowDragAndDropLabel() {
        Platform.runLater(() -> {
            Stage stage = (Stage) serverListView.getScene().getWindow();

            serverPanel.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {

                    dragDeltaX = stage.getX() - mouseEvent.getScreenX();
                    dragDeltaY = stage.getY() - mouseEvent.getScreenY();
                }
            });
            serverListView.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    stage.setX(mouseEvent.getScreenX() + dragDeltaX);
                    stage.setY(mouseEvent.getScreenY() + dragDeltaY);
                }
            });
        });
    }

    private void openFile(File file) {
        try {
            this.desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFileFromServ(ActionEvent actionEvent) throws IOException {
        Alert alert;
        if (serverListView.getItems().isEmpty()) {
            alert = new Alert(Alert.AlertType.CONFIRMATION, "Nothing to copy", ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
        } else {
            String addFiles = serverListView.getSelectionModel().getSelectedItem();
            textField.setText(addFiles);
        }
    }

    public void deleteFile(ActionEvent actionEvent) throws IOException {
        Alert alert;
        if (myListView.getItems().isEmpty()) {
            alert = new Alert(Alert.AlertType.CONFIRMATION, "Nothing to delete", ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
        } else {
            alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete files?", ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get().getText().equals("OK")) {
                String fileName = clientPath + myListView.getSelectionModel().getSelectedItem();
                System.out.println("File: " + myListView.getSelectionModel().getSelectedItem() +
                        " - was deleted from client storage");
                Files.delete(Paths.get(fileName));
                myListView.getItems().clear();
            }
        }
    }

    public void deleteServerFile(ActionEvent actionEvent) throws IOException, InterruptedException {
        Alert alert;
        if (serverListView.getItems().isEmpty()) {
            alert = new Alert(Alert.AlertType.CONFIRMATION, "Nothing to delete", ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
        } else {
            alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete files?", ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get().getText().equals("OK")) {
                String fileDelName = serverListView.getSelectionModel().getSelectedItem();
                System.out.println("File: " + serverListView.getSelectionModel().getSelectedItem() +
                        " - was deleted from server storage");
                Network.sendFdm(fileDelName);
                try {
                    refreshServerStorage();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
