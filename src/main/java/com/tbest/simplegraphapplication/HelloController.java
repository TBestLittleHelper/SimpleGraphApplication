package com.tbest.simplegraphapplication;

import chariot.Client;
import chariot.model.RatingHistory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
    private Spinner<String> variantSpinner;

    private int numberOfUsers;

    public void initialize() {
        numberOfUsers = 0;
        ObservableList<String> variantsList = FXCollections.observableArrayList(//
                "Bullet", "Blitz", "Rapid", "Classical", //
                "Correspondence", "Crazyhouse", "Chess960", "King of the Hill", //
                "Three-check", "Antichess", "Atomic", "Horde", "Racing Kings", "Puzzles");
        SpinnerValueFactory.ListSpinnerValueFactory<String> valueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(variantsList);
        valueFactory.setValue("Blitz");
        valueFactory.wrapAroundProperty();
        variantSpinner.setValueFactory(valueFactory);


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
            Alert alert = new Alert(Alert.AlertType.INFORMATION, user + " not found");
            alert.show();
            return;
        }
        if (client.users().byId(user).get().disabled()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, user + " is disabled");
            alert.show();
            return;
        }

        //We found the user, so increment
        numberOfUsers++;

        final String displayName = client.users().byId(user).get().username();
        final var Result = client.users().ratingHistoryById(user);

        Consumer<RatingHistory> addPoint = RatingHistory -> {
            //Prepare XYChart.Series objects by setting data
            XYChart.Series<Long, Integer> series = new XYChart.Series<>();
            series.setName(displayName + " " + RatingHistory.name());
            for (chariot.model.RatingHistory.DateResult dateResult : RatingHistory.results()) {
                series.getData().add(new XYChart.Data<>(dateResult.date().toEpochSecond(LocalTime.MIN, ZoneOffset.UTC), dateResult.points()));
            }

            //Only include the selected variant
            if (RatingHistory.name().equals(variantSpinner.getValue())) {

                //Setting the data to scatter chart
                lineChart.getData().add(series);

                //Set color for series, nodes and legend.
                final String variantColor = getVariantColor(RatingHistory.name());
                final String colorPlayer = getPlayerColor();

                System.out.println(RatingHistory.name() + " : " + series.getData().size() + variantSpinner.getValue());
                System.out.println(displayName + " " + colorPlayer + " " + variantColor);

                //Style line nodes
                int index = 0;
                while (index < series.getData().size()) {
                    XYChart.Data<Long, Integer> dataPoint = series.getData().get(index);
                    Node lineSymbol = dataPoint.getNode().lookup(".chart-line-symbol");
                    lineSymbol.setStyle("-fx-background-radius: 1px;" +
                            " -fx-background-color: " + colorPlayer);
                    index++;
                }
                //Color lines
                StringBuilder seriesStyleString = new StringBuilder("-fx-stroke-width: 1; -fx-stroke: " + variantColor);
                series.getNode().setStyle(seriesStyleString.toString());

                //Color chart legend
                for (Node n : chart.getChildrenUnmodifiable()) {
                    if (n.getClass().getSimpleName().equals("Legend")) {
                        n.setStyle("-fx-background-color:" + colorPlayer);
                        //-fx-create-symbols: M 100 100L 300 100L 200 300Z;
                    }
                }
            }
        };
        Result.stream().forEach(addPoint);

        //Update the chart title
        if (lineChart.getTitle() != null) {
            lineChart.setTitle(lineChart.getTitle() + ", " + displayName + " " + variantSpinner.getValue());
        } else {
            lineChart.setTitle(displayName + " " + variantSpinner.getValue());
        }
    }

    @FXML
    private void onClearButtonClick() {
        chart.getData().clear();
        chart.setTitle(null);
        numberOfUsers = 0;
    }

    @FXML
    private void onCloseButtonClick() {
        System.exit(0);
    }

    private String getVariantColor(String variant) {
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

    private String getPlayerColor() {
        return switch (numberOfUsers) {
            case 1 -> "LAWNGREEN";
            case 2 -> "LIGHTBLUE";
            case 3 -> "DARKMAGENTA";
            case 4 -> "LIGHTSALMON";
            case 5 -> "LIGHTPINK";

            default -> "GREY";
        };
    }
}
