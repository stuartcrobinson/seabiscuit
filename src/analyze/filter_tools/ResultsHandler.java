package analyze.filter_tools;
import static analyze.filter_tools.TemplateScreen.ALL;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import utilities.G;

public class ResultsHandler {

    private static final String fmt = "%6s%2s%6s%2s%7s%2s%5s%2s%1s%-3s%3s%3s%3s%2s%3s%3s%2s%1s%7s%2s%1s%6s%2s%5s%2s%5s%2s%1s%-3s%3s%3s%3s%2s%4s%2s%4s%2s%5s%2s%6s%2s%7s%8s%8s%3s%2s"; //pct_abs_gt2_str
    private static final String header0 = String.format("%54s%48s%51s", "__ABS__", "_______________DAYS________________", "___________PORTFOLIO___________");
    private static final String header = String.format(fmt, "yVar", "", "ave", "", "n", "", "ad", "", "", "l-1", "g0", "g1", "gA", "", "g5", "10", "", "", "ave*n", "", "", "ave", "", "n", "", "ad", "", "", "l-1", "g0", "g1", "gA", "", "nCo", "", "HpWk", "", "span", "", "%", "", "min$", "max$", "fin$", "ni", "");

    /**hits sorted by secondaryValue, decreasing for vol, increasing for close */
    private Map<Integer, Set<Hit>> map__dateInt_hits;

    /**only filled if running portfolio simulator */
    public Set<Integer> datesInPeriod;

    private Set<String> companies;

    private final float originalNumberOfDaysInTheExecutionTimeSpan;

//    public boolean alreadyTabulated = false;	//TODELETE

    private final Class yVarClass;
    private final Short descriptor;

    private float count_total = 0;

    private float ave_total = G.null_float;
    private float pct_ltminus1 = G.null_float;
    private float pct_gt0 = G.null_float;
    private float pct_gt1 = G.null_float;
    private float pct_val_gtAv = G.null_float;
    /** deviation is the absolute value of the difference between a value and the average value */
    private float pct_dev_gt5 = G.null_float;
    private float pct_dev_gt10 = G.null_float;
    private float ave_times_n = G.null_float;

    private float dayssAve = G.null_float;

    /** average deviation between days.  deviation is absolute value of difference */
    private float ad_days = G.null_float;
    private float pct_days_ltMinus1 = G.null_float;
    private float pct_days_gt0 = G.null_float;
    private float pct_days_gt1 = G.null_float;
    private float pct_days_gtAv = G.null_float;

    /** average deviation http://www.vitutor.com/statistics/descriptive/average_deviation.html .  standard deviation over-weights large deviations,i think*/
    private float avdev;
    private int hitsPerWeek;
    private Portfolio portfolio;


    private void calculateAndSetTotalAve() {

	float count = 0;
	float sum = 0;
	ave_total = G.null_float;

	for (Set<Hit> hits : map__dateInt_hits.values()) {
	    for (Hit hit : hits) {
		count++;
		sum += hit.value;
	    }
	}
	if (count > 0)
	    ave_total = sum / count;
    }

    public ResultsHandler(Class yVarClass, Short descriptionKey, float originalNumberOfDaysInTheExecutionTimeSpan) {
	this.yVarClass = yVarClass;
	this.descriptor = descriptionKey;
	this.originalNumberOfDaysInTheExecutionTimeSpan = originalNumberOfDaysInTheExecutionTimeSpan;

	companies = new LinkedHashSet();

	map__dateInt_hits = new TreeMap();
	datesInPeriod = new TreeSet();
    }

    public void writeHits() throws IOException {
	File file = G.getResultsHitsFile(yVarClass, getDescriptorString(descriptor));
	file.getParentFile().mkdirs();

	try (PrintWriter pw = new PrintWriter(file)) {
	    pw.println(Hit.outputHeaderString());
	    for (Set<Hit> hits : map__dateInt_hits.values()) {
		TreeSet<Hit> hitsAlphabetized = new TreeSet(hits);	//using Hit.compareTo, which goes by ticker
		for (Hit hit : hitsAlphabetized)
		    pw.println(hit.outputString());
	    }
	}
    }

