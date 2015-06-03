package version2.prototype.projection;

import java.io.File;
import java.io.IOException;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.Transformer;
import org.gdal.gdal.gdal;

import version2.prototype.util.GdalUtils;

import org.apache.commons.io.FileUtils;

public class Mask {
    //locations for the input files. for this step, will only use inputFolders[0]
    private String[] inputFolders;
    //location for the output file
    private String outputFolder;
    private File inputFolder;
    // the files in the input folder for composition
    private File [] inputFiles;
    // mask file
    private File maskFile;

    public Mask(ProcessData data) {
        inputFolders = data.getInputFolders();
        outputFolder = data.getOutputFolder();

        //check if there are more than one input file in the given folder
        inputFolder = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder.listFiles();
        assert (listOfFiles.length >= 1);
        //set the input files
        inputFiles = listOfFiles;

        maskFile = new File(data.getMaskfile());
    }

    // run method for the scheduler
    public void run(){
        try {
            masking();
            // remove the input folder
            FileUtils.deleteDirectory(inputFolder);
        } catch (IOException e) {
            // TODO :write into log
            e.printStackTrace();
        }
    }

    private void masking() throws IOException{
        synchronized (GdalUtils.lockObject) {
            for (File mInput : inputFiles){
                Dataset mInputDS = gdal.Open(mInput.getPath());
                Dataset mMaskDS = gdal.Open(maskFile.getPath());
                String filename = mInput.getName();
                File mOutput = new File(outputFolder,filename);

                Dataset mOutputDS = mInputDS.GetDriver().CreateCopy(mOutput.getPath(), mInputDS); // FIXME: create 32bit new raster instead?

                assert(mInputDS.GetRasterCount() == 1);
                assert(mMaskDS.GetRasterCount() == 1);

                int rasterX = 0;
                int rasterY = 0;
                int rasterWidth = mInputDS.GetRasterXSize();
                int rasterHeight = mInputDS.GetRasterYSize();
                int rasterRight = rasterWidth;
                int rasterBottom = rasterHeight;
                Transformer transformer = new Transformer(mMaskDS, mInputDS, null);
                double[] point = new double[] {-0.5, -0.5, 0}; // Location of corner of first zone raster pixel

                transformer.TransformPoint(0, point);

                int maskX = (int) Math.round(point[0]);
                int maskY = (int) Math.round(point[1]);
                int maskWidth = mMaskDS.GetRasterXSize();
                int maskHeight = mMaskDS.GetRasterYSize();
                int maskRight = maskX + maskWidth;
                int maskBottom = maskY + maskHeight;

                int intersectX = Math.max(rasterX, maskX);
                int intersectY = Math.max(rasterY, maskY);
                int intersectRight = Math.min(rasterRight, maskRight);
                int intersectBottom = Math.min(rasterBottom, maskBottom);
                int intersectWidth = intersectRight - intersectX;
                int intersectHeight = intersectBottom - intersectY;

                double[] output = new double[intersectWidth];
                double[] mask = new double[intersectWidth];

                for (int y=0; y<intersectHeight; y++) {
                    mInputDS.GetRasterBand(1).ReadRaster(intersectX, intersectY + y, intersectWidth, 1, output);
                    mMaskDS.GetRasterBand(1).ReadRaster(intersectX - maskX, intersectY - maskY + y, intersectWidth, 1, mask);

                    for (int x=0; x<intersectWidth; x++) {
                        if (mask[x] == 0) {
                            output[x] = 32767; // FIXME: variable no data values
                        }
                    }

                    mOutputDS.GetRasterBand(1).WriteRaster(intersectX, intersectY + y, intersectWidth, 1, output);
                }

                mOutputDS.GetRasterBand(1).SetNoDataValue(32767);
                mOutputDS.GetRasterBand(1).ComputeStatistics(false);
                mInputDS.delete();
                mMaskDS.delete();
                mOutputDS.delete();
            }
        }
    }
}

