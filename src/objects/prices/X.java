package objects.prices;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import objects.Stock;
import objects.splitsAndDividends.Split;
import utilities.G;
import static utilities.G.get;
import static utilities.G.is_weird;
import static utilities.G.new_filled_array;
import static utilities.G.null_float;
import supers.SuperX;
import supers.Var;
import static utilities.G.is_null_or_outOfBounds;

/** <b>future change prices be careful not to use bogus today's data cell.</b>  filled with crap as placeholder until we update it with real current prices.  make sure we're not incorporating the null int value in to future price changes!  this is the only concern for having bogus values for index 0 data.  nothing else should be looking at future values. */
public final class X extends SuperX implements supers.SuperXInterface, supers.XInterface_uses_Vars {


    private HistoricalPrices hps;
    /** must replace ( -- no -- CREATE!) this after getting current prices! */
    public Map<Integer, Integer> date_i__map;
    public Map<Integer, Split> splitsMap;


    public X() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(null);
	fill_emptyVarsMap();
    }

    public X(String ticker) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(ticker, null);
	fill_emptyVarsMap();
    }

    public void calculate_data_origination(int minDate) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	setAttributes(minDate);
	fill_data__origination(this);
	date_i__map = G.get__date_i__map(date(this));
    }

    public void setAttributes(int minDate) throws IOException, G.No_DiskData_Exception {

	hps = HistoricalPrices.getInstance(G.getPricesDailyFile(ticker), minDate);

	try {
	    List<Split> splits = Split.readFromDisk(ticker);
	    splitsMap = new LinkedHashMap();
	    for (Split split : splits)
		splitsMap.put(split.date, split);

	    hps.nullifyPricesNearSplitDates(splitsMap);
	} catch (G.No_DiskData_Exception e) {
	}

    }

    @Override
    public void setXFiles() throws IOException {
	setXFile_vars();
    }

    @Override
    public void setXFile_vars() throws IOException {
	varsFile = G.newChildTickerFile(G.XPrices_vars, ticker);
    }

    @Override
    public void fill_data__origination(SuperX x) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	fill_vars(x, hps.date.length + 1);
    }

    @Override
    public void fill_varClasses() {
	varClasses.add(X.date.class);
//	varClasses.add(X.open.class);
//	varClasses.add(X.high.class);
//	varClasses.add(X.low.class);
	varClasses.add(X.close.class);
	varClasses.add(X.vol.class);
	varClasses.add(X.emaVol5.class);
	varClasses.add(X.volChPct.class);
	varClasses.add(X.AG_3.class);
	varClasses.add(X.ema3.class);
	varClasses.add(X.ema5.class);
	varClasses.add(X.ema9.class);
//	varClasses.add(X.ema20.class);
//	varClasses.add(X.ema50.class);
//	varClasses.add(X.ema200.class);
	varClasses.add(X.ema3m5.class);
	varClasses.add(X.ema3m9.class);
//	varClasses.add(X.ema3m20.class);
//	varClasses.add(X.ema3m200.class);
	varClasses.add(X.priceChPct_day.class);
	varClasses.add(X.priceChPct_week.class);
	varClasses.add(X.priceChPct_month.class);
	varClasses.add(X.priceChPct_2month.class);
//	varClasses.add(X.futChPct_intraDay.class);
//	varClasses.add(X.futChPct_day.class);
//	varClasses.add(X.futChPct_2day.class);
//	varClasses.add(X.futChPct_week.class);
//	varClasses.add(X.futChPct_8day.class);
//	varClasses.add(X.futChPct_2week.class);
//	varClasses.add(X.futChPct_3week.class);
//	varClasses.add(X.futChPct_month.class);
//	varClasses.add(X.futChPct_5week.class);
//	varClasses.add(X.futChPct_7week.class);

//	varClasses.add(X.futChPct_2month.class);
//	varClasses.add(X.futChPct_6month.class);
//	varClasses.add(X.futChPct_year.class);
    }

    /* things needed for calculating arrays */
    @Override @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public void calculate_vars_origination(SuperX x, boolean doUpdateForCurrentPrices) {	//actually i think we're better off calculating w/ Var methods here.  since not costly data searching.
	for (Var var : vars.values())
	    var.calculateData(x, doUpdateForCurrentPrices);

//	cleanUpData();

    }

    private static final int _day = 1;
    private static final int _2day = 2;
    private static final int _week = 5;
    private static final int _8day = 8;
    private static final int _2week = 10;
    private static final int _3week = 15;
    private static final int _month = 21;
    private static final int _5week = 25;
    private static final int _7week = 35;
    private static final int _2month = 42;
    private static final int _6month = 130;
    private static final int _year = 261;


    public void updatePrices(CurrentPrices cp) {
	date(this)[0] = cp.date;
//	open(this)[0] = cp.open;
//	high(this)[0] = cp.high;
//	low(this)[0] = cp.low;
	close(this)[0] = cp.close;
	vol(this)[0] = cp.volume;

	//
	//	int i = 0;
	//	int cpDate = date(this)[i];
	//	G.asdf("awefawefargrg " + date_i__map);
	//	G.asdf("awefawefargrg " + cp);
	//	G.asdf("awefawefargrg " + cpDate);
	//	G.asdf("awefawefargrg " + i);
	//	date_i__map.put(cpDate, i);

	date_i__map = G.get__date_i__map(date(this));
    }

    /** Gain is the price difference WHEN todays price is higher than yesterday's.  gain = today - yesterday <br><br> average gain is the sum of gains over the last N days, divided by N (so even if there was only a gain on one day, if N is 14, you still divide that 1 gain by 14)<br><br>First Average Gain = Sum of Gains over the past 14 periods / 14. <br><br>Average Gain = [(previous Average Gain) x 13 + current Gain] / 14. <br><br> http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:relative_strength_index_rsi */

    /*
    
    
     TODO -- start here!  the average gain calculation is wrong!!!!
    
     go to the website.  i think this calcualtaiong is just like, average change.  we want average GAIN!!!!  maybe we should look at average LOSS again and maybe even RSI
    
     since apparently my RSI calculation must have been wrong.  
    
    
     */
    private void build_ag(float[] ag_x, int duration, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;
	float[] close = close(this);
	float[] gain = new_filled_array(null_float, ag_x.length);

	int duration1 = 1;	//cos price_diff is comparing day w/ day prior
	for (int i = 0; i < Math.min(iterMax, ag_x.length - duration1); i++)
	    try {
		gain[i] = Math.max(0, today(close, i) - yesterday(close, i));
	    } catch (G.My_null_exception | ArrayIndexOutOfBoundsException e) {
	    }

	for (int i = 0; i < Math.min(iterMax, ag_x.length - duration); i++) {
	    try {
		if (is_null_or_outOfBounds(ag_x, yesterday(i))) {
		    float sum = 0;
		    for (int k = i; k < i + duration; k++)
			sum += get(gain, k);		    //throws here
		    ag_x[i] = sum / duration;
		} else {
		    ag_x[i] = (yesterday(ag_x, i) * (duration - 1) + get(gain, i)) / duration;
		}
	    } catch (G.My_null_exception e) {
	    }
	}
    }

    private void build_pctChange(float[] pctCh, float[] init, float[] fin, boolean doUpdateForCurrentPrices) {

	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

	for (int k = 0; k < Math.min(iterMax, pctCh.length); k++) {
	    try {
		float i = G.get(init, k);
		float f = G.get(fin, k);

		pctCh[k] = 100 * (f - i) / i;
	    } catch (G.My_null_exception e) {
	    }
	}
    }

    /** //adjust all these calculat0rs so they can input index number, i (for when updating data for current prices)
     *	//	     SMA: 10 period sum / 10 
     *	//	     Multiplier: (2 / (Time periods + 1) ) = (2 / (10 + 1) ) = 0.1818 (18.18%) ..... EMA_multiplier(duration)
     *	//	     EMA: {Close - EMA(previous day)} x multiplier + EMA(previous day). 
     *	*/
    private void build_ema(float[] emaX, float[] close, int timePeriod, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;
//	float[] close = close(this);

	for (int i = Math.min(iterMax, emaX.length - timePeriod); i >= 0; i--) {	//is it wasteful to repeat loop for each var?  i don't think so.  tiny time cost.  would be ugly to combine
	    try {
		float previousDayValue;
		if (is_null_or_outOfBounds(emaX, yesterday(i))) {
		    float sum = 0;
		    float count = 0;
		    for (int k = i; k < i + timePeriod; k++) {
			sum += get(close, k);
			count++;
		    }
		    previousDayValue = sum / count;
		} else {
		    previousDayValue = get(emaX, yesterday(i));
		}
		emaX[i] = (get(close, i) - previousDayValue) * EMA_multiplier(timePeriod) + previousDayValue;
		if (is_weird(get(emaX, i))) {
		    emaX[i] = null_float;
		}
//		System.out.println("34t34t23 ema" + timePeriod + ": " + emaX[i]);
	    } catch (G.My_null_exception e) {
	    }
	}
    }

    private static float EMA_multiplier(int timePeriod) {
	return 2f / (timePeriod + 1f);
    }

    private void build_ema3m(float[] ema3mX, int timePeriod, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;
	float[] ema3 = vars.get(ema3.class).ar_float;
	float[] emaX = get_emaX(timePeriod);
	for (int i = Math.min(iterMax, ema3mX.length - timePeriod); i >= 0; i--) {
	    try {
		ema3mX[i] = get(ema3, i) - get(emaX, i);
//		System.out.println("34t34t23 ema3m"+timePeriod + ": " + emaX[i]);
	    } catch (G.My_null_exception ex) {
	    }
	}
    }

    /** good for ema3, 5, 9, 20, 50, 200 */
    private float[] get_emaX(int X) {
	Var var = null;
	switch (X) {
	    case 3: var = vars.get(ema3.class);
		break;
	    case 5: var = vars.get(ema5.class);
		break;
	    case 9: var = vars.get(ema9.class);
		break;
	    case 20: var = vars.get(ema20.class);
		break;
	    case 50: var = vars.get(ema50.class);
		break;
	    case 200: var = vars.get(ema200.class);
		break;
	}
	if (var == null) {
	    System.out.print("wrong ema number.");
	    System.exit(0);
	}
	return var.ar_float;
    }

    /** calculate array of changes (pct) in price over given timePeriod (days).  <br><br><b>todo:  </b>rename this to: ...pastPriceChPct (to distinguish from future change) */
    private void build_priceChPct(float[] ch_x, int nDays, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;
	float[] close = close(this);

	for (int i = 0; i < Math.min(iterMax, ch_x.length - nDays); i++) {
	    try {
		float current_close = get(close, i);
		float past_close = daysAgo(close, i, nDays);
		ch_x[i] = (current_close - past_close) / past_close;
		ch_x[i] = get(ch_x, i) * 100;
	    } catch (G.My_null_exception e) {
	    }
	}
    }

