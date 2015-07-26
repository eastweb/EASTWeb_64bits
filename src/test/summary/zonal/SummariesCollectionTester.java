package test.summary.zonal;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import version2.prototype.summary.zonal.SummariesCollection;
import version2.prototype.summary.zonal.SummaryNameResultPair;

public class SummariesCollectionTester {

    @Test
    public final void testSummariesCollection() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        SummariesCollection col1 = new SummariesCollection(new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev")));
        SummariesCollection col2 = new SummariesCollection(new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev")));
        testData1(col1);
        testData2(col2);

        ArrayList<SummaryNameResultPair> results = col1.getResults();
        for(SummaryNameResultPair pair : results){
            System.out.println(pair.getSimpleName() + ": " + pair.getResult().toString());
        }
        System.out.println();

        results = col2.getResults();
        for(SummaryNameResultPair pair : results){
            System.out.println(pair.getSimpleName() + ": " + pair.getResult().toString());
        }
        System.out.println();

        col1 = null;
        col2 = null;
    }

    private void testData1(SummariesCollection col) {
        col.put(0, 1.0);
        col.put(1, 2.0);
        col.put(2, 3.0);
        col.put(3, 4.0);
        col.put(4, 5.0);
        col.put(5, 6.0);

        col.put(0, 1.0);
        col.put(1, 2.0);
        col.put(2, 3.0);
        col.put(3, 4.0);
        col.put(4, 5.0);
        col.put(5, 6.0);

        col.put(0, 1.0);
        col.put(1, 2.0);
        col.put(2, 3.0);
        col.put(3, 4.0);
        col.put(4, 5.0);
        col.put(5, 6.0);
    }

    private static void testData2(SummariesCollection col) {
        col.put(0, 1.0);
        col.put(1, 1.0);
        col.put(2, 1.0);
        col.put(3, 1.0);
        col.put(4, 1.0);
        col.put(5, 1.0);

        col.put(0, 2.0);
        col.put(1, 2.0);
        col.put(2, 2.0);
        col.put(3, 2.0);
        col.put(4, 2.0);
        col.put(5, 2.0);

        col.put(0, 3.0);
        col.put(1, 3.0);
        col.put(2, 3.0);
        col.put(3, 3.0);
        col.put(4, 3.0);
        col.put(5, 3.0);

        col.put(0, 4.0);
    }
}
