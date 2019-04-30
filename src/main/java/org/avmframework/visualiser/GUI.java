package org.avmframework.visualiser;

//imports
import org.avmframework.Monitor;

import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.scene.control.Slider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.util.Duration;
import com.sun.javafx.charts.Legend;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.RoundingMode;
import java.util.ArrayList;

// enums to support graph panning mechanic
enum XDirection {
    NONE,
    LEFT,
    RIGHT
}

enum YDirection{
    NONE,
    UP,
    DOWN
}


public class GUI extends Application {

    // to keep a record of what the original upper and lower bounds were calculated as using JavaFx bulit-in autoranging.
    private double originalXAxisLowerBound, originalXAxisUpperBound;
    private double originalYAxisLowerBound, originalYAxisUpperBound;

    // to store values involved in calculating logarithmic axes.
    private double xALog, xBLog, yALog, yBLog;

    // to keep record of clicked points on graph axes.
    private Double clickPointX,clickPointY = 0.0;

    final double PAN_SENSITIVITY = 50; // higher = less speed



    // used to keep track of what variable is having its series set up.
    private int currentVariable = 1;
    // used to keep track of whether the current variable has finished being set up. Used in logic to not record the wrap round at end of AVMf run.
    private boolean currentVarFinished = false;
    // variable to keeping track of which vector variable is currently being animated
    private int currentAnimatedVariable = 0; // note: first landscape cuepoint has label 0 NOT 1
    // Keeps track of the number of variables in the vector
    private int noOfVariables;

    private int currentln = 0;


    // the avmfAnimationSequence
    private SequentialTransition avmfAnimationSequence = new SequentialTransition();

    // label initialisations
    final private Label currentOptVarValueLbl = new Label(); // initialised empty
    final private Label animationRateValueLbl = new Label(String.valueOf(avmfAnimationSequence.getCurrentRate()));
    final private Label animationStateValueLbl = new Label(String.valueOf(avmfAnimationSequence.getStatus()));
    final private Label pairVariableValueLbL = new Label("");
    final private Label pairVariableObjValLbl = new Label("");

    @Override public void start(final Stage stage) {

        // initialise filename from monitor
        String fileName = Monitor.getFileName();



        // if the launcher configuration hasn't got a file loaded, load one here thorough a JavaFX file chooser GUI
        if (!Launcher.fileLoaded){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog(stage);
            String filePath = String.valueOf(file.toPath());
            fileName = file.getName();// set file name
            Launcher.setJsonFileName(filePath);
            try{
                Launcher.loadRunLog(filePath);
                System.out.println("Loaded file " + fileName);
                System.out.println("At :" + filePath);
            }
            catch(FileNotFoundException e){
                System.out.println("Error loading JSON AMVf runlog file");
            }
        }

        // make linechart from data
        final LineChart<Number,Number> lineChart = makeLineChart(Launcher.getDataPairs());

        // record number of variables in vector
        noOfVariables = Launcher.getDataPairs().get(0).getVector().size();
        // set title of stage to file name
        stage.setTitle(fileName);

    // setting event handler for animation sequence finish.
    avmfAnimationSequence.setOnFinished(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            //update labels
            animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus()));
            currentOptVarValueLbl.setText("END");

            int dataLength = Launcher.getDataPairs().size();
            lineChart.setTitle("Current Vector: " + String.valueOf(Launcher.getDataPairs().get(dataLength-1).getVector()));
        }
    });



        // setup xAxis zoom slider
        final Slider xZoomSlider = new Slider(1, 100, originalXAxisUpperBound);
        xZoomSlider.setCursor(Cursor.HAND);
        xZoomSlider.setOrientation(Orientation.VERTICAL);
        xZoomSlider.setShowTickLabels(true);
        // Adding Listener to value property.
        xZoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

//                System.out.println("Slider X has slidden!!!"); // Debugging

                final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis(); // get xAxis
                xAxis.setAutoRanging(false); // otherwise zoom overridden by auto ranging.

                double currentXLowerBound = xAxis.getLowerBound();
                double currentXUpperBound = xAxis.getUpperBound();



                // converts slider value into logarithmic value.
                double logNewValue = xCalcLogValue((double) newValue);
                // find new centre of x zoom when graph panned
                double pannedMidpoint = (currentXUpperBound + currentXLowerBound)/2;
                // calculating new bounds
                double newLowerBound = - (logNewValue - pannedMidpoint);
                double newUpperBound = logNewValue + pannedMidpoint;

                // do the xAxis zoom
                xZoom(lineChart, newLowerBound, newUpperBound);
                // change the cursor to open hand to tell user they can now pan around zoomed graph
                lineChart.setCursor(Cursor.OPEN_HAND);
            }
        });

        // setup xAxis zoom slider
        final Slider yZoomSlider = new Slider(1, 100, originalYAxisUpperBound);
        yZoomSlider.setCursor(Cursor.HAND);
        yZoomSlider.setOrientation(Orientation.VERTICAL);
        yZoomSlider.setShowTickLabels(true);
        // Adding Listener to value property.
        yZoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

