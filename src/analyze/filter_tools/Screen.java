package analyze.filter_tools;
import analyze.comparators.MyComparator;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.DataManager;
import utilities.G;
import analyze.conditions.EraCondition;
import analyze.conditions.VarCondition;
import analyze.conditions.VarCondition_AND_float;
import analyze.conditions.VarCondition_AND_int;
import analyze.conditions.VarCondition_OR_float;
import analyze.conditions.VarCondition_OR_int;
import categories.C;
import categories.category_earnings_response.Y;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Predicate;
import objects.Macro;
import objects.Macro_float;
import objects.Macro_int;
import objects.Stock;
import objects.prices.X;
import objects.profile.Profile_EraDataRow;
import supers.Era;
import supers.Var;
import supers.Var.Type;

/** only a Filter object can use a Screen.  uses pre-initialized maps and reads the screen text to build condition structures.  screens change per filter file upload */
public final class Screen {
    public static final String conditionsOutputDelimiter = G.semicolon + G.space;
    public static final String condSubDelimiter = G.comma + G.space;
    public static final String condSubSubDelimiter = G.space;
    public static final String filterSubsetInfoDelim = G.comma;
    public ProductionManager productionManager;
    public Integer rank1KeepAmount;
    public Integer rank2KeepAmount;
    public Class<X.vol> rank1MetricVarClass;
    public Class<X.vol> rank2MetricVarClass;
    public final Comparator rank1Comparator;
    public final Comparator rank2Comparator;
    public Integer pfMaxStocks;
    public int pfMaxStocksBuyPerDay;
    public final int pfInitialMoney;
    public final int pfMinHoldDays;
    public final boolean pfSellASAP;
    public final boolean pfCanRebuy;
    public boolean pfDoSimulate;
    boolean pfUseFees;
    public boolean pfUseSellDates;
    public Set<Integer> shrinkingValidSellDates;


    private String makeStr1(List<VarCondition> stockVarConditions) {
	String s = "";
	for (VarCondition vc : stockVarConditions) {
	    s += vc.getOutputString() + Screen.conditionsOutputDelimiter;
	}
	return s;
    }

    private String makeStr2(List<EraCondition> eraConditions) {

	String s = "";
	for (EraCondition ec : eraConditions) {
	    s += ec.getOutputString() + Screen.conditionsOutputDelimiter;
	}

	return s;
    }

    /** wtf, how do macroConditions work..... ????????????? */
    private String makeStr3(Map<VarCondition, Macro> macroConditions) {

	String s = "";
	for (VarCondition vc : macroConditions.keySet()) {
	    s += vc.getOutputString() + Screen.conditionsOutputDelimiter;
	}
	return s;
    }

    private void distributeHitThroughResultsHandlers(Hit hit, Map<Short, ResultsHandler> subresults, int dateInt, Map<Integer, Byte> date_weekDay__map) {

	short year = (short)G.getYearFromDateInt(dateInt);
	short weekDay = date_weekDay__map.get(dateInt);

	ResultsHandler rh_all = subresults.get(TemplateScreen.ALL);
	if (rh_all != null)
	    rh_all.input_a_passing_value(hit, this);
	ResultsHandler rh_year = subresults.get(year);
	if (rh_year != null)
	    rh_year.input_a_passing_value(hit, this);
	ResultsHandler rh_wkday = subresults.get(weekDay);
	if (rh_wkday != null)
	    rh_wkday.input_a_passing_value(hit, this);
    }


    public static class ThereMustBeTwoMacroVariablesWithSameName extends Exception {
	public ThereMustBeTwoMacroVariablesWithSameName() {
	}
    }

    public static class ThereMustBeEraDataRowFieldsWithNonUniqueNames extends Exception {
	public ThereMustBeEraDataRowFieldsWithNonUniqueNames() {
	}
    }

    final public static String d0 = G.space;
    final public static String d1 = G.comma;    //cedilla mark
    final public static String d2 = "$";
    final public static String dComment = "#";

    public DataManager data;


    List<Short> resultsDatespanDescriptors;
    Set<Class> yVarsClases;			    //containing the Var class of all vars we want to make averages,etc, of as dependent variables.  ave, pct>0, pct>1, n, n*ave

    Map<VarCondition, Macro> macroConditions;
    Map<VarCondition, Macro> macroConditionsSELL;
    List<EraCondition> eraConditions;

