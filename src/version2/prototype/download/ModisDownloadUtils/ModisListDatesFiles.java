package version2.prototype.download.ModisDownloadUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.ErrorLog;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.download.DownloadUtils;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.util.ParallelUtils.Parallel;

/*
 * @Author: Chris Plucker, Fangyu Zhang
 * @Author: Yi Liu
 */

public class ModisListDatesFiles extends ListDatesFiles
{
    public ModisListDatesFiles(DataDate startDate, DownloadMetaData data, ProjectInfoFile project) throws IOException
    {
        super(startDate, data, project);
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP() {
        return null;
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP()
    {
        mapDatesFiles =  new HashMap<DataDate, ArrayList<String>>();

        ArrayList<String> modisTiles = new ArrayList<String>();
        for(ProjectInfoPlugin pluginInfo : mProject.GetPlugins())
        {
            if(pluginInfo.GetName().equals(mData.Title))
            {
                modisTiles = pluginInfo.GetModisTiles();
                break;
            }
        }


        final String mHostURL = mData.myHttp.url;

        //final List<DataDate> desiredDates = new ArrayList<DataDate>();
        //        final ReentrantLock lock = new ReentrantLock();

        try
        {
            ByteArrayOutputStream folderOutStream = new ByteArrayOutputStream();
            DownloadUtils.downloadToStream(new URL(mHostURL), folderOutStream);

            List<String> availableDates = Arrays.asList(folderOutStream.toString().split("[\\r\\n]+"));
            //            for(String param : availableDates) {
            List<Future<HashMap<DataDate, ArrayList<String>>>> futures = Parallel.ForEach(availableDates,
                    new Parallel.Operation<String, HashMap<DataDate, ArrayList<String>>>() {
                @Override
                public HashMap<DataDate, ArrayList<String>> perform(String param)
                {
                    String datePattern = "((19|20)\\d\\d).(0?[1-9]|1[012]).(0?[1-9]|[12][0-9]|3[01])/";
                    Pattern pattern = Pattern.compile(datePattern);
                    Matcher matcher = pattern.matcher(param);
                    HashMap<DataDate, ArrayList<String>> result = new HashMap<DataDate, ArrayList<String>>();

                    if(matcher.find())
                    {
                        try
                        {
                            int year = Integer.parseInt(matcher.group(1));
                            int month = Integer.parseInt(matcher.group(3));
                            int day = Integer.parseInt(matcher.group(4));
                            DataDate dataDate = new DataDate(day, month, year);
                            if(dataDate.compareTo(sDate) >= 0)
                            {
                                //                            System.out.println("param= " + param);
                                String fileFolderURL = mHostURL +
                                        String.format("%04d.%02d.%02d/", year, month, day);

                                ArrayList<String> fileList = new ArrayList<String>();

                                ByteArrayOutputStream dateFolderOutstream = new ByteArrayOutputStream();
                                DownloadUtils.downloadToStream(new URL(fileFolderURL), dateFolderOutstream);

                                Pattern fileNamePattern = mData.fileNamePattern;

                                // extract all the files that matches the filename pattern in the date directory
                                // add each one into the fileList
                                Iterable<String> folderContents = Arrays.asList(dateFolderOutstream.toString().split("[//r//n]+"));
                                for (String line : folderContents)
                                {
                                    Matcher m = fileNamePattern.matcher(line);
                                    if(line.contains(".hdf") && !line.contains(".xml") && (m.find()))
                                    {
                                        // limit to the targeted MODIS tiles only
                                        //                                        for (String tile : modisTiles)
                                        //                                        {
                                        //                                            if (line.contains(tile))
                                        //                                            {
                                        //                                        lock.lock();
                                        fileList.add(m.group(0));
                                        //                                        lock.unlock();
                                        //                                            }
                                        //                                        }

                                    }
                                }

                                // add the date and fileList pair to the map
                                //                                lock.lock();
                                //                                synchronized(mapDatesFiles) {
                                DataDate tempD = new DataDate(day, month, year);
                                result.put(tempD, fileList);
                                //                                }
                            }
                        }
                        catch(Exception e)
                        {
                            ErrorLog.add(Config.getInstance(), mData.Title, mData.name, "ModisListDatesFiles.ListDatesFilesHTTP problem while getting file list in Parallel.ForEach.", e);
                            //                        return;
                        }
                        finally {
                            //                            if(lock.isHeldByCurrentThread()) {
                            //                                lock.unlock();
                            //                            }
                        }
                    }
                    return result;
                    //            }
                };
            });

            synchronized(mapDatesFiles)
            {
                for(Future<HashMap<DataDate, ArrayList<String>>> future : futures)
                {
                    HashMap<DataDate, ArrayList<String>> mapResult = future.get();
                    if(mapResult == null)
                    {
                        throw new Exception("mapResult null.");
                    }
                    Iterator<DataDate> keysIt = mapResult.keySet().iterator();
                    DataDate tempKey;
                    while(keysIt.hasNext())
                    {
                        tempKey = keysIt.next();
                        mapDatesFiles.put(tempKey, mapResult.get(tempKey));
                    }
                }
            }
        }
        catch (Exception e)
        {
            ErrorLog.add(Config.getInstance(), mData.Title, mData.name, "ModisListDatesFiles.ListDatesFilesHTTP problem while setting up download stream or ParallelForEach.", e);
            return null;
        }

        System.out.println("Finished running ModisListDatesFiles.");
        return mapDatesFiles;
    }

}

