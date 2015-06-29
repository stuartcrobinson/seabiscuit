package objects;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utilities.G;
import supers.Era;
import supers.SuperX;
import supers.Var;
import categories.C;
import categories.CatAves;
import categories.Seasons;
import java.util.Iterator;
import java.util.function.Predicate;
import objects.earnings.Earnings;
import objects.prices.CurrentPrices;

//stock needs category info.  is this eraly? no.  i know this is sloppy.  it should be time-based.  but lets just assume constant class categories for now. 

/** a package is a group of X vars.  (and other stuff? or just xVars?  like what about Y (category earnings response?  NO.  that is macro.  Package contains only non-macro (micro?) vars.) a package can be an actual stock OR it can be a categoryFullName -- so that it's all the vars a stock would have, but the vars are averages among the stocks in that category */
public class Stock {
    private static List<Earnings> earningsList_all;

    public static List<String> getTickers(List<Stock> stocks) {
	List<String> tickers = new ArrayList(stocks.size());
	for (Stock stock : stocks) {
	    tickers.add(stock.name);
	}
	return tickers;
    }

    /** ticker or categoryFullName */
    public String name;

    /** missing prices or finance data */
    public boolean missingCriticalData;

    /** i might change date to float later ! */
    public int[] date;					    //TODO how to initialize this???

    public Map<Integer, Integer> date_i__map;

    public objects.earnings.X earnings_X;
    public objects.finance.X finance_X;
    public objects.news.X news_X;
    public objects.people.X people_X;
    public objects.prices.X prices_X;
    public objects.profile.X profile_X;
    public objects.sec.X sec_X;
    public objects.short_interest.X short_interest_X;
    public objects.splitsAndDividends.X splits_X;

    public List<SuperX> xs;

    public Map<Class, Var> vars;
    public Map<Class, Var> vars_catAveable;

//    public static Set<C.CategoryType> categoryTypes_to_build_comparisons_from = Stock.make__categoryTypes_to_build_comparisons_from();

    /** key: eraDataRow_class.  */
    public Map<Class, List<Era>> eras;

    private Integer minDate;

    /** all vars names, then the vars' catAveComps data */
    public String getMegaTableHeader(List<Class> varClassList, boolean printTempVars, Set<Class> x_objects_to_use) {

	StringBuilder sb = new StringBuilder();
	for (Class vc : varClassList) {
	    Var var = vars.get(vc);
	    if (var.isTemp && !printTempVars)
		continue;

	    String name_ = var.getName();
	    sb.append(name_).append(G.megaTableDelim);
	}


	if (x_objects_to_use.contains(objects.profile.X.class)) {
	    /********** catAv comparisons *****/
	    for (Class vc : varClassList) {
		Var var = vars.get(vc);

		String name = var.getName();

		if (var.isForCatAveComparison) {
		    for (C.CategoryType ct : C.catTypesToAnalyze) {
			sb.append("v").append(C.lowercaseExceptFirstLetter(ct.nickname)).append("_").append(name).append(G.megaTableDelim);
		    }
		}
	    }
	}
	return sb.toString();
    }


    /** NOTE - this only uses minDate cos our data doesn't include old unlisted stocks :( */
    public boolean containsDate(Integer dateInt) {
	return dateInt >= minDate;// && dateInt <= maxDate;
    }

    /** empty and useless besides examining structure for analysis.  do not use for data */
    public Stock(Set<Class> x_objects_to_use) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