    List<VarCondition> stockVarConditions;
    List<VarCondition> cerVarConditions;

    public String screenParametersOutputString;

    private final int subsetOffset_date;
    private final int subsetFractionDenominator_date;
    private final int subsetOffset_co;
    private final int subsetFractionDenominator_co;

    public Set<String> specificTickers;

    /** Class is the yvar Var class  i think*/
    public Map<Class, Map<Short, ResultsHandler>> results;

    /** if true, then print a file -- at end of screen execution - with list of all the tickers and dates that passed the screen */
    boolean doPrintHits;

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public Screen(File filterFile, DataManager data) throws IOException, InvalidMetricName, MyComparator.BadComparatorSymbol, FilterFileException {

	this.data = data;
	stockVarConditions = new ArrayList();
	cerVarConditions = new ArrayList();
	eraConditions = new ArrayList();
	macroConditions = new LinkedHashMap();
	macroConditionsSELL = new LinkedHashMap();

	List<String> lines = getPreparedLines(filterFile);

	String[] subsetInfoLineAr_date = lines.get(0).split("\\" + Screen.filterSubsetInfoDelim);
	subsetOffset_date = Integer.parseInt(subsetInfoLineAr_date[0].trim());
	subsetFractionDenominator_date = Integer.parseInt(subsetInfoLineAr_date[1].trim());

	String[] subsetInfoLineAr_co = lines.get(1).split("\\" + Screen.filterSubsetInfoDelim);
	subsetOffset_co = Integer.parseInt(subsetInfoLineAr_co[0].trim());
	subsetFractionDenominator_co = Math.max(1, Integer.parseInt(subsetInfoLineAr_co[1].trim()));

	specificTickers = new HashSet(Arrays.asList(lines.get(2).replaceAll("\\s", "").split(",")));
	if (specificTickers.contains("all_tickers") || specificTickers.isEmpty())
	    specificTickers = null;

	doPrintHits = lines.get(3).toLowerCase().contains("doprinthitstofile");
	productionManager = ProductionManager.parseInput(lines.get(4));
	resultsDatespanDescriptors = get_resultsDatespans(lines.get(5));
	yVarsClases = get_yVarClasses(lines.get(6));

	String[] rank1LineParameters = lines.get(7).split(",");
	rank1KeepAmount = G.parse_Integer(rank1LineParameters[0]);
	rank1MetricVarClass = getVarClass(rank1LineParameters[1].trim());
	Boolean doRank1Increasing = rank1LineParameters[2].contains("increasing");

	String[] rank2LineParameters = lines.get(8).split(",");
	rank2KeepAmount = G.parse_Integer(rank2LineParameters[0]);
	rank2MetricVarClass = getVarClass(rank2LineParameters[1].trim());
	Boolean doRank2Increasing = rank2LineParameters[2].contains("increasing");

	String[] portfolioDetails = lines.get(9).split(",");
	pfMaxStocks = G.parse_Integer(portfolioDetails[0].trim());
	pfMaxStocksBuyPerDay = Integer.parseInt(portfolioDetails[1].trim());
	pfInitialMoney = Integer.parseInt(portfolioDetails[2].trim());
	pfMinHoldDays = Integer.parseInt(portfolioDetails[3].trim());
	pfSellASAP = portfolioDetails[4].toLowerCase().contains("dosellasap");
	pfCanRebuy = portfolioDetails[5].toLowerCase().contains("canrebuy");
	pfUseFees = portfolioDetails[6].toLowerCase().contains("dousefees");
	pfUseSellDates = portfolioDetails[7].toLowerCase().contains("douseselldates");


	for (String conditionDetailsLine : lines.subList(10, lines.size())) {
	    ConditionDetails conditionDetails = make_conditionDetails(conditionDetailsLine);//new ConditionDetails(conditionDetailsLine, demoStock, var__name_clas__map);

	    //determine if var, era, or macro
	    if (data.meta.metricIs_var(conditionDetails.metricName) && !data.meta.metricIs_cer(conditionDetails.metricName)) {
		stockVarConditions.add(VarCondition.create_new_varCondition(conditionDetails));
	    } else if (data.meta.metricIs_cer(conditionDetails.metricName)) {
		cerVarConditions.add(VarCondition.create_new_varCondition(conditionDetails));
	    } else if (data.meta.metricIs_era(conditionDetails.metricName)) {
		eraConditions.add(EraCondition.create_new_varCondition(conditionDetails));
	    } else if (data.meta.metricIs_macro(conditionDetails.metricName)) {
		VarCondition mc = VarCondition.create_new_varCondition(conditionDetails);
		if (conditionDetails.isSellMacro)
		    macroConditionsSELL.put(mc, data.macrometric_macro__map.get(mc.metric));
		else
		    macroConditions.put(mc, data.macrometric_macro__map.get(mc.metric));
	    } else
		throw new InvalidMetricName(conditionDetails.metricName);
	}

	productionManager.setRankingDetails(this);

	if (rank1KeepAmount != null) {
	    if (rank1MetricVarClass == null) {
		G.asdf("bad rank1 metric: " + rank1LineParameters[1].trim());
		throw new InvalidMetricName(rank1LineParameters[1].trim());
	    }
	    if (doRank1Increasing)
		rank1Comparator = (Comparator<Hit>)new Comparator<Hit>() {
		public int compare(Hit h1, Hit h2) {
		    return Float.compare(h1.rank1Value, h2.rank1Value);
		}
	    };
	    else
		rank1Comparator = (Comparator<Hit>)new Comparator<Hit>() {
		public int compare(Hit h1, Hit h2) {
		    return Float.compare(h2.rank1Value, h1.rank1Value);
		}
	    };
	} else {
	    rank1Comparator = null;
	    rank1MetricVarClass = null;
	}

	if (rank2KeepAmount != null) {
	    if (rank2MetricVarClass == null) {
		G.asdf("bad rank2 metric: " + rank2LineParameters[1].trim());
		throw new InvalidMetricName(rank2LineParameters[1].trim());
	    }
	    if (doRank1Increasing)
		rank2Comparator = (Comparator<Hit>)new Comparator<Hit>() {
		public int compare(Hit h1, Hit h2) {
		    return Float.compare(h1.rank2Value, h2.rank2Value);
		}
	    };
	    else
		rank2Comparator = (Comparator<Hit>)new Comparator<Hit>() {
		public int compare(Hit h1, Hit h2) {
		    return Float.compare(h2.rank2Value, h1.rank2Value);
		}
	    };
	} else {
	    rank2Comparator = null;
	    rank2MetricVarClass = null;
	}

	for (Map.Entry<VarCondition, Macro> entry : macroConditions.entrySet()) {
	    VarCondition mc = entry.getKey();
	    Macro m = entry.getValue();
	    mc.setArray(m);
	}
	for (Map.Entry<VarCondition, Macro> entry : macroConditionsSELL.entrySet()) {
	    VarCondition mc = entry.getKey();
	    Macro m = entry.getValue();
	    mc.setArray(m);
	}

	String productionOrResearch = productionManager.getClass().equals(ProductionManager_Active.class) ? "P" : "R";


	screenParametersOutputString = productionOrResearch + ", "
		+ (rank1Comparator == null ? "" : "rank1: " + rank1KeepAmount + " " + G.getClassShortName(rank1MetricVarClass) + (doRank1Increasing ? " increasing, " : " decreasing, "))
		+ (rank2Comparator == null ? "" : "rank2: " + rank2KeepAmount + " " + G.getClassShortName(rank2MetricVarClass) + (doRank2Increasing ? " increasing, " : " decreasing, "))
		+ (subsetFractionDenominator_date > 1 ? (", dateSub: (" + subsetOffset_date + ", " + subsetFractionDenominator_date + "), ") : "")
		+ (subsetFractionDenominator_co > 1 ? (", coSub: (" + subsetOffset_co + ", " + subsetFractionDenominator_co + "), ") : "")
		+ (specificTickers == null ? "" : ("only: " + specificTickers + ", "))
		+ makeStr1(stockVarConditions) + makeStr1(cerVarConditions) + makeStr2(eraConditions) + makeStr3(macroConditions);

	System.out.print("per filt: " + screenParametersOutputString);

	pfDoSimulate = pfMaxStocks != null && pfMaxStocks > 0;
	if (pfDoSimulate) {

	    if (pfMinHoldDays < 1)
		throw new FilterFileException("bad minHoldDays: " + pfMinHoldDays);

	    yVarsClases = new LinkedHashSet();
	    yVarsClases.add(futChPct_pfolio.class);

	    if (pfDoSimulate && !pfCanRebuy) //then make max stocks equal to number of stocks in Stocks list
		pfMaxStocks = Math.min(data.stocks.size(), pfMaxStocks);
	    System.out.print("\nprtfolio: ["
		    + pfMaxStocks + ", "
		    + pfMaxStocksBuyPerDay + ", "
		    + pfInitialMoney + ", "
		    + pfMinHoldDays + ", "
		    + pfSellASAP + ", "
		    + pfCanRebuy + ", "
		    + pfUseFees + ", "
		    + pfUseSellDates + "] (maxStocks, perDay, init$, minHoldDays, sellASAP?, allowDD?, useFees?, onlySellOnValidSellDates?)." + (macroConditionsSELL.isEmpty() ? "" : " <sell> " + makeStr3(macroConditionsSELL)));
	}
    }

