package version2.prototype;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;

/**
 * Abstract framework worker class. Frameworks are to use a concrete class that extends this class to handle doing their required processing work.
 *
 * @author michael.devos
 *
 */
public abstract class ProcessWorker implements Callable<ProcessWorkerReturn> {
    /**
     * The name of the ProcessWorker.
     */
    public final String processWorkerName;
    public final Config configInstance;
    public final Process process;
    public final ProjectInfoFile projectInfoFile;
    public final ProjectInfoPlugin pluginInfo;
    public final PluginMetaData pluginMetaData;
    protected ArrayList<DataFileMetaData> cachedFiles;
    protected final DatabaseCache outputCache;

    /**
     * Creates a ProcessWorker object labeled by the given processWorkerName, owned by the given process, and set to work on the given cachedFiles.
     *
     * @param configInstance  - Config reference to use
     * @param processWorkerName  - name of this worker
     * @param process  - reference to the owning process object
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param cachedFiles  - the files to process
     * @param outputCache  - DatbaseCache instance to use as the outputCache
     */
    protected ProcessWorker(Config configInstance, String processWorkerName, Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles, DatabaseCache outputCache)
    {
        this.configInstance = configInstance;
        this.processWorkerName = processWorkerName;
        this.process = process;
        this.projectInfoFile = projectInfoFile;
        this.pluginInfo = pluginInfo;
        this.pluginMetaData = pluginMetaData;
        this.cachedFiles = cachedFiles;
        this.outputCache = outputCache;
    }

    /**
     * Method to override to handle the processing to be done in the implementing class. Called only when Scheduler TaskState is set to RUNNING.
     */
    public abstract ProcessWorkerReturn process();

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ProcessWorkerReturn call() throws Exception {
        if((process.getState() == TaskState.RUNNING || process.getState() == TaskState.STARTING) && !Thread.currentThread().isInterrupted()) {
            return process();
        } else {
            return null;
        }
    }
}
