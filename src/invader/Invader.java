package invader;

import javafx.application.Application;
import javafx.stage.Stage;

public class Invader extends Application {

    private InvaderData data = new InvaderData(1, 0, 0, 1, 0);

    @Override
    public void start(Stage primaryStage) {
        InvaderGUI invaderGUI = new InvaderGUI(data);
        invaderGUI.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}


