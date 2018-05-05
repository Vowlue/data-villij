package algorithms;

import algorithmbase.Classifier;
import data.DataSet;
import data.ListCollector;

import java.util.Arrays;
import java.util.Random;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;

    private ListCollector collector;

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
        return false;
    }

    public RandomClassifier(ListCollector collector, DataSet dataset,
                            int maxIterations,
                            int updateInterval) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.collector = collector;
    }

    @Override
    public void run() {
        for (int i = 1; i <= maxIterations; i++) {
            int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            double random = RAND.nextDouble();
            if(i % updateInterval == 0 || i > maxIterations * .6 && random < 0.05){
                collector.put(output);
                if(i > maxIterations * .6 && random < 0.05) break;
            }
        }
        collector.put(null);
    }
}
