package objects.prices;
import categories.Seasons;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import main.DataManager;
import objects.Macro;
import objects.Macro_float;
import objects.Macro_int;
import objects.Stock;
import org.apache.commons.lang3.ArrayUtils;
import utilities.G;

public class Prices {

    /** AA, AAPL, and AAON all go back to at least 1991.   should be descending order */
    public static TreeSet<Integer> getValidDates(int minDate, String... tickers) throws G.No_DiskData_Exception {

	String ticker1 = tickers[0];
	HistoricalPrices hp1 = HistoricalPrices.getInstance(ticker1, minDate);

	TreeSet<Integer> validDates = new TreeSet(hp1.date_i__map.keySet());

	for (int i = 1; i < tickers.length; i++) {
	    String ticker = tickers[i];
	    HistoricalPrices hp = HistoricalPrices.getInstance(ticker, minDate);
	    Set<Integer> newDates = hp.date_i__map.keySet();
	    validDates.retainAll(newDates);
	}

	return (TreeSet<Integer>)validDates.descendingSet();
    }


    public static Macro getMacro(int minDate, DataManager data, String... tickers) throws ParseException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {

	int[] ssnDateInts = ArrayUtils.toPrimitive((new Seasons(minDate)).datesArray);

	Map<String, float[]> dataArrays = new LinkedHashMap();

	for (String ticker : tickers) {

	    String metricNamePrefix = "m_" + ticker;
	    float[] dataArray__priceChPct_day = G.new_null_float_ar(ssnDateInts.length);
	    float[] dataArray__priceChPct_week = G.new_null_float_ar(ssnDateInts.length);
	    float[] dataArray__ema3m5 = G.new_null_float_ar(ssnDateInts.length);

	    G.asdf(ticker);

	    objects.prices.X prices_x = new objects.prices.X(ticker);  //load pricesfrom raw
	    prices_x.calculate_data_origination(minDate);

	    float[] priceChPct_day = prices_x.vars.get(objects.prices.X.priceChPct_day.class).ar_float;
	    float[] priceChPct_week = prices_x.vars.get(objects.prices.X.priceChPct_week.class).ar_float;
	    float[] ema3m5 = prices_x.vars.get(objects.prices.X.ema3m5.class).ar_float;

	    for (int i = 0; i < ssnDateInts.length; i++) {
		int dateInt = ssnDateInts[i];
		Integer prices_x_dataArray_index = prices_x.date_i__map.get(dateInt);
		if (prices_x_dataArray_index != null && !G.isnull(prices_x_dataArray_index)) {
		    dataArray__priceChPct_day[i] = priceChPct_day[prices_x_dataArray_index];
		    dataArray__priceChPct_week[i] = priceChPct_week[prices_x_dataArray_index];
		    dataArray__ema3m5[i] = ema3m5[prices_x_dataArray_index];
		}
	    }
	    dataArrays.put(metricNamePrefix + "_priceChPct_day", dataArray__priceChPct_day);
	    dataArrays.put(metricNamePrefix + "_priceChPct_week", dataArray__priceChPct_week);
	    dataArrays.put(metricNamePrefix + "_ema3m5", dataArray__ema3m5);
	}


	Macro pricesMacro = new Macro_float(Macro.Names.tickerPrice, ssnDateInts, dataArrays);
	return pricesMacro;
    }
}
