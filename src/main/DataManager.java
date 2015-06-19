package main;
import utilities.G;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import analyze.filter_tools.Screen;
import analyze.filter_tools.Screen.ThereMustBeTwoMacroVariablesWithSameName;
import categories.C;
import static categories.C.lowercaseExceptFirstLetter;
import categories.CatAves;
import categories.Seasons;
import categories.category_earnings_response.CategoryEarningsResponses;
import categories.category_earnings_response.Y;
import static downloaders.Fetch___Prices_Current.download_currentPrices;
import downloaders.regular_other.Replace_Weather;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import objects.Macro;
import objects.Macro_float;
import objects.Macro_int;
import objects.Stock;
import objects.Weather;
import objects.earnings.Earnings_EraDataRow;
import objects.prices.CurrentPrices;
import objects.prices.Prices;
import objects.profile.Profile_EraDataRow;
import objects.short_interest.ShortInterest;
import org.apache.commons.io.FileUtils;
import supers.Era;
import supers.Var;

public class DataManager {
//    private Class[] xClassesToExclude;
    private C.CategoryType[] excludedCatTypes;
    private File limitedTickersFile = null;
    public TreeSet<Integer> validDates;
    public static Set<Class> catAveableVars = null;


    /** WTF Why doesn't this work :( :( :(   its not saving time or memory */
    void setCatAveableVars(Class... classes) {
	catAveableVars = new LinkedHashSet();
	for (Class c : classes)
	    catAveableVars.add(c);
    }


    public enum From {
	RAW,
	XFILES;
    }

    public int minDate;
    private String min;
    private String max;
    private String[] limitedTickers;
    private Integer subsetFractionDenominator, subsetOffSet;
    public Set<Class> x_objects_to_use;

    public List<Stock> stocks;

    public Map<String, Profile_EraDataRow> profiles;
    public Map<String, Set<String>> categoryFullName_tickers__map;

    public Map<String, Set<Stock>> catFullName_stocks__map;

    /** is catAves null if only one member stock ? */
    public Map<C.CategoryType, Map<String, CatAves>> catType__catName_catAves__map;

    public Map<String, List<Earnings_EraDataRow>> ticker_earnings__map;

    public CategoryEarningsResponses cers;

    public Map<String, Macro> macrometric_macro__map;
    public TreeMap<String, Macro> map__macroname_macro;

    public MetaData meta;

    String parametersOutputString() {
	return "using " + stocks.size() + " tickers, "
		+ "from " + min + " to " + max + ", "
		+ (limitedTickers == null ? "" : (limitedTickersFile == null ? ("only: " + Arrays.toString(limitedTickers) + ", ") : ("only ticks in file: " + limitedTickersFile.getName() + ", ")))
		+ "startingdate: " + minDate + ". subset offset, fraction: (" + subsetOffSet + ", " + subsetFractionDenominator + "). using X's: " + x_objects_to_use + ".  excluding catTypes: " + Arrays.toString(excludedCatTypes);
    }


    public DataManager() throws IOException, ParseException {
	this.minDate = G.minimumDateInt;
	this.min = G.minimumTicker;
	this.max = G.maximumTicker;
	this.limitedTickers = (String[])null;

	//DONT EDIT THIS!  use datamanager's removal tool to futz with which x objects we're loading
	x_objects_to_use = new LinkedHashSet();
//	x_objects_to_use.add(objects.prices.X.class);
//	x_objects_to_use.add(objects.finance.X.class);
//	x_objects_to_use.add(objects.profile.X.class);
//	x_objects_to_use.add(objects.people.X.class);
//	x_objects_to_use.add(objects.sec.X.class);
//	x_objects_to_use.add(objects.short_interest.X.class);
//	x_objects_to_use.add(objects.splits.X.class);
//	x_objects_to_use.add(objects.news.X.class);
//	x_objects_to_use.add(objects.earnings.X.class);

	C.catTypesToAnalyze = new LinkedHashSet();
	for (C.CategoryType ct : C.CategoryType.values()) {
	    C.catTypesToAnalyze.add(ct);
	}

//	validDates = Prices.getValidDates(minDate, "AA", "AAPL", "AAON");
	try {
	    validDates = Prices.getValidDates(minDate, "AA", "AAPL", "AAON");
	} catch (G.No_DiskData_Exception ex) {
	    ex.printStackTrace();
	    G.asdf("missing prices for aa or aapl or aaon.  necessary for building validdates set");
	    System.exit(0);
	}
    }

