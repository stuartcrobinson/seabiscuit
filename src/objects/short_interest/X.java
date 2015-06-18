package objects.short_interest;
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

//http://www.nyxdata.com/Data-Products/NYSE-MKT-Short-Interest

public final class X extends SuperX implements supers.SuperXInterface, supers.XInterface_uses_Vars {

    /** cooked-disseminationdate key, original SI value - map */
    private Map<Integer, ShortInterest> map__cookedDissDate_SI;
    private Map<Integer, ShortInterest> map__settlementDate_SI;

    private Map<Integer, Integer> map__settlementDate_disseminationDate;
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
	map__settlementDate_disseminationDate = ShortInterest.getDisseminationDates_from_separateFile();

	this.prices_X = prices_X;
	List<ShortInterest> sis = ShortInterest.readFromDisk(ticker);
	ShortInterest.setDisseminationDatesInSI_list(sis, map__settlementDate_disseminationDate);
	map__cookedDissDate_SI = cookSIDisseminationDates(sis);
	map__settlementDate_SI = make_settlementDates_SI__map(sis);
    }

    @Override
    public void setXFiles() throws IOException {
	setXFile_vars();
    }

    @Override
    public void setXFile_vars() throws IOException {
	varsFile = G.newChildTickerFile(G.XShortInterest_vars, ticker);
    }

    @Override
    public void fill_data__origination(SuperX x) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	fill_vars(x, length(prices_X));
    }

    @Override
    public void fill_varClasses() {
	varClasses.add(X.SI_numDaysUntilSettlement.class);
	varClasses.add(X.SI_numDaysSinceSettlement.class);
	varClasses.add(X.SI_numDaysUntilCookedDissemination.class);
	varClasses.add(X.SI_numDaysSinceCookedDissemination.class);
	varClasses.add(X.SI_recent_DaysToCover.class);
    }

    /** this actually just modifies the original short interests and returns a reference.  replaces each date with the following trading day, since reports come out after 4 pm */
    private Map<Integer, ShortInterest> cookSIDisseminationDates(List<ShortInterest> sis) throws ParseException {

	Map<Integer, ShortInterest> map = new LinkedHashMap();

	for (ShortInterest si : sis) {
	    map.put(si.getCookedDisseminationDate(), si);
	}
	return map;
    }

    private Map<Integer, ShortInterest> make_settlementDates_SI__map(List<ShortInterest> sis) {

	Map<Integer, ShortInterest> map = new LinkedHashMap();

	for (ShortInterest si : sis) {
	    map.put(si.settlementDate, si);
	}
	return map;
    }

    @Override
    public void calculate_vars_origination(SuperX x, boolean doUpdateForCurrentPrices) {
	if (doUpdateForCurrentPrices)
	    return;

	ShortInterest[] dummyArSI_cookedDissDates = create_dummyArSI_nullEverwhereExceptForIndexOfDate_that_is_in_map(map__cookedDissDate_SI, doUpdateForCurrentPrices);
	ShortInterest[] dummyArSI_settlementDates = create_dummyArSI_nullEverwhereExceptForIndexOfDate_that_is_in_map(map__settlementDate_SI, doUpdateForCurrentPrices);

	((SI_numDaysUntilSettlement)vars.get(X.SI_numDaysUntilSettlement.class)).calculateData(dummyArSI_settlementDates, doUpdateForCurrentPrices);
	((SI_numDaysSinceSettlement)vars.get(X.SI_numDaysSinceSettlement.class)).calculateData(dummyArSI_settlementDates, doUpdateForCurrentPrices);
	((SI_numDaysUntilCookedDissemination)vars.get(X.SI_numDaysUntilCookedDissemination.class)).calculateData(dummyArSI_cookedDissDates, doUpdateForCurrentPrices);
	((SI_numDaysSinceCookedDissemination)vars.get(X.SI_numDaysSinceCookedDissemination.class)).calculateData(dummyArSI_cookedDissDates, doUpdateForCurrentPrices);
	((SI_recent_DaysToCover)vars.get(X.SI_recent_DaysToCover.class)).calculateData(x, dummyArSI_cookedDissDates, doUpdateForCurrentPrices);
    }

    private ShortInterest[] create_dummyArSI_nullEverwhereExceptForIndexOfDate_that_is_in_map(Map<Integer, ShortInterest> map__date_SI, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 17 : Integer.MAX_VALUE;

	int[] date = date(prices_X);

	ShortInterest[] dummyArSI = new ShortInterest[length(prices_X)];

	for (int i = 0; i < Math.min(iterMax, dummyArSI.length); i++) {
	    try {
		int dateInt = get(date, i);
//		if (map__date_SI.containsKey(dateInt))
//		    dummyArSI[i] = map__date_SI.get(dateInt);
		ShortInterest si = map__date_SI.get(dateInt);
		if (si != null) //                                        will this save any time?  i could just be putting si directly in dummyArSI since it's supposed to be null where it's not found.  but will that extra memory manipulation take time?  i think checking boolean is faster 
		    dummyArSI[i] = map__date_SI.get(dateInt);
	    } catch (G.My_null_exception ex) {
	    }
	}
	return dummyArSI;
    }


    /** should this be moved more centrally so other methods can use it? */
    Integer[] create_arIndexOfRecentSI(ShortInterest[] dummyArSI, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 17 : Integer.MAX_VALUE;

	Integer[] arIndexOfRecentSI = new Integer[length(prices_X)];

	Integer lastSI_release_cookedDate = null;

	for (int i = Math.min(iterMax, length(prices_X) - 1); i >= 0; i--) {
	    if (dummyArSI[i] != null) {
		lastSI_release_cookedDate = i;
	    }
	    arIndexOfRecentSI[i] = lastSI_release_cookedDate;	//null when there hasn't been a release yet
	}
	return arIndexOfRecentSI;
    }
