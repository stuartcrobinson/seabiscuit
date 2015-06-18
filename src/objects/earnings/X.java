package objects.earnings;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import objects.Stock;
import utilities.G;
import static utilities.G.get;
import supers.Era;
import supers.SuperX;
import supers.Var;

/** this X calculates array data all at once, ignoring Var.calculateData(SuperX x).  but we want to keep it in the interface cos some stuff uses them, like prices */
public final class X extends SuperX implements supers.SuperXInterface, supers.XInterface_uses_Vars, supers.XInterface_uses_Eras {

    /** ticker-specific !!!!!!!!!!!!!!!! */
    private List<Earnings> earningsList;
    private objects.news.X newsX;
    private objects.sec.X secX;
    private objects.prices.X prices_X;

    public X() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(Earnings_EraDataRow.class);
	fill_emptyVarsMap();
    }

    public X(String ticker) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(ticker, Earnings_EraDataRow.class);
	fill_emptyVarsMap();
    }

    public void calculate_data_origination(List<Earnings> earningsList_all, objects.prices.X prices_X, objects.news.X newsX, objects.sec.X secX) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	setAttributes(earningsList_all, prices_X, newsX, secX);
	fill_data__origination(this);
    }

    public final void setAttributes(List<Earnings> earningsList_all, objects.prices.X prices_X, objects.news.X newsX, objects.sec.X secX) throws G.No_DiskData_Exception {
	this.prices_X = prices_X;
	this.earningsList = Earnings.getEarningsForTicker(earningsList_all, ticker);
	if (this.earningsList.isEmpty())
	    throw new G.No_DiskData_Exception();
	this.newsX = newsX;
	this.secX = secX;
    }

    @Override
    public void setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(Stock stock) {
	newsX = stock.news_X;
	secX = stock.sec_X;
	prices_X = stock.prices_X;
    }

    @Override
    public void setXFiles() throws IOException {
	setXFile_vars();
	setXFile_Eras();
    }

    @Override
    public void setXFile_vars() throws IOException {
	varsFile = G.newChildTickerFile(G.XEarnings_vars, ticker);
    }

    @Override
    public void setXFile_Eras() throws IOException {
	erasFile = G.newChildTickerFile(G.XEarnings_eras, ticker);
    }

    @Override
    public final void fill_data__origination(SuperX x) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	fill_eras();    //must build eras before vars! (needed by vars)
	fill_vars(x, length(prices_X));    //vars map requires data in eras for calculations (eras are the financial/key-ratios data!)
    }

    @Override
    public void fill_varClasses() {
	varClasses.add(X.Earnings_numDaysUntil.class);
	varClasses.add(X.Earnings_numDaysSince.class);
    }

    @Override
    public void fill_eras() throws IOException, ParseException, IllegalAccessException {
	//era, here, is earnings_data_row from (minDate) cooked earnings release date.  maxDate is the day before the following earnings' cooked date
	/*
	 private List<Earnings> earningsList;
	 private stuff.objects.news.X newsX;
	 private stuff.objects.sec.X secX;
	 */

	eras = new ArrayList();

	Collections.sort(earningsList);

//	for (Earnings e : earningsList)
//	    G.asdf("89898 " + e.outputLine());	    //looks good. shows all earnings dates

	for (int k = 0; k < earningsList.size(); k++) {
	    Earnings e = earningsList.get(k);

	    //1.  get cooked date
	    //2.  get num sec reports 2 weeks prior
	    //3.  get num press releases 2 weeks prior
	    //4.  build earnings data row object,  then build era!  add it to eras!

	    int cookedDate = getCookedDate(e);
//	    G.asdf("cookedDateawefawef: " + cookedDate);	//looks good

	    try {

		float pricePctChange = G.null_float;
		Integer numSecReportsInPrior2Weeks = null;
		Integer numPressReleasesInPrior2Weeks = null;


		Integer date_i = prices_X.date_i__map.get(cookedDate);
		if (date_i != null) {						    //this is null when cookedDate not in the date_i__map.  this will happen for today's date, before getting currentPrices (i think).  yeah, cos one of the cookedDates could be today.

		    pricePctChange = get_pricePctChange(date_i, close(prices_X));		    //this could be null_float!  if cooked date is today and we don't have current prices yet
		    numSecReportsInPrior2Weeks = secX == null ? null : get_numSecReportsInPrior2Weeks(date_i, secX);
		    numPressReleasesInPrior2Weeks = newsX == null ? null : get_numPressReleasesInPrior2Weeks(date_i, newsX);

		}

		Earnings_EraDataRow eedr = new Earnings_EraDataRow(e, cookedDate, pricePctChange, numSecReportsInPrior2Weeks, numPressReleasesInPrior2Weeks);

		int minDate = cookedDate;
		int maxDate = k + 1 < earningsList.size() ? getCookedDate(earningsList.get(k + 1)) - 1 : cookedDate + 100;	    //day before next earnings' date (okay if not real date) vs. random point in future, if no next earnings available
		Era era = new Era(minDate, maxDate, eedr);

		eras.add(era);
	    } catch (Exception ex) {
		//for testing.  debugging.
		System.out.println(cookedDate);
		System.out.println(ticker);
		ex.printStackTrace();
		System.exit(0);
	    }
	}
	Collections.sort(eras, Collections.reverseOrder());	    //reverse - most recent dates first
    }

    /** only thing needs adjusting is pricePctChange following the report */
    public void updateErasForCurrentPrices() {
	if (eras == null || eras.isEmpty())
	    return;

	int dateInt = G.currentDate;
	int date_i = prices_X.date_i__map.get(dateInt);

	Earnings_EraDataRow eedr = (Earnings_EraDataRow)eras.get(0).eraDataRow;
	if (dateInt == eedr.e_cookedDate) {
	    float pricePctChange = get_pricePctChange(date_i, close(prices_X));
	    eedr.e_pricePctChange = pricePctChange;
	}
    }

    private float get_pricePctChange(int date_i, float[] close) {

	try {
	    float FINAL = G.get(close, date_i);
	    float INITIAL = G.get(close, date_i + 1);
	    return 100 * (FINAL - INITIAL) / INITIAL;
	} catch (ArrayIndexOutOfBoundsException | G.My_null_exception e) {
	    return G.null_float;
	}
    }


    /** does this work? */
    private static int get_numSecReportsInPrior2Weeks(int date_i, objects.sec.X secX) {

	try {
	    float[] _numSecReports = secX.vars.get(objects.sec.X.Sec_NumReports.class).ar_float;

	    int countSECReports = 0;

	    for (int i = date_i; i < _numSecReports.length && i < date_i + 10; i++) {
		countSECReports += get(_numSecReports, i);
	    }
	    return countSECReports;
	} catch (G.My_null_exception ex) {
	    return G.null_int;			    //this shouldn't ever happen.  numSecReports should be positive or 0
	}
    }

    private static int get_numPressReleasesInPrior2Weeks(int date_i, objects.news.X newsX) {
	try {
	    float[] _numPressReleases = newsX.vars.get(objects.news.X.News_numPressReleases.class).ar_float;

	    int countPressReleases = 0;

	    for (int i = date_i; i < _numPressReleases.length && i < date_i + 10; i++) {
		countPressReleases += get(_numPressReleases, i);
	    }
	    return countPressReleases;
	} catch (G.My_null_exception ex) {
	    return G.null_int;			    //this shouldn't ever happen.  numSecReports should be positive or 0
	}
    }

    private static int getCookedDate(Earnings e) throws ParseException {

	if (e.time == null || e.time.isEmpty())
	    return Integer.parseInt(e.date);		//if no time, then assume listed date is cooked date (date of first market close after event)

	int hour = Integer.parseInt(e.time.substring(0, 2));

	if (hour < 16)
	    return Integer.parseInt(e.date);
	else
	    return Integer.parseInt(G.getNextWeekdayDate(e.date));
    }

    /* things needed for calculating arrays */

    @Override @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public void calculate_vars_origination(SuperX x, boolean doUpdateForCurrentPrices) {
	if (doUpdateForCurrentPrices)
	    return;

	int[] _daysUntil = x.vars.get(X.Earnings_numDaysUntil.class).ar_int;
	int[] _daysSince = x.vars.get(X.Earnings_numDaysSince.class).ar_int;

	int numExtraFutureDays = 10;

	Object[] dummyAr_earnings_UNIQUELENGTH = null;
	try {
	    dummyAr_earnings_UNIQUELENGTH = create_dummyAr_earnings_nullEverwhereExceptForDatesEqual_an_earnings_cookedDate(numExtraFutureDays);
	} catch (ParseException ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}

//	System.out.println("awefawef4r4r4calculate_vars_origination " + ticker);

	G.calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(_daysUntil, dummyAr_earnings_UNIQUELENGTH, numExtraFutureDays, doUpdateForCurrentPrices);
	G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(_daysSince, dummyAr_earnings_UNIQUELENGTH, numExtraFutureDays, doUpdateForCurrentPrices);
    }

    private Object[] create_dummyAr_earnings_nullEverwhereExceptForDatesEqual_an_earnings_cookedDate(int numExtraFutureDays) throws ParseException {

	Set<Integer> earningsDates = new HashSet();

	for (Era e : eras) {
	    Earnings_EraDataRow eedr = ((Earnings_EraDataRow)e.eraDataRow);
//	    System.out.println("h4h65y56y date: " + eedr.e.date + "cooked date: " + eedr.cookedDate);

//	    G.asdf("000000 " + eedr.toOutputStringEDR());

	    earningsDates.add(eedr.e_cookedDate);
	}

	Object[] dummyAr = new Object[length(prices_X) + numExtraFutureDays];
	int[] dummyDates = new int[dummyAr.length];

	for (int i = numExtraFutureDays; i < dummyAr.length; i++) {
	    try {
		dummyDates[i] = get(date(prices_X), i - numExtraFutureDays);
	    } catch (G.My_null_exception ex) {
		ex.printStackTrace();
		System.exit(0);
	    }
	}

	for (int i = numExtraFutureDays - 1; i >= 0; i--) {
	    dummyDates[i] = G.getNextWeekDay(dummyDates[i + 1]);
	}

	for (int i = 0; i < dummyAr.length; i++) {
//	    try {
	    int dateInt = dummyDates[i];
	    if (earningsDates.contains(dateInt))
		dummyAr[i] = dateInt;	    //null elsewhere
//	    } catch (G.My_null_exception ex) {
//	    }
	}

//	for (int i = 0; i < 80 && i < dummyDates.length; i++) {
//	    G.asdf("awefawefeee " + i + "  " + dummyDates[i] + "   " + dummyAr[i]);
//	}


	return dummyAr;

    }


    public static class Earnings_numDaysUntil extends Var {
	public Earnings_numDaysUntil() {
	    arrayDataType = Type.INTEGER;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class Earnings_numDaysSince extends Var {
	public Earnings_numDaysSince() {
	    arrayDataType = Type.INTEGER;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

}
