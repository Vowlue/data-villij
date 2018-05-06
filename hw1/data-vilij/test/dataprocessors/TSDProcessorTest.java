package dataprocessors;

import javafx.geometry.Point2D;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class TSDProcessorTest {
    @Test
    public void createInstanceNormal() {
        TSDProcessor processor = new TSDProcessor();
        try {
            processor.processString("@a\ta\t1,1");
        } catch (Exception ignored) { }
        assertEquals(new HashMap<String, String>(){{put("@a","a");}}, processor.getDataLabels());
        assertEquals(new HashMap<String, Point2D>(){{put("@a",new Point2D(1,1));}}, processor.getDataPoints());
    }
    /**
     * In this case, these are boundary values because they are using points that are slightly lower than the maximum containable values
     * for a double. Any higher would mean that the numbers inputted are no longer doubles.
     */
    @Test
    public void createInstanceBoundaryHigh() {
        TSDProcessor processor = new TSDProcessor();
        try {
            processor.processString("@a\ta\t" + Double.MAX_VALUE + "," + Double.MAX_VALUE);
        } catch (Exception ignored) { }
        assertEquals(new HashMap<String, String>(){{put("@a","a");}}, processor.getDataLabels());
        assertEquals(new HashMap<String, Point2D>(){{put("@a",new Point2D(Double.MAX_VALUE,Double.MAX_VALUE));}}, processor.getDataPoints());
    }
    @Test
    public void createInstanceBoundaryLow(){
        TSDProcessor processor = new TSDProcessor();
        try {
            processor.processString("@a\ta\t" + Double.MIN_VALUE + "," + Double.MIN_VALUE);
        } catch (Exception ignored) { }
        assertEquals(new HashMap<String, String>(){{put("@a","a");}}, processor.getDataLabels());
        assertEquals(new HashMap<String, Point2D>(){{put("@a",new Point2D(Double.MIN_VALUE,Double.MIN_VALUE));}}, processor.getDataPoints());
    }
    @Test(expected = Exception.class)
    public void createInstanceWithError() throws Exception {
        TSDProcessor processor = new TSDProcessor();
        processor.processString("");

    }
}