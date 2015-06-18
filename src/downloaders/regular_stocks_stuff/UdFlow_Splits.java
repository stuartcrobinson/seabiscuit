package downloaders.regular_stocks_stuff;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utilities.G;
import static utilities.G.sdf_date;
import objects.splitsAndDividends.Split;

//todo this is really stupid.  jsoup method of looking for splits and click next pages is terrible.

/** NOTE:  this downloads dividends into the splits files.  we just need dates of split or divs.  cos prices are screwed up around both :( ex: BAA 2016-05-24 */
public class UdFlow_Splits {


    public static void main(String[] args) throws InterruptedException, IOException, XPathExpressionException, ParseException, G.No_DiskData_Exception {
//	go(args);

//	go(new String[]{"0"});
//	go(new String[]{"1"});
//	go(new String[]{"2"});
	go(new String[]{"3"});
    }

    /* input int 0, 1, 2, or 3 for subdivision of tickers list */
    public static void go(String[] args) throws InterruptedException, IOException, XPathExpressionException, ParseException {
	Integer input = G.initialize("download splits", args, G.splitsDir);

	for (String ticker : G.getIncompleteTickersSubset(args, G.splitsCompletedDummyDir)) {
	    System.out.print("dl_splits: " + input + ": " + ticker + "  --  ");

	    if (downloadSplits(ticker)) {
		G.notateCompletion(G.getSplitsUpdatedDummyFile(ticker));
		System.out.print("success!\n");
	    } else System.out.println();
	}
    }


    private static List<Split> scrapeSplits(String ticker, int lastRecordedDate) throws TryAgainLaterException, DontTryAgainLaterException {

	List<Split> splits = new ArrayList();

	SimpleDateFormat yahooDateFormat = new SimpleDateFormat("MMM dd, yyyy");

	String urlStr = "http://finance.yahoo.com/q/hp?s=" + ticker + "&a=11&b=12&c=1960&g=v";
	Elements nextLinks;

	boolean splitOrDivWasEverFound = false;
	do {
	    Document doc = null;
	    boolean doIt = true;
	    while (doIt) {
		try {
		    doc = Jsoup.connect(urlStr).get();
		    doIt = false;//org.jsoup.HttpStatusException
		} catch (java.net.UnknownHostException e) {
		    throw new TryAgainLaterException();
		} catch (org.jsoup.HttpStatusException e) {
		    throw new DontTryAgainLaterException();	//so later it will return true.  this is for bogus ticker.  don't try again later
		} catch (Exception e) {
		    e.printStackTrace();
		    System.out.println(e.getMessage());
		    if (e.getMessage().contains("HTTP error fetching URL"))
			throw new TryAgainLaterException();
		    doIt = true;
		}
	    }

	    String html = doc.html();
	    boolean thisPageHasSplitOrDividend = html.contains("Stock Split") || html.contains(" Dividend");
	    boolean thisPageHasNextPageLink = html.contains("<a rel=\"next\"");

	    if (thisPageHasSplitOrDividend) {
	    } else if (thisPageHasNextPageLink) {

		nextLinks = doc.getElementsByAttributeValueMatching("rel", "next");
		String urlNextPost;
		if (nextLinks.size() > 0) {
		    urlNextPost = nextLinks.get(0).attr("href");
		    urlStr = "http://finance.yahoo.com" + urlNextPost;
		}
		continue;
	    } else {
		if (splitOrDivWasEverFound) {
		}
		break;
	    }

	    nextLinks = doc.getElementsByAttributeValueMatching("rel", "next");
	    String urlNextPost;
	    if (nextLinks.size() > 0) {
		urlNextPost = nextLinks.get(0).attr("href");
		urlStr = "http://finance.yahoo.com" + urlNextPost;
	    }

	    Elements newsHeadlines = doc.getElementsByClass("yfnc_tabledata1");
	    boolean splitOrDivFoundOnThisPage = false;
	    String prevText = "";
	    for (Element e : newsHeadlines) {

		String text = e.text();
		if (text.contains("Stock Split") || text.contains(" Dividend")) {
		    String outputDateStr;
		    String yhDate = prevText.trim();
		    try {

			Date d = yahooDateFormat.parse(yhDate);
			outputDateStr = sdf_date.format(d);
			int dateInt = Integer.parseInt(outputDateStr);
			if (dateInt < lastRecordedDate)
			    return splits;
		    } catch (Exception ee) {
			outputDateStr = yhDate.replace("-", "");
		    }

		    if (text.contains("Stock Split")) {
			text = text.replace("Stock Split", "").replace(" ", "");
			String[] nums = text.split(":");
			Split s = new Split(outputDateStr, nums[0], nums[1]);
			splits.add(s);
			System.out.println(s.outputLine());
		    }
		    if (text.contains(" Dividend")) {
			text = text.replace(" Dividend", "").replace(" ", "");
			String num = text;
			Split s = new Split(outputDateStr, "-1", "-1");		//just need dates of splits and divs cos prices are screwed up around them
			splits.add(s);
			System.out.println(s.outputLine());
		    }
		    splitOrDivFoundOnThisPage = true;
		}
		prevText = e.text();

	    }
	    if (splitOrDivFoundOnThisPage) {
//			System.out.println("     split found here");
		splitOrDivFoundOnThisPage = false;
	    } else {
//			System.out.println("     split not found here");
	    }

	    if (!thisPageHasNextPageLink && splitOrDivWasEverFound) {
		break;
	    }
	} while (nextLinks.size() > 0);
	return splits;
    }

    private static boolean downloadSplits(String ticker) throws IOException {

	boolean success___dont_need_to_try_again_later = true;

	List<Split> splits = new ArrayList();
	try {
	    splits = Split.readFromDisk(ticker);
	} catch (G.No_DiskData_Exception ex) {
	}
	int lastRecordedDate = splits.isEmpty() ? 10661014 : splits.get(0).date;

	List<Split> newsplits;
	try {
	    newsplits = scrapeSplits(ticker, lastRecordedDate);
	} catch (TryAgainLaterException ex) {
	    return false;
	} catch (DontTryAgainLaterException ex) {
	    return true;
	}

	if (newsplits.isEmpty()) //bogus ticker.  if there was a connection problem, exception thrown
	    return true;

	splits.addAll(newsplits);	    //add to secList.  scrape from lastRecordedDate to today

	Split.writeToDisk(splits, ticker);

	return success___dont_need_to_try_again_later;

    }

    private static class TryAgainLaterException extends Exception {
	public TryAgainLaterException() {
	}
    }

    private static class DontTryAgainLaterException extends Exception {
	public DontTryAgainLaterException() {
	}
    }
}
