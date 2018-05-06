package components;

import org.junit.Test;

import static org.junit.Assert.*;

public class RunConfigurationTest {

    private void confirmClassificationSettings(RunConfiguration.ConfigInfo config){
        int iterations = config.confirmIterations(""+config.getMaxIterations());
        config.setMaxIterations(iterations);
        config.setUpdateInterval(config.confirmInterval(""+config.getUpdateInterval(), iterations));
    }
    private void confirmClusteringSettings(RunConfiguration.ClusteringConfig config){
        confirmClassificationSettings(config);
        int labels = config.confirmLabels(""+config.getLabelNumber());
        config.setLabelNumber(labels);
    }
    @Test
    public void testGeneralClassificationCase() {
        RunConfiguration.ConfigInfo config = new RunConfiguration.ConfigInfo();
        config.setMaxIterations(1000);
        config.setUpdateInterval(100);
        config.setContinuous(true);
        confirmClassificationSettings(config);
        assertEquals(1000, config.getMaxIterations());
        assertEquals(100, config.getUpdateInterval());
        assertTrue(config.isContinuous());
    }
    /**
     *  This is a boundary value for max iterations because it makes no sense if max iterations is 0 or less. Thererfore, if the value is 0 or less,
     *  it will be defaulted to 1.
     */
    @Test
    public void testMaxIterations() {
        RunConfiguration.ConfigInfo config = new RunConfiguration.ConfigInfo();
        config.setMaxIterations(1);
        confirmClassificationSettings(config);
        assertEquals(1, config.getMaxIterations());
    }
    @Test
    public void testInvalidIterations(){
        RunConfiguration.ConfigInfo config = new RunConfiguration.ConfigInfo();
        config.setMaxIterations(Integer.MIN_VALUE);
        confirmClassificationSettings(config);
        assertEquals(0, config.getMaxIterations());
    }
    /**
     *  For update interval, it makes no sense if the update interval is 0 or less, so it will also be defaulted to 1 like the iterations.
     *  In addition, it makes no sense for the update interval to be higher than the max iterations; therefore, if this is the case,
     *  the update interval will be set equal to the max iteration.
     */
    @Test
    public void testUpdateInterval() {
        RunConfiguration.ConfigInfo config = new RunConfiguration.ConfigInfo();
        config.setMaxIterations(1000);
        config.setUpdateInterval(1000);
        confirmClassificationSettings(config);
        assertEquals(1000, config.getUpdateInterval());
    }
    @Test
    public void testInvalidInterval(){
        RunConfiguration.ConfigInfo config = new RunConfiguration.ConfigInfo();
        config.setMaxIterations(100);
        config.setMaxIterations(101);
        confirmClassificationSettings(config);
        assertEquals(100, config.getUpdateInterval());
    }
    @Test
    public void testGeneralClusteringCase() {
        RunConfiguration.ClusteringConfig config = new RunConfiguration.ClusteringConfig();
        config.setMaxIterations(1000);
        config.setUpdateInterval(100);
        config.setLabelNumber(3);
        config.setContinuous(true);
        confirmClusteringSettings(config);
        assertEquals(1000, config.getMaxIterations());
        assertEquals(100, config.getUpdateInterval());
        assertEquals(3, config.getLabelNumber());
        assertTrue(config.isContinuous());
    }
    /**
     *  As mentioned in the SRS, the clustering algorithms will only be able to handle between 2 and 4 (inclusive) labels.
     *  Therefore, if the number is outside the bounds, it will be set equal to the closest number within the bound.
     */
    @Test
    public void testlLabelNumber() {
        RunConfiguration.ClusteringConfig config = new RunConfiguration.ClusteringConfig();
        config.setLabelNumber(Integer.MAX_VALUE);
        confirmClusteringSettings(config);
        assertEquals(4, config.getLabelNumber());
    }
    @Test
    public void testInvalidLabelNumber(){
        RunConfiguration.ClusteringConfig config = new RunConfiguration.ClusteringConfig();
        config.setLabelNumber(0);
        confirmClusteringSettings(config);
        assertEquals(2, config.getLabelNumber());
    }
}