package downloaders.regular_stocks_stuff;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.w3c.dom.Document;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NodeList;
import utilities.G;
import utilities.HttpDownloadUtility;
import objects.sec.SECReport;

public class UdFlow_SecReportDates {

    public static void main(String[] args) throws InterruptedException, IOException, XPathExpressionException, ParseException {
//	go(args);
//	go(new String[]{"0"});
//	go(new String[]{"1"});
//	go(new String[]{"2"});
	go(new String[]{"3"});
    }

    public static void go(String[] args) throws IOException, ParseException {
	Integer input = G.initialize("Download_SecReportDates.go", args, G.secDatesDir);

	for (String ticker : G.getIncompleteTickersSubset(args, G.secCompletedDummyDir)) {
	    System.out.print("dl_sec: " + input + ": " + ticker + "  --  ");

	    if (downloadSec(ticker)) {
		G.notateCompletion(G.getSecUpdatedDummyFile(ticker));
		System.out.print("success\n");
	    } else System.out.println();
	}
    }

    @SuppressWarnings("null")
    private static List<SECReport> scrapeNewSECReportsForTicker(String ticker, String startingDate) throws InterruptedException, IOException, XPathExpressionException, ParseException, TickerNotFoundBySecException {

    //http://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=aapl&start=0&count=100&output=atom
	//make files per ticker:
	//  timestamp,type

	//http://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=aapl&start=0&count=100&output=atom
	ticker = ticker.replace(".", "").replace("-", "");

	XPath xPath = XPathFactory.newInstance().newXPath();
	String startKey = "STARTTIMEGOESHEREOPTIONSARE_0_100_200_ETC";

	String urlStartTemplate = "http://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=" + ticker + "&start=" + startKey + "&count=100&output=atom";

	//now get document doc
	//if it contains reports, then get the next page

	List<SECReport> list = new ArrayList();

	for (int start = 0;; start += 100) {
	    String url = urlStartTemplate.replace(startKey, Integer.toString(start));
	    System.out.println(url);
	    String source;
	    try {
		source = HttpDownloadUtility.getPageSource(url);
//		System.out.println("############################################################\n" + source + "\n############################################################");
	    } catch (Exception e) {
		e.printStackTrace();
		G.recordFailure(G.getSecFailedLinksFile(ticker), ticker, url);
		break;
	    }
	    if (source.contains("No matching Ticker Symbol."))
		throw new TickerNotFoundBySecException();
	    if (!sourceContainsSecReports(source)) {
		System.out.println("no reports here");
		break;
	    }
	    Document doc = HttpDownloadUtility.getWebpageDocument_fromSource(source);

	    NodeList reports = (NodeList)xPath.compile("//entry/category").evaluate(doc, XPathConstants.NODESET);

	    SECReport sec = null;			    //just to avoid null derefence warning later
	    for (int i = 0; i < reports.getLength(); i++) {
		sec = new SECReport(reports.item(i));
		System.out.println(sec.outputLine());
		list.add(sec);
	    }
	    if (sec.date.compareTo(startingDate) < 0)
		break;
	}
	return list;
    }

    private static boolean sourceContainsSecReports(String source) {
	return source.contains("<entry>");

    }

    private static boolean downloadSec(String ticker) throws IOException {

	boolean success___dont_need_to_try_again_later = true;

	List<SECReport> secList = new ArrayList();
	try {
	    secList = SECReport.readFromDisk(ticker);
	} catch (G.No_DiskData_Exception ex) {
	}
	String lastRecordedDate = secList.isEmpty() ? (G.minDataDownloadYear + "0101") : secList.get(0).date;

	System.out.println("removing stored sec reports on or later than (its last date) " + lastRecordedDate + " ...");
	SECReport.removeDatesGtE_to_date(secList, lastRecordedDate);

	try {
	    secList.addAll(scrapeNewSECReportsForTicker(ticker, lastRecordedDate));	    //add to secList.  scrape from lastRecordedDate to today
	} catch (TickerNotFoundBySecException ex) {
	    return true;
	} catch (Exception ex) {	//if there's some other exception, don't print anything!  don't want gaps in data. try again later
	    ex.printStackTrace();
	    return false;
	}

	Files.write(G.getSecDatesFile(ticker).toPath(), SECReport.getOutputLines(secList), StandardCharsets.UTF_8);

	return success___dont_need_to_try_again_later;
    }

    private static class TickerNotFoundBySecException extends Exception {
	public TickerNotFoundBySecException() {
	}
    }

}
