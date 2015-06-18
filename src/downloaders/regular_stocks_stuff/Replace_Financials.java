package downloaders.regular_stocks_stuff;

import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import utilities.G;
import utilities.HttpDownloadUtility;

/**failure note:  if ticker webpage DNE, no file gets downloaded and no exception is thrown.  the webpage returned is blank.  if an exception is thrown, failure is recorded in text file*/
public class Replace_Financials {

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
//	go_annual(args);
//	go_quarterly(args);
	
	
//	go_quarterly(new String[]{"0"});
//	go_quarterly(new String[]{"1"});
//	go_quarterly(new String[]{"2"});
	go_quarterly(new String[]{"3"});
    }

    /** for testing */
    public static void download(String ticker) throws IOException, InterruptedException, ParseException {
	G.initialize("download ms ", null, G.msAnnualDir, G.msAnnualCompletedDummyDir, G.msAnnualFailedLinksDir, G.msQuarterlyDir, G.msQuarterlyCompletedDummyDir, G.msQuarterlyFailedLinksDir);

	if (downloadAnnualKeyRatios(ticker))
	    G.notateCompletion(G.getMsAnnualUpdatedDummyFile(ticker));
	if (downloadQuarterlyFinancials(ticker))
	    G.notateCompletion(G.getMsQuarterlyUpdatedDummyFile(ticker));

    }

    /* input int 0, 1, 2, or 3 for subdivision of tickers list */
    public static void go_annual(String[] args) throws IOException, InterruptedException, ParseException {
	Integer input = G.initialize("download ms annual", args, G.msAnnualDir, G.msAnnualCompletedDummyDir, G.msAnnualFailedLinksDir);

	for (String ticker : G.getIncompleteTickersSubset(args, G.msAnnualCompletedDummyDir)) {

	    System.out.print("d_msA " + input + ": " + ticker + "  --  ");

	    if (downloadAnnualKeyRatios(ticker)) {
		G.notateCompletion(G.getMsAnnualUpdatedDummyFile(ticker));
		System.out.print("success\n");
	    } else System.out.println();
	}
    }

    /* input int 0, 1, 2, or 3 for subdivision of tickers list */
    public static void go_quarterly(String[] args) throws IOException, InterruptedException, ParseException {
	Integer input = G.initialize("download ms quarterly", args, G.msQuarterlyDir, G.msQuarterlyCompletedDummyDir, G.msQuarterlyFailedLinksDir);

	for (String ticker : G.getIncompleteTickersSubset(args, G.msQuarterlyCompletedDummyDir)) {

	    System.out.println("d_msA " + input + ": " + ticker + "  --  ");

	    if (downloadQuarterlyFinancials(ticker)) {
		G.notateCompletion(G.getMsQuarterlyUpdatedDummyFile(ticker));
		System.out.print("success\n");
	    }

	}
    }


    /** returns false if an exception was thrown AND no data was downloaded.  no exception thrown for empty data (bad ticker) */
    private static boolean downloadAnnualKeyRatios(String ticker) throws InterruptedException, IOException {

	String msUrlTicker = ticker.replace("-", ".");			//MS accepts "bwl.a" and "brk.b" -- uses periods!!
	String urlAnnualBase1 = "http://financials.morningstar.com/ajax/exportKR2CSV.html?&callback=?&t=XNAS:" + msUrlTicker;
	String urlAnnualBase2 = "http://financials.morningstar.com/ajax/exportKR2CSV.html?&callback=?&t=XNYS:" + msUrlTicker;
	String urlAnnualBase3 = "http://financials.morningstar.com/ajax/exportKR2CSV.html?&callback=?&t=XASE:" + msUrlTicker;
	String[] urls = new String[]{urlAnnualBase1, urlAnnualBase2, urlAnnualBase3};

	boolean success = true;

	String fileSt;
	for (String url : urls) {
	    try {
		fileSt = HttpDownloadUtility.getFileSt(url);
		if (!fileSt.isEmpty()) {
		    Files.write(G.getMsAnnualFile(ticker).toPath(), formatMsAnnualsAbridged(fileSt).getBytes());
		    return true;
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
		G.recordFailure(G.getMsAnnualFailedLinksFile(ticker), ticker, url);
		Thread.sleep(1000 * 2);
		success = false;
	    }
	}
	return success;
    }

    /** returns false if an exception was thrown AND no data was downloaded */
    private static boolean downloadQuarterlyFinancials(String ticker) throws InterruptedException, IOException {
	//http://financials.morningstar.com/ajax/ReportProcess4CSV.html?t=aapl&region=USA&culture=us_EN&productCode=COM&reportType=is&period=3&dataType=A&order=asc&columnYear=5&curYearPart=1st5year&view=raw&denominatorView=raw&number=2

	String msUrlTicker = ticker.replace("-", ".");		     //MS accepts "bwl.a" and "brk.b" -- uses periods!!
	String url_incomeStatement = "http://financials.morningstar.com/ajax/ReportProcess4CSV.html?t=" + msUrlTicker + "&region=USA&culture=us_EN&productCode=COM&reportType=is&period=3&dataType=A&order=asc&columnYear=5&curYearPart=1st5year&view=raw&denominatorView=raw&number=3";
	String url_balanceSheet = "http://financials.morningstar.com/ajax/ReportProcess4CSV.html?t=" + msUrlTicker + "&region=USA&culture=us_EN&productCode=COM&reportType=bs&period=3&dataType=A&order=asc&columnYear=5&curYearPart=1st5year&view=raw&denominatorView=raw&number=3";
	String url_cashFlow = "http://financials.morningstar.com/ajax/ReportProcess4CSV.html?t=" + msUrlTicker + "&region=USA&culture=us_EN&productCode=COM&reportType=cf&period=3&dataType=A&order=asc&columnYear=5&curYearPart=1st5year&view=raw&denominatorView=raw&number=3";
	String[] urls = new String[]{url_incomeStatement, url_balanceSheet, url_cashFlow};


	StringBuilder sb = new StringBuilder();
	for (String url : urls) {

	    try {
		sb.append(HttpDownloadUtility.getFileSt(url)).append(System.lineSeparator());
	    } catch (Exception ex) {
		ex.printStackTrace();
		G.recordFailure(G.getMsQuarterlyFailedLinksFile(ticker), ticker, url);
		Thread.sleep(1000 * 2);	    //maybe something wrong w/ connection?
		return false;
	    }
	}
	Files.write(G.getMsQuarterlyFile(ticker).toPath(), formatMsQuarterlies(sb.toString()).getBytes());
	return true;
    }

    /** this prints only EPS, BPS, and shares and fcf ? per share? */
    private static String formatMsAnnualsAbridged(String fileSt) {

	//TODO -- calculate and include FCF per share

	/*
	 Growth Profitability and Financial Ratios for 1347 Property Insurance Holdings Inc
	 Financials
	 ,2004-12,2005-12,2006-12,2007-12,2008-12,2009-12,2010-12,2011-12,2012-12,2013-12,TTM
	
	 Earnings Per Share USD,,,,,,,,,0.12,0.12,0.12
	 Shares Mil,,,,,,,,,12,14,15
	 Book Value Per Share USD,,,,,,,,,,,1.38
	 Free Cash Flow USD Mil,,,,,,,,-1,-6,-10,-6
	 Free Cash Flow Per Share USD,,,,,,,,,,,	(DONT USE THIS ONE!)
    
	 always millions
	 */
	String[] lines = fileSt.split(System.lineSeparator());

	boolean gotHeader = false, gotRevenue = false, gotEPS = false, gotShares = false, gotBPS = false, gotFCF = false;
	String sharesLine = null, fcfLine = null;
	String currency = "";
	//Revenue USD 

	StringBuilder sb = new StringBuilder();

	for (String line : lines) {
	    line = G.cleanCsvLine(line);

	    if (!gotHeader && line.startsWith(",")) {
		gotHeader = true;
		String header = "name" + line.replace("-", "");
		sb.append(header).append(System.lineSeparator());
	    }
	    if (!gotEPS && line.startsWith("Earnings Per Share")) {
		gotEPS = true;
		currency = line.split(",")[0].replace("Earnings Per Share ", "");
		sb.append(G.cleanCsvLine(line)).append(System.lineSeparator());
	    }
	    if (!gotRevenue && line.startsWith("Revenue")) {
		gotRevenue = true;

		String cleancsv = G.cleanCsvLine(line);
		String inDollarsCsv = multiplyEachNumberByOneMillion(cleancsv);

		sb.append(inDollarsCsv).append(System.lineSeparator());
	    }
	    if (!gotShares && line.startsWith("Shares ")) {
		gotShares = true;

		String cleancsv = G.cleanCsvLine(line);
		String inDollarsCsv = multiplyEachNumberByOneMillion(cleancsv);

		sb.append(inDollarsCsv).append(System.lineSeparator());
		sharesLine = line;
	    }
	    if (!gotBPS && line.startsWith("Book Value Per Share ")) {
		gotBPS = true;
		sb.append(line).append(System.lineSeparator());
	    }
	    if (!gotFCF && line.startsWith("Free Cash Flow ")) {	    //does NOT get FCF per share (missing data often) cos that comes next.  
		gotFCF = true;
		sb.append(line).append(System.lineSeparator());
		fcfLine = line;
	    }
	    if (gotBPS && gotEPS && gotFCF && gotHeader && gotShares)
		break;
	}
	if (currency.equals("USD")) {
	    //now make new FCFPS line

	    String FCFPS_Line = getCsvLine_ValuePerShare("FCFPS", fcfLine, sharesLine);

	    sb.append(FCFPS_Line).append(System.lineSeparator());

	    return sb.toString();
	} else {
	    System.out.println("currency " + currency);
	    return "";
	}
    }

    /** this prints only [diluted] shares, EPS, book (total stockholder equity) per share, and free cash flow per share */
    private static String formatMsQuarterlies(String fileSt) {

	//don't print if number of shares not listed

	String[] lines = fileSt.split(System.lineSeparator());

	/*
	
	 Fiscal year ends in September. USD in millions except per share data.,2013-12,2014-03,2014-06,2014-09,2014-12,TTM
	
	 Earnings per share
	 Basic,2.08,1.67,1.29,1.43,3.08,7.44
	 Diluted,2.07,1.66,1.28,1.42,3.06,7.39
	 Weighted average shares outstanding
	 Basic,6273,6123,6013,5934,5843,5978
	 Diluted,6310,6157,6052,5972,5882,6016
	
	
	 Total stockholders' equity,129684,120179,120940,111547,123328
	
	
	
	 Free cash flow,20626,12052,7824,9398,30457,59731
	
	 */

	StringBuilder sb = new StringBuilder();
	String sharesLine = null, revenueLine = null, bookLine = null, fcfLine = null, epsLine = null;


	boolean gotHeader = false, gotRevenue = false, gotEPS = false, gotShares = false, gotBook = false, gotFCF = false;

	for (int i = 0; i < lines.length; i++) {
	    final String line = G.cleanCsvLine(lines[i]);

	    if (!gotHeader && line.contains("Fiscal year ends")) {
		String[] cells = G.split_(line.replace("-", ""));
		cells[0] = "name";
		String joined = String.join(",", cells).trim();
		sb.append(joined).append(System.lineSeparator());
		gotHeader = true;
	    }

	    if (!gotRevenue && line.contains("Revenue,")) {


		String cleancsv = G.cleanCsvLine(line);
		String inDollarsCsv = multiplyEachNumberByOneMillion(cleancsv);

		sb.append(inDollarsCsv).append(System.lineSeparator());
		gotRevenue = true;
		revenueLine = line;
	    }

	    if (!gotEPS && line.contains("Earnings per share")) {
		String dilutedEPS = lines[i + 2].replace("Diluted", "Diluted EPS").trim();
		sb.append(dilutedEPS).append(System.lineSeparator());
		gotEPS = true;
		epsLine = dilutedEPS;
	    }

	    if (!gotShares && line.contains("Weighted average shares outstanding")) {
		String dilutedShares = lines[i + 2].replace("Diluted", "Diluted Shares").trim();


		String cleancsv = G.cleanCsvLine(dilutedShares);
		String inDollarsCsv = multiplyEachNumberByOneMillion(cleancsv);

		sb.append(inDollarsCsv).append(System.lineSeparator());
		gotShares = true;
		sharesLine = dilutedShares;
	    }

	    if (!gotBook && line.contains("Total stockholders' equity")) {

		String cleancsv = G.cleanCsvLine(line);
		String inDollarsCsv = multiplyEachNumberByOneMillion(cleancsv);

		sb.append(inDollarsCsv).append(System.lineSeparator());
		gotBook = true;
		bookLine = line;
	    }

	    if (!gotFCF && line.contains("Free cash flow")) {


		String cleancsv = G.cleanCsvLine(line);
		String inDollarsCsv = multiplyEachNumberByOneMillion(cleancsv);

		sb.append(inDollarsCsv).append(System.lineSeparator());
		gotFCF = true;
		fcfLine = line;
	    }
	}
	if (sharesLine != null && fcfLine != null && bookLine != null
		&& !sharesLine.isEmpty() && !epsLine.isEmpty() && !bookLine.isEmpty() && !fcfLine.isEmpty()) {
	    try {
		String FCFPS_Line = getCsvLine_ValuePerShare("FCFPS", fcfLine, sharesLine);
		sb.append(FCFPS_Line).append(System.lineSeparator());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    try {
		String BPS_Line = getCsvLine_ValuePerShare("BPS", bookLine, sharesLine);
		sb.append(BPS_Line).append(System.lineSeparator());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    return sb.toString();
	} else return "";
    }

    private static String getCsvLine_ValuePerShare(String name, String valuesLine, String sharesLine) {

	String[] sharesSt = G.split_(sharesLine);
	String[] valuesSt = G.split_(valuesLine);

	Long[] shares = new Long[sharesSt.length - 1];
	Double[] values = new Double[valuesSt.length - 1];

	for (int i = 1; i < sharesSt.length; i++)
	    shares[i - 1] = G.parse_LLong(sharesSt[i]);
	for (int i = 1; i < valuesSt.length; i++)
	    values[i - 1] = G.parse_Double(valuesSt[i]);

	String[] ValuePerShareSt = new String[sharesSt.length];
	ValuePerShareSt[0] = name;

	System.out.println(values.length + " " + shares.length);

	for (int i = 1; i < ValuePerShareSt.length; i++)
	    ValuePerShareSt[i] = "";

	for (int i = 1; i < ValuePerShareSt.length && i < values.length + 1 && i < shares.length + 1; i++)
	    if (values[i - 1] != null && shares[i - 1] != null)
		ValuePerShareSt[i] = G.parse_Str(values[i - 1] / shares[i - 1]);


	return String.join(",", ValuePerShareSt);

    }

    private static String multiplyEachNumberByOneMillion(String csv) {

	float one_million = 1_000_000;
	String[] strs = csv.split(",", -1);

	String out = "";

	for (String str : strs) {
	    String finalValue = str;
	    try {
		double d = Double.parseDouble(str);
		d = d * one_million;
		finalValue = G.parse_Str(d);
	    } catch (Exception e) {
	    }
	    out = out + finalValue + ",";
	}
	return out;
    }
}
