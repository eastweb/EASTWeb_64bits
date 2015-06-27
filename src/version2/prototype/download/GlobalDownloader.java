package version2.prototype.download;

import version2.prototype.TaskState;


/**
 * @author michael.devos
 * @param <V>
 *
 */
public abstract class GlobalDownloader implements Runnable{
    protected GlobalDownloader instance;
    protected TaskState state;
    protected final int ID;
    protected final String pluginName;

    protected GlobalDownloader(TaskState initialState, String pluginName, int myID)
    {
        state = initialState;
        ID = myID;
        this.pluginName = pluginName;
    }

    public abstract GlobalDownloader GetInstance(int myID);

    public abstract void Stop();

    public abstract void Start();

    public int GetID() { return ID; }

    public String GetPluginName() { return pluginName; }
}