//                System.out.println("Slider Y has slidden!!!"); // Debugging

                final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis(); // get yAxis
                yAxis.setAutoRanging(false); // otherwise zoom overridden by auto ranging.

                double currentYLowerBound = yAxis.getLowerBound();
                double currentYUpperBound = yAxis.getUpperBound();

                // converts slider value into logarithmic value.
                double logNewValue = yCalcLogValue((double) newValue);
                // find new centre of y zoom when graph panned
                double pannedMidpoint = (currentYUpperBound + currentYLowerBound)/2;
                // calculating new bounds
                double newLowerBound = - (logNewValue - pannedMidpoint);
                double newUpperBound = logNewValue + pannedMidpoint;


                // constrictions on bounds for y axis zooming
                if (newLowerBound < originalYAxisLowerBound && newUpperBound > originalYAxisUpperBound){
                    yZoom(lineChart, originalYAxisLowerBound, originalYAxisUpperBound);
                }
                else if (newLowerBound < originalYAxisLowerBound){
                    yZoom(lineChart, originalYAxisLowerBound, newUpperBound);
                }
                else if (newUpperBound > originalYAxisUpperBound){
                    yZoom(lineChart, newLowerBound, originalYAxisUpperBound);
                }
                else {
                    yZoom(lineChart, newLowerBound, newUpperBound);
                }
                lineChart.setCursor(Cursor.OPEN_HAND);




            }
        });

        // button for resetting the graph to minimum zoom.
        Button resetZoomButton = new Button("Reset Zoom");
        resetZoomButton.setCursor(Cursor.HAND);
        resetZoomButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
                final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
                // resetting graph axes
                xAxis.setLowerBound(originalXAxisLowerBound);
                xAxis.setUpperBound(originalXAxisUpperBound);
                yAxis.setLowerBound(originalYAxisLowerBound);
                yAxis.setUpperBound(originalYAxisUpperBound);
                // updating sliders to match
                xZoomSlider.setValue(originalXAxisUpperBound);
                yZoomSlider.setValue(originalYAxisUpperBound);
                lineChart.setCursor(Cursor.DEFAULT);
            }
        });


        // -------- Animation control buttons --------------- //

        // button to start/restart animation
        final Button restartAnimationButton  = new Button("Start");
        restartAnimationButton.setCursor(Cursor.HAND);
        restartAnimationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                restartAnimationButton.setText("Restart");
                avmfAnimationSequence.playFromStart();
                animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus()));
                // reset title to initial conditions
                lineChart.setTitle("Current Vector: " + String.valueOf(Launcher.getDataPairs().get(0).getVector()));


                // set tool tips on all nodes.
                ObservableList<XYChart.Series<Number,Number>> chartData = lineChart.getData();
                // get series from chart
                for (XYChart.Series series : chartData){
                    ObservableList<XYChart.Data> theData = series.getData();

                    // remove tooltips for all datapoints of series
                    for (XYChart.Data dataPoint : theData){
                        Node dataPointNode = dataPoint.getNode();
                        final Tooltip tooltip = new Tooltip(String.valueOf("Value: " + dataPoint.getXValue()) + " : Fitness: " + dataPoint.getYValue());
                        hackTooltipStartTiming(tooltip);
                        Tooltip.uninstall(dataPointNode,tooltip);

                        // set cursor on datapoints back to default
                        dataPointNode.setCursor(Cursor.DEFAULT);

                    }
                }


            }
        });

        // button to play animation
        Button playAnimationButton = new Button("Play");
        playAnimationButton.setCursor(Cursor.HAND);
        playAnimationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if (avmfAnimationSequence.getStatus() != Animation.Status.STOPPED){
                    avmfAnimationSequence.play();
                    System.out.println(avmfAnimationSequence.getCuePoints());
                    animationRateValueLbl.setText(String.valueOf(avmfAnimationSequence.getCurrentRate()));
                    animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus()));
                }

            }
        });

        // button to pause animation
        Button pauseAnimationButton  = new Button("Pause");
        pauseAnimationButton.setCursor(Cursor.HAND);
        pauseAnimationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                avmfAnimationSequence.pause();
                animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus()));


            }
        });

        // button to decrease animation rate to minimum
        Button decreaseRateButton  = new Button("Decrease Rate");
        decreaseRateButton.setCursor(Cursor.HAND);
        decreaseRateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                double currentRate = avmfAnimationSequence.getCurrentRate();
                // shouldn't update while paused as creates bug
                if (currentRate > 1 && avmfAnimationSequence.getStatus() != Animation.Status.PAUSED) {
                    avmfAnimationSequence.setRate(currentRate - 1);
                    // updating reporting of rate
                    animationRateValueLbl.setText(String.valueOf(avmfAnimationSequence.getCurrentRate()));
                }
            }
        });

        // button to increase animation rate to maximum
        Button increaseRateButton  = new Button("Increase Rate");
        increaseRateButton.setCursor(Cursor.HAND);
        increaseRateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                double currentRate = avmfAnimationSequence.getCurrentRate();
                // shouldn't update while paused as creates bug
                if (currentRate <= 9 && avmfAnimationSequence.getStatus() != Animation.Status.PAUSED) {
                    avmfAnimationSequence.setRate(currentRate + 1);
                    // updating reporting of rate
                    animationRateValueLbl.setText(String.valueOf(avmfAnimationSequence.getCurrentRate()));
                }
            }
        });

        // button ro jump to end of animation and finish.
        Button jumpToEndButton  = new Button("Jump To End");
        jumpToEndButton.setCursor(Cursor.HAND);
        jumpToEndButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                // for rounding to 4dp
                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.HALF_EVEN);

                if (avmfAnimationSequence.getStatus() != Animation.Status.STOPPED){
                    // jump to end and update labels
                    avmfAnimationSequence.play();
                    avmfAnimationSequence.jumpTo("end");
                    animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus()));
                    currentAnimatedVariable = noOfVariables;
                    currentOptVarValueLbl.setText("END");
                    // set title to final vector
                    int dataLength = Launcher.getDataPairs().size();
                    lineChart.setTitle(String.valueOf(Launcher.getDataPairs().get(dataLength-1).getVector()));

                    // set tool tips on all nodes.
                    ObservableList<XYChart.Series<Number,Number>> chartData = lineChart.getData();
                    // get series from chart
                    for (XYChart.Series series : chartData){
                        ObservableList<XYChart.Data> theData = series.getData();

                        // set tooltips for all datapoints of series
                        for (XYChart.Data dataPoint : theData){
                            Node dataPointNode = dataPoint.getNode();
                            final Tooltip tooltip = new Tooltip(String.valueOf("Value: " + dataPoint.getXValue()) + " : Fitness: " + df.format(dataPoint.getYValue()));
                            hackTooltipStartTiming(tooltip);
                            Tooltip.install(dataPointNode,tooltip);

                            // setting crosshair cursor for datapoint nodes - guides user when using tooltips
                            dataPointNode.setCursor(Cursor.CROSSHAIR);

                        }
                    }

                }


            }
        });

        // button to jump to start of previous variable
        Button previousVariableButton  = new Button("Previous Variable");
        previousVariableButton.setCursor(Cursor.HAND);
        previousVariableButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                // for rounding to 4dp
                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.HALF_EVEN);

                if (avmfAnimationSequence.getStatus() != Animation.Status.STOPPED){
                    if (currentAnimatedVariable > 0) {
                        currentAnimatedVariable--;
                        currentOptVarValueLbl.setText(String.valueOf(currentAnimatedVariable + 1)); // update reporting label
                    }

                    // set chart title
                    ArrayList<AvmfIterationOutput> dataPairs = Launcher.getDataPairs();
                    int currentLine = 0;
                    boolean firstLineFound = false;
                    for (AvmfIterationOutput pair : dataPairs){

                        if(pair.getIteration() == (currentAnimatedVariable + 1) && !firstLineFound){
//                            System.out.println("update vector"); // debugging
                            lineChart.setTitle("Current Vector: " + Launcher.getDataPairs().get(currentLine).getVector());
                            firstLineFound = true;
                        }
                        else{
                            currentLine++;
                        }
                    }

//                    System.out.println("currentAnimatedVariable: " + currentAnimatedVariable); // debugging
                    avmfAnimationSequence.jumpTo(String.valueOf(currentAnimatedVariable));


                    // remove tooltips from series now hidden.
                    ObservableList<XYChart.Series<Number,Number>> chartData = lineChart.getData();


                    // get from chart the series to remove tooltips from.
                    // remove tooltips from previously animated series and currently animated series.
                    for (int i = currentAnimatedVariable; i <= currentAnimatedVariable + 1; i++ ){
                        XYChart.Series series = chartData.get(i);
                        ObservableList<XYChart.Data> theData = series.getData();

                        // uninstall tooltips for all datapoints of series
                        for (XYChart.Data dataPoint : theData){
                            Node dataPointNode = dataPoint.getNode();
                            final Tooltip tooltip = new Tooltip(String.valueOf("Value: " + dataPoint.getXValue()) + " : Fitness: " + df.format(dataPoint.getYValue()));
                            hackTooltipStartTiming(tooltip);
                            Tooltip.uninstall(dataPointNode,tooltip);

                            // removes crosshair cursor for hidden nodes.
                            dataPointNode.setCursor(Cursor.DEFAULT);

                        }
                    }
                }






            }
        });

        // button to jump to start of next variable
        Button nextVariableButton  = new Button("Next Variable");
        nextVariableButton.setCursor(Cursor.HAND);
        nextVariableButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                // for rounding to 4dp
                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.HALF_EVEN);

                if (avmfAnimationSequence.getStatus() != Animation.Status.STOPPED){
                    // if possible increment index of current animated variable
                    if (currentAnimatedVariable < noOfVariables - 1) {
                        currentAnimatedVariable++;
                        currentOptVarValueLbl.setText(String.valueOf(currentAnimatedVariable + 1)); // update reporting label
                    }

                    // set chart title
                    ArrayList<AvmfIterationOutput> dataPairs = Launcher.getDataPairs();
                    int currentLine = 0;
                    boolean firstLineFound = false;
                    for (AvmfIterationOutput pair : dataPairs){

                        if(pair.getIteration() == (currentAnimatedVariable + 1) && !firstLineFound){
//                            System.out.println("update vector"); // debugging
                            lineChart.setTitle("Current Vector: " + Launcher.getDataPairs().get(currentLine).getVector());
                            firstLineFound = true;
                        }
                        else{
                            currentLine++;
                        }
                    }

//                    System.out.println("currentAnimatedVariable: " + currentAnimatedVariable); // debugging
                    // jump to cure point of next variable.
                    avmfAnimationSequence.jumpTo(String.valueOf(currentAnimatedVariable));




                    // add tooltips to series before the currently animated one
                    ObservableList<XYChart.Series<Number,Number>> chartData = lineChart.getData();
                    XYChart.Series series = chartData.get(currentAnimatedVariable-1);
                    ObservableList<XYChart.Data> theData = series.getData();

                    // set tooltips for all datapoints of series
                    for (XYChart.Data dataPoint : theData){
                        Node dataPointNode = dataPoint.getNode();
                        final Tooltip tooltip = new Tooltip(String.valueOf("Value: " + dataPoint.getXValue()) + " : Fitness: " + df.format(dataPoint.getYValue()));
                        hackTooltipStartTiming(tooltip);
                        Tooltip.install(dataPointNode,tooltip);

                        // setting crosshair cursor for datapoint nodes - guides user when using tooltips
                        dataPointNode.setCursor(Cursor.CROSSHAIR);

                    }

                }


            }
        });


        // ---------- Layout setup ------------//

        // main layout pane
        BorderPane mainLayout = new BorderPane();

        // unchanging labels
        Label headerTitle = new Label("Header Title");
        Label xZoomLbl = new Label("xZoom");
        Label yZoomLbl = new Label("yZoom");
        Label currentOptVarLbl = new Label("Optimising Variable:");
        Label animationStateLbl = new Label("Animation State:");
        Label animationRateLbl = new Label("Animation Rate:");
        Label animationControlLbl = new Label("Animation Control: ");

        Label variableValueLbl = new Label("Variable Value: ");
        Label variableFitnessLbl = new Label("Objective Value:");

        // header labels (static)
        Label searchNameLbl = new Label("Search Type:"); // might not use
        Label runningTimeLbl = new Label("Running Time:");
        Label bestObjValLbl = new Label("Best Objective Value:");
        Label bestVectorLbl = new Label("Best Vector:");
        Label numEvaluationsLbl = new Label("No. Evaluations:");
        Label numUniqueEvaluationsLbl = new Label("No. Unique Evaluations:");
        Label numRestartsLbl = new Label("No. Restarts:");
        // header data labels (static)
        Label dataSearchNameLbl = new Label(Launcher.runLog.getHeader().searchName); // might not use
        Label dataRunningTimeLbl = new Label(String.valueOf(Launcher.runLog.getHeader().runningTime + "ms"));
        Label dataBestObjValLbl = new Label(String.valueOf(Launcher.runLog.getHeader().bestObjVal));
        Label dataBestVectorLbl = new Label(String.valueOf(Launcher.runLog.getHeader().bestVector));
        Label dataNumEvaluationsLbl = new Label(String.valueOf(Launcher.runLog.getHeader().numEvaluations));
        Label dataNumUniqueEvaluationsLbl = new Label(String.valueOf(Launcher.runLog.getHeader().numUniqueEvaluations));
        Label dataNumRestartsLbl = new Label(String.valueOf(Launcher.runLog.getHeader().numRestarts));


        // termination policy labels (static)
        Label terminationPoilicyTitle = new Label("Termination Policy");
        Label terminateOnOptimalLbl = new Label("Terminate On Optimal:");
        Label maxEvaluationsLbl = new Label("Max Evaluations:");
        Label maxRestartsLbl = new Label("Max Restarts:");
        Label tpRunningTimeLbl = new Label("Max Running Time:");
        // termination policy labels
        Label dataTerminateOnOptimalLbl = new Label(String.valueOf(Launcher.runLog.getHeader().terminateOnOptimal));
        // (dynamically initialised labels)
        Label dataMaxEvaluationsLbl, dataMaxRestartsLbl, dataTpRunningTimeLbl;
        if (Launcher.runLog.getHeader().maxEvaluations == -1 ){
            dataMaxEvaluationsLbl = new Label("No Limit");
        }
        else{
            dataMaxEvaluationsLbl = new Label(String.valueOf(Launcher.runLog.getHeader().maxEvaluations));
        }

        if(Launcher.runLog.getHeader().maxRestarts == -1 ){
            dataMaxRestartsLbl = new Label("No Limit");
        }
        else{
            dataMaxRestartsLbl = new Label(String.valueOf(Launcher.runLog.getHeader().maxRestarts));
        }

        if(Launcher.runLog.getHeader().tpRunningTime == -1){
            dataTpRunningTimeLbl = new Label("No Limit");
        }
        else{
            dataTpRunningTimeLbl = new Label(String.valueOf(Launcher.runLog.getHeader().tpRunningTime) + "ms");
        }


        // setup header reporting area
        GridPane headerArea = new GridPane();
        headerArea.setPadding(new Insets(10, 10, 10, 10));
        GridPane.setHalignment(dataSearchNameLbl, HPos.CENTER);
        GridPane.setHalignment(terminationPoilicyTitle, HPos.CENTER);
        GridPane.setHalignment(dataBestVectorLbl, HPos.LEFT);

        headerArea.add(dataSearchNameLbl,0,0, 2, 1);
        headerArea.add(runningTimeLbl,0,1);
        headerArea.add(dataRunningTimeLbl,1,1);
        headerArea.add(bestObjValLbl,0,2);
        headerArea.add(dataBestObjValLbl,1,2);
        headerArea.add(numEvaluationsLbl,0,3);
        headerArea.add(dataNumEvaluationsLbl,1,3);
        headerArea.add(numUniqueEvaluationsLbl,0,4);
        headerArea.add(dataNumUniqueEvaluationsLbl,1,4);
        headerArea.add(numRestartsLbl,0,5);
        headerArea.add(dataNumRestartsLbl,1,5);

        // termination policy area
        headerArea.add(terminationPoilicyTitle,0,6, 2 , 1);
        headerArea.add(terminateOnOptimalLbl,0,7);
        headerArea.add(dataTerminateOnOptimalLbl,1,7);
        headerArea.add(maxEvaluationsLbl,0,8);
        headerArea.add(dataMaxEvaluationsLbl,1,8);
        headerArea.add(maxRestartsLbl,0,9);
        headerArea.add(dataMaxRestartsLbl,1,9);
        headerArea.add(tpRunningTimeLbl,0,10);
        headerArea.add(dataTpRunningTimeLbl,1,10);




        // setup zoom control box
        GridPane zoomControl = new GridPane();
        zoomControl.setHgap(10);
        zoomControl.setVgap(10);
        zoomControl.setPadding(new Insets(10, 10, 10, 10));

        zoomControl.add(xZoomLbl,0,0);
        zoomControl.add(yZoomLbl,1,0);
        zoomControl.add(xZoomSlider, 0,1);
        zoomControl.add(yZoomSlider,1,1);
        zoomControl.add(resetZoomButton,0,2,2,1);
        GridPane.setHalignment(resetZoomButton, HPos.CENTER);




        // setup live reporting area
        GridPane reportingArea = new GridPane();
        reportingArea.setPadding(new Insets(10, 10, 10, 10));
        reportingArea.add(currentOptVarLbl,0,0);
        reportingArea.add(currentOptVarValueLbl,1,0);

        reportingArea.add(variableValueLbl,0,1);
        reportingArea.add(pairVariableValueLbL,1,1);

        reportingArea.add(variableFitnessLbl,0,2);
        reportingArea.add(pairVariableObjValLbl, 1,2);

        reportingArea.add(animationStateLbl,0,3);
        reportingArea.add(animationStateValueLbl,1,3);

        reportingArea.add(animationRateLbl,0,4);
        reportingArea.add(animationRateValueLbl,1,4);



        // VBox for Right hand side
        VBox rightArea = new VBox();
        rightArea.getChildren().addAll(
                headerArea,
                zoomControl,
                reportingArea
        );


        // animation control area setup.
        HBox animationControl = new HBox();
        animationControl.setAlignment(Pos.BOTTOM_CENTER);
        animationControlLbl.setAlignment(Pos.TOP_CENTER);

        animationControl.getChildren().addAll(
                animationControlLbl,
                restartAnimationButton,
                playAnimationButton,
                pauseAnimationButton,
                decreaseRateButton,
                increaseRateButton,
                previousVariableButton,
                nextVariableButton,
                jumpToEndButton
        );



        // adding areas to main BorderPane layout
        mainLayout.setCenter(lineChart);
        BorderPane.setAlignment(animationControl, Pos.BOTTOM_RIGHT);
        mainLayout.setBottom(animationControl);
        mainLayout.setRight(rightArea);

        // setup scene with default 16:9 aspect ratio resolution
        Scene scene  = new Scene(mainLayout,1366,768);
        // make things happen!!!
        stage.setScene(scene);
        stage.show();


        // ---------- after stage.show() adjustments to GUI ------------- //

        // all this happening after stage.show() because autoranging has been done by this point and is needed to set default values in the slider.
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

        // setting original x axis bounds from autoranging
        originalXAxisLowerBound = xAxis.getLowerBound();
        originalXAxisUpperBound = xAxis.getUpperBound();

        // debugging
