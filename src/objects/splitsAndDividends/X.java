package objects.splitsAndDividends;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import objects.Stock;
import utilities.G;
import static utilities.G.get;
import supers.SuperX;
import supers.Var;

public final class X extends SuperX implements supers.SuperXInterface, supers.XInterface_uses_Vars {

    /** date (precooked) key, original split value - map. the cooked date is the date of the first trading day close following an event.  only valid when NOT READ FROM XFILES */
    public Map<Integer, Split> splitsMap;
    objects.prices.X prices_X;

    public X() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(null);
	fill_emptyVarsMap();
    }

    public X(String ticker) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(ticker, null);
	fill_emptyVarsMap();
    }

    public void calculate_data_origination(objects.prices.X prices_X) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	try {
	    setAttributes(prices_X);
	    fill_data__origination(this);
	} catch (G.No_DiskData_Exception ex) {
	}
    }

    public void setAttributes(objects.prices.X prices_X) throws IOException, ParseException, G.No_DiskData_Exception {
	this.prices_X = prices_X;
	
	splitsMap = prices_X.splitsMap;		//prices needs to get splits first, so it can nullify the prices data surrounding splits.  because prices data is unreliable around splits.  see ALE 20040920 https://www.google.com/finance/historical?cid=24610&startdate=Sep+15%2C+2004&enddate=Sep+25%2C+2004&num=30&ei=mbc2VbCKIeSPsgeS14DoCw and http://finance.yahoo.com/q/hp?s=ALE&a=08&b=15&c=2004&d=11&e=25&f=2004&g=d
//	
//	List<Split> splits = Split.readFromDisk(ticker);
//	splitsMap = new LinkedHashMap();
//	for (Split split : splits)
//	    splitsMap.put(split.date, split);
    }


    @Override
    public void setXFiles() throws IOException {
	setXFile_vars();
    }

    @Override
    public void setXFile_vars() throws IOException {
	varsFile = G.newChildTickerFile(G.XSplits_vars, ticker);
    }

    @Override
    public void fill_data__origination(SuperX x) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	fill_vars(x, length(prices_X));
    }

    @Override
    public void fill_varClasses() {
	varClasses.add(X.spl_numDaysSinceGood.class);
	varClasses.add(X.spl_numDaysUntilGood.class);
	varClasses.add(X.spl_numDaysSinceBad.class);
	varClasses.add(X.spl_numDaysUntilBad.class);
    }

    @Override
    public void calculate_vars_origination(SuperX x, boolean doUpdateForCurrentPrices) {
	if (doUpdateForCurrentPrices)
	    return;
	//make array dummyArSI of ShortInterests that aligns with []date.  make the cell null unless there's a short interest from that date.
	// then use dummyArSI to calcualte daysUntil and daysSince and recent days to cover
	// make another dummy array: arIndexOfRecentSI - to speed up accessing the recent report.  to avoid search each time.

	Split[] dummyArSpl_good = create_dummyArSpl_good_nullEverwhereExceptForDatesEqualA_Spl_cookedDate(doUpdateForCurrentPrices);
	Split[] dummyArSpl_bad = create_dummyArSpl_bad_nullEverwhereExceptForDatesEqualA_Spl_cookedDate(doUpdateForCurrentPrices);

	((spl_numDaysSinceGood)vars.get(spl_numDaysSinceGood.class)).calculateData(dummyArSpl_good, doUpdateForCurrentPrices);
	((spl_numDaysUntilGood)vars.get(spl_numDaysUntilGood.class)).calculateData(dummyArSpl_good, doUpdateForCurrentPrices);
	((spl_numDaysSinceBad)vars.get(spl_numDaysSinceBad.class)).calculateData(dummyArSpl_bad, doUpdateForCurrentPrices);
	((spl_numDaysUntilBad)vars.get(spl_numDaysUntilBad.class)).calculateData(dummyArSpl_bad, doUpdateForCurrentPrices);
    }


    private Split[] create_dummyArSpl_good_nullEverwhereExceptForDatesEqualA_Spl_cookedDate(boolean doUpdateForCurrentPrices) {

	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

	Split[] dummyArSpl = new Split[length(prices_X)];

	int[] date = date(prices_X);

	for (int i = 0; i < Math.min(iterMax, dummyArSpl.length); i++) {
	    try {
		int dateInt = get(date, i);
		if (splitsMap.containsKey(dateInt))
		    if (splitsMap.get(dateInt).isGood())
			dummyArSpl[i] = splitsMap.get(dateInt);
	    } catch (G.My_null_exception ex) {
	    }
	}
	return dummyArSpl;
    }


    private Split[] create_dummyArSpl_bad_nullEverwhereExceptForDatesEqualA_Spl_cookedDate(boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

	Split[] dummyArSpl = new Split[length(prices_X)];

	int[] date = date(prices_X);

	for (int i = 0; i < Math.min(iterMax, dummyArSpl.length); i++) {

	    try {
		int dateInt = get(date, i);
		if (splitsMap.containsKey(dateInt))
		    if (!splitsMap.get(dateInt).isGood())
			dummyArSpl[i] = splitsMap.get(dateInt);
	    } catch (G.My_null_exception ex) {
	    }
	}
	return dummyArSpl;
    }

    public static class spl_numDaysSinceGood extends Var {
	public spl_numDaysSinceGood() {
	    arrayDataType = Type.INTEGER;
	}

	private void calculateData(Split[] dummyArSpl_good, boolean doUpdateForCurrentPrices) {
	    G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(ar_int, dummyArSpl_good, doUpdateForCurrentPrices);
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class spl_numDaysUntilGood extends Var {
	public spl_numDaysUntilGood() {
	    arrayDataType = Type.INTEGER;
	}

	private void calculateData(Split[] dummyArSpl_good, boolean doUpdateForCurrentPrices) {
	    G.calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(ar_int, dummyArSpl_good, doUpdateForCurrentPrices);
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class spl_numDaysSinceBad extends Var {
	public spl_numDaysSinceBad() {
	    arrayDataType = Type.INTEGER;
	}

	private void calculateData(Split[] dummyArSpl_bad, boolean doUpdateForCurrentPrices) {
	    G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(ar_int, dummyArSpl_bad, doUpdateForCurrentPrices);
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class spl_numDaysUntilBad extends Var {
	public spl_numDaysUntilBad() {
	    arrayDataType = Type.INTEGER;
	}

	private void calculateData(Split[] dummyArSpl_bad, boolean doUpdateForCurrentPrices) {
	    G.calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(ar_int, dummyArSpl_bad, doUpdateForCurrentPrices);
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    @Override //empty - not needed.  splits data not affected by current prices
    public void setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(Stock stock) {
    }
}
