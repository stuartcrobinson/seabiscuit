package downloaders.regular_stocks_stuff;

import java.io.IOException;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utilities.G;
import utilities.HttpDownloadUtility;
import objects.earnings.Earnings;

public class UdFlow_Earnings_Estimize {
    private static String estimize_birth_date = "20110101";

    public static void main(String[] args) throws InterruptedException, IOException {
	go(args);
    }

    public static boolean go(String[] args) {
	String errorOutputUrl = "";
	try {
	    G.initialize("Download_EstimizeEarningsCalendar.go", args, G.earningsCalendarsDir);
	    List<Earnings> earningsInfos = Earnings.readFile(G.estimizeEarningsCalendar);
	    System.out.println(earningsInfos.size());
	    String startingDate = Earnings.getEarningsCalendarMostRecentRecordedDateOnDisk(earningsInfos, G.estimizeEarningsCalendar, estimize_birth_date);
	    System.out.println("starting date: " + startingDate);

	    System.out.println("removing stored earnings on or later than " + startingDate + " ...");
	    Earnings.removeDatesGtE_to_date(earningsInfos, startingDate);

	    List<String> urls = getDatelyUrls(startingDate, 10);

	    int count = 1;
	    for (String url : urls) {
		earningsInfos.addAll(getEstimizeEarningsInfosForDayWebpage(url));
		errorOutputUrl = url;
		if (count++ % 20 == 0)
		    Earnings.writeListToDisk(G.estimizeEarningsCalendar, earningsInfos);
	    }
	    Earnings.writeListToDisk(G.estimizeEarningsCalendar, earningsInfos);

	    return true;    //got through all tickers without Exception (if it's still running)
	} catch (Exception ex) {
	    System.out.println("wtf idk " + errorOutputUrl);
	    ex.printStackTrace();
	}
	return false;
    }


    /** https://www.estimize.com/calendar/2014/12/25?page=2 */
    private static String get_url(Calendar cal) {

	int year = cal.get(Calendar.YEAR);
	int month = cal.get(Calendar.MONTH) + 1;
	int day = cal.get(Calendar.DAY_OF_MONTH);

	String url = "https://www.estimize.com/calendar/" + year + "/" + month + "/" + day + "/";

	return url;
    }


    /** one week from today */
    private static Calendar getFirstDateToScrape(int numDaysInFuture) {

	Calendar cal = Calendar.getInstance();
	cal.add(Calendar.DAY_OF_YEAR, numDaysInFuture);
	return cal;
    }

    /** starting with one week from today, all the way back in time to lastGoodRecordedDate, gotten from file or default */
    private static List<String> getDatelyUrls(String startDate, int numDaysInFuture) throws ParseException {

	Date startDateDate = G.sdf_date.parse(startDate);
	Calendar startDateCal = Calendar.getInstance();
	startDateCal.setTime(startDateDate);

	Calendar cal = getFirstDateToScrape(numDaysInFuture);

	List<String> urls = new ArrayList();

	while (!cal.before(startDateCal)) {
	    if (G.isDateCalWeekday(cal))
		urls.add(get_url(cal));
	    cal = getPreviousDay(cal);
	}
	Collections.sort(urls);	    //increasing
	return urls;
    }

    /** sorted decreasing i think.  sorted naturally not by me.  by insertion order that is */
    private static List<String> getDatelyUrls_fromEstimizeBirthToEndDate(String earliestDateOnDisk) throws ParseException {

	Date earliestRecordedDate = G.sdf_date.parse(earliestDateOnDisk);
	Calendar cal = Calendar.getInstance();
	cal.setTime(earliestRecordedDate);

	Calendar estimizeBirthDate = Calendar.getInstance();
	estimizeBirthDate.setTime(G.sdf_date.parse(estimize_birth_date));

//	Calendar cal = Calendar.getInstance();
//	cal.setTime(G.sdf_date.parse("20110101"));

	List<String> urls = new ArrayList();

	while (!cal.before(estimizeBirthDate)) {
	    if (G.isDateCalWeekday(cal))
		urls.add(get_url(cal));
	    cal = getPreviousDay(cal);
	}
	return urls;
    }

    private static Calendar getPreviousDay(Calendar cal) {
	cal.add(Calendar.DAY_OF_YEAR, -1);
	return cal;
    }

    private static List<Earnings> getEstimizeEarningsInfosForDayWebpage(String url) throws InterruptedException, IOException, XPathExpressionException, ParseException {

	System.out.println(url);
	Document doc = HttpDownloadUtility.getWebpageDocument(url);
	String date = getDateFromEstimizeUrlAndDoc(url, doc);

	List<Earnings> earningsInfos = new ArrayList();

	earningsInfos.addAll(getEstimizeEarningsInfosFromDoc(date, doc));

	XPath xPath = XPathFactory.newInstance().newXPath();
	String show_all_stocks_button_href = (String)xPath.compile("//a[@class=\"more\"]/@href").evaluate(doc, XPathConstants.STRING);

	if (!show_all_stocks_button_href.isEmpty()) {
	    url = "https://www.estimize.com" + show_all_stocks_button_href;
	    System.out.println("MORE button found!  " + url);
	    System.out.println(url);
	    earningsInfos.addAll(getEstimizeEarningsInfosFromDoc(date, HttpDownloadUtility.getWebpageDocument(url)));
	}

	return earningsInfos;
    }

    /** works! ...mize.com/calendar/2015/2/19/ returns 20150219*/
    private static String getDateFromEstimizeUrlAndDoc(String url, Document doc) throws ParseException, XPathExpressionException {

	String st = url.split("calendar")[1];
	st = st.replaceAll("/", " ").trim();

	String year = st.split(" ")[0];

	String yearlessDateXpath = "//strong[@class=\"calendar-report-date\"]/text()";
	XPath xPath = XPathFactory.newInstance().newXPath();
	String estimizeYearlessDate = (String)xPath.compile(yearlessDateXpath).evaluate(doc, XPathConstants.STRING);

	//Monday, March 2

	String fullDate = estimizeYearlessDate + ", " + year;

	//now format is like: Monday, March 2, 1984
	SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd, yyyy");

	return G.sdf_date.format(sdf.parse(fullDate));

    }

    private static List<Earnings> getEstimizeEarningsInfosFromDoc(String date, Document doc) throws XPathExpressionException, ParseException {
	List<Earnings> earningsInfos = new ArrayList();
	String earningsWeblineDivXpath = "//div[contains(@class, 'tbody')]/div";    //includes gov't data
	XPath xPath = XPathFactory.newInstance().newXPath();
	NodeList earningsDivs = (NodeList)xPath.compile(earningsWeblineDivXpath).evaluate(doc, XPathConstants.NODESET);

	for (int i = 0; i < earningsDivs.getLength(); i++) {
	    Node earningsDiv = earningsDivs.item(i);

	    Earnings ei = new Earnings(date, earningsDiv);
	    earningsInfos.add(ei);
	}

	return earningsInfos;
    }

    public static boolean dateStIsWeekend(String dateSt) throws ParseException {
	Date d = G.sdf_date.parse(dateSt);
	Calendar cal = Calendar.getInstance();
	cal.setTime(d);
	return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }


}
