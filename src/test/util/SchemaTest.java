package test.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.ZonalSummary;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

public class SchemaTest {
    private static Connection con;
    private static String testProjectName;
    private static String testPluginName;
    private static String testGlobalSchema;
    private static String testSchemaName;
    private static String shapeFile;
    private static ArrayList<String> summaryNames;
    private static String areaValueField;
    private static String areaNameField;
    private static TemporalSummaryCompositionStrategy compStrategy;
    private static ArrayList<ProjectInfoSummary> summaries;
    private static ArrayList<String> extraDownloadFiles;

    /**
     * Defined for executable jar file to be used in setting up EASTWeb database for testing purposes. The config.xml file is needed for the database connection information but nothing else is used from
     * in it. The summary calculation fields created are "Count", "Sum", "Mean", and "StdDev". The "extra download file" fields created are just "QC" which effects global Download table and the caches.
     *
     * @param args  - 1. Global schema name, 2. Project name, 3. Plugin name (Two schemas are created: 1. The global schema and 2. A schema named by combining project name and plugin name separated
     * by an '_')
     * @throws ConfigReadException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void main(String[] args) throws ConfigReadException, ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        setUpBeforeClass();
        System.out.println("Setting up PostgreSQL Schema...");
        System.out.println("Global schema to use or create: " + args[0]);
        System.out.println("Project name to use: " + args[1]);
        System.out.println("Plugin name to use: " + args[2]);
        System.out.println("Project schema to create or recreate: " + Schemas.getSchemaName(args[1], args[2]));
        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), args[0], args[1], args[2], summaryNames, extraDownloadFiles, LocalDate.now().minusDays(8), 8, 3, null);
        System.out.println("DONE");
    }

    @BeforeClass
    public static void setUpBeforeClass() throws SQLException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
        con = PostgreSQLConnection.getConnection();
        testProjectName = "Test_Project";
        testPluginName = "Test_Plugin";
        testGlobalSchema = "Test_EASTWeb";
        testSchemaName = testProjectName + "_" + testPluginName;
        shapeFile = "C:\\Users\\sufi\\Desktop\\shapefile\\shapefile.shp";
        areaValueField = "COUNTYNS10";
        areaNameField = "NAME10";
        compStrategy = new GregorianWeeklyStrategy();

        summaryNames = new ArrayList<String>();
        summaryNames.add("Count");
        summaryNames.add("Sum");
        summaryNames.add("Mean");
        summaryNames.add("StdDev");

        summaries = new ArrayList<ProjectInfoSummary>(0);
        summaries.add(new ProjectInfoSummary(new ZonalSummary(shapeFile, areaValueField, areaNameField), new TemporalSummaryRasterFileStore(compStrategy), compStrategy.getClass().getCanonicalName()));

        extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");
    }

    @AfterClass
    public static void tearDownAfterClass() throws SQLException {
        //        Statement stmt = con.createStatement();
        //        stmt.execute(String.format(
        //                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
        //                testGlobalSchema
        //                ));
        //        stmt.execute(String.format(
        //                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
        //                testSchemaName
        //                ));
        //        stmt.close();
        //        con.close();
    }

    @Test
    public final void testGetSchemaName() {
        String schemaName = Schemas.getSchemaName(testProjectName, testPluginName);
        assertTrue("Schema name is " + schemaName, schemaName.equals("test_project_test_plugin"));

        schemaName = Schemas.getSchemaName("1Test_Project", testPluginName);
        assertTrue("Schema name is " + schemaName, schemaName.equals("_test_project_test_plugin"));

        schemaName = Schemas.getSchemaName(" ", " ");
        assertTrue("Schema name is " + schemaName, schemaName.equals("___"));
    }

    @Test
    public final void testCreateProjectPluginSchema() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = null;

        // Remove test schemas if they exist
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testGlobalSchema
                ));
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testSchemaName
                ));

        // Run method under test - defined for MODIS plugin
        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), testGlobalSchema, testProjectName, testPluginName, summaryNames, extraDownloadFiles, LocalDate.now().minusDays(8), 8, 3,
                summaries);

        // Check the created test schemas
        String query = "select n.nspname as \"Name\", count(*) over() as \"RowCount\" " +
                "from pg_catalog.pg_namespace n " +
                "where (n.nspname = '" + testGlobalSchema + "' OR n.nspname = '" + testSchemaName + "') " +
                "AND pg_catalog.pg_get_userbyid(n.nspowner) = '" + Config.getInstance().getDatabaseUsername() + "' " +
                "order by \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 1);
        } else {
            fail("ResultSet is null from querying schemas");
        }

        // Check the existence of the test global schema tables
        query = "SELECT c.relname as \"Name\", count(*) over() as \"RowCount\" " +
                "FROM pg_catalog.pg_class c LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                "WHERE c.relkind = 'r' AND n.nspname = '" + testGlobalSchema + "' " +
                "AND pg_catalog.pg_get_userbyid(c.relowner) = '" + Config.getInstance().getDatabaseUsername() + "' " +
                "ORDER BY \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("Test Global Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 11);

            // Check global schema table names
            do{
                switch(rs.getString("Name"))
                {
                case "DateGroup": break;
                case "Index": break;
                case "Region": break;
                case "Zone": break;
                case "ZoneVar": break;
                case "ExpectedResults": break;
                case "Project": break;
                case "Plugin": break;
                case "GDExpectedResults": break;
                case "GlobalDownloader": break;
                case "Download": break;
                default: fail("Unknown table in test global schema: " + rs.getString("Name"));
                }
            } while(rs.next());
        } else {
            fail("ResultSet is null from querying test global schema tables");
        }

        // Check the existence of the test schema tables
        query = "SELECT c.relname as \"Name\", count(*) over() as \"RowCount\" " +
                "FROM pg_catalog.pg_class c LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                "WHERE c.relkind = 'r' AND n.nspname = '" + testSchemaName + "' " +
                "AND pg_catalog.pg_get_userbyid(c.relowner) = '" + Config.getInstance().getDatabaseUsername() + "' " +
                "ORDER BY \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("Test Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 6);

            // Check schema table names
            do{
                switch(rs.getString("Name"))
                {
                case "ZoneMapping": break;
                case "ZoneField": break;
                case "ZonalStat": break;
                case "DownloadCache": break;
                case "ProcessorCache": break;
                case "IndicesCache": break;
                default: fail("Unknown table in test schema: " + rs.getString("Name"));
                }
            } while(rs.next());
        } else {
            fail("ResultSet is null from querying test schema tables");
        }

        stmt.close();
        rs.close();
    }
}