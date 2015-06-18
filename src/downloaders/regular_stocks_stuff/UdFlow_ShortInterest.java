package downloaders.regular_stocks_stuff;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import utilities.G;
import static utilities.HttpDownloadUtility.getPageSource;
import static utilities.G.getTagNode;
import static utilities.G.sdf_date;
import objects.short_interest.ShortInterest;
import utilities.HttpDownloadUtility;

//get 2 years for $30 http://shortsqueeze.com/Sign-Up.php
public class UdFlow_ShortInterest {

    public static void main(String[] args) throws InterruptedException, IOException, XPathExpressionException, ParseException, G.No_DiskData_Exception, XPatherException {

//	go(args);
	go(new String[]{"2"});
    }

    /* input int 0, 1, 2, or 3 for subdivision of tickers list */
    public static void go(String[] args) throws InterruptedException, IOException, XPathExpressionException, ParseException, XPatherException {
	Integer input = G.initialize("download si", args, G.shortInterestDir);

	for (String ticker : G.getIncompleteTickersSubset(args, G.shortInterestCompletedDummyDir)) {
	    System.out.print("dl_si: " + input + ": " + ticker + "  --  ");

	    if (downloadSI(ticker)) {
		G.notateCompletion(G.getShortInterestUpdatedDummyFile(ticker));
		System.out.print("success\n");
	    } else System.out.println();
	}
    }


    private static List<ShortInterest> scrape(String ticker) throws InterruptedException, XPatherException, ParseException, TickerNotFoundForNasdaqSIException, IOException, G.DontTryAgainLaterException {

	//interesting: http://www.nasdaq.com/symbol/adxsw/short-interest -- sometimes data is blank, maybe we should interperet that as "0"?
//	    doesn't really matter, that occurs with such tiny stocks anyway.  volume too low, will get thrown out

	SimpleDateFormat sdf_nasdaq = new SimpleDateFormat("MM/dd/yyyy");

	List<ShortInterest> list = new ArrayList();

	String source = null;
	try {
	    source = getPageSource("http://www.nasdaq.com/symbol/" + ticker + "/short-interest");
	} catch (HttpDownloadUtility.PermissionDeniedWebException ex) {
	    System.out.println(ex.toString());
	    throw new G.DontTryAgainLaterException();
//	    System.exit(0);
	}
	if (source.contains("No Short Interest"))
	    throw new TickerNotFoundForNasdaqSIException();
	TagNode node = getTagNode(source);

	Object[] table_rows = node.evaluateXPath(".//table[@id='quotes_content_left_ShortInterest1_ShortInterestGrid']/tbody/tr");

	for (Object row : table_rows) {
	    Object[] c1ar = ((TagNode)row).evaluateXPath("td[1]");
	    Object[] c2ar = ((TagNode)row).evaluateXPath("td[2]");
	    Object[] c3ar = ((TagNode)row).evaluateXPath("td[3]");
//	    Object[] c4ar = ((TagNode)row).evaluateXPath("td[4]");

	    String date_nasdaq = ((TagNode)c1ar[0]).getText().toString().trim();
	    String shortinterestStr = ((TagNode)c2ar[0]).getText().toString().trim();
	    String avDlyShareVolStr = ((TagNode)c3ar[0]).getText().toString().trim();
//	    String daysToCoverStr = ((TagNode)c4ar[0]).getText().toString().trim();	    //calculate this myself

	    Date d = sdf_nasdaq.parse(date_nasdaq);
	    try {									//note: sometimes nasdaq data has a row with missing data.  that's why this trycatch is here.  to just ignore those.
		int dateInt = Integer.parseInt(sdf_date.format(d));
		float shortInterest = Float.parseFloat(shortinterestStr.replaceAll(",", "").replace("&nbsp;", ""));
		float aveDailyShareVol = Float.parseFloat(avDlyShareVolStr.replaceAll(",", ""));
//	    float daysToCover = Float.parseFloat(daysToCoverStr);				//don't use nasdaq reported daysToCover cos it doesn't go lower than 1.  i think that's just stupid

		ShortInterest si = new ShortInterest(dateInt, shortInterest, aveDailyShareVol);

		System.out.println(si.outputLine());
		list.add(si);
	    } catch (Exception e) {
		System.out.println("error with ticker " + ticker + ": " + e.getMessage());
	    }
	}
	return list;
    }

    private static boolean downloadSI(String ticker) {

	boolean success___dont_need_to_try_again_later = true;
	try {


	    List<ShortInterest> shortInterests = new ArrayList<>();
	    try {
		shortInterests = ShortInterest.readFromDisk(ticker);
	    } catch (G.No_DiskData_Exception ex) {
	    }
	    int lastSiDate = shortInterests.isEmpty() ? 10661014 : shortInterests.get(0).settlementDate;
	    System.out.println("last short interest date: " + lastSiDate + ". days since then: " + G.daysSinceDate(lastSiDate));


	    if (G.daysSinceDate(lastSiDate) < 12)
		return true;				//short interest reports posted every 2 weeks.  mid-month and end of month (on nasdaq)
	    try {
		shortInterests.addAll(scrape(ticker));	 //date doesn't matter here cos all info is on one webpage.  download the whole thing. nasdaq only shows 12 months at a time :( but we will save them!  to make archive for longtime :)
	    } catch (TickerNotFoundForNasdaqSIException | G.DontTryAgainLaterException ex) {
		ex.printStackTrace();
		return true;
	    } catch (Exception ex) {
		ex.printStackTrace();
		return false;
	    }

	    ShortInterest.writeToDisk(shortInterests, ticker);

	} catch (IOException | ParseException ex) {
	    System.out.println("something unexpected and weird happened (so try again later) WTF ??? ex.toString(): " + ex.toString());
	    ex.printStackTrace();
	    success___dont_need_to_try_again_later = false;
	}

	return success___dont_need_to_try_again_later;
    }

    private static class TickerNotFoundForNasdaqSIException extends Exception {
	public TickerNotFoundForNasdaqSIException() {
	}
    }
}
