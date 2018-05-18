package version2.prototype.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.Scheduler.ProcessName;

/**
 * Handles getting and creating all directories used by the program, creating files, and deleting files.
 * Names are also used in a SQL compliant database. So, all names are standardized to be valid identifiers for it.
 *
 * @author michael.devos
 *
 */

public final class FileSystem {
    private static final Pattern rule2Pattern = Pattern.compile("^[a-zA-Z_]\\S+");

    private FileSystem() {}

    /**
     * Gets cleaned root directory path from project info.
     *
     * @param data  - ProjectInfoFile to get root directory from
     * @return cleaned root directory path
     */
    public static String GetRootDirectoryPath(ProjectInfoFile data)
    {
        String path = data.GetWorkingDir();
        if(!path.endsWith("/") && path.endsWith("\\")) {
            //path += "\\";
            path += File.separator;
        }
        new File(path).mkdirs();
        return path;
    }

    /**
     * Project naming rules to be displayed to users.
     *
     * @return short single-sentenced project naming rules listing
     */
    public static String DescribeNamingRules()
    {
        return new String("Names are case-sensitive and must start with either a letter or underscore and can contain letters, digits, "
                + "and underscores.");
    }

    /**
     * Project naming rules to be displayed to users.
     *
     * @return project naming rules separated within a string array
     */
    public static String[] ListNamingRules()
    {
        String[] rules = new String[3];

        rules[0] = "Case-sensitive";
        rules[1] = "Start with either a letter or underscore";
        rules[2] = "Contains only letters, digits, or underscores";
        return rules;
    }

    /**
     * Standardizes a given name. Returns what is allowed within the system (safe within PostgreSQL database and on expected major file systems).
     *
     * @param name  - any identifying name whether it be a directory name or table name to be used
     * @return the given name after cleaning and formatting to be safe to use within the PostgreSQL database and on the file system
     */
    public static String StandardizeName(String name)
    {
        if(name.length() == 0) {
            return name;
        }

        // Rule 3: Replace any characters that aren't letters, numbers, or an underscore
        name = name.replaceAll("[^a-zA-Z0-9_]", "_");

        // Rule 2: Check that name starts with either a letter or an underscore
        Matcher matcher = rule2Pattern.matcher(name);
        if(!matcher.matches())
        {
            name = "_" + name.substring(1);
        }
        return name;
    }

    /**
     * Gets the project's directory path.
     *
     * @param workingDir  - path of the working directory for EASTWeb data gotten from ProjectInfoFile
     * @param projectName  - name of the project in use
     * @return path to the project's directory
     */
    public static String GetProjectDirectoryPath(String workingDir, String projectName)
    {
        //String path = CheckDirPath(CheckDirPath(workingDir) + "Projects\\" + StandardizeName(projectName) + "\\");
        String path = CheckDirPath(CheckDirPath(workingDir) + "Projects" + File.separator + StandardizeName(projectName) + File.separator);
        new File(path).mkdirs();
        return path;
    }

    /**
     * Gets the process' directory path.
     *
     * @param workingDir  - path of the working directory for EASTWeb data gotten from ProjectInfoFile
     * @param projectName  - name of the project in use
     * @param pluginName  - name of the plugin in use
     * @param processName  - name of the process in use
     * @return path to the process' directory
     */
    public static String GetProcessDirectoryPath(String workingDir, String projectName, String pluginName, ProcessName processName)
    {
        //String path = CheckDirPath(GetProjectDirectoryPath(workingDir, projectName) + StandardizeName(pluginName) + "\\" + GetProcessDirectoryName(processName) + "\\");
        String path = CheckDirPath(GetProjectDirectoryPath(workingDir, projectName) + StandardizeName(pluginName) + File.separator + GetProcessDirectoryName(processName) + File.separator);
        new File(path).mkdirs();
        return path;
    }

    /**
     * Gets the process' output directory path.
     *
     * @param workingDir  - path of the working directory for EASTWeb data gotten from ProjectInfoFile
     * @param projectName  - name of the project in use
     * @param pluginName  - name of the plugin in use
     * @param processName  - name of the process in use
     * @return path to the process' output directory
     */
    public static String GetProcessOutputDirectoryPath(String workingDir, String projectName, String pluginName, ProcessName processName)
    {
        //String path = CheckDirPath(GetProcessDirectoryPath(workingDir, projectName, pluginName, processName) + "Output\\");
        String path = CheckDirPath(GetProcessDirectoryPath(workingDir, projectName, pluginName, processName) + "Output" + File.separator);
        new File(path).mkdirs();
        return path;
    }

    /**
     * Gets the ProcessWorker's temp directory path.
     *
     * @param workingDir  - path of the working directory for EASTWeb data gotten from ProjectInfoFile
     * @param projectName  - name of the project in use
     * @param pluginName  - name of the plugin in use
     * @param processName  - name of the process in use
     * @return path to the ProcessWorker's temp directory
     */
    public static String GetProcessWorkerTempDirectoryPath(String workingDir, String projectName, String pluginName, ProcessName processName)
    {
        //String path = CheckDirPath(GetProcessDirectoryPath(workingDir, projectName, pluginName, processName) + "Temp\\");
        String path = CheckDirPath(GetProcessDirectoryPath(workingDir, projectName, pluginName, processName) + "Temp" + File.separator);
        new File(path).mkdirs();
        return path;
    }

    /**
     * Gets the global download directory path for the given type of data downloaded.
     * @param configInstance
     *
     * @param pluginName  - name of the plugin as defined by global downloaders (e.g. MODIS, NLDAS, etc.)
     * @param dataName  - name of the data files
     * @return path to the download directory for the data
     * @throws ConfigReadException
     */
    public static String GetGlobalDownloadDirectory(Config configInstance, String pluginName, String dataName) throws ConfigReadException
    {
        //String path = CheckDirPath(CheckDirPath(configInstance.getDownloadDir()) + StandardizeName(pluginName) + "\\" + StandardizeName(dataName) + "\\");
        String path = CheckDirPath(CheckDirPath(configInstance.getDownloadDir()) + StandardizeName(pluginName) + File.separator + StandardizeName(dataName) + File.separator);
        new File(path).mkdirs();
        return path;
    }

    private static String GetProcessDirectoryName(ProcessName name)
    {
        String dirName = null;

        switch(name)
        {
        case DOWNLOAD:
            dirName = "Download";
            break;
        case INDICES:
            dirName = "Indices";
            break;
        case PROCESSOR:
            dirName = "Processor";
            break;
        default:    // SUMMARY
            dirName = "Summary";
            break;
        }
        return dirName;
    }

    /**
     * Cleans up the directory path string. Handles adding a slash at the end if there is none to conform to expectations within program.
     * @param dirPath  - directory path string
     * @return  directory path after modifications
     */
    public static String CheckDirPath(String dirPath)
    {
        if(!dirPath.endsWith("/") && !dirPath.endsWith("\\")) {
            //dirPath += "\\";
            dirPath += File.separator;
        }
        return dirPath;
    }
}