	if (x_objects_to_use != null && x_objects_to_use.contains(objects.prices.X.class))
	    prices_X = new objects.prices.X();
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.finance.X.class))
	    finance_X = new objects.finance.X();
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.profile.X.class))
	    profile_X = new objects.profile.X();
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.people.X.class))
	    people_X = new objects.people.X();
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.sec.X.class))
	    sec_X = new objects.sec.X();
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.short_interest.X.class))
	    short_interest_X = new objects.short_interest.X();
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.splitsAndDividends.X.class))
	    splits_X = new objects.splitsAndDividends.X();
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.news.X.class))
	    news_X = new objects.news.X();
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.earnings.X.class))
	    earnings_X = new objects.earnings.X();

	initializeStuff();
    }

    /** make x_objects_to_use null if loading from xfiles.  not sure what to do next in terms of loading data that is missing stuff, for when i don't care about it.  like if i'm not studying earnings right now.  so just don't load it to save space and time.  not updating xfiles mechanisms cos it's slower on this computer.  maybe someday i will get it workinng if i have a faster computer to where using xfiles actually saves time */
    public Stock(String ticker, Set<Class> x_objects_to_use) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

	name = ticker;

	if (x_objects_to_use != null && x_objects_to_use.contains(objects.prices.X.class))
	    prices_X = new objects.prices.X(name);
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.finance.X.class))
	    finance_X = new objects.finance.X(name);
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.profile.X.class))
	    profile_X = new objects.profile.X(name);
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.people.X.class))
	    people_X = new objects.people.X(name);
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.sec.X.class))
	    sec_X = new objects.sec.X(name);
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.short_interest.X.class))
	    short_interest_X = new objects.short_interest.X(name);
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.splitsAndDividends.X.class))
	    splits_X = new objects.splitsAndDividends.X(name);
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.news.X.class))
	    news_X = new objects.news.X(name);
	if (x_objects_to_use != null && x_objects_to_use.contains(objects.earnings.X.class))
	    earnings_X = new objects.earnings.X(name);

	initializeStuff();
    }

    public void initializeStuff() {
	xs = new ArrayList();
	xs.add(prices_X);
	xs.add(finance_X);
	xs.add(profile_X);
	xs.add(people_X);
	xs.add(sec_X);
	xs.add(short_interest_X);
	xs.add(splits_X);
	xs.add(news_X);
	xs.add(earnings_X);

	for (Iterator<SuperX> it = xs.iterator(); it.hasNext();) {
	    SuperX x = it.next();
	    if (x == null) it.remove();
	}

	vars = new LinkedHashMap();
	vars_catAveable = new LinkedHashMap();

	for (SuperX x : xs) {
	    if (x.vars != null)
		vars.putAll(x.vars);
	    if (x.vars_catAveable != null)
		vars_catAveable.putAll(x.vars_catAveable);
	}
    }

    /** all data is aligned to the same date index, found in prices_X.  extra null cell at start is placeholder for today's data */
    public void build_data_from_raw_files(int minDate, List<Earnings> earningsList_all) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, IOException {
	//i think this is messy and stupid.  i should replace this monster with a for (SuperX : xs) -- null X's have already been removed from that.  then just pass each data_origination constructor the "this" stock object, or pass them all the same stuff (sec_x, etc)
	try {
	    try {
		if (prices_X != null)
		    prices_X.calculate_data_origination(minDate);
	    } catch (G.No_DiskData_Exception ex) {
		System.out.println("                        missing prices,");
		throw ex;
	    }
	    if (splits_X != null)
		splits_X.calculate_data_origination(prices_X);		    //this is the only one to not care if missing data.  since lots of stocks will be missing this

//	    if (splits_X != null && prices_X != null)			    //don't delete this 
//		prices_X.adjustForSplits(splits_X);

	    try {
		if (finance_X != null)
		    finance_X.calculate_data_origination(prices_X);
	    } catch (G.No_DiskData_Exception ex) {
		System.out.println("                         missing finance,");
		throw ex;
	    }
	    try {
		if (people_X != null)
		    people_X.calculate_data_origination();
	    } catch (G.No_DiskData_Exception ex) {
		System.out.println("                         missing ppl,");
		throw ex;
	    }
	    try {
		if (sec_X != null)
		    sec_X.calculate_data_origination(prices_X);
	    } catch (G.No_DiskData_Exception ex) {
		System.out.println("                         missing sec,");
		throw ex;
	    }
	    try {
		if (profile_X != null)
		    profile_X.calculate_data_origination();
	    } catch (G.No_DiskData_Exception ex) {
		System.out.println("                         missing profile,");
		throw ex;
	    }
	    try {
		if (short_interest_X != null)
		    short_interest_X.calculate_data_origination(prices_X);
	    } catch (G.No_DiskData_Exception ex) {
		System.out.println("                         missing si,");
		throw ex;
	    }
	    try {
		if (news_X != null)
		    news_X.calculate_data_origination(prices_X);
	    } catch (G.No_DiskData_Exception ex) {
		System.out.println("                         missing news,");
		throw ex;
	    }
	    try {
		if (earnings_X != null)
		    earnings_X.calculate_data_origination(earningsList_all, prices_X, news_X, sec_X);
	    } catch (G.No_DiskData_Exception ex) {
		System.out.println("                         missing earnings,");
		throw ex;
	    }

	    setStockDateStuff();
	    set_eras();
	} catch (G.No_DiskData_Exception ex) {
//	    System.out.print("     MISSING DATA!\n");
	    missingCriticalData = true;
	}
    }

    /** this data should have an extra cell -- for today's data -- already built in */
    public void build_data_from_xFiles() throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException {
	for (SuperX x : xs) {
//	    System.out.println("349t8u349tr: class: " + x.getClass() + "     varsfile:   " + x.varsFile);
	    x.calculate_data_from_xFiles();

//
//	    //testing
//	    for (Map.Entry<Class, Var> entry : x.vars.entrySet()) {
//		System.out.println("CLASS, IN VARS MAP:                                            " + entry.getKey());
//		Var var = entry.getValue();
//		System.out.println("CLASS, IN VARS MAP:                    " + var.arrayDataType);
//		System.out.println("CLASS, IN VARS MAP:  int array len                  " + var.ar_int.length);
//
//		System.out.println("from map, date len: " + prices_X.vars.get(objects.prices.X.date.class).ar_int.length);
//		System.out.println("CLASS, IN VARS MAP:  flo array len                  " + var.ar_float.length);
//	    }

	}
	setStockDateStuff();
	set_eras();
//	for (SuperX x : xs) {
//	    x.setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(stock);
//	}

    }

    /** only updates date, open , high, low, close */
    public void updatePrices(Map<String, CurrentPrices> ticker_currentPrices__map) {
	CurrentPrices currentPrices = ticker_currentPrices__map.get(name);
	prices_X.updatePrices(currentPrices);
    }

    public void updateMainDataForCurrentPrices() throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

	for (SuperX x : xs) {
	    x.reinitializeVarsIfYouNeedTo__forIsTempVars(date.length);
	    x.updateMainDataForCurrentPrices();
	}

	setStockDateStuff();

	//how can i update earnings eras only?
	if (earnings_X != null)
	    earnings_X.updateErasForCurrentPrices();//fill_eras();	    //this is to update stuff like, price change after earnings.  for current prices.  
