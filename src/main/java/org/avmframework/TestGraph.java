package org.avmframework;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class TestGraph extends Application {

    @Override public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");
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
        XYChart.Series series = new XYChart.Series();
        series.setName("Restart 1");
        //populating the series with data
        series.getData().add(new XYChart.Data(-5, 42));
        series.getData().add(new XYChart.Data(-4, 35));
        series.getData().add(new XYChart.Data(-3, 32));
        series.getData().add(new XYChart.Data(-2, 25));
        series.getData().add(new XYChart.Data(-1, 17));
        series.getData().add(new XYChart.Data(0, 0));


        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Restart 2");
        //populating the series with data
        series2.getData().add(new XYChart.Data(6, 36));
        series2.getData().add(new XYChart.Data(5, 22));
        series2.getData().add(new XYChart.Data(4, 19));
        series2.getData().add(new XYChart.Data(3, 15));
        series2.getData().add(new XYChart.Data(2, 10));
        series2.getData().add(new XYChart.Data(1, 4));
        series2.getData().add(new XYChart.Data(0, 0));

        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().addAll(series, series2);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
