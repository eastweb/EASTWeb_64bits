package version2.prototype.EastWebUI.CLI;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import version2.prototype.ProjectInfoMetaData.ProjectInfoCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;

public class CheckDownloadedFiles {

    public static void checkPathes(ProjectInfoFile project){

        Document doc =  project.doc;
        String workingDir = project.GetWorkingDir();
        String ShapFile = project.GetMasterShapeFile();
        String MaskingFile = project.GetMaskingFile();

        Element workingDirNew, ShapFileNew, MaskingFileNew ,SumShapFileNew = null;
        Element projectInfo = (Element) doc.getElementsByTagName("ProjectInfo").item(0);
        Element workDir = (Element) projectInfo.getElementsByTagName("WorkingDir").item(0);
        Element MasterShapF =  (Element) projectInfo.getElementsByTagName("MasterShapeFile").item(0);

        ArrayList<ProjectInfoSummary> ss = project.GetSummaries();


        //Working Directory
        if (workingDir != null){
            projectInfo.removeChild(workDir);
            System.out.println("Old workingDir: "+workingDir);
            int i = workingDir.indexOf("projects");
            String str = workingDir.substring(i, workingDir.length());
            String dire = System.getProperty("user.dir")+"\\"+str;
            System.out.println("New WorkingDirectory: "+ dire);
            if (dire.endsWith("\\") || dire.endsWith("/")){
                dire = dire.substring(0, dire.length()-1);
            }
            System.out.println("New WorkingDirectory: "+ dire);
            workingDirNew =  doc.createElement("WorkingDir");
            workingDirNew.appendChild(doc.createTextNode(dire));
            projectInfo.appendChild(workingDirNew);
        }
        //ShapFile
        if (ShapFile != null){
            projectInfo.removeChild(MasterShapF);
            System.out.println("Old ShapFile: "+ShapFile);
            int j = ShapFile.indexOf("Documentation");
            String str1 = ShapFile.substring(j, ShapFile.length());
            String temp  = System.getProperty("user.dir")+"\\" + str1;
            System.out.println("New ShapFile: "+temp);
            ShapFileNew = doc.createElement("MasterShapeFile");
            ShapFileNew.appendChild(doc.createTextNode(temp));
            projectInfo.appendChild(ShapFileNew);


        }

        //Masking File
        if (MaskingFile != null){
            Element parent = (Element) projectInfo.getElementsByTagName("Masking").item(0);
            Element MaskingF = (Element) parent.getElementsByTagName("File").item(0);
            parent.removeChild(MaskingF);
            System.out.println("Old MaskingFile: "+MaskingFile);
            int j = MaskingFile.indexOf("Documentation");
            String str1 = MaskingFile.substring(j, MaskingFile.length());
            String temp  = System.getProperty("user.dir")+"\\" + str1;
            System.out.println("New MaskingFile: "+temp);
            MaskingFileNew = doc.createElement("File");
            MaskingFileNew.appendChild(doc.createTextNode(temp));
            parent.appendChild(MaskingFileNew);
        }

        if(ss.size() > 0){
            Element parent =  (Element) projectInfo.getElementsByTagName("Summaries").item(0);

            for (int i=0; i< ss.size(); i++){
                Element summary = (Element) parent.getElementsByTagName("Summary").item(i);
                parent.removeChild(summary);
                Element summaryNew = doc.createElement("Summary");
                summaryNew.setAttribute("ID", String.valueOf(ss.get(i).GetID()));

                String content = ss.get(i).toString();
                String str = content.substring(content.indexOf(ProjectInfoSummary.SHAPE_FILE_TAG + ": ") + String.valueOf(ProjectInfoSummary.SHAPE_FILE_TAG + ": ").length(), content.indexOf(";",
                        content.indexOf(ProjectInfoSummary.SHAPE_FILE_TAG + ": ")));
                System.out.println(content);
                System.out.println(str);
                int j = str.indexOf("Documentation");
                String str1 = str.substring(j, str.length());
                String MasterShapFileNew  = System.getProperty("user.dir")+"\\" + str1;
                System.out.println("New ShapFile: "+MasterShapFileNew);
                content = content.replace(str, MasterShapFileNew);
                System.out.println();

                summaryNew.appendChild(doc.createTextNode(content));
                parent.appendChild(summaryNew);
            }
        }
        if(ProjectInfoCollection.WriteProjectToFile(doc, project.GetProjectName())){
            System.out.println("File saved!");
        }




        /*Element workingDirNew, ShapFileNew, SumShapFileNew = null;
        Element projectInfo = (Element) doc.getElementsByTagName("ProjectInfo");
        Element workDir =  (Element) projectInfo.getElementsByTagName("WorkingDir");
        Element MasterShapF =  (Element) projectInfo.getElementsByTagName("MasterShapeFile");
        //Element Summery =  (Element) projectInfo.getElementsByTagName("Summaries");
        projectInfo.removeChild(workDir);
        projectInfo.removeChild(MasterShapF);
        workingDirNew =  doc.createElement("WorkingDir");

        workingDirNew.appendChild(doc.createTextNode(""));

        ShapFileNew = doc.createElement("MasterShapeFile");
        ShapFileNew.appendChild(doc.createTextNode(""));

         */
        //Summery.
        //projectInfo.removeChild(WorkingDir);
        //projectInfo.removeChild(projectName);
    }

}