    void setMinMaxTickers(String minTicker, String maxTicker) {
	this.min = minTicker;
	this.max = maxTicker;
    }

    void setLimitedTickers(String... limitedTickers) {
	this.limitedTickers = limitedTickers;
    }

    void setLimitedTickers(File dir) {
	limitedTickersFile = dir;
	Set<String> tickers = G.getTickers(dir);
	this.limitedTickers = tickers.toArray(new String[tickers.size()]);
    }

    void setMinDate(int minDate) {
	if (minDate > 20151212) {
	    G.asdf("invalid date: " + minDate);
	    System.exit(0);
	}
	this.minDate = minDate;
    }

    /** this only affects loading from raw data.  if subsetFractionDenominator = 3, then this should load only a third of the data  */
    void set_subset_offset_and_fractionDenominator(int subsetOffSet, int subsetFractionDenominator) {
	this.subsetFractionDenominator = subsetFractionDenominator;
	this.subsetOffSet = subsetOffSet;
    }

    void x_objects_to_use__by_class(Class... xClassesToUse) {
//	this.xClassesToExclude = xClassesToExclude;
	for (Class c : xClassesToUse) {
	    x_objects_to_use.add(c);
	}
    }

    void excludeCategoryTypes(C.CategoryType... cts) {
	this.excludedCatTypes = cts;
	for (C.CategoryType ct : cts)
	    C.catTypesToAnalyze.remove(ct);
    }