    public Set<Class> get_yVarClasses(String yVarNamesLine) {


	String str = G.removeAllWhitespace(yVarNamesLine);
	String[] ar = str.split("\\" + Screen.d1);
	List<String> list = Arrays.asList(ar);
	Set<String> yVar_metrics = new LinkedHashSet(list);

	return getClassSet(yVar_metrics);
    }

    private Class getVarClass(String varName) {
	return data.meta.allvars__name_class__map.get(varName);
    }

    private Set<Class> getClassSet(Set<String> strSet) {
	Set<Class> classSet = new LinkedHashSet(strSet.size());
	for (String ySt : strSet)
	    classSet.add(getVarClass(ySt));
	return classSet;
    }

    @SuppressWarnings({"element-type-mismatch", "null"})
    public void runFilter() throws ParseException {

	Set<Integer> shrinkingValidBuyDates = Stock.getDatesUniverse(data.minDate);
	shrinkingValidSellDates = Stock.getDatesUniverse(data.minDate);

	shrinkingValidBuyDates.retainAll(data.validDates);
	shrinkingValidSellDates.retainAll(data.validDates);

	if (subsetFractionDenominator_date > 1)
	    shrinkingValidBuyDates = G.getSubset(shrinkingValidBuyDates, subsetOffset_date, subsetFractionDenominator_date);


	//filter macros  (BUY macros)!!!!!!!!!!!!!!
	for (Map.Entry<VarCondition, Macro> entry : macroConditions.entrySet()) {
	    VarCondition mc = entry.getKey();
	    Macro m = entry.getValue();

	    if (m.getClass().equals(Macro_float.class)) {

		float[] data_array = null;
		if (mc.getClass().equals(VarCondition_AND_float.class))
		    data_array = ((VarCondition_AND_float)mc).ar;
		if (mc.getClass().equals(VarCondition_OR_float.class))
		    data_array = ((VarCondition_OR_float)mc).ar;

		int[] dates_array = m.dates;

		Set<Integer> goodDates = new LinkedHashSet(dates_array.length);

		for (int i = 0; i < data_array.length; i++) //nullpe here
		    if (mc.isMet(i))
			goodDates.add(dates_array[i]);
//		G.asdf();
//		G.asdf("num goodDates: " + goodDates.size());
//		G.asdf("num shrinkingValidBuyDates BEFORE: " + shrinkingValidBuyDates.size());

		shrinkingValidBuyDates.retainAll(goodDates);
//		G.asdf("num shrinkingValidBuyDates AFTER: " + shrinkingValidBuyDates.size());

	    } else if (m.getClass().equals(Macro_int.class)) {

		int[] data_array = null;
		if (mc.getClass().equals(VarCondition_AND_int.class))
		    data_array = ((VarCondition_AND_int)mc).ar;
		if (mc.getClass().equals(VarCondition_OR_int.class))
		    data_array = ((VarCondition_OR_int)mc).ar;

		int[] dates_array = m.dates;

		Set<Integer> goodDates = new LinkedHashSet(dates_array.length);

		for (int i = 0; i < data_array.length; i++) //nullpe here
		    if (mc.isMet(i))
			goodDates.add(dates_array[i]);

		shrinkingValidBuyDates.retainAll(goodDates);
	    }
	}

	//filter macros  (SELL macros)!!!!!!!!!!!!!!
	for (Map.Entry<VarCondition, Macro> entry : macroConditionsSELL.entrySet()) {
	    VarCondition mc = entry.getKey();
	    Macro m = entry.getValue();

	    if (m.getClass().equals(Macro_float.class)) {

		float[] data_array = null;
		if (mc.getClass().equals(VarCondition_AND_float.class))
		    data_array = ((VarCondition_AND_float)mc).ar;
		if (mc.getClass().equals(VarCondition_OR_float.class))
		    data_array = ((VarCondition_OR_float)mc).ar;

		int[] dates_array = m.dates;

		Set<Integer> goodDates = new LinkedHashSet(dates_array.length);

		for (int i = 0; i < data_array.length; i++) //nullpe here  //NPE HERE FOR REAL
		    if (mc.isMet(i))
			goodDates.add(dates_array[i]);

		shrinkingValidSellDates.retainAll(goodDates);

	    } else if (m.getClass().equals(Macro_int.class)) {

		int[] data_array = null;
		if (mc.getClass().equals(VarCondition_AND_int.class))
		    data_array = ((VarCondition_AND_int)mc).ar;
		if (mc.getClass().equals(VarCondition_OR_int.class))
		    data_array = ((VarCondition_OR_int)mc).ar;

		int[] dates_array = m.dates;

		Set<Integer> goodDates = new LinkedHashSet(dates_array.length);

		for (int i = 0; i < data_array.length; i++) //nullpe here
		    if (mc.isMet(i))
			goodDates.add(dates_array[i]);

		shrinkingValidSellDates.retainAll(goodDates);
	    }
	}


	/****************** cers prep ***********************/
	/** value (dates set) is null if no conditions (not screening for cer stuff).  value is empty is we ARE screening, and NO dates passed the test! */
	Map<String, Set<Integer>> map__catFullName_validDates = null;
	if (data.x_objects_to_use.contains(objects.profile.X.class) && data.x_objects_to_use.contains(objects.earnings.X.class)) {
	    //go throuh all cers.y objects ........   make variable (changes through screens) map of valid dates for that category:
	    //	 Map<cattype, map<catname, set<dateInts>>
	    //how to get this from filter file?
	    //make a set of dates for EVERY cateogry!
	    //make list of just cer vars!
	    map__catFullName_validDates = new LinkedHashMap(data.cers.categoryFullName_Y__map.size());

	    for (Map.Entry<String, Y> entry : data.cers.categoryFullName_Y__map.entrySet()) {
		String cfn = entry.getKey();
		Y cerY = entry.getValue();

		Map<Integer, Integer> shrinkingValidDates__date_i_map;				    // a different validdates per cfn?  right.
		if (cerVarConditions.isEmpty()) {
		    shrinkingValidDates__date_i_map = null;
		} else {
		    shrinkingValidDates__date_i_map = new LinkedHashMap(cerY.date_i__map);

		    for (VarCondition vc : cerVarConditions) {
			vc.setDataArray(cerY);					    //temporarily initialize varconditions with data arrays -- for this cateogry only!!!

			//we should be SHRINKING the validdates set!  validdaets PER CFN! 
			for (Iterator<Map.Entry<Integer, Integer>> iter = shrinkingValidDates__date_i_map.entrySet().iterator(); iter.hasNext();) {
			    Map.Entry<Integer, Integer> date_i_entry = iter.next();
			    int i = date_i_entry.getValue();

			    if (!vc.isMet(i))
				iter.remove();
			}
			vc.clearDataArray();		    //clear varconditions data arrays to make sure it's clean for the next category (cfn)
		    }
		}
		map__catFullName_validDates.put(cfn, (shrinkingValidDates__date_i_map == null ? null : shrinkingValidDates__date_i_map.keySet()));		//nullpe here
	    }
	}


	//initialize results maps (outermap: key per yvar; map to contain results for all, specific years, specific weekdays, months, etc)
	List<Short> periodDescriptors = resultsDatespanDescriptors;//  null;// = Arrays.asList(new Short[]{ALL, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, WeekDayInt.MONDAY.ordinal(), WeekDayInt.TUESDAY.ordinal(), WeekDayInt.WEDNESDAY.ordinal(), WeekDayInt.THURSDAY.ordinal(), WeekDayInt.FRIDAY.ordinal()});
	float originalNumberOfDaysInTheExecutionTimeSpan = shrinkingValidBuyDates.size();
	results = new LinkedHashMap(stockVarConditions.size());
	for (Class yc : yVarsClases) {
	    Map<Short, ResultsHandler> subresults = new LinkedHashMap(periodDescriptors.size());
	    for (Short descriptor : periodDescriptors)
		subresults.put(descriptor, new ResultsHandler(yc, descriptor, originalNumberOfDaysInTheExecutionTimeSpan));
	    results.put(yc, subresults);
	}

	System.out.print(".");
	if (pfDoSimulate) {
	    Set<Integer> dates = Stock.getDatesUniverse(data.minDate);
	    dates.retainAll(data.validDates);
	    Map<Integer, Byte> map__date_weekday = get__date_weekDay__map(dates);

	    for (Integer dateInt : dates) {
		for (Class yc : yVarsClases) {
		    Map<Short, ResultsHandler> subresults = results.get(yc);

		    short year = (short)G.getYearFromDateInt(dateInt);
		    short weekDay = map__date_weekday.get(dateInt);

		    ResultsHandler rh_all = subresults.get(TemplateScreen.ALL);
		    if (rh_all != null)
			rh_all.datesInPeriod.add(dateInt);
		    ResultsHandler rh_year = subresults.get(year);
		    if (rh_year != null)
			rh_year.datesInPeriod.add(dateInt);
		    ResultsHandler rh_wkday = subresults.get(weekDay);
		    if (rh_wkday != null)
			rh_wkday.datesInPeriod.add(dateInt);
		}
	    }
	}

	/**************************************************************************************/
	/**************************************************************************************/
	/************************************** STOCKS ****************************************/
	System.out.print(".");
	for (int si = subsetOffset_co; si < data.stocks.size(); si += subsetFractionDenominator_co) {
	    Stock stock = data.stocks.get(si);

	    if (specificTickers != null && !specificTickers.contains(stock.name))
		continue;

	    Map<Integer, Integer> date_i__map = new LinkedHashMap(stock.date_i__map);	//copy of stock's date_i__map.  macros and eras will whittle down these dates.  

	    Map<Integer, Byte> date_weekDay__map = get__date_weekDay__map(date_i__map.keySet());

	    //adjust dates for macro results
	    date_i__map = removeDatesFromMapWhichArentInUniverse(date_i__map, shrinkingValidBuyDates);

	    //eras!
	    for (EraCondition ec : eraConditions) {
		List<Era> passingEras = ec.getPassingEras(stock);
		removeDatesFromMapWhichArentInAnyOfTheseEras(date_i__map, passingEras);
	    }

	    //adjust dates for cer  results
	    if (data.x_objects_to_use.contains(objects.profile.X.class) && data.x_objects_to_use.contains(objects.earnings.X.class)) {
		Map<C.CategoryType, String> categories = ((Profile_EraDataRow)stock.profile_X.eras.get(0).eraDataRow).categories;
		for (C.CategoryType ct : C.catTypesToAnalyze) {
		    String cfn = C.getCategoryFullName(ct, categories.get(ct));
		    date_i__map = removeDatesFromMapWhichArentInUniverse(date_i__map, map__catFullName_validDates.get(cfn));
		}
	    }
	    //Vars ...... ?
	    //initialize varconditions with data arrays
	    for (VarCondition vc : stockVarConditions)
		vc.setArray(stock);

	    for (Map.Entry<Integer, Integer> entry : date_i__map.entrySet()) {
		int dateInt = entry.getKey();
		int i = entry.getValue();

		boolean allConditionsWereMet = true;
		for (VarCondition vc : stockVarConditions) {
		    if (!vc.isMet(i)) {
			allConditionsWereMet = false;
			break;
		    }
		}
		if (allConditionsWereMet) {
		    float rank1Value = G.null_float;
		    if (rank1MetricVarClass != null)
			rank1Value = stock.vars.get(rank1MetricVarClass).ar_float[i];

		    float rank2Value = G.null_float;
		    if (rank2MetricVarClass != null)
			rank2Value = stock.vars.get(rank2MetricVarClass).ar_float[i];
		    float close = stock.vars.get(objects.prices.X.close.class).ar_float[i];			//for production, just use date as yasd ofiashdofiahsdf;oiajsd fuck, can't cos date is int.
		    float vol = stock.vars.get(objects.prices.X.vol.class).ar_float[i];			//for production, just use date as yasd ofiashdofiahsdf;oiajsd fuck, can't cos date is int.

		    productionManager.manage(rank1Value, rank2Value, stock.name, dateInt, vol, close, stock);

		    for (Class yc : yVarsClases) {
			Map<Short, ResultsHandler> subresults = results.get(yc);

			if (yc.equals(futChPct_pfolio.class)) { //PORTFOLIO ONLY

			    Hit hit = new Hit(G.dummyNonNull_float, rank1Value, rank2Value, dateInt, vol, close, stock);
			    distributeHitThroughResultsHandlers(hit, subresults, dateInt, date_weekDay__map);

			} else {
			    float yValue = stock.vars.get(yc).ar_float[i];	    //NOT portfolio		

			    if (yValue != G.null_float) {
				Hit hit = new Hit(yValue, rank1Value, rank2Value, dateInt, vol, close, stock);
				distributeHitThroughResultsHandlers(hit, subresults, dateInt, date_weekDay__map);
			    }
			}
		    }
		}
	    }
	}
	/*********************************************  RANK & SHRINK -- 1st round shrinking,  2nd round hits sorting and shrinking **************************************/
	for (Class yc : yVarsClases) {
	    Map<Short, ResultsHandler> subresults = results.get(yc);
	    for (ResultsHandler rh : subresults.values()) {
		if (rank1Comparator != null)
		    rh.shrink1(this);
		if (rank2Comparator != null)
		    rh.rankAndShrink2(this);
	    }
	}	//now we've gone through all the tickers!   results is full of untabulated data!	}
    }

