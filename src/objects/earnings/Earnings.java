package objects.earnings;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utilities.G;
import static utilities.G.sdf_militaryTime;

public class Earnings implements Comparable {

    public static void writeListToDisk(File file, List<Earnings> earningsInfos) throws IOException {
	Files.write(file.toPath(), Earnings.makeOutputStrings(earningsInfos), StandardCharsets.UTF_8);
    }

    public static void removeDatesGtE_to_date(List<Earnings> earningsInfos, String startingDate) {

	for (Iterator<Earnings> iter = earningsInfos.iterator(); iter.hasNext();) {
	    Earnings e = iter.next();

	    if (e.date.compareTo(startingDate) >= 0)
		iter.remove();
	}


    }


    public String ticker = "";
    String name = "";
    public String date = "";

    /** military time, like news time.  */
    String time = "";

    Boolean timeIsEstimatedFromGenericBeforeOrAfter = false;

    String zacks_estimate = "";
    String zacks_reported = "";

    String estimize_ticker = "";
    String estimize_name = "";
    String estimize_period = "";
    String estimize_frequency = "";
    String estimize_popularity = "";
    String estimize_value_wallst = "";
    String estimize_value_estimize = "";
    String estimize_value_actual = "";
    String estimize_pctCh_wallst = "";
    String estimize_pctCh_estimize = "";
    String estimize_pctCh_actual = "";


    String estimize_eps_wallst = "";
    String estimize_eps_estimize = "";
    String estimize_eps_actual = "";
    String estimize_rev_wallst = "";
    String estimize_rev_estimize = "";
    String estimize_rev_actual = "";


    private final String defaultBeforeOpenTime = "01:19";
    private final String defaultAfterCloseTime = "23:19";
    private boolean estimize_isEconomicIndicator = false;

    private boolean isPreexistingRecord = false;

    private String key;

    public String getKey() {
	return key;
    }

    public String setKey() {
	return key = date + ticker;
    }

    /** from yahoo calendar */
    public Earnings(String date, String ticker, String yahooTimeStamp) throws ParseException {

	this.date = date;
	this.ticker = ticker;
	this.timeIsEstimatedFromGenericBeforeOrAfter = false;

	if (yahooTimeStamp.toLowerCase().contains("after market close")) {
	    this.time = defaultAfterCloseTime;
	    this.timeIsEstimatedFromGenericBeforeOrAfter = true;
	} else if (yahooTimeStamp.toLowerCase().contains("before market open")) {
	    this.time = defaultBeforeOpenTime;
	    this.timeIsEstimatedFromGenericBeforeOrAfter = true;
	} else if (yahooTimeStamp.toLowerCase().contains("time not supplied"))
	    this.time = "";
	else {
	    this.time = convertYahooEarningsCalTimestampToMilitaryTime(yahooTimeStamp);
	}
    }

    /** from zacks */
    public Earnings(String date, NodeList td) {
	timeIsEstimatedFromGenericBeforeOrAfter = true;
	/*
	    
	 BMRA * Biomerica Inc * -- * $0.00 * -$0.03 * -- * 9.62% * 
	 BPFH * Boston Priv Fin * After Close * $0.22 * $0.14 * -0.08 (36.36%) * 0.88% * 
	 */

	this.date = date;

	this.ticker = G.cleanUpHtmlSyntax(td.item(0).getTextContent());
	this.name = G.cleanUpHtmlSyntax(td.item(1).getTextContent());
	String zacksTime = td.item(2).getTextContent();
	this.zacks_estimate = td.item(3).getTextContent().replace("$", "");
	this.zacks_reported = td.item(4).getTextContent().replace("$", "");

	if (this.zacks_estimate.contains("--"))
	    this.zacks_estimate = "";
	if (this.zacks_reported.contains("--"))
	    this.zacks_reported = "";
	if (zacksTime.contains("--"))
	    this.time = "";
	if (zacksTime.contains("Before Open"))
	    this.time = defaultBeforeOpenTime;
	if (zacksTime.contains("After Close"))
	    this.time = defaultAfterCloseTime;

    }

