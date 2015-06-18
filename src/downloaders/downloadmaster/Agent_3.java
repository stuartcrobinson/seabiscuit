package downloaders.downloadmaster;
import java.io.IOException;
import java.text.ParseException;
import javax.xml.xpath.XPathExpressionException;
import org.htmlcleaner.XPatherException;
import utilities.G;
import downloaders.regular_stocks_stuff.Replace_Financials;
import downloaders.regular_stocks_stuff.Replace_Prices_Daily;
import downloaders.regular_stocks_stuff.UdFlow_News_Yahoo;
import downloaders.regular_stocks_stuff.UdFlow_SecReportDates;
import downloaders.regular_stocks_stuff.UdFlow_ShortInterest;
import downloaders.regular_stocks_stuff.UdFlow_Splits;
import downloaders.regular_stocks_stuff.UdSttc_People;
import downloaders.regular_stocks_stuff.UdSttc_Profile_Yahoo;

public class Agent_3 {
    public static void main(String[] args) throws IOException, InterruptedException, ParseException, XPathExpressionException, XPatherException, UdSttc_People.EmptyTableException, G.No_DiskData_Exception {

	args = new String[]{"3"};

	Replace_Prices_Daily.go(args);
	Replace_Financials.go_annual(args);
	Replace_Financials.go_quarterly(args);

	UdSttc_Profile_Yahoo.go(args);	    //google Agent_Google

	UdFlow_SecReportDates.go(args);
	
	UdSttc_People.go(args);

	UdFlow_ShortInterest.go(args);

	UdFlow_Splits.go(args);

	UdFlow_News_Yahoo.go(args);		    //google Agent_Google
    }
}
