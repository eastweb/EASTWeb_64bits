package version2.prototype.indices.NldasForcing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

public class NldasForcingFreezingDegreeDays extends IndicesFramework {

    public NldasForcingFreezingDegreeDays() { }

    @Override
    public void calculate() throws Exception
    {
        for(File input : mInputFiles)
        {
            if(input.getName().contains("FreezingDegreeDays"))
            {
                FileUtils.forceMkdir(mOutputFile.getParentFile());
                if(!mOutputFile.exists()) {
                    Files.copy(input.toPath(), mOutputFile.toPath());
                }
                //                if(!input.renameTo(mOutputFile)) {
                //                    CopyFile(input, mOutputFile);
                //                }
                break;
            }
        }
    }

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        double cdd = -9999;

        for (double value : values)
        {
            // Fill value == 9999
            if(value != 9999)
            {
                if(cdd == -9999) {
                    cdd = 0;
                }
                cdd = value;
            }
        }

        if(cdd == -9999){
            //            return -3.4028234663852886E38;
            return GdalUtils.NoValue;
        }

        return cdd;
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

    public static void CopyFile(File src, File dest)
    {

        InputStream inStream = null;
        OutputStream outStream = null;

        try{
            inStream = new FileInputStream(src);
            outStream = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0){

                outStream.write(buffer, 0, length);

            }

            inStream.close();
            outStream.close();

            //delete the original file
            src.delete();

            System.out.println("File is copied successful!");

        }catch(IOException e){
            ErrorLog.add(Config.getInstance(), "NldasForcingFreezingDegreeDays.CopyFile problem with file stream opperations.", e);
        }
    }
}
