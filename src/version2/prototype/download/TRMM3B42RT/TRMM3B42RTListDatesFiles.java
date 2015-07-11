package version2.prototype.download.TRMM3B42RT;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.DownloadUtils;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.util.ParallelUtils.Parallel;

public class TRMM3B42RTListDatesFiles extends ListDatesFiles
{

    public TRMM3B42RTListDatesFiles(DataDate date, DownloadMetaData data) throws IOException
    {
        super(date, data);
    }


    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP()
    {
        return null;
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP()
    {
        final Pattern yearDirPattern = Pattern.compile("\\d{4}");

        FTPClient ftpC = null;

        try
        {
            ftpC = (FTPClient) ConnectionContext.getConnection(mData);
        }
        catch (ConnectException e)
        {
            System.out.println("Can't connect to download website, please check your URL.");
            return null;
        }

        String mRoot = mData.myFtp.rootDir;
        try
        {
            if (!ftpC.changeWorkingDirectory(mRoot))
            {
                throw new IOException("Couldn't navigate to directory: " + mRoot);
            }


            final List<DataDate> list = new ArrayList<DataDate>();

            mapDatesFiles =  new HashMap<DataDate, ArrayList<String>>();

            // List years
            for (FTPFile yearFile : ftpC.listFiles())
            {
                // Skip non-directory, non-year entries
                if (!yearFile.isDirectory()
                        || !yearDirPattern.matcher(yearFile.getName())
                        .matches())
                {
                    continue;
                }

                int year = Integer.parseInt(yearFile.getName());
                if (year < sDate.getYear())
                {
                    continue;
                }

                // List days in this year
                String yearDirectory =
                        String.format("%s/%s", mRoot, yearFile.getName());

                if (!ftpC.changeWorkingDirectory(yearDirectory))
                {
                    throw new IOException(
                            "Couldn't navigate to directory: " + yearDirectory);
                }

                for (FTPFile file : ftpC.listFiles())
                {
                    Pattern tPattern =
                            Pattern.compile("3B42RT_daily\\.(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.bin");

                    if (file.isFile() &&
                            tPattern.matcher(file.getName()).matches())
                    {
                        /* pattern of TRMM 3B42RT
                         * {productname}.%y4.%m2.%d2.7.bin
                         */
                        ArrayList<String> fileNames = new ArrayList<String>();
                        fileNames.add(file.getName());

                        String[] strings = file.getName().split("[.]");
                        final int month = Integer.parseInt(strings[2]);
                        final int day = Integer.parseInt(strings[3]);
                        DataDate dataDate = new DataDate(day, month, year);
                        if (dataDate.compareTo(sDate) >= 0)
                        {
                            list.add(dataDate);

                            mapDatesFiles.put(dataDate, fileNames);
                        }
                    }
                }
            }

            ftpC.disconnect();
            return mapDatesFiles;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

}

