package version2.prototype.indices.ModisLST;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

public class ModisLSTDay extends IndicesFramework{

    private final int INPUT;

    public ModisLSTDay()
    {
        int tempINPUT = -1;

        for(int i=0; i < mInputFiles.length; i++)
        {
            if(mInputFiles[i].getName().toLowerCase().contains(new String("day")))
            {
                tempINPUT = i;
            }

            if(tempINPUT > -1) {
                break;
            }
        }

        INPUT = tempINPUT;
    }

    /**
     * Valid input value range: 7500 to 65535
     * Valid output value range: all values
     */
    @Override
    protected double calculatePixelValue(double[] values) {
        if (values[INPUT] < 7500 || values[INPUT] > 65535 || values[INPUT] == GdalUtils.NO_VALUE) {
            //        if(values[INPUT] == GdalUtils.NoValue) {
            //            return -3.4028234663852886E38;
            return GdalUtils.NO_DATA;
        } else {
            return (values[INPUT] * 0.02) - 273.16;
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }
}