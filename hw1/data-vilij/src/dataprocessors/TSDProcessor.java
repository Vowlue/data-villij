package dataprocessors;

import data.DataSet;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Tooltip;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 */
final class TSDProcessor {

    static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character";

        InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'. " + NAME_ERROR_MSG, name));
        }
    }

    private Map<String, String> dataLabels;
    private Map<String, Point2D> dataPoints;
    private LineChart.Series<Number,Number> algSeries;
    private boolean seriesInserted;

    TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        algSeries = new LineChart.Series<>();
        seriesInserted = false;
    }

    Map<String, String> getDataLabels() {
        return dataLabels;
    }

    Map<String, Point2D> getDataPoints() {
        return dataPoints;
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    void processString(String tsdString) throws Exception {
        seriesInserted = false;
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try {
                      String   name  = checkedname(list.get(0));
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);
                  } catch (Exception e) {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                      hadAnError.set(true);
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }

    void processString(DataSet dataSet){
        dataLabels.clear();
        dataPoints.clear();
        for(String instance: dataSet.getLabels().keySet()){
            String label = dataSet.getLabels().get(instance);
            Point2D point = dataSet.getLocations().get(instance);
            dataLabels.put(instance, label);
            dataPoints.put(instance, point);
        }
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(LineChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            LineChart.Series<Number, Number> series = new LineChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                LineChart.Data<Number, Number> data = new LineChart.Data<>(point.getX(), point.getY());
                series.getData().add(data);
            });
            chart.getData().add(series);
            for (LineChart.Data<Number, Number> data: series.getData()) {
                Tooltip.install(data.getNode(), new Tooltip(label));
                data.getNode().getStyleClass().add("data");
            }
        }
        seriesInserted = !seriesInserted;
        algSeries = new LineChart.Series<>();
        chart.getData().add(algSeries);
        algSeries.getNode().setId("alg");
    }

    void removeAlgorithmSeries(LineChart<Number, Number> chart){
        if(seriesInserted){
            chart.getData().remove(algSeries);
            seriesInserted = !seriesInserted;
        }
        chart.getXAxis().setAutoRanging(true);
        chart.getYAxis().setAutoRanging(true);
    }

    void processList(List<Integer> output){
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        for(Point2D point: dataPoints.values()){
            double currentX = point.getX();
            if(currentX > maxX)
                maxX = currentX;
            if(currentX < minX)
                minX = currentX;
        }
        algSeries.getData().clear();
        algSeries.getData().add(new LineChart.Data<>(minX, calculateY(output, minX)));
        algSeries.getData().add(new LineChart.Data<>(maxX, calculateY(output, maxX)));
        for(LineChart.Data<Number,Number> data: algSeries.getData()){
            data.getNode().setVisible(false);
        }
    }
    private double calculateY(List<Integer> output, double x){
        return ((1.0)*output.get(2)-output.get(0)*x)/(1.0*output.get(1));
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
}
