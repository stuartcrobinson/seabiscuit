package objects.prices;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import objects.splitsAndDividends.Split;
import utilities.G;
import static utilities.G.new_filled_array;
import static utilities.G.new_null_float_ar;
import static utilities.G.null_int;

/**   dates must be decreasing.  set validDates to null to get all dates */
public class HistoricalPrices {

    public static HistoricalPrices getInstance(File file, int minDate) throws G.No_DiskData_Exception {
	return new HistoricalPrices(file, minDate);
    }

    public static HistoricalPrices getInstance(String ticker, int minDate) throws G.No_DiskData_Exception {
	return new HistoricalPrices(G.getPricesDailyFile(ticker), minDate);
    }

    public float[] open, high, low, close, volume;
    public int[] date;

    public String ticker;
    public Map<Integer, Integer> date_i__map;

    /** .  dates must be decreasing.  set minDate to 0 to get all dates (duh) */
    private HistoricalPrices(File file, int minDate) throws G.No_DiskData_Exception {
	List<YahooHistoricalPrices_Row> yhRows = YahooHistoricalPrices_Row.getValidRows(file, minDate);
	this.ticker = G.getTickFromFile(file);
	if (yhRows.isEmpty())
	    throw new G.No_DiskData_Exception();
	int datalength = yhRows.size();

	open = new_null_float_ar(datalength);
	high = new_null_float_ar(datalength);
	low = new_null_float_ar(datalength);
	close = new_null_float_ar(datalength);
	date = new_filled_array(null_int, datalength);
	volume = new_null_float_ar(datalength);

	for (int i = 0; i < datalength; i++) {
	    YahooHistoricalPrices_Row yhRow = yhRows.get(i);
	    open[i] = yhRow.openAdj;
	    high[i] = yhRow.highAdj;
	    low[i] = yhRow.lowAdj;
	    close[i] = yhRow.closeAdj;
	    date[i] = yhRow.date;
	    volume[i] = yhRow.volume;
	}
	date_i__map = G.get__date_i__map(date);
    }


    /** for testing only! */
    public HistoricalPrices() {
    }

    void incorporateCurrentPrices(CurrentPrices cp) {
	open = G.insertAtFront(open, cp.open);
	high = G.insertAtFront(high, cp.high);
	low = G.insertAtFront(low, cp.low);
	close = G.insertAtFront(close, cp.close);
	date = G.insertAtFront(date, cp.date);
	volume = G.insertAtFront(volume, cp.volume);

	date_i__map = G.get__date_i__map(date);
    }

    public static Map<String, HistoricalPrices> readAllFromDisk() throws G.No_DiskData_Exception {

	Map<String, HistoricalPrices> map = new LinkedHashMap();

	File dir = G.pricesDailyDir;
	for (File file : dir.listFiles()) {
	    HistoricalPrices hp = HistoricalPrices.getInstance(file, 0);
	    String ticker = G.getTickFromFile(file);
	    map.put(ticker, hp);
	}
	return map;
    }

    void nullifyPricesNearSplitDates(Map<Integer, Split> splitsMap) {

	for (Integer dateInt : splitsMap.keySet()) {
	    Integer i = date_i__map.get(dateInt);
	    if (i != null) {

		int x = 2;

		//nullify dates x days before and after
		for (int k = Math.min(i + x, open.length - 1); k >= i - x && k >= 0; k--) {
		    open[k] = G.null_float;
		    high[k] = G.null_float;
		    low[k] = G.null_float;
		    close[k] = G.null_float;
		    volume[k] = G.null_float;
		}
	    }
	}


    }

}