    private void writePortfolio() throws FileNotFoundException {
	portfolio.writeToFile(G.getResultsPortfolioFile(yVarClass, getDescriptorString(descriptor)));
	portfolio.writeInvalidsToFile(G.getResultsPortfolioInvalidInvestmentsFile(yVarClass, getDescriptorString(descriptor)));
    }


    /**  does NOT check for value nullness!  do not pass potentially null_float values!!!!!  or null dateInt!!! */
    void input_a_passing_value(Hit hit, Screen screen) {

	Set<Hit> day_hits_set = map__dateInt_hits.get(hit.dateInt);
	if (day_hits_set == null) {
	    if (screen.rank1Comparator == null)
		day_hits_set = new LinkedHashSet();
	    else
		day_hits_set = new TreeSet(screen.rank1Comparator);
	    day_hits_set.add(hit);
	    map__dateInt_hits.put(hit.dateInt, day_hits_set);
	} else
	    day_hits_set.add(hit);
    }


    public String outputString() {

	String ave = G.parse_Str_two0s(ave_total);
	String n = G.parse_Str(count_total);
	String pctLtMinus1 = G.parse_Str(Math.round(pct_ltminus1));
	String pctGt0 = G.parse_Str(Math.round(pct_gt0));
	String pctGt1 = G.parse_Str(Math.round(pct_gt1));
	String pctGtAv = G.parse_Str(Math.round(pct_val_gtAv));
	String a_n = G.parse_Str(Math.round(ave_times_n));
	String avdev_str = G.parse_Str_one0(avdev);
	String pct_abs_gt5_str = G.parse_Str(Math.round(pct_dev_gt5));
	String pct_abs_gt10_str = G.parse_Str(Math.round(pct_dev_gt10));

	String nCompanies_str = G.parse_Str(companies.size());
	String nDates_str = G.parse_Str(map__dateInt_hits.size());

	String ad_daysStr = G.parse_Str_one0(ad_days);

	String yVar = G.getClassShortName(yVarClass);

	String descriptorStr = getDescriptorString(descriptor).replace("NESDAY", "").replace("SDAY", "").replace("DAY", "");

	String pct_days_LtMinus1_str = G.parse_Str(Math.round(pct_days_ltMinus1));
	String pct_days_gt0_str = G.parse_Str(Math.round(pct_days_gt0));
	String pct_days_gt1_str = G.parse_Str(Math.round(pct_days_gt1));
	String pct_days_gtAv_str = G.parse_Str(Math.round(pct_days_gtAv));

	String hitsPerWeek_str = G.parse_Str(hitsPerWeek);

	String portGainPct = "";
	String portN = "", portNumDays = "";
	String minVal = "", maxVal = "", finVal = "", ni = "";

	if (portfolio != null) {
	    portN = G.parse_Str(portfolio.finalTotalN);
	    portGainPct = G.parse_Str_one0((float)portfolio.finalTotalGainPct);
	    minVal = G.parse_Str((int)portfolio.minLifetimeValue);
	    maxVal = G.parse_Str((int)portfolio.maxLifetimeValue);
	    finVal = G.parse_Str((int)portfolio.finalTotalValue);
	    portNumDays = G.parse_Str(portfolio.finalTotalNumDays);
	    ni = G.parse_Str(portfolio.numInvalids);
	}

	if (descriptor != ALL)
	    hitsPerWeek_str = "";
	String s = "";

	if (yVar.contains("_week") || yVar.contains("_month")) {
	    s = "*";
	}

	return String.format(fmt, yVar.replace("futChPct_", ""), s, ave, s, n, s, avdev_str, s, "", pctLtMinus1, pctGt0, pctGt1, pctGtAv, s, pct_abs_gt5_str, pct_abs_gt10_str, s, s, a_n, s, s, G.parse_Str(dayssAve), s, nDates_str, s, ad_daysStr, s, "", pct_days_LtMinus1_str, pct_days_gt0_str, pct_days_gt1_str, pct_days_gtAv_str, s, nCompanies_str, s, hitsPerWeek_str, s, descriptorStr, s, portGainPct, s, minVal, maxVal, finVal, ni, "");    //final empty string is so timestamp has space before it. when on same line (when research mdoe)

    }

