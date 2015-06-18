package categories.category_earnings_response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utilities.G;
import supers.SuperX;
import supers.Var;
import categories.Seasons;
import categories.Seasons.Season;
import objects.Stock;
import objects.earnings.Earnings_EraDataRow;

/**  for CategoryEarningsResponses.  NOT CONNECTED TO SEASONS.  each y has it's own dates and date_i__map.  also, each Y will be different length! otherwise, all y's will [have all their data arrays be] maximum length!  will save space by adding a dates array.  Y is like a stock's X, except it is tied to a specific category rather than a stock.  it holds daily Var data relating to earnings responses.  <br><br> Y objects are NOT TIED TO A TICKER!  X objects are attached to a specific ticker.  NOT Y!  Y is like macrodata.  could be extended for macroeconomic data?*/
public final class Y {

    String categoryFullName;

    File varsFile;

    public Map<Class, Var> vars;
    public List<Class> varClasses;
    public Map<Integer, Integer> date_i__map;

    /** for demoY for data.meta */
    public Y() throws InstantiationException, IllegalAccessException {
	initialize_varsMap_and_varClasses();
    }

    /** DOES NOT CALCULATE! */
    public Y(String categoryFullName) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IOException, FileNotFoundException, ClassNotFoundException {
	this.categoryFullName = categoryFullName;
	varsFile = G.getCategoryEarningsResponseFile(categoryFullName); //.newChildTickerFile(G.XEarnings_vars, ticker);
	initialize_varsMap_and_varClasses();
    }

    public void calculate_data_from_xFiles() throws IOException, FileNotFoundException, ClassNotFoundException {
	Var.readFromDisk_intoPreInitializedMap(varsFile, vars);
	setDatesStuff();
    }

    public void calculate_data_origination(Set<Stock> shrinkingStocksSet, Seasons seasons, Map<String, List<Earnings_EraDataRow>> earnings, boolean doUpdateForCurrentPrices) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	if (!doUpdateForCurrentPrices)
	    initialize_vars_arrays(seasons.numDays);
	calculateDataArrays(shrinkingStocksSet, seasons, earnings, doUpdateForCurrentPrices);
	trimArrays();
	setDatesStuff();
    }

    public void initialize_varsMap_and_varClasses() throws InstantiationException, IllegalAccessException {
	varClasses = new ArrayList();
	vars = new LinkedHashMap();
	Var.initializeVars(varClasses, vars,
		Y.cer_date.class,
		Y.cer_n.class,
		Y.cer_priceChPct.class,
		Y.cer_pctGt0.class,
		Y.cer_pctGt2.class,
		Y.cer_pctGt4.class);
    }


    public void initialize_vars_arrays(int length) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	for (Var var : vars.values()) {	//test before deleting old way
	    var.initializeArray(length);
	}
    }

    void writeToDisk() throws IOException {
	//name of varsFile is like tickerfile but ticker is catFullName
	Var.writeVarsToDisk(vars, varsFile);
    }

    public void readVarsFromFile() throws FileNotFoundException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