//SI_numDaysUntilCookedSettlement

    public static class SI_numDaysUntilSettlement extends Var {
	public SI_numDaysUntilSettlement() {
	    arrayDataType = Type.INTEGER;
	}

	private void calculateData(ShortInterest[] dummyArSI, boolean doUpdateForCurrentPrices) {
	    G.calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(ar_int, dummyArSI, doUpdateForCurrentPrices);
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class SI_numDaysSinceSettlement extends Var {
	public SI_numDaysSinceSettlement() {
	    arrayDataType = Var.Type.INTEGER;
	}

	private void calculateData(ShortInterest[] dummyArSI, boolean doUpdateForCurrentPrices) {
	    G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(ar_int, dummyArSI, doUpdateForCurrentPrices);
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class SI_numDaysUntilCookedDissemination extends Var {
	public SI_numDaysUntilCookedDissemination() {
	    arrayDataType = Type.INTEGER;
	}

	private void calculateData(ShortInterest[] dummyArSI, boolean doUpdateForCurrentPrices) {
	    G.calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(ar_int, dummyArSI, doUpdateForCurrentPrices);
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class SI_numDaysSinceCookedDissemination extends Var {
	public SI_numDaysSinceCookedDissemination() {
	    arrayDataType = Var.Type.INTEGER;
	}

	private void calculateData(ShortInterest[] dummyArSI, boolean doUpdateForCurrentPrices) {
	    G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(ar_int, dummyArSI, doUpdateForCurrentPrices);
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class SI_recent_DaysToCover extends Var {
	public SI_recent_DaysToCover() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	private void calculateData(SuperX x, ShortInterest[] dummyArSI, boolean doUpdateForCurrentPrices) {
	    int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

	    Integer[] arIndexOfRecentSI = ((X)x).create_arIndexOfRecentSI(dummyArSI, doUpdateForCurrentPrices);

	    for (int i = 0; i < Math.min(iterMax, length(((X)x).prices_X)); i++) {					//wtf why
		Integer indexOfRecentSI = arIndexOfRecentSI[i];
		ar_float[i] = indexOfRecentSI == null ? G.null_float : dummyArSI[indexOfRecentSI].daysToCover;
	    }
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    @Override //empty - not needed.  short interest data not affected by current prices.
    public void setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(Stock stock) {
    }

}