    /** for estimize */
    public Earnings(String date, Node earningsDiv) throws XPathExpressionException, ParseException {

	XPath xPath = XPathFactory.newInstance().newXPath();

	String boldInstrumentText = G.cleanUpHtmlSyntax((String)xPath.evaluate("./div[@class='td instrument']/a/strong/text()", earningsDiv, XPathConstants.STRING)).replaceAll(",", "");
	String boldInstrumentHref = G.cleanUpHtmlSyntax((String)xPath.evaluate("./div[@class='td instrument']/a/@href", earningsDiv, XPathConstants.STRING)).replaceAll(",", "");
	String greyInstrumentText = G.cleanUpHtmlSyntax((String)xPath.evaluate("./div[@class='td instrument']/a[@class='secondary']/text()", earningsDiv, XPathConstants.STRING)).replaceAll(",", "");

	String name_, ticker_;

	if (boldInstrumentHref.contains("economic_indicators")) {
	    ticker_ = boldInstrumentText;
	    name_ = "";
	} else {
	    ticker_ = boldInstrumentText;
	    name_ = greyInstrumentText;
	}

	String period = G.cleanUpHtmlSyntax((String)xPath.evaluate("./div[@class='td release']/text()", earningsDiv, XPathConstants.STRING)).trim();
	String frequency = (String)xPath.evaluate("./div[@class='td release']/span[@class='secondary']/text()", earningsDiv, XPathConstants.STRING);

	//convert this from "8:30 AM" or "BMO" or "AMC" to military time
	String estzTime = ((String)xPath.evaluate("./div[@class='td reports']/text()", earningsDiv, XPathConstants.STRING)).trim();

	if (estzTime.contains(" AM") || estzTime.contains(" PM")) {

	    this.timeIsEstimatedFromGenericBeforeOrAfter = false;
	    SimpleDateFormat estimizesdf = new SimpleDateFormat("hh:mm a");
	    Date d = estimizesdf.parse(estzTime);
	    estzTime = sdf_militaryTime.format(d);

	} else if (estzTime.equals("AMC")) {
	    this.timeIsEstimatedFromGenericBeforeOrAfter = true;
	    estzTime = defaultAfterCloseTime;
	} else if (estzTime.equals("BMO")) {
	    this.timeIsEstimatedFromGenericBeforeOrAfter = true;
	    estzTime = defaultBeforeOpenTime;
	}

	String popularity = ((String)xPath.evaluate("./div[@class='td popularity']/div/@class", earningsDiv, XPathConstants.STRING))
		.replace("popularity_bar pop_", "");

	//VALUE: or VALUE (B): -- B or M or K -- or EPS:
	String datapoint = (String)xPath.evaluate("./div[@class='td datapoint']/strong[1]", earningsDiv, XPathConstants.STRING);

	String wallSt1 = ((String)xPath.evaluate("./div[@class='td wall-street']/a[1]/text()", earningsDiv, XPathConstants.STRING)).replaceAll(",", "");
	String wallSt2 = ((String)xPath.evaluate("./div[@class='td wall-street']/a[2]/text()", earningsDiv, XPathConstants.STRING)).replaceAll(",", "");

	String estimize1 = ((String)xPath.evaluate("./div[@class='td estimize']/a[1]/text()", earningsDiv, XPathConstants.STRING)).replaceAll(",", "");
	String estimize2 = ((String)xPath.evaluate("./div[@class='td estimize']/a[2]/text()", earningsDiv, XPathConstants.STRING)).replaceAll(",", "");

	String actuals1 = ((String)xPath.evaluate("./div[@class='td actuals']/a[1]/text()", earningsDiv, XPathConstants.STRING)).replaceAll(",", "");
	String actuals2 = ((String)xPath.evaluate("./div[@class='td actuals']/a[2]/text()", earningsDiv, XPathConstants.STRING)).replaceAll(",", "");


	if (boldInstrumentHref.contains("economic_indicators")) {
	    wallSt2 = wallSt2.replace("%", "");
	    estimize2 = estimize2.replace("%", "");
	    actuals2 = actuals2.replace("%", "");

	    long multiplier = 1;

	    if (datapoint.contains("(K)"))
		multiplier = 1_000;
	    if (datapoint.contains("(M)"))
		multiplier = 1_000_000;
	    if (datapoint.contains("(B)"))
		multiplier = 1_000_000_000;

	    if (multiplier > 1) {
		if (!wallSt1.isEmpty()) wallSt1 = String.format("%.0f", Double.parseDouble(wallSt1) * multiplier);
		if (!estimize1.isEmpty()) estimize1 = String.format("%.0f", Double.parseDouble(estimize1) * multiplier);
		if (!actuals1.isEmpty()) actuals1 = String.format("%.0f", Double.parseDouble(actuals1) * multiplier);
	    }
	}

//	System.out.println(""
//		+ "date: " + date + "\n "
//		+ "name: " + name_ + "\n "
//		+ "ticker: " + ticker_ + "\n "
//		+ "period: " + period + "\n "
//		+ "frequency: " + frequency + "\n "
//		+ "estzTime: " + estzTime + "\n "
//		+ "popularityClass: " + popularity + "\n "
//		+ "datapoint: " + datapoint + "\n "
//		+ "wallSt1: " + wallSt1 + "\n "
//		+ "wallSt2: " + wallSt2 + "\n "
//		+ "estimize1: " + estimize1 + "\n "
//		+ "estimize2: " + estimize2 + "\n "
//		+ "actuals1: " + actuals1 + "\n "
//		+ "actuals2: " + actuals2 + "\n------------------\n "
//	);

	this.ticker = ticker_;
	this.name = name_;
	this.date = date;
	this.time = estzTime;	    //todo is this right?  it used to be time = time w/ warning.  so i changed this.time to accept estzTime.  w/out testing

	this.estimize_period = period;
	this.estimize_frequency = frequency;
	this.estimize_popularity = popularity;

	if (boldInstrumentHref.contains("economic_indicators")) {

	    this.estimize_isEconomicIndicator = true;

	    this.estimize_value_wallst = wallSt1;
	    this.estimize_value_estimize = estimize1;
	    this.estimize_value_actual = actuals1;
	    this.estimize_pctCh_wallst = wallSt2;
	    this.estimize_pctCh_estimize = estimize2;
	    this.estimize_pctCh_actual = actuals2;

	} else {

	    this.estimize_isEconomicIndicator = false;	//redundant. false by default.

	    this.estimize_eps_wallst = wallSt1;
	    this.estimize_eps_estimize = estimize1;
	    this.estimize_eps_actual = actuals1;
	    this.estimize_rev_wallst = wallSt2;
	    this.estimize_rev_estimize = estimize2;
	    this.estimize_rev_actual = actuals2;
	}
    }


