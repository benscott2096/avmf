package org.avmframework.visualiser;



import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import com.sun.javafx.charts.Legend;
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
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import javafx.scene.text.Font;

import javafx.scene.control.Slider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.event.EventHandler;


import javafx.util.Duration;
import org.avmframework.Monitor;


import java.io.File;
import java.io.FileNotFoundException;
import java.math.RoundingMode;
import java.util.ArrayList;


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



public class FirstGraph extends Application {

    // to keep a record of what the original upper and lower bounds were calculated as using JavaFx bulit-in autoranging.
    private double originalXAxisLowerBound, originalXAxisUpperBound;
    private double originalYAxisLowerBound, originalYAxisUpperBound;

    // to store values involved in calculating logarithmic axes.
    private double xALog, xBLog, yALog, yBLog;

    // used to keep track of what variable is having its series set up.
    private int currentVariable = 1;
    // variable to keeping track of which vector variable is currently being animated
    private int currentAnimatedVariable = 0; // first landscape cuepoint has label 0
    private int noOfVariables;




    // the avmfAnimationSequence
    private SequentialTransition avmfAnimationSequence = new SequentialTransition();
    final private Label currentOptVarValueLbl = new Label(); // initialised empty
    final private Label animationRateValueLbl = new Label(String.valueOf(avmfAnimationSequence.getCurrentRate()));
    final private Label animationStateValueLbl = new Label(String.valueOf(avmfAnimationSequence.getStatus()));

    final private Label pairVariableValueLbL = new Label("");
    final private Label pairVariableFitnessLbl = new Label("");

