package objects.earnings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import objects.Stock;
import supers.Era;
import utilities.G;


public class Earnings_EraDataRow extends Era.EraDataRow {


    private Earnings e;
    public int e_cookedDate;
    public float e_pricePctChange;
    public Integer e_numSecReportsinPrior_2weeks;
    public Integer e_numPressReleasesinPrior_2weeks;


    //expand to get these from Earnings strings
    /** Sunday is 0 */
    public byte e_cookedDateWeekDay;
    public float e_zacks_estimate;
    public float e_zacks_reported;
    public float e_estimize_popularity;
    public float e_estimize_eps_wallst;
    public float e_estimize_eps_estimize;
    public float e_estimize_eps_actual;
    public float e_estimize_rev_wallst;
    public float e_estimize_rev_estimize;
    public float e_estimize_rev_actual;

    /** where is this called?  !?!?!?!?!?  in fill_eras() ??*/
    public Earnings_EraDataRow(Earnings e, int cookedDate, float pricePctChange, Integer nSecPast2Weeks, Integer nPRPast2Weeks) throws ParseException, IllegalArgumentException, IllegalAccessException {

	this.e = e;
	this.e_cookedDate = cookedDate;
	this.e_pricePctChange = pricePctChange;
	this.e_numSecReportsinPrior_2weeks = nSecPast2Weeks;
	this.e_numPressReleasesinPrior_2weeks = nPRPast2Weeks;

	expandData();

	valuesMap = Era.EraDataRow.make_valuesMap(this);
    }

    @Override
    public String toOutputStringEDR() {
	return ""
		+ e.outputLine() + G.edrDelim
		+ G.parse_Str(e_cookedDate) + G.edrDelim
		+ G.parse_Str(e_pricePctChange) + G.edrDelim
		+ G.parse_Str(e_numSecReportsinPrior_2weeks == null ? G.null_int : e_numSecReportsinPrior_2weeks) + G.edrDelim
		+ G.parse_Str(e_numPressReleasesinPrior_2weeks == null ? G.null_int : e_numPressReleasesinPrior_2weeks);
    }

    /** for assimilateValues.  and testing.  why doesn't the constructor accept the String[] inputStrs and do the work there?  cos putting assimilateValues in the interface adds clarity and consistency */
    public Earnings_EraDataRow() {
    }

    @Override
    public void assimilateValues(String[] ar) throws ParseException, IllegalArgumentException, IllegalAccessException {
	e = new Earnings(ar[0]);
	e_cookedDate = G.parse_int(ar[1]);
	e_pricePctChange = G.parse_float(ar[2]);
	e_numSecReportsinPrior_2weeks = G.parse_int(ar[3]);
	e_numPressReleasesinPrior_2weeks = G.parse_int(ar[4]);

	expandData();
	valuesMap = Era.EraDataRow.make_valuesMap(this);
    }

    private void expandData() throws ParseException {

	this.e_cookedDateWeekDay = G.getWeekDay(e_cookedDate);

	this.e_zacks_estimate = G.parse_float(e.zacks_estimate);
	this.e_zacks_reported = G.parse_float(e.zacks_reported);
	this.e_estimize_popularity = G.parse_float(e.estimize_popularity);
	this.e_estimize_eps_wallst = G.parse_float(e.estimize_eps_wallst);
	this.e_estimize_eps_estimize = G.parse_float(e.estimize_eps_estimize);
	this.e_estimize_eps_actual = G.parse_float(e.estimize_eps_actual);
	this.e_estimize_rev_wallst = G.parse_float(e.estimize_rev_wallst);
	this.e_estimize_rev_estimize = G.parse_float(e.estimize_rev_estimize);
	this.e_estimize_rev_actual = G.parse_float(e.estimize_rev_actual);
    }

    public static Map<String, List<Earnings_EraDataRow>> readAllFromDisk() throws IOException, FileNotFoundException, InstantiationException, IllegalAccessException, ParseException {
	List<Earnings_EraDataRow> earnings_eraDataRows = new ArrayList();

	Map<String, List<Earnings_EraDataRow>> map = new LinkedHashMap();

	File dir = G.XEarnings_eras;

	for (File file : dir.listFiles()) {
	    List<Era> eras = Era.readFromDisk(file, Earnings_EraDataRow.class);
	    for (Era era : eras) {
		earnings_eraDataRows.add((Earnings_EraDataRow)era.eraDataRow);
	    }
	    String ticker = G.getTickFromFile(file);
	    map.put(ticker, earnings_eraDataRows);

	}


//	for (Map.Entry<String, List<Earnings_EraDataRow>> entry : map.entrySet()) {
//	    System.out.println("45t49t58u9e8tuuuuuuuuuuuuuu:   " + entry.getKey() + ", size: " + entry.getValue().size());
//	}
	return map;
    }

    public static Map<String, List<Earnings_EraDataRow>> getAllFromStocks(List<Stock> stocks) {

	Map<String, List<Earnings_EraDataRow>> map = new LinkedHashMap();

	for (Stock stock : stocks) {
	    List<Earnings_EraDataRow> earnings_eraDataRows = new ArrayList();
	    List<Era> eras = stock.earnings_X.eras;
	    for (Era era : eras) {
		earnings_eraDataRows.add((Earnings_EraDataRow)era.eraDataRow);
	    }
	    String ticker = stock.name;
	    map.put(ticker, earnings_eraDataRows);
	}

//
//	System.out.println("a34tq34tq34t");
//	for (Map.Entry<String, List<Earnings_EraDataRow>> entry : map.entrySet()) {
//	    System.out.println(entry.getKey());
//
//	    List<Earnings_EraDataRow> list = entry.getValue();
//	    for (Earnings_EraDataRow edr : list) {
//		System.out.println(edr.toOutputStringEDR());
//	    }
//	}
//	System.exit(0);
	return map;
    }

}
