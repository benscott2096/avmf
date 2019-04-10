package org.avmframework.visualiser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.Button;

import javafx.scene.control.Slider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.event.EventHandler;




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

    private double originalXAxisLowerBound, originalXAxisUpperBound;
    private double originalYAxisLowerBound, originalYAxisUpperBound;

    private double xALog, xBLog, yALog, yBLog;

    private int currentVariable = 1;

    @Override public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");


        final LineChart<Number,Number> lineChart = makeLineChart(start.getDataPairs());

        VBox layout= new VBox();






        final Slider xZoomSlider = new Slider(0.1, 100, originalXAxisUpperBound);
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

                xZoom(lineChart, - (logNewValue - pannedMidpoint), (logNewValue + pannedMidpoint));
            }
        });


        final Slider yZoomSlider = new Slider(0.1, 100, originalYAxisUpperBound);
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


//                yZoom(lineChart, - (logNewValue - pannedMidpoint), (logNewValue + pannedMidpoint));
                double newLowerBound = - (logNewValue - pannedMidpoint);
                double newUpperBound = logNewValue + pannedMidpoint;

//
//                if (- (logNewValue - pannedMidpoint) > originalYAxisLowerBound && (logNewValue + pannedMidpoint) < originalYAxisUpperBound){
//                    yZoom(lineChart, - (logNewValue - pannedMidpoint), (logNewValue + pannedMidpoint));
//                }
//                else{
//                    yZoom(lineChart, originalYAxisLowerBound, originalYAxisUpperBound);
//                }

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




            }
        });

        Button resetGraphButton = new Button("Reset Graph");
        resetGraphButton.setOnAction(new EventHandler<ActionEvent>() {
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
            }
        });

        layout.setMargin(lineChart, new Insets(20, 20, 20, 20));
        layout.setMargin(xZoomSlider, new Insets(20, 20, 20, 20));
        layout.setMargin(yZoomSlider, new Insets(20, 20, 20, 20));
        layout.setMargin(resetGraphButton, new Insets(20, 20, 20, 20));


        layout.getChildren().add(lineChart);
        layout.getChildren().add(xZoomSlider);
        layout.getChildren().add(yZoomSlider);
        layout.getChildren().add(resetGraphButton);

        Scene scene  = new Scene(layout,800,600);


        stage.setScene(scene);
        stage.show();



        // all this happening after stage.show() because autoranging done at this point and is needed to set default values in the slider.
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

        originalXAxisLowerBound = xAxis.getLowerBound();
        originalXAxisUpperBound = xAxis.getUpperBound();

        System.out.println("original X lower bound");
        System.out.println(originalXAxisLowerBound);

        System.out.println("original X upper bound");
        System.out.println(originalXAxisUpperBound);


        xSetupLogCalc(0.1, originalXAxisUpperBound);

        xZoomSlider.setMax(originalXAxisUpperBound);
        xZoomSlider.setValue(originalXAxisUpperBound);
        xZoomSlider.setMajorTickUnit(originalXAxisUpperBound/4);







        final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();

        originalYAxisLowerBound = yAxis.getLowerBound();
        originalYAxisUpperBound = yAxis.getUpperBound();

        System.out.println("original Y lower bound");
        System.out.println(originalYAxisLowerBound);

        System.out.println("original Y upper bound");
        System.out.println(originalYAxisUpperBound);


        ySetupLogCalc(0.1, originalYAxisUpperBound);

        yZoomSlider.setMax(originalYAxisUpperBound);
        yZoomSlider.setValue(originalYAxisUpperBound);
        yZoomSlider.setMajorTickUnit(originalYAxisUpperBound/4);


    }

    private double xCalcLogValue(double value){

        double logValue = xALog * Math.exp(xBLog *value);
        return logValue;
    }

    private void xSetupLogCalc(double x, double y){
//        double x = 0.1;
//        double y = 500000;
        xBLog = Math.log(y/x)/(y-x);
        xALog = y / Math.exp(xBLog * y);
    }


    private double yCalcLogValue(double value){

        double logValue = yALog * Math.exp(yBLog *value);
        return logValue;
    }

    private void ySetupLogCalc(double x, double y){
//        double x = 0.1;
//        double y = 500000;
        yBLog = Math.log(y/x)/(y-x);
        yALog = y / Math.exp(yBLog * y);
    }





    public static void main(String[] args) {
        launch(args);
    }

    public static void launchUI(String[] args) {
        launch(args);
    }





    private LineChart<Number,Number> makeLineChart(ArrayList<AvmfIterationOutput> dataPairs) {

        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Value of Vector Variable");
        yAxis.setLabel("Fitness (lower is better)");


        //creating the chart
        final LineChart<Number, Number> lineChart =
                new LineChart<Number, Number>(xAxis, yAxis);

        lineChart.setTitle("Test Line Graph");


        //defining a series
//        XYChart.Series series = setUpSeries(dataPairs);

//        lineChart.getData().addAll(series);

        for (int it = 0; it < dataPairs.get(0).getVector().size(); it++) {
            XYChart.Series series = setUpSeries(dataPairs);
            lineChart.getData().add(series);
        }

//        lineChart.addEventHandler(DragEvent.DRAG_OVER , new EventHandler<DragEvent>() {
//
//            public void handle(DragEvent) {System.out.println("such a drag!"); }
//
//
//        });

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


                // both axis pans
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
                else if(clickPointY > newY){
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

        return lineChart;
    }


    private Double clickPointX,clickPointY = 0.0; // move to top?

// event handler for initial mouse click that sets up first click points of pan.
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


final double PAN_SENSITIVITY = 100.0; // higher = more sensitive -- might need to split into x and y? wait until graph correct size.

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
    double yWidth = Math.abs(yCurrentUpperBound) + Math.abs(yCurrentLowerBound);
    double yPanStep = yWidth / PAN_SENSITIVITY;

    // X axis panning right
    if(xCurrentUpperBound < originalXAxisUpperBound){
        if (xDirection == XDirection.RIGHT){
            xAxis.setLowerBound(xCurrentLowerBound + xPanStep);
            xAxis.setUpperBound(xCurrentUpperBound + xPanStep);
        }
    }
    else{
        System.out.println("reached X upper bound");
    }
    // X axis panning Left
    if(xCurrentLowerBound > originalXAxisLowerBound){
        if (xDirection == XDirection.LEFT){
            xAxis.setLowerBound(xCurrentLowerBound - xPanStep);
            xAxis.setUpperBound(xCurrentUpperBound - xPanStep);
        }
    }
    else {
        System.out.println("reached X Lower bound");
    }

    // Y axis panning down
    if(yCurrentUpperBound < originalYAxisUpperBound){
        if (yDirection == YDirection.DOWN){
            yAxis.setLowerBound(yCurrentLowerBound + yPanStep);
            yAxis.setUpperBound(yCurrentUpperBound + yPanStep);
        }
    }
    else{
        System.out.println("reached Y upper bound");
    }
    // Y axis panning up
    if(yCurrentLowerBound > originalYAxisLowerBound){
        if (yDirection == YDirection.UP){
            yAxis.setLowerBound(yCurrentLowerBound - yPanStep);
            yAxis.setUpperBound(yCurrentUpperBound - yPanStep);
        }
    }
    else {
        System.out.println("reached Y Lower bound");
    }



}



    private XYChart.Series setUpSeries(ArrayList<AvmfIterationOutput> dataPairs){

        XYChart.Series series = new XYChart.Series();
        series.setName("Variable " + currentVariable);


        for (AvmfIterationOutput pair : dataPairs){
            if ((pair.getIteration() == currentVariable)){
                series.getData().add(new XYChart.Data(pair.getVector().get(currentVariable -1), pair.getObjVal()));
            }
        }
        currentVariable++;
        return series;
    }

    // why do I have this?
    public void xZoom(LineChart<Number,Number> lineChart){
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        System.out.println(xAxis.getLowerBound());

        xAxis.setLowerBound(-10);
        xAxis.setUpperBound(10);


    }

    // for zooming x axis
    public void xZoom(LineChart<Number,Number> lineChart, double lowerBound, double upperBound){
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
//        System.out.println(xAxis.getLowerBound()); // debugging

        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);

    }

    // for zooming y axis
    public void yZoom(LineChart<Number,Number> lineChart, double lowerBound, double upperBound){
        final NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
//        System.out.println(yAxis.getLowerBound()); // debugging

        yAxis.setLowerBound(lowerBound);
        yAxis.setUpperBound(upperBound);

    }

}
