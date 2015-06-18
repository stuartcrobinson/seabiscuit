package supers;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import utilities.G;
import categories.C;
import categories.CatAves;
import categories.Seasons;
import java.util.LinkedHashMap;
import main.DataManager;
import objects.Stock;
import objects.prices.X;

/**  only origination uses attributes.  categoryAveragings use passed parameters only.  reading from files uses files only.<br><br> an X class is used for handling data in a way that will be used exclusively for final analysis.  X class should implement the ...uses_Eras or ...uses_Vars or both. <br><br>
 vars are held in a map.  map of Var objects, each one contains method for calculating one date-indexed array-based piece of data (like PE or ema3m5) <br><br>
 eras are a list of Era objects.  refers to data that is identical over a given time period.  stored in an eras file, one line per era.  mindate, maxdate, eraDataRow <br><br>
 X should be able to: (i) read data from raw download, (i.i) accept pre-loaded data objects,  (ii) write to Xfiles, (iii) read from Xfiles, and be structured in a way to let an external object read&write an X object from a megamasterdatafile (probably will still have a separate file per stock since opening 10,000 different files takes less than a second */
public abstract class SuperX implements supers.SuperXInterface {

    /** ticker OR categoryFullName */
    public String ticker;

    public Class eraDataRowClass;

    /** some vars are null!  for X's that don't use the vars map. like... ppl i think? */
    public Map<Class, Var> vars;
    /** sorted by minDate decreasing */
    public List<Era> eras;

    public File varsFile;
    public File erasFile;

    public List<Class> varClasses;

    /**only Var elements that have the boolean variable set to show it's to be used for category averaging */
    public Map<Class, Var> vars_catAveable;

    public SuperX(Class childEraDataRowClass) {
	this.eraDataRowClass = childEraDataRowClass;
    }

    /** combines empty constructor and setTicker(String).  set the name and filenames.   built from raw downloaded files.*/
    public SuperX(String ticker, Class childEraDataRowClass) throws IOException {
	this.eraDataRowClass = childEraDataRowClass;
	this.ticker = ticker;
	setXFiles();	//should we put fill_emptyVarsMap(); here and remove it from child classes?
    }

    @Override
    public void calculate_data_from_xFiles() throws IOException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException {

	if (varsFile != null)
	    readVarsFromFile();//varsFile
	if (erasFile != null && !erasFile.getAbsolutePath().contains("——")) //if contains "——", then it's a category full name.  file is a category's averages.
	    readErasFromFile(eraDataRowClass);	//TODO


    }

    @Override
    public void calculate_data_from_categoryAveraging(Seasons seasons, Set<Stock> stocksInTheCategory) throws Var.TriedToPutBadDataTypeInVarDataArray, IllegalAccessException, InstantiationException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	if (vars != null && !stocksInTheCategory.isEmpty()) {
	    initialize_vars_arrays(seasons.numDays);
	    calculate_vars_categoryAveraging(seasons, stocksInTheCategory);
	}
    }

    public void updateMainDataForCurrentPrices() {
	calculate_vars_origination(this, true);
    }

    public void createNew_varsMap() {
	vars = new LinkedHashMap();
    }

    public void createNew_varClasses() {
	varClasses = new ArrayList();
    }

    /** vars contain no data.  fills map with blanks */
    public void fill_emptyVarsMap() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	createNew_varsMap();
	createNew_varClasses();
	fill_varClasses();

	for (Class c : varClasses) {
	    Var var = (Var)c.newInstance();

	    if (DataManager.catAveableVars != null) {
//		G.asdf("NOT NULL!!!");
//		System.exit(0);
		if (DataManager.catAveableVars.contains(var.getClass()))
		    var.isForCatAveComparison = true;
		else
		    var.isForCatAveComparison = false;
	    }

	    Var.putInMap(vars, var);
	}

	vars_catAveable = C.get_vars_catAveable(vars);
    }

    /** new data arrays length is hps.data.length + 1 to account for current prices that we'll insert later! */
    public void fill_vars(SuperX x, int dataLength) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	initialize_vars_arrays(dataLength);
	calculate_vars_origination(x, false);
    }

    public void initialize_vars_arrays(int length) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	for (Var var : vars.values()) {	//test before deleting old way
//	    if (!var.isTemp)
	    var.initializeArray(length);
	}
    }

    public void writeToDisk() throws IOException {
	if (vars != null)
	    Var.writeVarsToDisk(vars, varsFile);
	if (eras != null)
	    Era.writeErasToFile(eras, erasFile);
    }

    @Deprecated /** KEEP.  for testing -- make sure it reprints the same files!*/
    public void writeToDisk(String parentTestingDirInC) throws IOException {
	Var.writeVarsToDisk(vars, new File("C:\\" + parentTestingDirInC + "\\varstesting.csv"));
	Era.writeErasToFile(eras, new File("C:\\" + parentTestingDirInC + "\\erastesting.csv"));
    }

    public void readVarsFromFile() throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
