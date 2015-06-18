package categories.category_earnings_response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import categories.Seasons;
import objects.Stock;
import objects.earnings.Earnings_EraDataRow;

/** this seems to be the object that, when created, calculates or reads all categories' earnings responses data (daily data i think).   HOW TO READ FROM FILE????????? */
public class CategoryEarningsResponses {
    Map<String, List<Earnings_EraDataRow>> ticker_earnings__map;
    Map<String, Set<Stock>> categoryFullName_stocks__map;

    Seasons seasons;

    /** Y holds the Var objects.  should be limited to restricted categories of current run */
    public Map<String, Y> categoryFullName_Y__map;

//    public CategoryEarningsResponses() throws ParseException {
//	this.seasons = new Seasons(null);
//    }

    public CategoryEarningsResponses(int minDate) throws ParseException {
	this.seasons = new Seasons(minDate);
    }

    public static CategoryEarningsResponses getInstance_origination(int minDate, Map<String, Set<Stock>> catFullName_stocks__map, Map<String, List<Earnings_EraDataRow>> ticker_earnings__map) throws ParseException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException {
	CategoryEarningsResponses cer = new CategoryEarningsResponses(minDate);
	cer.calculate_data(catFullName_stocks__map, ticker_earnings__map);
	return cer;
    }

    public static CategoryEarningsResponses getInstance_fromXfiles(int minDate, Map<String, Set<Stock>> categoryFullName_stocks__map) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IOException, FileNotFoundException, ClassNotFoundException, ParseException {
	CategoryEarningsResponses cer = new CategoryEarningsResponses(minDate);
	cer.readFromDisk_xFiles(categoryFullName_stocks__map);
	return cer;
    }

    private void calculate_data(Map<String, Set<Stock>> catFullName_stocks__map, Map<String, List<Earnings_EraDataRow>> ticker_earnings__map) throws ParseException, IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException {
	this.categoryFullName_stocks__map = catFullName_stocks__map;
	this.ticker_earnings__map = ticker_earnings__map;
	buildAllCategoryEarningsResponses();
    }

    private void readFromDisk_xFiles(Map<String, Set<Stock>> categoryFullName_stocks__map_) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IOException, FileNotFoundException, ClassNotFoundException {
	this.categoryFullName_stocks__map = categoryFullName_stocks__map_;

	Set<String> categoryFullNames = categoryFullName_stocks__map.keySet();
	categoryFullName_Y__map = new LinkedHashMap(categoryFullNames.size());

	System.out.println("loading " + categoryFullNames.size() + " Category Earnings Reponses datafiles...");

	int totalsize = categoryFullNames.size();
	int count = 1;
	for (String cfn : categoryFullNames) {
	    Y y = new Y(cfn);
	    y.calculate_data_from_xFiles();

////	    zxcv
//	    System.out.println("XXXXXXXXXXXfgr2gtrgre  : " + cfn);
//	    System.out.println("XXXcer_priceChPct_len  : " + y.vars.get(categories.category_earnings_response.Y.cer_priceChPct.class).ar_float.length);
//	    System.out.println("XXXXXXXXXXX_2date_len  : " + y.vars.get(categories.category_earnings_response.Y.cer_date.class).ar_int.length);

	    categoryFullName_Y__map.put(cfn, y);
	    System.out.println((totalsize - count++) + ". " + cfn);
	}
    }

    public void writeToDisk() throws IOException {
	for (Y y : categoryFullName_Y__map.values())
	    y.writeToDisk();
    }

    private void buildAllCategoryEarningsResponses() throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException {


	System.out.println("loading " + categoryFullName_stocks__map.size() + " Category Earnings Reponses datafiles from raw...");

	categoryFullName_Y__map = new LinkedHashMap();

	int totalsize = categoryFullName_stocks__map.size();
	int count = 1;
	for (Entry<String, Set<Stock>> entry : categoryFullName_stocks__map.entrySet()) {
	    String categoryFullName = entry.getKey();
	    Set<Stock> shrinkingStocksSet = entry.getValue();
	    Y y = new Y(categoryFullName);
	    y.calculate_data_origination(shrinkingStocksSet, seasons, ticker_earnings__map, false);
	    categoryFullName_Y__map.put(categoryFullName, y);
	    System.out.println((totalsize - count++) + ". " + categoryFullName);
	}
    }

    public void updateForCurrentPrices() throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, FileNotFoundException, ClassNotFoundException {

	for (Entry<String, Set<Stock>> entry : categoryFullName_stocks__map.entrySet()) {
	    String cfn = entry.getKey();
	    Set<Stock> shrinkingStocksSet = entry.getValue();

	    Y y = categoryFullName_Y__map.get(cfn);
	    y.calculate_data_origination(shrinkingStocksSet, seasons, ticker_earnings__map, true);
	    categoryFullName_Y__map.put(cfn, y);
	}
    }
}
