package com.tbest.simplegraphapplication;

import chariot.Client;
import chariot.model.RatingHistory;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class HelloController {
    @FXML
    private ScatterChart<String, Integer> chart;

    @FXML
    private TextField username;

    @FXML
    private Spinner<?> minRatingDays;


    @FXML
    protected void onSubmitButtonClick() {
        // Username to lower case, since with the lichess api the ID is always lower case
        UpdateScatterChart(chart, username.getText().toLowerCase());
    }

    private void UpdateScatterChart(ScatterChart<String, Integer> scatterChart, String user) {
        var client = Client.basic();
        if (client.users().byId(user).isPresent() == false) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "User not found");
            alert.show();
            return;
        }
        String displayName = client.users().byId(user).get().username();
        var Result = client.users().ratingHistoryById(user);

        Consumer<RatingHistory> addPoint = RatingHistory -> {
            //Prepare XYChart.Series objects by setting data
            XYChart.Series<String, Integer> series = new XYChart.Series<>();
            series.setName(displayName + " " + RatingHistory.name());
            for (chariot.model.RatingHistory.DateResult dateResult : RatingHistory.results()) {
                //    System.out.println(dateResult.date() + " " + dateResult.points());
                series.getData().add(new XYChart.Data<>(dateResult.date().toString(), dateResult.points()));
            }

            //Only include categorises with minRatingDays or more points
            System.out.println(RatingHistory.name() + " : " + series.getData().size());
            if (series.getData().size() > (Integer) minRatingDays.getValue()) {
                //Setting the data to scatter chart
                scatterChart.getData().addAll(series);
            }
        };
        Result.stream().forEach(addPoint);

        //Update the chart title
        if (scatterChart.getTitle() != null) {
            scatterChart.setTitle(scatterChart.getTitle() + " " + displayName);
        } else {
            scatterChart.setTitle(displayName);
        }
    }

    @FXML
    private void onClearButtonClick() {
        chart.getData().clear();
        chart.setTitle(null);
        System.out.println("clear clicked");
    }
}