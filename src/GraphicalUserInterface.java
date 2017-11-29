import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.*;

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
    private ObservableList<ProcessControlBlock> procsObserver;


    private BorderPane pane;
    private VBox infoBox = new VBox();


    //------All this stuff is for main thread synchronization------
    public static final CountDownLatch latch = new CountDownLatch(1);
    public static GraphicalUserInterface gui = null;

    public static GraphicalUserInterface waitForGui(){
        try{
            latch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return gui;
    }

    public static void setGui(GraphicalUserInterface thisGui){
        gui = thisGui;
        latch.countDown();
    }

    public GraphicalUserInterface(){
        setGui(this);
    }
    //---------------------------------------------------------------



    private void init(Stage window, BorderPane pane){
        //line chart variables/setup
        this.pane = pane;

        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);
        NumberAxis yAxis = new NumberAxis();



        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
            }
        };

        lineChart.setAnimated(false);
        lineChart.setTitle("Resource Monitor");
        lineChart.setHorizontalGridLinesVisible(true);

        memSeries.setName("Memory Usage");

        lineChart.getData().add(memSeries);
        infoBox.getChildren().add(lineChart);

        cli = Main.cli;

        System.out.println("log: GUI window created successfully.");
    }


    @Override
    public void start(Stage stage) throws Exception {

        BorderPane mainPane = new BorderPane();
        window = stage;
        window.setTitle("DANK.os");

        //table setup
        procsObserver = FXCollections.observableArrayList();

        table.setEditable(true);

        procCol = new TableColumn("Process ID");
        procCol.setPrefWidth(100);
        procCol.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        totalCol = new TableColumn("Total Cycles");
        totalCol.setPrefWidth(100);
        totalCol.setCellValueFactory(
                new PropertyValueFactory<>("cyclesRequired")
        );

        remainingCol = new TableColumn("Remaining Cycles");
        remainingCol.setPrefWidth(150);
        remainingCol.setCellValueFactory(
                new PropertyValueFactory<>("cyclesRemaining")
        );

        table.setItems(procsObserver);
        table.getColumns().addAll(procCol, totalCol, remainingCol);

        infoBox.getChildren().add(table);
        mainPane.setRight(infoBox);

        //execute the close method on clicking the exit button
        window.setOnCloseRequest(e -> {
            e.consume();
            Main.shutDown();
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

                    String input = commandInput.getText();

                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            String output = cli.interpretInput(input);

                            //update console
                            display.appendText(output);

                        }
                    }).start();

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
        commandInput.requestFocus();

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
                updateObserver();
                memDataQueue.add(Math.random());
                Thread.sleep(200);
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

    public void updateObserver(){
        procsObserver.clear();
        /*for(int i = 0; i < CpuClock.procs.size(); i++){
            procsObserver.add((ProcessControlBlock)CpuClock.procs.get(i));
        }*/

        procsObserver.addAll(Main.clock.runningProcs);
        procsObserver.addAll(Main.queue);
        procsObserver.removeAll(Collections.singleton(null));
    }


    protected void closeProgram(){
        Platform.exit();
        System.out.println("log: GUI window closed successfully.");
        return;
    }

    public void test(){
        System.out.println("Referenced.");
    }
}