    public static void calculateAndDisplayResults(Screen screen) throws IOException, FilterFileException {
	calculateScreenResults(screen);
	displayScreenResults(screen);
    }

    private static void calculateScreenResults(Screen screen) throws FilterFileException {

	screen.productionManager.rankAndShrink();

	for (Map<Short, ResultsHandler> subresults : screen.results.values()) {
	    for (ResultsHandler rh : subresults.values()) {

		if (screen.pfDoSimulate)
		    rh.simulatePortfolio(screen);

		rh.calculateAndSetTotalAve();
		rh.calculateEverythingElse();
	    }
	}
    }

    void shrink1(Screen screen) {
	for (Set<Hit> hits : map__dateInt_hits.values()) {
	    keepFirstElements(hits, screen.rank1KeepAmount);
	}
    }


    void rankAndShrink2(Screen screen) {
	Map<Integer, Set<Hit>> map__dateInt_hits_NEW = new TreeMap();

	for (Map.Entry<Integer, Set<Hit>> entry : map__dateInt_hits.entrySet()) {
	    Set<Hit> originalHits = entry.getValue();
	    Integer dateInt = entry.getKey();

	    Set<Hit> newHits = new TreeSet(screen.rank2Comparator);
	    newHits.addAll(originalHits);

	    keepFirstElements(newHits, screen.rank2KeepAmount);

	    map__dateInt_hits_NEW.put(dateInt, newHits);
	}
	map__dateInt_hits = map__dateInt_hits_NEW;
    }


    private static void displayScreenResults(Screen screen) throws IOException {

	Map<Class, Map<Short, ResultsHandler>> results = screen.results;
	Set<Hit> todaysPicks = screen.productionManager.todaysHits;

	G.asdf();
//	
//	/***************** display invalid investments **********************/
//	G.asdf("INVALID INVESTMENTS");
//	G.asdf(Investment.toFileHeaderLine());
//	for (Map<Short, ResultsHandler> subresults : results.values())
//	    for (ResultsHandler tab : subresults.values())
//		if (screen.pfDoSimulate && !tab.portfolio.invalidInvestments.isEmpty())
//		    for (Investment inv : tab.portfolio.invalidInvestments)
//			G.asdf(inv.toFileLine());

	System.out.println(header0);
	System.out.println(header);

	int periodsLength = 0;


	int count = 0;
	for (Map<Short, ResultsHandler> subresults : results.values()) {
	    for (Map.Entry<Short, ResultsHandler> entry : subresults.entrySet()) {
		periodsLength = subresults.size();
		ResultsHandler tab = entry.getValue();
		Short period = entry.getKey();
		if (!(period == TemplateScreen.ALL)) {
		    if (count++ > 0)
			System.out.println();
		    System.out.print(tab.outputString());
		}
	    }
	}

	if (periodsLength > 1) {
	    System.out.println();
	    System.out.println(header0);
	    System.out.println(header);
	}
	count = 0;
	for (Map<Short, ResultsHandler> subresults : results.values()) {
	    ResultsHandler tab = subresults.get(TemplateScreen.ALL);
	    if (tab == null) continue;
	    if (count++ > 0)
		System.out.println();
	    System.out.print(tab.outputString());
	}

	//now print hits

	if (screen.doPrintHits) {
	    G.deleteDirContents(G.hits);
	    for (Map<Short, ResultsHandler> subresults : results.values())
		for (Map.Entry<Short, ResultsHandler> entry : subresults.entrySet())
		    entry.getValue().writeHits();
	}

	if (screen.pfDoSimulate) {
	    G.deleteDirContents(G.portfolios);
	    for (Map<Short, ResultsHandler> subresults : results.values())
		for (Map.Entry<Short, ResultsHandler> entry : subresults.entrySet()) {
		    entry.getValue().writePortfolio();
		}
	}

	if (todaysPicks != null) {
	    System.out.println("\n" + todaysPicks.size() + " picks today: " + new TreeSet(todaysPicks));
	}
    }

