package version2.prototype.indices.ModisNBARIndices;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GeneralListener;

/**
 * uses the same logic for ndwi5 and ndwi6
 * NDWI5 = (NIR-SWIR2)/(NIR+SWIR2)
 * NDWI6 = (NIR-SWIR)/(NIR+SWIR)
 * @author Isaiah Snell-Feikema
 */

public class GdalNDWI5Calculator extends IndicesFramework {
    private static final int NIR = 0;
    private static final int SWIR = 1;

    public GdalNDWI5Calculator(GeneralListener l) {
        super(l);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[NIR] == 32767 || values[SWIR] == 32767) {
            return -3.40282346639e+038;
        } else {
            return (values[NIR] - values[SWIR]) / (values[SWIR] + values[NIR]);
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

}