    private Map<Integer, Byte> get__date_weekDay__map(Set<Integer> dates) throws ParseException {

	Map<Integer, Byte> date_weekDay__map = new LinkedHashMap(dates.size());
	for (Integer dateInt : dates)
	    date_weekDay__map.put(dateInt, G.getWeekDay(dateInt));

	return date_weekDay__map;
    }

    private Map<Integer, Integer> removeDatesFromMapWhichArentInUniverse(Map<Integer, Integer> date_i__map, Set<Integer> datesUniverse) {
	if (datesUniverse == null)
	    return date_i__map;
	if (datesUniverse.isEmpty())
	    return new LinkedHashMap();
	date_i__map.entrySet().removeIf(new Predicate<Map.Entry<Integer, Integer>>() {
	    public boolean test(Map.Entry<Integer, Integer> t) {
		return !datesUniverse.contains(t.getKey());
	    }
	});
	return date_i__map;
    }

    private void removeDatesFromMapWhichArentInAnyOfTheseEras(Map<Integer, Integer> date_i__map, List<Era> passingEras) {
	date_i__map.entrySet().removeIf(new Predicate<Map.Entry<Integer, Integer>>() {
	    public boolean test(Map.Entry<Integer, Integer> t) {
		return !Era.erasContainDate(passingEras, t.getKey());
	    }
	});
    }