    @Override public void start(final Stage stage) {

        String fileName = Monitor.getFileName();

        // handles loading in a file using file chooser if visualiser not implemented in AVMf... todo: reword
        if (!start.fileLoaded){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog(stage);
            String filePath = String.valueOf(file.toPath());
            fileName = file.getName();
            System.out.println(filePath);
            start.setJsonFileName(filePath);
            try{
                start.loadRunLog(filePath);
                System.out.println("File Loaded");
//                System.out.println(start.getDataPairs());

            }
            catch(FileNotFoundException e){
                System.out.println("Error loading JSON run log file");
            }
        }

        noOfVariables = start.getDataPairs().get(0).getVector().size();

        stage.setTitle(fileName);

    // setting event handler for animation sequence finish.
    avmfAnimationSequence.setOnFinished(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {

            animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus()));
            currentOptVarValueLbl.setText("END");
        }
    });

        final LineChart<Number,Number> lineChart = makeLineChart(start.getDataPairs());


        final Slider xZoomSlider = new Slider(1, 100, originalXAxisUpperBound);
        xZoomSlider.setCursor(Cursor.HAND);
        xZoomSlider.setOrientation(Orientation.VERTICAL);
        xZoomSlider.setShowTickLabels(true);
        // Adding Listener to value property.
        xZoomSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, //
                                Number oldValue, Number newValue) {

                final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
                xAxis.setAutoRanging(false);

                double currentXLowerBound = xAxis.getLowerBound();
                double currentXUpperBound = xAxis.getUpperBound();

                System.out.println("Slider X has slidden!!!");

                // converts slider value into logarithmic value.
                double logNewValue = xCalcLogValue((double) newValue);
                // find new centre of x zoom when graph panned
                double pannedMidpoint = (currentXUpperBound + currentXLowerBound)/2;
                // calculating new bounds
                double newLowerBound = - (logNewValue - pannedMidpoint);
                double newUpperBound = logNewValue + pannedMidpoint;

                xZoom(lineChart, newLowerBound, newUpperBound);

                lineChart.setCursor(Cursor.OPEN_HAND);
            }
        });


        final Slider yZoomSlider = new Slider(1, 100, originalYAxisUpperBound);
        yZoomSlider.setCursor(Cursor.HAND);
        yZoomSlider.setOrientation(Orientation.VERTICAL);
        yZoomSlider.setShowTickLabels(true);
        // Adding Listener to value property.
        yZoomSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, //
                                Number oldValue, Number newValue) {

                final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
                yAxis.setAutoRanging(false);

                double currentYLowerBound = yAxis.getLowerBound();
                double currentYUpperBound = yAxis.getUpperBound();

                System.out.println("Slider Y has slidden!!!");


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

        // button for resetting the graph to largest.
        Button resetZoomButton = new Button("Reset Zoom");
        resetZoomButton.setCursor(Cursor.HAND);
        resetZoomButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
                final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
                // reseting graph axes
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

        Button pauseAnimationButton  = new Button("Pause");
        pauseAnimationButton.setCursor(Cursor.HAND);
        pauseAnimationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                avmfAnimationSequence.pause();
                animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus()));


            }
        });

        final Button restartAnimationButton  = new Button("Start");
        restartAnimationButton.setCursor(Cursor.HAND);
        restartAnimationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                restartAnimationButton.setText("Restart");
                avmfAnimationSequence.playFromStart();
                animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus()));


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

        Button jumpToEndButton  = new Button("Jump To End");
        jumpToEndButton.setCursor(Cursor.HAND);
        jumpToEndButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.HALF_EVEN);

                if (avmfAnimationSequence.getStatus() != Animation.Status.STOPPED){
                    // jump to end and update labels
                    avmfAnimationSequence.play();
                    avmfAnimationSequence.jumpTo("end");
                    animationStateValueLbl.setText(String.valueOf(avmfAnimationSequence.getStatus()));
                    currentAnimatedVariable = noOfVariables;
                    currentOptVarValueLbl.setText("END");




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


        Button previousVariableButton  = new Button("Previous Variable");
        previousVariableButton.setCursor(Cursor.HAND);
        previousVariableButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.HALF_EVEN);

                if (avmfAnimationSequence.getStatus() != Animation.Status.STOPPED){
                    if (currentAnimatedVariable > 0) {
                        currentAnimatedVariable--;
                        currentOptVarValueLbl.setText(String.valueOf(currentAnimatedVariable + 1)); // update reporting label
                    }
                    System.out.println("currentAnimatedVariable: " + currentAnimatedVariable); // debugging
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

        Button nextVariableButton  = new Button("Next Variable");
        nextVariableButton.setCursor(Cursor.HAND);
        nextVariableButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.HALF_EVEN);

                if (avmfAnimationSequence.getStatus() != Animation.Status.STOPPED){
                    // if possible increment index of current animated variable
                    if (currentAnimatedVariable < noOfVariables - 1) {
                        currentAnimatedVariable++;
                        currentOptVarValueLbl.setText(String.valueOf(currentAnimatedVariable + 1)); // update reporting label
                    }
                    System.out.println("currentAnimatedVariable: " + currentAnimatedVariable); // debugging
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



        BorderPane border = new BorderPane();


        // unchanging labels
        Label headerTitle = new Label("Header Title");
        Label xZoomLbl = new Label("xZoom");
        Label yZoomLbl = new Label("yZoom");
        Label currentOptVarLbl = new Label("Optimising Variable:");
        Label animationStateLbl = new Label("Animation State:");
        Label animationRateLbl = new Label("Animation Rate:");
        Label animationControlLbl = new Label("Animation Control: ");

        Label variableValueLbl = new Label("Variable Value: ");
        Label variableFitnessLbl = new Label("Variable Fitness");

        // header labels
        Label searchNameLbl = new Label("Search Type:"); // might not use
        Label runningTimeLbl = new Label("Running Time:");
        Label bestObjValLbl = new Label("Best Objective Value:");
        Label bestVectorLbl = new Label("Best Vector:");
        Label numEvaluationsLbl = new Label("No. Evaluations:");
        Label numUniqueEvaluationsLbl = new Label("No. Unique Evaluations:");
        Label numRestartsLbl = new Label("No. Restarts:");
        // header data labels
        Label dataSearchNameLbl = new Label(start.runLog.getHeader().searchName); // might not use
        Label dataRunningTimeLbl = new Label(String.valueOf(start.runLog.getHeader().runningTime + "ms"));
        Label dataBestObjValLbl = new Label(String.valueOf(start.runLog.getHeader().bestObjVal));
        Label dataBestVectorLbl = new Label(String.valueOf(start.runLog.getHeader().bestVector));
        Label dataNumEvaluationsLbl = new Label(String.valueOf(start.runLog.getHeader().numEvaluations));
        Label dataNumUniqueEvaluationsLbl = new Label(String.valueOf(start.runLog.getHeader().numUniqueEvaluations));
        Label dataNumRestartsLbl = new Label(String.valueOf(start.runLog.getHeader().numRestarts));


        // termination policy labels
        Label terminationPoilicyTitle = new Label("Termination Policy");
        Label terminateOnOptimalLbl = new Label("Terminate On Optimal:");
        Label maxEvaluationsLbl = new Label("Max Evaluations:");
        Label maxRestartsLbl = new Label("Max Restarts:");
        Label tpRunningTimeLbl = new Label("Max Running Time:");
        // termination policy labels
        Label dataTerminateOnOptimalLbl = new Label(String.valueOf(start.runLog.getHeader().terminateOnOptimal));
        //

        Label dataMaxEvaluationsLbl, dataMaxRestartsLbl, dataTpRunningTimeLbl;


        if (start.runLog.getHeader().maxEvaluations == -1 ){
            dataMaxEvaluationsLbl = new Label("No Limit");
        }
        else{
            dataMaxEvaluationsLbl = new Label(String.valueOf(start.runLog.getHeader().maxEvaluations));
        }

        if(start.runLog.getHeader().maxRestarts == -1 ){
            dataMaxRestartsLbl = new Label("No Limit");
        }
        else{
            dataMaxRestartsLbl = new Label(String.valueOf(start.runLog.getHeader().maxRestarts));
        }

        if(start.runLog.getHeader().tpRunningTime == -1){
            dataTpRunningTimeLbl = new Label("No Limit");
        }
        else{
            dataTpRunningTimeLbl = new Label(String.valueOf(start.runLog.getHeader().tpRunningTime) + "ms");
        }





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


        HBox animationControl = new HBox();
        animationControl.setAlignment(Pos.BOTTOM_CENTER);


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

        animationControlLbl.setAlignment(Pos.TOP_CENTER);

        // header
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

        headerArea.add(bestVectorLbl,0,3);
//        headerArea.add(dataBestVectorLbl,0,4, 2, 1);

        headerArea.add(numEvaluationsLbl,0,5);
        headerArea.add(dataNumEvaluationsLbl,1,5);

        headerArea.add(numUniqueEvaluationsLbl,0,6);
        headerArea.add(dataNumUniqueEvaluationsLbl,1,6);

        headerArea.add(numRestartsLbl,0,7);
        headerArea.add(dataNumRestartsLbl,1,7);

        // termination policy area
        headerArea.add(terminationPoilicyTitle,0,8, 2 , 1);

        headerArea.add(terminateOnOptimalLbl,0,9);
        headerArea.add(dataTerminateOnOptimalLbl,1,9);

        headerArea.add(maxEvaluationsLbl,0,10);
        headerArea.add(dataMaxEvaluationsLbl,1,10);

        headerArea.add(maxRestartsLbl,0,11);
        headerArea.add(dataMaxRestartsLbl,1,11);

        headerArea.add(tpRunningTimeLbl,0,12);
        headerArea.add(dataTpRunningTimeLbl,1,12);







        GridPane reportingArea = new GridPane();
        reportingArea.add(currentOptVarLbl,0,0);
        reportingArea.add(currentOptVarValueLbl,1,0);
        reportingArea.add(animationStateLbl,0,1);
        reportingArea.add(animationStateValueLbl,1,1);
        reportingArea.add(animationRateLbl,0,2);
        reportingArea.add(animationRateValueLbl,1,2);
        reportingArea.add(variableValueLbl,0,3);
        reportingArea.add(pairVariableValueLbL,1,3);
        reportingArea.add(variableFitnessLbl,0,4);
        reportingArea.add(pairVariableFitnessLbl, 1,4);



        // VBox for Right hand side
        VBox rightArea = new VBox();
        rightArea.getChildren().addAll(
                headerArea,
                zoomControl,
                reportingArea
        );

        // adding areas to main layout
        border.setCenter(lineChart);
        BorderPane.setAlignment(animationControl, Pos.BOTTOM_RIGHT);
        border.setBottom(animationControl);


        border.setRight(rightArea);



        Scene scene  = new Scene(border,1366,768);


        stage.setScene(scene);
        stage.show();



        // all this happening after stage.show() because autoranging has been done by this point and is needed to set default values in the slider.
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

        // setting original x axis bounds from autoranging
        originalXAxisLowerBound = xAxis.getLowerBound();
        originalXAxisUpperBound = xAxis.getUpperBound();

        // debugging
        System.out.println("original X lower bound");
        System.out.println(originalXAxisLowerBound);
        System.out.println("original X upper bound");
        System.out.println(originalXAxisUpperBound);

        // setting up logarithm calculation engine for x axis zooming
        xSetupLogCalc(0.1, originalXAxisUpperBound);
        // setting x zoom slider default values
        xZoomSlider.setMax(originalXAxisUpperBound);
        xZoomSlider.setValue(originalXAxisUpperBound);
        xZoomSlider.setMajorTickUnit(originalXAxisUpperBound); // line to fudge the showing of no major tick marks. Can't easily show them because they don't correspond to logarithmic zoom



        final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();

        // setting original y axis bounds from autoranging
        originalYAxisLowerBound = yAxis.getLowerBound();
        originalYAxisUpperBound = yAxis.getUpperBound();

        // debugging
        System.out.println("original Y lower bound");
        System.out.println(originalYAxisLowerBound);
        System.out.println("original Y upper bound");
        System.out.println(originalYAxisUpperBound);


        // setting up logarithm calculation engine for y axis zooming
        ySetupLogCalc(0.1, originalYAxisUpperBound);
        // setting y zoom slider default values
        yZoomSlider.setMax(originalYAxisUpperBound);
        yZoomSlider.setValue(originalYAxisUpperBound);
        yZoomSlider.setMajorTickUnit(originalYAxisUpperBound);// line to fudge the showing of no major tick marks. Can't easily show them because they don't correspond to logarithmic zoom

        lineChart.setCursor(Cursor.DEFAULT);


    }


    // takes in a value between x axis upper and lower bounds (value used from slider) and returns its logarithmic zoom equivalent
    private double xCalcLogValue(double value){
        double logValue = xALog * Math.exp(xBLog *value);
        return logValue;
    }

    // method to calculate and store values needed for logarithmic x axis zoom calculations
    private void xSetupLogCalc(double x, double y){
        xBLog = Math.log(y/x)/(y-x);
        xALog = y / Math.exp(xBLog * y);
    }

    // takes in a value between y axis upper and lower bounds (value used from slider) and returns its logarithmic zoom equivalent
    private double yCalcLogValue(double value){
        double logValue = yALog * Math.exp(yBLog *value);
        return logValue;
    }

    // method to calculate and store values needed for logarithmic y axis zoom calculations
    private void ySetupLogCalc(double x, double y){
        yBLog = Math.log(y/x)/(y-x);
        yALog = y / Math.exp(yBLog * y);
    }




    public static void main(String[] args) {
        launch(args);
    }

    public static void launchUI(String[] args) {
        launch(args);
    }


    // method that returns a line chart set up with the series of all vector variables.
    private LineChart<Number,Number> makeLineChart(ArrayList<AvmfIterationOutput> dataPairs) {

        double runningDurationTotal = 0.0;
        int previousVarCount = 0;

        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Value of Vector Variable");
        yAxis.setLabel("Fitness (lower is better)");


        //creating the chart
        final LineChart<Number, Number> lineChart =
                new LineChart<Number, Number>(xAxis, yAxis);

        lineChart.setTitle("Optimising to best vector: " + start.runLog.getHeader().bestVector); // todo: change!!!
//lineChart.t().set(Font.font(15));
        Label chartTitle = (Label)lineChart.lookup(".chart-title");
        chartTitle.setFont(Font.font(11));
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
            final int variableNoHelper = variableNo; // need new final variable declaration to get variableNo into inner class
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

                dropPoint.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {

                        DecimalFormat df = new DecimalFormat("#.####");
                        df.setRoundingMode(RoundingMode.HALF_EVEN);

                        pairVariableValueLbL.setText(String.valueOf(dataPoint.getXValue()));
                        pairVariableFitnessLbl.setText(String.valueOf(df.format(dataPoint.getYValue())));

                        // setting tooltip on data points


                        final Tooltip tooltip = new Tooltip(String.valueOf("Value: " + dataPoint.getXValue()) + " : Fitness: " + df.format(dataPoint.getYValue()));
                        hackTooltipStartTiming(tooltip);
                        Tooltip.install(dataPointNode,tooltip);

                        dataPointNode.setCursor(Cursor.CROSSHAIR);
                    }
                });


                avmfAnimationSequence.getChildren().addAll(dropPoint); // adding keyframes to avmfAnimationSequence.

            }
            System.out.println("series.length : " + theData.size());

            // conditional allows the first cue point to = 0 milliseconds
            if (variableNo > 0){
                double varCount = previousVarCount; // explicitly coded fix for indexing issue where cue points were set off by 1.
                double thisVarDuration = varCount * STANDARD_POINT_ANIMATION_TIME; // length of this variables duration with offset from landscape fades.
                double thisVarStartTime = runningDurationTotal + thisVarDuration + STANDARD_LANDSCAPE_ANIMATION_TIME; // calculate start time of this variableNo
                Duration nextVarStartDuration = new Duration(thisVarStartTime); // new duration object with the this variables start time.
                avmfAnimationSequence.getCuePoints().put(String.valueOf(variableNo), nextVarStartDuration); // add cue point to avmfAnimationSequence.
                runningDurationTotal = thisVarStartTime; // update running total
            }
            else if (variableNo == 0){
                avmfAnimationSequence.getCuePoints().put(String.valueOf(variableNo), new Duration(0));
            }
            previousVarCount = theData.size(); // update for fixing indexing issue

        }



        // Panning on the graph axes
        lineChart.setOnMousePressed(pressHandler);
        lineChart.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // debugging
                System.out.println("DRAG!!!");
                System.out.println(event.getX());
                System.out.println(event.getY());

                double newX = event.getX();
                double newY = event.getY();


                // both axis pans... think this is less intuutive with the cursor open handed.
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


    // used to control the speed of tooltip show.
    // found here: https://stackoverflow.com/questions/26854301/how-to-control-the-javafx-tooltips-delay
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


    private Double clickPointX,clickPointY = 0.0; // move to top?

