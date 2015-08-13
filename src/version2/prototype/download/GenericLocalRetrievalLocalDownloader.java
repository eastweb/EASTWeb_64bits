/**
 *
 */
package version2.prototype.download;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.EASTWebManagerI;
import version2.prototype.ErrorLog;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * GenericLocalDownloader used for getting files from a locally used GlobalDownloader for the DatabaseCache this LocalDownloader uses as its output cache.
 *
 * @author michael.devos
 *
 */
public class GenericLocalRetrievalLocalDownloader extends LocalDownloader {

    /**
     * Creates a GenericLocalDownloader that expects to finds the GlobalDownloader download records in a locally and globally accessible table in the database.
     *
     * @param manager
     * @param configInstance
     * @param gdl
     * @param projectInfoFile
     * @param pluginInfo
     * @param pluginMetaData
     * @param scheduler
     * @param outputCache
     */
    public GenericLocalRetrievalLocalDownloader(EASTWebManagerI manager, Config configInstance, GlobalDownloader gdl, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache) {
        super(manager, configInstance, gdl, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
    }

    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        Connection conn = null;
        try {
            if(scheduler.GetState() == TaskState.RUNNING)
            {
                conn = PostgreSQLConnection.getConnection();
                Schemas.updateExpectedResults(configInstance.getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName(), projectInfoFile.GetStartDate(),
                        pluginMetaData.DaysPerInputData, pluginInfo.GetIndices().size(), projectInfoFile.GetSummaries(), conn);

                outputCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(configInstance.getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName(), dataName, projectInfoFile.GetStartDate(),
                        pluginMetaData.ExtraDownloadFiles, projectInfoFile.GetModisTiles());
            }
        } catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException e) {
            ErrorLog.add(projectInfoFile, processName, scheduler, "GenericLocalRetrievalLocalDownloader.process error.", e);
        }
        finally
        {
            if(conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    ErrorLog.add(projectInfoFile, processName, scheduler, "Problem in GenericLocalRetrievalLocalDownloader.process while closing connection.", e);
                }
            }
        }
    }

}