    public String outputLine() {
	return ""
		+ ticker + G.earningsDelim
		+ name + G.earningsDelim
		+ date + G.earningsDelim
		+ time + G.earningsDelim
		+ timeIsEstimatedFromGenericBeforeOrAfter + G.earningsDelim
		+ zacks_estimate + G.earningsDelim
		+ zacks_reported + G.earningsDelim
		+ estimize_isEconomicIndicator + G.earningsDelim
		+ estimize_period + G.earningsDelim
		+ estimize_frequency + G.earningsDelim
		+ estimize_popularity + G.earningsDelim
		+ estimize_value_wallst + G.earningsDelim
		+ estimize_value_estimize + G.earningsDelim
		+ estimize_value_actual + G.earningsDelim
		+ estimize_pctCh_wallst + G.earningsDelim
		+ estimize_pctCh_estimize + G.earningsDelim
		+ estimize_pctCh_actual + G.earningsDelim
		+ estimize_eps_wallst + G.earningsDelim
		+ estimize_eps_estimize + G.earningsDelim
		+ estimize_eps_actual + G.earningsDelim
		+ estimize_rev_wallst + G.earningsDelim
		+ estimize_rev_estimize + G.earningsDelim
		+ estimize_rev_actual;
    }


    public Earnings(String outputFileLine) {
	String[] ar = outputFileLine.split("\\" + G.earningsDelim, -1);
	
//	System.out.println(ar.length);
//	
//	System.out.println(Arrays.asList(ar));

	try {

	    ticker = ar[0].toUpperCase();
	    name = ar[1];
	    date = ar[2];
	    time = ar[3];
	    timeIsEstimatedFromGenericBeforeOrAfter = Boolean.parseBoolean(ar[4]);
	    zacks_estimate = ar[5];
	    zacks_reported = ar[6];

	    estimize_isEconomicIndicator = Boolean.parseBoolean(ar[7]);

	    estimize_period = ar[8];
	    estimize_frequency = ar[9];
	    estimize_popularity = ar[10];
	    estimize_value_wallst = ar[11];
	    estimize_value_estimize = ar[12];
	    estimize_value_actual = ar[13];
	    estimize_pctCh_wallst = ar[14];
	    estimize_pctCh_estimize = ar[15];
	    estimize_pctCh_actual = ar[16];
	    estimize_eps_wallst = ar[17];
	    estimize_eps_estimize = ar[18];
	    estimize_eps_actual = ar[19];
	    estimize_rev_wallst = ar[20];
	    estimize_rev_estimize = ar[21];
	    estimize_rev_actual = ar[22];
	} catch (Exception e) {
	}

	isPreexistingRecord = true;
    }


