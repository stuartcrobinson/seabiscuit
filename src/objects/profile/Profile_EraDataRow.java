package objects.profile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilities.G;
import supers.Era;
import categories.C;
import java.util.LinkedHashMap;
import objects.Stock;

public class Profile_EraDataRow extends Era.EraDataRow {

    /** map key: ticker, value: profileEraDataRow.  this works because there is only 1 profile era per stock (maybe we should update this later.  do profiles change over time?  very slowly */
    public static Map<String, Profile_EraDataRow> getAllFromStocks(List<Stock> stocks) {
	Map<String, Profile_EraDataRow> map = new LinkedHashMap(stocks.size());
	for (Stock stock : stocks) {
	    String ticker = stock.name;
	    Profile_EraDataRow pedr = (Profile_EraDataRow)stock.profile_X.eras.get(0).eraDataRow;
//	    //testing
//	    System.out.println("aw3fw4fu8u8u:    " + ticker + " " + pedr.google_industry + "  #### " + pedr.toOutputStringEDR());
	    map.put(ticker, pedr);
	}
	return map;
    }
    public List<String> google_relatedCompanies;

    private String google_sector;
    private String google_industry;

    private String yahoo_sector;
    private String yahoo_industry;

    /** NOTE:  this is ALL the categories!!! not just the ones we want to analyze right now.  filter first with C.catTypesToAnalyze for that.  value is the specific cleaned category subname, like "mining."  for google related companies, it's a list of all the related companies */
    public Map<C.CategoryType, String> categories;

    private void fillMap() {
	categories = new HashMap<>(4);
	categories.put(C.CategoryType.GOOGLE_SECTOR, google_sector);
	categories.put(C.CategoryType.GOOGLE_INDUSTRY, google_industry);
	categories.put(C.CategoryType.YAHOO_SECTOR, yahoo_sector);
	categories.put(C.CategoryType.YAHOO_INDUSTRY, yahoo_industry);
	if (google_relatedCompanies != null)
	    categories.put(C.CategoryType.GOOGLE_RELATED_COMPANIES, google_relatedCompanies.toString());
    }

    /** cleans category name for file-safety*/
    public Profile_EraDataRow(GoogleProfile gp, YahooProfile yp) throws G.No_DiskData_Exception {
	if (yp == null && gp == null)
	    throw new G.No_DiskData_Exception();
	if (gp != null) {
	    google_sector = C.getCleanedCategoryName(gp.sector);
	    google_industry = C.getCleanedCategoryName(gp.industry);

	    google_relatedCompanies = gp.relatedCompanies;
	}
	if (yp != null) {
	    yahoo_sector = C.getCleanedCategoryName(yp.sector);
	    yahoo_industry = C.getCleanedCategoryName(yp.industry);
	}

	fillMap();
    }

    @Override
    public String toOutputStringEDR() {

	String str = ""
		+ G.parse_Str(google_relatedCompanies, G.edrSubDelim) + G.edrDelim
		+ G.parse_Str(google_sector) + G.edrDelim
		+ G.parse_Str(google_industry) + G.edrDelim
		+ G.parse_Str(yahoo_sector) + G.edrDelim
		+ G.parse_Str(yahoo_industry);
//	System.out.println(str);
	return str;
    }

    /** for assimilate values -- for reading from file.  THIS IS A LITTLE SKETCHY */
    public Profile_EraDataRow() {
    }

    @Override
    public void assimilateValues(String[] ar) {
	google_relatedCompanies = G.parse_ListString(ar[0], G.edrSubDelim);
	google_sector = ar[1];
	google_industry = ar[2];
	yahoo_sector = ar[3];
	yahoo_industry = ar[4];

	fillMap();

    }

    /** returns map (key: ticker, value: profile_eradatarow) for all profiles*/
    public static Map<String, Profile_EraDataRow> readAllFromDisk() throws IOException, FileNotFoundException, InstantiationException, IllegalAccessException, ParseException {

	Map<String, Profile_EraDataRow> map = new LinkedHashMap();

	File dir = G.XProfile_eras;

	for (File file : dir.listFiles()) {
	    List<Era> eras = Era.readFromDisk(file, Profile_EraDataRow.class);
	    Era era = eras.get(0);								//cos each file has only 1 era right now!  TODO change when start archiving quarterly eras!
	    Profile_EraDataRow pedr = (Profile_EraDataRow)era.eraDataRow;
	    String ticker = G.getTickFromFile(file);
	    map.put(ticker, pedr);

	}
	return map;
    }
}
