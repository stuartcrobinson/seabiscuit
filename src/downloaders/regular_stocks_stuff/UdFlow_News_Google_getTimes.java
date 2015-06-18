package downloaders.regular_stocks_stuff;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import utilities.G;
import utilities.G.GoogleBlockedMeException;
import utilities.HttpDownloadUtility;
import objects.news.News;
import objects.Symbol;
import objects.news.News.MyDateTime;

public class UdFlow_News_Google_getTimes {

    /**
     go through downloaded news info.  get times for articles that need it. 
     */
    public static void main(String[] args) throws InterruptedException, IOException, GoogleBlockedMeException, ParseException {
	go(args);
    }

    /**
     args: {partition, getArticlePubTimes}.  make partition = "" if don't want to partition.  is true or false for getArticlePubTimes.  does NOT make partition of tickers list since it can only do like 1700 at a time.  but what about when getting timestamps?  maybe that would delay it enough to go slow enough to not get blocked? leave args in there.  just make the first parameter empty if we don't want to partition
     <br><br>NOTE:  preferentially save the yahoo press release url vs google.  yahoo more likely to contain time stamp.
     <br><br>    <b> failure handling </b><br> saves dummy if data success or invalid ticker
     */
    public static void go(String[] args) throws InterruptedException, IOException, GoogleBlockedMeException, ParseException {
	Integer input = G.initialize("download google news pubtimes", args, new File[]{G.newsDir, G.newsGoogleDir, G.newsGoogleCompletedDummyDir});

	List<Symbol> symbolsInfo = Symbol.getSymbolsList();

	for (String ticker : G.getTickerSubsetAmongThese(args, G.newsGoogleDir)) {
	    System.out.println("d_newsG_pt " + input + ": " + ticker + ": " + Symbol.getCompanyName(symbolsInfo, ticker));
	    tryToUpdatePubTimes(ticker);
	}
    }

    /** returns "" if no match found */
    private static String getTimeStamp(String html) {

	//what about pages that show the current time!?!?!? yahoo
	if (html.contains("abbr>")) {
	    String[] ar = html.split("abbr>");
	    html = "&" + ar[1];
	}
//b 12, 2015, 6:00am EST <sp
	//">February 11, 2015, 07:48:53 PM EDT</span
	//<span>February 15 2015 7:33 PM</span>		--ibtimes
	String[] regexs = new String[]{
	    "(?s).*?<p class=\"date\">\\w{3} \\d{2}, \\d{4}, (\\d{2}:\\d{2}) .*?", //prnewswire search: <p class="date">Jan 06, 2015, 06:30 ET</p>
	    "(?s).*?Published: <span>\\w+ \\d?\\d, \\d{4} (\\d?\\d:\\d\\d) ([aApP]\\.?[mM]\\.?).*?", //marketwatch Published: <span>Feb 19, 2015 12:43 p.m.  -- http://www.marketwatch.com/story/great-ajax-corp-closes-initial-public-offering-2015-02-19
	    "(?s).*?>\\w+ \\d\\d, \\d{4} (\\d\\d:\\d\\d)</time>.*?", //>January 05, 2015 08:00</time>	--globe newswire
	    "(?s).*?meta name=\"sailthru.date\" content=\"\\d{4}-\\d{2}-\\d{2} (\\d\\d:\\d\\d):\\d\\d\".*?", //meta name="sailthru.date" content="2015-01-05 14:03:39" -- fortune.com
	    "(?s).*?<span>\\w+ \\d?\\d \\d{4} (\\d?\\d:\\d\\d) (AM|PM)</span>.*?", //<span>February 15 2015 7:33 PM</span>		--ibtimes
	    "(?s).*?, (\\d\\d:\\d\\d):\\d\\d ([aApP][mM]).*?", //">February 11, 2015, 07:48:53 PM EDT</span -- nasdaq
	    "(?s).*? (\\d?\\d:\\d\\d)([aApP][mM]).*?",
	    "(?s).*? (\\d?\\d:\\d\\d)  ([aApP][mM]).*?",
	    "(?s).*?\\D(\\d?\\d:\\d\\d)[ ]*([aApP]\\.?[mM]\\.?).*?"
	};

	for (String regex : regexs) {
	    Pattern pattern = Pattern.compile(regex);
	    Matcher matcher = pattern.matcher(html);

	    if (matcher.matches()) {

		try {
		    String timePortion = matcher.group(1);
		    String concatTimeStamp;
		    int hour = Integer.parseInt(timePortion.split(":")[0]);
		    try {
			String ampmPortion = matcher.group(2).toUpperCase();
			concatTimeStamp = timePortion + " " + ampmPortion.replaceAll("\\.", "");
		    } catch (Exception e) {  //no am/mp	 -- todo - check and see if there is a group 2 instead of trycatch
			concatTimeStamp = timePortion + " AM";
		    }
		    SimpleDateFormat timeAmPmSdf = new SimpleDateFormat("hh:mm a");

		    String myTime = G.sdf_militaryTime.format(timeAmPmSdf.parse(concatTimeStamp));

//		    System.out.println("regexs[" + i + "]: " + regex);
		    return myTime;
		} catch (ParseException ex) {
		    ex.printStackTrace();
		    System.exit(0);
		}
	    }
	}
	//"1 hour ago" "29 seconds ago" etc
	{
	    Pattern pattern = Pattern.compile("(?s).*?\\D(\\d+)[ ]+([Hh]our[s]?|[Mm]inute[s]?|[Ss]econd[s]?)[ ]+ago.*?");
	    Matcher matcher = pattern.matcher(html);

	    if (matcher.matches()) {

//		System.out.println("here3");

		String timeAmount = matcher.group(1);
		String timeUnit = matcher.group(2).toLowerCase();

		MyDateTime defaultDateTime = parseGoogleDateStamp_for_MyDateTime(timeAmount + " " + timeUnit + " ago");  //cludgey

		return defaultDateTime.time;

	    }
	}
	return "";

    }

