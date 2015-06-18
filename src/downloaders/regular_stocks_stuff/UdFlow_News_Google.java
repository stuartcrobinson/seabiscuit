package downloaders.regular_stocks_stuff;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.w3c.dom.Document;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utilities.G;
import utilities.G.GoogleBlockedMeException;
import utilities.HttpDownloadUtility;
import static utilities.G.tickerKey;
import static downloaders.regular_stocks_stuff.UdFlow_News_Yahoo.normal;
import objects.news.News;
import objects.Symbol;
import objects.news.News.MyDateTime;
import objects.news.News.NoDateFoundException;
import static objects.news.News.doesNewsTitleContainCompanyName;

public class UdFlow_News_Google {

    /**
     input int 0, 1, 2, or 3 for subdivision of tickers list.  2nd element: input true/false for if you want to dig for article publication times  <br><br>
     newsDir is permanent archive of news.  always being added to and improved.  <br><br>
    
     each day, fetch news without digging for news times.  but record the times if they're listed on the front page of google or yahoo or whatever.<br><br>
    
     at other times, fetch news AND dig for article publish times.  this might take 20 full days to get all news and article times.  so do this gradually over time<br><br>
    
     or just build this up slowly by getting daily news that has timestamps included (except google sub articles) <br><br>
    
     G.newsCompletedDummyDir holds a file with stock name if that stock has been completely newsed in this current iteration.<br><br>
    
     another file records urls that didn't load.  maybe the syntax was wrong, maybe we have to go through more loops to find time (like prnewswire), etc<br><br>
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
	Integer input = G.initialize("download google news", args, new File[]{G.newsDir, G.newsGoogleDir, G.newsGoogleCompletedDummyDir});

	boolean getArticlePubTimes = false;	    // this is permanently false now!	    //args == null || args.length == 0 ? false : Boolean.parseBoolean(args[1]);

	List<Symbol> symbolsInfo = Symbol.getSymbolsList();

	for (String ticker : G.getIncompleteTickersSubset(args, G.newsGoogleCompletedDummyDir)) {
	    System.out.println("d_newsG " + input + ": " + ticker + ": " + Symbol.getCompanyName(symbolsInfo, ticker));
	    if (downloadGoogleNews(ticker, symbolsInfo, getArticlePubTimes)) {
		G.notateCompletion(G.getNewsGoogleUpdatedDummyFile(ticker));
		System.out.println("                                       success!");
	    } else System.out.println();
	}
    }

    /** returns"" if not found on page */
    public static String getWebpageTimestamp(String href) throws IOException, InterruptedException {
	String html = HttpDownloadUtility.getFileSt(href);
	return getTimeStamp(html);
    }

