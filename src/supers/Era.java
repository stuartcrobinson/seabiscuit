package supers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import utilities.G;

public class Era implements EraInterface, Comparable {

    /** for testing */
    public String colDate;

    /** for quickly determining if a date is within this era */
    public int minDate, maxDate;

    public EraDataRow eraDataRow;

    public Era(int minDate, int maxDate, EraDataRow eraDataRow) {
	this.minDate = minDate;
	this.maxDate = maxDate;
	this.eraDataRow = eraDataRow;
    }

    /** for testing only */
    public Era(String colDate, int minDate, int maxDate, EraDataRow eraDataRow) {
	this.colDate = colDate;
	this.minDate = minDate;
	this.maxDate = maxDate;
	this.eraDataRow = eraDataRow;
    }

    /** from xfile */
    public Era(Class c, String xfileLine) throws InstantiationException, IllegalAccessException, ParseException {

	String[] ar = xfileLine.split("\\" + G.eraDelim, -1);
	minDate = Integer.parseInt(ar[0]);
	maxDate = Integer.parseInt(ar[1]);

//	System.out.println(c);
//	System.out.println("edr string (should contain macrons):\n" + ar[2]);

	String[] edrAr = ar[2].split("\\" + G.edrDelim, -1);

//	System.out.println("split up: " + Arrays.asList(edrAr));
	eraDataRow = (EraDataRow)c.newInstance();
	eraDataRow.assimilateValues(edrAr);
    }

    public static EraDataRow get(List<Era> eras, int date) {
	for (Era era : eras) {
	    if (era.containsDate(date))
		return era.eraDataRow;
	}
	return null;
    }

    public boolean containsDate(int dateInt) {
	return dateInt >= minDate && dateInt <= maxDate;
    }

    /** 20140101 - beginning of last year.  TODO - make this be 1 year ago today */
    public static int getDefaultSingleEraMinDate() {
	return 20140101;
    }

    /** current date */
    public static int getDefaultSingleEraMaxDate() {
	return G.currentDate;
    }

    public String toOutputString() {
	return minDate + G.eraDelim + maxDate + G.eraDelim + eraDataRow.toOutputStringEDR();
    }

    public String toOutputStringTesting() {
	return colDate + G.eraDelim + minDate + G.eraDelim + maxDate + G.eraDelim + eraDataRow.toOutputStringEDR();
    }

    static void writeErasToFile(List<Era> eras, File erasFile) throws IOException {
	erasFile.getParentFile().mkdirs();

	try (PrintWriter pw = new PrintWriter(new FileWriter(erasFile))) {
	    for (Era era : eras)
		pw.println(era.toOutputString());
	}
    }

    /**combine the eras using eras1 over eras2.  used for Financial data, to override annual w/ quarterly data.  this modifies and returns era1.  */
    public static List<Era> combineButPreferTheFirstOne(List<Era> eras1, List<Era> eras2) throws ParseException {
	if (eras1 == null) return eras2;
	if (eras2 == null) return eras1;
	//	List<Era> eras = new ArrayList(eras1);
	for (Era era : eras2)
	    add_an_era__based_off_this_one__to_the_list__with_no_overlapping_dates(eras1, era);
	return eras1;
    }

    /** this makes lists that still have some overlap. that's okay.  fix later if you really want to.  although, maybe quarterly data is bad since it's seasonal.  need to adjust for season?*/
    private static void add_an_era__based_off_this_one__to_the_list__with_no_overlapping_dates(List<Era> preferredEras, Era newEra) throws ParseException {

	for (Era era : preferredEras) {
	    if (newEra.overlaps(era)) {

		//now, modify newera mindate or maxdate so it doenst overlap anymore.  check all listed eras!  it could overlap more than one

		if (era.containsDate(newEra.minDate))
		    newEra.minDate = Integer.parseInt(G.incrementDateByDays(era.maxDate, 1));
		if (era.containsDate(newEra.maxDate))
		    newEra.maxDate = Integer.parseInt(G.incrementDateByDays(era.minDate, -1));
	    }
	}
	if (newEra.minDate < newEra.maxDate) //it might have been totally within a preferred era.  in that case, it's dates would have gotten all screwed up. 
	    preferredEras.add(newEra);
    }

    private boolean overlaps(Era era) {
	return containsDate(era.minDate) || containsDate(era.maxDate);
    }

    /** null if no era found in the list that contains dateInt */
    public static EraDataRow getEraDataRow(List<Era> eras, int dateInt) {

	for (Era era : eras) {
	    if (era.containsDate(dateInt))
		return era.eraDataRow;
	}
	return null;
    }

    @Override
    public int compareTo(Object o) {
	return Integer.compare(minDate, ((Era)o).minDate);
    }

    /** eras should be already sorted by date decreasing (reverse order)*/
    public static int getSmallestMinDate(List<Era> eras) {
	return eras.get(eras.size() - 1).minDate;
    }

    public static List<Era> readFromDisk(File file, Class c) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, ParseException {
	List<Era> eras = new ArrayList();
	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	    String xfileLine;
	    while ((xfileLine = br.readLine()) != null) {
		Era era = new Era(c, xfileLine);
		eras.add(era);
	    }
	}
	return eras;
    }

    /** use \t delimiter */
    public abstract static class EraDataRow implements EraDataRowInterface {

	/** this is how screener/filter will access era data.  key is the variable name in the eraDataRow! */
	public Map<String, Float> valuesMap;

	/** yes!  this should work passing Object.  test worked */
	public static Map<String, Float> make_valuesMap(Object o) throws IllegalArgumentException, IllegalAccessException {

//	    G.asdf("34t34tr making values map for: " + o.getClass());

	    Field[] fs = o.getClass().getFields();

//	    G.asdf("a4faewf found " + fs.length + " fields");

	    Map<String, Float> valuesMap = new LinkedHashMap(fs.length);

	    for (Field f : fs) {

		String type = f.getType().toString();


//		G.asdf("a3w4tgwerfaew  found field: " + f.getName());

		if (type.equals("int") || type.equals("float") || type.equals("byte") || type.equals("char") || type.equals("short")) {
		    valuesMap.put(f.getName(), f.getFloat(o));
//		    G.asdf("g45tw34tw34tvadded to valuesmap, field: " + f.getName());
		}
	    }

//	    G.asdf("a4faewf made valuesMap of size:  " + valuesMap.size());
	    return valuesMap;
	}
    }

    /** true if at least one of these eras contains the date dateInt */
    public static boolean erasContainDate(List<Era> passingEras, int dateInt) {

	for (Era era : passingEras)
	    if (era.containsDate(dateInt))
		return true;
	return false;
    }


    public static interface EraDataRowInterface {
	/** this will package the variables into a string  -  the minDate and maxDate must be prepended before printing -   use \t to delineate */
	public String toOutputStringEDR();

	/** rename this to: assimilateFileLineValues. this is nice because it can be part of the interface -  a simpler alternative would be to put this mechanism in the constructor, but then there would be no contractual system for enforcing that a constructor exist with the given parameters & functionality */
	public void assimilateValues(String[] inputStrs) throws ParseException, IllegalArgumentException, IllegalAccessException;

//	public Map<
    }
}
