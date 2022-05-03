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
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;
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
    private HashMap<String, String> legendStyle;

    public void initialize() {
        legendStyle = new HashMap<>();
        numberOfUsers = 0;
        ObservableList<String> variantsList = FXCollections.observableArrayList(//
                "Bullet", "Blitz", "Rapid", "Classical", //
                "Correspondence", "Crazyhouse", "Chess960", "King of the Hill", //
                "Three-check", "Antichess", "Atomic", "Horde", "Racing Kings", "Puzzles");
        SpinnerValueFactory.ListSpinnerValueFactory<String> valueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(variantsList);
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
        username.requestFocus();
        username.selectAll();
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


        final String displayName = client.users().byId(user).get().username() + " ";
        final var Result = client.users().ratingHistoryById(user);

        Consumer<RatingHistory> addPoint = RatingHistory -> {
            //Only include the selected variant
            if (RatingHistory.name().equals(variantSpinner.getValue())) {
                //Prepare XYChart.Series objects by setting data
                XYChart.Series<Long, Integer> series = new XYChart.Series<>();
                series.setName(displayName + RatingHistory.name());

                //Check if we have already added them
                if (legendStyle.get(series.getName() + "colorPlayer") != null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, series.getName() + " is already added");
                    alert.show();
                    return;
                }


                for (chariot.model.RatingHistory.DateResult dateResult : RatingHistory.results()) {
                    series.getData().add(new XYChart.Data<>(dateResult.date().toEpochSecond(LocalTime.MIN, ZoneOffset.UTC), dateResult.points()));
                }


                //Setting the data to scatter chart
                lineChart.getData().add(series);

                //Set color for series, nodes and legend.
                legendStyle.put(series.getName() + "colorPlayer", getPlayerColor());
                legendStyle.put(series.getName() + "colorVariant", getVariantColor(RatingHistory.name()));


                System.out.println(RatingHistory.name() + " : " + series.getData().size());
                System.out.println("colorPlayer" + legendStyle.get(series.getName() + "colorPlayer"));
                System.out.println("colorVariant" + legendStyle.get(series.getName() + "colorVariant"));


                //Style line nodes
                int index = 0;
                while (index < series.getData().size()) {
                    XYChart.Data<Long, Integer> dataPoint = series.getData().get(index);
                    Node lineSymbol = dataPoint.getNode().lookup(".chart-line-symbol");
                    lineSymbol.setStyle("-fx-background-radius: 0px;" +
                            " -fx-background-color: " + legendStyle.get(series.getName() + "colorPlayer"));
                    index++;
                }

                //Color lines
                String seriesStyleString = "-fx-stroke-width: 1; -fx-stroke: " + legendStyle.get(series.getName() + "colorVariant");
                series.getNode().setStyle(seriesStyleString);

                Set<Node> items = chart.lookupAll("Label.chart-legend-item");

                for (Node item : items) {
                    Label label = (Label) item;
                    final Rectangle rectangle = new Rectangle(10, 10, Color.web(legendStyle.get(label.getText() + "colorPlayer")));
                    final Glow niceEffect = new Glow();
                    niceEffect.setInput(new Reflection());
                    rectangle.setEffect(niceEffect);
                    label.setTextFill(Color.web(legendStyle.get(label.getText() + "colorVariant")));
                    label.setGraphic(rectangle);
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
        //We found the user, so increment the user count
        numberOfUsers++;
    }

    @FXML
    private void onClearButtonClick() {
        chart.getData().clear();
        chart.setTitle(null);
        numberOfUsers = 0;
        legendStyle.clear();
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
            case 0 -> "AQUAMARINE";
            case 1 -> "LAWNGREEN";
            case 2 -> "LIGHTBLUE";
            case 3 -> "DARKMAGENTA";
            case 4 -> "LIGHTSALMON";
            case 5 -> "LIGHTPINK";

            default -> "GREY";
        };
    }
}
