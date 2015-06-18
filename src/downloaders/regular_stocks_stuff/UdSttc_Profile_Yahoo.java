package downloaders.regular_stocks_stuff;

import java.io.IOException;
import java.text.ParseException;
import org.htmlcleaner.XPatherException;
import utilities.G;
import objects.profile.YahooProfile;
//industry, sector, employees, some weird scores, address?

public class UdSttc_Profile_Yahoo {

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
	go(args);
    }

    /** input int 0, 1, 2, or 3 for subdivision of tickers list */
    public static void go(String[] args) throws IOException, ParseException {
	Integer input = G.initialize("download yahoo profile", args, G.yahooProfileDir);

	for (String ticker : G.getIncompleteTickersSubset(args, G.yahooProfileCompletedDummyDir)) {
	    System.out.print("d_p " + input + ": " + ticker + " -- ");

	    if (downloadYahooProfileYeah(ticker)) {
		G.notateCompletion(G.getYahooProfileUpdatedDummyFile(ticker));
		System.out.print(" -- success!\n");
	    } else System.out.print("\n");
	}
    }

    private static YahooProfile downloadYahooProfile(String ticker) throws XPatherException, IOException, InterruptedException, ParseException, TickerNotFoundByYahooException, YahooProfile.NoYahooProfileForThisStock, YahooProfile.SpecificStockCouldntBeMatchedWithTicker, YahooProfile.TickerIsNoLongerValidInYahoo_itChanged {
	return new YahooProfile(ticker, true);
    }

    private static boolean downloadYahooProfileYeah(String ticker) {

	try {
	    if (G.fileIsMoreThanThisManyDaysOld(G.getYahooProfileFile(ticker), 12)) {

		YahooProfile p;
		try {
		    p = downloadYahooProfile(ticker);
		    p.writeToFile();
		} catch (TickerNotFoundByYahooException | YahooProfile.NoYahooProfileForThisStock | YahooProfile.SpecificStockCouldntBeMatchedWithTicker | YahooProfile.TickerIsNoLongerValidInYahoo_itChanged ex) {
		    System.out.print(" -- no data ");
		    return true;						    //don't try this again.
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return false;		    //something weird happened, try again later
	}
	return true;
    }

    public static class TickerNotFoundByYahooException extends Exception {
	public TickerNotFoundByYahooException() {
	}
    }
}
