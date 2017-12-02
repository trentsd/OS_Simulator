import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import java.util.ConcurrentModificationException;
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
    private StringProperty memString = new SimpleStringProperty();
    private StringProperty storString = new SimpleStringProperty();

    private Label memUsage, storageUsage;

    private TableView table = new TableView();
    private TableColumn procCol;
    private TableColumn totalCol;
    private TableColumn remainingCol;
    private TableColumn stateCol;
    private TableColumn nameCol;
    public ObservableList<ProcessControlBlock> procsObserver;


    private BorderPane pane;
    private VBox infoBox = new VBox();
    private TextArea display;


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


        //Setup for memory labels
        memUsage = new Label();
        memUsage.textProperty().bind(memString);
        memString.set("Memory used: " + 0 + "/4096 MB");
        storageUsage = new Label();
        storageUsage.textProperty().bind(storString);
        storString.set("Storage used: " + 0 + " MB");

        //add them to be rendered
        infoBox.getChildren().addAll(lineChart, memUsage, storageUsage);

        //instantiate the cli
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
                new PropertyValueFactory<>("pid")
        );

        nameCol = new TableColumn("Name");
        nameCol.setPrefWidth(100);
        nameCol.setCellValueFactory(
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

        stateCol = new TableColumn("Status");
        stateCol.setPrefWidth(80);
        stateCol.setCellValueFactory(
                new PropertyValueFactory<>("state")
        );

        table.setItems(procsObserver);
        table.getColumns().addAll(procCol, nameCol, totalCol, remainingCol, stateCol);

        infoBox.getChildren().add(table);

        Label stateKey = new Label("NEW = 0, READY = 1, RUN = 2, WAIT = 3, EXIT = 4");
        infoBox.getChildren().add(stateKey);

        mainPane.setRight(infoBox);

        //execute the close method on clicking the exit button
        window.setOnCloseRequest(e -> {
            e.consume();
            Main.shutDown();
        });

        //initalize graph
        init(stage, mainPane);

        //setup the console
        VBox vertBox = new VBox();

        display = new TextArea();
        display.setMaxWidth(400);
        display.setMinHeight(500);
        display.setWrapText(true);
        display.setEditable(false);


        TextField commandInput = new TextField();
        commandInput.setMaxWidth(400);

        //do this when user hits enter
        commandInput.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER) {

                String input = commandInput.getText();//get the text from the input field

                new Thread(new Runnable(){//run the parsing in a new thread
                    @Override
                    public void run() {
                        cli.interpretInput(input);//parse input

                    }
                }).start();

                commandInput.setText("");//reset input line
            }
        });



        //Scheduler buttons
        HBox buttons = new HBox();

        Button roundRobinButton = new Button("Round Robin");
        roundRobinButton.setOnAction(e -> {
            System.out.println("log: Round Robbin selected.");
            displayText("Round Robin selected");
            Main.selectScheduler(0);
        });

        Button firstInButton = new Button("First Come First Serve");
        firstInButton.setOnAction(e -> {
            System.out.println("log: FcFs selected.");
            displayText("First Come First Serve selected");
            Main.selectScheduler(1);
        });

        /*Button shortestButton = new Button("SRTF");
        shortestButton.setOnAction(e -> {
            System.out.println("log: SRTF selected.");
            Main.selectScheduler(2);
        });*/

        buttons.getChildren().addAll(roundRobinButton, firstInButton);


        vertBox.getChildren().addAll(display, commandInput, buttons);

        mainPane.setLeft(vertBox);

        Scene scene = new Scene(mainPane, 920, 600);
        window.setScene(scene);
        window.show();



        //set graph updating to its own thread
        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        });

        commandInput.requestFocus();

        //graph startup
        AddToQueue addToQueue = new AddToQueue();
        executor.execute(addToQueue);

        prepareTimeline();
    }


    //----Methods to handle updating input on graphs----

    //fetch data
    private class AddToQueue implements Runnable {
        public void run() {
            try {
                updateObserver();

                memDataQueue.add(Main.mmu.getMemFramesPercent());

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

        int kbUse = (Main.mmu.getMemFramesUsed()*4);
        int mgbUse = kbUse/1024;
        int storUse = (Main.mmu.getStorageFramesUsed()*4)/1024;
        memString.set("Memory used: " + mgbUse + "/4096 MB (" + kbUse + " kb)");
        storString.set("Storage used: " + storUse + " MB");
    }

    public void show(){
        launch();
    }

    public void updateObserver(){ //pulls info on processes from cpu
        procsObserver.clear();

        try{
            procsObserver.addAll(Main.clock.allProcs);
        }catch (Exception e){
            System.out.println(e.toString());
        }



    }


    protected void closeProgram(){
        Platform.exit();
        System.out.println("log: GUI window closed successfully.");
        return;
    }

    public void displayText(String output){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                display.appendText("\n" + output);
            }
        });

    } //prints text to console display
}