//        System.out.println("original X lower bound");
//        System.out.println(originalXAxisLowerBound);
//        System.out.println("original X upper bound");
//        System.out.println(originalXAxisUpperBound);

        // setting up logarithm calculation engine for x axis zooming
        xSetupLogCalc(0.1, originalXAxisUpperBound);
        // setting x zoom slider default values
        xZoomSlider.setMax(originalXAxisUpperBound);
        xZoomSlider.setValue(originalXAxisUpperBound);
        xZoomSlider.setMajorTickUnit(originalXAxisUpperBound);// Show no major tick marks. Can't easily show them because they don't correspond to logarithmic zoom




        final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();

        // setting original y axis bounds from autoranging
        originalYAxisLowerBound = yAxis.getLowerBound();
        originalYAxisUpperBound = yAxis.getUpperBound();

        // debugging
//        System.out.println("original Y lower bound");
//        System.out.println(originalYAxisLowerBound);
//        System.out.println("original Y upper bound");
//        System.out.println(originalYAxisUpperBound);


        // setting up logarithm calculation engine for y axis zooming
        ySetupLogCalc(0.1, originalYAxisUpperBound);
        // setting y zoom slider default values
        yZoomSlider.setMax(originalYAxisUpperBound);
        yZoomSlider.setValue(originalYAxisUpperBound);
        yZoomSlider.setMajorTickUnit(originalYAxisUpperBound);// Show no major tick marks. Can't easily show them because they don't correspond to logarithmic zoom

        // resetting the cursor and zoom to default, ensures correct cursor behaviour

        // resetting graph axes
        xAxis.setLowerBound(originalXAxisLowerBound);
        xAxis.setUpperBound(originalXAxisUpperBound);
        yAxis.setLowerBound(originalYAxisLowerBound);
        yAxis.setUpperBound(originalYAxisUpperBound);
        // updating sliders to match -- duplicate?
        xZoomSlider.setValue(originalXAxisUpperBound);
        yZoomSlider.setValue(originalYAxisUpperBound);
        lineChart.setCursor(Cursor.DEFAULT);


    }

