package downloaders.regular_stocks_stuff;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import utilities.G;
import utilities.HttpDownloadUtility;

/** <b>failure handling:</b> <br> if successful attempt to download non-existing ticker, a 404 web page is returned. and a java.io.FileNotFoundException is thrown. <br><br> if wireless is off, java.net.UnknownHostException is thrown <br><br> so this will save a dummy completed file if there is no exception or a FileNotFoundException*/
public class Replace_Prices_Daily {

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
//	go(args);
	
	go(new String[]{"0"});
//	go(new String[]{"1"});
//	go(new String[]{"2"});
//	go(new String[]{"3"});
    }

    /** for testing */
    public static void download(String ticker) throws InterruptedException, IOException, ParseException {
	G.initialize("download yahoo historical prices", null, G.pricesDailyCompletedDummyDir, G.pricesDailyDir);

	if (downloadPrices(ticker, G.minDataDownloadYear)) {
	    G.notateCompletion(G.getPricesDailyUpdatedDummyFile(ticker));
	}
    }

    /** input int 0, 1, 2, or 3 for subdivision of tickers list */
    public static void go(String[] args) throws IOException, InterruptedException, ParseException {
	Integer input = G.initialize("download yahoo historical prices", args, G.pricesDailyCompletedDummyDir, G.pricesDailyDir);


	for (String ticker : G.getIncompleteTickersSubset(args, G.pricesDailyCompletedDummyDir)) {
	    System.out.print("d_hp " + input + ": " + ticker + "  --  ");

	    if (downloadPrices(ticker, G.minDataDownloadYear)) {
		G.notateCompletion(G.getPricesDailyUpdatedDummyFile(ticker));
		System.out.print("success\n");
	    } else System.out.println();
	}
    }

    /** returns false if a web-related exception was thrown AND no data was downloaded.  no exception thrown for empty data (bad ticker) */
    private static boolean downloadPrices(String ticker, String hp_min_year) throws InterruptedException, IOException {

	String url = "http://ichart.finance.yahoo.com/table.csv?s=" + ticker + "&a=00&b=1&c=" + hp_min_year;
	try {
	    HttpDownloadUtility.downloadFile(url, G.getPricesDailyFile(ticker), false);	//don't be persistent here.  because we'll run this program again several times to retry the failures later.  don't waste time now.
	} catch (FileNotFoundException e1) {
	    System.out.println("    File not found.  assuming that " + ticker + " DNE in yahoo prices.  will not retry");
	} catch (Exception e) {
	    e.printStackTrace();
	    G.recordFailure(G.getPricesDailyFailedLinksFile(ticker), ticker, url);
	    Thread.sleep(2000);
	    return false;
	}
	return true;
    }

}