    /** call this right before running screen.  after ALL data is loaded/built */
    public void build_metaData() throws Screen.ThereMustBeEraDataRowFieldsWithNonUniqueNames, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	if (meta == null)
	    meta = new MetaData(this);
    }

    void loadStocks(From where) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException, ParseException, G.No_DiskData_Exception, Var.TriedToPutBadDataTypeInVarDataArray {
	if (where == From.RAW) {
	    stocks = Stock.getStocksFromRawDownloads(minDate, min, max, limitedTickers, subsetFractionDenominator, subsetOffSet, x_objects_to_use);

	    if (catAveableVars != null && !catAveableVars.isEmpty()) {
		for (Stock stock : stocks) {
//		    stock.vars_catAveable
		    for (Var var : stock.vars.values()) {
			if (catAveableVars.contains(var.getClass()))
			    var.isForCatAveComparison = true;
			else
			    var.isForCatAveComparison = false;
		    }
		}
	    }

	    System.out.println("loaded " + stocks.size() + " stocks from raw downloads");

	    if (x_objects_to_use.contains(objects.profile.X.class))
		build_catAves();
	}
	if (where == From.XFILES) {
	    stocks = Stock.getStocksFromXfiles(min, max);	    //catavescomps should have already been calculated before saving.
	    System.out.println("loaded " + stocks.size() + " stocks from xfiles");
	}
    }

    void printMegaTable() throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, Var.TriedToPutBadDataTypeInVarDataArray, Screen.ThereMustBeEraDataRowFieldsWithNonUniqueNames {
	System.out.println("printing megatable for QC! ...");

	boolean printTempVars = false;
	build_metaData();
	File file = G.finalMegaTable;

	//use these!! to preserve order throughout table
	List<Class> varClassList = new ArrayList(meta.demoStock.vars.keySet());

//	Macro weatherMacro = map__macroname_macro.get(Macro.Names.weather);
//	Macro shortInterestMacro = map__macroname_macro.get(Macro.Names.shortInterest);

//	for (String metricName : meta.macroMetrics) {
	String headerMacro = getMegaTableHeaderMacro(map__macroname_macro);//
//	meta.getMegaTableMacro();
	String headerCategoryNames = "GOOGLE_INDUSTRY\t" + "GOOGLE_SECTOR\t" + "YAHOO_INDUSTRY\t" + "YAHOO_SECTOR\t" + "GOOGLE_RELATED_COMPANIES\t";
	String headerStockStuff = meta.demoStock.getMegaTableHeader(varClassList, printTempVars, x_objects_to_use);
	String headerEraStuff = getHeaderEraStuff();
	String headerCatAves = C.getMegaTableCatAvesHeaders(C.catTypesToAnalyze, printTempVars, x_objects_to_use);
	String headerCER = getMegaTableCERHeaders(C.catTypesToAnalyze);

//	G.asdf("tw34t4t34t0 ");
	String header = ""
		+ headerMacro
		+ "ticker\t"
		//		+ headerCategoryNames
		+ headerStockStuff
		+ headerEraStuff
		+ headerCatAves
		+ headerCER
		+ "";

	try (PrintWriter pw = new PrintWriter(file)) {
	    pw.println(header);

	    for (Stock stock : stocks) {
		System.out.println("megatable printing: " + stock.name);

		Map<Class, Map<Integer, Era.EraDataRow>> map___edrclass__date_edr = build_map_for_printing_eras_stuff(stock);
//		G.asdf("hawefawef_1");

		for (int i = 0; i < stock.date.length; i++) {
		    StringBuilder sb = new StringBuilder();
		    int dateInt = stock.date[i];

		    //TODO STARRT HERE  WEF(WUE)Fu 
		    //trying to print era stuff in megatable

		    append_macroStuff(sb, dateInt, map__macroname_macro);
		    sb.append(stock.name).append(G.megaTableDelim);
		    //		    append_categoryNames(stock, sb);
		    append_stockStuff(sb, i, stock, varClassList, printTempVars);
//		    G.asdf("hawefawef_2");
		    append_erasStuff(dateInt, sb, map___edrclass__date_edr);
//		    G.asdf("hawefawef_3");
		    append_catAves(sb, dateInt, stock, printTempVars);
//		    G.asdf("hawefawef_4");
		    appendCER_stuff(dateInt, sb, stock);

		    pw.println(sb.toString());
//		    G.asdf("hawefawef_5 " + stock.name + " " + i + " " + dateInt);
		}
	    }
	}
    }

    void build_catAves() throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException, ParseException, IOException, InstantiationException, Var.TriedToPutBadDataTypeInVarDataArray {
	if (profiles == null)
	    profiles = loadProfiles();
	if (categoryFullName_tickers__map == null)
	    categoryFullName_tickers__map = C.getMap__categoryFullName_tickers(profiles);


//	//testing
//	for (String st : categoryFullName_tickers__map.keySet()) {
//	    System.out.println("awefaw4fewfwewef:    " + st);
//	}
//	System.exit(0);
	if (catFullName_stocks__map == null)
	    catFullName_stocks__map = C.get__catFullName_stocks__map(categoryFullName_tickers__map, stocks);
	catType__catName_catAves__map = CatAves.build___catType__catName_catAves__maps(minDate, catFullName_stocks__map, x_objects_to_use);
    }

    void supplementStocksWithCatAveComparisons() throws IOException {


	System.out.println("building catave comparisons....");
	if (stocks != null && catType__catName_catAves__map != null)
	    for (Stock stock : stocks) {
		stock.calculate_catAveComparisons(catType__catName_catAves__map, false);
	    }
	else {
	    System.out.println("(stocks != null && catType__name_catAves__map != null)");
	    throw new UnsupportedOperationException();
	}
    }


    void updateCatAveCompsForCurrentPrices() {
	System.out.println("updating category averages comparisons for current prices ...");

	for (Stock stock : stocks) {
	    stock.calculate_catAveComparisons(catType__catName_catAves__map, true);
	}
    }

    void build_CategoryEarningsResponses(From where) throws ParseException, IOException, InstantiationException, IllegalAccessException, ParseException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException {
	if (ticker_earnings__map == null) {
	    ticker_earnings__map = load___ticker_earnings__map(where);
	}
	if (where == From.RAW)
	    cers = CategoryEarningsResponses.getInstance_origination(minDate, catFullName_stocks__map, ticker_earnings__map);
	if (where == From.XFILES) {
	    if (profiles == null)
		profiles = loadProfiles();
	    if (categoryFullName_tickers__map == null)
		categoryFullName_tickers__map = C.getMap__categoryFullName_tickers(profiles);
	    if (catFullName_stocks__map == null)
		catFullName_stocks__map = C.get__catFullName_stocks__map(categoryFullName_tickers__map, stocks);
	    cers = CategoryEarningsResponses.getInstance_fromXfiles(minDate, catFullName_stocks__map);
	}
    }

    void updateCategoryEarningsResponsesForCurrentPrices() throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException {
	System.out.println("updating category earnings responses current prices ...");
	cers.updateForCurrentPrices();
    }

    public void loadMacroData(boolean getCurrentWeather) throws Weather.NoWeatherDataFound, IOException, FileNotFoundException, InterruptedException, Replace_Weather.WeatherTimeoutException, ParseException, ThereMustBeTwoMacroVariablesWithSameName, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	macrometric_macro__map = new LinkedHashMap();

	List<Macro> macros = new ArrayList();
	/********* Date **********/
	macros.add(Seasons.getMacro(minDate));


	/********* SPECIFIC STOCKS:  VTI,  **********/
	macros.add(Prices.getMacro(minDate, this, "VTI", "AAPL"));

	/********* Weather **********/
	if (getCurrentWeather)
	    macros.add(Weather.getMacro(Weather.get__date_weather_map(Stock.getDatesUniverse(minDate))));
	else
	    macros.add(Weather.getMacro(Weather.get__date_weather_map_NOAA_HISTORIC()));

	/********* Short Interest **********/
	macros.add(ShortInterest.getMacro(minDate, validDates));

	/*** finished adding macros ****/

	int macroMetricsCount = 0;

	for (Macro m : macros) {
	    for (String metric : m.metrics) {
		macroMetricsCount++;
		macrometric_macro__map.put(metric, m);
	    }
	}
	if (macroMetricsCount != macrometric_macro__map.size())
	    throw new ThereMustBeTwoMacroVariablesWithSameName();

	this.map__macroname_macro = new TreeMap();

	for (Macro m : macros)
	    this.map__macroname_macro.put(m.name, m);
    }

    /** for template filter creation */
    public void loadMacroDummyData() throws Weather.NoWeatherDataFound, IOException, FileNotFoundException, InterruptedException, Replace_Weather.WeatherTimeoutException, ParseException, ThereMustBeTwoMacroVariablesWithSameName {
	macrometric_macro__map = macrometric_macro__map = new LinkedHashMap();

	List<Macro> macros = new ArrayList();
	macros.add(Weather.getMacro(Weather.get__date_weather_map_NOAA_HISTORIC()));

	int numMacroMetrics = 0;

	for (Macro m : macros) {
	    for (String metric : m.metrics) {
		numMacroMetrics++;
		macrometric_macro__map.put(metric, m);
	    }
	}
	if (numMacroMetrics != macrometric_macro__map.size())
	    throw new ThereMustBeTwoMacroVariablesWithSameName();
    }

    public void loadStocksCurrentPricesFromWeb() throws IOException, InterruptedException {
	    //TODO!
	//doing this will add a day to prices?
	//no, hps will stay constant.  ?   fuck...........
	//can we just add an extra cell to the hps arrays?  a placeholder for current prices

	//1.  calculate everything using old data, putting in to arrays that have empty data for today
	//2.  download current prices.  fill today's cell in hps (hmmm .... crap maybe i should rename that something instead of "historical prices" if it's going to be holding current prices!
	//3.  calculate all data for today (X's, ave's, cat earning responses, etc)  (FUCK HOW DO I DO THAT!!!!???)

	// i think keep hps as hps.  just stop using hps as a data reference.  use prices_X actual Var data for prices, dates, etc
	List<String> tickers = Stock.getTickers(stocks);

	Map<String, CurrentPrices> ticker_currentPrices__map = download_currentPrices(tickers);

	for (Stock stock : stocks)
	    stock.updatePrices(ticker_currentPrices__map);
    }

    public void updateStocksMainDataForCurrentPrices() throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

	System.out.println("inserting current prices in Stocks list ...");

	for (Stock stock : stocks) {
	    stock.updateMainDataForCurrentPrices();
	    stock.clearTemporaryVariables();
	}
    }

    private Map<String, Profile_EraDataRow> loadProfiles() throws IOException, FileNotFoundException, InstantiationException, IllegalAccessException, ParseException {
	Map<String, Profile_EraDataRow> profiles_ = new LinkedHashMap();
	if (stocks != null)
	    profiles_ = Profile_EraDataRow.getAllFromStocks(stocks);
	else if (G.XProfile_eras.exists())
	    profiles_ = Profile_EraDataRow.readAllFromDisk();
	return profiles_;
    }

    private Map<String, List<Earnings_EraDataRow>> load___ticker_earnings__map(From where) throws IOException, FileNotFoundException, InstantiationException, IllegalAccessException, ParseException {
	Map<String, List<Earnings_EraDataRow>> ticker_earnings__map_ = new LinkedHashMap();
	if (where == From.RAW && stocks != null) {
	    ticker_earnings__map_ = Earnings_EraDataRow.getAllFromStocks(stocks);
//	    System.out.println("w45gw34gt here1;");
//
//	    for (Map.Entry<String, List<Earnings_EraDataRow>> entry : ticker_earnings__map_.entrySet()) {
//		System.out.println("45t49t58u9e8tu:   " + entry.getKey() + ", size: " + entry.getValue().size());
//	    }
//	    System.out.println("5y45y4958yu945 contains AAON? :  " + ticker_earnings__map_.containsKey("AAON"));
//	    System.exit(0); 
	} else if (where == From.XFILES && G.XEarnings_eras.exists()) {
	    ticker_earnings__map_ = Earnings_EraDataRow.readAllFromDisk();
//	    System.out.println("a342ar34 here2;");
//	    System.exit(0);
	}
	return ticker_earnings__map_;
    }

    void writeToDisk() throws IOException {

	FileUtils.deleteQuietly(G.tempX_day);

	Stock.writeToDisk_xFiles(stocks);
	cers.writeToDisk();
    }

    void updateCatAvesForCurrentPrices() throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, Var.TriedToPutBadDataTypeInVarDataArray {
	if (catType__catName_catAves__map == null)
	    catType__catName_catAves__map = CatAves.build___catType__catName_catAves__maps(minDate, catFullName_stocks__map, x_objects_to_use);	//;lkj;lkj;lkj

	for (C.CategoryType ct : C.catTypesToAnalyze)
	    for (CatAves ca : catType__catName_catAves__map.get(ct).values())
		ca.updateForCurrentPrices();
    }


    void updateFromWeb() throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, InterruptedException, Var.TriedToPutBadDataTypeInVarDataArray, FileNotFoundException, ClassNotFoundException, Screen.ThereMustBeEraDataRowFieldsWithNonUniqueNames {

	loadStocksCurrentPricesFromWeb();
	updateStocksMainDataForCurrentPrices();
	if (x_objects_to_use.contains(objects.profile.X.class)) {
	    updateCatAvesForCurrentPrices();
	    updateCatAveCompsForCurrentPrices();
	    if (x_objects_to_use.contains(objects.earnings.X.class))
		updateCategoryEarningsResponsesForCurrentPrices();
	}
    }

    void prepareForScreen() throws Screen.ThereMustBeEraDataRowFieldsWithNonUniqueNames, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	build_metaData();					//meta data is used by screen to build conditions?  or should filter do that?
	catType__catName_catAves__map = null;
    }

    void load(From where) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException, ParseException, G.No_DiskData_Exception, Var.TriedToPutBadDataTypeInVarDataArray, Weather.NoWeatherDataFound, InterruptedException, Replace_Weather.WeatherTimeoutException, ThereMustBeTwoMacroVariablesWithSameName {

	loadStocks(where);

	if (x_objects_to_use.contains(objects.profile.X.class)) {
	    if (where == From.RAW) {
		supplementStocksWithCatAveComparisons();
	    }
	    if (x_objects_to_use.contains(objects.earnings.X.class)) //objects.earnings.X.class,)
		build_CategoryEarningsResponses(where);
	}
//	readFromDisk_CategoryEarningsResponses();
	loadMacroData(false);
    }


    private String getHeaderEraStuff() {
	StringBuilder sb = new StringBuilder();
	for (String st : meta.alledrs__fieldname_edrclass__map.keySet()) {				    //this used to be a linkedhashset made from an arraylist made from this .keyset.  i deleted all that crap -- was it important? i hope not
	    String name_ = st;
	    sb.append(name_).append(G.megaTableDelim);
	}
	return sb.toString();
    }

    private String getMegaTableHeaderMacro(TreeMap<String, Macro> map__macroname_macro) {
	String returner = "";
	for (Macro m : map__macroname_macro.values()) {
	    for (String metricName : m.metrics)
		returner += metricName + G.megaTableDelim;
	}

	return returner;
    }

    private void append_erasStuff(int dateInt, StringBuilder sb, Map<Class, Map<Integer, Era.EraDataRow>> map___edrclass__date_edr) {


	//for date in eras list, put that date in the 2ndary map, and get its associated eradatarow from the era
	//start with minimum date in the eras list
	//will earliest era be the first list element?  no.  eras sorted in decreasing dates.   earliest is last!
	//
	//stock.eras.entrySet() should have 3 elements now.  one per X (that uses eras) 
	//
	//
	//
	//NOW.  how to get all eras data....???????????
	// we have name of the era variables....
	// how to get data from names?  where else are we getting eras data .... ? per name?  no where?  oh during filter/screening.  htf does that work?
	//so, there is nothing that connects a date with an era value.  that is not used/needed for filtering.  but we want to do that for making megatable.  
	//so we'll make a map like <eradatarowmetricclass, Map<dateInt, eradatarow>>
	for (Map.Entry<String, Class> entry : meta.alledrs__fieldname_edrclass__map.entrySet()) {

	    String edrMetric = entry.getKey();
	    Class edrClass = entry.getValue();

	    //how get value now???? with dateInt and map___edrclass__date_edr

	    Era.EraDataRow edr = map___edrclass__date_edr.get(edrClass).get(dateInt);
	    Float value = null;
	    if (edr != null) {
		value = edr.valuesMap.get(edrMetric);

//		if (value == null) {
//		    G.asdf("append_erasStuff(...) ERROR value==null, edrMetric= " + edrMetric);
//		    G.asdf("append_erasStuff(...) ERROR value==null, dateInt= " + dateInt);
//		    G.asdf(edr.valuesMap.size());
//
//		    for (Map.Entry<String, Float> entry2 : edr.valuesMap.entrySet()) {
//			G.asdf("append_erasStuff(...) ERROR value==null, edr.valuesMap.entrySet(): " + entry2.getKey() + " " + entry2.getValue());
//		    }
//
//
//		    System.exit(0);
//		}

		sb.append(value == null ? "NA" : value).append(G.megaTableDelim);
	    } else {
//		System.out.println("WHY IS EDR NULL??????????");
//		System.out.println(edrClass);
//		G.asdf(dateInt);

		sb.append(value).append(G.megaTableDelim);
	    }
	}
    }

    private void appendCER_stuff(int dateInt, StringBuilder sb, Stock stock) {

	if (!x_objects_to_use.contains(objects.profile.X.class) || !x_objects_to_use.contains(objects.earnings.X.class))
	    return;

	//CER
	for (C.CategoryType ct : C.catTypesToAnalyze) {

	    String cfn = C.getCategoryFullName(stock, ct, profiles);

	    Y y = cers.categoryFullName_Y__map.get(cfn);
	    Integer k = y.date_i__map.get(dateInt);

	    for (Var var : y.vars.values()) {

		String value = "";
		try {
		    value = (k == null ? null : var.get(k));
		} catch (Exception e) {

		    System.out.println("WTF ERROR");
		    System.out.println("fgrgtrgre  : " + cfn);
		    System.out.println("fgrgtrgre  : " + var.getName());
		    System.out.println("fgrgtrgre  : " + var.ar_float.length);
		    System.out.println("_date_len  : " + y.vars.get(categories.category_earnings_response.Y.cer_date.class).ar_int.length);
		    e.printStackTrace();

		    System.exit(0);
		}
		sb.append(value).append(G.megaTableDelim);
	    }
	}
    }

    private void append_catAveComps_stuff(StringBuilder sb, Stock stock) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void append_catAveComps_stuff(int i, StringBuilder sb, Stock stock, List<Class> varClassList, boolean printTempVars) {

	if (!x_objects_to_use.contains(objects.profile.X.class))
	    return;
	for (Class vc : varClassList) {
	    Var var = stock.vars.get(vc);
	    if (var.isTemp && !printTempVars)
		continue;

	    if (var.isForCatAveComparison) {
		for (C.CategoryType ct : C.catTypesToAnalyze) {

		    String value = "";
		    try {
			value = var.catComparisonArrays.get(ct)[i] + "";					    //missing data for some split-related data for stocks, cos some stocks don't have splits data.  and that's okay!
		    } catch (ArrayIndexOutOfBoundsException ex) {
		    }

		    sb.append(value).append(G.megaTableDelim);
		}
	    }
	}
    }

    private void append_catAves(StringBuilder sb, int dateInt, Stock stock, boolean printTempVars) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, Var.TriedToPutBadDataTypeInVarDataArray {

	if (!x_objects_to_use.contains(objects.profile.X.class))
	    return;

	//cataves
	for (C.CategoryType ct : C.catTypesToAnalyze) {
	    String catName = C.getCategoryName(stock, ct, profiles);

	    if (catName == null) {
		for (int i = 0; i < stock.vars_catAveable.size(); i++)
		    sb.append("NA").append(G.megaTableDelim);
		sb.append("NA").append(G.megaTableDelim);
	    } else {

		CatAves ca = catType__catName_catAves__map.get(ct).get(catName);
		Integer k = ca.date_i__map.get(dateInt);

		for (Var var : ca.vars_catAveable.values()) {
		    if (var.isTemp && !printTempVars)
			continue;
		    String value = (k == null || catName == null ? null : var.get(k));
		    sb.append(value).append(G.megaTableDelim);
		}
		sb.append(ca.stocksInTheCategory.size()).append(G.megaTableDelim);
	    }
	}
    }

    private void append_stockMainData(Stock stock, int i, StringBuilder sb, List<Class> varClassList, boolean printTempVars) {

	//vars main data
	for (Class vc : varClassList) {
	    Var var = stock.vars.get(vc);
	    if (var.isTemp && !printTempVars)
		continue;

	    String value = "";
	    try {
		value = var.get(i);					    //missing data for some split-related data for stocks, cos some stocks don't have splits data.  and that's okay!
	    } catch (ArrayIndexOutOfBoundsException ex) {
	    }
	    sb.append(value).append(G.megaTableDelim);
	}
    }

    /** the order of map__macroname_macro is important! use treemap! */
    private void append_macroStuff(StringBuilder sb, int dateInt, Map<String, Macro> map__macroname_macro) {

//	for (String metric : )
//	for (Macro m : map__macroname_macro.values()) {
//	    for (String metricName : m.metrics)
//		returner += metricName + G.megaTableDelim;
//	}

	for (Macro m : map__macroname_macro.values()) {
	    Integer date_i = m.date_i_map.get(dateInt);

	    if (m.getClass().equals(Macro_float.class)) {

		for (String metric : m.metrics) {
		    float[] ar = ((Macro_float)m).dataArrays.get(metric);
		    Float value = null;
		    if (date_i != null)
			value = ar[date_i];
		    sb.append(value).append(G.megaTableDelim);
		}
	    }
	    if (m.getClass().equals(Macro_int.class)) {

		for (String metric : m.metrics) {
		    int[] ar = ((Macro_int)m).dataArrays.get(metric);
		    Integer value = null;
		    if (date_i != null)
			value = ar[date_i];
		    sb.append(value).append(G.megaTableDelim);
		}


	    }
	}
    }

    private void append_categoryNames(Stock stock, StringBuilder sb) {

	sb.append(C.getCategoryName(stock, C.CategoryType.GOOGLE_INDUSTRY, profiles)).append(G.megaTableDelim);
	sb.append(C.getCategoryName(stock, C.CategoryType.GOOGLE_SECTOR, profiles)).append(G.megaTableDelim);
	sb.append(C.getCategoryName(stock, C.CategoryType.YAHOO_INDUSTRY, profiles)).append(G.megaTableDelim);
	sb.append(C.getCategoryName(stock, C.CategoryType.YAHOO_SECTOR, profiles)).append(G.megaTableDelim);
	sb.append(C.getCategoryName(stock, C.CategoryType.GOOGLE_RELATED_COMPANIES, profiles)).append(G.megaTableDelim);
    }

    private Map<Class, Map<Integer, Era.EraDataRow>> build_map_for_printing_eras_stuff(Stock stock) {

	/********************** building data structure for era stuff *******************/
	//so, there is nothing that connects a date with an era value.  that is not used/needed for filtering.  but we want to do that for making megatable.  
	//so we'll make a map like <eradatarowmetricclass, Map<dateInt, eradatarow>>
	Map<Class, Map<Integer, Era.EraDataRow>> map___edrclass__date_edr = new LinkedHashMap();

	for (Map.Entry<Class, List<Era>> entry : stock.eras.entrySet()) {
	    Class c = entry.getKey();
	    List<Era> eras = entry.getValue();

	    Collections.sort(eras, Collections.reverseOrder());	    //reverse - most recent dates first

	    int minDate = eras.get(eras.size() - 1).minDate;
	    int maxDate = eras.get(0).maxDate;

	    Map<Integer, Era.EraDataRow> map__date_edr = new LinkedHashMap();

	    for (int dateInt = minDate; dateInt <= maxDate; dateInt++) {
		Era.EraDataRow edr = Era.getEraDataRow(eras, dateInt);
		map__date_edr.put(dateInt, edr);
	    }
	    map___edrclass__date_edr.put(c, map__date_edr);
	}
	return map___edrclass__date_edr;
    }

    public String getMegaTableCERHeaders(Set<C.CategoryType> catTypes) {
	if (x_objects_to_use.contains(objects.profile.X.class) && x_objects_to_use.contains(objects.earnings.X.class)) {
	    StringBuilder sb = new StringBuilder();

	    Y dummy_y = (Y)(new ArrayList(cers.categoryFullName_Y__map.values())).get(0);

	    for (C.CategoryType ct : catTypes)
		for (Var var : dummy_y.vars.values())
		    sb.append(lowercaseExceptFirstLetter(ct.nickname)).append("_").append(var.getName()).append(G.megaTableDelim);

	    return sb.toString();
	} else return "";
    }

    private void append_stockStuff(StringBuilder sb, int i, Stock stock, List<Class> varClassList, boolean printTempVars) {

	append_stockMainData(stock, i, sb, varClassList, printTempVars);

	append_catAveComps_stuff(i, sb, stock, varClassList, printTempVars);
    }


}