    /** returns "" if no match found */
    public static String getTimeStamp(String html) {

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

    /** google's news is not sorted, so don't stop just cos you find an article we already have in file <br><br> TODO break this up -- first, get the google document.  then go through yahoo news.  then look through google news.  cos subarticles on google's page might be listed in yahoo with the actual timestamp! */
    private static void newsList_getGoogle(List<News> newsList, List<Symbol> symbols, String ticker, Document doc, String lastRecordedDate, boolean getArticlePubTimes) throws IOException {
//	System.out.println("**************************** GOOGLE ********************************");

	try {
	    XPath xPath = XPathFactory.newInstance().newXPath();
	    NodeList articleBlocks = (NodeList)xPath.compile("//*[@id='news-main']/div").evaluate(doc, XPathConstants.NODESET);////*[@id='news-main']/div

	    for (int i = 0; i < articleBlocks.getLength(); i++) {
//		System.out.println("________________________________");
		Node articleBlock = articleBlocks.item(i);

		News mainGoogleArticleNews = addNewsFromGoogleMainArticle(articleBlocks.item(i), xPath, newsList, symbols, ticker, getArticlePubTimes);

//		System.out.print("f34r8349u   " + mainGoogleArticleNews);
//		System.out.print("f34r8349u   " + articleBlocks.item(i).getTextContent());
//		System.out.print("f34r8349u   " + mainGoogleArticleNews.date);
//		System.out.println("f34r8349u   " + mainGoogleArticleNews.time);

		Node articleblockContent = (Node)xPath.evaluate("./div[2]", articleBlock, XPathConstants.NODE);
//		System.out.println("subarticlenode text w984yw9e8r : " + articleblockContent.getTextContent());

		NodeList subarticles_a = (NodeList)xPath.evaluate("./a", articleblockContent, XPathConstants.NODESET);
		for (int k = 0; k < subarticles_a.getLength(); k++) {
		    Node a = subarticles_a.item(k);

		    addNewsFromGoogleSubArticle(a, xPath, new MyDateTime(mainGoogleArticleNews.date, mainGoogleArticleNews.time),
			    symbols, ticker, getArticlePubTimes, newsList);
		}
	    }
	} catch (XPathExpressionException | InterruptedException ex) {
	    ex.printStackTrace();
	}
    }

    private static MyDateTime getDateTimeForGoogleArticle_withDefaultBackup(MyDateTime defaultDateTime, String subarticlehref, String title) throws InterruptedException, IOException {

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

    public static MyDateTime try_to_get_webpage_DateTime(String subarticlehref) throws InterruptedException, IOException {


	if (subarticlehref.contains("prnewswire.com/news")) //TODO - i should just extract title from origianl newswire url.  to avoid extra function parameters
	    return getPubTimeForPRNewsWireArticle(subarticlehref);

	String html = HttpDownloadUtility.getFileSt(subarticlehref);

//	System.out.println(html);

	String time = getTimeStamp(html);
	String date = getDateStampIfOnlyOneDate(html);
	if (date == null) {
	    try {
		throw new NoDateFoundException();
	    } catch (NoDateFoundException ex) {
//		System.out.println("no date here? " + subarticlehref);
		date = "";
		ex.printStackTrace();
	    }
	}
	return new MyDateTime(date, time);
    }

    /** null if not found.  return array.  first element is the sdf_date formatted date.  second element is the matched text on the page */
    public static String[] getDate_andMatchedText(String html) {

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
		Pattern pattern = Pattern.compile("(?s).*\\D(\\d+ \\w{3,}? \\d{4})\\D.*");
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
    public static String getDateStampIfOnlyOneDate(String html) {

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


    public static String removeCrapFromGoogleURL(String href) {
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

    public static MyDateTime getPubTimeForPRNewsWireArticle(String url) throws InterruptedException, IOException {

//	System.out.println("prenewswire!");
	String cleanTitle = getPrNewsWireTitleFromArticleURL(url);

//	System.out.println("prenewswire title: "+ cleanTitle);
	String prnewswire_search_url = "http://www.prnewswire.com/search-results/news/" + cleanTitle + "-180-days";
	prnewswire_search_url = fixUpUrl(prnewswire_search_url);

//	System.out.println("prenewswire search urL: " + prnewswire_search_url);
	return try_to_get_webpage_DateTime(prnewswire_search_url);

    }

    public static String getPrNewsWireTitleFromArticleURL(String url) {

	//http://www.prnewswire.com/news-releases/st-pete-server-gets-10000-surprise-from-squaremouth-and-mayor-kriseman-300038929.html

	Pattern pattern = Pattern.compile(".*prnewswire.com/news-releases/(.*?)\\d+\\.html.*");
	Matcher matcher = pattern.matcher(url);
	if (matcher.matches())
	    return matcher.group(1).replaceAll("-", " ").replaceAll("\\W", " ").replaceAll("  ", " ");
	else
	    return null;


    }

    public static String fixUpUrl(String url) {
	return url.replaceAll(" ", "%20");
    }

    /** skip if the article already exists.  unless this is newly a press release.  and unless we're getting times now and the existing one doesn't have a time. */
    public static boolean skip(String title, String url, String agg, String date, boolean isPressRelease, List<News> newsList, boolean getArticlePubTimes) {

	List<Integer> indicesToRemove = new ArrayList();

	for (int i = 0; i < newsList.size(); i++) {
	    News news = newsList.get(i);
	    if (normal(news.title).equals(normal(title)) && (news.date.equals(date) || news.url.equals(url))) {		//article match.  flexible date v. url matching since sometimes google dates are wrong (1  day off) and the article url might be different on yahoo vs. google.  but even if both fail, after we get the actual article time stamp, these existing rare duplicates would get fixed up
		if (getArticlePubTimes) {
		    if (news.pubTimeHasBeenInvestigated) {
			if (isPressRelease && !news.isPressRelease) {
			    news.isPressRelease = true;
			}
			if (!news.aggregator.contains(agg))
			    news.aggregator += agg;
//			System.out.println("skipping insertion cos this article already found.");
//			System.out.println(news.outputLine().replaceAll("\t", "\n"));
			return true;
		    } //else probably return false - if none other duplicates found who HAVE gotten the time already (like from alternative aggregator).
		    else
			indicesToRemove.add(i);	    //wtf does this not get used?	//if the article exists w/out time-find-attempt, and we're looking for time now, then delete the existing entry.  time trumps press release
		} else if (!getArticlePubTimes) {	    //if we're not getting times, and the article exists already... check press release status...
		    if (isPressRelease && !news.isPressRelease) {
			news.isPressRelease = true;
		    }
		    if (!news.aggregator.contains(agg))
			news.aggregator += agg;
//		    System.out.println("skipping insertion cos this article already found.");
//		    System.out.println(news.outputLine().replaceAll("\t", "\n"));

		    //TODO start here!  see if this outputs properly when scraping a press release entry that was already scraped as a normal article
		    return true;
		}
	    }
	}

	if (G.listContainsDuplicates(indicesToRemove)) {
	    System.out.println("AAAGHGHGHGHGH indicesToRemove contains duplicates! wtf!!!!");
	    System.exit(0);
	}

	if (!indicesToRemove.isEmpty()) {
	    Collections.sort(indicesToRemove, Collections.reverseOrder());
	    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%    SKIP-REMOVING     %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	    System.out.println("found " + indicesToRemove.size() + " news items to remove from list!");

	    for (int i : indicesToRemove) {
		System.out.println("REMOVING: " + newsList.get(i).outputLine().replaceAll("\t", "\n"));
		newsList.remove(i);
	    }
	    System.out.println("%%%%%%%%%%%%%%%%%%%%%%----- finished skipremoving -----%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}
	//removing elements that exist currently without time, but which we're going to look for the time for now.

	return false;			//don't skip
    }


    private static News addNewsFromGoogleSubArticle(Node a, XPath xPath, MyDateTime mainArticleDateTime,
	    List<Symbol> symbols, String ticker, boolean getArticlePubTimes, List<News> newsList)
	    throws XPathExpressionException, InterruptedException, IOException {

	String title = G.cleanUpHtmlSyntax(a.getTextContent());
	if (title.contains("Related articles »"))
	    return null;
	String href = (String)xPath.evaluate("./@href", a, XPathConstants.STRING);
	Node sourceNode = a.getNextSibling().getNextSibling();
	String newsSource = G.cleanUpHtmlSyntax(sourceNode.getTextContent());	    //was removecrapfrom googleulr -- wtf why?i think that was amistkae

//	System.out.println("4g5t45t4 subarticlenode title : " + title + ", source: " + newsSource);

	if (skip(title, href, News.Aggregators.G, "", false, newsList, getArticlePubTimes))
	    return null;

	MyDateTime dateTime = mainArticleDateTime;

	if (getArticlePubTimes)
	    dateTime = getDateTimeForGoogleArticle_withDefaultBackup(mainArticleDateTime, href, title);

	News news = new News(dateTime.date, dateTime.time, false, News.Aggregators.G,
		newsSource, title, doesNewsTitleContainCompanyName(symbols, title, ticker), href, getArticlePubTimes);

	news.openArticleToScrapePubDateLater = true;
	news.openArticleToScrapePubtimeLater = true;

//	System.out.println(news.onScreenOutputLine());
//	System.out.println(news.onScreenOutputBlock());
	
	System.out.println(news.onScreenOutputLine(true));

	newsList.add(news);
	return news;
    }

    private static News addNewsFromGoogleMainArticle(Node articleBlock, XPath xPath, List<News> newsList,
	    List<Symbol> symbols, String ticker, boolean getArticlePubTimes) throws XPathExpressionException, InterruptedException, IOException {

	Node byline_div = (Node)xPath.evaluate("./div[@class='byline']", articleBlock, XPathConstants.NODE);

	String source = (String)xPath.evaluate("./span[1]/text()", byline_div, XPathConstants.STRING);
	String dateStamp = (String)xPath.evaluate("./span[@class='date']", byline_div, XPathConstants.STRING);

	MyDateTime dateTime = parseGoogleDateStamp_for_MyDateTime(dateStamp);

	Node titleLink_a = (Node)xPath.evaluate("./span/a", articleBlock, XPathConstants.NODE);
	String title = G.cleanUpHtmlSyntax(titleLink_a.getTextContent());

	String href = G.cleanUpHtmlSyntax((String)xPath.evaluate("./@href", titleLink_a, XPathConstants.STRING));
	String originalHref = href;
//	href = removeCrapFromURL(href);

	boolean skipThis = false;

	if (skip(title, href, News.Aggregators.G, dateTime.date, false, newsList, getArticlePubTimes))
	    skipThis = true;   //skip subarticles too -- that's no good

	//TODO start here ! i just removed the previous if block that continued if it hit a certain date.
	//i'm changing it now to not care about dates but rather, whether or not we are currently fetching article times.  
	//because if we ARE, then we want to re-fetch article info that we already have (if it hasn't had the date fetched yet)
	//NOW check the skip() function.   then see if that aligns with adding the News item to the list.  
	//also , add a function to clear out duplicate entries from newsList - in case yahoo and google both add the same article.
	//   QUESTION - is it possible for yahoo to add the same article twice?  try to make sure that the SKIP() function prevents that.
	if (getArticlePubTimes && dateTime.time.equals("")) {

	    // handle the main article
	    try {
		dateTime = getDateTimeForGoogleArticle_withDefaultBackup(dateTime, href, title);
	    } catch (IOException m) {
		try {
		    href = originalHref;
		    dateTime = getDateTimeForGoogleArticle_withDefaultBackup(dateTime, originalHref, title);
		} catch (IOException e) {
		    G.recordFailure(G.getNewsGoogleFailedLinksFile(ticker), ticker, href);
		    e.printStackTrace();
		}
	    }
	    if (dateTime.time == null) //will this ever be?
		dateTime.time = "";
	}
	News news = new News(dateTime.date, dateTime.time, false, News.Aggregators.G,
		source, title, doesNewsTitleContainCompanyName(symbols, title, ticker), href, getArticlePubTimes);
	if (dateTime.time.equals(""))
	    news.openArticleToScrapePubtimeLater = true;

	if (!skipThis)
	    newsList.add(news);
//	System.out.println("_________" + news.outputLine().replaceAll("\t", "\n"));
	System.out.println(news.onScreenOutputLine());

	return news;
    }

    public static String getGoogleNewsPageSource(String ticker) throws InterruptedException, IOException {

	String urlTemplate = "https://www.google.com/finance/company_news?q=" + tickerKey + "&start=0&num=10000";
	String googleNewsUrl = urlTemplate.replace(G.tickerKey, ticker.replace("-", "."));

	System.out.println(googleNewsUrl);
	return HttpDownloadUtility.getPageSource(googleNewsUrl);
    }


    private static boolean downloadGoogleNews(String ticker, List<Symbol> symbolsInfo, boolean getArticlePubTimes) throws InterruptedException, IOException, GoogleBlockedMeException {

	//if page source does NOT contain: "Articles published" -- but it DOES contain "Recent Quotes" -- then it was a succesful loading of a bogus ticker.  do not try again later.

	//TODO -- running test now!  trying to get blocked by google to see how it responds.  
	//FileNotFoundException returned when going to bad google url (while not blockd)
	boolean success___dont_need_to_try_again_later = true;

	List<News> newsList = News.readFromDisk(G.getNewsGoogleFile(ticker));

	String lastRecordedDate = newsList.isEmpty() ? null : newsList.get(0).date;

	String source = null;

	try {
	    source = getGoogleNewsPageSource(ticker);
	} catch (IOException e) {
	    if (e.getMessage().contains("Server returned HTTP response code: 503"))
		throw new G.GoogleBlockedMeException();
	}

	if (!source.contains("Articles published") && source.contains("Recent Quotes")) //google doens't have this company.  don't try again later
	    return true;

	if (!source.contains("Recent Quotes")) //idk wtf happened.  try again later.
	    return false;

//	System.out.println("###############################################\n" + source + "\n###############################################\n");
	Document googleNewsPageDoc = HttpDownloadUtility.getWebpageDocument_fromSource(source);    //throw new exception or return false pending what happens here


	newsList_getGoogle(newsList, symbolsInfo, ticker, googleNewsPageDoc, lastRecordedDate, getArticlePubTimes);

	News.writeToDisk(newsList, G.getNewsGoogleFile(ticker));

	//return false if we need to try this again later

	//throw an exception if we got blocked by google and we need to wait an hour
	return success___dont_need_to_try_again_later;
    }
    /*
    
     boolean success___dont_need_to_try_again_later = true;

     List<News> newsList = News.readNewsFile(G.getGoogleNewsFile(ticker));

     String lastRecordedDate = newsList.isEmpty() ? null : newsList.get(0).date;

     String urlTemplate = "https://www.google.com/finance/com999999pany_news?q=" + tickerKey + "&start=0&num=10000";
     String googleNewsUrl = urlTemplate.replace(G.tickerKey, ticker.replace("-", "."));
     String source = HttpDownloadUtility.getPageSource(googleNewsUrl);
	



     run:
     0
     Download_News2.getAllHistoricalNews
     d_hp null: ajasdgasdg342aw4t4wTWT$#$#$#$Tx: 
     failed to load page. try again in 1 seconds...https://www.google.com/finance/com999999pany_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
     failed 2nd time. try again in 3 seconds...https://www.google.com/finance/com999999pany_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
     failed the 3rd time. giving up https://www.google.com/finance/com999999pany_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
     java.io.FileNotFoundException: https://www.google.com/finance/com999999pany_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
     at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1834)
     at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1439)
     at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:254)
     at stuff.HttpDownloadUtility.getWebInputStream(HttpDownloadUtility.java:119)
     at stuff.HttpDownloadUtility.getWebInputStream(HttpDownloadUtility.java:89)
     at stuff.HttpDownloadUtility.getPageSource(HttpDownloadUtility.java:132)
     at stuff.downloaders.UdFlow_News_Google.downloadGoogleNews(UdFlow_News_Google.java:613)
     at stuff.downloaders.UdFlow_News_Google.go(UdFlow_News_Google.java:69)
     at stuff.downloaders.UdFlow_News_Google.main(UdFlow_News_Google.java:50)
     BUILD SUCCESSFUL (total time: 5 seconds)

    
    
    
     ------------------------------

     blocked around 1:40 pm

     i
     1786 89639 true true
     1787 89639 true true
     1788 89639 true true
     1789 89639 true true
     1790 89639 true true
     1791 89639 true true
     1792 89639 true true
     1793 89639 true true
     1794 89639 true true
     1795 89639 true true
     1796 89639 true true
     1797 89639 true true
     1798 89397 true true
     failed to load page. try again in 1 seconds...https://www.google.com/finance/company_news?q=ajx&start=0&num=10000
     failed 2nd time. try again in 3 seconds...https://www.google.com/finance/company_news?q=ajx&start=0&num=10000
     failed the 3rd time. giving up https://www.google.com/finance/company_news?q=ajx&start=0&num=10000
     java.io.IOException: Server returned HTTP response code: 503 for URL: https://ipv4.google.com/sorry/IndexRedirect?continue=https://www.google.com/finance/company_news%3Fq%3Dajx%26start%3D0%26num%3D10000&q=CGMSBMZWHRMYrfuBqAUiGQDxp4NL7D8YzDrouJzwCR4Lr1D6CwG_5Uk
     at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1838)
     at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1439)
     at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:254)
     at stuff.HttpDownloadUtility.getWebInputStream(HttpDownloadUtility.java:119)
     at stuff.HttpDownloadUtility.getWebInputStream(HttpDownloadUtility.java:89)
     at stuff.HttpDownloadUtility.getPageSource(HttpDownloadUtility.java:132)
     at stuff.downloaders.UdFlow_News_Google.getGoogleNewsPageSource(UdFlow_News_Google.java:594)
     at stuff.Test.main(Test.java:19)
     BUILD SUCCESSFUL (total time: 16 minutes 45 seconds)
    
     ------------------------

     this is wireless turned off:
    
     Download_News2.getAllHistoricalNews
     d_hp null: ajasdgasdg342aw4t4wTWT$#$#$#$Tx: 
     failed to load page. try again in 1 seconds...https://www.google.com/finance/company_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
     failed 2nd time. try again in 3 seconds...https://www.google.com/finance/company_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
     failed the 3rd time. giving up https://www.google.com/finance/company_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
     java.net.UnknownHostException: www.google.com
     at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:184)
     at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:172)
     at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)
     at java.net.Socket.connect(Socket.java:589)
     at sun.security.ssl.SSLSocketImpl.connect(SSLSocketImpl.java:649)
     at sun.security.ssl.BaseSSLSocketImpl.connect(BaseSSLSocketImpl.java:173)
     at sun.net.NetworkClient.doConnect(NetworkClient.java:180)
    
     -------
     SOOOOOOOO we are looking for IOException with message:
     Server returned HTTP response code: 503
    
    
     */


}
