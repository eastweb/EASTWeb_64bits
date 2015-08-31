package version2.prototype.Scheduler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.EASTWebManagerI;
import version2.prototype.ErrorLog;
import version2.prototype.GenericProcess;
import version2.prototype.Process;
import version2.prototype.TaskState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.LocalDownloader;
import version2.prototype.indices.IndicesWorker;
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.summary.Summary;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.FileSystem;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class Scheduler {
    /**
     * SchedulerData object this Scheduler was created with.
     */
    public final SchedulerData data;
    /**
     * ProjectInfoFile object gotten from SchedulerData during Scheduler creation.
     */
    public final ProjectInfoFile projectInfoFile;
    /**
     * PluginMetaDataCollection object gotten from SchedulerData during Scheduler creation.
     */
    public final PluginMetaDataCollection pluginMetaDataCollection;

    private final int ID;
    private final Config configInstance;
    private final EASTWebManagerI manager;
    protected SchedulerStatusContainer statusContainer;
    private ArrayList<LocalDownloader> localDownloaders;
    private ArrayList<Process> processorProcesses;
    private ArrayList<Process> indicesProcesses;
    private ArrayList<Process> summaryProcesses;
    private ArrayList<DatabaseCache> downloadCaches;
    private ArrayList<DatabaseCache> processorCaches;
    private ArrayList<DatabaseCache> indicesCaches;

    /**
     * Creates and sets up a Scheduler instance with the given project data. Does not start the Scheduler and Processes.
     * To start processing call start().
     *
     * @param data  - SchedulerData describing the project to setup for
     * @param myID  - a unique ID for this Scheduler instance
     * @param manager  - reference to the EASTWebManager creating this Scheduler
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Scheduler(SchedulerData data, int myID, EASTWebManagerI manager) throws IOException, ParserConfigurationException, SAXException
    {
        this(data, myID, TaskState.STOPPED, manager, Config.getInstance());
    }

    /**
     * Creates and sets up a Scheduler instance with the given project data. Does not start the Scheduler and Processes.
     * To start processing call start().
     *
     * @param data  - SchedulerData describing the project to setup for
     * @param myID  - a unique ID for this Scheduler instance
     * @param manager  - reference to the EASTWebManager creating this Scheduler
     * @param configInstance
     */
    public Scheduler(SchedulerData data, int myID, EASTWebManagerI manager, Config configInstance)
    {
        this(data, myID, TaskState.STOPPED, manager, configInstance);
    }

    /**
     * Creates and sets up a Scheduler instance with the given project data. Sets the Scheduler's running state to the given TaskState.
     * Call Start() eventually, if initState is TaskState.STOPPED, to start the project processing.
     *
     * @param data  - SchedulerData describing the project to setup for
     * @param myID  - a unique ID for this Scheduler instance
     * @param initState  - Initial TaskState to set this Scheduler to.
     * @param manager  - reference to the EASTWebManager creating this Scheduler
     * @param configInstance
     */
    public Scheduler(SchedulerData data, int myID, TaskState initState, EASTWebManagerI manager, Config configInstance)
    {
        this.data = data;
        projectInfoFile = data.projectInfoFile;
        pluginMetaDataCollection = data.pluginMetaDataCollection;

        statusContainer = new SchedulerStatusContainer(configInstance, myID, projectInfoFile.GetProjectName(), data.projectInfoFile.GetPlugins(), data.projectInfoFile.GetSummaries(),
                pluginMetaDataCollection, initState);
        localDownloaders = new ArrayList<LocalDownloader>(1);
        processorProcesses = new ArrayList<Process>(1);
        indicesProcesses = new ArrayList<Process>(1);
        summaryProcesses = new ArrayList<Process>(1);
        downloadCaches = new ArrayList<DatabaseCache>(1);
        processorCaches = new ArrayList<DatabaseCache>(1);
        indicesCaches = new ArrayList<DatabaseCache>(1);
        ID = myID;
        this.configInstance = configInstance;
        this.manager = manager;

        // Make sure directory layout is set up
        new File(FileSystem.GetRootDirectoryPath(projectInfoFile)).mkdirs();
        new File(FileSystem.GetProjectDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName())).mkdirs();

        // Update status in EASTWebManager
        manager.NotifyUI(statusContainer.GetStatus());

        PluginMetaData pluginMetaData;
        Connection con = null;
        try {
            con = DatabaseConnector.getConnection();
            for(ProjectInfoPlugin item: data.projectInfoFile.GetPlugins())
            {
                pluginMetaData = pluginMetaDataCollection.pluginMetaDataMap.get(item.GetName());
                new File(FileSystem.GetGlobalDownloadDirectory(configInstance, item.GetName())).mkdirs();

                // Total Input Units = (((#_of_days_since_start_date / #_of_days_in_a_single_input_unit) * #_of_days_to_interpolate_out) / #_of_days_in_temporal_composite)
                Schemas.CreateProjectPluginSchema(con, configInstance.getGlobalSchema(), projectInfoFile.GetProjectName(), item.GetName(),
                        configInstance.getSummaryCalculations(), projectInfoFile.GetStartDate(), pluginMetaData.DaysPerInputData, pluginMetaData.Download.filesPerDay, item.GetIndices().size(),
                        projectInfoFile.GetSummaries(), true);
                SetupProcesses(item, pluginMetaData);
            }
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | ParseException | ParserConfigurationException | SAXException | IOException e) {
            ErrorLog.add(projectInfoFile, this, "Problem setting up Scheduler.", e);
        }
        finally {
            if(con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    ErrorLog.add(projectInfoFile, this, "Problem closing connection.", e);
                }
            }
        }
    }

    /**
     * Returns this Scheduler's ID number that acts similar to a system process ID.
     *
     * @author michael.devos
     * @return unique ID number
     */
    public int GetID() { return ID; }

    /**
     * Returns the current status of this Scheduler.
     *
     * @author michael.devos
     * @return SchedulerStatus - represents status of this Scheduler instance at that point in time
     */
    public SchedulerStatus GetSchedulerStatus()
    {
        synchronized (statusContainer)
        {
            return statusContainer.GetStatus();
        }
    }

    /**
     * Updates the Scheduler's {@link TaskState TaskState} to RUNNING notifying all observers of said state of the change.
     *
     * @author michael.devos
     */
    public void Start()
    {
        synchronized (statusContainer)
        {
            statusContainer.UpdateSchedulerTaskState(TaskState.RUNNING);
        }
        for(DatabaseCache cache : downloadCaches)
        {
            cache.NotifyObserversToCheckForPastUpdates();
        }
        for(DatabaseCache cache : processorCaches)
        {
            cache.NotifyObserversToCheckForPastUpdates();
        }
        for(DatabaseCache cache : indicesCaches)
        {
            cache.NotifyObserversToCheckForPastUpdates();
        }
    }

    /**
     * Updates the Scheduler's {@link TaskState TaskState} to STOPPED notifying all observers of said state of the change.
     *
     * @author michael.devos
     *
     */
    public void Stop()
    {
        synchronized (statusContainer)
        {
            statusContainer.UpdateSchedulerTaskState(TaskState.STOPPED);
        }
    }

    /**
     * Gets this scheduler's thread state.
     *
     * @author michael.devos
     * @return scheduler's current thread state
     */
    public TaskState GetState()
    {
        return statusContainer.GetState();
    }

    /**
     * Used by the executed frameworks ({@link Process Process} objects) to send information up to the GUI.
     *
     * @param e  - GUI update information
     */
    public void NotifyUI(GeneralUIEventObject e)
    {
        if((e.getSource() instanceof Process) && (e.getProgress() != null) && (e.getPluginName() != null) && (e.getExpectedNumOfOutputs() != null))
        {
            Process process = (Process)e.getSource();

            synchronized (statusContainer)
            {
                switch(process.processName)
                {
                case DOWNLOAD:
                    if(e.getDataName() != null) {
                        statusContainer.UpdateDownloadProgressByData(e.getProgress(), e.getPluginName(), e.getDataName(), e.getExpectedNumOfOutputs());
                    }
                    break;
                case PROCESSOR:
                    statusContainer.UpdateProcessorProgress(e.getProgress(), e.getPluginName(), e.getExpectedNumOfOutputs());
                    break;
                case INDICES:
                    statusContainer.UpdateIndicesProgress(e.getProgress(), e.getPluginName(), e.getExpectedNumOfOutputs());
                    break;
                default:    // SUMMARY
                    if(e.getSummaryID() != null) {
                        statusContainer.UpdateSummaryProgress(e.getProgress(), e.getPluginName(), e.getSummaryID(), e.getExpectedNumOfOutputs());
                    }
                    break;
                }
            }
        }
        else if((e.getSource() instanceof DatabaseCache) && (e.getProgress() != null) && (e.getPluginName() != null) && (e.getExpectedNumOfOutputs() != null))
        {
            DatabaseCache cache = (DatabaseCache)e.getSource();

            synchronized (statusContainer)
            {
                switch(cache.processCachingFor)
                {
                case DOWNLOAD:
                    if(e.getDataName() != null) {
                        statusContainer.UpdateDownloadProgressByData(e.getProgress(), e.getPluginName(), e.getDataName(), e.getExpectedNumOfOutputs());
                    }
                    break;
                case PROCESSOR:
                    statusContainer.UpdateProcessorProgress(e.getProgress(), e.getPluginName(), e.getExpectedNumOfOutputs());
                    break;
                case INDICES:
                    statusContainer.UpdateIndicesProgress(e.getProgress(), e.getPluginName(), e.getExpectedNumOfOutputs());
                    break;
                default:    // SUMMARY
                    if(e.getSummaryID() != null) {
                        statusContainer.UpdateSummaryProgress(e.getProgress(), e.getPluginName(), e.getSummaryID(), e.getExpectedNumOfOutputs());
                    }
                    break;
                }
            }
        }

        if(e.getStatus() != null) {
            statusContainer.AddToLog(e.getStatus());
        }

        manager.NotifyUI(statusContainer.GetStatus());
    }

    /**
     * Checks for new work from associated GlobalDownloaders using the stored LocalDownloaders which gets the number of available files to process from each of them and updates their local caches to
     * start processing the new files.
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void AttemptUpdate() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        TreeMap<String, TreeMap<Integer, Integer>> results = new TreeMap<String, TreeMap<Integer, Integer>>();
        for(LocalDownloader dl : localDownloaders)
        {
            results.put(dl.pluginInfo.GetName(), dl.AttemptUpdate());
        }
        Start();
        statusContainer.CheckIfProjectIsUpToDate();
        manager.NotifyUI(statusContainer.GetStatus());
    }

    protected Scheduler(ProjectInfoFile projectInfoFile, PluginMetaDataCollection pluginMetaDataCollection, int myID, Config configInstance, EASTWebManagerI manager)
    {
        data = new SchedulerData(projectInfoFile, pluginMetaDataCollection);
        this.projectInfoFile = projectInfoFile;
        this.pluginMetaDataCollection = pluginMetaDataCollection;
        ID = myID;
        this.configInstance = configInstance;
        this.manager = manager;
    }

    protected Scheduler(ProjectInfoFile projectInfoFile, PluginMetaDataCollection pluginMetaDataCollection, int myID, Config configInstance, EASTWebManagerI manager, SchedulerStatusContainer statusContainer)
    {
        data = new SchedulerData(projectInfoFile, pluginMetaDataCollection);
        this.projectInfoFile = projectInfoFile;
        this.pluginMetaDataCollection = pluginMetaDataCollection;
        ID = myID;
        this.configInstance = configInstance;
        this.manager = manager;
        this.statusContainer = statusContainer;
    }

    /**
     * Sets up a set of Process extending classes to act as ProcesWorker Managers for each of the four frameworks for each ProjectInfoPlugin given.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParseException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    protected void SetupProcesses(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, IOException, ParserConfigurationException, SAXException
    {
        //Scheduler scheduler, String globalSchema, String projectName, String pluginName, String workingDir, ProcessName processCachingFor, ArrayList<String> extraDownloadFiles
        DatabaseCache downloadCache = new DatabaseCache(this, configInstance.getGlobalSchema(), data.projectInfoFile.GetProjectName(), pluginInfo, pluginMetaData, data.projectInfoFile.GetWorkingDir(),
                ProcessName.DOWNLOAD);
        DatabaseCache processorCache = new DatabaseCache(this, configInstance.getGlobalSchema(), data.projectInfoFile.GetProjectName(), pluginInfo, pluginMetaData, data.projectInfoFile.GetWorkingDir(),
                ProcessName.PROCESSOR);
        DatabaseCache indicesCache = new DatabaseCache(this, configInstance.getGlobalSchema(), data.projectInfoFile.GetProjectName(), pluginInfo, pluginMetaData, data.projectInfoFile.GetWorkingDir(),
                ProcessName.INDICES);
        DatabaseCache outputCache = new DatabaseCache(this, configInstance.getGlobalSchema(), data.projectInfoFile.GetProjectName(), pluginInfo, pluginMetaData, data.projectInfoFile.GetWorkingDir(),
                ProcessName.SUMMARY);

        localDownloaders.addAll(SetupDownloadProcess(pluginInfo, pluginMetaData, downloadCache));
        processorProcesses.add(SetupProcessorProcess(pluginInfo, pluginMetaData, downloadCache, processorCache));
        indicesProcesses.add(SetupIndicesProcess(pluginInfo, pluginMetaData, processorCache, indicesCache));
        summaryProcesses.add(SetupSummaryProcess(pluginInfo, pluginMetaData, indicesCache, outputCache));

        downloadCaches.add(downloadCache);
        processorCaches.add(processorCache);
        indicesCaches.add(indicesCache);
    }

    /**
     * Sets up a {@link GenericFrameworkProcess GenericFrameworkProcess} object to manage DownloadWorkers.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @param pluginMetaData  - plugin information gotten from a PluginMetaData.*.xml
     * @param outputCache  - DatabaseCache object used to cache output files from a LocaldDownloader
     * @return general concrete Process object for managing ProcessWorkers
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    protected ArrayList<LocalDownloader> SetupDownloadProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache outputCache) throws ClassNotFoundException, NoSuchMethodException,
    SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, IOException
    {
        ArrayList<LocalDownloader> lDownloders = new ArrayList<LocalDownloader>();

        // Create "data" DownloadFactory
        Class<?> downloadFactoryClass = Class.forName("version2.prototype.download." + pluginInfo.GetName() + "." + pluginMetaData.Download.downloadFactoryClassName);
        Constructor<?> downloadFactoryCtor = downloadFactoryClass.getConstructor(EASTWebManagerI.class, Config.class, ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class, Scheduler.class,
                DatabaseCache.class, LocalDate.class, DownloadMetaData.class);
        DownloadFactory downloadFactory = (DownloadFactory) downloadFactoryCtor.newInstance(manager, configInstance, projectInfoFile, pluginInfo, pluginMetaData, this, outputCache,
                projectInfoFile.GetStartDate(), pluginMetaData.Download);

        // Create "data" GenericGlobalDownloader
        lDownloders.add(manager.StartGlobalDownloader(downloadFactory));

        for(DownloadMetaData dlMetaData : pluginMetaData.Download.extraDownloads)
        {
            // Create extra ListDatesFiles instance
            downloadFactoryClass = Class.forName("version2.prototype.download." + pluginInfo.GetName() + "." + dlMetaData.downloadFactoryClassName);
            downloadFactoryCtor = downloadFactoryClass.getConstructor(EASTWebManagerI.class, Config.class, ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class, Scheduler.class,
                    DatabaseCache.class, LocalDate.class, DownloadMetaData.class);
            downloadFactory = (DownloadFactory) downloadFactoryCtor.newInstance(manager, configInstance, projectInfoFile, pluginInfo, pluginMetaData, this, outputCache,
                    projectInfoFile.GetStartDate(), dlMetaData);

            // Create extra GenericGlobalDownloader
            lDownloders.add(manager.StartGlobalDownloader(downloadFactory));
        }

        return lDownloders;
    }

    /**
     * Sets up a {@link GenericFrameworkProcess GenericFrameworkProcess} object to manage ProcessorWorkers.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @param pluginMetaData  - plugin information gotten from a PluginMetaData.*.xml
     * @param inputCache  - DatabaseCache object used to acquire files available for processor processing
     * @param outputCache  - DatabaseCache object used to cache output files from processor processing
     * @return general concrete Process object for managing ProcessWorkers
     * @throws ClassNotFoundException
     */
    protected Process SetupProcessorProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException {

        // If desired, GenericFrameworkProcess can be replaced with a custom Process extending class.
        Process process = new GenericProcess<ProcessorWorker>(manager, configInstance, ProcessName.PROCESSOR, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache,
                "version2.prototype.processor.ProcessorWorker");
        //        inputCache.addObserver(process);
        return process;
    }

    /**
     * Sets up a {@link GenericFrameworkProcess GenericFrameworkProcess} object to manage IndicesWorkers.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @param pluginMetaData  - plugin information gotten from a PluginMetaData.*.xml
     * @param inputCache  - DatabacheCache object used to acquire files available for indices processing
     * @param outputCache  - DatabaseCache object used to cache output files from indices processing
     * @return general concrete Process object for managing ProcessWorkers
     * @throws ClassNotFoundException
     */
    protected Process SetupIndicesProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException {
        // If desired, GenericFrameworkProcess can be replaced with a custom Process extending class.
        Process process = new GenericProcess<IndicesWorker>(manager, configInstance, ProcessName.INDICES, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache,
                "version2.prototype.indices.IndicesWorker");
        //        inputCache.addObserver(process);
        return process;
    }

    /**
     * Sets up a {@link version2.prototype.summary.Summary Summary} object to manage SummaryWorkers.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @param pluginMetaData  - plugin information gotten from a PluginMetaData.*.xml
     * @return {@link Summary Summary}  - the manager object of SummaryWorkers for the current project and given plugin
     * @param inputCache  - the DatabaseCache object used to acquire files available for summary processing
     */
    protected Process SetupSummaryProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException
    {
        //        Summary process = new Summary(manager, configInstance, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache);
        Process process = new GenericProcess<IndicesWorker>(manager, configInstance, ProcessName.SUMMARY, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache,
                "version2.prototype.summary.SummaryWorker");
        //        inputCache.addObserver(process);
        return process;
    }

    /**
     * @deprecated
     *
     * @param plugin
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    @Deprecated
    private void RunDownloader(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        // uses reflection
        Class<?> clazzDownloader = Class.forName("version2.prototype.download."
                + pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Title
                + pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Download.downloadFactoryClassName);
        Constructor<?> ctorDownloader = clazzDownloader.getConstructor(DataDate.class, DownloadMetaData.class);
        Object downloader =  ctorDownloader.newInstance(new Object[] {
                data.projectInfoFile.GetStartDate(),
                pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Download});
        Method methodDownloader = downloader.getClass().getMethod("run");
        methodDownloader.invoke(downloader);

        //        DownloadProgress = 100;
        //        log.add("Download Finish");
    }

    /**
     * @deprecated
     *
     * @param plugin
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    @Deprecated
    private void RunProcess(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        //        ProcessorMetaData temp = pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Projection;
        // TODO: revise the "date"
        //        PrepareProcessTask prepareProcessTask;
        // TODO: initiate it with each plugin's implementation
        //prepareProcessTask= new PrepareProcessTask(projectInfoFile, plugin.GetName(), projectInfoFile.startDate, new processListener());

        /* will move to the Projection framework
        for (int i = 1; i <= temp.processStep.size(); i++) {
            if(temp.processStep.get(i) != null && !temp.processStep.get(i).isEmpty())
            {
                Class<?> clazzProcess = Class.forName("version2.prototype.projection."
                        + pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Title
                        + temp.processStep.get(i));
                Constructor<?> ctorProcess = clazzProcess.getConstructor(ProcessData.class);
                Object process =  ctorProcess.newInstance(new Object[] {new ProcessData(
                        prepareProcessTask.getInputFolders(i),
                        prepareProcessTask.getOutputFolder(i),
                        prepareProcessTask.getQC(),
                        prepareProcessTask.getShapeFile(),
                        prepareProcessTask.getMaskFile(),
                        prepareProcessTask.getDataBands(),
                        prepareProcessTask.getQCBands(),
                        prepareProcessTask.getProjection(),
                        prepareProcessTask.getListener())});
                Method methodProcess = process.getClass().getMethod("run");
                methodProcess.invoke(process);
            }
        }
         */
        //        ProcessorProgress = 100;
        //        log.add("Process Finish");
    }

    /**
     * @deprecated
     *
     * @param plugin
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    @Deprecated
    private void RunIndicies(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        for(String indicie: plugin.GetIndices())
        {
            Class<?> clazzIndicies;
            try{
                clazzIndicies = Class.forName(String.format("version2.prototype.indices.%S.%S", plugin.GetName(), indicie));
            }catch(Exception e){
                try{
                    clazzIndicies = Class.forName(String.format("version2.prototype.indices.%S", indicie));
                }catch(Exception ex){
                    throw new EmptyStackException(); // class not found
                }
            }
            Constructor<?> ctorIndicies = clazzIndicies.getConstructor(String.class, DataDate.class, String.class, String.class);
            Object indexCalculator =  ctorIndicies.newInstance(
                    new Object[] {
                            plugin.GetName(),
                            data.projectInfoFile.GetStartDate(),
                            new File(indicie).getName().split("\\.")[0],
                            indicie});
            Method methodIndicies = indexCalculator.getClass().getMethod("calculate");
            methodIndicies.invoke(indexCalculator);
        }
        //        IndiciesProgress = 100;
        //        log.add("Indicies Finish");
    }
}

