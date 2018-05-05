package algorithms;

import algorithmbase.Clusterer;
import data.DataCollector;
import data.DataSet;

import java.util.Random;

public class RandomClusterer extends Clusterer {
    private static final Random RAND = new Random();
    private DataSet dataset;
    private final int maxIterations;
    private final int updateInterval;
    private DataCollector collector;
    public RandomClusterer(DataCollector collector, DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            int clusters) {
        super(clusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.collector = collector;
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return true;
    }

    @Override
    public void run() {
        int iteration = 0;
        while (iteration < maxIterations && tocontinue()) {
            iteration += updateInterval;
            for(String instance: dataset.getLabels().keySet()){
                dataset.updateLabel(instance, String.valueOf(RAND.nextInt(numberOfClusters)+1));
            }
            collector.put(dataset);
        }
        collector.put(null);
    }
}