    private ConditionDetails make_conditionDetails(String conditionDetailsLine) throws InvalidMetricName, FilterFileException {
	boolean logical_operator_is_OR = false;
	boolean isSellMacro = false;

	if (conditionDetailsLine.contains("<sell>")) {
	    conditionDetailsLine = conditionDetailsLine.replace("<sell>", "");
	    isSellMacro = true;
	}

	if (conditionDetailsLine.contains("$OR")) {
	    conditionDetailsLine = conditionDetailsLine.replaceAll("\\$OR", "\\$");
	    logical_operator_is_OR = true;
	}

	String[] ar = conditionDetailsLine.split("\\" + d2);
	String metricName = ar[0].trim();
	String originalFullName = metricName;
	C.CategoryType categoryType = null;

	if (metricName.contains(d0)) {
	    String[] ar2 = metricName.split("\\" + d0);
	    metricName = ar2[1];
	    categoryType = C.CategoryType.parse(ar2[0]);
	    if (!C.catTypesToAnalyze.contains(categoryType)){
		G.asdf(metricName);
		G.asdf(originalFullName);
		G.asdf(categoryType);
		G.asdf(C.catTypesToAnalyze);
		throw new FilterFileException("invalid category in : " + originalFullName + ". valid types: " + C.catTypesToAnalyze);
	    }
	}


	List<SubconditionText> subconditionStrs = ConditionDetails.getConditionDetailsStrs(Arrays.asList(ar).subList(1, ar.length));

	Class myclass = null;
	Var.Type arrayDataType = null;

	if (data.meta.metricIs_var(metricName)) {
	    myclass = data.meta.getVarClass(metricName);
	    if (!data.meta.metricIs_cer(metricName)) {			//normal var - could be caTAveCOmp
		Var var = data.meta.demoStock.vars.get(myclass);
		arrayDataType = var.arrayDataType;
		if (categoryType != null && !var.isForCatAveComparison)
		    throw new InvalidMetricName(originalFullName);
	    } else if (data.meta.metricIs_cer(metricName)) {
		Var var = data.meta.demoY.vars.get(myclass);
		arrayDataType = var.arrayDataType;
	    }
	} else if (data.meta.metricIs_era(metricName)) {
	    myclass = data.meta.alledrs__fieldname_edrclass__map.get(metricName);
	    arrayDataType = Type.FLOAT;		//all era data use float
	} else if (data.meta.metricIs_macro(metricName)) {
	    Macro m = data.macrometric_macro__map.get(metricName);
	    if (m.getClass().equals(Macro_float.class))
		arrayDataType = Type.FLOAT;
	    else if (m.getClass().equals(Macro_int.class))
		arrayDataType = Type.INTEGER;
	} else
	    throw new InvalidMetricName(metricName);


	return new ConditionDetails(metricName, categoryType, subconditionStrs, myclass, arrayDataType, logical_operator_is_OR, isSellMacro);
    }