//	fill_emptyVarsMap();					    //this happens in X constructor
	Var.readFromDisk_intoPreInitializedMap(varsFile, vars);
    }

    /** TODO. 
     @param t_eraDataRow_Class describe what this is used for */
    public void readErasFromFile(Class t_eraDataRow_Class) throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, ParseException {

//	System.out.println(erasFile + " ---- " + t_eraDataRow_Class);

	try {
	    eras = Era.readFromDisk(erasFile, t_eraDataRow_Class);
	} catch (Exception e) {	    //debugging
	    System.out.println(erasFile);
	    e.printStackTrace();
	    System.exit(0);
	}
    }

    /** not called if vars is null.  todo vars_catAveable only uses this vars list, and all the Vars in there use float arrays */
    public void calculate_vars_categoryAveraging(Seasons catAvSeasons, Set<Stock> stocksInTheCategory) throws Var.TriedToPutBadDataTypeInVarDataArray {
	/*			*	//do all the vars at once to minimize date map access!!
	 *	for each day (dateInt) in seasons (using seasons' datesArray, at index i)
	 *	    initialize two maps, using varsMap (need list of all Vars): 
	 *		(1) sumsMap Map<key: Class (of the variable), value: Float (sum)>
	 *		(2) countsMap Map<key: Class (of the variable), value: Float (count)>
	 *	    for each stock in category
	 *		get date's index k in stocks vars.  where is date_i__map? in Stock
	 *		for each Var in varsMap
	 *		    modify the day's sumsMap and countsMap for this Var based on this stock's data
	 *	    for each Var in CatAving X
	 *		calculate average (usings sumsMap and countsMap via var's class)
	 *		place average in CatAvingX's Var's array at index i			*/
	for (int i = 0; i < catAvSeasons.datesArray.length; i++) {
	    int dateInt = catAvSeasons.datesArray[i];
	    calculateAndInsertVarAveragesIn__vars_catAveable_amongStocksOnThisDayAtThisDateIndex(dateInt, i, stocksInTheCategory);
	}
    }

    public void update_category_averaging_for_currentPrices(Seasons catAvSeasons, Set<Stock> stocksInTheCategory) {

	if (vars != null) {
	    int dateInt = G.currentDate;

	    Integer i = catAvSeasons.date_i__map.get(dateInt);

	    if (i != null) //i is null when date_i__map doesn't contain dateInt -- like if dateInt is a weekend date.  that happens if i'm running this and downloading current prices over the weekend.
		calculateAndInsertVarAveragesIn__vars_catAveable_amongStocksOnThisDayAtThisDateIndex(dateInt, i, stocksInTheCategory);
	}
    }

    private Map<Class, MyFloat> make__class_MyFloat__map(Map<Class, Var> vars) {
	Map<Class, MyFloat> map = new HashMap<>(vars.size());
	for (Class varClass : vars.keySet())
	    map.put(varClass, new MyFloat(0));
	return map;
    }

    /**handles null_floats */
    private void adjustAveragingMaps(Map<Class, MyFloat> sumsMap, Map<Class, MyFloat> countsMap, Class varClass, float value) {

	if (G.isnull(value) || G.is_weird(value))
	    return;
	sumsMap.get(varClass).add(value);
	countsMap.get(varClass).add(1);
    }

    /** for building catAves -- DIFFERENT from catAveComparisons!! */
    private float getAverage(Map<Class, MyFloat> sumsMap, Map<Class, MyFloat> countsMap, Class varClass) {
	float numerator = sumsMap.get(varClass).value;
	float denominator = countsMap.get(varClass).value;

//	System.out.println(numerator + "  " + denominator);

	if (G.isnull(numerator) || G.is_weird(numerator) || G.isnull(denominator) || G.is_weird(denominator))
	    return G.null_float;

	float value = numerator / denominator;

	if (G.isnull(value) || G.is_weird(value))
	    return G.null_float;

	return value;
    }

    /** @param catsAves  Map<C.CategoryType, CatAves>  is keys: the categoryTypes we want to study, and Values: their averages */
    public void calculate_catAveComparisons(Map<C.CategoryType, CatAves> catsAves, int stockDataLength, boolean doUpdateForCurrentPrices, int[] date, Map<Integer, Integer> date_i__map) {

	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

//	G.asdf("awefadffawefadfff siZe: vars_catAveable.entrySet().size(): " + vars_catAveable.entrySet().size());
	
	for (Entry<Class, Var> entry : vars_catAveable.entrySet()) {
	    Var var = entry.getValue();
	    Class varClass = entry.getKey();

	    if (!doUpdateForCurrentPrices)
		var.initializeCatComparisonArrays(catsAves.keySet(), stockDataLength);

	    for (Entry<C.CategoryType, CatAves> entry2 : catsAves.entrySet()) {
		C.CategoryType ct = entry2.getKey();
		CatAves catAves = entry2.getValue();

		if (catAves != null) {
		    @SuppressWarnings("MismatchedReadAndWriteOfArray")
		    float[] xVarCompAr = var.catComparisonArrays.get(ct);    //no needs to be speciifc to cat class
		    float[] xVarOrigAr = var.ar_float;					    //cat-comparable vars always use float
		    float[] varCatAvesAr = catAves.vars_catAveable.get(varClass).ar_float;

		    //dates should be decreasing (so 0 is today.  cos this is a normal data stock 
		    for (int x_i = 0; x_i < Math.min(iterMax, stockDataLength); x_i++) {

			//WE NEED TO KNOW WHAT THE DATE IS FOR INDEX x_i SO WE CAN GET THE RIGHT CATEGORY AVE FROM THE CATAVES DATA.  NOT USING SAME DATES INDICES!!!

			int dateInt = date[x_i];

			Integer catAves_i = catAves.get_array_index_for_date(dateInt);

			if (catAves_i == null)
			    continue;

			float FINAL = xVarOrigAr[x_i];
			float INITIAL = varCatAvesAr[catAves_i];

			if (!G.isnull(FINAL) && !G.isnull(INITIAL)) {	//else, array keeps it's null_float value
//			    float comparison = 100 * (FINAL - INITIAL) / INITIAL;
			    float comparison = FINAL - INITIAL;

			    if (!G.is_weird(comparison))
				xVarCompAr[x_i] = comparison;
			}
		    }
		}
	    }
	}
    }

    public static int length(objects.prices.X prices_X) {
	return prices_X.vars.get(objects.prices.X.date.class).ar_int.length;
    }

    public float[] vol(X prices_X) {
	return prices_X.vars.get(objects.prices.X.vol.class).ar_float;
    }

    public float[] close(X prices_X) {
	return prices_X.vars.get(objects.prices.X.close.class).ar_float;
    }

