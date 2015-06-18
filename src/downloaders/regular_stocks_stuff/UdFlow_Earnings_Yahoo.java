package downloaders.regular_stocks_stuff;

import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import utilities.G;
import utilities.HttpDownloadUtility;
import objects.earnings.Earnings;

public class UdFlow_Earnings_Yahoo {

    public static void main(String[] args) throws InterruptedException, IOException, ParseException, Exception {
	go(args);
    }

    //TODO later -- get company names

    /** this gets page links from the lists at the top of earnings calendar yahoo pages.  this skips some data because the links dont list weekend days.  but some earnings reports do come out on weekends.  maybe just weird and foreign stuff tho 
     NOTE:  brk earnings release date isn't listed on yahoo OR zacks!  but it is listed here:  http://www.nasdaq.com/earnings/report/brk-b */
    public static boolean go(String[] args) throws InterruptedException, IOException, ParseException {
	String errorOutput = "";
	try {
	    G.initialize("Download_YahooEarningsCalendar.go", args, G.earningsCalendarsDir);

	    List<Earnings> earningsInfos = Earnings.readFile(G.yahooEarningsCalendar);
	    String startingDate;
	    startingDate = Earnings.getEarningsCalendarMostRecentRecordedDateOnDisk(earningsInfos, G.yahooEarningsCalendar, "20040101");
	    String linkDate = startingDate;
	    List<String> linkDates = new ArrayList();

	    System.out.println("removing stored earnings on or later than " + startingDate + " ...");
	    Earnings.removeDatesGtE_to_date(earningsInfos, startingDate);

	    int count = 1;
	    do {
		errorOutput = linkDate;
		try {
		    String html = getPreppedPageSourceForLinkDatePage(linkDate);
		    addPageEarningsInfos(linkDate, earningsInfos, html);
		    if (count++ % 60 == 0)
			Earnings.writeListToDisk(G.yahooEarningsCalendar, earningsInfos);
		    linkDate = getNextYahooEarningsCalendarDate_and_update_linkDates(linkDates, html, linkDate);
		} catch (java.io.FileNotFoundException fnfe) {
		    linkDate = getNextYahooEarningsCalendarDate_and_update_linkDates(linkDates, null, linkDate);
		    G.asdf("using next weekday date: " + linkDate);
		}
	    } while (date_is_not_more_than_a_week_from_today(linkDate));
	    Earnings.writeListToDisk(G.yahooEarningsCalendar, earningsInfos);

	    return true;    //got through all tickers without Exception (if it's still running)
	} catch (Exception ex) {
	    System.out.println("wtf idk " + errorOutput);
	    ex.printStackTrace();
	}
	return false;
    }

    public static String getPreppedPageSourceForLinkDatePage(String linkDate) throws java.io.FileNotFoundException, InterruptedException, IOException, ParseException {

	String html;
	String url = yahooEarningsCalendarUrl(linkDate);
	System.out.println(url);
	try {
	    html = HttpDownloadUtility.getPageSource(url, false);
	} catch (java.io.FileNotFoundException e) {
	    G.asdf("file not found: " + url);
	    throw e;
//	    try {
//		html = HttpDownloadUtility.getPageSource(yahooEarningsCalendarUrl(G.incrementDateByDays(linkDate, 1)));
//	    } catch (java.io.FileNotFoundException e2) {
//		html = HttpDownloadUtility.getPageSource(yahooEarningsCalendarUrl(G.incrementDateByDays(linkDate, 1)));
//	    }
	}
	html = html.replaceAll("⌂", "");
	html = html.replaceAll("<small>", "⌂");
	html = html.replaceAll("♣", "");
	html = html.replaceAll("finance\\.yahoo\\.com/q\\?s=", "♣");
	return html;
    }

    private static List<String> updateLinkDates(String html, List<String> linkDates) {
	Pattern otherDayLinkDateStrings = Pattern.compile("<a href=/research/earncal/(\\d+)\\.html>[^P]");
	Matcher matcher = otherDayLinkDateStrings.matcher(html);

	while (matcher.find()) {
	    linkDates.add(matcher.group(1));
	}
	return linkDates;
    }

    public static boolean addPageEarningsInfos(String linkDate, List<Earnings> earningsInfos, String html) throws ParseException {

	/*
	 href="http://♣aoi">AOI</a></td><td
	 align=center>⌂After Market Close</small></td><td

	 href="http://♣cdvi">CDVI</a></td><td
	 align=center>-0.06</td><td
	 align=center>⌂After Market Close</small></td><td
	 */
//	System.out.println(html);
	Pattern symbol_and_time_pattern = Pattern.compile("(?s)♣([^\"]+)[^⌂]+⌂([^<]+)<");
	Matcher matcher = symbol_and_time_pattern.matcher(html);

//	System.out.println();

	while (matcher.find()) {
	    String ticker = matcher.group(1).toUpperCase();
//	    System.out.println(ticker);
	    String yahooTimeStamp = matcher.group(2);

//	    System.out.println(ticker + " " + yahooTimeStamp);
	    earningsInfos.add(new Earnings(linkDate, ticker, yahooTimeStamp));
	}
//	System.out.println();

	return true;
    }

    public static boolean date_is_not_more_than_a_week_from_today(String dateSt) throws ParseException {

	Calendar weekFromNow_cal = Calendar.getInstance();
	weekFromNow_cal.add(Calendar.DAY_OF_YEAR, 7);
	Date weekFromNow_date = weekFromNow_cal.getTime();

	Date dateYahoo = G.sdf_date.parse(dateSt);

	return dateYahoo.compareTo(weekFromNow_date) < 0;
    }

    private static String yahooEarningsCalendarUrl(String linkDate) {
	return "http://biz.yahoo.com/research/earncal/" + linkDate + ".html";
    }

    private static String getNextYahooEarningsCalendarDate_and_update_linkDates(List<String> linkDates, String html, String prevLinkDate) throws ParseException {

	if (linkDates.isEmpty()) {
	    if (html == null) {
		return G.getNextWeekdayDate(prevLinkDate);
	    } else
		updateLinkDates(html, linkDates);
	}
	return linkDates.remove(0);
    }


}
/*
 href="http://♣aoi">AOI</a></td><td
 align=center>⌂After Market Close</small></td><td

 href="http://♣cdvi">CDVI</a></td><td
 align=center>-0.06</td><td
 align=center>⌂After Market Close</small></td><td
	
	
	
	
	
	
	
	
	
	
 <a href=/research/earncal/20031222.html>Prev. Week</a>
 |
 <a href=/research/earncal/20031229.html>Dec 29</a>
 |
 <a href=/research/earncal/20031230.html>Dec 30</a>
 |
 <font color=gray>Dec 31</font>
 |
 <b>Jan 1</b>
 |
 <font color=gray>Jan 2</font>
 |
 <a href=/research/earncal/20040105.html>Next Week</a></center><p><table
 */