//    private void build_futChPct_intraDay(float[] fch_intr, boolean doUpdateForCurrentPrices) {
//	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;
//	float[] close = close(this);
//	float[] open = open(this);
//
//	//TODO make it iterMax-1 for i-- loops!  (not critical but would save an infinitessimal amount of time)
//
//	for (int i = Math.min(iterMax, fch_intr.length - 1); i >= 0; i--) {
//	    try {
//		float openi = get(open, i);
//		float closei = get(close, i);
//		fch_intr[i] = (closei - openi) / openi;
//		fch_intr[i] = get(fch_intr, i) * 100;
//	    } catch (G.My_null_exception e) {
//	    }
//	}
//    }

    /** new and untested */
    private void build_futChPct(float[] fch_x, int nDays, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;
	float[] close = close(this);

	for (int i = Math.min(iterMax, fch_x.length - 1); i >= nDays; i--) {
	    try {
		float current_close = get(close, i);
		float fut_close = daysFromNow(close, i, nDays);
//		if (G.isnull(fut_close))
//		    fut_close = daysFromNow(i, close, nDays - 1);   //unnecessary since daysFromNow(...) throws null exception if value is my_null  ...	    //this is incase it's trying to compare a day with the value at index=0, which is null placeholder to put in current prices data, later
		fch_x[i] = (fut_close - current_close) / current_close;
		fch_x[i] = get(fch_x, i) * 100;
	    } catch (G.My_null_exception e) {
	    }
	}
    }

    private int yesterday(int i) {
	return daysAgo(i, 1);
    }

    private int tomorrow(int i) {
	return daysFromNow(i, 1);
    }

    private int daysAgo(int i, int numDaysAgo) {
	return i + numDaysAgo;
    }

    private int daysFromNow(int i, int numDaysFromNow) {
	return i - numDaysFromNow;
    }

    private float today(float[] dataValuesWithDecreasingDates, int i) throws G.My_null_exception {
	return get(dataValuesWithDecreasingDates, i);
    }

    private float yesterday(float[] dataValuesWithDecreasingDates, int i) throws G.My_null_exception {
	return get(dataValuesWithDecreasingDates, yesterday(i));
    }

    private float daysAgo(float[] dataValuesWithDecreasingDates, int i, int numDaysAgo) throws G.My_null_exception {
	return get(dataValuesWithDecreasingDates, daysAgo(i, numDaysAgo));
    }

    private float tomorrow(float[] dataValuesWithDecreasingDates, int i) throws G.My_null_exception {
	return get(dataValuesWithDecreasingDates, tomorrow(i));
    }

    private float daysFromNow(float[] dataValuesWithDecreasingDates, int i, int numDaysFromNow) throws G.My_null_exception {
	return get(dataValuesWithDecreasingDates, daysFromNow(i, numDaysFromNow));
    }

    public void deleteHPS() {
	hps = null;
    }

    private void cleanUpData() {
	float[] close = close(this);
	float[] pcChDayPct = vars.get(objects.prices.X.priceChPct_day.class).ar_float;

	for (int i = 0; i < pcChDayPct.length; i++) {
	    if (pcChDayPct[i] > 500 || pcChDayPct[i] < -500) {
		close[i] = G.null_float;
		pcChDayPct[i] = G.null_float;
	    }
	}
    }


    public static class date extends Var {
	public date() {
	    arrayDataType = Var.Type.INTEGER;
//	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    if (!doUpdateForCurrentPrices) {
		G.insert_2nd_array_starting_at_index_1(ar_int, ((X)x).hps.date);
		ar_int[0] = G.currentDate;
	    }
	}
    }

    public static class open extends Var {
	public open() {
	    arrayDataType = Var.Type.FLOAT;
	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    if (!doUpdateForCurrentPrices)
		G.insert_2nd_array_starting_at_index_1(ar_float, ((X)x).hps.open);
	}
    }


    public static class high extends Var {
	public high() {
	    arrayDataType = Var.Type.FLOAT;
	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    if (!doUpdateForCurrentPrices)
		G.insert_2nd_array_starting_at_index_1(ar_float, ((X)x).hps.high);
	}
    }


