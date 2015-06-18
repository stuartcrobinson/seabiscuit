package analyze.filter_tools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import utilities.G;

class Portfolio {

    public TreeSet<Investment> openInvestments;
    public TreeSet<Investment> closedInvestments;
    public TreeSet<Investment> invalidInvestments;
    public double currentCash;

    public double minLifetimeValue;
    public double maxLifetimeValue;

    public double finalTotalGainPct;
    public double finalTotalValue;
    public int finalTotalN;
    public int finalTotalNumDays;
    public LinkedHashMap<Integer, Double> map__date_portValue;
    int buysTodayCount;
    private final Screen screen;
    public int numInvalids = 0;

    public Portfolio(Screen screen) {

	this.screen = screen;
	currentCash = screen.pfInitialMoney;

	openInvestments = new TreeSet();
	closedInvestments = new TreeSet();
	invalidInvestments = new TreeSet();

	minLifetimeValue = screen.pfInitialMoney;
	maxLifetimeValue = screen.pfInitialMoney;

	map__date_portValue = new LinkedHashMap();
    }


    /** modifies currentCash.  returns false if not able to buy another stock right now.  sells a stock if it needs to, and if it can.  returns true if we might be able to buy another stock (NOT if we bought one.  cos we might not buy if we already own some of that stock)*/
    public boolean buyAndSell(Hit hit, int dateInt) throws FilterFileException {

	if (!screen.pfCanRebuy && weAlreadyOwnStockForThisCompany(hit))
	    return true;

	/************************************************* SELL IF NEEDED & if possible *****************************************/
	if (!screen.pfSellASAP) {
	    if (openInvestments.size() == screen.pfMaxStocks) {
		Investment theOldestInvestment = openInvestments.first();
		if (theOldestInvestment.daysHeld >= screen.pfMinHoldDays)
		    sell(theOldestInvestment, dateInt);
		else
		    return false;
	    }
	    if (openInvestments.size() >= screen.pfMaxStocks) {
		System.out.println("BIG PROBLEM -- openInvestments.size() >= maxStocks!!!!  can't be > and should have returned if == ");
		System.exit(0);
	    }
	}

	/************************************************* BUY  *****************************************/

	double shareBuyPrice = hit.close;

	if (shareBuyPrice < 0) {
	    G.asdf();
	    G.asdf("WEFWEFSDIFLDK aaaaaaagh " + dateInt);
	    G.asdf(Hit.outputHeaderString());
	    G.asdf(hit.outputStringVerbose());

	    G.asdf("probably cos no filter, so invalid dates were added to portfolio");
	    throw new FilterFileException("shareBuyPrice < 0: " + shareBuyPrice);
	}
	double moneyAllotmentToSpendOnThisHit = currentCash / (screen.pfMaxStocks - (double)openInvestments.size()); // divide cash equally among # of companies we have left to buy

	int numShares = (int)(moneyAllotmentToSpendOnThisHit / shareBuyPrice);	// = (int) moneyamount / shareprice

	if (numShares == 0) {
	    //lets try to buy just one share.  it will use more money than an even split among maxStocks, but better that than just buying nothing, ever

	    numShares = 1;
	}

	double moneyToActuallySpend = numShares * shareBuyPrice;

	if (moneyToActuallySpend <= currentCash) {

	    currentCash = currentCash - moneyToActuallySpend;

	    openInvestments.add(new Investment(hit, numShares, shareBuyPrice, screen));
	    buysTodayCount++;

//	G.asdf("moneymoneymoney to spend on new investment: " + moneyAmount + ", num InvestmentsOpen: " + openInvestments.size());
	}
	return true;
    }

    void incrementInvestmentsAges() {
	for (Investment inv : openInvestments)
	    inv.daysHeld++;
    }

    void sellAllMatureConditionally(int dateInt) {

	List<Investment> invsToSell = new ArrayList();

	if (screen.pfUseSellDates) {
	    if (screen.shrinkingValidSellDates.contains(dateInt)) {
		for (Investment inv : openInvestments)
		    if (inv.daysHeld >= screen.pfMinHoldDays)
			invsToSell.add(inv);
	    }							    //else, don't sell.  wait until a good sellDate
	} else {						    //sell promptly when matured!  daysHeld should never be greater than minHoldDays
	    for (Investment inv : openInvestments)
		if (inv.daysHeld == screen.pfMinHoldDays)
		    invsToSell.add(inv);
	}
	for (Investment inv : invsToSell) {
	    sell(inv, dateInt);
	    numInvalids += inv.isInvalid ? 1 : 0;
	}
    }

    void sellAllMatureForcibly(int dateInt) {

	List<Investment> invsToSell = new ArrayList();

	for (Investment inv : openInvestments)
	    if (inv.daysHeld >= screen.pfMinHoldDays)
		invsToSell.add(inv);

	for (Investment inv : invsToSell) {
	    sell(inv, dateInt);
	    numInvalids += inv.isInvalid ? 1 : 0;
	}
    }

