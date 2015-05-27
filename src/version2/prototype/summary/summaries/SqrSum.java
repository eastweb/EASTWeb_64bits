package version2.prototype.summary.summaries;

import java.util.ArrayList;
import java.util.Map;

public class SqrSum extends SummarySingleton {

    public SqrSum(SummariesCollection col) {
        super(col);
    }

    @Override
    public void put(int index, double value) {
        if(map.get(index) == null) {
            map.put(index, value);
        } else {
            map.put(index, map.get(index) + value*value);
        }
    }

    @Override
    public Map<Integer, Double> getResult() {
        return map;
    }

    @Override
    public ArrayList<SummarySingleton> getDistinctLeaflets() {
        ArrayList<SummarySingleton> temp = new ArrayList<SummarySingleton>();
        temp.add(this);
        return temp;
    }
}