//    public static class low extends Var {
//	public low() {
//	    arrayDataType = Var.Type.FLOAT;
//	    super.isTemp = true;
//	}
//
//	@Override
//	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
//	    if (!doUpdateForCurrentPrices)
//		G.insert_2nd_array_starting_at_index_1(ar_float, ((X)x).hps.low);
//	}
//    }
    public static class close extends Var {
	public close() {
	    arrayDataType = Var.Type.FLOAT;
//	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    if (!doUpdateForCurrentPrices)
		G.insert_2nd_array_starting_at_index_1(ar_float, ((X)x).hps.close);
	}
    }

    public static class vol extends Var {
	public vol() {
	    arrayDataType = Var.Type.FLOAT;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    if (!doUpdateForCurrentPrices)
		G.insert_2nd_array_starting_at_index_1(ar_float, ((X)x).hps.volume);
	}
    }

    public static class emaVol5 extends Var {
	public emaVol5() {
	    arrayDataType = Var.Type.FLOAT;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema(ar_float, ((X)x).vol((X)x), 5, doUpdateForCurrentPrices);
	}
    }

    public static class volChPct extends Var {
	public volChPct() {
	    arrayDataType = Var.Type.FLOAT;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_pctChange(ar_float, ((X)x).vars.get(objects.prices.X.emaVol5.class).ar_float, ((X)x).vol((X)x), doUpdateForCurrentPrices);
	}
    }

    /*
     (done) go through rest of data and:
    
     1.  insert data starting at index 1! not 0 (save room for current prices later)
     2.  figure out how to go back later and calculate data for just 1 index!?
    
     ALSO -- FUTURE CALCULATIONS:  NOTHING SHOULD USE HPS!!!!!!!!!!!!!!!!!!!!!!!!!!!!  this.hps should be private actually!  other things should access this.<data arrays>
     */
    public static class AG_3 extends Var {
	public AG_3() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ag(ar_float, 3, doUpdateForCurrentPrices);
	}
    }

    public static class ema3 extends Var {
	public ema3() {
	    arrayDataType = Var.Type.FLOAT;
	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema(ar_float, ((X)x).close((X)x), 3, doUpdateForCurrentPrices);
	}
    }

    public static class ema5 extends Var {
	public ema5() {
	    arrayDataType = Var.Type.FLOAT;
	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema(ar_float, ((X)x).close((X)x), 5, doUpdateForCurrentPrices);
	}
    }

    public static class ema9 extends Var {
	public ema9() {
	    arrayDataType = Var.Type.FLOAT;
	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema(ar_float, ((X)x).close((X)x), 9, doUpdateForCurrentPrices);
	}
    }

    public static class ema20 extends Var {
	public ema20() {
	    arrayDataType = Var.Type.FLOAT;
	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema(ar_float, ((X)x).close((X)x), 20, doUpdateForCurrentPrices);
	}
    }

    public static class ema50 extends Var {
	public ema50() {
	    arrayDataType = Var.Type.FLOAT;
	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema(ar_float, ((X)x).close((X)x), 50, doUpdateForCurrentPrices);
	}
    }

    public static class ema200 extends Var {
	public ema200() {
	    arrayDataType = Var.Type.FLOAT;
	    super.isTemp = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema(ar_float, ((X)x).close((X)x), 200, doUpdateForCurrentPrices);
	}
    }

    public static class ema3m5 extends Var {
	public ema3m5() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema3m(ar_float, 5, doUpdateForCurrentPrices);
	}
    }

    public static class ema3m9 extends Var {
	public ema3m9() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema3m(ar_float, 9, doUpdateForCurrentPrices);
	}
    }

    public static class ema3m20 extends Var {
	public ema3m20() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema3m(ar_float, 20, doUpdateForCurrentPrices);
	}
    }

    public static class ema3m200 extends Var {
	public ema3m200() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_ema3m(ar_float, 200, doUpdateForCurrentPrices);
	}
    }

    public static class priceChPct_day extends Var {
	public priceChPct_day() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_priceChPct(ar_float, _day, doUpdateForCurrentPrices);

	    ((X)x).cleanUpData();
	}
    }

    public static class priceChPct_week extends Var {
	public priceChPct_week() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_priceChPct(ar_float, _week, doUpdateForCurrentPrices);
	}
    }

    public static class priceChPct_month extends Var {
	public priceChPct_month() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_priceChPct(ar_float, _month, doUpdateForCurrentPrices);
	}
    }

    public static class priceChPct_2month extends Var {
	public priceChPct_2month() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
//	    System.out.println("here");
	    ((X)x).build_priceChPct(ar_float, _2month, doUpdateForCurrentPrices);
	}
    }