//    public float[] open(X prices_X) {
//	return prices_X.vars.get(objects.prices.X.open.class).ar_float;
//    }

//    public float[] high(X prices_X) {
//	return prices_X.vars.get(objects.prices.X.high.class).ar_float;
//    }
//    public float[] low(X prices_X) {
//	return prices_X.vars.get(objects.prices.X.low.class).ar_float;
//    }
    public int[] date(X prices_X) {
	return prices_X.vars.get(objects.prices.X.date.class).ar_int;
    }

    /** stocksInTheCategory should not be empty!! */
    private void calculateAndInsertVarAveragesIn__vars_catAveable_amongStocksOnThisDayAtThisDateIndex(int dateInt, int catAves_i, Set<Stock> stocksInTheCategory) {

	if (vars == null) {
	    System.out.println("EWRWErwr32r null vars! ");
	    System.out.println("EWRWErwr32r " + ticker);
	    System.out.println("EWRWErwr32r " + this.eraDataRowClass);
	    System.out.println("EWRWErwr32r " + this.getClass());
	} else {
//	    if (dateInt == G.currentDate)
//		System.out.println("23r23r date: " + dateInt);//+ ", stock: " + stock.name + "var: " + averagee_varClass + ", value: " + averagee_var_value);
	    Map<Class, MyFloat> sumsMap = make__class_MyFloat__map(vars_catAveable);
	    Map<Class, MyFloat> countsMap = make__class_MyFloat__map(vars_catAveable);	    //just changed to vars_catAveable from vars.  hope i didn't screw anything up

	    for (Stock stock : stocksInTheCategory) {

		Integer k = stock.date_i__map.get(dateInt);
//		if (dateInt == G.currentDate)
//		    System.out.println("23r23r date: " + dateInt + ", stock: " + stock.name + "k: " + k);// + "var: " + averagee_varClass + ", value: " + averagee_var_value);
		if (k != null) {									//will be null on placeholder cell for today.  fill with current prices later
		    for (Class averagee_varClass : vars_catAveable.keySet()) {
			float averagee_var_value = stock.vars.get(averagee_varClass).ar_float[k];	//catAveable vars always use float
			adjustAveragingMaps(sumsMap, countsMap, averagee_varClass, averagee_var_value);
//			if (dateInt == G.currentDate)
//			    System.out.println("23r23r date: " + dateInt + ", stock: " + stock.name + "var: " + averagee_varClass + ", value: " + averagee_var_value);
		    }
		}
	    }
	    for (Entry<Class, Var> entry : vars_catAveable.entrySet()) {
		@SuppressWarnings("MismatchedReadAndWriteOfArray")
		float[] catAvVarAr = entry.getValue().ar_float;
		float value = getAverage(sumsMap, countsMap, entry.getKey());
		catAvVarAr[catAves_i] = value;
	    }
	}
    }

    /** NOTE -- this will take slightly longer than keeping temp vars in memory.  because will have to recalculate EMA's, etc.  but hopefully will save lots of mem???? */
    public void reinitializeVarsIfYouNeedTo__forIsTempVars(int length) {

	if (vars != null) {
	    for (Var var : vars.values()) {
		if (var.isTemp)
		    var.initializeArray(length);
	    }
	}
    }

    /** must ensure that values are not null before building this.  DOES NOT HANDLE null_float! */
    public static class MyFloat {
	public float value;

	public MyFloat(float x) {
	    this.value = x;
	}

	public void add(float x) {
	    this.value += x;
	}

	@Override
	public String toString() {
	    return value + "";
	}
    }


}
