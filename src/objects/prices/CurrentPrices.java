package objects.prices;
import static utilities.G.null_float;
import static utilities.G.null_int;
import static utilities.G.parse_float;

public class CurrentPrices {

    public float open = null_float;
    public float high = null_float;
    public float low = null_float;
    public float close = null_float;
    public int date = null_int;
    public float volume = null_float;

    public CurrentPrices() {
    }

    public CurrentPrices(int date, String str) {

//	    //o: open, h: high, g: low, l1: last price, v: volume
//	    String URL_KEY = "TICKERS_LIST_SEPARATED_BY_PLUSSES";
//	    String urlTemplate = "http://finance.yahoo.com/d/quotes.csv?s=" + URL_KEY + "&f=ohgl1v";

	this.date = date;

	String[] lineAr = str.split(",");

	open = parse_float(lineAr[0]);
	high = parse_float(lineAr[1]);
	low = parse_float(lineAr[2]);
	close = parse_float(lineAr[3]);
	volume = parse_float(lineAr[4]);
    }
}