    private String getDescriptorString(Short descriptor) {
	String descriptorStr;
	if (descriptor == -1)
	    descriptorStr = "all";
	else if (descriptor < 10)
	    descriptorStr = Screen.weekDaysMap.get((int)descriptor).toString();
	else
	    descriptorStr = String.valueOf(descriptor);

	return descriptorStr;
    }

    private void calculateEverythingElse() {

	float count_ltminus1 = 0;
	float count_gt0 = 0;
	float count_gt1 = 0;
	companies = new LinkedHashSet();


	float count_days = 0;
	float sum_days_aves = 0;
	float sum_dayDevs = 0;

	float count_daysLtMinus1 = 0;
	float count_daysGt0 = 0;
	float count_daysGt1 = 0;
	float count_daysGtAv = 0;

	count_total = 0;
	float sumAbsValDeviations = 0;
	float countDevGt5 = 0;
	float countDevGt10 = 0;
	float countValGtAv = 0;

	for (Set<Hit> dayHits : map__dateInt_hits.values()) {
	    count_days++;
	    float count_valuesOnThisDay = 0;
	    float sum_valuesOnThisDay = 0;

	    for (Hit hit : dayHits) {
		float value = hit.value;

		count_valuesOnThisDay++;
		sum_valuesOnThisDay += value;

		if (value < -1)
		    count_ltminus1++;

		if (value > 0)
		    count_gt0++;

		if (value > 1)
		    count_gt1++;

		companies.add(hit.stock.name);

		float absValDev = Math.abs(ave_total - value);

		count_total++;
		sumAbsValDeviations += absValDev;

		if (absValDev > 5)
		    countDevGt5++;
		if (absValDev > 10)
		    countDevGt10++;
		if (value > ave_total)
		    countValGtAv++;
	    }

	    if (count_valuesOnThisDay > 0) {
		float dayValueAve = sum_valuesOnThisDay / count_valuesOnThisDay;		    //works for NA
		sum_days_aves += dayValueAve;
		if (dayValueAve < -1)
		    count_daysLtMinus1++;
		if (dayValueAve > 0)
		    count_daysGt0++;
		if (dayValueAve > 1)
		    count_daysGt1++;
		if (dayValueAve > ave_total)
		    count_daysGtAv++;
		float deviation = Math.abs(dayValueAve - ave_total);	    //deviation is the absValueOfDeviation_betweenAveOfThisDay_and_aveOverAllDays
		sum_dayDevs += deviation;
	    }

	}
	if (count_total > 0) {
	    pct_ltminus1 = 100 * count_ltminus1 / count_total;
	    pct_gt0 = 100 * count_gt0 / count_total;
	    pct_gt1 = 100 * count_gt1 / count_total;
	    ave_times_n = ave_total * count_total;

	    float originalNumberOfWeeksInTimespan = (originalNumberOfDaysInTheExecutionTimeSpan / 5.0f);
	    hitsPerWeek = Math.round(count_total / originalNumberOfWeeksInTimespan);

	    pct_dev_gt5 = 100 * countDevGt5 / count_total;
	    pct_dev_gt10 = 100 * countDevGt10 / count_total;
	    pct_val_gtAv = 100 * countValGtAv / count_total;

	    avdev = sumAbsValDeviations / count_total;
	}
	if (count_days > 0) {
	    ad_days = sum_dayDevs / count_days;

	    pct_days_ltMinus1 = 100 * count_daysLtMinus1 / count_days;
	    pct_days_gt0 = (100 * count_daysGt0 / count_days);
	    pct_days_gt1 = (100 * count_daysGt1 / count_days);
	    pct_days_gtAv = (100 * count_daysGtAv / count_days);

	    dayssAve = sum_days_aves / count_days;
	}
    }

