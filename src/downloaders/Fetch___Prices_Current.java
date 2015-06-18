package downloaders;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import utilities.G;
import static utilities.G.symbolsFile;
import utilities.HttpDownloadUtility;
import objects.prices.CurrentPrices;

public class Fetch___Prices_Current {

    public static Map<String, CurrentPrices> download_currentPrices_as_ticker_prices_map() throws IOException, InterruptedException {
	return download_currentPrices(Files.readAllLines(symbolsFile.toPath(), StandardCharsets.UTF_8));
    }

    public static Map<String, CurrentPrices> download_currentPrices(List<String> tickers) throws IOException, InterruptedException {
	//for each ticker in symbols file
	//	append ticker to URL for yahoo gooeystuff url
	//download gooeystuff file
	//send data to squisher
	System.out.println("getting current prices...");

	Map<String, CurrentPrices> ticker_prices_map = new LinkedHashMap<>();

	for (List<String> symbolsList : Lists.partition(tickers, 199)) {
	    //o: open, h: high, g: low, l1: last price, v: volume
	    String URL_KEY = "TICKERS_LIST_SEPARATED_BY_PLUSSES";
	    String urlTemplate = "http://finance.yahoo.com/d/quotes.csv?s=" + URL_KEY + "&f=ohgl1v";

	    StringBuilder sbUrlTickers = new StringBuilder();

	    for (String tick : symbolsList) {
		sbUrlTickers.append(tick);
		sbUrlTickers.append("+");
	    }
	    sbUrlTickers = sbUrlTickers.deleteCharAt(sbUrlTickers.length() - 1);

	    String url = urlTemplate.replace(URL_KEY, sbUrlTickers.toString());

	    System.out.println("fetching " + url);

	    List<String> results;
	    try {
		results = HttpDownloadUtility.getFile(url);
	    } catch (Exception e) {
//		e.printStackTrace();
		System.out.println("   try again - 2nd time");
		try {
		    results = HttpDownloadUtility.getFile(url);
		} catch (Exception e1) {
//		    e1.printStackTrace();
		    System.out.println("   try again - 3rd time - last time");
		    results = HttpDownloadUtility.getFile(url);
		}
	    }
	    
	    for (int i = 0; i < symbolsList.size(); i++)
		ticker_prices_map.put(symbolsList.get(i), new CurrentPrices(G.currentDate, results.get(i)));
	}
//	timer.lapPrint();
	return ticker_prices_map;
    }

}