//	finance_X.fill_eras();	    //aren't eras already filled?    check finance stuff during calculation on current day

//	G.asdf("e45ghe4g5e45g DO WE STILL HAVE PRICES???? " + prices_X.close(prices_X)[0]);

    }

    private void setStockDateStuff() {

//	System.out.println(name + ": from map, date len: " + prices_X.vars.get(objects.prices.X.date.class).ar_int.length);

	date = prices_X.vars.get(objects.prices.X.date.class).ar_int;
	date_i__map = G.get__date_i__map(date);
	minDate = date[date.length - 1];
    }

    private void set_eras() {
	eras = new HashMap<>();
	for (SuperX x : new SuperX[]{people_X, earnings_X, finance_X}) {
	    if (x == null) continue;
	    Class eradatarowclass = x.eraDataRowClass;
	    eras.put(eradatarowclass, x.eras);
	}
    }


    /** must have already built data! input map: String is the cleaned category name, like "ElectricalComponents_NEC"*/
    public void calculate_catAveComparisons(Map<C.CategoryType, Map<String, CatAves>> catType__catName_catAves__map, boolean doUpdateForCurrentPrices) {

	Map<C.CategoryType, String> catType_catNameForThisTicker_map = ((objects.profile.Profile_EraDataRow)profile_X.eras.get(0).eraDataRow).categories;
//	System.out.println("aw43gtaawefawef " + catType_catNameForThisTicker_map.get(C.CategoryType.GOOGLE_INDUSTRY));

	Map<C.CategoryType, CatAves> catType_catAvesForThisTicker_map = get_tickerSpecificCatAves(catType__catName_catAves__map, catType_catNameForThisTicker_map);

	int stockDataLength = date.length;

	for (SuperX x : xs) {
	    if (x.vars_catAveable != null) //TODO should i make separate list of catAveable X's???? so i'm not wasting time iterating over things to skip????  eh not worth clutter of extra variables
		x.calculate_catAveComparisons(catType_catAvesForThisTicker_map, stockDataLength, doUpdateForCurrentPrices, date, date_i__map);
	}
    }


    public void write_xFiles() throws IOException {
	for (SuperX x : xs)
	    x.writeToDisk();
    }

    public Integer get_array_index_for_date(int dateInt) {
	return date_i__map.get(dateInt);
    }

    private static Set<C.CategoryType> make__categoryTypes_to_build_comparisons_from() {

	//don't forget google related companies
	return new HashSet(Arrays.asList(C.catTypesToAnalyze));
    }

    private Map<C.CategoryType, CatAves> get_tickerSpecificCatAves(Map<C.CategoryType, Map<String, CatAves>> catType__name_catAves__map, Map<C.CategoryType, String> ticker_all_categories) {

	Map<C.CategoryType, CatAves> tickers_CatAves = new LinkedHashMap(C.catTypesToAnalyze.size());

	for (C.CategoryType ctype : C.catTypesToAnalyze) {

	    String tickerCategoryName;

	    if (ctype == C.CategoryType.GOOGLE_RELATED_COMPANIES)
		tickerCategoryName = name;
	    else
		tickerCategoryName = ticker_all_categories.get(ctype);


//	    System.out.println("h45trte : cat nAME : " + tickerCategoryName);
	    //todo start hereherehere
	    CatAves ca = catType__name_catAves__map.get(ctype).get(tickerCategoryName);

	    tickers_CatAves.put(ctype, ca);

	}
	return tickers_CatAves;

    }

    /** we should make sure this doesn't include dates past today.  get all possible dates that we are going to be analyzing */
    public static Set<Integer> getDatesUniverse(int minDate) throws ParseException {
	return new LinkedHashSet<>((new Seasons(minDate)).datesList);
    }