//	fill_emptyVarsMap();					    //this happens in X constructor
	Var.readFromDisk_intoPreInitializedMap(varsFile, vars);
	setDatesStuff();
    }

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    void calculateDataArrays(Set<Stock> shrinkingStocksSet, Seasons seasons, Map<String, List<Earnings_EraDataRow>> earningsMap, boolean doUpdateForCurrentPrices) {

	int[] _date = vars.get(Y.cer_date.class).ar_int;	    //why is _date null?	//cos datestuff hasn't been called yet!!!!!!!
	int[] _n = vars.get(Y.cer_n.class).ar_int;
	float[] _priceChPct = vars.get(Y.cer_priceChPct.class).ar_float;
	float[] _pctGt0 = vars.get(Y.cer_pctGt0.class).ar_float;
	float[] _pctGt2 = vars.get(Y.cer_pctGt2.class).ar_float;
	float[] _pctGt4 = vars.get(Y.cer_pctGt4.class).ar_float;


	//i think that even for doUpdateForCurrentPrices, we have to iterate over entire season to calculate accurate value balanced averages
	for (int j = 0; j < seasons.seasonList.size(); j++) {
	    if (doUpdateForCurrentPrices && j < seasons.seasonList.size() - 1)
		continue;
	    Season season = seasons.seasonList.get(j);

	    Integer n = 0;
	    Float priceChPct = null, pctGt0 = null, pctGt2 = null, pctGt4 = null;

	    List<Float> pricePctChanges = new ArrayList();
	    Set<Stock> shrinking_set_of_stocks_who_havent_released_earnings_yet__seasonal = new LinkedHashSet(shrinkingStocksSet);

	    for (Integer dateInt : season.dates) {	    //TODO TODO dang ... every day, we're searching earnings for every stock .... :/  limit to valid dates per stock!?
		boolean changedToday = false;

//		System.out.println("g54g54yt54t seasonJ" + j + ", dateInt" + dateInt + "" + "");

		//for this day dateInt, build a set of all the pctPriceChanges following earnings reports w/ cooked date of dateInt
		for (Iterator<Stock> iter = shrinking_set_of_stocks_who_havent_released_earnings_yet__seasonal.iterator(); iter.hasNext();) {
		    Stock stock = iter.next();

		    if (stock == null) {
			System.out.println("WTF NULL! awefa948u9awe8fu");
		    }

		    if (stock.containsDate(dateInt)) {

			List<Earnings_EraDataRow> earnings = earningsMap.get(stock.name);

//			//testing
//			System.out.println(stock.name);
//			for (Earnings_EraDataRow e : earnings) {
//			    System.out.println(e.cookedDate + ",   " + e.toOutputStringEDR());
//			}
//			System.exit(0);

			for (Earnings_EraDataRow e : earnings) {
			    if (e.e_cookedDate == dateInt) {

				float pricePctChange = e.e_pricePctChange;

				if (!G.isnull(pricePctChange)) {
				    pricePctChanges.add(pricePctChange);
				    changedToday = true;
				}
//				System.out.println("here awefa948u9awe8fu REMOVING! " + stock.name + " " + dateInt);
				iter.remove();		    //shrink the set of tickers!
				break;
				/*
				 //sometimes, a stock will be listed in our earnings set twice in same period.  conflicting time stamp / cooked date.  here, we're just taking the first one.  the discovered error was AAN
				 it looks like estimize and something else listed different times and dates.  but very similar -- same cooked date!
				 20131025,   AAN	Aaron's Inc.	20131024	23:19	true			false	FQ3 '13		1							0.40		0.40	539.52		539.50¯20131025¯-0.04¯5¯0
				 20131025,   AAN	Aarons Inc	20131025	01:19	true	0.40	0.40	false															¯20131025¯-0.04¯5¯0
				 */
			    }
			}
		    }
		}

		//now we've checked everything for this day.  calculate the day's category earnings response value!
		//  add a boolean changedYesterday value to prevent recalculating the same data every day (if there's no new earnings releases)!  or we could see if the earnings set is bigger than it was before ....

		int i = seasons.date_i__map.get(dateInt);
		_date[i] = G.toPrimitive(dateInt);

		if (changedToday) {					    //this works because we're iterating through an increasing dates set

		    n = pricePctChanges.size();
		    priceChPct = G.getMean(pricePctChanges);
		    pctGt0 = G.getPctGteX(pricePctChanges, 0);
		    pctGt2 = G.getPctGteX(pricePctChanges, 2);
		    pctGt4 = G.getPctGteX(pricePctChanges, 4);

		}
		if (n != null) {					    //values reset to null at beginning of each season
		    _n[i] = G.toPrimitive(n);
		    _priceChPct[i] = G.toPrimitive(priceChPct);
		    _pctGt0[i] = G.toPrimitive(pctGt0);
		    _pctGt2[i] = G.toPrimitive(pctGt2);
		    _pctGt4[i] = G.toPrimitive(pctGt4);
		}
	    }
	}
//	for (int i = 0; i < _date.length; i++) {
//	    System.out.println("r4tr4 :: " + i + " " + _date[i] + " " + _n[i]);
//	    
//	    if (_n[i] == 2){
//		System.out.println("found a 2!");
//		System.exit(0);
//	    }
//	}
//	System.exit(0);
    }

    /** reduces data down to indices of nonnull dates.  is it okay to move the float array pointers? */
    private void trimArrays() {

	//get valid indices from dates
	int[] _date = vars.get(Y.cer_date.class).ar_int;

	List<Integer> valid_indices = new ArrayList();
	for (int i = 0; i < _date.length; i++) {
	    int value = _date[i];
	    if (!G.isnull(value)) {
		valid_indices.add(i);
	    }
	}

	for (Var var : vars.values()) {
	    if (var.arrayDataType.equals(Var.Type.INTEGER)) {
		int[] newar = new int[valid_indices.size()];
		int i = 0;
		for (Integer vi : valid_indices) {
		    newar[i] = var.ar_int[vi];
		    i++;
		}
		var.ar_int = newar;
	    }
	    if (var.arrayDataType.equals(Var.Type.FLOAT)) {
		float[] newar = new float[valid_indices.size()];
		int i = 0;
		for (Integer vi : valid_indices) {
		    newar[i] = var.ar_float[vi];
		    i++;
		}
		var.ar_float = newar;
	    }
	}
    }

    private void setDatesStuff() {
	int[] _date = vars.get(Y.cer_date.class).ar_int;
	date_i__map = G.get__date_i__map(_date);
    }

    public static class cer_date extends Var {
	public cer_date() {
	    arrayDataType = Var.Type.INTEGER;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class cer_n extends Var {
	public cer_n() {
	    arrayDataType = Type.INTEGER;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class cer_priceChPct extends Var {
	public cer_priceChPct() {
	    arrayDataType = Type.FLOAT;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class cer_pctGt0 extends Var {
	public cer_pctGt0() {
	    arrayDataType = Type.FLOAT;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class cer_pctGt2 extends Var {
	public cer_pctGt2() {
	    arrayDataType = Type.FLOAT;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class cer_pctGt4 extends Var {
	public cer_pctGt4() {
	    arrayDataType = Type.FLOAT;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }


}
