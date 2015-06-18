package objects;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Macro_float extends Macro {

    /** i'm assuming String key is the metric name  */
    public Map<String, float[]> dataArrays;

    public Macro_float(String name, int[] dates, Map<String, float[]> dataArrays) {
	super(name, dates, dataArrays.keySet());
	this.dataArrays = dataArrays;
    }

}
