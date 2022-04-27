package com.tbest.simplegraphapplication;

import chariot.Client;
import chariot.model.RatingHistory;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class HelloController {
    @FXML
    private LineChart<Long, Integer> chart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private TextField username;

    @FXML
    private Spinner<?> minRatingDays;

    public void initialize() {
        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number number) {
                long epoch = number.longValue();

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
                ZonedDateTime zonedDateTime = Instant.ofEpochSecond(epoch).atZone(ZoneId.of("Europe/Paris"));
                return zonedDateTime.format(dateTimeFormatter);
            }

            @Override
            public Long fromString(String s) {

                return 0L;
            }

        });
    }

    @FXML
    protected void onSubmitButtonClick() {
        // Username to lower case, since with the lichess api the ID is always lower case
        UpdateLineChart(chart, username.getText().toLowerCase());
    }

    //We are requesting from the API, so only want to send one request at a time
    private synchronized void UpdateLineChart(LineChart<Long, Integer> lineChart, String user) {
        System.out.println("Requesting " + user);
        final var client = Client.basic();
        if (!client.users().byId(user).isPresent()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "User not found");
            alert.show();
            return;
        }
        final String displayName = client.users().byId(user).get().username();
        final var Result = client.users().ratingHistoryById(user);

        Consumer<RatingHistory> addPoint = RatingHistory -> {
            //Prepare XYChart.Series objects by setting data
            XYChart.Series<Long, Integer> series = new XYChart.Series<>();
            series.setName(displayName + " " + RatingHistory.name());
            for (chariot.model.RatingHistory.DateResult dateResult : RatingHistory.results()) {
                series.getData().add(new XYChart.Data<>(dateResult.date().toEpochSecond(LocalTime.MIN, ZoneOffset.UTC), dateResult.points()));
            }

            //Only include categorises with minRatingDays or more points
            if (series.getData().size() > (Integer) minRatingDays.getValue()) {
                System.out.println(RatingHistory.name() + " : " + series.getData().size());


                //Setting the data to scatter chart
                lineChart.getData().add(series);


                //Set color for series, nodes and legend.
                final String color = getColor(RatingHistory.name());
                System.out.println(color);


                //Style nodes
                int index = 0;
                while (index < series.getData().size()) {
                    XYChart.Data<Long, Integer> dataPoint = series.getData().get(index);
                    Node lineSymbol = dataPoint.getNode().lookup(".chart-line-symbol");
                    lineSymbol.setStyle("-fx-background-radius: 10px; -fx-background-color: " + color);
//-fx-shape: M40,60 C42,48 44,30 25,32 Z;
                    index++;
                }
                //Color lines
                StringBuilder seriesStyleString = new StringBuilder("-fx-stroke-width: 3; -fx-stroke: " + color);
                series.getNode().setStyle(seriesStyleString.toString());

                //Color chart legend
                for (Node n : chart.getChildrenUnmodifiable()) {
                    if (n.getClass().getSimpleName() == "Legend") {
                        // n.setStyle("-fx-background-color: #0000ff, " + color);
                    }

                }

            }
        };
        Result.stream().forEach(addPoint);

        //Update the chart title
        if (lineChart.getTitle() != null) {
            lineChart.setTitle(lineChart.getTitle() + ", " + displayName);
        } else {
            lineChart.setTitle(displayName);
        }
    }

    @FXML
    private void onClearButtonClick() {
        chart.getData().clear();
        chart.setTitle(null);
    }

    @FXML
    private void onCloseButtonClick() {
        System.exit(0);
    }

    private String getColor(String variant) {
        return switch (variant) {
            case "Bullet" -> "RED";
            case "Blitz" -> "BLUE";
            case "Rapid" -> "LIGHTSTEELBLUE";
            case "Classical" -> "GREEN";
            case "Chess960" -> "DEEPPINK";
            case "Atomic" -> "FIREBRICK";
            case "Racing Kings" -> "GOLD";
            case "Crazyhouse" -> "BLUEVIOLET";
            case "Puzzles" -> "LIGHTGOLDENRODYELLOW";

            default -> "GRAY";
        };
    }

}
