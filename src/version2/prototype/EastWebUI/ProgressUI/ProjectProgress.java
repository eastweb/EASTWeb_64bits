package version2.prototype.EastWebUI.ProgressUI;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.JList;

import java.util.Iterator;
import java.util.TreeMap;

import version2.prototype.Config;
import version2.prototype.EASTWebManager;
import version2.prototype.ErrorLog;
import version2.prototype.GUIUpdateHandler;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.SchedulerStatus;

public class ProjectProgress {
    private JFrame frame;
    private JList<String> logList;
    private JProgressBar downloadProgressBar;
    private JProgressBar processProgressBar;
    private JProgressBar indiciesProgressBar;
    private JProgressBar summaryProgressBar;

    private DefaultListModel<String> itemLog;
    private GUIUpdateHandlerImplementation updateHandler;
    private String projectName;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    @SuppressWarnings("unused")
                    ProjectProgress window = new ProjectProgress("Test Progress window");
                } catch (Exception e) {
                    ErrorLog.add(Config.getInstance(), "ProjectProgress.main problem with running a ProjectProgress window.", e);
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ProjectProgress(String projectName) throws Exception{
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

        initialize(projectName);

        updateHandler = new GUIUpdateHandlerImplementation(projectName);
        updateHandler.run();
        EASTWebManager.RegisterGUIUpdateHandler(updateHandler);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize(String projectName) {
        this.projectName = projectName;

        frame = new JFrame();
        frame.setBounds(100, 100, 800, 700);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setVisible(true);

        CreateProgressView();
        CreateLogView();
    }

    private void CreateProgressView() {
        JPanel panel = new JPanel();
        panel.setBounds(10, 11, 764, 167);
        panel.setBorder(new TitledBorder(null, String.format("%1$s Progress Summary", projectName), TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JLabel lblDownloadProgress = new JLabel("Download Progress:");
        lblDownloadProgress.setBounds(10, 25, 150, 25);
        panel.add(lblDownloadProgress);
        downloadProgressBar = new JProgressBar();
        downloadProgressBar.setStringPainted(true);
        downloadProgressBar.setBounds(225, 25, 525, 25);
        panel.add(downloadProgressBar);

        JLabel lblProcessProgress = new JLabel("Process Progress: ");
        lblProcessProgress.setBounds(10, 55, 150, 25);
        panel.add(lblProcessProgress);
        processProgressBar = new JProgressBar();
        processProgressBar.setStringPainted(true);
        processProgressBar.setBounds(225, 55, 525, 25);
        panel.add(processProgressBar);

        JLabel lblIndiciesProgress = new JLabel("Indicies Progress:");
        lblIndiciesProgress.setBounds(10, 85, 150, 25);
        panel.add(lblIndiciesProgress);
        indiciesProgressBar = new JProgressBar();
        indiciesProgressBar.setStringPainted(true);
        indiciesProgressBar.setBounds(225, 85, 525, 25);
        panel.add(indiciesProgressBar);

        JLabel lblSummaryProgress = new JLabel("Summary Progress:");
        lblSummaryProgress.setBounds(10, 115, 150, 25);
        panel.add(lblSummaryProgress);
        summaryProgressBar = new JProgressBar();
        summaryProgressBar.setStringPainted(true);
        summaryProgressBar.setBounds(225, 115, 525, 25);
        panel.add(summaryProgressBar);


        System.out.println("MAX: " + downloadProgressBar.getMaximum());
        System.out.println("MIN: " + downloadProgressBar.getMinimum());
    }

    private void CreateLogView() {
        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(null, "Log", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_1.setBounds(10, 200, 765, 450);
        frame.getContentPane().add(panel_1);
        panel_1.setLayout(null);

        itemLog = new DefaultListModel<String>();
        logList = new JList<String>(itemLog);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(logList);
        scrollPane.setBounds(10, 25, 740, 410);
        panel_1.add(scrollPane);
    }

    class ProgressValue{
        public int current;
        public double total;

        public ProgressValue(int current, double total){
            this.current = current;
            this.total = total;
        }

        public int PercentTotal(){
            return (int)(current/total*100);
        }

        public String Description(){
            return String.format("%d / %d",  current, (int)total);
        }
    }

    class GUIUpdateHandlerImplementation implements GUIUpdateHandler{
        private String projectName;

        public GUIUpdateHandlerImplementation(String projectName){
            this.projectName = projectName;
        }

        @Override
        public void run() {
            synchronized(frame){
                if(frame != null) {
                    SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);

                    if(status == null) {
                        downloadProgressBar.setValue(0);
                        downloadProgressBar.setString("Error in Status");

                        processProgressBar.setValue(0);
                        processProgressBar.setString("Error in Status");

                        indiciesProgressBar.setValue(0);
                        indiciesProgressBar.setString("Error in Status");

                        summaryProgressBar.setValue(0);
                        summaryProgressBar.setString("Error in Status");

                        itemLog.clear();
                    } else {
                        ProgressValue downloadValue = GetAverageDownload(status.GetDownloadProgressesByData());
                        downloadProgressBar.setValue(downloadValue.PercentTotal());   // Truncates the double (so value always equates to double rounded down)
                        downloadProgressBar.setString(downloadValue.Description());

                        ProgressValue processValue = GetAverage(status.GetProcessorProgresses());
                        processProgressBar.setValue(processValue.PercentTotal());
                        processProgressBar.setString(processValue.Description());

                        ProgressValue indicesValue = GetAverage(status.GetIndicesProgresses());
                        indiciesProgressBar.setValue(indicesValue.PercentTotal());
                        indiciesProgressBar.setString(indicesValue.Description());

                        ProgressValue summaryValue = GetAverageSummary(status.GetSummaryProgresses());
                        summaryProgressBar.setValue(summaryValue.PercentTotal());
                        summaryProgressBar.setString(summaryValue.Description());

                        itemLog.clear();
                        for(String log : status.ReadAllRemainingLogEntries())
                        {
                            itemLog.addElement(log);
                        }

                        StringBuilder processWorkerInfo = new StringBuilder();
                        Iterator<ProcessName> it = status.GetWorkersInQueuePerProcess().keySet().iterator();
                        ProcessName tempKey = null;

                        processWorkerInfo.append("Project '" + status.ProjectName + "' Workers Queued For Processes:\n");
                        while(it.hasNext())
                        {
                            tempKey = it.next();
                            processWorkerInfo.append("\t" + tempKey.toString() + ":\t" + status.GetWorkersInQueuePerProcess().get(tempKey) + "\n");
                        }

                        it = status.GetActiveWorkersPerProcess().keySet().iterator();
                        processWorkerInfo.append("Project '" + status.ProjectName + "' Active Workers For Processes:\n");
                        while(it.hasNext())
                        {
                            tempKey = it.next();
                            processWorkerInfo.append("\t" + tempKey.toString() + ":\t" + status.GetActiveWorkersPerProcess().get(tempKey) + "\n");
                        }

                        System.out.print(processWorkerInfo);
                    }
                    frame.repaint();
                } else {
                    EASTWebManager.RemoveGUIUpdateHandler(updateHandler);
                }
            }
        }

        private ProgressValue GetAverage(TreeMap<String, Double> TotalProgress){
            double total = 0;
            Iterator<String> pluginIt = TotalProgress.keySet().iterator();

            while(pluginIt.hasNext()) {
                total += TotalProgress.get(pluginIt.next());
            }

            return new ProgressValue(TotalProgress.size(), total);
        }

        private ProgressValue GetAverageSummary(TreeMap<String, TreeMap<Integer, Double>> TotalProgress){
            double total = 0;
            int count = 0;
            Iterator<String> pluginIt = TotalProgress.keySet().iterator();
            Iterator<Integer> summaryIt;
            TreeMap<Integer, Double> pluginTemp;

            while(pluginIt.hasNext()) {
                pluginTemp = TotalProgress.get(pluginIt.next());
                summaryIt = pluginTemp.keySet().iterator();

                while(summaryIt.hasNext()) {
                    total += pluginTemp.get(summaryIt.next());
                    count++;
                }
            }

            return new ProgressValue(count, total);
        }

        private ProgressValue GetAverageDownload(TreeMap<String, TreeMap<String, Double>> TotalProgress){
            double total = 0;
            int count = 0;
            Iterator<String> pluginIt = TotalProgress.keySet().iterator();
            TreeMap<String, Double> dataMap;

            while(pluginIt.hasNext()) {
                dataMap = TotalProgress.get(pluginIt.next());

                for(Double value : dataMap.values()){
                    total += value;
                    count++;
                }
            }

            return new ProgressValue(count, total);
        }
    }
}