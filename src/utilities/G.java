package utilities;

import com.google.common.collect.Lists;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import objects.Symbol;
import objects.people.Person;
import supers.SuperX;

/** global helper */
public class G {

    public static String yVarNamesStr = "futChange_weekPct";
    public static String[] yVarNamesAr;

//    public static int minDate = 20040101;

    public static final String fileDateStampStr = "20150508";
    public static String minDataDownloadYear = "2000";

    ;

    /** this can either be a constant (set for testing, so we don't have to make all data up-to-date all the time, or set to today's actual date for production */

    public static Float parse_nonPrimitive(float x) {
	if (G.isnull(x))
	    return null;
	else return x;
    }

    public static float getMean(List<Float> set) {

	float sum = 0;

	for (Float f : set) {
	    sum += f;
	}
	return sum / set.size();
    }

    /** returns the percentage of the values of SET which are greater than or equal to x */
    public static float getPctGteX(List<Float> set, int x) {

	float count = 0;

	for (Float f : set) {
	    if (f >= x)
		count++;
	}
	return 100 * count / set.size();
    }

    public static final Integer initialize(String printlnText, String[] args, File... dirs) throws IOException, ParseException {
	initialize();
	System.out.println(printlnText);
	for (File file : dirs)
	    file.mkdirs();

	if (args != null)
	    for (String s : args)
		System.out.println(s);
	return G.getFirstArgsInt(args);
    }

    public static final void initialize() throws IOException, ParseException {
	yVarNamesAr = yVarNamesStr.split(",");
	dayDataDir.mkdirs();
	current_year = Integer.parseInt(sdf_year.format(sdf_date.parse(G.currentDate + "")));


//	Do_Screen.writeTemplateFilterFile();	    //TODO put these back!!!!
//	Do_Screen.writeAFilterFile();
	startTime = new Date().getTime();

	System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Java My Libraries\\Selenium\\chromedriver.exe");

    }


    public static final File root = new File("C:" + File.separator + "stocks");

    public static final File permanentDataDir = newChildFile(root, "Permanent Data Archive");
    public static final File dataDaysDir = newChildFile(root, "Daily Data");
    public static final File dummiesAndFailuresDir = newChildFile(root, "Dummies and Failures");
    public static final File manualData = newChildFile(root, "Manual Data");

    public static final File tempX = newChildFile(root, "TempX");
    public static final File tempX_day = newChildFile(tempX, fileDateStampStr);

    public static final File analysis = newChildFile(root, "Analysis");
    public static final File hits = newChildFile(analysis, "hits");
    public static final File portfolios = newChildFile(analysis, "portfolios");

    public static final File finalMegaTable = newChildFile(analysis, "megatable.csv");


    public static final File dayDataDir = newChildFile(dataDaysDir, fileDateStampStr);

    public static final File dummiesAndFailuresDayDir = newChildFile(dummiesAndFailuresDir, fileDateStampStr);
    public static final File dummiesDir = newChildFile(dummiesAndFailuresDayDir, "Success Dummies");
    public static final File failuresDir = newChildFile(dummiesAndFailuresDayDir, "Failures");

    public static final File symbolsDir = newChildFile(dayDataDir, "Symbols");
    public static final File symbolsFile_optionable = newChildFile(symbolsDir, "optionable.csv");
    public static final File symbolsFile_nasdaqs_all = newChildFile(symbolsDir, "nasdaqs_all.csv");
    public static final File symbolsFile_eoddata_US_stocks = newChildFile(symbolsDir, "eoddata_US_stocks.csv");
    public static final File symbolsFile = newChildFile(symbolsDir, "symbols.csv");
    public static final File symbolsInfoFile = newChildFile(symbolsDir, "symbols info.csv");

    public static final File squishedDir = newChildFile(dayDataDir, "Squished");
    public static final File squishedFile = newChildFile(squishedDir, "feb5.csv");

    public static final File weatherDir = newChildFile(dayDataDir, "Weather");
    public static final File weatherIDFile = G.newChildFile(weatherDir, "orderID.txt");

    public static final File nyWeatherFile = newChildFile(weatherDir, "ny.csv");

    public static final File peopleDir = newChildFile(permanentDataDir, "People");
    public static final File peopleCompletedDummyDir = newChildFile(dummiesDir, "People Dummy Completed");
    public static final File peopleFailedLinksDir = newChildFile(failuresDir, "People Failed Links");

    public static final File profileDir = newChildFile(permanentDataDir, "Profile");

    public static final File yahooProfileDir = newChildFile(profileDir, "Yahoo Profile");
    public static final File yahooProfileCompletedDummyDir = newChildFile(dummiesDir, "Yahoo Profile Dummy Completed");
    public static final File yahooProfileFailedLinksDir = newChildFile(failuresDir, "Yahoo Profile Failed Links");


//	this is stupid.  should put related companies and factset categories in the smae file
    public static final File googleRelatedCompaniesDir = newChildFile(profileDir, "Google Related Companies");
    public static final File googleFactsetCategoriesDir = newChildFile(profileDir, "Factset Categories");
    public static final File googleProfileCompletedDummyDir = newChildFile(dummiesDir, "Google Profile Dummy Completed");
    public static final File googleProfileFailedLinksDir = newChildFile(failuresDir, "Google Profile Failed Links");

    public static final File newsDir = newChildFile(permanentDataDir, "News");

    public static final File newsYahooDir = newChildFile(newsDir, "Yahoo News");
    public static final File newsYahooCompletedDummyDir = newChildFile(dummiesDir, "News Yahoo Dummy Completed");
    public static final File newsYahooFailedLinksDir = newChildFile(failuresDir, "Failed Links");

    public static final File newsGoogleDir = newChildFile(newsDir, "Google News");
    public static final File newsGoogleCompletedDummyDir = newChildFile(dummiesDir, "News Google Dummy Completed");
    public static final File newsGoogleFailedLinksDir = newChildFile(failuresDir, "Failed Links");

    public static final File newsCombinedDir = newChildFile(newsDir, "Combined News");

    public static final File secDatesDir = newChildFile(permanentDataDir, "SEC Report Dates");
    public static final File secFailedLinksDir = newChildFile(failuresDir, "Sec Failed Links");
    public static final File secCompletedDummyDir = newChildFile(dummiesDir, "Sec Dummy Completed");

    public static final File splitsDir = newChildFile(permanentDataDir, "Splits");
    public static final File splitsCompletedDummyDir = newChildFile(dummiesDir, "Splits Dummy Completed");
    public static final File splitsFailedLinksDir = newChildFile(failuresDir, "Splits Failed Links");

//    public static final File shortInterestDisseminationDatesFile = newChildFile(manualData, "settlementdate_tab_releasedateAt4Pm.txt");
    public static final File shortInterestDisseminationDatesFile = newChildFile(manualData, "short_interest_dates_2007_through_2015.csv");
    public static final File shortInterestDir = newChildFile(permanentDataDir, "Short Interest");
    public static final File shortInterestCompletedDummyDir = newChildFile(dummiesDir, "Short Interest Dummy Completed");
    public static final File shortInterestFailedLinksDir = newChildFile(failuresDir, "Short Interest Failed Links");

    public static final File msAnnualDir = newChildFile(dayDataDir, "MorningStar Annual Financials");
    public static final File msAnnualCompletedDummyDir = newChildFile(dummiesDir, "MorningStar Annual Financials Dummy Completed");
    public static final File msAnnualFailedLinksDir = newChildFile(failuresDir, "MorningStar Annual Financials Failed Links");

    public static final File msQuarterlyDir = newChildFile(dayDataDir, "MorningStar Quarterly Financials");
    public static final File msQuarterlyCompletedDummyDir = newChildFile(dummiesDir, "MorningStar Quarterly Completed");
    public static final File msQuarterlyFailedLinksDir = newChildFile(failuresDir, "MorningStar Quarterly Failed Links");

