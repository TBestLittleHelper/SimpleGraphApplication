package com.tbest.simplegraphapplication;

import chariot.Client;
import chariot.model.RatingHistory;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class HelloController {
    @FXML
    ScatterChart chart;
    @FXML
    private Label welcomeText;
    @FXML
    private TextField username;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onSubmitButtonClick() {
        ScatterChart(chart, username.getText().toLowerCase());

        System.out.println(username.getText().toLowerCase());
    }

    private ScatterChart<String, Number> ScatterChart(ScatterChart scatterChart, String user) {

        int minRatingDays = 60;
        var client = Client.basic();
        var Result = client.users().ratingHistoryById(user);

        //Defining the axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");

        NumberAxis yAxis = new NumberAxis(600, 3000, 100);
        yAxis.setLabel("Points");


        Consumer<RatingHistory> addPoint = RatingHistory -> {
            //Prepare XYChart.Series objects by setting data
            XYChart.Series series = new XYChart.Series();
            series.setName(RatingHistory.name());
            for (chariot.model.RatingHistory.DateResult dateResult : RatingHistory.results()) {
                //    System.out.println(dateResult.date() + " " + dateResult.points());
                series.getData().add(new XYChart.Data(dateResult.date().toString(), dateResult.points()));

            }
            //Only include categorises with minRatingDays or more points
            System.out.println(RatingHistory.name() + " " + series.getData().size());
            if (series.getData().size() > minRatingDays) {
                //Setting the data to scatter chart
                scatterChart.getData().addAll(series);
            }
        };
        Result.stream().forEach(addPoint);
        //Styling the chart
        scatterChart.setTitle(user.toUpperCase());
        scatterChart.autosize();
        scatterChart.setVerticalGridLinesVisible(false);
        final DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2);
        shadow.setColor(Color.GREY);
        scatterChart.setEffect(shadow);

        return scatterChart;
    }
}