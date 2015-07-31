/**
 *
 */
package test.processor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.Process;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class TestProcessWorkerNldasNOAH {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.processor.ProcessorWorker#call()}.
     * @throws Exception
     */
    @Test
    public final void testCall() throws Exception {
        Process process = null;
        ProjectInfoFile projectInfoFile = new ProjectInfoFile("C:\\Users\\yi.liu\\git\\EastWeb.V2\\src\\version2\\prototype\\ProjectInfoMetaData\\Project_EA.xml");
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File("C:\\Users\\yi.liu\\git\\EastWeb.V2\\src\\version2\\prototype\\PluginMetaData\\Plugin_NldasNOAH.xml")).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());
        //ArrayList<String> extraDownloadFiles;
        //extraDownloadFiles.add("QC");
        //Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), "Test_EASTWeb", "Test_Project", "Test_Plugin", null, null, null,
        //        pluginMetaData.DaysPerInputData, pluginMetaData.Download.filesPerDay, pluginMetaData.IndicesMetaData.size(), projectInfoFile.GetSummaries(), false);

        // Setup test files
        ArrayList<DownloadFileMetaData> extraDownloads = new ArrayList<DownloadFileMetaData>(1);
        // extraDownloads.add(new DownloadFileMetaData("QC", "QC download file path", year, day, null));
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();

        File[] list = new File("D:\\project\\download\\NOAH\\2015\\155").listFiles();
        assert (list.length > 1);
        for(File input : list)
        {
            cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", input.getAbsolutePath(), 2015, 155, null)));
        }

        // DatabaseCache outputCache = new MyDatabaseCache("Project_EA_NldasNOAH.ProcessorCache", projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.PROCESSOR, null);
        ProcessorWorker worker = new ProcessorWorker(process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);

        // Verify results
        //ArrayList<DataFileMetaData> result = outputCache.GetUnprocessedCacheFiles();
        worker.call();
    }

    protected class MyDatabaseCache extends DatabaseCache
    {
        public MyDatabaseCache(String globalSchema, String projectName, String pluginName, ProcessName dataComingFrom, ArrayList<String> extraDownloadFiles) throws ParseException {
            super(globalSchema, projectName, pluginName, dataComingFrom, extraDownloadFiles);
        }

        @Override
        public void CacheFiles(ArrayList<DataFileMetaData> filesForASingleComposite) throws SQLException, ParseException, ClassNotFoundException,
        ParserConfigurationException, SAXException, IOException {
            for(DataFileMetaData data : filesForASingleComposite)
            {
                System.out.println(data.ReadMetaDataForIndices().dataFilePath);
            }
        }
    }

}