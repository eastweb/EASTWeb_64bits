package version2.prototype.Scheduler;

import java.util.Iterator;
import java.util.concurrent.Callable;

import version2.prototype.EASTWebManager;
import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
import version2.prototype.TaskState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.indices.IndicesWorker;
import version2.prototype.util.GeneralUIEventObject;

/**
 * Handles monitoring the status of running workers.
 * @author michael.devos
 */
public class SchedulerWorker implements Callable<ProcessWorkerReturn> {
    private final ProcessWorker worker;
    private final SchedulerStatusContainer statusContainer;
    private final Scheduler scheduler;


    /**
     * Creates a SchedulerWorker that will run the given Callable once executed.
     * @param worker
     * @param statusContainer
     * @param scheduler
     */
    public SchedulerWorker(ProcessWorker worker, SchedulerStatusContainer statusContainer, Scheduler scheduler)
    {
        this.worker = worker;
        this.statusContainer = statusContainer;
        this.scheduler = scheduler;
        synchronized(statusContainer) {
            statusContainer.AddWorker(worker.process.processName);
        }
    }

    public ProjectInfoFile GetProjectInfo()
    {
        return worker.projectInfoFile;
    }

    public String GetWorkerName()
    {
        return worker.processWorkerName;
    }

    @Override
    public ProcessWorkerReturn call() throws Exception {

        double download;
        double processor;
        double indicies;
        double summary;
        boolean extraIndices = false;

        ProcessWorkerReturn theReturn = null;

        synchronized(statusContainer) {
            statusContainer.AddActiveWorker(worker.process.processName);
        }
        scheduler.NotifyUI(new GeneralUIEventObject(this, null));

        String oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(oldThreadName + "-" + worker. projectInfoFile.GetProjectName() + "-" + worker.processWorkerName + "");
        TaskState temp = worker.getTaskState();
        worker.setTaskState(TaskState.RUNNING);

        theReturn = worker.call();

        worker.setTaskState(temp);
        Thread.currentThread().setName(oldThreadName + "-Updating-Scheduler-Status");


        synchronized(statusContainer) {
            statusContainer.SubtractActiveWorker(worker.process.processName);
            statusContainer.SubtractWorker(worker.process.processName);

            download = statusContainer.downloadProgressesByData.lastEntry().getValue().lastEntry().getValue();
            processor = statusContainer.processorProgresses.lastEntry().getValue();
            indicies = statusContainer.indicesProgresses.lastEntry().getValue();
            summary = statusContainer.summaryProgresses.lastEntry().getValue().lastEntry().getValue();

            StringBuilder processWorkerInfo = new StringBuilder();
            SchedulerStatus status = scheduler.GetSchedulerStatus();
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


            if(download >= 100 && processor >= 100 && indicies >= 100 && summary >= 100)
            {
                if(worker.verifyResults() == false)
                {
                    scheduler.AttemptUpdate();

                }
                else
                {
                    EASTWebManager.StopExistingScheduler(scheduler.GetID(), false);
                    EASTWebManager.StopAndShutdown();
                }

                //                if(worker.pluginMetaData.ExtraIndices)
                //                {
                //                    System.out.println();
                //                    System.out.println("ExtraIndicesBoolean: " + worker.pluginMetaData.ExtraIndices);
                //                    System.out.println();
                //
                //                    IndicesWorker extra = new IndicesWorker(worker.configInstance, worker.process, worker.projectInfoFile, worker.pluginInfo, worker.pluginMetaData);
                //
                //                    if(extra.verifyResults() == true)
                //                    {
                //                        System.out.println();
                //                        System.out.println("Attempting Scheduler Update.");
                //                        System.out.println();
                //                        scheduler.AttemptUpdate();
                //
                //                    }
                //                    else
                //                    {
                //                        scheduler.Stop();
                //                        EASTWebManager.StopAndShutdown();
                //                    }
                //
                //                }

            }
        }


        scheduler.NotifyUI(new GeneralUIEventObject(this, null));

        Thread.currentThread().setName(oldThreadName);
        return theReturn;
    }

}