    public static final File pricesDailyDir = newChildFile(dayDataDir, "Prices");
    public static final File pricesDailyCompletedDummyDir = newChildFile(dummiesDir, "Prices Completed");
    public static final File pricesDailyFailedLinksDir = newChildFile(failuresDir, "Prices Failed Links");//permanentDataDir

    public static final File pricesMinutelyParentDir = newChildFile(permanentDataDir, "Prices Minutely");
    public static final File pricesMinutelyCompletedDummyDir = newChildFile(dummiesDir, "Prices Minutely Completed");
    public static final File pricesMinutelyFailedLinksDir = newChildFile(failuresDir, "Prices Minutely Failed Links");

    public static final File earningsCalendarsDir = newChildFile(permanentDataDir, "Earnings Calendars");
    public static final File yahooEarningsCalendar = newChildFile(earningsCalendarsDir, "yahoo MANUAL_MODIFY_WILL_SCREW_UP_SCRAPING_SCHEDULE.csv");
    public static final File zacksEarningsCalendar = newChildFile(earningsCalendarsDir, "zacks MANUAL_MODIFY_WILL_SCREW_UP_SCRAPING_SCHEDULE.csv");
    public static final File estimizeEarningsCalendar = newChildFile(earningsCalendarsDir, "estimize MANUAL_MODIFY_WILL_SCREW_UP_SCRAPING_SCHEDULE.csv");
    public static final File combinedEarningsCalendar = newChildFile(earningsCalendarsDir, "combined.csv");


//    TempX --> Financials --> Ticker --> data.csv (date, PE, PB, PFCF, era#), era#.csv
    public static final File XPrices = newChildFile(tempX_day, "Prices");
    public static final File XFinancials = newChildFile(tempX_day, "Financials");
    public static final File XProfile = newChildFile(tempX_day, "Profile");
    public static final File XPeople = newChildFile(tempX_day, "People");
    public static final File XSECReports = newChildFile(tempX_day, "SECReports");
    public static final File XShortInterest = newChildFile(tempX_day, "ShortInterest");
    public static final File XSplits = newChildFile(tempX_day, "Splits");
    public static final File XNews = newChildFile(tempX_day, "News");
    public static final File XEarnings = newChildFile(tempX_day, "Earnings");
    public static final File YCategoryEarningsReponse = newChildFile(tempX_day, "CategoryEarningsReponse");
    public static final File XCateogryComparisons = newChildFile(tempX_day, "CateogryComparisons");


    public static final File XCatAves = newChildFile(tempX_day, "CatAves");

    public static final File XPrices_vars = newChildFile(XPrices, "Prices Vars");
    public static final File XFinancials_vars = newChildFile(XFinancials, "Financials Vars");
    public static final File XProfile_vars = newChildFile(XProfile, "Profile Vars");
    public static final File XPeople_vars = newChildFile(XPeople, "People Vars");
    public static final File XSECReports_vars = newChildFile(XSECReports, "SECReports Vars");
    public static final File XShortInterest_vars = newChildFile(XShortInterest, "ShortInterest Vars");
    public static final File XSplits_vars = newChildFile(XSplits, "Splits Vars");
    public static final File XNews_vars = newChildFile(XNews, "News Vars");
    public static final File XEarnings_vars = newChildFile(XEarnings, "Earnings Vars");
//    public static final File XCategoryEarningsReponse_vars = newChildFile(YCategoryEarningsReponse, "CategoryEarningsReponse Vars");
    public static final File XCateogryComparisons_vars = newChildFile(XCateogryComparisons, "CateogryComparisons Vars");

    public static final File XPrices_eras = newChildFile(XPrices, "Prices Eras");
    public static final File XFinancials_eras = newChildFile(XFinancials, "Financials Eras");
    public static final File XProfile_eras = newChildFile(XProfile, "Profile Eras");
    public static final File XPeople_eras = newChildFile(XPeople, "People Eras");
    public static final File XSECReports_eras = newChildFile(XSECReports, "SECReports Eras");
    public static final File XShorrtInterest_eras = newChildFile(XShortInterest, "ShortInterest Eras");
    public static final File XSplits_eras = newChildFile(XSplits, "Splits Eras");
    public static final File XNews_eras = newChildFile(XNews, "News Eras");
    public static final File XEarnings_eras = newChildFile(XEarnings, "Earnings Eras");
//    public static final File XCategoryEarningsReponse_eras = newChildFile(YCategoryEarningsReponse, "CategoryEarningsReponse Eras");
    public static final File XCateogryComparisons_eras = newChildFile(XCateogryComparisons, "CateogryComparisons Eras");

    public static File getCategoryEarningsResponseFile(String categoryFullName) throws IOException {
	return newChildTickerFile(YCategoryEarningsReponse, categoryFullName);
    }

    public static final File filter_file = newChildFile(analysis, "filter.pl");
    public static final File filter_templatefile = newChildFile(analysis, "filterTemplate.pl");

    static long startTime;
    public static String ls = System.lineSeparator();

    public static String tickerFileNameSuffix = "__.csv";
    public static String tickerDirNameSuffix = "__";

    public static String minimumTicker = "A";
    public static String maximumTicker = "ZZZZ";
    public static int minimumDateInt = 19900101;

    public static SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMMdd");
    public static DecimalFormat df = new DecimalFormat("#.##");
    public static DecimalFormat df_two0s = new DecimalFormat("0.00");//df_one0
    public static DecimalFormat df_one0 = new DecimalFormat("0.0");//df_one0
    public static DecimalFormat sciDf = new DecimalFormat("0.#E0");
    public static SimpleDateFormat sdf_year = new SimpleDateFormat("yyyy");
    public static SimpleDateFormat sdf_militaryTime = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat sdf_full = new SimpleDateFormat("yyyyMMdd:HH:mm:ss");
    public static final SimpleDateFormat sdf_full_forFileName = new SimpleDateFormat("yyyyMMdd HH mm ss");

    public static int currentDate = G.currentDate_int_actual(); //20150115;//
    public static Integer current_year;

    public static File averagesDir;
    public static File correlationsDir;

    // don't use custom nulls in raw data!!!!  or permanent data!! so i can change them if i want to.

    public static final float null_double = -99;	//this CANNOT BE NaN because NaN != NaN !!!!!!!!!!!!!!!!!
    public static final float null_float = -99;	//this CANNOT BE NaN because NaN != NaN !!!!!!!!!!!!!!!!!
    public static final short null_short = -99;
    public static final byte null_byte = -99;
    public static final int null_int = -99;
    public static final long null_long = -99;
    public static final String null_String = null;
    public static final boolean null_boolean = false;


    public static float dummyNonNull_float = 1000000000;


    public static String tickerKey = "THISISWHERETHETICKERGOES";
    public static final int SECTOR = 0;
    public static final int INDUSTRY = 1;

    public static final int SUM = 0;
    public static final int COUNT = 1;
    public static final int AVE = 0;

    /** cedilla mark ¸ */
    public static final String cedilla = "¸";
    public static final String circumflex = "ˆ";
    public static final String tab = "\t";
    public static final char tabChar = '\t';
    public static final String macron = "¯";

    /**not file-safe */
    public static final String colon = ":";
    public static final String semicolon = ";";
    public static final String comma = ",";
    public static final String space = " ";
    public static final String spacedhash = " # ";

    public static final String eraDelim = tab;

    public static final String edrDelim = macron;
    public static final String edrSubDelim = "#";

//    public static final String varSuperDelim = cedilla;
    public static final char varDelim = tabChar;
    public static final String varSubDelim = colon;

    public static final String earningsDelim = "^";

    public static String megaTableDelim = G.tab;

    public static String SecsStringDelimiter = ";  ";


    private static long webdriver_wait_millis = 20 * 1000;

    /* managers */
    public static File getPeopleFile(String tick) throws IOException {
	return newChildTickerFile(peopleDir, tick);
    }

    public static File getPeopleUpdatedDummyFile(String tick) throws IOException {
	return newChildTickerFile(peopleCompletedDummyDir, tick);
    }