// ------ Logarithm methods ------ //

    // Method takes in a value between x axis upper and lower bounds (value used from slider) and returns its logarithmic zoom equivalent
    private double xCalcLogValue(double value){
        double logValue = xALog * Math.exp(xBLog *value);
        return logValue;
    }

    // Method to calculate and store values needed for logarithmic x axis zoom calculations
    private void xSetupLogCalc(double x, double y){
        xBLog = Math.log(y/x)/(y-x);
        xALog = y / Math.exp(xBLog * y);
    }

    // Method takes in a value between y axis upper and lower bounds (value used from slider) and returns its logarithmic zoom equivalent
    private double yCalcLogValue(double value){
        double logValue = yALog * Math.exp(yBLog *value);
        return logValue;
    }

    // Method to calculate and store values needed for logarithmic y axis zoom calculations
    private void ySetupLogCalc(double x, double y){
        yBLog = Math.log(y/x)/(y-x);
        yALog = y / Math.exp(yBLog * y);
    }


    // --------- main methods --------- //

    // main method
    public static void main(String[] args) {
        launch(args);
    }

    // has exactly the same purpose as main method but name is more semantic so its use is favoured.
    public static void launchUI(String[] args) {
        launch(args);
    }

    // -------- Chart setup methods -------- //


    // method that returns a line chart set up with the series of all vector variables.
    private LineChart<Number,Number> makeLineChart(ArrayList<AvmfIterationOutput> dataPairs) {

        // initialisations
        double runningDurationTotal = 0.0;
        int previousVarCount = 0;

        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Value of Vector Variable");
        yAxis.setLabel("Objective Value");


        //creating the chart
        final LineChart<Number, Number> lineChart =
                new LineChart<Number, Number>(xAxis, yAxis);

        // chart title and formatting.
        lineChart.setTitle("Current Vector: " + Launcher.runLog.getDataPairs().get(currentln).getVector());
        Label chartTitle = (Label)lineChart.lookup(".chart-title");
        chartTitle.setFont(Font.font(12));
        chartTitle.setWrapText(true);


        // setting up and adding series for all vector variables to chart? also adding animation keyframes. Uses final set of pairs for index to make compatible with variable length string variables.
        for (int variableNo = 0; variableNo < dataPairs.get(dataPairs.size()-1).getVector().size(); variableNo++) {
            // setting up series
            XYChart.Series series = setUpSeries(dataPairs);
            lineChart.getData().add(series);
            // setting series line (represents landscape) to transparent
            Node seriesNode = series.getNode();
            seriesNode.setOpacity(0);

            // animation speed control constants.
            final double STANDARD_POINT_ANIMATION_TIME = 1000; // in milliseconds
            final double STANDARD_LANDSCAPE_ANIMATION_TIME = 2000; // in milliseconds

            // setting up animation for series line fade in.
            Timeline fadeInLandscape = new Timeline();
            fadeInLandscape.getKeyFrames().addAll(
                    new KeyFrame(new Duration(STANDARD_LANDSCAPE_ANIMATION_TIME), "landscape of Variable " + String.valueOf(variableNo),  new KeyValue(seriesNode.opacityProperty(), 1))
            );
            final int variableNoHelper = variableNo; // new final variable declaration to get variableNo into inner class
            // event handler to update the global variable keeping track of which vector variable is currently being animated
            fadeInLandscape.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    currentAnimatedVariable = variableNoHelper;
                    currentOptVarValueLbl.setText(String.valueOf(currentAnimatedVariable + 1)); // update reporting label for current variable being optimised
                    animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus())); // update reporting label for animation state.


                }
            });
            avmfAnimationSequence.getChildren().add(fadeInLandscape); // adding timeline fade to avmfAnimationSequence



            // setting up animation timeline for dropping points onto landscape.
            ObservableList<XYChart.Data> theData = series.getData();
            for (final XYChart.Data dataPoint : theData){
                final Node dataPointNode = dataPoint.getNode();
                // set initial properties
                dataPointNode.setOpacity(0);
                dataPointNode.setScaleX(4);
                dataPointNode.setScaleY(4);
                final Timeline dropPoint = new Timeline();
                // keyframes for doing the fade in and shrink onto landscape for each point.
                dropPoint.getKeyFrames().addAll(
                        new KeyFrame(new Duration(1000), new KeyValue(dataPointNode.opacityProperty(), 1 )),
                        new KeyFrame(new Duration(1000), new KeyValue(dataPointNode.scaleXProperty(), 1 )),
                        new KeyFrame(new Duration(1000), new KeyValue(dataPointNode.scaleYProperty(), 1 ))
                        );

                // event handler for drop point animation finished.
                dropPoint.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        DecimalFormat df = new DecimalFormat("#.####");
                        df.setRoundingMode(RoundingMode.HALF_EVEN);

                        double x = (double) dataPoint.getXValue();
                        double y = (double) dataPoint.getYValue();

                        pairVariableValueLbL.setText(String.valueOf(x));
                        pairVariableObjValLbl.setText(String.valueOf(df.format(y)));

                        // set chart title
                        ArrayList<AvmfIterationOutput> dataPairs = Launcher.getDataPairs();
                        int currentLine = 0;
                        for (AvmfIterationOutput pair : dataPairs){

                            if(pair.getIteration() == (currentAnimatedVariable + 1) && pair.getVector().get(currentAnimatedVariable) == x && pair.getObjVal() == y){
//                                System.out.println("update vector"); // debugging
                                lineChart.setTitle("Current Vector: " + Launcher.getDataPairs().get(currentLine).getVector());
                            }
                            else{
                                currentLine++;
                            }
                        }

//                        lineChart.setTitle("current Vector " + Launcher.getDataPairs().get(currentln).getVector());


                        // setting tooltip on data points
                        final Tooltip tooltip = new Tooltip(String.valueOf("Value: " + dataPoint.getXValue()) + " : Fitness: " + df.format(dataPoint.getYValue()));
                        hackTooltipStartTiming(tooltip);
                        Tooltip.install(dataPointNode,tooltip);

                        dataPointNode.setCursor(Cursor.CROSSHAIR);

                    }
                });


                avmfAnimationSequence.getChildren().addAll(dropPoint); // adding keyframes to avmfAnimationSequence.

            }
