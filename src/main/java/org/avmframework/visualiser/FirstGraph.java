package org.avmframework.visualiser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.FlowPane;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;



import java.util.ArrayList;

public class FirstGraph extends Application {

    private double originalXAxisLowerBound;
    private double originalXAxisUpperBound;

    @Override public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");


        final LineChart<Number,Number> lineChart = makeLineChart(start.getDataPairs());

        FlowPane layout= new FlowPane();






        Slider zoomSlider = new Slider(0.1, 100, originalXAxisUpperBound);
//        zoomSlider.setMin(0.1); // change to constant
//        zoomSlider.setMax(originalXAxisUpperBound);
        zoomSlider.setShowTickLabels(true);
        // Adding Listener to value property.
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, //
                                Number oldValue, Number newValue) {

                final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
                xAxis.setAutoRanging(false);

            System.out.println("Slider has slidden!!!");
                System.out.println(oldValue);
                System.out.println(newValue);


                // todo: replace min/max things for constants.

                // converts slider value into logarithmic value.
                    double logNewValue = calcLogValue((double) newValue);


                    xZoom(lineChart, - logNewValue, logNewValue);
            }
        });



        layout.getChildren().add(lineChart);
        layout.getChildren().add(zoomSlider);

        Scene scene  = new Scene(layout,800,600);


        stage.setScene(scene);
        stage.show();



        // all this happening after stage.show() because autoranging done at this point and is needed to set default values in the slider.
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

        originalXAxisLowerBound = xAxis.getLowerBound();
        originalXAxisUpperBound = xAxis.getUpperBound();

        System.out.println("original lower bound");
        System.out.println(originalXAxisLowerBound);

        System.out.println("original upper bound");
        System.out.println(originalXAxisUpperBound);


        setupLogCalc(0.1, originalXAxisUpperBound);

        zoomSlider.setMax(originalXAxisUpperBound);
        zoomSlider.setValue(originalXAxisUpperBound);




    }

    private double calcLogValue(double value){

        double logValue = aLog * Math.exp(bLog*value);
        return logValue;
    }

    private void setupLogCalc(double x, double y){
//        double x = 0.1;
//        double y = 500000;
        bLog = Math.log(y/x)/(y-x);
        aLog = y / Math.exp(bLog * y);


    }

    private double aLog,bLog;



    public static void main(String[] args) {
        launch(args);
    }

    public static void launchUI(String[] args) {
        launch(args);
    }





    private LineChart<Number,Number> makeLineChart(ArrayList<AvmfIterationOutput> dataPairs){

        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Value of Vector Variable");
        yAxis.setLabel("Fitness (lower is better)");



        //creating the chart
        final LineChart<Number,Number> lineChart =
                new LineChart<Number,Number>(xAxis,yAxis);

        lineChart.setTitle("Test Line Graph");




        //defining a series
        XYChart.Series series = setUpSeries(dataPairs);

        lineChart.getData().addAll(series);



        return lineChart;
    }


    private XYChart.Series setUpSeries(ArrayList<AvmfIterationOutput> dataPairs){

        XYChart.Series series = new XYChart.Series();
        series.setName("Restart 1");


        for (AvmfIterationOutput pair : dataPairs){
            series.getData().add(new XYChart.Data(pair.getVector().get(0), pair.getObjVal()));
        }

        return series;
    }


    public void xZoom(LineChart<Number,Number> lineChart){
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        System.out.println(xAxis.getLowerBound());

        xAxis.setLowerBound(-10);
        xAxis.setUpperBound(10);


    }

    public void xZoom(LineChart<Number,Number> lineChart, double lowerBound, double upperBound){
        final NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        System.out.println(xAxis.getLowerBound());

        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);

    }

}
