package com.company;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class MainApp extends Application {
    @Override
    public void start(Stage st) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        st.setScene(new Scene(root));
        st.setTitle("Shop");
        st.show();
    }
    public static void main(String[] args) { launch(args); }
}

///./gradlew run
///./gradlew shadowJar