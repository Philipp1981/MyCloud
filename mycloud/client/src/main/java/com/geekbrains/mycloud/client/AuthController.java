package com.geekbrains.mycloud.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class AuthController implements Initializable {

    @FXML
    VBox autorization;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    VBox registration;
    @FXML
    Label messageToUser;
    @FXML
    Label messageToUserRegistration;
    @FXML
    TextField loginFieldReg;
    @FXML
    PasswordField passwordField1;
    @FXML
    PasswordField passwordField2;
    @FXML
    Button button;
    @FXML
    HBox authHbox;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();

        Thread ServerListener = new Thread(() -> {
            for (; ; ) {
                Object messageFromServer = null;
                try {
                    messageFromServer = Network.readInObject();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (messageFromServer.toString().startsWith("User checked/")) {
                    String[] receive = messageFromServer.toString().split("/");
                    String login = receive[1];
                    UserLogin.setLogin(login);
                    SuccessfulEnter();

                } else if (messageFromServer.toString().startsWith("Enter correct password")) {

                    Platform.runLater(() -> messageToUser.setText("Enter correct password"));

                } else if (messageFromServer.toString().startsWith("Enter correct user")) {

                    Platform.runLater(() -> messageToUser.setText("User doesn't exist"));

                } else if (messageFromServer.toString().equals("User already registered")) {

                    Platform.runLater(() -> {
                        messageToUserRegistration.setText("User is already registered");
                        loginFieldReg.clear();
                        passwordField1.clear();
                        passwordField2.clear();
                    });

                } else if (messageFromServer.toString().equals("Correct registration")) {
                    Platform.runLater(() -> {
                        exitReg();
                        messageToUser.setText("Successful");
                    });
                }
            }
        });

        ServerListener.setDaemon(true);
        ServerListener.start();
    }

    public void showRegForm() {
        autorization.setVisible(false);
        authHbox.setVisible(false);
        registration.setVisible(true);
    }

    public void exitReg() {
        autorization.setVisible(true);
        authHbox.setVisible(true);
        registration.setVisible(false);
    }

    public void sendAuthMessage() {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            Network.sendAuthMes(loginField.getText(), passwordField.getText());
            loginField.clear();
            passwordField.clear();
        }
    }

    public void sendRegMessage() {
        if (!loginFieldReg.getText().isEmpty() && !passwordField1.getText().isEmpty() && !passwordField2.getText().isEmpty()) {
            if (passwordField1.getText().equals(passwordField2.getText())) {
                Network.sendRegMessage(loginFieldReg.getText(), passwordField1.getText());
            } else {
                messageToUserRegistration.setText("Enter correct password");
                passwordField1.clear();
                passwordField2.clear();
            }
        }
    }

    public void switchScene() throws IOException {
        Stage stage;
        stage = (Stage) button.getScene().getWindow();
        Parent root1 = null;
        try {
            root1 = FXMLLoader.load(getClass().getResource("/sample.fxml"));
            stage.setTitle("MyCloud");
            Scene scene = new Scene(root1, 1000, 700);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SuccessfulEnter() {
        Platform.runLater(() -> {
            try {
                switchScene();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