//            System.out.println("series.length : " + theData.size()); // Debugging

            // conditional allows the first cue point to = 0 milliseconds
            if (variableNo > 0){
                double varCount = previousVarCount; // explicitly coded fix for indexing issue where cue points were set off by 1.
                double thisVarDuration = varCount * STANDARD_POINT_ANIMATION_TIME; // length of this variables duration with offset from landscape fades.
                double thisVarStartTime = runningDurationTotal + thisVarDuration + STANDARD_LANDSCAPE_ANIMATION_TIME; // calculate Launcher time of this variableNo
                Duration nextVarStartDuration = new Duration(thisVarStartTime); // new duration object with the this variables Launcher time.
                avmfAnimationSequence.getCuePoints().put(String.valueOf(variableNo), nextVarStartDuration); // add cue point to avmfAnimationSequence.
                runningDurationTotal = thisVarStartTime; // update running total
            }
            else if (variableNo == 0){
                avmfAnimationSequence.getCuePoints().put(String.valueOf(variableNo), new Duration(0));
            }
            previousVarCount = theData.size(); // update for fixing indexing issue

        }



        // Panning on the graph axes
        lineChart.setOnMousePressed(new EventHandler<MouseEvent>() {
            // handler for initial mouse click that sets up first click points of pan.
            @Override
            public void handle(MouseEvent event) {
                // debugging
//                System.out.println("press!!!");
//                System.out.println(event.getX());
//                System.out.println(event.getY());

                clickPointX = event.getX();
                clickPointY = event.getY();
            }
        });
        lineChart.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // debugging
