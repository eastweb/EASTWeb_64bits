package version2.prototype.EastWebUI.CLI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.amazonaws.samples.DeployCode.S3;

import version2.prototype.Config;
import version2.prototype.EASTWebManager;
import version2.prototype.ErrorLog;
import version2.prototype.TaskState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.Scheduler.SchedulerData;
import version2.prototype.Scheduler.SchedulerStatus;

public class MainEntry {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        //String ProjectName = "NIdasForcing_T1_2";
        //String selectedProject = String.valueOf(ProjectName);
        Boolean Flag = true;

        EASTWebManager.Start();
        String selectedProject = ReadSubprojectFile();
        /* S3 s3 = new S3();
        ArrayList<String> list = s3.GetListOfAllS3Objects(); //get the xml files' name from S3 bucket

        String filename = list.get(0); //take first file in the bucket
        String Path = System.getProperty("user.dir");
        System.out.println(Path);
        if(s3.GetObject(filename,Path)){
            //Delete the file from S3 bucket after download the file in the instance
            s3.deleteFile(filename);
        }

        int i = filename.indexOf("/")+1;
        int j = filename.indexOf(".");
        String selectedProject = filename.substring(i, j);

        System.out.println(selectedProject);
        ProjectInfoFile pro = ProjectInfoCollection.GetProject(Config.getInstance(), selectedProject);
        CheckDownloadedFiles.checkPathes(pro);*/
        EASTWebManager.Start();
        System.out.println(selectedProject);
        run(selectedProject);

        /* ProjectInfoFile project = ProjectInfoCollection.GetProject(Config.getInstance(), selectedProject);
        try {
            SchedulerData data = new SchedulerData(project);
            EASTWebManager.LoadNewScheduler(data, false);
            EASTWebManager.StartExistingScheduler(selectedProject, true);
        }catch (PatternSyntaxException | DOMException | ParserConfigurationException | SAXException | IOException e) {
            ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with creating new file from Desktop.", e);
        } catch (Exception e) {
            ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with creating new file from Desktop.", e);
        }*/

        while (Flag){

            try{
                SchedulerStatus status = EASTWebManager.GetSchedulerStatus(selectedProject);

                if (status.State != null) {
                    //System.out.println(status.State);
                }

                /* if (status != null && status.State != null && status.ProjectUpToDate) {

                    System.out.println("Project is up to date. or completed---------1");
                    System.exit(0);
                }*/
                if (status != null && status.State != null && status.State == TaskState.STOPPED && status.ProjectUpToDate) {
                    if (IsThereUnprocessedFile()){

                        selectedProject = ReadSubprojectFile();
                        run(selectedProject);
                    }
                    else{

                        System.out.println("Project is up to date. or completed---------2");
                        EASTWebManager.StopAndShutdown();
                        System.exit(0);
                    }
                }
                /*if (status.State != TaskState.STOPPED && status.ProjectUpToDate) {
                    System.out.println("Project is up to date. or completed---------3");
                    EASTWebManager.StopAndShutdown();
                    System.exit(0);
                }*/

                Thread.sleep(5000);
            }catch(Exception e){
                e.printStackTrace();
            }


        }
        /* while (Flag) {

            try {

                SchedulerStatus status =
                        EASTWebManager.GetSchedulerStatus(selectedProject);
                try {
                    if (status.State != null) {
                        System.out.println(status.State);
                    }
                } catch (Exception e) {

                }
                try {
                    if (status != null) {
                        if (status.State != null) {
                            if (status.ProjectUpToDate) {

                                System.out
                                .println("Project is up to date. or completed");

                                System.exit(0);

                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }   if (status != null) {
                    if (status.State != null) {
                        if (status.State == TaskState.STOPPED
                                && status.ProjectUpToDate) {

                            System.out.println("Project is up to date. or completed");
                            EASTWebManager.StopAndShutdown();
                            System.exit(0);
                        }
                    }
                }if (status != null) {
                    if (status.State == TaskState.STOPPED
                            && !status.ProjectUpToDate) {
                        //EASTWebManager.StartExistingScheduler(selectedProject, true);

                    }
                }
                if (status == null) {
                    System.out.println("Project is not running.");
                } else if (status.State != TaskState.STOPPED) {
                    if (status.ProjectUpToDate) {

                        System.out
                        .println("Project is up to date. or completed");
                        EASTWebManager.StopAndShutdown();
                        System.exit(0);

                    } else {

                        System.out.println("Project is processing.");
                    }
                }
                Thread.sleep(5000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    private static boolean IsThereUnprocessedFile() {
        // TODO Auto-generated method stub
        S3 s3 = new S3();
        ArrayList<String> list = s3.GetListOfAllS3Objects();
        if (list.size() > 0) {
            System.out.println("the number of subproject Files"+list.size());
            return true;
        } else {
            System.out.println("There is no file in S3 bucket");
            return false;
        }
    }

    private static void run(String selectedProject) {
        // TODO Auto-generated method stub
        ProjectInfoFile project = ProjectInfoCollection.GetProject(Config.getInstance(), selectedProject);
        try {
            SchedulerData data = new SchedulerData(project);
            EASTWebManager.LoadNewScheduler(data, false);
            EASTWebManager.StartExistingScheduler(selectedProject, true);
        }catch (PatternSyntaxException | DOMException | ParserConfigurationException | SAXException | IOException e) {
            ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with creating new file from Desktop.", e);
        } catch (Exception e) {
            ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with creating new file from Desktop.", e);
        }

    }

    private static String ReadSubprojectFile() {
        S3 s3 = new S3();
        ArrayList<String> list = s3.GetListOfAllS3Objects(); //get the xml files' name from S3 bucket

        String filename = list.get(0); //take first file in the bucket
        String Path = System.getProperty("user.dir");
        System.out.println(Path);
        if(s3.GetObject(filename,Path)){
            //Delete the file from S3 bucket after download the file in the instance
            s3.deleteFile(filename);
        }

        int i = filename.indexOf("/")+1;
        int j = filename.indexOf(".");
        String selectedProject = filename.substring(i, j);

        System.out.println(selectedProject);
        ProjectInfoFile pro = ProjectInfoCollection.GetProject(Config.getInstance(), selectedProject);
        CheckDownloadedFiles.checkPathes(pro);
        return selectedProject;

    }

}
