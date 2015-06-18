package objects.finance;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import utilities.G;
import supers.Era;

/** ignore TTM values!!! not sure exactly what those are / how calculated / normalized? trash them immediately!*/
public class MsFinancialDownload {


    /*	    dont delete this!  descriptions of variables
     from KEY RATIOS
     revenue, net income, shares,										bps, 								fcf, working capital.  				REDUNDANT: eps
     from FINANCIALS
     revenue, net income, diluted weighted average shares outstanding,	total stockholders' equity / dwaso,	fcf, ???							REDUNDANT: diluted earnings per share
     */
    public String[] yearmo;
    public float[] profitMargin;	//  = profit/revenue = earnings/revenue.   net income = earnings
    public float[] eps;
    public float[] bps;
    public float[] fcfps;

    private float[] revenue;
    private float[] income;	    //net income
    private float[] book;	    //total stockholders equity
    private float[] shares;	    //diluted weighted average shares outstanding
    private float[] fcf;

    /** this only works with the abridged downloads from Replace_Financials */
    public MsFinancialDownload(File file) throws FileNotFoundException, IOException {

	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	    String line;
	    while ((line = br.readLine()) != null)
		assignProperArray(line);
	}
	//NOW build stragglers
	build_income();
	build_profitMargin();
	build_bps();
//	build_fcfps();	    //getting this straight from file now.  confused, why not like that originally?
    }

    private void build_income() {
	try {
	    income = new float[yearmo.length];
	    for (int i = 0; i < income.length; i++)
		income[i] = eps[i] * shares[i];
	} catch (Exception e) {
	}
    }

    private void build_profitMargin() {
	try {
	    profitMargin = new float[yearmo.length];
	    for (int i = 0; i < profitMargin.length; i++)
		profitMargin[i] = income[i] / revenue[i];
	} catch (Exception e) {
	}
    }

    private void build_bps() {
	try {
	    if (bps != null) return;
	    bps = new float[yearmo.length];
	    for (int i = 0; i < bps.length; i++)
		bps[i] = book[i] / shares[i];
	} catch (Exception e) {
	}
    }

    private void build_fcfps() {
	try {
	    fcfps = new float[yearmo.length];
	    for (int i = 0; i < profitMargin.length; i++)
		fcfps[i] = fcf[i] / shares[i];
	} catch (Exception e) {
	}
    }

    /** this method trashes TTM values! attribute arrays only have values per official earnings release */
    private void assignProperArray(String line) {
	// quarterly
	// name,201312,201403,201406,201409,201412,TTM
	// Revenue,57594000,45646000,37432000,42123000,74599000,199800000
	// Diluted EPS,2.07,1.66,1.28,1.42,3.06,7.39
	// Diluted Shares,6310164,6156696,6051711,5972081,5881803,6015574
	// Total stockholders' equity,129684000,120179000,120940000,111547000,123328000
	// Free cash flow,20626000,12052000,7824000,9398000,30457000,59731000
	// FCFPS,3.27,1.96,1.29,1.57,5.18,9.93
	// BPS,20.55,19.52,19.98,18.68,20.97,
	//
	// annual
	// 	
	// name,200509,200609,200709,200809,200909,201009,201109,201209,201309,201409,TTM
	// Revenue USD Mil,13931,19315,24006,32479,42905,65225,108249,156508,170910,182795,199800
	// Earnings Per Share USD,0.22,0.32,0.56,0.77,1.30,2.16,3.95,6.31,5.68,6.45,7.39
	// Shares Mil,5997,6143,6225,6315,6349,6473,6557,6617,6522,6123,6016
	// Book Value Per Share USD,1.28,1.68,2.39,3.60,5.02,7.45,11.78,17.98,19.63,19.02,21.17
	// Free Cash Flow USD Mil,2275,1563,4484,8397,8946,16474,30077,41454,44590,49900,59731
	// FCFPS,0.38,0.25,0.72,1.33,1.41,2.55,4.59,6.26,6.84,8.15,9.93


	String[] ar = line.split(",");	    //not -1, cos different lines are diff lengths (not all have  TTM data)
	String name = ar[0];
	int numValues = ar.length - 1;  //minus 1 for the name cell.  KEEPING TTMs!!!   (and minus 1 for the TTM value) // TODO does this work????????  keeping TTM's, that is.

	//dates
	if (name.contains("name")) {
	    yearmo = new String[numValues];
	    for (int i = 1; i < ar.length; i++) {
		yearmo[i - 1] = (ar[i].equals("TTM") ? (String.valueOf(G.current_year + 1) + "01") : ar[i]);
//		System.out.println("s5rts8erut9s8erut " + yearmo[i - 1]);
	    }
	}
	//revenue
	if (line.contains("Revenue")) {
	    revenue = new float[numValues];
	    for (int i = 1; i < ar.length; i++)
		if (i < ar.length)
		    revenue[i - 1] = G.parse_float(ar[i]);
	}
	//eps
	if (line.toLowerCase().contains("earnings per share") || name.contains("Diluted EPS")) {
	    eps = new float[numValues];
	    for (int i = 1; i < ar.length; i++)
		if (i < ar.length)
		    eps[i - 1] = G.parse_float(ar[i]);
	}
	//shares
	if (line.toLowerCase().contains("shares")) {
	    shares = new float[numValues];
	    for (int i = 1; i < ar.length; i++)
		if (i < ar.length)
		    shares[i - 1] = G.parse_float(ar[i]);
	}
	//book
	if (line.contains("Total stockholders' equity")) {
	    book = new float[numValues];
	    for (int i = 1; i < ar.length; i++)
		if (i < ar.length)
		    book[i - 1] = G.parse_float(ar[i]);
	}
//	//fcf
//	if (line.contains("Free Cash Flow ") || line.contains("Free cash flow")) {
//	    fcf = new float[numValues];
//	    for (int i = 1; i < ar.length; i++)
//		if (i < ar.length)
//		    fcf[i - 1] = G.parse_float(ar[i]);
//	}
	//bps
	if (line.startsWith("Book Value Per Share ")) {
	    bps = new float[numValues];
	    for (int i = 1; i < ar.length; i++)
		if (i < ar.length)
		    bps[i - 1] = G.parse_float(ar[i]);
	}
	//fcfps
	if (line.contains("FCFPS")) {
	    fcfps = new float[numValues];
	    for (int i = 1; i < ar.length; i++)
		if (i < ar.length)
		    fcfps[i - 1] = G.parse_float(ar[i]);
	}
    }

    List<Era> getErasForX() throws ParseException, IllegalArgumentException, IllegalAccessException, G.No_DiskData_Exception {
	/*
	 // name,201310,201401,201404,201407,201410,TTM
	 // Diluted EPS,0.61,0.88,0.60,0.56,0.90,2.94
	 // Diluted Shares,59416,59713,59892,59784,59756,59756
	    
	 era: mindate, maxdate, datarow
	    
	 go through arrays.... for each yearmo in []yearmo, calculate minDate and maxDate
	    
	 for each element --
	    
	 minDate is:
		
	 prevDate, colDate, nextDate
	    
	 minDate is the last day in the month of colDate
	    
	 maxDate is the last day in the month of nextDate
	    
	 WHAT IF nextDate DOESNT EXIST?
	 - determine the number of days between prevDate and colDate, N.
	 - increment colDate by N to get nextDate.  then calculate maxDate using this synthetic nextDate
	 */

	List<Era> eras = new ArrayList();

	for (int i = 0; i < yearmo.length; i++) {
	    int minDate = getMinDate(yearmo, i);
	    int maxDate = getMaxDate(yearmo, i);

	    try {
		X.Financial_EraDataRow eraDataRow = new X.Financial_EraDataRow( //NULLPEXCEPTION HERE!
			profitMargin[i],
			eps[i],
			bps[i],
			fcfps[i],
			shares[i]
		);
		Era era = new Era(yearmo[i], minDate, maxDate, eraDataRow);
		eras.add(era);
	    } catch (ArrayIndexOutOfBoundsException e) {
	    } catch (NullPointerException e) {
		throw new G.No_DiskData_Exception();
	    }
	}
	return eras;
    }

    /** minDate is the first day of the first month after the date of colDate */
    private int getMinDate(String[] yearmo, int i) throws ParseException {
	String colDate = yearmo[i] + "01";
	Date d = G.sdf_date.parse(colDate);
	Calendar cal = Calendar.getInstance();
	cal.setTime(d);
	cal.add(Calendar.MONTH, 1);
	cal.set(Calendar.DAY_OF_MONTH, 1);

	return Integer.parseInt(G.sdf_date.format(cal.getTime()));
    }

    /** maxDate is the last day in the month of nextDate */
    private int getMaxDate(String[] yearmo, int i) throws ParseException {

	///wait ... what about TTM???????????????????????????????????????????
	String nextDate_ym;
	//if nextDate exists:
	if (i + 1 < yearmo.length) {
//	    System.out.println("q243rt3r here1");
	    //then nextDate exists
	    nextDate_ym = yearmo[i + 1];
	} else {
//	    System.out.println("q243rt3r here2");
	    //nextDate doesn't exist, we need to synthesize one using the time span between colDate and prevDate
	    nextDate_ym = synthesizeNextDateUsingPastDate(yearmo, i);
	}

	return Integer.parseInt(G.sdf_date.format(lastDayOfThisMonth(nextDate_ym + "01")));

    }

    /**this is stupid (below).  just increment it by a quarter (91 days) <br><br> WHAT IF nextDate DOESNT EXIST?
     - determine the number of days between prevDate and firstDayOfNextMonth(colDate), N.
     - increment colDate by N to get nextDate.  then calculate maxDate using this synthetic nextDate <br><br> returns in format yearmo: like 201603*/
    private String synthesizeNextDateUsingPastDate(String[] yearmo, int i) throws ParseException {

//	    String prevDate = yearmo[i - 1] + "01";
	String colDate = yearmo[i] + "01";

	Date d = lastDayOfThisMonth(colDate);
	Calendar cal = Calendar.getInstance();
	cal.setTime(d);
	cal.add(Calendar.DAY_OF_YEAR, 93);
	return G.sdf_date.format(cal.getTime()).substring(0, 6);

//	    int numDaysBetweenColDateAndPrevDate = G.daysBetween(prevDate, colDate);
//
//	    Date colDateDate = G.sdf_date.parse(colDate);
//	    Calendar cal = Calendar.getInstance();
//	    cal.setTime(colDateDate);
//	    cal.add(Calendar.DAY_OF_YEAR, numDaysBetweenColDateAndPrevDate);
//	    return G.sdf_date.format(cal.getTime()).substring(0, 6);
    }

    public static Date lastDayOfThisMonth(String ymd) throws ParseException {

	Date d = G.sdf_date.parse(ymd);
	//add a month to the date, then get first day of the month of that date, then decrement 1 day.
	Calendar cal = Calendar.getInstance();
	cal.setTime(d);
	cal.add(Calendar.MONTH, 1);
	cal.set(Calendar.DAY_OF_MONTH, 1);
	cal.add(Calendar.DAY_OF_YEAR, -1);
	return cal.getTime();
    }

}