//                System.out.println("DRAG!!!");
//                System.out.println(event.getX());
//                System.out.println(event.getY());

                double newX = event.getX();
                double newY = event.getY();


                // both axis pans... think this is less intuitive with the cursor open handed. commented out.
//                if (clickPointX < newX && clickPointY < newY){
//                    System.out.println("Down Right");
//                    panGraph(lineChart,XDirection.LEFT, YDirection.DOWN);
//                }
//                else if(clickPointX > newX && clickPointY < newY){
//                    System.out.println("Down Left");
//                    panGraph(lineChart,XDirection.RIGHT, YDirection.DOWN);
//                }
//                else if(clickPointX < newX && clickPointY > newY){
//                    System.out.println("Up Right");
//                    panGraph(lineChart, XDirection.LEFT, YDirection.UP);
//                }
//                else if(clickPointX > newX && clickPointY > newY){
//                    System.out.println("Up Left");
//                    panGraph(lineChart, XDirection.RIGHT, YDirection.UP);
//                }

                // single axis pans
                if(clickPointY > newY){
                    System.out.println("Up");
                    panGraph(lineChart, XDirection.NONE, YDirection.UP);
                }
                else if(clickPointY < newY){
                    System.out.println("Down");
                    panGraph(lineChart, XDirection.NONE, YDirection.DOWN);
                }
                else if(clickPointX > newX){
                    System.out.println("Left");
                    panGraph(lineChart, XDirection.RIGHT, YDirection.NONE);
                }
                else if(clickPointX < newX){
                    System.out.println("Right");
                    panGraph(lineChart, XDirection.LEFT, YDirection.NONE);
                }

                // update click points
                clickPointX = newX;
                clickPointY = newY;

            }
        });


        // code for adding event handlers to legend items, code adapted from here: https://stackoverflow.com/questions/44956955/javafx-use-chart-legend-to-toggle-show-hide-series-possible
        for (Node n : lineChart.getChildrenUnmodifiable()) {
            if (n instanceof Legend) {
                Legend l = (Legend) n;
                for (final Legend.LegendItem li : l.getItems()) {
                    for (final XYChart.Series<Number, Number> s : lineChart.getData()) {
                        if (s.getName().equals(li.getText())) {
                            li.getSymbol().setCursor(Cursor.HAND); // Hint user that legend symbol is clickable
                            li.getSymbol().setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {

                                        // toggle greyed out symbol to show user toggle status.
                                        if (li.getSymbol().getOpacity() == 1){
                                            li.getSymbol().setOpacity(0.3);
                                        }
                                        else {
                                            li.getSymbol().setOpacity(1);
                                        }

                                        s.getNode().setVisible(!s.getNode().isVisible()); // Toggle visibility of line
                                        for (XYChart.Data<Number, Number> d : s.getData()) {
                                            if (d.getNode() != null) {
                                                d.getNode().setVisible(s.getNode().isVisible()); // Toggle visibility of every node in the series
                                            }
                                        }
                                }
                            });
                        }
                    }
                }
            }
        }

        return lineChart;
    }


    // method that given a set of data pairs returns a series for a current variable
    private XYChart.Series setUpSeries(ArrayList<AvmfIterationOutput> dataPairs){
        XYChart.Series series = new XYChart.Series();
        series.setName("Variable " + currentVariable);
//        System.out.println("making series for variable " + currentVariable); // debugging
//        System.out.println(dataPairs.get(dataPairs.size()-1).getVector().size()); //debugging

        // used in logic for determining if a series is finished. used for not including the wrap around.
        int previousIteration = 1;

        // alternative series generation attempting to work around variable length stringVariable vectors -- not completely successful
        if ((currentVariable - 1) > dataPairs.get(0).getVector().size()) {
//            System.out.println("Alternative series write"); // debugging
            for (AvmfIterationOutput pair : dataPairs) {
                if ((pair.getIteration() == currentVariable && pair.getVector().size() > currentVariable -1)) {
//                    System.out.print("writing it!"); // debugging
                    // only add to series if current variable still has pairs to add. This conditional filters out the extra wrap round at the end of an AVM run. Handles restarts by only making series for the first run of AVM algorithm.
                    if ((pair.getIteration() == currentVariable && pair.getRestartNo() == 0) && !currentVarFinished) {
                        ObservableList seriesData = series.getData();
                        // add the data point to series
                        seriesData.add(new XYChart.Data(pair.getVector().get(currentVariable - 1), pair.getObjVal()));
                    }
                    else if (pair.getIteration() != currentVariable && previousIteration == currentVariable){
//                        System.out.println("Series finished for var"); // debugging
                        currentVarFinished = true;
                    }
                    previousIteration = pair.getIteration();
                }
            }
        }
        // standard series generation
        else{
            for (AvmfIterationOutput pair : dataPairs) {
                // only add to series if current variable still has pairs to add. This conditional filters out the extra wrap round at the end of an AVM run. Handles restarts by only making series for the first run of AVM algorithm.
                if ((pair.getIteration() == currentVariable && pair.getRestartNo() == 0) && !currentVarFinished) {
                    ObservableList seriesData = series.getData();
                    // add the data point to series
                    seriesData.add(new XYChart.Data(pair.getVector().get(currentVariable - 1), pair.getObjVal()));
                }
                else if (pair.getIteration() != currentVariable && previousIteration == currentVariable){
//                    System.out.println("Series finished for var"); // debugging
                    currentVarFinished = true;
                }
                previousIteration = pair.getIteration();
            }
        }

//        System.out.println(series.getData()); // debugging
        // updating current variable stats
        currentVariable++;
        currentVarFinished = false;
        return series;
    }


    // ----------- zoom and panning methods -------------- //


    // for zooming x axis
    public void xZoom(LineChart<Number,Number> lineChart, double lowerBound, double upperBound){
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

        final double MINNIMUM_ZOOM_RANGE = 2;

        // Conditional sorts out maximum zoom, should not be closer than a range of 2 or issues appear.
        if (Math.abs(upperBound - lowerBound) > MINNIMUM_ZOOM_RANGE){
            xAxis.setLowerBound(lowerBound);
            xAxis.setUpperBound(upperBound);
        }

    }

    // for zooming y axis
    public void yZoom(LineChart<Number,Number> lineChart, double lowerBound, double upperBound){
        final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();

        final double MINNIMUM_ZOOM_RANGE = 2;

        if (Math.abs(upperBound - lowerBound) > MINNIMUM_ZOOM_RANGE){
            yAxis.setLowerBound(lowerBound);
            yAxis.setUpperBound(upperBound);
        }

    }


    private void panGraph(LineChart<Number,Number> lineChart, XDirection xDirection, YDirection yDirection) {
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();

        // setup for x pans
        double xCurrentLowerBound = xAxis.getLowerBound();
        double xCurrentUpperBound = xAxis.getUpperBound();
        double xWidth = Math.abs(xCurrentUpperBound) + Math.abs(xCurrentLowerBound);
        double xPanStep = xWidth / PAN_SENSITIVITY;

        // setup for y pans
        double yCurrentLowerBound = yAxis.getLowerBound();
        double yCurrentUpperBound = yAxis.getUpperBound();
        double yWidth = Math.abs(yCurrentUpperBound - yCurrentLowerBound);
        double yPanStep = yWidth / (PAN_SENSITIVITY); // still seems to sensitive at higher zooms.


        // X axis panning right
        if(xCurrentUpperBound < originalXAxisUpperBound){
            if (xDirection == XDirection.RIGHT){
                xAxis.setLowerBound(xCurrentLowerBound + xPanStep);
                xAxis.setUpperBound(xCurrentUpperBound + xPanStep);
            }
        }
        else{
//        System.out.println("reached X upper bound"); // terminal reporting/debugging
        }
        // X axis panning Left
        if(xCurrentLowerBound > originalXAxisLowerBound){
            if (xDirection == XDirection.LEFT){
                xAxis.setLowerBound(xCurrentLowerBound - xPanStep);
                xAxis.setUpperBound(xCurrentUpperBound - xPanStep);
            }
        }
        else {
//        System.out.println("reached X Lower bound"); // terminal reporting/debugging
        }

        // Y axis panning down
        if(yCurrentUpperBound < originalYAxisUpperBound){
            if (yDirection == YDirection.DOWN){
                yAxis.setLowerBound(yCurrentLowerBound + yPanStep);
                yAxis.setUpperBound(yCurrentUpperBound + yPanStep);
            }
        }
        else{
//        System.out.println("reached Y upper bound"); // terminal reporting/debugging
        }
        // Y axis panning up
        if(yCurrentLowerBound > originalYAxisLowerBound){
            if (yDirection == YDirection.UP){
                yAxis.setLowerBound(yCurrentLowerBound - yPanStep);
                yAxis.setUpperBound(yCurrentUpperBound - yPanStep);
            }
        }
        else {
//        System.out.println("reached Y Lower bound"); // terminal reporting/debugging
        }

    }

    // used to control the speed of tooltip show, default was too slow for use as inspection and feedback.
    // Adapted from code found here: https://stackoverflow.com/questions/26854301/how-to-control-the-javafx-tooltips-delay
    // note: Java 9 simplifies this considerably.
    public static void hackTooltipStartTiming(Tooltip tooltip) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(100)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
