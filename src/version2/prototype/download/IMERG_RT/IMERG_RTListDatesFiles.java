package version2.prototype.download.IMERG_RT;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.ErrorLog;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.ListDatesFiles;

public class IMERG_RTListDatesFiles extends ListDatesFiles{

    private Map<DataDate, ArrayList<String>>  tempMapDatesToFiles = new HashMap<DataDate, ArrayList<String>>();

    // although exactly the same, the pattern fetched from the plugin file does not work
    private String fileNamePatternStr =
            "3B-HHR-L\\.MS\\.MRG\\.3IMERG\\.(\\d{8})-S233000-E235959\\.1410\\.V(\\d{2})[A-Z]\\.1day\\.tif((\\.gz)?)";

    public IMERG_RTListDatesFiles(DataDate date, DownloadMetaData data, ProjectInfoFile project) throws IOException
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
        FTPClient ftpC = null;

        try
        {
            ftpC = (FTPClient) ConnectionContext.getConnection(mData);
        }
        catch (ConnectException e)
        {
            ErrorLog.add(Config.getInstance(), "IMERG_RT", mData.name, "IMERG_RTListDatesFiles.ListDatesFiles: "
                    + "Can't connect to download website, please check your URL.", e);
            return null;
        }

        try
        {
            /* the location of the file for downloading follows the following rules
             * 1. if it is for the current year, search the files in the month directory under
             *            jsimpson.pps.eosdis.nasa.gov/data/imerg/gis
             * 2. if it is for the previous years, search the files in the yyyy/mm directory
             *
             * 3. added on 6/9/18:  the whole folder of Oct 2017 is misplaced in a subfolder V04 under the expected folder
             */
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH)+ 1;

            //int currentYear = 2016;
            //int currentMonth = 1;

            int startYear = sDate.getYear();
            int startMonth = sDate.getMonth();

            //int monthInPath = startMonth;

            String rootDir = mData.myFtp.rootDir;

            for (int year = startYear; year <= currentYear; year ++)
            {
                int monthS;
                if (year == startYear) {
                    monthS = startMonth;
                } else {
                    monthS = 1;
                }

                // access month folder
                if (year == currentYear)
                {
                    int monthEnd = currentMonth;
                    //System.out.println(monthInPath + " : " + monthEnd);

                    for (int month = monthS; month <= monthEnd; month++)
                    {
                        String monthDir = String.format("%s/%02d", rootDir, month);
                        //System.out.println("file: "+ monthDir);
                        AddFiles(ftpC, monthDir, month, year);
                    }
                }
                else   // not the current year. need to access folder yyyy/mm
                {
                    for (int month = monthS; month <= 12; month++)
                    {
                        String monthDir = String.format("%s/%04d/%02d", rootDir, year, month);
                        AddFiles(ftpC, monthDir, month, year);
                    }
                }
            }

            ftpC.disconnect();
            ftpC = null;
            return tempMapDatesToFiles;

        }
        catch (Exception e)
        {
            ErrorLog.add(Config.getInstance(), "IMERG_RT", mData.name, "IMERG_RTListDatesFiles.ListDatesFiles: FTP problem while creating list using FTP.", e);
            return null;
        }
    }

    private void AddFiles(FTPClient ftpC, String monthDir, int month, int year) throws IOException
    {
        if (!ftpC.changeWorkingDirectory(monthDir))
        {
            throw new IOException("Couldn't navigate to directory: " + monthDir);
        }

        FTPFile[] fFiles = ftpC.listFiles();

        /*
         *  6/9/18 by YL: to fix the folder problem of Oct 2017 as described as 3 above
         */
        while (fFiles.length < 2) {
            monthDir += "/" + fFiles[0].getName();
            if (!ftpC.changeWorkingDirectory(monthDir))
            {
                throw new IOException("Couldn't navigate to directory: " + monthDir);
            }

            fFiles = ftpC.listFiles();
        }


        for (FTPFile file : fFiles)
        {
            if(Thread.currentThread().isInterrupted()) {
                break;
            }

            ArrayList<String> fileNames = new ArrayList<String>();

            //filename pattern:
            //3B-HHR-L\.MS\.MRG\.3IMERG\.(\d{8})-S233000-E235959\.1410\.V(\d{2})[A-Z]\.1day\.tif((\.gz)?)
            /*  if (file.isFile() &&
                        mData.fileNamePattern.matcher(file.getName()).matches())
             */
            // modified on 6/8/18 since the most recent files listed online failed from isFile() check
            if (Pattern.compile(fileNamePatternStr).matcher(file.getName()).matches())
            {
                //System.out.println(file.getName());
                fileNames.add(file.getName());

                String[] str = file.getName().split("[.]");

                final int day = Integer.parseInt(str[4].substring(6, 8));
                // always get the last hour of the day -  23
                DataDate dataDate = new DataDate(23, day, month, year);
                if (dataDate.compareTo(sDate) >= 0)
                {
                    //System.out.println(file.getName());
                    tempMapDatesToFiles.put(dataDate, fileNames);
                }
            } else {
                continue;
            }
        }
    }
}
