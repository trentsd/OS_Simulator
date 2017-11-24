import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        String enteredCommand;
        window = stage;
        window.setTitle("TEST TITLE");


        HBox topMenu = new HBox();
        Button buttonA = new Button("File");
        Button buttonB = new Button("Edit");
        Button buttonC = new Button("View");
        topMenu.getChildren().addAll(buttonA, buttonB, buttonC);

        VBox vertBox = new VBox();

        TextArea display = new TextArea();
        display.setMaxWidth(400);
        display.setWrapText(true);
        display.setEditable(false);


        TextField commandInput = new TextField();
        commandInput.setMaxWidth(400);
        commandInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.ENTER) {
                    display.appendText(commandInput.getText() + "\n");
                    commandInput.setText("");
                }
            }
        });


        vertBox.getChildren().addAll(display, commandInput);

        BorderPane mainPane = new BorderPane();
        mainPane.setTop(topMenu);
        mainPane.setLeft(vertBox);


        Scene scene = new Scene(mainPane, 400, 250);
        window.setScene(scene);
        window.show();
    }
}
