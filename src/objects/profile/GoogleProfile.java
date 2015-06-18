package objects.profile;
import categories.C;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import utilities.G;

public class GoogleProfile {

    public String ticker;
    public List<String> relatedCompanies;
    public String sector;
    public String industry;

    /** from downloader */
    public GoogleProfile(String ticker, List<String> relatedCompanies, String sector, String industry) {
	this.ticker = ticker;
	this.sector = C.getCleanedCategoryName(sector);
	this.industry = C.getCleanedCategoryName(industry);
	this.relatedCompanies = relatedCompanies;
    }

    /** reads from disk.  <b>unchecked</b>  what should be default or missing values?  "" or null? from file */
    public GoogleProfile(String ticker) throws G.No_DiskData_Exception {
	sector = "";					    //probably pointless
	industry = "";
	try {
	    relatedCompanies = Files.readAllLines(G.getGoogleRelatedCompaniesFile(ticker).toPath());
	} catch (IOException ex) {
	}
	try {
	    List<String> sectorThenIndustry = null;
	    sectorThenIndustry = Files.readAllLines(G.getGoogleFactsetCategoriesFile(ticker).toPath());
	    sector = C.getCleanedCategoryName(sectorThenIndustry.get(0));
	    industry = C.getCleanedCategoryName(sectorThenIndustry.get(1));
	} catch (IOException ex) {
	}
	if (relatedCompanies == null && sector.isEmpty() && industry.isEmpty())
	    throw new G.No_DiskData_Exception();
    }

    /**writes nothing if no data */
    public void writeToFile() throws IOException {
	List<String> factSetSectorThenIndustry = new ArrayList();

	if (!relatedCompanies.isEmpty())
	    Files.write(G.getGoogleRelatedCompaniesFile(ticker).toPath(), relatedCompanies, StandardCharsets.UTF_8);
	if (!sector.isEmpty() || !industry.isEmpty()) {
	    factSetSectorThenIndustry.add(sector);
	    factSetSectorThenIndustry.add(industry);
	    Files.write(G.getGoogleFactsetCategoriesFile(ticker).toPath(), factSetSectorThenIndustry, StandardCharsets.UTF_8);
	}
    }

    /** for readability in profile.X.fill_eras() */
    public static GoogleProfile readFromDisk(String ticker) throws IOException, ParseException, G.No_DiskData_Exception {
	return new GoogleProfile(ticker);
    }

}
