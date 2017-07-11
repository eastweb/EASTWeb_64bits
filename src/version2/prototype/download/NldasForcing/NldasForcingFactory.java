package version2.prototype.download.NldasForcing;

import java.io.IOException;
import java.time.LocalDate;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.DownloaderFactory;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.LocalStorageDownloadFactory;
import version2.prototype.util.DatabaseCache;

/**
 * @author michael.devos
 *
 */
public final class NldasForcingFactory extends DownloadFactory {

    public NldasForcingFactory(Config configInstance, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, DownloadMetaData downloadMetaData, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache, LocalDate startDate, LocalDate endDate) {
        super(configInstance, projectInfoFile, pluginInfo, downloadMetaData, pluginMetaData, scheduler, outputCache,  startDate, endDate);
    }

    @Override
    public DownloaderFactory CreateDownloaderFactory(ListDatesFiles listDatesFiles) {
        return new LocalStorageDownloadFactory(configInstance, "NldasForcingDownloader", projectInfoFile, pluginInfo, downloadMetaData, pluginMetaData, scheduler, outputCache, listDatesFiles, startDate, endDate);
    }

    @Override
    public ListDatesFiles CreateListDatesFiles() throws IOException {
        return new NldasForcingListDatesFiles(new DataDate(startDate), new DataDate(endDate), downloadMetaData, projectInfoFile);
    }
}
