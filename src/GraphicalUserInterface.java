import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

    public Stage window;

    //crap for graphs
    private static  final int MAX_DATA_POINTS = 50;
    private int xSeriesData = 0;
    private XYChart.Series<Number, Number> cpuSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> memSeries = new XYChart.Series<>();
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Number> cpuDataQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Number> memDataQueue = new ConcurrentLinkedQueue<>();
    private NumberAxis xAxis;

    private void init(Stage window, BorderPane pane){
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
        lineChart.setTitle("Resourcce Monitor");
        lineChart.setHorizontalGridLinesVisible(true);

        cpuSeries.setName("CPU Usage");
        memSeries.setName("Memory Usage");

        lineChart.getData().addAll(cpuSeries, memSeries);

        pane.setRight(lineChart);
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
                    display.appendText(commandInput.getText() + "\n");
                    commandInput.setText("");
                }
            }
        });


        vertBox.getChildren().addAll(display, commandInput);

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
                cpuDataQueue.add(Math.random());
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
            if (cpuDataQueue.isEmpty()) break;
            cpuSeries.getData().add(new XYChart.Data<>(xSeriesData++, cpuDataQueue.remove()));
            memSeries.getData().add(new XYChart.Data<>(xSeriesData++, memDataQueue.remove()));

        }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (cpuSeries.getData().size() > MAX_DATA_POINTS) {
            cpuSeries.getData().remove(0, cpuSeries.getData().size() - MAX_DATA_POINTS);
        }
        if (memSeries.getData().size() > MAX_DATA_POINTS) {
            memSeries.getData().remove(0, memSeries.getData().size() - MAX_DATA_POINTS);
        }

        // update
        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
    }


    private void closeProgram(){
        window.close();
    }
}