//    public static class futChPct_intraDay extends Var {
//	public futChPct_intraDay() {
//	    arrayDataType = Var.Type.FLOAT;
//	    isDependentVariable = true;
//	}
//
//	@Override
//	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
//	    ((X)x).build_futChPct_intraDay(ar_float, doUpdateForCurrentPrices);
//	}
//    }

    public static class futChPct_day extends Var {
	public futChPct_day() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _day, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_2day extends Var {
	public futChPct_2day() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _2day, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_week extends Var {
	public futChPct_week() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _week, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_8day extends Var {
	public futChPct_8day() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _8day, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_2week extends Var {
	public futChPct_2week() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _2week, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_3week extends Var {
	public futChPct_3week() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _3week, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_month extends Var {
	public futChPct_month() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _month, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_5week extends Var {
	public futChPct_5week() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _5week, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_7week extends Var {
	public futChPct_7week() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _7week, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_2month extends Var {
	public futChPct_2month() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _2month, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_6month extends Var {
	public futChPct_6month() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _6month, doUpdateForCurrentPrices);
	}
    }

    public static class futChPct_year extends Var {
	public futChPct_year() {
	    arrayDataType = Var.Type.FLOAT;
	    isDependentVariable = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    ((X)x).build_futChPct(ar_float, _year, doUpdateForCurrentPrices);
	}
    }

    @Override //stays empty
    public void setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(Stock stock) {
    }

    /** don't use this.  prices are unreliable around splits.  just nullify them */
    @Deprecated
    public void adjustForSplits(objects.splitsAndDividends.X sx) {


	//this only works if splits are reported before dayS END!!!
	//if there is a split TODAY, then adjust all "close" values accordingly.  so these close prices match financial data and current prices
	Split s = sx.splitsMap.get(G.currentDate);
	if (s != null) {
	    float n1 = s.n1;
	    float n2 = s.n2;

	    float factor = n2 / n1;	    //doublecheck thiS!!!???

	    float[] close = close(this);

	    for (int i = 1; i < close.length; i++) {
		try {
		    float value = G.get(close, i);
		    close[i] = value * factor;
		} catch (G.My_null_exception ex) {
		}
	    }
	}
    }
}
