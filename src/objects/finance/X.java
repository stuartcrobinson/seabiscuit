package objects.finance;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import objects.Stock;
import utilities.G;
import static utilities.G.get;
import supers.Era;
import supers.SuperX;
import supers.Var;

/** this X calculates array data all at once, ignoring Var.calculateData(SuperX x).  but we want to keep it in the interface cos some stuff uses them, like prices */
public final class X extends SuperX implements supers.SuperXInterface, supers.XInterface_uses_Vars, supers.XInterface_uses_Eras {

    objects.prices.X prices_X;

    public X() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(Financial_EraDataRow.class);
	fill_emptyVarsMap();
    }

    public X(String ticker) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(ticker, Financial_EraDataRow.class);
	fill_emptyVarsMap();
    }

    public void calculate_data_origination(objects.prices.X prices_X) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	setAttributes(prices_X);
	fill_data__origination(this);
    }

    public final void setAttributes(objects.prices.X prices_X) {
	this.prices_X = prices_X;
    }

    @Override
    public void setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(Stock stock) {
	prices_X = stock.prices_X;
    }

    @Override
    public void setXFiles() throws IOException {
	setXFile_vars();
	setXFile_Eras();
    }

    @Override
    public void setXFile_vars() throws IOException {
	varsFile = G.newChildTickerFile(G.XFinancials_vars, ticker);
    }

    @Override
    public void setXFile_Eras() throws IOException {
	erasFile = G.newChildTickerFile(G.XFinancials_eras, ticker);
    }

    @Override
    public void fill_data__origination(SuperX x) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	try {
	    fill_eras();    //must build eras before vars! (needed by vars)
	    fill_vars(x, length(prices_X));  //vars map requires data in eras for calculations (eras are the financial/key-ratios data!)
	} catch (G.No_DiskData_Exception ex) {
	    throw new G.No_DiskData_Exception();
	}
    }

    @Override
    public void fill_varClasses() {
	varClasses.add(PE.class);
	varClasses.add(PB.class);
	varClasses.add(PFCF.class);
	varClasses.add(cap.class);
    }

    @Override
    public void fill_eras() throws IOException, ParseException, IllegalAccessException, G.No_DiskData_Exception {

	List<Era> erasAnnual = null;
	try {
	    erasAnnual = getErasFromFinancials(G.getMsAnnualFile(ticker));
	} catch (G.No_DiskData_Exception ex) {
	}
	List<Era> erasQuarterly = null;
	try {										    //comment this stuff out to use only annual numbers
	    erasQuarterly = getErasFromFinancials(G.getMsQuarterlyFile(ticker));
	    adjustQuarterlyDataToYearlyUnits(erasQuarterly);
	} catch (G.No_DiskData_Exception ex) {
	}

	eras = Era.combineButPreferTheFirstOne(erasQuarterly, erasAnnual);
	if (eras == null || eras.isEmpty())
	    throw new G.No_DiskData_Exception();
	Collections.sort(eras, Collections.reverseOrder());	    //reverse - most recent dates first
    }

    private List<Era> getErasFromFinancials(File file) throws IOException, ParseException, IllegalArgumentException, IllegalAccessException, G.No_DiskData_Exception {
	if (!file.exists()) throw new G.No_DiskData_Exception();
	MsFinancialDownload msdl = new MsFinancialDownload(file);
	if (msdl.yearmo == null) throw new G.No_DiskData_Exception();				    //cos of empty files.
	return msdl.getErasForX();
	//TODO there were problems here because i'm downloading empty MS files.  future improvement:  delete all empty data files before loading 
    }

    /* things needed for calculating arrays */

    @Override @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public void calculate_vars_origination(SuperX x, boolean doUpdateForCurrentPrices) {
	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

	float[] close = close(prices_X);
	int[] date = date(prices_X);

	float[] _PE = x.vars.get(X.PE.class).ar_float;
	float[] _PB = x.vars.get(X.PB.class).ar_float;
	float[] _PFCF = x.vars.get(X.PFCF.class).ar_float;
	float[] _cap = x.vars.get(X.cap.class).ar_float;

	for (int i = 0; i < Math.min(iterMax, length(prices_X)); i++) {
	    try {
		int dateInt = get(date, i);
		Financial_EraDataRow f_era = (Financial_EraDataRow)Era.getEraDataRow(x.eras, dateInt);	//could be null

		if (f_era == null) {
		    if (dateInt < Era.getSmallestMinDate(x.eras))
			break;
		    continue;
		}
		_PE[i] = get(close, i) / f_era.eps;
		_PB[i] = get(close, i) / f_era.bps;
		_PFCF[i] = get(close, i) / f_era.fcfps;
		_cap[i] = get(close, i) * f_era.shares;

	    } catch (G.My_null_exception ex) {
	    }
	}
    }

    /** as in, multiply quarterly EPS by 4 to use it as an alternative to yearly eps */
    private void adjustQuarterlyDataToYearlyUnits(List<Era> erasQuarterly) throws IllegalArgumentException, IllegalAccessException {
	for (Era era : erasQuarterly) {
	    ((Financial_EraDataRow)era.eraDataRow).adjustQuarterlyNumbersToCompareToYearly();
	}
    }


    public static class PE extends Var {
	public PE() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class PB extends Var {
	public PB() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

    }

    public static class PFCF extends Var {
	public PFCF() {
	    arrayDataType = Var.Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class cap extends Var {
	public cap() {
	    arrayDataType = Var.Type.FLOAT;
	    isForCatAveComparison = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }


    public static class Financial_EraDataRow extends Era.EraDataRow {

	public float profitMargin;
	public float eps;
	public float bps;
	public float fcfps;
	public float shares;

	public Financial_EraDataRow(float profitMargin, float eps, float bps, float fcfps, float shares) throws IllegalArgumentException, IllegalAccessException {

	    this.profitMargin = profitMargin;
	    this.eps = eps;
	    this.bps = bps;
	    this.fcfps = fcfps;
	    this.shares = shares;

	    valuesMap = Era.EraDataRow.make_valuesMap(this);	    //this needs to be adjusted when quarterly data gets adjusted.
	}

	@Override
	public String toOutputStringEDR() {
	    return ""
		    + G.parse_Str(profitMargin) + G.edrDelim
		    + G.parse_Str(eps) + G.edrDelim
		    + G.parse_Str(bps) + G.edrDelim
		    + G.parse_Str(fcfps) + G.edrDelim
		    + G.parse_Str(shares);
	}

	/** for assimilateValues.  and testing.  why doesn't the constructor accept the String[] inputStrs and do the work there?  cos putting assimilateValues in the interface adds clarity and consistency */
	public Financial_EraDataRow() {
	}

	@Override
	public void assimilateValues(String[] ar) throws IllegalArgumentException, IllegalAccessException {
	    profitMargin = G.parse_float(ar[0]);
	    eps = G.parse_float(ar[1]);
	    bps = G.parse_float(ar[2]);
	    fcfps = G.parse_float(ar[3]);
	    shares = G.parse_float(ar[4]);

	    valuesMap = Era.EraDataRow.make_valuesMap(this);
	}

	private void adjustQuarterlyNumbersToCompareToYearly() throws IllegalArgumentException, IllegalAccessException {
	    eps *= 4;
	    fcfps *= 4;


	    valuesMap = Era.EraDataRow.make_valuesMap(this);	    //adjusted when quarterly data gets adjusted.
	}

    }
}
