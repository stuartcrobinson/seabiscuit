package analyze.filter_tools;
import objects.Stock;
import utilities.G;

public final class Hit implements Comparable {
    public float value;
//    public final String ticker;
    public final int dateInt;

    /** just use volume (testing close now)*/
    public final float rank1Value;
    public final float rank2Value;
    public final float vol;
    public final float close;
    public final Stock stock;

    public Hit(float value, float rank1Value, float rank2Value, int dateInt, float vol, float close, Stock stock) {
	this.value = value;
//	this.ticker = stock.name;
	this.dateInt = dateInt;
	this.rank1Value = rank1Value;
	this.rank2Value = rank2Value;
	this.vol = vol;
	this.close = close;
	this.stock = stock;

    }

    static String outputHeaderString() {
//	return String.format("%8d%8s%8.2f%8.2f%8.2f", dateInt, ticker, close, vol, value, secondaryValue);
	return "dateInt" + "\t" + "ticker" + "\t" + "close" + "\t" + "vol" + "\t" + "value" + "\t" + "rank1Value";
    }

    String outputString() {
//	return String.format("%8d%8s%8.2f%8.2f%8.2f", dateInt, ticker, close, vol, value, secondaryValue);
	return dateInt + "\t" + stock.name + "\t" + G.parse_Str(close) + "\t" + G.parse_Str(vol) + "\t" + G.parse_Str(value) + "\t" + G.parse_Str(rank1Value) + "\t" + G.parse_Str(rank2Value);
    }
    String outputStringVerbose() {
//	return String.format("%8d%8s%8.2f%8.2f%8.2f", dateInt, ticker, close, vol, value, secondaryValue);
	return dateInt + "\t" + stock.name + "\t" + (close) + "\t" + (vol) + "\t" + (value) + "\t" + (rank1Value) + "\t" + (rank2Value);
    }

    /** USED BY INVESTMENT!  use caution if you want to change this.  just for alphabetized output.  sort for secondary ranking metric elsewhere! */
    @Override
    public int compareTo(Object o) {
	return stock.name.compareTo(((Hit)o).stock.name);
    }

}
