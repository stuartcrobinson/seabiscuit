package downloaders.regular_stocks_stuff;

import java.io.IOException;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utilities.G;
import utilities.HttpDownloadUtility;
import static utilities.HttpDownloadUtility.getWebpageDocument_fromSource;
import objects.earnings.Earnings;

public class UdFlow_Earnings_Zacks {

    public static void main(String[] args) throws InterruptedException, IOException {
	go(args);
    }

    //i think this works!
    //export button doestn' work.  release times are wrong
    //pagination buttons seem to be pointless!  looks like the full day's data loads on the page w/out having to click pagination button.  
    public static boolean go(String[] args) {

	String errorOutput = "";
	try {
	    G.initialize("Download_ZacksEarningsCalendar.go", args, G.earningsCalendarsDir);
	    List<Earnings> earningsInfos;
	    earningsInfos = Earnings.readFile(G.zacksEarningsCalendar);
	    System.out.println(earningsInfos.size());
	    String startingDate = Earnings.getEarningsCalendarMostRecentRecordedDateOnDisk(earningsInfos, G.zacksEarningsCalendar, "20040101");
	    System.out.println("starting date: " + startingDate);
	    String earnings_calendar_url = "http://www.zacks.com/earnings/earnings-calendar";
	    String earnings_date_header_xpath = "//*[@id=\"events_list\"]/h1";

	    WebDriver driver = new ChromeDriver();	//   driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	    driver.get(earnings_calendar_url);
	    System.out.println(driver.getCurrentUrl());
	    G.waitForElementByXpath(driver, "//*[@id=\"events_results\"]/tbody/tr[1]/td[2]/span");


	    System.out.println("removing stored earnings on or later than " + startingDate + " ...");
	    Earnings.removeDatesGtE_to_date(earningsInfos, startingDate);

	    String date = null;
	    Document doc = HttpDownloadUtility.getWebpageDocument_fromSource(driver.getPageSource());

	    while (goToPrevious(driver)) {
		doc = HttpDownloadUtility.getWebpageDocument_fromSource(driver.getPageSource());
		date = getWeekSpanStartDate(doc);
		System.out.println("week start date: " + date);
		if (date.compareTo(startingDate) <= 0)
		    break;
	    }

//	    do {
//		goToPrevious(driver);
//		doc = HttpDownloadUtility.getWebpageDocument_fromSource(driver.getPageSource());
//		date = getWeekSpanStartDate(doc);
//		System.out.println("week start date: " + date);
//	    } while ((date).compareTo(startingDate) > 0);

	    int numDaysPastToday = 0;

	    int count = 1;
	    do { //get week's data

		List<String> thisWeeksEarningsLinks_javascripts = getVisibleEarningsLinksJavascripts(driver);

		System.out.println("num earnings javascript links for this week:  " + thisWeeksEarningsLinks_javascripts.size());

		for (String earningsLink_js : thisWeeksEarningsLinks_javascripts) {
		    System.out.println("js " + earningsLink_js + " loading ...");
		    G.executeJavascript_and_wait_for_indicator_text_to_change(driver, earningsLink_js, earnings_date_header_xpath);
		    System.out.println("js loaded");
		    doc = HttpDownloadUtility.getWebpageDocument_fromSource(driver.getPageSource());

		    //now get date's earnings data
		    date = getCurrentPageDate(doc);
		    errorOutput = date;
		    System.out.println(date + " ############################################################################################# " + date);
		    if (date.compareTo(startingDate) < 0) { // || numDaysScraped > 3
			System.out.println("skipping date, too early: " + date);
			continue;
		    }
		    if (date.compareTo(G.currentDate + "") > 0)
			numDaysPastToday++;
		    if (numDaysPastToday > 10)
			break;
		    System.out.println("asdf here1");
		    earningsInfos.addAll(getPageEarningsInfos(driver, date));

		    System.out.println("asdf here2");
		    if (count++ % 10 == 0) {
			System.out.println("printing...");
			Files.write(G.zacksEarningsCalendar.toPath(), Earnings.makeOutputStrings(earningsInfos), StandardCharsets.UTF_8);
		    }

		    System.out.println("asdf here3");
		}
//
//		System.out.println("currentDate: " + (G.currentDate + ""));
//		System.out.println("date: " + date);
//		System.out.println("numDaysPastToday: " + numDaysPastToday);
	    } while (numDaysPastToday < 10 && goToNext(driver)); //&& numDaysScraped < 3

	    driver.quit();
	    System.out.println(earningsInfos.size());

	    Files.write(G.zacksEarningsCalendar.toPath(), Earnings.makeOutputStrings(earningsInfos), StandardCharsets.UTF_8);

	    return true;    //got through all tickers without Exception (if it's still running)
	} catch (Exception ex) {
	    System.out.println("wtf idk " + errorOutput);
	    ex.printStackTrace();
	}
	return false;
    }


