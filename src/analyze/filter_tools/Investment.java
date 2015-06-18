package analyze.filter_tools;
import utilities.G;

/** sortable by daysHeld in reverse */
public class Investment implements Comparable<Investment> {

    public Hit hit;
    public int buyDate;
    public int daysHeld;

    int numShares;
    double buyPrice;
    double sellPrice;
    int sellDate;

    double totalFees;
    /** remember - data gets wiped (data turned to primitive nulls) surrounding stock splits */
    boolean isInvalid = false;

    public Investment(Hit hit, int numShares, double buyPrice, Screen screen) {

	this.hit = hit;
	this.numShares = numShares;

	this.buyDate = hit.dateInt;
	this.buyPrice = buyPrice;
	daysHeld = 0;

	this.sellPrice = G.null_double;

	if (screen.pfUseFees) {
//	    totalFees = 2 + bidAskSpread(hit.vol) * buyPrice * numShares;	//2 is 1 dollar trade price times 2 - buy and sell
	    totalFees = 2 + 0.02 * numShares;					//a penny loss during buy and sell.  and the 1 dollar fee to IB each way.
	} else
	    totalFees = 0;
    }

    @Override
    public int compareTo(Investment o) {

	int compared = Integer.compare(o.daysHeld, daysHeld);

	if (compared == 0) {

	    compared = Integer.compare(buyDate, o.buyDate);

	    if (compared == 0) {

		compared = hit.stock.name.compareTo(o.hit.stock.name);
	    }
	}
	return compared;
    }

    static String toFileHeaderLine() {

	return "buyDate"
		+ "\t" + "sellDate"
		+ "\t" + "co"
		+ "\t" + "shares"
		+ "\t" + "daysHeld"
		+ "\t" + "buyPrice"
		+ "\t" + "sellPrice"
		+ "\t" + "gainPct"
		+ "\t" + "indtVar"
		+ "\t" + "vol"
		+ "\t" + "rank1Val"
		+ "\t" + "rank2Val";
    }

    String toFileLine() {

	double gainPct = 100 * (sellPrice - buyPrice) / buyPrice;

	return buyDate
		+ "\t" + sellDate
		+ "\t" + G.parse_Str(hit.stock.name)
		+ "\t" + G.parse_Str(numShares)
		+ "\t" + G.parse_Str(daysHeld)
		+ "\t" + G.parse_Str(buyPrice)
		+ "\t" + G.parse_Str(sellPrice)
		+ "\t" + G.parse_Str(gainPct)
		+ "\t" + G.parse_Str(hit.value)
		+ "\t" + G.parse_Str(hit.vol)
		+ "\t" + G.parse_Str(hit.rank1Value)
		+ "\t" + G.parse_Str(hit.rank2Value);
    }


    /** i mostly made this stuff up.  used analysis of 40ish stocks in yahoo portfolio. https://finance.yahoo.com/portfolio/pf_1/view/v3 */
    private double bidAskSpread(float vol) {

	if (vol < 1_000_000)
	    return 0.003;
	return 0.001;

    }

}
