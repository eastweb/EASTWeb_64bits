package version2.prototype.indices.NldasNOAH;

import java.io.File;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

//import java.io.File;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

/*
 * Degrees Celsius = Kelvin - 273.15
 * originalValue - 273.15 = Degrees Celsius
 */

public class NldasNOAHMeanDailySoilTemp40_100 extends IndicesFramework{

    private final static int INPUT = 0;

    public NldasNOAHMeanDailySoilTemp40_100() { }

    @Override
    public void calculate() throws Exception {
        GdalUtils.register();

        synchronized (GdalUtils.lockObject) {

            Dataset[] inputs = new Dataset[1];

            for(File inputFile : mInputFiles)
            {
                if(inputFile.getName().contains("Band21"))
                {
                    inputs[0] = gdal.Open(inputFile.getAbsolutePath());
                }
            }

            Dataset outputDS = createOutput(inputs);
            process(inputs, outputDS);

            for (int i = 1; i <= outputDS.GetRasterCount(); i++) {
                Band band = outputDS.GetRasterBand(i);

                band.SetNoDataValue(OUTPUT_NODATA);
                band.ComputeStatistics(false);
            }

            for (Dataset input : inputs) {
                input.delete();
            }

            outputDS.delete();
        }
    }

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        // TODO Auto-generated method stub

        if(values[INPUT] == GdalUtils.NoValue)
        {
            //            return -3.4028234663852886E38;
            return GdalUtils.NoValue;
        }
        else
        {
            //return calculated value
            return values[INPUT] - 273.15;
        }
    }

    @Override
    protected String className() {
        // TODO Auto-generated method stub

        return getClass().getName();
    }
}