//    /** we should make sure this doesn't include dates past today.  get all possible dates that we are going to be analyzing */
//    public static Set<Integer> getDatesUniverse() throws ParseException {
//	return new LinkedHashSet<>((new Seasons(null)).datesList);
//    }

//    /** get all possible dates that we are going to be analyzing */
//    public static Set<Integer> getDatesUniverse(int startingYear) throws ParseException {
//	return new LinkedHashSet<>((new Seasons(startingYear)).datesList);
//    }
    /** should read main data and catAveComps.  (how do we load catEarningsResponses? (CER) */
    public static List<Stock> getStocksFromXfiles(String min, String max) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException, ParseException {

	Set<String> tickers = G.getTickers(G.XPrices_vars);

	tickers.removeIf(new Predicate<Object>() {
	    public boolean test(Object t) {
		return ((String)t).compareTo(min) < 0 || ((String)t).compareTo(max) > 0;
	    }
	});

	List<Stock> stocks = new ArrayList(tickers.size());

	for (String ticker : tickers)
	    stocks.add(new Stock(ticker, null));

	System.out.println("loading " + stocks.size() + " stocks...");

	int totalsize = stocks.size();

	int count = 1;
	for (Stock stock : stocks) {

	    stock.build_data_from_xFiles();

	    for (SuperX x : stock.xs) {
		x.setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(stock);
	    }
	    System.out.println((totalsize - count++) + ".  " + stock.name);
	}

	return stocks;
    }

    /** limitedTickers as null gets ignored.  then loads all */
    public static List<Stock> getStocksFromRawDownloads(int minDate, String min, String max, String[] limitedTickers, Integer subsetFractionDenominator, Integer subsetOffSet, Set<Class> x_objects_to_use) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException {
	List<String> tickers = Symbol.getTickersList(min, max);

	List<Stock> stocks = new ArrayList();

	System.out.print("loading empty Stocks: ");
	if (limitedTickers != null && limitedTickers[0] != null) {
	    System.out.print(limitedTickers);
	} else {
	    limitedTickers = null;
	    if (!min.equals(G.minimumTicker) || !max.equals(G.maximumTicker))
		System.out.print(" between " + min + " and " + max + ", ");
	}
	if (minDate != G.minimumDateInt)
	    System.out.print("starting on " + minDate + ", ");
	if (subsetFractionDenominator != null)
	    System.out.print("subset: 1 in " + subsetFractionDenominator);
	System.out.println("... ");

	int count = 0;
	for (String ticker : tickers) {
	    if (subsetFractionDenominator != null && !((count++ + subsetOffSet) % subsetFractionDenominator == 0)) //only keeps multiples of subsetFractionDenominator! this should skip iterations that are not a multiple of subsetsubsetFractionDenominator = n.  so the final set should be 1/nth the size of total
		continue;

	    if (limitedTickers != null && !Arrays.asList(limitedTickers).contains(ticker)) //"AAON", "A", 
		continue;

	    stocks.add(new Stock(ticker, x_objects_to_use));
	}

	System.out.println("loaded " + stocks.size() + " stock shells.");

	System.out.println("loadeding all earnings data ...");


	if (x_objects_to_use.contains(objects.earnings.X.class))
	    earningsList_all = Earnings.readAllEarningsFiles(minDate);	    //wtf?

	System.out.println("loading all stocks data ...");

	for (Stock stock : stocks) {
	    System.out.println("initial loading: " + stock.name);
	    stock.build_data_from_raw_files(minDate, earningsList_all);
	    stock.clearTemporaryVariables();
	}

	stocks.removeIf(new Predicate<Object>() {
	    public boolean test(Object t) {
		return ((Stock)t).missingCriticalData;
	    }
	});

	return stocks;
    }

    public void clearTemporaryVariables() {
	prices_X.deleteHPS();
	for (Var var : vars.values()) {
	    if (var.isTemp)
		var.clearData();
	}
    }

//    public static List<Stock> getStocksFromRawDownloads(int minDate, String min, String max) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException {
//	return getStocksFromRawDownloads(minDate, min, max, null);
//    }
//
//    public static List<Stock> getStocksFromRawDownloads() throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException, ParseException, G.No_DiskData_Exception {
//	return getStocksFromRawDownloads(0, "A", "ZZZZZ");
//    }
//
//    public static List<Stock> getStocksFromRawDownloads(int minDate) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException, ParseException, G.No_DiskData_Exception {
//	return getStocksFromRawDownloads(minDate, "A", "ZZZZZ");
//    }

    public static void writeToDisk_xFiles(List<? extends Stock> stocks) throws IOException {
//	G.tempX_day.mkdirs();
	for (Stock stock : stocks) {
	    System.out.println("writing stock: " + stock.name);
	    stock.write_xFiles();
	}
    }

    public static Map<String, Stock> get__ticker_stock__map(List<Stock> stocks) {
	Map<String, Stock> ticker_stock__map = new LinkedHashMap(stocks.size());
	for (Stock stock : stocks)
	    ticker_stock__map.put(stock.name, stock);
	return ticker_stock__map;
    }

    public String getCategoryName(C.CategoryType ct) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
