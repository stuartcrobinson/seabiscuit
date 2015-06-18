package objects.profile;
import categories.C;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import utilities.G;
import static utilities.G.cleanNAs;
import static utilities.G.getTagNode;
import static utilities.G.null_float;
import static utilities.G.null_int;
import static utilities.G.null_short;
import static utilities.G.parse_Str;
import static utilities.G.parse_float;
import static utilities.G.parse_int;
import static utilities.G.parse_intYhDate;
import static utilities.G.sbDelimiterAppend;
import static utilities.G.sdf_date;
import utilities.HttpDownloadUtility;
import downloaders.regular_stocks_stuff.UdSttc_Profile_Yahoo.TickerNotFoundByYahooException;

public class YahooProfile {

    //ticker	exchange	Index Membership	Sector	Industry	Full Time Employees
    public String ticker = null;
    public String exchange = null;

    public boolean isStock = false;
    public String index_membership = null;
    public String sector = null;
    public String industry = null;

    public int nEmployees = null_int;

    public boolean isFund = false;
    public String fundCategory = null;
    public String fundFamily = null;
    public short fundCategory_i = null_short;
    public short fundFamily_i = null_short;
    public float net_assets = null_float;
    public float yield = null_float;
    public int inception_date = null_int;
    public String legal_type = null;
    public float annual_report_expense_ratio_net = null_float;
    public float annual_holdings_turnover = null_float;
    public float total_net_assets = null_float;

    /** downloads from internet */
    public YahooProfile(String ticker, boolean dummy_from_internet) throws IOException, XPatherException, ParseException, InterruptedException, TickerNotFoundByYahooException, NoYahooProfileForThisStock, SpecificStockCouldntBeMatchedWithTicker, TickerIsNoLongerValidInYahoo_itChanged {

	this.ticker = ticker;

	//http://finance.yahoo.com/q/pr?s=AAPL+Profile
	String url = "http://finance.yahoo.com/q/pr?s=" + ticker + "+Profile";

	System.out.print(url);
	String source = HttpDownloadUtility.getPageSource(url);

	if (source.contains("There are no results for the given search term."))
	    throw new TickerNotFoundByYahooException();
	if (source.contains("There is no Profile data available"))
	    throw new NoYahooProfileForThisStock();
	if (source.contains("Get Quotes Results for"))
	    throw new SpecificStockCouldntBeMatchedWithTicker();
	if (source.contains("is no longer valid. It has changed to"))
	    throw new TickerIsNoLongerValidInYahoo_itChanged();
	//

	/*
	 is no longer valid
	
	 Get Quotes Results for
	 There is no Profile data available
	 */

	TagNode node = getTagNode(source);

	SimpleDateFormat yhProfileInceptionDateSdf = new SimpleDateFormat("MMM dd, yyyy");

	Object[] exchange_ = node.evaluateXPath("//span[@class='rtq_exch']");
	exchange = ((TagNode)exchange_[0]).getText().toString().trim().replace("-", "");
	Object[] tables = node.evaluateXPath(".//table[@class='yfnc_datamodoutline1']/tbody/tr/td/table/tbody");
	TagNode t1 = (TagNode)tables[0];
	TagNode t2 = (TagNode)tables[0];

	if (tables.length > 0) {

	    if (source.contains("Details")) {				//is a stock
		index_membership = cleanNAs(((TagNode)t1.evaluateXPath("//tr[1]/td[2]")[0]).getText());
		sector = C.getCleanedCategoryName(cleanNAs(((TagNode)t1.evaluateXPath("//tr[2]/td[2]")[0]).getText()));
		industry = C.getCleanedCategoryName(cleanNAs(((TagNode)t1.evaluateXPath("//tr[3]/td[2]")[0]).getText()));
		nEmployees = parse_int(cleanNAs(((TagNode)t1.evaluateXPath("//tr[4]/td[2]")[0]).getText().toString()));

	    } else if (source.contains("Fund Overview")) {				//is a fund
		fundCategory = cleanNAs(((TagNode)t1.evaluateXPath("//tr[1]/td[2]")[0]).getText());
		fundFamily = cleanNAs(((TagNode)t1.evaluateXPath("//tr[2]/td[2]")[0]).getText());
		net_assets = parse_float(cleanNAs(((TagNode)t1.evaluateXPath("//tr[3]/td[2]")[0]).getText().toString()));
		yield = parse_float(cleanNAs(((TagNode)t1.evaluateXPath("//tr[4]/td[2]")[0]).getText().toString().trim()));
		inception_date = parse_intYhDate(yhProfileInceptionDateSdf, sdf_date, cleanNAs(((TagNode)t1.evaluateXPath("//tr[5]/td[2]")[0]).getText().toString().trim()));
		legal_type = cleanNAs(((TagNode)t1.evaluateXPath("//tr[6]/td[2]")[0]).getText());

		annual_report_expense_ratio_net = parse_float(cleanNAs(((TagNode)t2.evaluateXPath("//tr[2]/td[2]")[0]).getText().toString().trim()));
		annual_holdings_turnover = parse_float(cleanNAs(((TagNode)t2.evaluateXPath("//tr[3]/td[2]")[0]).getText().toString().trim()));
		total_net_assets = parse_float(cleanNAs(((TagNode)t2.evaluateXPath("//tr[4]/td[2]")[0]).getText().toString().replace("NaN", "").trim()));

	    }
	}
    }

