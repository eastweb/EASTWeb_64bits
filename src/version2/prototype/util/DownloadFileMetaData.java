package version2.prototype.util;

import java.util.ArrayList;

/**
 *
 * @author michael.devos
 *
 */
public class DownloadFileMetaData {
    public final String dataName;
    public final String dataFilePath;
    public final int dataGroupID;
    public final int year;
    public final int day;
    public final ArrayList<DataFileMetaData> extraDownloads;

    /**
     * Creates a DownloadFileMetaData object initialized to the given values.
     *
     * @param dataFilePath  - full path to the data file
     * @param qcFilePath  - full path to the QC file associated to the data file
     * @param dateDirectoryPath  - path to the data file's date directory (e.g. ".../2015/001/")
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     */
    public DownloadFileMetaData(String dataName, String dataFilePath, int dataGroupID, int year, int day, ArrayList<DataFileMetaData> extraDownloads) {
        this.dataName = dataName;
        this.dataFilePath = dataFilePath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        this.extraDownloads = extraDownloads;
    }
}