    private static String getCurrentPageDate(Document doc) throws ParseException, InterruptedException, Exception {

	SimpleDateFormat zacksHeaderEarningsDate_sdf = new SimpleDateFormat("MMM d, yyyy");

//	G.waitForElementByXpath(driver, "//*[@id=\"events_list\"]/h1");

//	String dateStamp = driver.findElement(By.xpath("//*[@id=\"events_list\"]/h1")).getText();
	XPath xPath = XPathFactory.newInstance().newXPath();

	String dateStamp = (String)xPath.compile("//*[@id=\"events_list\"]/h1").evaluate(doc, XPathConstants.STRING);////*[@id='news-main']/div


	dateStamp = dateStamp.replace("Earnings Announcements", "").trim();
	String date = G.sdf_date.format(zacksHeaderEarningsDate_sdf.parse(dateStamp));

	return date;
    }

    private static String getWeekSpanStartDate(Document doc) throws XPathExpressionException, ParseException {

	SimpleDateFormat zacksHeaderEarningsDate_sdf = new SimpleDateFormat("MM/dd/yyyy");

//	G.waitForElementByXpath(driver, "//*[@id=\"events_list\"]/h1");

//	String dateStamp = driver.findElement(By.xpath("//*[@id=\"events_list\"]/h1")).getText();
	XPath xPath = XPathFactory.newInstance().newXPath();


	//<h1 id="WeeklyEventsTitle">Events for 3/15/2015 - 3/21/2015</h1>
	String dateStamp = (String)xPath.compile("//*[@id=\"WeeklyEventsTitle\"]").evaluate(doc, XPathConstants.STRING);////*[@id='news-main']/div

	dateStamp = dateStamp.split("\\-")[0];
	dateStamp = dateStamp.replace("Events for ", "").trim();
	String date = G.sdf_date.format(zacksHeaderEarningsDate_sdf.parse(dateStamp));

	return date;
    }

    private static List<String> getDayEarningsLinksJs(Document doc) throws XPathExpressionException {

	String dayEarningsLinkJsXpath = "//a[text()='Earnings' and contains(@onclick,'setHighlight')]/@onclick";

	XPath xPath = XPathFactory.newInstance().newXPath();

	NodeList js_nodes = (NodeList)xPath.compile(dayEarningsLinkJsXpath).evaluate(doc, XPathConstants.NODESET);

	List<String> js_list = new ArrayList();

//	//reverse
//	for (int i = js_nodes.getLength() - 1; i >= 0; i--) {
//	    js_list.add(js_nodes.item(i).getTextContent());
//	}

	for (int i = 0; i < js_nodes.getLength(); i++) {
	    js_list.add(js_nodes.item(i).getTextContent());
	}
	return js_list;
    }


    private static boolean goToPrevious(WebDriver driver) throws InterruptedException {
	try {
	    String prevJs = driver.findElement(By.xpath("//*[@id=\"prev_next\"]/div[1]/a")).getAttribute("onclick");
	    return G.executeJavascript_and_wait_for_indicator_text_to_change(driver, prevJs, "//*[@id=\"WeeklyEventsTitle\"]");
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }

    private static boolean goToNext(WebDriver driver) throws Exception {

	String next_link_xpath = "//*[@id=\"prev_next\"]/div[2]/a";
	WebElement next = G.waitForElementByXpath(driver, next_link_xpath);

	try {
	    String nextJs = next.getAttribute("onclick");
	    return G.executeJavascript_and_wait_for_indicator_text_to_change(driver, nextJs, "//*[@id=\"WeeklyEventsTitle\"]");
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }

    private static List<String> getVisibleEarningsLinksJavascripts(WebDriver driver) throws InterruptedException, IOException, XPathExpressionException {
	return getDayEarningsLinksJs(HttpDownloadUtility.getWebpageDocument_fromSource(driver.getPageSource()));

    }


    /** NOTE:  some earnings info is hidden!  but we can record it anyway w/out having to click buttons */
    private static List<Earnings> getPageEarningsInfos(WebDriver driver, String date) throws InterruptedException, IOException, XPathExpressionException {

	List<Earnings> earningsInfos = new ArrayList();

	XPath xPath = XPathFactory.newInstance().newXPath();

	Document doc = getWebpageDocument_fromSource(driver.getPageSource()); //this is much faster than trying to get strings from selenium

	NodeList rows_nodes = (NodeList)xPath.compile("//*[@id=\"events_results\"]/tbody/tr").evaluate(doc, XPathConstants.NODESET);////*[@id='news-main']/div

	for (int j = 0; j < rows_nodes.getLength(); j++) {

	    Node row_node = rows_nodes.item(j);
//	    System.out.println(row_node.getTextContent().trim().split("\\s")[0]);

	    NodeList td_nodes = (NodeList)xPath.evaluate("./td", row_node, XPathConstants.NODESET);////*[@id='news-main']/div

	    for (int m = 0; m < td_nodes.getLength(); m++) {
		Node td = td_nodes.item(m);
		System.out.print(td.getTextContent() + " * ");
	    }
	    System.out.println();
	    earningsInfos.add(new Earnings(date, td_nodes));
	}
	return earningsInfos;
    }

    

}