    /** reads all files (yahoo vs. zacks vs. estimize) the same way */
    public static List<Earnings> readFile(int minDate, File file) throws IOException {

	String minDateStr = minDate + "";

	List<Earnings> earningsInfos = new ArrayList();

	if (file.exists()) {
	    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
	    for (String line : lines) {
		Earnings e = new Earnings(line);
		if (e.date.compareTo(minDateStr) < 0)
		    break;
		else
		    earningsInfos.add(e);
	    }
	}

	return earningsInfos;
    }

    /** reads all files (yahoo vs. zacks vs. estimize) the same way */
    public static List<Earnings> readFile(File file) throws IOException {
	return readFile(0, file);
    }

    public static List<Earnings> readFileSome(File file, int numLines) throws IOException {

	List<Earnings> earningsInfos = new ArrayList();

	int count = 0;
	if (file.exists()) {
	    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
	    for (String line : lines) {
		if (count++ > numLines)
		    break;
		earningsInfos.add(new Earnings(line));
	    }
	}

	return earningsInfos;
    }

    public static String convertYahooEarningsCalTimestampToMilitaryTime(String yahooTimeStamp) throws ParseException {

	yahooTimeStamp = yahooTimeStamp.replace("am", "AM");
	yahooTimeStamp = yahooTimeStamp.replace("pm", "PM");
	yahooTimeStamp = yahooTimeStamp.replace("ET", "EST");

	SimpleDateFormat sdf_yahoo_earnings = new SimpleDateFormat("hh:mm a zz");

	return sdf_militaryTime.format(sdf_yahoo_earnings.parse(yahooTimeStamp));
    }

    @Override
    public int compareTo(Object o) {		    //sorted increasing
	return date.compareTo(((Earnings)o).date);
    }


    /** this is complicated since usually names don't matter, except for US data, where the name is all we have.  maybe i should just call those "ticker".... */
    public boolean matchesTickerAndDate(Earnings other) {
	String ticker1 = this.ticker.replace(".", "-").toLowerCase();
	String ticker2 = other.ticker.replace(".", "-").toLowerCase();

	return ticker1.equals(ticker2) && this.date.equals(other.date);


    }