    /** each file should have only one line now.  one line per company */
    public static YahooProfile readFromDisk(String ticker) throws IOException, ParseException, G.No_DiskData_Exception {
	File file = G.getYahooProfileFile(ticker);
	if (!file.exists())
	    throw new G.No_DiskData_Exception();
	List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
	String line = lines.get(0);
	return new YahooProfile(line);
    }


    public YahooProfile(String line) throws ParseException {
	String[] lineAr = line.split("\t");
	try {
	    ticker = lineAr[0];
	    exchange = lineAr[1];
	} catch (Exception e) {
	}

	//stock stuff:
	try {
	    index_membership = lineAr[2];
	    sector = C.getCleanedCategoryName(lineAr[3]);
	    industry = C.getCleanedCategoryName(lineAr[4]);
	    nEmployees = parse_int(lineAr[5].replace(",", ""));	//TODO remove replace comma.  downloader does that.
	} catch (Exception e) {
	}
	if (sector == null || sector.isEmpty() || sector.equals("N/A"))
	    sector = null;
	if (industry == null || industry.isEmpty() || industry.equals("N/A"))
	    industry = null;
	isStock = sector != null && industry != null;


	if (isStock && (sector.isEmpty() || industry.isEmpty())) {
	    System.out.println("@@@@ FOUND IT stock " + ticker);
	    System.exit(0);
	}

	//fund stuff:
	try {
	    fundCategory = lineAr[6];
	    fundFamily = lineAr[7];
	    net_assets = parse_float(lineAr[8]);
	    yield = parse_float(lineAr[9]);
	    inception_date = parse_int(lineAr[10]);
	    legal_type = lineAr[11];
	    annual_report_expense_ratio_net = parse_float(lineAr[12]);
	    annual_holdings_turnover = parse_float(lineAr[13]);
	    total_net_assets = parse_float(lineAr[14]);
	} catch (Exception e) {
	}
	if (fundCategory == null || fundCategory.isEmpty() || fundCategory.equals("N/A"))
	    fundCategory = null;
	if (fundFamily == null || fundFamily.isEmpty() || fundFamily.equals("N/A"))
	    fundFamily = null;
	isFund = fundCategory != null && fundFamily != null;
    }


    public String toOutputString() {
	char t = '\t';
	StringBuilder sb = new StringBuilder();
	sbDelimiterAppend(sb, parse_Str(ticker), t);
	sbDelimiterAppend(sb, parse_Str(exchange), t);
	sbDelimiterAppend(sb, parse_Str(index_membership), t);
	sbDelimiterAppend(sb, parse_Str(sector), t);
	sbDelimiterAppend(sb, parse_Str(industry), t);
	sbDelimiterAppend(sb, parse_Str(nEmployees), t);
	sbDelimiterAppend(sb, parse_Str(fundCategory), t);
	sbDelimiterAppend(sb, parse_Str(fundFamily), t);
	sbDelimiterAppend(sb, parse_Str(net_assets), t);
	sbDelimiterAppend(sb, parse_Str(yield), t);
	sbDelimiterAppend(sb, parse_Str(inception_date), t);
	sbDelimiterAppend(sb, parse_Str(legal_type), t);
	sbDelimiterAppend(sb, parse_Str(annual_report_expense_ratio_net), t);
	sbDelimiterAppend(sb, parse_Str(annual_holdings_turnover), t);
	sbDelimiterAppend(sb, parse_Str(total_net_assets), t);

	return sb.toString();
    }

    public void writeToFile() throws IOException {
	FileUtils.writeStringToFile(G.getYahooProfileFile(ticker), toOutputString());
    }

    public static class NoYahooProfileForThisStock extends Exception {
	public NoYahooProfileForThisStock() {
	}
    }

    public static class SpecificStockCouldntBeMatchedWithTicker extends Exception {
	public SpecificStockCouldntBeMatchedWithTicker() {
	}
    }

    public static class TickerIsNoLongerValidInYahoo_itChanged extends Exception {
	public TickerIsNoLongerValidInYahoo_itChanged() {
	}
    }

}