    /** removes comments and repeated whitespace and blank lines */
    private List<String> getPreparedLines(File filterFile) throws IOException {
//	System.out.println("fawefawef " + filterFile.toPath());
	List<String> lines = new ArrayList();

	try (BufferedReader br = new BufferedReader(new FileReader(filterFile))) {
	    String line;
	    while ((line = br.readLine()) != null) {
		line = line.trim();
		if (!line.isEmpty())
		    lines.add(line);
	    }
	}

//	Files.readAllLines(filterFile.toPath(), StandardCharsets.UTF_8);

	return removeCommentsAndRepeatedWhitespaceAndBlankLines(lines);
    }

    public static List<String> removeCommentsAndRepeatedWhitespaceAndBlankLines(List<String> lines) {
	List<String> newlines = new ArrayList(lines.size());

	for (String line : lines) {

	    line = removeCommentsAndRepeatedWhitespace(line).trim();
	    if (!line.isEmpty())
		newlines.add(line);
	}
	return newlines;
    }


    /** remove comments and allow single spaces as only whitespace */
    public static String removeCommentsAndRepeatedWhitespace(String line) {
	String[] ar = line.split("\\" + Screen.dComment);
	if (ar.length == 0)
	    return "";
	line = ar[0];
	line = G.singleSpacesOnly(line);
	return line;
    }