    /** be sure not to modify the file after download for yahoo file.  that will screw up the downloading system.  it assumes the last modified date is the last time it scraped */
    public static String getEarningsCalendarMostRecentRecordedDateOnDisk(List<Earnings> earningsInfos, File earningsInfosFile, String defaultStartDate) { //redundant since if it's not yahoo, then the file will be null.
	//it doens't work to use the most recent date with earnings data as the starting date.  cos zacks sucks and sometimes has earnings data listed for future releases
	try {

//	    for (Earnings e : earningsInfos)
//		System.out.println("awe4fawef date: " + e.date);

	    Earnings ei = earningsInfos.get(0);
	    String latestDateInFile = ei.date;
	    System.out.println("top date: " + latestDateInFile);
	    String earningsInfoFileLastModifiedDate = G.sdf_date.format(earningsInfosFile.lastModified());
	    System.out.println("file last modified date: " + earningsInfoFileLastModifiedDate);
	    String startingDate = G.minString(latestDateInFile, earningsInfoFileLastModifiedDate);  //starts w/ last modified date if there's data after that (would have been recorded before the actual stock release so maybe it got updated afterwards)

	    System.out.println("now, starting date: " + startingDate);

	    if (startingDate.isEmpty()) startingDate = defaultStartDate;

	    System.out.println("using starting date, newer " + startingDate);
	    return startingDate;
	} catch (Exception e) {
	    System.out.println("no valid recorded earnings info found.  using " + defaultStartDate);
	    return defaultStartDate;
	}
    }

    public static String getEarningsCalendarEarliestRecordedDateOnDisk(List<Earnings> earningsInfos, File earningsInfosFile, String defaultStartDate) {
	try {
	    Earnings ei = earningsInfos.get(earningsInfos.size() - 1);
	    String earliestDateInFile = ei.date;
	    String earningsInfoFileEarliestModifiedDate = G.sdf_date.format(earningsInfosFile.lastModified());
	    String startingDate = G.minString(earliestDateInFile, earningsInfoFileEarliestModifiedDate);  //starts w/ last modified date if there's data after that (would have been recorded before the actual stock release so maybe it got updated afterwards)

	    if (startingDate.isEmpty()) startingDate = defaultStartDate;

	    System.out.println("using starting date, older " + startingDate);
	    return startingDate;
	} catch (Exception e) {
	    System.out.println("no valid recorded earnings info found.  using " + defaultStartDate);
	    return defaultStartDate;
	}
    }

    public static List<String> makeOutputStrings(List<Earnings> earningsInfos) {


//	System.out.println("removing duplicates...");
//	Earnings.removeDuplicatesFromList(earningsInfos);
	System.out.println("sorting by ticker...");
	Collections.sort(earningsInfos, new Comparator<Earnings>() {
	    public int compare(Earnings o1, Earnings o2) {
		return o1.ticker.compareTo(o2.ticker);
	    }
	});
	System.out.println("sorting by date...");
	Collections.sort(earningsInfos, new Comparator<Earnings>() {
	    public int compare(Earnings o1, Earnings o2) {
		return o2.date.compareTo(o1.date);
	    }
	});	//reverse

	List<String> lines = new ArrayList();

	for (Earnings ei : earningsInfos) {
	    lines.add(ei.outputLine());
	}
	return lines;
    }

    private static List<Earnings> readAllEarningsFiles(int minDate, boolean justCombine_returnIsMeaningless) throws IOException, ParseException {
	try {
	    G.initialize();
	    long eLM = G.estimizeEarningsCalendar.lastModified();
	    long zLM = G.zacksEarningsCalendar.lastModified();
	    long yLM = G.yahooEarningsCalendar.lastModified();
	    long cLM = G.combinedEarningsCalendar.lastModified();

	    if (cLM > eLM && cLM > zLM && cLM > yLM) {
		if (!justCombine_returnIsMeaningless) {
		    System.out.println("    reading combined earnings list ...");
		    return Earnings.readFile(minDate, G.combinedEarningsCalendar);
		} else
		    System.out.println("earnings were already combined!");
	    } else {
		System.out.println("    reading estimize ...");
		List<Earnings> estimize = Earnings.readFile(minDate, G.estimizeEarningsCalendar);
		System.out.println("    reading zacks...");
		List<Earnings> zacks = Earnings.readFile(minDate, G.zacksEarningsCalendar);
		System.out.println("    reading yahoo ...");
		List<Earnings> yahoo = Earnings.readFile(minDate, G.yahooEarningsCalendar);

		System.out.println("	    combining ...");
		List<Earnings> earningsInfos = Earnings.combineEarningsInfoListsFrom_Est_Zacks_Yahoo(estimize, zacks, yahoo);

		System.out.println("	    writing ...");
		Earnings.writeListToDisk(G.combinedEarningsCalendar, earningsInfos);
		return earningsInfos;
	    }
	} catch (Exception e) {
	    System.out.println("WTF!?!?!?!?!?!?!");
	    e.printStackTrace();
	    System.exit(0);
	}
	return null;
    }

