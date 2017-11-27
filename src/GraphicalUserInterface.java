import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class GraphicalUserInterface extends Application{

    public static Stage window;
    public CommandLine cli;

    //crap for graphs
    private static  final int MAX_DATA_POINTS = 50;
    private int xSeriesData = 0;
    private XYChart.Series<Number, Number> memSeries = new XYChart.Series<>();
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Number> memDataQueue = new ConcurrentLinkedQueue<>();
    private NumberAxis xAxis;

    private TableView table = new TableView();
    private TableColumn procCol;
    private TableColumn totalCol;
    private TableColumn remainingCol;

    private VBox infoBox = new VBox();

    private void init(Stage window, BorderPane pane){
        //line chart variables/setup
        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);
        NumberAxis yAxis = new NumberAxis();

        //table variables
        table.setEditable(true);
        procCol = new TableColumn("Process ID");
        procCol.setPrefWidth(100);
        totalCol = new TableColumn("Total Cycles");
        totalCol.setPrefWidth(100);
        remainingCol = new TableColumn("Remaining Cycles");
        remainingCol.setPrefWidth(150);
        table.getColumns().addAll(procCol, totalCol, remainingCol);

        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
            }
        };

        lineChart.setAnimated(false);
        lineChart.setTitle("Resourcce Monitor");
        lineChart.setHorizontalGridLinesVisible(true);

        memSeries.setName("Memory Usage");

        lineChart.getData().add(memSeries);


        infoBox.getChildren().addAll(lineChart, table);
        pane.setRight(infoBox);

        cli = Main.cli;

        System.out.println("log: GUI window created successfully.");
    }


    @Override
    public void start(Stage stage) throws Exception {

        BorderPane mainPane = new BorderPane();
        window = stage;
        window.setTitle("DANK.os");


        //execute the close method on clicking the exit button
        window.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });

        //initalize graph
        init(stage, mainPane);

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
                    //display.appendText(commandInput.getText() + "\n");
                    String input = commandInput.getText();
                    String output = cli.interpretInput(input);
                    display.appendText(output);
                    //display.appendText(cli.interpretInput(commandInput.getText()));
                    commandInput.setText("");
                }
            }
        });


        //Scheduler buttons
        HBox buttons = new HBox();

        Button roundRobinButton = new Button("RR");
        roundRobinButton.setOnAction(e -> {
            System.out.println("log: Round Robbin selected.");
            Main.selectScheduler(0);
        });

        Button firstInButton = new Button("FiFs");
        firstInButton.setOnAction(e -> {
            System.out.println("log: FiFs selected.");
            Main.selectScheduler(1);
        });

        Button shortestButton = new Button("SRTF");
        shortestButton.setOnAction(e -> {
            System.out.println("log: SRTF selected.");
            Main.selectScheduler(2);
        });

        buttons.getChildren().addAll(roundRobinButton, firstInButton, shortestButton);


        vertBox.getChildren().addAll(display, commandInput, buttons);

        mainPane.setLeft(vertBox);

        Scene scene = new Scene(mainPane, 800, 250);
        window.setScene(scene);
        window.show();


        //---graph stuff---
        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        });

        AddToQueue addToQueue = new AddToQueue();
        executor.execute(addToQueue);

        prepareTimeline();
    }


    //----Methods to handle updating input on graphs----

    //fetch data
    private class AddToQueue implements Runnable {
        public void run() {
            try {
                // add a item of random data to queues todo: make this actually pull data

                memDataQueue.add(Math.random());

                Thread.sleep(500);
                executor.execute(this);

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    //compile updated chart from dataset
    private void prepareTimeline() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    //take data from queue and put it in graph data
    private void addDataToSeries() {
        for (int i = 0; i < 20; i++) { //add 20 numbers to the plot
            if (memDataQueue.isEmpty()) break;
            memSeries.getData().add(new XYChart.Data<>(xSeriesData++, memDataQueue.remove()));

        }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (memSeries.getData().size() > MAX_DATA_POINTS) {
            memSeries.getData().remove(0, memSeries.getData().size() - MAX_DATA_POINTS);
        }

        // update
        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
    }

    public void show(){
        launch();
    }

    public void addProcInfo(String name, int totalCycles, int remainingCycles){
        /*
        procCol.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().get(name))
        );*/
    }


    private void closeProgram(){
        window.close();
        System.out.println("log: GUI window closed successfully.");
        return;
    }

}