    public static File getPeopleFailedLinksFile(String tick) throws IOException {
	return newChildTickerFile(peopleFailedLinksDir, tick);
    }

    /*				     Profile			    	     */

    /*	YahooProfile    */
    public static File getYahooProfileFile(String tick) throws IOException {
	return newChildTickerFile(yahooProfileDir, tick);
    }

    public static File getYahooProfileUpdatedDummyFile(String tick) throws IOException {
	return newChildTickerFile(yahooProfileCompletedDummyDir, tick);
    }

    public static File getYahooProfileFailedLinksFile(String tick) throws IOException {
	return newChildTickerFile(yahooProfileFailedLinksDir, tick);
    }

    /* Google Profile    */
    public static File getGoogleRelatedCompaniesFile(String ticker) throws IOException {
	return newChildTickerFile(G.googleRelatedCompaniesDir, ticker);
    }

    public static File getGoogleFactsetCategoriesFile(String ticker) throws IOException {
	return newChildTickerFile(G.googleFactsetCategoriesDir, ticker);
    }

    public static File getGoogleProfileUpdatedDummyFile(String tick) throws IOException {
	return newChildTickerFile(googleProfileCompletedDummyDir, tick);
    }

    public static File getGProfileFailedLinksFile(String tick) throws IOException {
	return newChildTickerFile(googleProfileFailedLinksDir, tick);
    }

    /* news */

    public static File getNewsYahooFile(String tick) throws IOException {
	return newChildTickerFile(newsYahooDir, tick);
    }

    public static File getNewsYahooUpdatedDummyFile(String tick) throws IOException {
	return newChildTickerFile(newsYahooCompletedDummyDir, tick);
    }

    public static File getNewsYahooFailedLinksFile(String tick) throws IOException {
	return newChildTickerFile(newsYahooFailedLinksDir, tick);
    }

    public static File getNewsGoogleFile(String tick) throws IOException {
	return newChildTickerFile(newsGoogleDir, tick);
    }

    public static File getNewsGoogleUpdatedDummyFile(String tick) throws IOException {
	return newChildTickerFile(newsGoogleCompletedDummyDir, tick);
    }

    public static File getNewsGoogleFailedLinksFile(String tick) throws IOException {
	return newChildTickerFile(newsGoogleFailedLinksDir, tick);
    }

    public static File getNewsCombinedFile(String tick) throws IOException {
	return newChildTickerFile(newsCombinedDir, tick);
    }

    /* sec reports */
    public static File getSecFailedLinksFile(String tick) throws IOException {
	return newChildTickerFile(secFailedLinksDir, tick);
    }

    public static File getSecUpdatedDummyFile(String tick) throws IOException {
	return newChildTickerFile(secCompletedDummyDir, tick);
    }

    public static File getSecDatesFile(String tick) throws IOException {
	return newChildTickerFile(secDatesDir, tick);
    }

    /* splits  */
    public static File getSplitsFile(String tick) throws IOException {
	return newChildTickerFile(splitsDir, tick);
    }

    public static File getSplitsUpdatedDummyFile(String tick) throws IOException {
	return newChildTickerFile(splitsCompletedDummyDir, tick);
    }

    public static File getSplitsFailedLinksFile(String tick) throws IOException {
	return newChildTickerFile(splitsFailedLinksDir, tick);
    }

    /* short interest  */
    public static File getShortInterestFile(String tick) {
	return newChildTickerFile(shortInterestDir, tick);
    }

    public static File getShortInterestUpdatedDummyFile(String tick) {
	return newChildTickerFile(shortInterestCompletedDummyDir, tick);
    }

    public static File getShortInterestFailedLinksFile(String tick) {
	return newChildTickerFile(shortInterestFailedLinksDir, tick);
    }

    /*				prices                                      */
    /* daily */
    public static File getPricesDailyFile(String tick) {
	return newChildTickerFile(pricesDailyDir, tick);
    }

    public static File getPricesDailyUpdatedDummyFile(String tick) {
	return newChildTickerFile(pricesDailyCompletedDummyDir, tick);
    }

    public static File getPricesDailyFailedLinksFile(String tick) {
	return newChildTickerFile(pricesDailyFailedLinksDir, tick);
    }

    /* minutely */
    public static File getPricesMinutelyTickerDir(String tick) {
	return newChildTickerDir(pricesMinutelyParentDir, tick);
    }

    public static File getPricesMinutelyFile(String tick) {
	return newChildTickerFile(getPricesMinutelyTickerDir(tick), currentDateTime_St_actual());
    }

    public static File getPricesMinutelyUpdatedDummyFile(String tick) {
	return newChildTickerFile(pricesMinutelyCompletedDummyDir, tick);
    }

    public static File getPricesMinutelyFailedLinksFile(String tick) {
	return newChildTickerFile(pricesMinutelyFailedLinksDir, tick);
    }


    /* financials */
    public static File getMsAnnualFile(String tick) {
	return newChildTickerFile(msAnnualDir, tick);
    }

    public static File getMsAnnualUpdatedDummyFile(String tick) {
	return newChildTickerFile(msAnnualCompletedDummyDir, tick);
    }

    public static File getMsAnnualFailedLinksFile(String tick) {
	return newChildTickerFile(msAnnualFailedLinksDir, tick);
    }

    public static File getMsQuarterlyFile(String tick) {
	return newChildTickerFile(msQuarterlyDir, tick);
    }

    public static File getMsQuarterlyUpdatedDummyFile(String tick) {
	return newChildTickerFile(msQuarterlyCompletedDummyDir, tick);
    }

    public static File getMsQuarterlyFailedLinksFile(String tick) {
	return newChildTickerFile(msQuarterlyFailedLinksDir, tick);
    }

    public static File getResultsHitsFile(Class yVarClass, String descriptorString) {
	return newChildTickerFile(hits, G.getClassShortName(yVarClass) + " " + descriptorString);
    }

    public static File getResultsPortfolioFile(Class yVarClass, String descriptorString) {
	return newChildTickerFile(portfolios, G.getClassShortName(yVarClass) + " " + descriptorString);
    }

    public static File getResultsPortfolioInvalidInvestmentsFile(Class yVarClass, String descriptorString) {
	return newChildTickerFile(portfolios, "INVALID " + G.getClassShortName(yVarClass) + " " + descriptorString);
    }

    public static String getTickFromFile(File tickerFile) {
	return fileNameWithoutSuffix(tickerFile);
    }

    public static String fileNameWithoutSuffix(File tickerFile) {
	return tickerFile.getName().replaceAll("\\^", "").replaceAll(tickerFileNameSuffix, "");
    }