    public static List<Earnings> readAllEarningsFiles(int minDate) throws IOException, ParseException {
	return readAllEarningsFiles(minDate, false);
    }

    public static List<Earnings> readAllEarningsFiles() throws IOException, ParseException {
	return readAllEarningsFiles(0, false);
    }

    public static List<Earnings> combineAllEarningsFiles() throws IOException, ParseException {
	return readAllEarningsFiles(0, true);
    }


    /** for testing! */
    public static List<Earnings> readSomeEarningsFiles() throws IOException, ParseException {
	G.initialize();
	long eLM = G.estimizeEarningsCalendar.lastModified();
	long zLM = G.zacksEarningsCalendar.lastModified();
	long yLM = G.yahooEarningsCalendar.lastModified();
	long cLM = G.combinedEarningsCalendar.lastModified();

	if (cLM > eLM && cLM > zLM && cLM > yLM) {
	    System.out.println("    reading combined earnings list ...");
	    return Earnings.readFile(G.combinedEarningsCalendar);
	} else {
	    System.out.println("    reading estimize ...");
	    List<Earnings> estimize = Earnings.readFileSome(G.estimizeEarningsCalendar, 30);
	    System.out.println("    reading zacks...");
	    List<Earnings> zacks = Earnings.readFileSome(G.zacksEarningsCalendar, 30);
	    System.out.println("    reading yahoo ...");
	    List<Earnings> yahoo = Earnings.readFileSome(G.yahooEarningsCalendar, 30);

	    System.out.println("	    combining ...");
	    List<Earnings> earningsInfos = Earnings.combineEarningsInfoListsFrom_Est_Zacks_Yahoo(estimize, zacks, yahoo);

	    Earnings.writeListToDisk(G.combinedEarningsCalendar, earningsInfos);
	    return earningsInfos;
	}
    }

    public static void merge_earningsCalendars_onDisk() throws IOException, ParseException {
	List<Earnings> earningsInfos = readAllEarningsFiles();
	Earnings.writeListToDisk(G.combinedEarningsCalendar, earningsInfos);
    }


