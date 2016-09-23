package version2.prototype.download.IMERG;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.ErrorLog;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.ListDatesFiles;

public class IMERGListDatesFiles extends ListDatesFiles{

    public IMERGListDatesFiles(DataDate date, DownloadMetaData data, ProjectInfoFile project) throws IOException
    {
        super(date, data, project);
    }


    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP()
    {
        return null;
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP()
    {
        Map<DataDate, ArrayList<String>>  tempMapDatesToFiles = new HashMap<DataDate, ArrayList<String>>();

        FTPClient ftpC = null;

        try
        {
            ftpC = (FTPClient) ConnectionContext.getConnection(mData);
        }
        catch (ConnectException e)
        {
            ErrorLog.add(Config.getInstance(), "IMERG", mData.name, "IMERG_RTListDatesFiles.ListDatesFiles: "
                    + "Can't connect to download website, please check your URL.", e);
            return null;
        }

        try
        {
            LocalDate currentDate = new LocalDate();
            //LocalDate currentDate = new LocalDate(2014, 6,30);
            LocalDate startDate = new LocalDate(sDate.getYear(), sDate.getMonth(), sDate.getDay());

            for (LocalDate d = startDate; d.isBefore(currentDate); d = d.plusDays(1))
            {
                // format the file directory
                // ftp://arthurhou.pps.eosdis.nasa.gov/gpmdata/yyyy/mm/dd

                String fileDir = String.format("%s/%04d/%02d/%02d/gis", mData.myFtp.rootDir,
                        d.getYear(), d.getMonthOfYear(), d.getDayOfMonth());

                if (!ftpC.changeWorkingDirectory(fileDir))
                {
                    continue;
                }

                for (FTPFile file : ftpC.listFiles())
                {
                    if(Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    //System.out.println(mData.fileNamePattern.matcher(file.getName()).matches());
                    ArrayList<String> fileNames = new ArrayList<String>();

                    //filename pattern:
                    //3B-DAY-GIS\.MS\.MRG\.3IMERG\.(\d{8})-S000000-E235959\.(\d{4})\.V03D\.tif
                    //if (file.isFile() &&
                    if (mData.fileNamePattern.matcher(file.getName()).matches())
                    {
                        fileNames.add(file.getName());

                        // always get the last hour of the day -  23
                        DataDate dataDate = new DataDate(23, d.getDayOfMonth(), d.getMonthOfYear(), d.getYear());

                        tempMapDatesToFiles.put(dataDate, fileNames);
                    } else {
                        continue;
                    }
                }
            }

            ftpC.disconnect();
            ftpC = null;
            return tempMapDatesToFiles;
        }
        catch (Exception e)
        {
            ErrorLog.add(Config.getInstance(), "IMERG", mData.name, "IMERGListDatesFiles.ListDatesFilesFTP problem while creating list using FTP.", e);
            return null;
        }
    }

}
