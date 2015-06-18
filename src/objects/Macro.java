package objects;
import java.util.List;
import java.util.Map;
import analyze.filter_tools.Screen;
import java.util.Set;
import java.util.TreeSet;
import utilities.G;


/** all macro data are float[] OR int[].  only one datatype per macro (all metrics have to be the same datatype :/ :( )  macro metric names should start with "m_" to differentiate from other potential name conflicts */
public class Macro {

    public static class Names {
	public static String dates = "Dates";
	public static String weather = "Weather";
	public static String shortInterest = "ShortInterest";
	public static String tickerPrice = "TickerPrice";
    }

    public static Object[] getDatelyArray_nonNullOn_specificDates(int[] allDates, Set<Integer> specificDates) {

	Object[] nonNullOnSpecificDates = new Object[allDates.length];

	for (int i = 0; i < nonNullOnSpecificDates.length; i++) {
	    if (specificDates.contains(allDates[i]))
		nonNullOnSpecificDates[i] = new Object();
	    else
		nonNullOnSpecificDates[i] = null;		    //i think this is redundant
	}
	return nonNullOnSpecificDates;
    }

    public String name;

    public int[] dates;

    public Map<Integer, Integer> date_i_map;
    public final Set<String> metrics;


    public Macro(String name, int[] dates, Set<String> metrics) {//, Map<String, float[]> dataArrays) {
	this.name = name;
	this.dates = dates;
	this.date_i_map = G.get__date_i__map(this.dates);
	this.metrics = metrics;
//	this.dataArrays = dataArrays;
    }


    /**  DATA  so we can match string from filter to appropriate Macro (each macro has different dates array.  one macro might be weather, another might be change in dollar-to-euro conversion, etc. */
    public static void set__macroMetric_macro__map(List<Macro> macros, Map<String, Macro> macrometric_macro__map) throws Screen.ThereMustBeTwoMacroVariablesWithSameName {

	int numMacroMetrics = 0;

	for (Macro m : macros) {
	    for (String metric : m.metrics) {
		numMacroMetrics++;
		macrometric_macro__map.put(metric, m);
	    }
	}
	if (numMacroMetrics != macrometric_macro__map.size())
	    throw new Screen.ThereMustBeTwoMacroVariablesWithSameName();
    }

}