    private static List<Earnings> combineEarningsInfoListsFrom_Est_Zacks_Yahoo(List<Earnings> estimize, List<Earnings> zacks, List<Earnings> yahoo) {

//	//do duplicates hurt anyone? NO I DOTN" THINK so
//	
//	System.out.println("	       removing duplicates from estimize ...");
//	Earnings.removeDuplicatesFromList(estimize);
//	System.out.println("	       removing duplicates from zacks ...");
//	Earnings.removeDuplicatesFromList(zacks);
//	System.out.println("	       removing duplicates from yahoo ...");
//	Earnings.removeDuplicatesFromList(yahoo);

	Map<String, Earnings> cmap = new TreeMap();

	//loop through each list -- if eis contains this one, add infomation.  otherwise add new element to list

	int count;

	count = 0;
	for (Earnings est_ei : estimize) {
	    count++;
	    if (count % 100 == 0)
		System.out.println("e " + (estimize.size() - count));

	    est_ei.setKey();

	    Earnings ce = cmap.get(est_ei.getKey());

	    if (ce != null) {
		ce.estimize_ticker = est_ei.estimize_ticker;
		ce.estimize_name = est_ei.estimize_name;
		ce.estimize_period = est_ei.estimize_period;
		ce.estimize_frequency = est_ei.estimize_frequency;
		ce.estimize_popularity = est_ei.estimize_popularity;
		ce.estimize_value_wallst = est_ei.estimize_value_wallst;
		ce.estimize_value_estimize = est_ei.estimize_value_estimize;
		ce.estimize_value_actual = est_ei.estimize_value_actual;
		ce.estimize_pctCh_wallst = est_ei.estimize_pctCh_wallst;
		ce.estimize_pctCh_estimize = est_ei.estimize_pctCh_estimize;
		ce.estimize_pctCh_actual = est_ei.estimize_pctCh_actual;
		ce.estimize_eps_wallst = est_ei.estimize_eps_wallst;
		ce.estimize_eps_estimize = est_ei.estimize_eps_estimize;
		ce.estimize_eps_actual = est_ei.estimize_eps_actual;
		ce.estimize_rev_wallst = est_ei.estimize_rev_wallst;
		ce.estimize_rev_estimize = est_ei.estimize_rev_estimize;
		ce.estimize_rev_actual = est_ei.estimize_rev_actual;
		if (ce.time.isEmpty()) {
		    ce.time = est_ei.time;
		    ce.timeIsEstimatedFromGenericBeforeOrAfter = est_ei.timeIsEstimatedFromGenericBeforeOrAfter;
		}
		if (ce.name.isEmpty())
		    ce.name = est_ei.name;
	    } else
		cmap.put(est_ei.getKey(), est_ei);
	}

	count = 0;
	for (Earnings zacks_ei : zacks) {
	    count++;
	    if (count % 100 == 0)
		System.out.println("z " + (zacks.size() - count));

	    zacks_ei.setKey();

	    Earnings ce = cmap.get(zacks_ei.getKey());

	    if (ce != null) {
		ce.zacks_estimate = zacks_ei.zacks_estimate;
		ce.zacks_reported = zacks_ei.zacks_reported;
		if (ce.time.isEmpty()) {
		    ce.time = zacks_ei.time;
		    ce.timeIsEstimatedFromGenericBeforeOrAfter = zacks_ei.timeIsEstimatedFromGenericBeforeOrAfter;
		}
		if (ce.name.isEmpty())
		    ce.name = zacks_ei.name;
	    } else
		cmap.put(zacks_ei.getKey(), zacks_ei);
	}

	count = 0;
	for (Earnings yahoo_ei : yahoo) {
	    count++;
	    if (count % 100 == 0)
		System.out.println("y " + (yahoo.size() - count));

	    yahoo_ei.setKey();

	    Earnings ce = cmap.get(yahoo_ei.getKey());

	    if (ce != null) {
		if (ce.time.isEmpty()) {
		    ce.time = yahoo_ei.time;
		    ce.timeIsEstimatedFromGenericBeforeOrAfter = yahoo_ei.timeIsEstimatedFromGenericBeforeOrAfter;
		}
		if (ce.name.isEmpty())
		    ce.name = yahoo_ei.name;
	    } else
		cmap.put(yahoo_ei.getKey(), yahoo_ei);
	}
	List<Earnings> combined = new ArrayList(cmap.values());

	return combined;
    }

    private static void removeDuplicatesFromList(List<Earnings> earningsInfos) {

	//first remove duplicates -- delete the copy that doesn't have "Reported"
	//start at end - prefer to keep more recently recorded date (if duplicate)
//	List<Integer> indicesToRemove = new ArrayList();
	for (Iterator<Earnings> iter1 = earningsInfos.iterator(); iter1.hasNext();) {
	    Earnings ei1 = iter1.next();

	    boolean doRemoveIter1 = false;
	    for (Iterator<Earnings> iter2 = earningsInfos.iterator(); iter2.hasNext();) {
		Earnings ei2 = iter2.next();

		if (ei1 != ei2 && ei1.matchesTickerAndDate(ei2)) {
		    if (ei1.isPreexistingRecord || (ei1.isPreexistingRecord == ei2.isPreexistingRecord)) {//delete it if it's older regardless of it has data or not
			doRemoveIter1 = true;
			break;
		    }
		}
	    }
	    if (doRemoveIter1) iter1.remove();
	}
    }

    static List<Earnings> getEarningsForTicker(List<Earnings> initList, String ticker) {

	List<Earnings> list = new ArrayList();

	for (Earnings e : initList) {
	    if (e.ticker.equals(ticker))
		list.add(e);
	}
	return list;
    }
}
