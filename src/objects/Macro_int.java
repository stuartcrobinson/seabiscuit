package objects;
import java.util.Map;
import java.util.TreeSet;

public class Macro_int extends Macro {

    /** i'm assuming String key is the metric name  */
    public Map<String, int[]> dataArrays;

    public Macro_int(String name, int[] dates, Map<String, int[]> dataArrays) {
	super(name, dates, dataArrays.keySet());
	this.dataArrays = dataArrays;
    }

}
