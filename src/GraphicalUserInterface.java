import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GraphicalUserInterface extends Application{

    public Stage window;

    /*public GraphicalUserInterface(String args[]){
        launch(args);
    }*/


    @Override
    public void start(Stage stage) throws Exception {
        window = stage;
        window.setTitle("TEST TITLE");


        HBox topMenu = new HBox();
        Button buttonA = new Button("File");
        Button buttonB = new Button("Edit");
        Button buttonC = new Button("View");
        topMenu.getChildren().addAll(buttonA, buttonB, buttonC);

        VBox leftMenu = new VBox();
        Button buttonD = new Button("D");
        Button buttonE = new Button("E");
        Button buttonF = new Button("F");
        leftMenu.getChildren().addAll(buttonD, buttonE, buttonF);

        BorderPane mainPane = new BorderPane();
        mainPane.setTop(topMenu);
        mainPane.setLeft(leftMenu);


        Scene scene = new Scene(mainPane, 300, 250);
        window.setScene(scene);
        window.show();
    }
}
