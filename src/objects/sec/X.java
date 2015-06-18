package objects.sec;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import objects.Stock;
import utilities.G;
import static utilities.G.get;
import supers.SuperX;
import supers.Var;

public final class X extends SuperX implements supers.SuperXInterface, supers.XInterface_uses_Vars {

    private Map<Integer, List<SECReport>> map__cookedDate_report;	///* contains list because one day might have lots of reports released.  want them lumped per day */
    objects.prices.X prices_X;

    public X() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(null);
	fill_emptyVarsMap();
    }

    public X(String ticker) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(ticker, null);
	fill_emptyVarsMap();
    }

    public void calculate_data_origination(objects.prices.X prices_X) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	setAttributes(prices_X);
	fill_data__origination(this);
    }

    public void setAttributes(objects.prices.X prices_X) throws IOException, ParseException, G.No_DiskData_Exception {
	this.prices_X = prices_X;
	List<SECReport> raw = SECReport.readFromDisk(ticker);	//i need the Var constructors to see this.  but don't want to make it an X attribute cos i don't want to be dragging it around during analysis.  and don't want to have to deal w/ deleting it, and then having clutter of empty attribute.  so, lets just pass it to each constructor
	this.map__cookedDate_report = convert_RawReportsList_to_cookedDatesMap(raw);
    }

    @Override
    public void setXFiles() throws IOException {
	setXFile_vars();
    }

    @Override
    public void setXFile_vars() throws IOException {
	varsFile = G.newChildTickerFile(G.XSECReports_vars, ticker);
    }

    @Override
    public void fill_data__origination(SuperX x) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	fill_vars(x, length(prices_X));
    }

    @Override
    public void fill_varClasses() {
	varClasses.add(Sec_NumReports.class);
	varClasses.add(Sec_TypesReported.class);	    //	    varClasses.add(Sec_numDaysSince.class); //	    varClasses.add(Sec_numDaysSince<specific report>.class);
    }

    /* things needed for X attributes */

    public Map<Integer, List<SECReport>> convert_RawReportsList_to_cookedDatesMap(List<SECReport> rawlist) throws ParseException {

	Map<Integer, List<SECReport>> map = new LinkedHashMap();

	//for each report in rawlist, determine cooked date
	//put in map for this date.  (if date exists, add to existing list.  else make new list, put for that date) 
	for (SECReport report : rawlist) {
	    int cookedDate = X.getCookedDate(report);

	    if (map.containsKey(cookedDate))
		map.get(cookedDate).add(report);
	    else
		map.put(cookedDate, new ArrayList(Arrays.asList(new SECReport[]{report})));
	}
	return map;
    }

    private static int getCookedDate(SECReport report) throws ParseException {

	int hour = Integer.parseInt(report.time.substring(0, 2));

	if (hour < 16) {
	    return Integer.parseInt(report.date);
	} else {
	    return Integer.parseInt(G.getNextWeekdayDate(report.date));
	}
    }

    /* things needed for calculating arrays */

    @Override @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public void calculate_vars_origination(SuperX x, boolean doUpdateForCurrentPrices) {
	if (doUpdateForCurrentPrices)
	    return;
//	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

	//can we calculate all the data arrays together here?
	//yes but it will make it sloppy :(  not nearly as tidy and maintainable as having a <public void calculateData(SuperX x) > within each constructor.  actually this might be clearer anyway.  much less code!!!.  but i think it's worth it for execution speed.  so we're not repeatedly searching for specific dates in large maps
	//listing these out here so we're not accessing a map for every index i.  probably a tiny time difference but whatever
	float[] _NumReports = x.vars.get(X.Sec_NumReports.class).ar_float;
	String[] _TypesReported = x.vars.get(X.Sec_TypesReported.class).ar_String;

	int[] date = date(prices_X);

//	for (int i = 0; i < Math.min(iterMax, length(prices_X)); i++) {
	for (int i = 0; i < length(prices_X); i++) {
	    try {
		int dateInt = get(date, i);
		List<SECReport> list;
		list = map__cookedDate_report.get(dateInt);

		_NumReports[i] = list == null ? 0 : list.size();
		_TypesReported[i] = getAllUniqueTypes_tabDelimited(list, G.SecsStringDelimiter);
	    } catch (G.My_null_exception ex) {
	    }
	}
    }

    private String getAllUniqueTypes_tabDelimited(List<SECReport> list, String delimiter) {

	if (list == null) return "";

	Set<String> types = new HashSet();
	for (SECReport r : list)
	    types.add(r.type);

	StringBuilder sb = new StringBuilder();
	for (String s : types) {
	    sb.append(s);
	    sb.append(delimiter);
	}
	return sb.toString().trim();
    }

    public static class Sec_NumReports extends Var {
	public Sec_NumReports() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

    }

    public static class Sec_TypesReported extends Var {
	/**(filter program will need a "contains" function for checking list elements)*/
	public Sec_TypesReported() {
	    arrayDataType = Var.Type.STRING;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    @Override //empty - not needed.  sec data not adjusted for current prices
    public void setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(Stock stock) {
    }

}