// event handler for initial mouse click that sets up first click points of pan. // todo: make annoymous?
    EventHandler<MouseEvent> pressHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            System.out.println("press!!!");
            System.out.println(event.getX());
            System.out.println(event.getY());

            clickPointX = event.getX();
            clickPointY = event.getY();

        }
    };


final double PAN_SENSITIVITY = 50; // higher = less speed -- might need to split into x and y? wait until graph correct size.

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
        System.out.println("reached X upper bound"); // terminal reporting/debugging
    }
    // X axis panning Left
    if(xCurrentLowerBound > originalXAxisLowerBound){
        if (xDirection == XDirection.LEFT){
            xAxis.setLowerBound(xCurrentLowerBound - xPanStep);
            xAxis.setUpperBound(xCurrentUpperBound - xPanStep);
        }
    }
    else {
        System.out.println("reached X Lower bound"); // terminal reporting/debugging
    }

    // Y axis panning down
    if(yCurrentUpperBound < originalYAxisUpperBound){
        if (yDirection == YDirection.DOWN){
            yAxis.setLowerBound(yCurrentLowerBound + yPanStep);
            yAxis.setUpperBound(yCurrentUpperBound + yPanStep);
        }
    }
    else{
        System.out.println("reached Y upper bound"); // terminal reporting/debugging
    }
    // Y axis panning up
    if(yCurrentLowerBound > originalYAxisLowerBound){
        if (yDirection == YDirection.UP){
            yAxis.setLowerBound(yCurrentLowerBound - yPanStep);
            yAxis.setUpperBound(yCurrentUpperBound - yPanStep);
        }
    }
    else {
        System.out.println("reached Y Lower bound"); // terminal reporting/debugging
    }



}



    private XYChart.Series setUpSeries(ArrayList<AvmfIterationOutput> dataPairs){
        XYChart.Series series = new XYChart.Series();
        series.setName("Variable " + currentVariable);
        System.out.println("making series for variable " + currentVariable);
        System.out.println(dataPairs.get(dataPairs.size()-1).getVector().size());



        // todo: introduce final variable(s) after
        if ((currentVariable - 1) > dataPairs.get(0).getVector().size()) {
            System.out.println("Alternative series write");
            for (AvmfIterationOutput pair : dataPairs) {
                if ((pair.getIteration() == currentVariable && pair.getVector().size() > currentVariable -1)) {
                    System.out.print("writing it!");
                    ObservableList seriesData = series.getData();
                    // add the data point to series
                    seriesData.add(new XYChart.Data(pair.getVector().get(currentVariable - 1), pair.getObjVal()));
                }
            }


        }
        else{
            for (AvmfIterationOutput pair : dataPairs) {
                if ((pair.getIteration() == currentVariable)) {
                    ObservableList seriesData = series.getData();
                    // add the data point to series
                    seriesData.add(new XYChart.Data(pair.getVector().get(currentVariable - 1), pair.getObjVal()));
                }
            }
        }

        currentVariable++;
        return series;
    }


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

}