    public static File newChildFile(File root, String childFileName) {
	try {
	    return new File(root.getCanonicalPath() + File.separator + childFileName);
	} catch (IOException ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
	return null;
    }

    public static File newChildTickerFile(File root, String ticker) {
	return newChildFile(root, ticker.toUpperCase() + tickerFileNameSuffix);
    }

    public static File newChildTickerDir(File root, String ticker) {
	return newChildFile(root, ticker.toUpperCase() + tickerDirNameSuffix);
    }


    public static long parse_long(String st) {
	try {
	    return Long.parseLong(st);
	} catch (Exception e) {
	    return null_long;
	}
    }

    public static Long parse_LLong(String st) {
	Long x;
	try {
	    x = Long.parseLong(st);
	} catch (Exception e) {
	    x = null;
	}
	return x;
    }

    public static Double parse_Double(String st) {
	Double x;
	try {
	    x = Double.parseDouble(st);
	} catch (Exception e) {
	    x = null;
	}
	return x;
    }

    public static int parse_int(String st) {
	try {
	    return Integer.parseInt(st.replace(",", "").trim());
	} catch (Exception e) {
	    return null_int;
	}
    }

    public static Integer parse_Integer(String st) {
	try {
	    return Integer.parseInt(st.trim());
	} catch (Exception e) {
	    return null;
	}
    }

    public static int toPrimitive(Integer x) {
	return (x == null ? null_int : x);
    }

    public static float toPrimitive(Float x) {
	return x == null ? null_float : x;
    }

    public static Integer toNonPrimitive(int x) {
	return x == null_int ? null : x;
    }

    /** fix this!!!! we shouldn't check for B, M etc for EVERY FLOAT!!! :( so wasteful are these in estimize???*/
    public static float parse_float(String st) {

	try {
	    st = st.replace(",", "").trim();
	    //handles these strings:
	    //9.92B
	    //1.27%
	    int factor = 1;
	    try {
		char lastLetter = st.charAt(st.length() - 1);
		switch (lastLetter) {
		    case 'B':
			factor = 1_000_000_000;
			st = st.substring(0, st.length() - 1);
			System.out.println("found awful B float usage!");
			throw new Exception();
//			break;
		    case 'M':
			factor = 1_000_000;
			st = st.substring(0, st.length() - 1);

			System.out.println("found awful M float usage!");
			throw new Exception();
//			break;	
		    case 'k':
			factor = 1_000;
			st = st.substring(0, st.length() - 1);
			System.out.println("found awful k float usage!");
			throw new Exception();
//			break;
		    case '%':
			st = st.substring(0, st.length() - 1);
			System.out.println("found awful % float usage!");
			throw new Exception();
//			break;
		}
	    } catch (Exception e) {
	    }
	    return Float.parseFloat(st) * factor;
	} catch (Exception e) {
	    return null_float;
	}
    }


    public static byte parse_byte(boolean bool) {
	return (byte)(bool ? 1 : 0);
    }

    public static byte parse_byte(String st) {
	byte x;
	try {
	    x = Byte.parseByte(st);
	} catch (Exception e) {
	    x = null_byte;
	}
	return x;
    }

    public static short parse_short(String st) {
	try {
	    return Short.parseShort(st);
	} catch (Exception e) {
	    return null_short;
	}
    }

    public static boolean parse_boolean(String st) {
	if (st != null)
	    return st.equals("1") || st.equals("true");
	return false;
    }

    public static String parse_Str(CharSequence text) {
	return parse_Str(text.toString());
    }

    public static String parse_Str(List list, String delimiter) {
	if (list == null || list.isEmpty())
	    return "";
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < list.size(); i++) {
	    Object o = list.get(i);
	    if (i > 0)
		sb.append(delimiter);
	    sb.append(o.toString());
	}
	return sb.toString();
    }

    /** fix this!!! don't waste everyone's time replacing html crap!!!!!!!!!!!!!!!!!!!!!!!!! */
    public static String parse_Str(String text) {
	return text == null ? "" : text.trim().replace("&amp;", "&").replace("N/A", "");    //this sucks.  i shouldn't have put html stuff in here.  slows everything down
    }

    public static String cleanNAs(String text) {
	text = text.trim();
	if (text.equals("N/A") || text.equals("NaN"))
	    return "";
	return text;
    }

    public static String cleanNAs(CharSequence text) {
	return cleanNAs(text.toString());
    }

    public static String parse_Str_fast(String text) {
	return text == null ? "" : text;    //this sucks.  i shouldn't have put html stuff in here.  slows everything down
    }