    public static List<Short> get_resultsDatespans(String datespansLine) {

	String[] strs = G.removeAllWhitespace(datespansLine).split("\\" + d1);

	List<Short> datespans = new ArrayList(strs.length);

	for (String str : strs) {
	    str = str.toLowerCase();
	    if (str.contains("mon"))
		datespans.add((short)WeekDayInt.MONDAY.ordinal());
	    else if (str.contains("tues"))
		datespans.add((short)WeekDayInt.TUESDAY.ordinal());
	    else if (str.contains("wed"))
		datespans.add((short)WeekDayInt.WEDNESDAY.ordinal());
	    else if (str.contains("thu"))
		datespans.add((short)WeekDayInt.THURSDAY.ordinal());
	    else if (str.contains("fri"))
		datespans.add((short)WeekDayInt.FRIDAY.ordinal());
	    else
		datespans.add(Short.parseShort(str));
	}
	return datespans;
    }

    public static class InvalidMetricName extends Exception {

	public InvalidMetricName(String metricName) {
	    super(metricName);
	}
    }


    public enum WeekDayInt {
	SUNDAY,
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY
    }

    public static Map<Integer, WeekDayInt> weekDaysMap = getWeekDaysMap();

    private static Map<Integer, WeekDayInt> getWeekDaysMap() {
	Map<Integer, WeekDayInt> weekDaysMap_ = new LinkedHashMap(WeekDayInt.values().length);
	for (WeekDayInt wd : WeekDayInt.values())
	    weekDaysMap_.put(wd.ordinal(), wd);
	return weekDaysMap_;
    }

}
