package objects.prices;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import static utilities.G.null_float;
import static utilities.G.null_int;
import static utilities.G.parse_float;
import static utilities.G.parse_int;

public class YahooHistoricalPrices_Row implements Comparable {


    public int date = null_int;
    public float open_unadjusted = null_float;
    public float high_unadjusted = null_float;
    public float low_unadjusted = null_float;
    public float close_unadjusted = null_float;
    public float volume = null_float;
    public float closeAdj = null_float;
    public float openAdj = null_float;
    public float highAdj = null_float;
    public float lowAdj = null_float;

    /* nullifies data where volume !> 0 or close !> 0*/
    public YahooHistoricalPrices_Row(String line) throws ParseException, BadYahooRowException {
	String[] lineAr = line.split(",");
	try {
	    String dateSt = lineAr[0].replace("-", "");
	    date = parse_int(dateSt);

	    open_unadjusted = parse_float(lineAr[1]);	//	 Date,Open,High,Low,Close,Volume,Adj Close
	    high_unadjusted = parse_float(lineAr[2]);	//	 2013-11-08,9.03,9.08,8.78,9.06,23678200,9.06
	    low_unadjusted = parse_float(lineAr[3]);
	    close_unadjusted = parse_float(lineAr[4]);
	    volume = parse_float(lineAr[5]);
	    closeAdj = parse_float(lineAr[6]);

//	    if (volume > 1000 && close_unadjusted > 1 && closeAdj > 1 && open_unadjusted > 1 && high_unadjusted > 1 && low_unadjusted > 1) {	//too dangerous to restrict over 1.  in case price falls that low.  after buying above 1. don't want that purchase to just disappear.//or, at vol: 100, and others 0

	    if (volume > 100 && close_unadjusted > 0 && closeAdj > 0 && open_unadjusted > 0 && high_unadjusted > 0 && low_unadjusted > 0) {	//or, at vol: 100, and others 0
		float adjusted_over_unadjusted = closeAdj / close_unadjusted;
		openAdj = open_unadjusted * adjusted_over_unadjusted;
		highAdj = high_unadjusted * adjusted_over_unadjusted;
		lowAdj = low_unadjusted * adjusted_over_unadjusted;
	    } else {
		volume = null_float;
		close_unadjusted = null_float;
		closeAdj = null_float;
	    }
	} catch (Exception e) {
	}
    }

    private YahooHistoricalPrices_Row(int validDate) {
	this.date = validDate;
    }

    public int getYear() {
	int year = date / 10000;
	return year;
    }

    /** prices file dates must be decreasing.  only adds dates greater than minDate */
    static List<YahooHistoricalPrices_Row> getValidRows(File file, int minDate) {
	List<YahooHistoricalPrices_Row> yhRows = new ArrayList<>();

	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	    String line, trash = br.readLine();
	    while ((line = br.readLine()) != null) {
		YahooHistoricalPrices_Row hp_row = new YahooHistoricalPrices_Row(line);
//		if (validDates == null || validDates.contains(hp_row.date)) //only add the date if it exists in validDates.  removed:  "!global_use_validDate_i_map || "  //fancymap //to get prices before validDates is created.  for validDates creation.
//		    yhRows.add(hp_row);
		if (hp_row.date >= minDate)// && hp_row.closeAdj > 1 && hp_row.close_unadjusted > 1 && hp_row.open_unadjusted > 1 && hp_row.high_unadjusted > 1 && hp_row.low_unadjusted > 1 && hp_row.volume > 1000)
		    yhRows.add(hp_row);
		else
		    break;
	    }
	} catch (Exception e) {//BadYahooRowException
	}
	return yhRows;
    }

    @Override
    public int compareTo(Object o) {
	YahooHistoricalPrices_Row row2 = (YahooHistoricalPrices_Row)o;
	return (new Integer(row2.date)).compareTo(this.date);		    //should sort backwards
    }

    public static class BadYahooRowException extends Exception {
	public BadYahooRowException() {
	}
    }
}