    /** must be first to calculate.  sets hits values with actual pricePctChange given actual sellPrice used in portfolio */
    private void simulatePortfolio(Screen screen) throws FilterFileException {

	portfolio = new Portfolio(screen);
	int numDates = datesInPeriod.size();
	if (numDates == 0)
	    return;

	int dayCount = 1;
	Integer lastDate = null;
	for (Integer dateInt : datesInPeriod) {
	    try {
		double currentValue = portfolio.currentValue();

		portfolio.map__date_portValue.put(dateInt, currentValue);
		lastDate = dateInt;

		portfolio.incrementInvestmentsAges();
		portfolio.handleMinMaxValue(currentValue);

		if (screen.pfSellASAP)
		    portfolio.sellAllMatureConditionally(dateInt);

		if (dayCount++ < numDates - screen.pfMinHoldDays) {  //keep looping even if this line is false! need to finish out the days to sell all holdings		//don't buy new if there's not enough time left to hold it for minimum holding period
		    Set<Hit> hits = map__dateInt_hits.get(dateInt);
		    if (hits == null) continue;

		    portfolio.buysTodayCount = 0;
		    for (Hit hit : hits) {
			if (!portfolio.buyAndSell(hit, dateInt))
			    break;
			if (portfolio.buysTodayCount == screen.pfMaxStocksBuyPerDay)
			    break;
		    }
		}
//		testing_printOpenAndClosed(dateInt);
	    } catch (Exception e) {
		G.asdf("awefao8wufoifdff why exceptiON?");
		e.printStackTrace();
		throw e;
	    }
	}
	portfolio.sellAllMatureForcibly(lastDate);
	portfolio.calculateResults();

	//now replace map__dateInt_hits again -- so main analysis looks at ONLY stocks from the portfolio history

	map__dateInt_hits = new TreeMap();

	if (!portfolio.openInvestments.isEmpty())
	    throw new FilterFileException("openInvestments not empty after finishing portfolio simulation!!!  size: " + portfolio.openInvestments.size());


	for (Investment inv : portfolio.closedInvestments) {


	    //make sure none of these are null_float!!!!!
	    inv.hit.value = (float)(100 * (inv.sellPrice - inv.buyPrice) / inv.buyPrice);

	    input_a_passing_value(inv.hit, screen);
	}
    }

    private void keepFirstElements(Set<Hit> hits, int topNHitsToKeepPerDay) {
	int count = 1;
	for (Iterator<Hit> iter = hits.iterator(); iter.hasNext();) {
	    iter.next();
	    if (count++ > topNHitsToKeepPerDay)
		iter.remove();
	}
    }

    private void printAllOpenInvestments(Integer dateInt) {
	G.asdf("-------------------------------------OPEN----- " + dateInt);
	G.asdf(Investment.toFileHeaderLine());
	for (Investment inv : portfolio.openInvestments) {
	    G.asdf(inv.toFileLine());
	}

    }


    private void printAllClosedInvestments(Integer dateInt) {
	G.asdf("-------------------------------------CLOSED---- " + dateInt);
	G.asdf(Investment.toFileHeaderLine());
	for (Investment inv : portfolio.closedInvestments) {
	    G.asdf(inv.toFileLine());
	}

    }

    private void testing_printOpenAndClosed(Integer dateInt) {
	G.asdf("###################################################################################################################### " + dateInt);
	printAllOpenInvestments(dateInt);
	printAllClosedInvestments(dateInt);
    }

}