    public static String parse_Str(float x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return df.format(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str_two0s(float x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return df_two0s.format(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str_two0s(double x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return df_two0s.format(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str_one0(float x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return df_one0.format(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str(Double x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return df.format(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str_sciDf(float x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return sciDf.format(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str_sciDf(int x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return sciDf.format(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str(int x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return Integer.toString(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str(byte x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return Integer.toString(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str(short x) {
	if (!isnull(x) && !is_weird(x))
	    try {
		return Integer.toString(x);
	    } catch (Exception e) {
	    }
	return "";
    }

    public static String parse_Str(boolean s) {
	if (s == true)
	    return "1";
	else
	    return "0";
    }

    public static int parse_intYhDate(SimpleDateFormat sdf1, SimpleDateFormat sdf2, String st) throws ParseException {

	if (st.isEmpty())
	    return null_int;

	Date dateDate = sdf1.parse(st);
	String dateStr = sdf2.format(dateDate);
	int date_int = parse_int(dateStr);
	return date_int;
    }

    public static int countRows(File file) throws IOException {
	try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
	    byte[] c = new byte[1024];
	    int count = 0;
	    int readChars;
	    boolean endsWithoutNewLine = false;
	    while ((readChars = is.read(c)) != -1) {
		for (int i = 0; i < readChars; ++i) {
		    if (c[i] == '\n') {
			++count;
		    }
		}
		endsWithoutNewLine = (c[readChars - 1] != '\n');
	    }
	    if (endsWithoutNewLine) {
		++count;
	    }
	    return count;
	}
    }

    public static String fileSafeName(String str) {
	return str.replaceAll("[^\\w\\&\\-\\, ]", "");
    }

    private static List<Integer> getFirstIntColValuesList(File file, String delimiter) throws FileNotFoundException, IOException, No_DiskData_Exception {

	List<Integer> list = new ArrayList<>();

	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	    String line, trash = br.readLine();
	    while ((line = br.readLine()) != null) {
		int value = parse_int(line.split(delimiter)[0]);
		if (isnull(value))
		    break;
		else
		    list.add(value);
	    }
	}
	if (list.isEmpty()) throw new No_DiskData_Exception();
	return list;
    }

    public static String myFormat(Double aDouble) {

	if (aDouble != null && !aDouble.isNaN()) {
	    return df.format(aDouble);
	} else {
	    return "";
	}
    }

    public static String myIntFormat(Double aDouble) {

	if (aDouble != null && !aDouble.isNaN()) {
	    return Integer.toString(aDouble.intValue());
	} else {
	    return "";
	}
    }

    public static TagNode getTagNode(String source) throws MalformedURLException, IOException {

	HtmlCleaner cleaner = new HtmlCleaner();
	CleanerProperties props = cleaner.getProperties();
	props.setAllowHtmlInsideAttributes(true);
	props.setAllowMultiWordAttributes(true);
	props.setRecognizeUnicodeChars(true);
	props.setOmitComments(true);

	TagNode node;
	try (InputStreamReader isr = new InputStreamReader(IOUtils.toInputStream(source))) {
	    node = cleaner.clean(isr);
	}
	return node;

    }


    public static float get(float[] ar, int i) throws My_null_exception {
	float value = ar[i];
	if (isnull(value))
	    throw new My_null_exception("null_float value");
	else
	    return value;
    }

    /** this is pointless, just for code conformity */
    public static boolean get(boolean[] ar, int i) throws My_null_exception {
	return ar[i];
    }

    public static int get(int[] ar, int i) throws My_null_exception {
	int value = ar[i];
	if (isnull(value))
	    throw new My_null_exception("null_int value");
	else
	    return value;
    }

    public static String get(String[] ar, int i) throws My_null_exception {
	String value = ar[i];
	if (isnull(value))
	    throw new My_null_exception("null_int value");
	else
	    return value;
    }

    public static boolean isnull(Double x) {
	return x == null;
    }

    public static boolean isnull(String x) {
	return x == null;
    }

    public static boolean isnull(int x) {
	return x == null_int;
    }

    public static boolean isnull(short s) {
	return s == null_short;
    }

    public static boolean isnull(byte b) {
	return b == null_byte;
    }

    public static boolean isnull(float x) {
	return x == null_float;
    }

    public static boolean isnull(double x) {
	return x == null_double;
    }

    public static boolean is_null_or_outOfBounds(float[] ar, int i) {
	try {
	    get(ar, i);
	} catch (Exception e) {
	    return true;
	}
	return false;
    }

    public static boolean is_null_or_outOfBounds(int[] ar, int i) {
	try {
	    get(ar, i);
	} catch (Exception e) {
	    return true;
	}
	return false;
    }

    public static boolean is_weird(float x) {
	return Float.isInfinite(x) || Float.isNaN(x);
    }

    public static boolean is_weird(double x) {
	return Double.isInfinite(x) || Double.isNaN(x);
    }

    public static boolean is_weird(Double x) {
	return x.isInfinite() || x.isNaN();
    }

    public static void myprint(BufferedWriter bw, String str, String separator) throws IOException {
	bw.write(str);
	bw.write(separator);
    }

    public static List<String> uniqueAndSorted(List<String> list) {
	return new ArrayList(new TreeSet(list));
    }

    public static float pctChange(float myfinal, float myinitial) {
	return 100 * (myfinal - myinitial) / myinitial;
    }


    public static float[] insertAtFront(float[] ar, float val) {
	float[] newAr = new float[ar.length + 1];
	newAr[0] = val;
	for (int i = 1; i < newAr.length; i++)
	    newAr[i] = ar[i - 1];

	return newAr;
    }

    public static int[] insertAtFront(int[] ar, int val) {
	int[] newAr = new int[ar.length + 1];
	newAr[0] = val;
	for (int i = 1; i < newAr.length; i++)
	    newAr[i] = ar[i - 1];

	return newAr;
    }

    /** this is the source of current date!  only allowed use of new Date().   and startTime.  and currentDateTime_St */
    private static int currentDate_int_actual() {
	return parse_int(sdf_date.format(new Date()));
    }

    public static Date currentDateDate() throws ParseException {
	return sdf_date.parse(G.currentDate + "");
    }

    static String currentDate_St() {
	return G.currentDate + "";
    }

    /** this is also allowed to use new Date() */
    static String currentDateTime_St_actual() {
	return sdf_full_forFileName.format(new Date());
    }

    public static Long secondsSinceInitialize() {
	return (((new Date()).getTime() - G.startTime) / 1000);
    }

    public static String cleanCsvLine(String line) {

	String[] ar = split_(line);
	return String.join(",", ar);
    }

//    }

    /** sorted reverse order
     * @param newsFile
     * @param delimiter
     * @return 
     * @throws java.io.IOException
     * @throws stuff.G.No_Data_Exception */
    public static int[] getFirstColAr_int(File newsFile, String delimiter) throws IOException, No_DiskData_Exception {

	List<Integer> dates = getFirstIntColValuesList(newsFile, delimiter);
	Collections.sort(dates, Collections.reverseOrder());
	return convertIntegerListTo_intArray(dates);
    }

    public static int[] convertIntegerListTo_intArray(List<Integer> list) {

	int[] ar = new int[list.size()];
	for (int i = 0; i < list.size(); i++) {
	    ar[i] = list.get(i);
	}
	return ar;
    }

    public static List<Integer> convertIntArrayToIntegerList(int[] ar) {
	List<Integer> list = new ArrayList();
	for (int x : ar) {
	    if (isnull(x))
		list.add(null);
	    else
		list.add(x);
	}

	return list;
    }

    public static List<String> getIncompleteTickersSubset(File previousOutputDir) throws IOException {
	return getIncompleteTickersSubset(null, previousOutputDir);
    }

    /** get list of tickers that still need to be gotten, given the subset from args.  what if args is null or empty? - returns full list */
    public static List<String> getIncompleteTickersSubset(String[] args, File previousOutputDir, String maxTicker) throws IOException {
	if (previousOutputDir == null)
	    return getTickersSubset(args);

	previousOutputDir.mkdirs();

	File[] previouslyFinishedOutputs = previousOutputDir.listFiles();
	List<String> tickers = getTickersSubset(args);

	for (File file : previouslyFinishedOutputs) {
	    String filesTicker = G.fileNameWithoutSuffix(file);
	    int indexToRemove = tickers.indexOf(filesTicker);	    //wtf?
	    if (indexToRemove != -1)
		tickers.remove(indexToRemove);
	}

	tickers.removeIf((Object t) -> t.toString().compareTo(maxTicker) > 0);

	return tickers;
    }

    public static List<String> getIncompleteTickersSubset(String[] args, File previousOutputDir) throws IOException {
	return getIncompleteTickersSubset(args, previousOutputDir, "ZZZZZ");
    }


    public static List<String> getTickerSubsetAmongThese(String[] args, File validTickerFilesDir) throws IOException {
	if (validTickerFilesDir == null || !validTickerFilesDir.exists())
	    return getTickersSubset(args);

	Set<String> tickersInTheDir = getTickers(validTickerFilesDir);

	Set<String> tickersArgsSubset = new LinkedHashSet(getTickersSubset(args));

	tickersArgsSubset.retainAll(tickersInTheDir);

	return new ArrayList(tickersArgsSubset);
    }

    public static Set<String> getTickers(File validTickerFilesDir) {
	Set<String> set = new LinkedHashSet();
	for (File file : validTickerFilesDir.listFiles()) {
	    set.add(G.fileNameWithoutSuffix(file));
	}
	return set;
    }

    public static List<String> getTickersSubset(String[] args) throws IOException {

	List<String> tickers = Symbol.getTickersList();

	if (args != null && args.length > 0) {
	    Integer input = getFirstArgsInt(args);
	    if (input != null) {
		List<List<String>> lists = Lists.partition(tickers, tickers.size() / 4);
		tickers = lists.get(input);
	    }
	}
	return tickers;
    }

    public static List<String> getTickers() throws IOException {
	return getTickersSubset(null);
    }


    public static Integer getFirstArgsInt(String[] args) {


	Integer input = null;
	if (args != null && args.length > 0) {
	    input = args[0] == null ? null : Integer.parseInt(args[0]);
	}
	return input;
    }

    /** good site: http://www.degraeve.com/reference/specialcharacters.php */
    public static String cleanUpHtmlSyntax(String st) {
	return st
		.replace("&rsquo;", "\'")
		.replace("&amp;", "&")
		.replace("&quot;", "\"")
		.replace("&nbsp;", " ") //&lsquo;
		.replace("&lsquo;", "\'")
		.replace("&#39;", "\'")
		.trim();
	//CNBC&nbsp;

    }

    /** false if indicator never changes. wait and then  throw exception if clickee doesn't exist */
    static boolean click_element_and_wait_for_indicator_text_to_change(WebDriver driver, String clickee_xpath, String indicator_xpath) throws InterruptedException, WebpageNeverChangedException, Exception {

	WebElement clickee = G.waitForElementByXpath(driver, clickee_xpath);

	return click_element_and_wait_for_indicator_text_to_change(driver, clickee, indicator_xpath);
    }

    /** doesnt wait for clickee.  returns false if not exist */
    static boolean click_element_quietly_and_wait_for_indicator_text_to_change(WebDriver driver, String clickee_xpath, String indicator_xpath) throws InterruptedException {

	WebElement clickee;
	try {
	    clickee = driver.findElement(By.xpath(clickee_xpath));
	} catch (Exception e) {
	    return false;
	}
	return click_element_and_wait_for_indicator_text_to_change(driver, clickee, indicator_xpath);
    }

    /** false if indicator never changes.  it's okay if indicator doesn't exist before clicking clickee*/
    static boolean click_element_and_wait_for_indicator_text_to_change(WebDriver driver, WebElement clickee, String indicator_xpath) throws InterruptedException {


	String indicator_initial_value = "aw3fawe3fa3wfa2w3fa3w43tws4tgwsg";

	try {
	    indicator_initial_value = driver.findElement(By.xpath(indicator_xpath)).getText();		//it's okay if indicator doesn't exist before clicking clickee
	} catch (Exception e) {
	}

	clickee.click();

	long napLength = 0;
	WebElement indicator = null;
	boolean couldntFindIndicator = false;
	do {
	    try {
		indicator = driver.findElement(By.xpath(indicator_xpath));
	    } catch (Exception e) {
		couldntFindIndicator = true;
	    }
	    if (indicator == null || couldntFindIndicator || indicator_initial_value.equals(driver.findElement(By.xpath(indicator_xpath)).getText())) {	// first two are redundant
		Thread.sleep(100);
		napLength += 100;
	    } else
		break;
	} while (napLength < G.webdriver_wait_millis);


	return indicator == null || !indicator_initial_value.equals(driver.findElement(By.xpath(indicator_xpath)).getText());
    }

    public static WebElement waitForElementByXpath(WebDriver driver, String element_xpath) throws InterruptedException, Exception {
	Exception e1;
	long napLength = 0;
	do {
	    try {
		WebElement element = driver.findElement(By.xpath(element_xpath));
		return element;
	    } catch (Exception e) {
		Thread.sleep(100);
		napLength += 100;
		e1 = e;
	    }
	} while (napLength < G.webdriver_wait_millis);
	System.out.println("\n\n\n\n\n\n" + driver.getPageSource());
	System.exit(0);
	throw e1;
    }

    /** false if indicator never changes.  it's okay if indicator doesn't exist before clicking clickee*/
    public static boolean executeJavascript_and_wait_for_indicator_text_to_change(WebDriver driver, String js, String indicator_xpath) throws InterruptedException {

	String indicator_initial_value = "aw3fawe3fa3wfa2w3fa3w43tws4tgwsg";

	try {
	    indicator_initial_value = driver.findElement(By.xpath(indicator_xpath)).getText();		//it's okay if indicator doesn't exist before clicking clickee
	} catch (Exception e) {
	}

//	System.out.println("executing: " + js);

	((JavascriptExecutor)driver).executeScript(js);

	long napLength = 0;
	WebElement indicator = null;
	boolean couldntFindIndicator = false;
	do {
	    try {
		try {
		    indicator = driver.findElement(By.xpath(indicator_xpath));
		} catch (Exception e) {
		    couldntFindIndicator = true;
		}
		if (couldntFindIndicator || indicator_initial_value.equals(driver.findElement(By.xpath(indicator_xpath)).getText())) {	// first two are redundant
		    Thread.sleep(100);
		    napLength += 100;
		} else
		    break;
	    } catch (org.openqa.selenium.StaleElementReferenceException e) {	//thrown by this, i think cos page changes in the middle of this command: indicator_initial_value.equals(driver.findElement(By.xpath(indicator_xpath)).getText())
	    }
	} while (napLength < G.webdriver_wait_millis);


	return indicator == null || !indicator_initial_value.equals(driver.findElement(By.xpath(indicator_xpath)).getText());


    }

    public static boolean listContainsDuplicates(List<Integer> indicesToRemove) {

	Set<Integer> intset = new HashSet(indicesToRemove);

	return intset.size() != indicesToRemove.size();


    }

    public static boolean isDateCalWeekday(Calendar cal) {
	int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
	return (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY);
    }

    public static String getNextWeekdayDate(String date) throws ParseException {

	Date d = sdf_date.parse(date);
	Calendar cal = Calendar.getInstance();
	cal.setTime(d);

	cal.add(Calendar.DAY_OF_YEAR, 1);
	if (isDateCalWeekday(cal))
	    return sdf_date.format(cal.getTime());

	cal.add(Calendar.DAY_OF_YEAR, 1);
	if (isDateCalWeekday(cal))
	    return sdf_date.format(cal.getTime());

	cal.add(Calendar.DAY_OF_YEAR, 1);
	if (isDateCalWeekday(cal))
	    return sdf_date.format(cal.getTime());

	return null;	//this should only happen if there are 3 non-weekdays in a row.  
    }

    private static String getPreviousWeekdayDate(String date) throws ParseException {

	Date d = sdf_date.parse(date);
	Calendar cal = Calendar.getInstance();
	cal.setTime(d);

	cal.add(Calendar.DAY_OF_YEAR, -1);
	if (isDateCalWeekday(cal))
	    return sdf_date.format(cal.getTime());

	cal.add(Calendar.DAY_OF_YEAR, -1);
	if (isDateCalWeekday(cal))
	    return sdf_date.format(cal.getTime());

	cal.add(Calendar.DAY_OF_YEAR, -1);
	if (isDateCalWeekday(cal))
	    return sdf_date.format(cal.getTime());

	return null; //this should only happen if there are 3 non-weekdays in a row.  
    }

    //Integer.parseInt(G.getNextWeekDay(date + ""));

    public static int getNextWeekDay(int date) throws ParseException {
	return Integer.parseInt(G.getNextWeekdayDate(date + ""));
    }

    public static int getPreviousWeekDay(int date) throws ParseException {
	return Integer.parseInt(G.getPreviousWeekdayDate(date + ""));
    }


    public static int daysSinceDate(String dateSt) throws ParseException {
	return G.daysSinceDate(G.sdf_date.parse(dateSt));
    }

    public static int daysSinceDate(int dateInt) throws ParseException {
	return G.daysSinceDate(dateInt + "");
    }

    private static int daysSinceDate(Date date) throws ParseException {
	return daysBetween(date, sdf_date.parse(G.currentDate + ""));
    }

    public static int daysBetween(Date d1, Date d2) {

	return daysBetween(d1.getTime(), d2.getTime());

//	
//	LocalDate localDate_d1 = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//	LocalDate localDate_d2 = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//	return Period.between(localDate_d1, localDate_d2).getDays();
    }

    public static int daysBetween(long t1, long t2) {
	return (int)((t2 - t1) / (1000 * 60 * 60 * 24));
    }

    public static int daysBetween(String ymd1, String ymd2) throws ParseException {
	return daysBetween(sdf_date.parse(ymd1), sdf_date.parse(ymd2));
    }

    public static boolean fileIsMoreThanThisManyDaysOld(File file, int days) throws ParseException {
	Date fileAge = file.exists() ? new Date(file.lastModified()) : G.sdf_date.parse("10661014");

	return (G.daysSinceDate(fileAge) > days);    //don't get people more than once a week.
    }


    public static void sbDelimiterAppend(StringBuilder sb, String str, char t) {
	sb.append(str);
	sb.append(t);
    }

    public static void sbDelimiterAppend(StringBuilder sb, int x, char t) {
	sbDelimiterAppend(sb, String.valueOf(x), t);
    }

    public static void sbDelimiterAppend(StringBuilder sb, float x, char t) {
	sbDelimiterAppend(sb, String.format("%.2f", x), t);
    }

    public static void sbDelimiterAppend(StringBuilder sb, boolean b, char t) {
	sbDelimiterAppend(sb, b == true ? "1" : "0", t);
    }

    /** CommaWithQuotes_removeCommasAndQuotes */
    public static String[] split_(String line) {
	String[] ar = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	for (int i = 0; i < ar.length; i++) {
	    ar[i] = ar[i].replace(",", "").replace("\"", "");
	}
	return ar;


    }


    public static int getIndex(float[] ar, int val) throws Value_Not_In_Array_Exception {
	for (int i = 0; i < ar.length; i++)
	    if (ar[i] == val)
		return i;
	throw new Value_Not_In_Array_Exception();
    }

    public static int getIndex(int[] ar, int val) throws Value_Not_In_Array_Exception {
	for (int i = 0; i < ar.length; i++)
	    if (ar[i] == val)
		return i;
	throw new Value_Not_In_Array_Exception();


    }


    public static float[] new_null_float_ar(int len) {
	return new_filled_array(null_float, len);
    }

    public static int[] new_null_int_ar(int len) {
	return new_filled_array(null_int, len);
    }

    public static boolean[] new_null_boolean_ar(int len) {
	return new_filled_array(null_boolean, len);
    }

    public static String[] new_null_String_ar(int len) {
	return new_filled_array(null_String, len);
    }

    public static byte[] new_null_byte_ar(int len) {
	return new_filled_array(null_byte, len);
    }

    public static float[] new_filled_array(float val, int len) {
	float[] floatAr = new float[len];
	for (int i = 0; i < len; i++)
	    floatAr[i] = val;
	return floatAr;
    }

    public static long[] new_filled_array(long val, int len) {
	long[] longAr = new long[len];
	for (int i = 0; i < len; i++)
	    longAr[i] = val;
	return longAr;
    }


    public static byte[] new_filled_array(byte val, int len) {
	byte[] ar = new byte[len];
	for (int i = 0; i < len; i++)
	    ar[i] = val;
	return ar;
    }

    public static boolean[] new_filled_array(boolean val, int len) {
	boolean[] ar = new boolean[len];
	for (int i = 0; i < len; i++)
	    ar[i] = val;
	return ar;
    }

    public static int[] new_filled_array(int val, int len) {
	int[] ar = new int[len];
	for (int i = 0; i < len; i++)
	    ar[i] = val;
	return ar;
    }

    public static short[] new_filled_array(short val, int len) {
	short[] ar = new short[len];
	for (int i = 0; i < len; i++)
	    ar[i] = val;
	return ar;
    }

    public static String[] new_filled_array(String val, int len) {
	String[] ar = new String[len];
	for (int i = 0; i < len; i++)
	    ar[i] = val;
	return ar;
    }

    public static Map<String, List<Person>> get__tick_management_map(Set<String> tickers) throws Exception {

	System.out.println("getting management...");

	Map<String, List<Person>> tick_management_map = new HashMap<>();
	for (String ticker : tickers) {
	    try {
//		if (!tickerProgress.handle(ticker)) continue;
		tick_management_map.put(ticker, Person.readPeopleFile(ticker));
	    } catch (Exception e) {
	    }
	}
//	timer.lapPrint();
	return tick_management_map;


    }

    public static void recordFailure(File failureFile, String... texts) throws IOException {
	failureFile.getParentFile().mkdirs();
	try (PrintWriter output = new PrintWriter(new FileWriter(failureFile, true))) {
	    output.print(System.lineSeparator());
	    for (String text : texts) {
		output.print(text + "\t");
	    }
	}
    }

    public static void recordFailure(File failureFile, String ticker, String url) throws IOException {
	failureFile.getParentFile().mkdirs();
	recordFailure(failureFile, ticker + "\t" + url);
    }

    /** this just means it tried?  so don't try again?   standardize failure handling!*/
    public static void notateCompletion(File updatedDummyFile) throws IOException {
	updatedDummyFile.getParentFile().mkdirs();
	updatedDummyFile.createNewFile();
    }

    static void test_makeABunchOfFiles() throws IOException {

	File dir = new File("C:\\testfiles");
	dir.mkdir();
	for (int i = 0; i < 10_000; i++) {
	    File file = new File(dir + File.separator + "file_" + i + ".csv");
	    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
		bw.write("x");
	    }
	}
    }

    static void test_makeOneFileWithNLinesOf(File file, int n, String str) throws IOException {
	file.getParentFile().mkdirs();
	for (int i = 0; i < n; i++) {
	    try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
		pw.println(str);

	    }
	}
    }

    static String test_readFromABunchOfFilesInDir(File dir) throws FileNotFoundException, IOException {

	StringBuilder sb = new StringBuilder();
	for (File file : dir.listFiles()) {
	    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		String line;
		while ((line = br.readLine()) != null) {
		    sb.append(line);
		}
	    }
	}
	return sb.toString();
    }

    static String test_readFromABunchOfLinesInFile(File file) throws FileNotFoundException, IOException {

	StringBuilder sb = new StringBuilder();
	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	    String line;
	    while ((line = br.readLine()) != null) {
		sb.append(line);
	    }
	}
	return sb.toString();
    }

    public static boolean append(StringBuilder sb, char d, float[] ar) {
	for (float x : ar)
	    append(sb, d, G.parse_Str(x));
	return true;
    }

    public static boolean append(StringBuilder sb, char d, int[] ar) {
	for (int x : ar)
	    append(sb, d, G.parse_Str(x));
	return true;
    }

    public static boolean append(StringBuilder sb, char d, String[] ar) {
	for (String x : ar)
	    append(sb, d, G.parse_Str(x));
	return true;
    }

    public static boolean append(StringBuilder sb, char d, boolean[] ar) {
	for (boolean x : ar)
	    append(sb, d, G.parse_Str(x));
	return true;
    }

    public static boolean append(StringBuilder sb, char d, byte[] ar) {
	for (byte x : ar)
	    append(sb, d, G.parse_Str(x));
	return true;
    }

    public static void append(StringBuilder sb, String st, char d) {
	sb.append(st);
	sb.append(d);
    }

    public static void append(StringBuilder sb, char d, String st) {
	sb.append(d);
	sb.append(st);
    }


    public static List<String> parse_ListString(String str, String delimiter_regex) {
	String[] ar = str.split("\\" + delimiter_regex);
	return Arrays.asList(ar);
    }

    public static int[] calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(Object[] nullExceptEvents) {
	int[] ar = G.new_null_int_ar(nullExceptEvents.length);
	calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(ar, nullExceptEvents);
	return ar;
    }

    public static void calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(int[] ar, Object[] nullExceptEvents) {
	calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(ar, nullExceptEvents, 0, false);
    }

    /** used to fill arrays that hold daily data for "days since" the most recent event, noted in dummy parameter object array */
    public static void calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(int[] daysSince, Object[] nullExceptEvents, boolean doUpdateForCurrentPrices) {
	calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(daysSince, nullExceptEvents, 0, doUpdateForCurrentPrices);
    }

    /** used to fill arrays that hold daily data for "days since" the most recent event, noted in dummy parameter object array */
    public static void calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(int[] daysSince, Object[] nullExceptEvents, int numExtraFutureDays, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

	for (int i = Math.min(iterMax, nullExceptEvents.length - 1); i >= numExtraFutureDays; i--) {
	    if (nullExceptEvents[i] != null) {
		daysSince[i - numExtraFutureDays] = 0;
		continue;
	    }
	    if (i + 1 < daysSince.length) {
		int prevDaysSince = daysSince[i + 1 - numExtraFutureDays];
		if (!G.isnull(prevDaysSince))
		    daysSince[i - numExtraFutureDays] = prevDaysSince + 1;
	    }					    //else, value stays my_null
	}
    }

    public static int[] calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(Object[] nullExceptEvents) {
	int[] ar = G.new_null_int_ar(nullExceptEvents.length);
	calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(ar, nullExceptEvents);
	return ar;
    }

    public static void calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(int[] ar, Object[] nullExceptEvents) {
	calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(ar, nullExceptEvents, 0, false);
    }

    public static void calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(int[] daysUntil, Object[] nullExceptEvents, boolean doUpdateForCurrentPrices) {
	calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(daysUntil, nullExceptEvents, 0, doUpdateForCurrentPrices);
    }

    public static void calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(int[] daysUntil, Object[] nullExceptEvents, int numExtraFutureDays, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

	int[] daysUntil_Extra = G.new_null_int_ar(nullExceptEvents.length);

	for (int i = 0; i < Math.min(iterMax, daysUntil_Extra.length); i++) {
	    if (nullExceptEvents[i] != null) {
		daysUntil_Extra[i] = 0;
		continue;
	    }
	    if (i + 1 < daysUntil_Extra.length && i > 0) {
		int prevDaysUntil = daysUntil_Extra[i - 1];
		if (!G.isnull(prevDaysUntil))
		    daysUntil_Extra[i] = prevDaysUntil + 1;
	    }					    //else, value stays my_null	//ERROR IT IS 0!!! wtf :(
	}

	for (int i = numExtraFutureDays; i < daysUntil_Extra.length; i++) {
	    daysUntil[i - numExtraFutureDays] = daysUntil_Extra[i];
	}

    }

    /** does not map.put my_null values */
    public static Map<Integer, Integer> get__date_i__map(int[] dates) {

	Map<Integer, Integer> date_i__map = new LinkedHashMap();

	for (int i = 0; i < dates.length; i++) {
	    if (!G.isnull(dates[i]))
		date_i__map.put(dates[i], i);
	}
	return date_i__map;
    }

    public static Map<Integer, Integer> get__date_i__map(Integer[] dates) {
	return get__date_i__map(ArrayUtils.toPrimitive(dates, null_int));
    }

    /** sunday is 0*/
    public static byte getWeekDay(int cookedDate) throws ParseException {

	Date d = sdf_date.parse(cookedDate + "");
	Calendar cal = Calendar.getInstance();
	cal.setTime(d);

	switch (cal.get(Calendar.DAY_OF_WEEK)) {
	    case Calendar.SUNDAY:
		return 0;
	    case Calendar.MONDAY:
		return 1;
	    case Calendar.TUESDAY:
		return 2;
	    case Calendar.WEDNESDAY:
		return 3;
	    case Calendar.THURSDAY:
		return 4;
	    case Calendar.FRIDAY:
		return 5;
	    case Calendar.SATURDAY:
		return 6;
	}
	throw new UnsupportedOperationException("invalid weekday???");
    }

    public static String getClassShortName(Class vclass) {
	String[] ar = vclass.toString().split("\\$");
	String afterTheDollarSign = ar[ar.length - 1];
	ar = afterTheDollarSign.split("\\.");
	String afterThePeriod = ar[ar.length - 1];
	return afterThePeriod;
    }

    public static int getYearFromDateInt(int dateInt) {
	return dateInt / 10_000;
    }

    public static List<String> removeAllWhitespace(List<String> lines) {

	List<String> newlines = new ArrayList(lines.size());
	for (String line : lines) {
	    newlines.add(G.removeAllWhitespace(line));
	}
	return newlines;
    }

    public static String removeAllWhitespace(String line) {
	line = line.trim();
	return line.replaceAll("\\s", "");
    }

    public static String singleSpacesOnly(String line) {
	line = line.replaceAll("\\s", " ");
	while (line.contains("  ")) {
	    line = line.replaceAll("  ", " ");
	}
	return line;
    }

    public static List<Field> getNonPrivateDeclaredFields(SuperX x) {
	Field[] fields = x.eraDataRowClass.getDeclaredFields();


	List<Field> list = new ArrayList(fields.length);

	for (Field f : fields) {
	    if (!Modifier.isPrivate(f.getModifiers())) {
		list.add(f);
//		System.out.println(f.getName());
	    }
	}
	return list;

    }

    public static float[] copy_with_an_extra_null_head_cell(float[] inputArray) {

	float[] ar = new float[inputArray.length + 1];
	ar[0] = null_float;

	for (int i = 0; i < inputArray.length; i++) {
	    ar[i + 1] = inputArray[i];
	}

	return ar;
    }

    public static int[] insert_2nd_array_starting_at_index_1(int[] inputArray) {

	int[] ar = new int[inputArray.length + 1];
	ar[0] = null_int;

	for (int i = 0; i < inputArray.length; i++) {
	    ar[i + 1] = inputArray[i];
	}

	return ar;
    }

    public static void insert_2nd_array_starting_at_index_1(int[] newAr, int[] oldAr) {
	newAr[0] = G.null_int;
	for (int i = 0; i < oldAr.length; i++) {
	    newAr[i + 1] = oldAr[i];
	}
    }

    public static void insert_2nd_array_starting_at_index_1(float[] newAr, float[] oldAr) {
	newAr[0] = G.null_float;
	for (int i = 0; i < oldAr.length; i++) {
	    newAr[i + 1] = oldAr[i];

//		System.out.println("34t34t23 ema3m"+timePeriod + ": " + emaX[i]);

//		System.out.println("34t34t23 newarray" + ": " + newAr[i+1]);
	}
    }

    public static float[] to_float(int[] in) {
	float[] out = new float[in.length];
	for (int i = 0; i < in.length; i++) {
	    out[i] = in[i];
	}
	return out;
    }

    public static double[] to_double(int[] in) {
	double[] out = new double[in.length];
	for (int i = 0; i < in.length; i++) {
	    out[i] = in[i];
	}
	return out;
    }

    public static LinkedHashSet<Integer> getSubset(Set<Integer> myset, int subsetOffset, int subsetFractionDenominator) {

	TreeSet<Integer> stockShrinkingDatesUniverse = new TreeSet(myset);	    //orders the set so that the subset is from a smooth distribution

	int i = 0;
	for (Iterator<Integer> it = stockShrinkingDatesUniverse.iterator(); it.hasNext();) {
	    Integer x = it.next();
	    if (!((i++ + subsetOffset) % subsetFractionDenominator == 0))
		it.remove();
	}

	return new LinkedHashSet(stockShrinkingDatesUniverse);
    }

    public static void deleteDirContents(File dir) {
	if (!dir.exists()) return;
	for (File file : dir.listFiles()) {
	    file.delete();
	}

    }


    public static class Timer {
	long time2, time1;

	public Timer() {
	    time1 = (new Date()).getTime();
	}

	public String lap() {
	    time2 = (new Date()).getTime();
	    String returner = (time2 - time1) / 1000. + " seconds";
	    time1 = time2;
	    return returner;
	}

	public void lapPrint() {
	    lapPrint("");
	}

	public void lapPrint(String pre) {
	    System.out.println(pre + lap());
//	    tickerProgress.reset();
	}
    }

    public static class LetterProgressOutput {
	String minStr;
	String maxStr;
	String prevStr = "0";

	LetterProgressOutput(String minString, String maxString) {
	    this.minStr = minString;
	    this.maxStr = maxString;
	}


//	public boolean handle(String str) {
//	    return handle(str, " ");
//	}
	public boolean handle(String str, String pre) {
	    if (str.isEmpty() || str.compareTo(maxStr) > 0 || str.compareTo(minStr) < 0) {
		prevStr = "0";
		return false;
	    }
	    if (str.charAt(0) != prevStr.charAt(0))
		System.out.println(pre + str.charAt(0));
	    prevStr = str;
	    return true;
	}

	public void reset() {
	    prevStr = "0";
	}

    }

    public static class My_null_exception extends Exception {
	public My_null_exception() {
	}

	public My_null_exception(String null_int_value) {
	    super(null_int_value);
	}

    }

//    public static class EmptyHPException extends Exception {
//	public EmptyHPException() {
//	}
//    }

    public static class Value_Not_In_Array_Exception extends Exception {
	public Value_Not_In_Array_Exception() {
	}
    }

    public static class No_DiskData_Exception extends Exception {
	public No_DiskData_Exception() {
	}
    }

    static class Dont_Write_this_line extends Exception {
	public Dont_Write_this_line() {
	}
    }

    public static String minString(String s1, String s2) {

	if (s1.compareTo(s2) < 0)
	    return s1;
	else
	    return s2;
    }

    public static String incrementDateByDays(String date, int numDays) throws ParseException {
	Date d = G.sdf_date.parse(date);

	Calendar cal = Calendar.getInstance();
	cal.setTime(d);
	cal.add(Calendar.DAY_OF_YEAR, numDays);
	return G.sdf_date.format(cal.getTime());
    }

    public static String incrementDateByDays(int date, int numDays) throws ParseException {
	return incrementDateByDays(date + "", numDays);
    }

    public static class WebpageNeverChangedException extends Exception {
	public WebpageNeverChangedException() {
	}
    }

    public static class GoogleBlockedMeException extends Exception {
	public GoogleBlockedMeException() {
	}
    }

    public static class DontTryAgainLaterException extends Exception {
	public DontTryAgainLaterException() {
	}
    }

    public static class Yahoo_Screwed_Up_Exception extends Exception {
	public Yahoo_Screwed_Up_Exception() {
	}
    }

    public static class TryAgainLater extends Exception {
	public TryAgainLater() {
	}
    }

    /** use this just for temporary testing text? */
    public static void asdf(Object text) {
	System.out.println(text);
    }

    /** use this just for temporary testing text? */
    public static void asdf() {
	System.out.println("");
    }

}