    public void sell(Investment inv, int sellDate) {
	try {
	    openInvestments.remove(inv);

	    inv.sellDate = sellDate;

	    float[] close = inv.hit.stock.vars.get(objects.prices.X.close.class).ar_float;

	    int buy_i = inv.hit.stock.date_i__map.get(inv.buyDate);
	    Integer sell_i = inv.hit.stock.date_i__map.get(sellDate);

	    if (sell_i == null) {
		cry1(sellDate, inv);
		invalidate(inv);
		return;
	    }

	    float buyPrice = close[buy_i];
	    float sellPrice = close[sell_i];
	    if (sellPrice == G.null_float) {
		invalidate(inv);
		return;
	    }

//	    //now check if there was a null price between buy and sell -- might add a  lot of time :(  -- but it would mean we can add a lot more data back! from blanked splits/divs
//	    for (int i = buy_i - 1; i > sell_i; i--) {
//		if (close[i] == G.null_float) {
//		    invalidate(inv);
//		    return;
//		}
//	    }

	    inv.sellPrice = sellPrice;

	    double changeRate_notpct = (sellPrice - buyPrice) / buyPrice;

	    if (changeRate_notpct > 5) {	//this means up 500% -- probably wrong.  i think i'll wipe these out during prices loading, also
		invalidate(inv);
		return;
	    }

	    currentCash += ((inv.numShares * inv.buyPrice) * (1.0 + changeRate_notpct)) - inv.totalFees;

	    closedInvestments.add(inv);

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(0);
	}
    }

    void calculateResults() {
	finalTotalValue = currentCash;
	finalTotalGainPct = 100.0 * (finalTotalValue - screen.pfInitialMoney) / screen.pfInitialMoney;
	finalTotalN = closedInvestments.size();

	finalTotalNumDays = 0;
	Set<Integer> days = new HashSet();
	for (Investment inv : closedInvestments)
	    days.add(inv.buyDate);
	finalTotalNumDays = days.size();
    }

    void writeToFile(File file) throws FileNotFoundException {
	file.getParentFile().mkdirs();

	//first print out summary stuff.  N, Gain
	try (PrintWriter pw = new PrintWriter(file)) {

	    List<Investment> closedInvsList = new LinkedList(closedInvestments);

	    Collections.sort(closedInvsList, (Investment o1, Investment o2) -> Integer.compare(o2.buyDate, o1.buyDate));

	    pw.println(Investment.toFileHeaderLine() + "\t" + "portfolioValueAtEndOfDay");

	    for (Investment inv : closedInvsList) {

		Double portfolioValue = map__date_portValue.get(inv.sellDate);
		String portValueStr = portfolioValue == null ? "null!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" : G.parse_Str_two0s(portfolioValue);

		if (portfolioValue == null) {
		    G.asdf("WHY IS PORTFOLIOVALUE NULL?? why didn't map contain date?");
		    G.asdf("awegaggfg 0 " + file.getName());
		    G.asdf("awegaggfg 1 " + inv);
		    G.asdf("awegaggfg 2 " + inv.toFileLine());
		    G.asdf("awegaggfg 3 " + inv.sellDate);
		    G.asdf("awegaggfg 4 " + map__date_portValue.get(inv.sellDate));
		    G.asdf("awegaggfg 5 " + map__date_portValue.containsKey(inv.sellDate));
		}

		pw.println(inv.toFileLine() + "\t" + "$" + portValueStr);
	    }
	    pw.println();
	    pw.println("note:       \"ind't_var\" is the variable upon which the screen was based.  like futureChangePct_2Week, or something.  most likely, this will be the price pct change over a certain fixed period.");
	    pw.println("            \"rankVal\" is the value of the variable used to rank (increasing or decreasing, depending on filter parameters) all the hits on a given day.  a \"hit\" is a buy-opportunity that passed the filter restrictions");
	    pw.println();
	    pw.println("n:          " + finalTotalN);
	    pw.println("gain (pct): " + finalTotalGainPct);
	}
    }

    void writeInvalidsToFile(File file) throws FileNotFoundException {
	file.getParentFile().mkdirs();

	try (PrintWriter pw = new PrintWriter(file)) {

	    if (!invalidInvestments.isEmpty()) {
		pw.println(Investment.toFileHeaderLine());
		for (Investment inv : invalidInvestments)
		    pw.println(inv.toFileLine());
	    }
	}
    }


    /** deal with minimum value.  check current value.  replace min value if less than that */
    void handleMinMaxValue(double currentValue) {

	if (currentValue < minLifetimeValue)
	    minLifetimeValue = currentValue;

	if (currentValue > maxLifetimeValue)
	    maxLifetimeValue = currentValue;
    }

    public double currentValue() {
	double currentValue = currentCash;
	for (Investment inv : openInvestments)
	    currentValue += (inv.numShares * inv.buyPrice);
	return currentValue;
    }

    public boolean weAlreadyOwnStockForThisCompany(Hit hit) {
	for (Investment inv : openInvestments) {
	    if (inv.hit.stock.name.equals(hit.stock.name))
		return true;
	}
	return false;
    }

    /** remember - data gets wiped (data turned to primitive nulls) surrounding stock splits */
    private void invalidate(Investment inv) {
	inv.isInvalid = true;
	currentCash += (inv.numShares * inv.buyPrice);
	invalidInvestments.add(inv);
    }

    private void cry1(int sellDate, Investment inv) {
	G.asdf("WTF?!!!!!? sell_i == null ????????????????????????????????");
	G.asdf("\n bad sellDate: " + sellDate);
	G.asdf(Investment.toFileHeaderLine());
	G.asdf(inv.toFileLine());
	G.asdf("all dates in: inv.hit.stock.date_i__map.keySet(): ");
	G.asdf(inv.hit.stock.date_i__map.keySet());
    }


}