    private static MyDateTime getDateTimeForGoogleArticle_withDefaultBackup(MyDateTime defaultDateTime, String subarticlehref) throws InterruptedException, IOException {

	//try to get date and time from webpage.  for failed returns, use defaults.

	if (!defaultDateTime.time.equals("")) //this should never happen.  should be caught early.  just doublechecking. redundant=bad?
	    return defaultDateTime;

	MyDateTime dateTime = try_to_get_webpage_DateTime(subarticlehref);

	if (dateTime.date.equals("")) //it is possible (rare)that the  final date would be "".  if the main article default date got a parsing exception.  like if google formatted it weird.  and no unique date found on article page.
	    return new MyDateTime(defaultDateTime.date, dateTime.time);		//use the original time.  pages often have multiple dates in html.  rarely multiple times.
	else
	    return dateTime;
    }

    private static MyDateTime parseGoogleDateStamp_for_MyDateTime(String dateStamp) {

	try {
	    String date, time;
	    SimpleDateFormat sdf_google = new SimpleDateFormat("MMM d, yyyy");
	    int minutes = 0;
	    if (dateStamp.contains("ago")) {
		if (dateStamp.contains("hour")) {
		    String hoursSt = dateStamp.replaceAll(" .*", "");
		    int hours = Integer.parseInt(hoursSt);
		    minutes = hours * 60;
		}
		if (dateStamp.contains("minute")) {
		    String minutesSt = dateStamp.replaceAll(" .*", "");
		    minutes = Integer.parseInt(minutesSt);
		}
		Date dateDate = G.currentDateDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateDate); // convert your date to Calendar object
		int minutesToDecrement = minutes * -1;
		cal.add(Calendar.MINUTE, minutesToDecrement);
		dateDate = cal.getTime();

		date = G.sdf_date.format(dateDate);
		time = G.sdf_militaryTime.format(dateDate);
	    } else {
		date = G.sdf_date.format(sdf_google.parse(dateStamp));
		time = "";
	    }
	    return new MyDateTime(date, time);
	} catch (ParseException ex) {
	    System.out.println("THIS IS VERY STRANGE! \n***********************************************************************\n******************************************************************\n***********************************************************************\n******************************************************************\n***********************************************************************\n******************************************************************\n***********************************************************************\n******************************************************************\n***********************************************************************\n******************************************************************\n***********************************************************************\n******************************************************************\n***********************************************************************\n******************************************************************\n");
	    ex.printStackTrace();
//	    System.exit(0);
	}
	return null;
    }

    private static MyDateTime try_to_get_webpage_DateTime(String subarticlehref) throws InterruptedException, IOException {


	if (subarticlehref.contains("prnewswire.com/news")) //TODO - i should just extract title from origianl newswire url.  to avoid extra function parameters
	    return getPubTimeForPRNewsWireArticle(subarticlehref);


	String html = HttpDownloadUtility.getFileSt(subarticlehref);
//	String html = HttpDownloadUtility.getFileSt("http://www.purple.com/purple.html");

//	System.out.println(html);

	String time = getTimeStamp(html);
	String date = getDateStampIfOnlyOneDate(html);
	if (date == null) {
//	    try {
//		throw new NoDateFoundException();
//	    } catch (NoDateFoundException ex) {
//		System.out.println("no date here? " + subarticlehref);
	    date = "";
//		ex.printStackTrace();
//	    }
	}
	return new MyDateTime(date, time);
    }

    /** null if not found.  return array.  first element is the sdf_date formatted date.  second element is the matched text on the page */
    private static String[] getDate_andMatchedText(String html) {

	try {
	    /*	 ine">Published: February 11, 2015 at 5:57 am</h6> <
	     <span itemprop="datePublished" content="2015-02-11T14:06:15Z">Feb. 11, 2015  9:06 AM ET</span>
	     Published: Feb 18, 2015 at 9:15 am EST
	     Feb 17, 2015
	     data-timestamp="Feb. 16, 2015 7:40 p.m. ET"
	     h</a> &middot; February 18, 2015 05:22 AM PST</p>
	     February 17, 2015 | by&nbsp;Chris Johnson</div><!-- /sin
	     Tuesday, 27 Jan 2015 04:30pm EST
	     op="dateCreated" datetime="2015-02-17T20:56">20:56 17.02.2015</time>(updated 21:22 17.02.2015) </div><div class="b-counters
	     February 17, 2015 at 9:40 am
	     ot;:&quot;Feb. 17, 2015 8:06 p.m. ET&quot;}}'      --view-source:http://www.wsj.com/articles/digits-apple-orders-more-than-five-million-watches-1424221600
	     : 18:42 EST, 10 February 2015 |
	     <span class="timestamp">Wed Feb 11, 2015 10:46pm EST</span>
	     
	     imi</a> on Feb 19th, 2015 // <a 
	     Jan 31st, 2015
	     get google news -- assume the clustered articles are on the same date.  search for a new date.  if the page has only one match, then use it.  otherwise, use the default date.
	     getDateStampIfOnlyOneDate
	     */
	    html = "&" + html;	    //to make sure string starts with non-word and non-digit character


	    //note: MMM works for Feb and February
	    String date;
	    {
		//tamp="Feb. 16th, 2015 7:
		//lass="date">Jan 06, 2015, 06:30 ET</p>

		SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy");
		Pattern pattern = Pattern.compile("(?s).*?\\W((\\w{3,}?)\\.?[ ]+(\\d{1,2})(?:st|nd|rd|th)?[, ]+(\\d{4})).*?");
		Matcher matcher = pattern.matcher(html);

		if (matcher.matches()) {
		    String raw = matcher.group(1);
		    String month = matcher.group(2);
		    String day = matcher.group(3);
		    String year = matcher.group(4);

//		    System.out.println("1: " + matcher.group(1));
//		    System.out.println("2: " + matcher.group(2));
//		    System.out.println("3: " + matcher.group(3));

		    String dateSt = month.substring(0, 3) + " " + day + " " + year;  //substring month incase of misspellings
//		    System.out.println(dateSt);
		    date = G.sdf_date.format(sdf.parse(dateSt));
		    return new String[]{date, raw};		//we are returning the matcher.group(1) so that it can be removed from the html and looked for date again.  if no date found, then keep this one.  so we know it was the only date on the page
		}
	    }
	    {
		//time="2015-02-17T20:56">
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
		Pattern pattern = Pattern.compile("(?s).*\\D(\\d{4}-\\d{2}-\\d{2})\\D.*");
		Matcher matcher = pattern.matcher(html);

		if (matcher.matches()) {
		    String dateSt = matcher.group(1);
		    date = G.sdf_date.format(sdf.parse(dateSt));
		    return new String[]{date, matcher.group(1)};
		}
	    }
	    {
		//18:42 EST, 10 February 2015 |
		SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy");
		Pattern pattern = Pattern.compile("(?s).*\\D(\\d{1,2} \\w{3,}? \\d{4})\\D.*");
		Matcher matcher = pattern.matcher(html);

		if (matcher.matches()) {
		    String dateSt = matcher.group(1);
		    date = G.sdf_date.format(sdf.parse(dateSt));
		    return new String[]{date, matcher.group(1)};
		}
	    }
	} catch (ParseException e) {
	    e.printStackTrace();
//	    System.exit(0);
	}

	return null;
    }

    /** null if no date found.  "" if two dates found.   */
    private static String getDateStampIfOnlyOneDate(String html) {

	String[] date_dateSt_ar = getDate_andMatchedText(html);	    //bad when returns null

	if (date_dateSt_ar == null)
	    return null;

//	System.out.println("found " + date_dateSt_ar[0]);

	String newHtml = html.replace(date_dateSt_ar[1], "");
	String[] newResult = getDate_andMatchedText(newHtml);
	if (newResult == null)
	    return date_dateSt_ar[0];
	else
	    return "";
    }


    private static String removeCrapFromURL(String href) {
	/*
	 https://www.google.com/url?source=finance&q=http%3A%2F%2Fwww.reuters.com%2Ffinance%2Fstocks%2FINOV.O%2Fkey-developments%2Farticle%2F3153229&ei=g7rmVNH8L43MsQflr4HABw&usg=AFQjCNFphlyhcHA2M0-19qbpTvN6XJZZrQ
	 */
	String googleReutersPrefix = "//www.google.com/url?source=finance&q=http%3A%2F%2F";

	if (href.contains(googleReutersPrefix)) {
	    href = href.replace(googleReutersPrefix, "http://");
	    href = href.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("&.*", "");
	    return href;
	}

	/*
	 http://news.google.com/news/url?sa=T&ct2=us&fd=S&url=http://www.livetradingnews.com/ipo-schedule-week-us-95429.htm&cid=0&ei=9s_mVKjDAdOksQfe6IDYCA&usg=AFQjCNF6QgikrqxYZNAbCVIU03AwMWUtvQ
	 http://news.google.com/news/url?sa=T&ct2=us&fd=S&url=http://www.livetradingnews.com/ipo-schedule-week-us-95429.htm&cid=0&ei=xtTmVNEPyuDxBtGTgLAH&usg=AFQjCNE3Dim3eCAAslN3jrD_HEqMzJIQqw
	 http://news.google.com/news/url?sa=T&ct2=us&fd=S&url=http://www.equities.com/editors-desk/investing-strategies/ipo/7-new-ipos-scheduled-for-feb-9wk-to-raise-1-billion&cid=0&ei=7MXnVOmbI-nxsAeEoIDgBg&usg=AFQjCNE2LQhOhulwy6sESk03oh3nSfqMog
	 http://news.google.com/news/url?sa=T&ct2=us&fd=S&url=
	 */
	String googlePrefix = "http://news.google.com/news/url?sa=T&ct2=us&fd=S&url=";
	if (href.startsWith(googlePrefix)) {
	    href = href.replace(googlePrefix, "").replaceAll("&.*", "").replaceAll("%.*", "");
	    return href;
	}

	href = href.replaceAll(".*http://", "http://").replaceAll("\\?.*", "").replaceAll("&.*", "");
	return href;

    }

    private static MyDateTime getPubTimeForPRNewsWireArticle(String url) throws InterruptedException, IOException {

//	System.out.println("prenewswire!");
	String cleanTitle = getPrNewsWireTitleFromArticleURL(url);

//	System.out.println("prenewswire title: "+ cleanTitle);
	String prnewswire_search_url = "http://www.prnewswire.com/search-results/news/" + cleanTitle + "-180-days";
	prnewswire_search_url = fixUpUrl(prnewswire_search_url);

//	System.out.println("prenewswire search urL: " + prnewswire_search_url);
	return try_to_get_webpage_DateTime(prnewswire_search_url);

    }

    private static String getPrNewsWireTitleFromArticleURL(String url) {

	//http://www.prnewswire.com/news-releases/st-pete-server-gets-10000-surprise-from-squaremouth-and-mayor-kriseman-300038929.html

	Pattern pattern = Pattern.compile(".*prnewswire.com/news-releases/(.*?)\\d+\\.html.*");
	Matcher matcher = pattern.matcher(url);
	if (matcher.matches())
	    return matcher.group(1).replaceAll("-", " ").replaceAll("\\W", " ").replaceAll("  ", " ");
	else
	    return null;
    }

    private static String fixUpUrl(String url) {
	return url.replaceAll(" ", "%20");
    }


    private static void tryToUpdatePubTimes(String ticker) throws InterruptedException, IOException, GoogleBlockedMeException {
	List<News> newsList = News.readFromDisk(G.getNewsGoogleFile(ticker));
	if (newsList.isEmpty()) return;			    //should never happen

	int count = 0;
	for (News news : newsList) {
	    if (news.openArticleToScrapePubtimeLater || news.openArticleToScrapePubDateLater) {
		if (news.pubTimeFetchAttemps > 10) {
		    news.openArticleToScrapePubDateLater = false;
		    news.openArticleToScrapePubtimeLater = false;
		    news.pubTimeFetchTotalFailure = true;
		    continue;
		}
		MyDateTime dateTime = new MyDateTime(news.date, news.time);
		try {
		    System.out.println(news.date + " " + news.time + " " + news.source + " " + news.title + " " + news.pubTimeFetchTotalFailure + " " + removeCrapFromURL(news.url) + " ");
		    news.pubTimeFetchAttemps++;
		    dateTime = try_to_get_webpage_DateTime(removeCrapFromURL(news.url));    //removeCrapFromURL(...) so it loads faster, so we don't have to go through google's servers first.  or yahoos idk
		} catch (Exception m) {
		    try {
			System.out.println(news.date + " " + news.time + " " + news.source + " " + news.title + " " + news.pubTimeFetchTotalFailure + " " + news.url + " ");
			dateTime = try_to_get_webpage_DateTime(news.url);
		    } catch (Exception e) {
			G.recordFailure(G.getNewsGoogleFailedLinksFile(ticker), ticker, news.url + " " + removeCrapFromURL(news.url));
//			e.printStackTrace();
			System.out.println("   -- fail");
			continue;
		    }
		}
		if (dateTime.time == null) //will this ever be?
		    dateTime.time = "";

		if (news.openArticleToScrapePubDateLater && !dateTime.date.isEmpty()) {
		    news.date = dateTime.date;
		    news.openArticleToScrapePubDateLater = false;
		}
		if (news.openArticleToScrapePubtimeLater && !dateTime.time.isEmpty()) {
		    news.time = dateTime.time;
		    news.openArticleToScrapePubtimeLater = false;
		}
		System.out.println(news.date + " " + news.time);
		if (count % 5 == 0)
		    Files.write(G.getNewsGoogleFile(ticker).toPath(), News.getOutputLines(new ArrayList(newsList)), StandardCharsets.UTF_8);
	    }
	}
	Files.write(G.getNewsGoogleFile(ticker).toPath(), News.getOutputLines(newsList), StandardCharsets.UTF_8);
    }
}
