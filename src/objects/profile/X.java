package objects.profile;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import objects.Stock;
import utilities.G;
import supers.Era;
import supers.SuperX;

/** if G.x_objects_to_use doens't contain this profile.x.class, then no category stuff will be done */
public final class X extends SuperX implements supers.SuperXInterface, supers.XInterface_uses_Eras {


    public X() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(Profile_EraDataRow.class);
//	fill_emptyVarsMap();
    }

    public X(String ticker) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(ticker, Profile_EraDataRow.class);
//	fill_emptyVarsMap();
    }

    public void calculate_data_origination() throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	fill_data__origination(this);
    }

    @Override
    public void setXFiles() throws IOException {
	setXFile_Eras();
    }

    @Override
    public void setXFile_Eras() throws IOException {
	erasFile = G.newChildTickerFile(G.XProfile_eras, ticker);
    }

    @Override
    public final void fill_data__origination(SuperX x) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	fill_eras();
    }

    @Override
    public void fill_eras() throws IOException, ParseException, G.No_DiskData_Exception {

	// there is only one era here .... in the future we will have past quaerters' data in other folders somehow.  deal w/ that later.  after we set up to archive quarterly

	int minDate = Era.getDefaultSingleEraMinDate();
	int maxDate = Era.getDefaultSingleEraMaxDate();

	GoogleProfile gp = null;
	try {
	    gp = GoogleProfile.readFromDisk(ticker);
	} catch (G.No_DiskData_Exception ex) {
	}

	YahooProfile yp = null;
	try {
	    yp = YahooProfile.readFromDisk(ticker);
	} catch (G.No_DiskData_Exception ex) {
	}

	Profile_EraDataRow pr = new Profile_EraDataRow(gp, yp);

	eras = new ArrayList();
	Era era = new Era(minDate, maxDate, pr);
	eras.add(era);
    }

    @Override //stays empty
    public void fill_varClasses() {
    }

    @Override //stays empty
    public void calculate_vars_origination(SuperX x, boolean doUpdateForCurrentPrices) {
    }

    @Override //stays empty
    public void setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(Stock stock) {
    }
}
