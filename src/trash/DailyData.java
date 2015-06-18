//package trash;
//
//import stuff.objects.Stock;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import stuff.G;
//import static stuff.G.INDUSTRY;
//import stuff.G.My_null_exception;
//import static stuff.G.SECTOR;
//import static stuff.G.get;
//import static stuff.G.getIndex;
//import static stuff.G.is_weird;
//import static stuff.G.isnull;
//import static stuff.G.myprint;
//import static stuff.G.newChildTickerFile;
//import static stuff.G.new_filled_array;
//import static stuff.G.new_null_float_ar;
//import static stuff.G.null_float;
//import static stuff.G.null_int;
//import static stuff.G.parse_Str;
//import static stuff.G.parse_Str_sciDf;
//import static stuff.G.parse_float;
//import static stuff.G.sbDelimiterAppend;
//import static stuff.Make_Squished.weatherMap_NY;
//import stuff.objects.Person;
//import stuff.objects.Prices.HistoricalPrices_;
//
//public class DailyData {
//    Stock stock;
//    int len;
//
//    public float[] open, high, low, close;
//    public int[] date, volume;
//
//    /* Custom Indicators -- Storing to compare with sector and industry aves*/
//    public float[] ag_3;
//    public float[] ema3, ema5, ema9, ema20, ema50, ema200;
//    public float[] ema3v5, ema3v9, ema3v20, ema3v50, ema3v200;
//    public float[] PE, PEG, PB, PC;
//    public float[] ch_day, ch_week, ch_month, ch_3month, ch_6month, ch_year;
//
//
//    /** includes String ticker */
//    public List<VarOld> varObjects;
//    public List<VarOld> accVarObjects;
//
//    public int dateStartOffset;	    //getting rid of this
//    int num_accVars;
//
//    DailyData() {
//	initializeACCs();
//	initialize_varObjects();
//	initialize_accVarObjects(); //
//    }
//
//    public DailyData(HistoricalPrices_ hp, Stock stock) {
//	this.open = hp.open;
//	this.high = hp.high;
//	this.low = hp.low;
//	this.close = hp.close;
//	this.date = hp.date;
//	this.volume = hp.volume;
//	this.dateStartOffset = hp.dateStartOffset;
//
//	len = this.date.length;
//
//	this.stock = stock;
//	initializeACCs();
//	initialize_varObjects(); //
//	initialize_accVarObjects(); //
//    }
//
//    /** initialize asset class comparator variables to be full len-length arrays of null_float */
//    private void initializeACCs() {
//	ag_3 = new_null_float_ar(len);
//	ema3 = new_null_float_ar(len);
//	ema5 = new_null_float_ar(len);
//	ema9 = new_null_float_ar(len);
//	ema20 = new_null_float_ar(len);
//	ema50 = new_null_float_ar(len);
//	ema200 = new_null_float_ar(len);
//	ema3v5 = new_null_float_ar(len);
//	ema3v9 = new_null_float_ar(len);
//	ema3v20 = new_null_float_ar(len);
//	ema3v50 = new_null_float_ar(len);
//	ema3v200 = new_null_float_ar(len);
//	PE = new_null_float_ar(len);
//	PEG = new_null_float_ar(len);
//	PB = new_null_float_ar(len);
//	PC = new_null_float_ar(len);
//	ch_day = new_null_float_ar(len);
//	ch_week = new_null_float_ar(len);
//	ch_month = new_null_float_ar(len);
//	ch_3month = new_null_float_ar(len);
//	ch_6month = new_null_float_ar(len);
//	ch_year = new_null_float_ar(len);
//    }
//
//    /** initialize varObjects and accVarObjects */
//    private void initialize_varObjects() {
//	varObjects = new ArrayList();
//
//	// i think the ag_3 parameter can be avoided using getClass and fields[]
//	//the name of the method must equal the name of the variable, if one is pre-made
//	varObjects.add(new ticker(-1, -1, null));					    //-1, and others size - 1, cos of String here, cant go in Double Matrix 
//	varObjects.add(new date(varObjects.size() - 1, -1, date));
//	varObjects.add(new close(varObjects.size() - 1, -1, close));
//	varObjects.add(new volume(varObjects.size() - 1, -1, volume));
//	varObjects.add(new ave_volume_2wk(varObjects.size() - 1, -1));
//	varObjects.add(new ceo_compensation_1(varObjects.size() - 1, -1, null));
//	varObjects.add(new ceo_compensation_2(varObjects.size() - 1, -1, null));
//	varObjects.add(new ceo_compensation_3(varObjects.size() - 1, -1, null));
//	varObjects.add(new ceo_compensation_aveAll(varObjects.size() - 1, -1, null));
//	varObjects.add(new ceo_age_1(varObjects.size() - 1, -1, null));
//	varObjects.add(new ceo_age_2(varObjects.size() - 1, -1, null));
//	varObjects.add(new ceo_age_aveAll(varObjects.size() - 1, -1, null));
//	varObjects.add(new ceo_duration_1(varObjects.size() - 1, -1, null));
//	varObjects.add(new ceo_duration_2(varObjects.size() - 1, -1, null));
//	varObjects.add(new ceo_duration_aveAll(varObjects.size() - 1, -1, null));
//	varObjects.add(new numDaysUntilSecReport(varObjects.size() - 1, -1, null));
//	varObjects.add(new numDaysSinceSecReport(varObjects.size() - 1, -1, null));
//	varObjects.add(new shortInterest_daysSinceLast(varObjects.size() - 1, -1, null));
//	varObjects.add(new split1_daysSince(varObjects.size() - 1, -1, null));
//	varObjects.add(new split2_daysSince(varObjects.size() - 1, -1, null));
//	varObjects.add(new news_daysSince(varObjects.size() - 1, -1, null));
//	varObjects.add(new noaa_ny_prcp(varObjects.size() - 1, -1, null));
//	varObjects.add(new noaa_ny_tmax(varObjects.size() - 1, -1, null));
//	varObjects.add(new eps(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_intraDay(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_day(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_day2(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_2day(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_week(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_2week(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_month(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_3month(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_6month(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_year(varObjects.size() - 1, -1, null));
//	varObjects.add(new futCh_2year(varObjects.size() - 1, -1, null));
//	varObjects.add(new ag_3(varObjects.size() - 1, 0, ag_3));
//	varObjects.add(new ema3(varObjects.size() - 1, 1, ema3));
//	varObjects.add(new ema5(varObjects.size() - 1, 2, ema5));
//	varObjects.add(new ema9(varObjects.size() - 1, 3, ema9));	    //next/this time, there wont be arrays outside of these Var objects. Var will CONTAIN the array.  will that work?  how to deal w/ fact that sometimes the data var will be byte[], boolean[], etc?  idk but i think it will be okay.  worstcase, store extra variable whatTypeIsArray and use it to interact w/ the correct data array
//	varObjects.add(new ema20(varObjects.size() - 1, 4, ema20));	    //don't set column index upon construction.  let something else do that later, once it has all the containing
//	varObjects.add(new ema50(varObjects.size() - 1, 5, ema50));	    //	    X_ objects together.  it will set their column indices.  for flexibility
//	varObjects.add(new ema200(varObjects.size() - 1, 6, ema200));
//	varObjects.add(new ema3v5(varObjects.size() - 1, 7, ema3v5));	    //how to deal with acc????   those Var objects/lists/arrays will be created by their own X_ class
//	varObjects.add(new ema3v9(varObjects.size() - 1, 8, ema3v9));		//no need for special differentiation.  so does that mean i have to have a constructor for 
//	varObjects.add(new ema3v20(varObjects.size() - 1, 9, ema3v20));		//EVERY variable?  yes i think so.  
//	varObjects.add(new ema3v50(varObjects.size() - 1, 10, ema3v50));
//	varObjects.add(new ema3v200(varObjects.size() - 1, 11, ema3v200));	//asdf.getClass() gets you "...ema3v200" :)  - i can use this for column names in file if i want
//	varObjects.add(new PE(varObjects.size() - 1, 12, PE));
//	varObjects.add(new PEG(varObjects.size() - 1, 13, PEG));
//	varObjects.add(new PB(varObjects.size() - 1, 14, PB));
//	varObjects.add(new PC(varObjects.size() - 1, 15, PC));
//	varObjects.add(new ch_day(varObjects.size() - 1, 16, ch_day));
//	varObjects.add(new ch_week(varObjects.size() - 1, 17, ch_week));
//	varObjects.add(new ch_month(varObjects.size() - 1, 18, ch_month));
//	varObjects.add(new ch_3month(varObjects.size() - 1, 19, ch_3month));
//	varObjects.add(new ch_6month(varObjects.size() - 1, 20, ch_6month));
//	varObjects.add(new ch_year(varObjects.size() - 1, 21, ch_year));
//    }
//
//    /** asset class comparator variables.  those variables that will be compared to say, the sector average */
//    private void initialize_accVarObjects() {
//	accVarObjects = new ArrayList();
//	for (VarOld var : varObjects)
//	    if (var.isAssetClassComparator)
//		accVarObjects.add(var);
//	num_accVars = accVarObjects.size();
//    }
//
//    /** calculate asset class comparator indicators */
//    void calculateACCIndicators() throws My_null_exception {
//	for (VarOld accVar : accVarObjects)
//	    accVar.fillDataArray();
//    }
//
//    @Override public String toString() {
//	return len + " "
//		+ open.length + " " + high.length + " " + low.length + " " + close.length + " "
//		+ date.length + " " + volume.length + " "
//		+ ag_3.length + " " + ema20.length + " " + ema3v20.length + " "
//		+ PB.length + " " + ch_3month.length;
//    }
//
//
//    private int yesterday(int i) {
//	return daysAgo(i, 1);
//    }
//
//    public int daysAgo(int i, int numDaysAgo) {
//	return i + numDaysAgo;
//    }
//
//    private int tomorrow(int i) {
//	return daysFromNow(i, 1);
//    }
//
//    private int daysFromNow(int i, int numDaysFromNow) {
//	return i - numDaysFromNow;
//    }
//
//    private float today(float[] dataValuesWithDecreasingDates, int i) throws My_null_exception {
//	return get(dataValuesWithDecreasingDates, i);
//    }
//
//    private float yesterday(float[] dataValuesWithDecreasingDates, int i) throws My_null_exception {
//	return get(dataValuesWithDecreasingDates, yesterday(i));
//    }
//
//    private float daysAgo(int i, float[] dataValuesWithDecreasingDates, int numDaysAgo) throws My_null_exception {
//	return get(dataValuesWithDecreasingDates, daysAgo(i, numDaysAgo));
//    }
//
//    private float tomorrow(int i, float[] dataValuesWithDecreasingDates) throws My_null_exception {
//	return get(dataValuesWithDecreasingDates, tomorrow(i));
//    }
//
//    private float daysFromNow(int i, float[] dataValuesWithDecreasingDates, int numDaysFromNow) throws My_null_exception {
//	return get(dataValuesWithDecreasingDates, daysFromNow(i, numDaysFromNow));
//    }
//
//    void writeHeader(BufferedWriter bw) throws IOException {
//	String[] headerAr = getHeaderAr();
//	String c = ",";
//	for (String str : headerAr)
//	    myprint(bw, str, c);
//	myprintln(bw);
////	for (Var var : varObjects)
////	    myprint(bw, var.outputHeaderName, c);
////	for (Var accVar : accVarObjects) {
////	    myprint(bw, "vSr_" + accVar.outputHeaderName, c);
////	    myprint(bw, "vIy_" + accVar.outputHeaderName, c);
////	}
//    }
//
//    /** includes ticker */
//    String[] getHeaderAr() {
//	int length = varObjects.size() + 2 * accVarObjects.size();
//	String[] ar = new String[length];
//
//	int i = 0;
//
//	for (VarOld var : varObjects)
//	    ar[i++] = var.outputHeaderName;
//
//	for (VarOld accVar : accVarObjects) {
//	    ar[i++] = "vSr_" + accVar.outputHeaderName;
//	    ar[i++] = "vIy_" + accVar.outputHeaderName;
//	}
//	if (i != length) {
//	    System.out.println("error in getHeaderAr");
//	    System.exit(0);
//	}
//	return ar;
//    }
//
//    void writeData(BufferedWriter bw, Map<Short, Map<Integer, float[]>> sectori__date_acciAvesAr, Map<Short, Map<Integer, float[]>> industryi__date_acciAvesAr) throws IOException {
//	char c = ',';
//	List<VarOld> vars = stock.data.varObjects;
//	List<VarOld> accVars = stock.data.accVarObjects;
//
//
//	for (int datai = 0; datai < stock.data.len; datai++) {
//	    if (stock.data.goodData(datai)) {
//		StringBuilder sb = new StringBuilder();
//		for (VarOld var : vars)
//		    sbDelimiterAppend(sb, var.valueStr(datai), c);
//		if (stock.p != null && stock.p.isStock)
//		    for (VarOld accVar : accVars) {
//			sbDelimiterAppend(sb, accVar.valueVsAcStr(datai, sectori__date_acciAvesAr, SECTOR), c);
//			sbDelimiterAppend(sb, accVar.valueVsAcStr(datai, industryi__date_acciAvesAr, INDUSTRY), c);
//		    }
//		bw.write(sb.toString());
//		bw.write(String.format("%n"));
//	    }
//	}
//    }
//
//    void writeData(BufferedWriter bw) throws IOException {
//	char c = ',';
//
//	for (int datai = 0; datai < stock.data.len; datai++) {
//	    if (stock.data.goodData(datai)) {
//		StringBuilder sb = new StringBuilder();
//		for (VarOld var : stock.data.varObjects)
//		    sbDelimiterAppend(sb, var.valueStr(datai), c);
//		bw.write(sb.toString());
//		bw.write(String.format("%n"));
//	    }
//	}
//    }
//
//    private void myprintln(BufferedWriter bw) throws IOException {
//	bw.write(String.format("%n"));
//    }
//
//    void writeToDir(File dir) throws IOException {
//	File File = newChildTickerFile(dir, stock.ticker);
//
//	try (BufferedWriter bw = new BufferedWriter(new FileWriter(File))) {
//	    writeHeader(bw);
//	    writeData(bw);
//	}
//
//
//    }
//
//    public boolean goodData(int datai) {
//	boolean goodAveVol = false;
//	for (VarOld var : varObjects) {
//	    if (var.outputHeaderName.equals("ave_volume_2wk")) {
//		String aveVolStr = var.valueStr(datai);
//		float aveVol = parse_float(aveVolStr);
//		if (!isnull(aveVol) && aveVol > 10)
//		    goodAveVol = true;
//		break;
//	    }
//	}
//
//	return !isnull(close[datai]) && !isnull(volume[datai]) && volume[datai] > 80000 && goodAveVol;
//    }
//
//    public abstract class VarOld {
//	/** variable's Double matrix index as column */
//	public int vDMi;
//	public boolean isAssetClassComparator;
//	public int acci;
//	public float[] ar;
//	public int[] ar_int;
//	public String outputHeaderName;
////	public boolean isNumeric = true;
//
//	public VarOld(int var_i, int acci) {
//	    this.vDMi = var_i;
//	    this.isAssetClassComparator = acci >= 0;
//	    this.acci = acci;
//	    this.ar = null;
//	    outputHeaderName = this.getClass().toString().split("\\$")[1];
//	}
//
//	public VarOld(int var_i, int acci, float[] ar) {
//	    this.vDMi = var_i;
//	    this.isAssetClassComparator = acci >= 0;
//	    this.acci = acci;
//	    this.ar = ar;
//	    outputHeaderName = this.getClass().toString().split("\\$")[1];
//	}
//
//	public VarOld(int var_i, int acci, int[] ar) {
//	    this.vDMi = var_i;
//	    this.isAssetClassComparator = acci >= 0;
//	    this.acci = acci;
//	    this.ar_int = ar;							//could we have avoided having two (ar_int and ar) by calling them Object or soemthing...? idk maybe not
//	    outputHeaderName = this.getClass().toString().split("\\$")[1];
//	}
//
//	public void fillDataArray() throws My_null_exception {
//	}
//
//	//override this method in all extended objects that are not associated w an array in "data"
//	public String valueStr(int i) {
//	    return parse_Str(ar[i]);
//	}
//
//	/**TODO untested
//	 * @param datai
//	 * @param aci__date_acciDataAr_map_per_acCategory
//	 * @param SECTOR_OR_INDUSTRY
//	 * @return  */
//	public String valueVsAcStr(int datai, Map<Short, Map<Integer, float[]>> aci__date_acciDataAr_map_per_acCategory, int SECTOR_OR_INDUSTRY) {
//
//	    try {
//		float stockValue = ar[datai];
//		short ac_i_ofStock = stock.p.ac_i[SECTOR_OR_INDUSTRY];
//
//		float acValue = aci__date_acciDataAr_map_per_acCategory.get(ac_i_ofStock).get(get(date, datai))[acci];
//
//		float returner = (stockValue - acValue) / acValue;
//
////		System.out.println("### " + returner);
//
//		return parse_Str(returner);
//
//	    } catch (My_null_exception ex) {
//		ex.printStackTrace();
//		System.exit(0);
//	    }
//	    return null;
//	}
//    }
//
//
//    public class ticker extends VarOld {
//
//	private ticker(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return stock.ticker;
//	}
//    }
//
//    public class date extends VarOld {
//	private date(int var_i, int acci, int[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return parse_Str(date[i]);
//	}
//    }
//
//    public class close extends VarOld {
//	private close(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return parse_Str(close[i]);
//	}
//    }
//
//    public class volume extends VarOld {
//	private volume(int var_i, int acci, int[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return parse_Str_sciDf(volume[i]);
//	}
//    }
//
//    /* WASTEFUL opportunity to make more efficient */	//??
//    public class ave_volume_2wk extends VarOld {
//	private ave_volume_2wk(int var_i, int acci) {
//	    super(var_i, acci);
//	}
//
//	/* WASTEFUL opportunity to make more efficient */
//	@Override public String valueStr(int i) {
//	    String returner;
//	    try {
//		int count = 0, sum = 0;
//		for (int k = 0; k < 9; k++) {
//		    int value = get(volume, daysAgo(i, k));
//		    sum += value;
//		    count++;
//		}
//		returner = parse_Str_sciDf(sum / count);
//	    } catch (My_null_exception | ArrayIndexOutOfBoundsException e) {
//		returner = "";
//	    }
//	    return returner;
//	}
//    }
//
//    public class ceo_compensation_1 extends VarOld {
//	private ceo_compensation_1(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//
//	    try {
//		return parse_Str_sciDf(stock.managers.get(0).compensation);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public class ceo_compensation_2 extends VarOld {
//	private ceo_compensation_2(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		return parse_Str_sciDf(stock.managers.get(1).compensation);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public class ceo_compensation_3 extends VarOld {
//	private ceo_compensation_3(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		return parse_Str_sciDf(stock.managers.get(2).compensation);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public class ceo_compensation_aveAll extends VarOld {
//	private ceo_compensation_aveAll(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		float count = 0, sum = 0;
//		for (Person manager : stock.managers) {
//		    float comp = manager.compensation;
//		    if (!isnull(comp)) {
//			sum += comp;
//			count++;
//		    }
//		}
//		return parse_Str_sciDf(sum / count);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public class ceo_age_1 extends VarOld {
//	private ceo_age_1(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		return parse_Str(stock.managers.get(0).age);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public class ceo_age_2 extends VarOld {
//	private ceo_age_2(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		return parse_Str(stock.managers.get(1).age);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public class ceo_age_aveAll extends VarOld {
//	private ceo_age_aveAll(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		float count = 0, sum = 0;
//		for (Person manager : stock.managers) {
//		    float age = manager.age;
//		    if (!isnull(age)) {
//			sum += age;
//			count++;
//		    }
//		}
//		return parse_Str(sum / count);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public class ceo_duration_1 extends VarOld {
//	private ceo_duration_1(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override
//	public String valueStr(int i) {
//	    try {
//		return parse_Str(G.current_year - stock.managers.get(0).since);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public class ceo_duration_2 extends VarOld {
//	private ceo_duration_2(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override
//	public String valueStr(int i) {
//	    try {
//		return parse_Str(G.current_year - stock.managers.get(1).since);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public class ceo_duration_aveAll extends VarOld {
//	private ceo_duration_aveAll(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		float count = 0, sum = 0;
//		for (Person manager : stock.managers) {
//		    float since = manager.since;
//		    if (!isnull(since)) {
//			sum += G.current_year - since;
//			count++;
//		    }
//		}
//		return parse_Str(sum / count);
//	    } catch (Exception e) {
//		return "";
//	    }
//	}
//    }
//
//    public String daysSinceMostRecentEvent_Str(int datai, int[] eventDatesAr_decreasing) throws My_null_exception {
//	int local_null = -1;
//
//	if (eventDatesAr_decreasing == null)
//	    return "";
//	int thisdate = get(date, datai);
//
//	int theMostRecentEventDate = 0;
//	for (int k = 0; k < eventDatesAr_decreasing.length; k++)
//	    if (eventDatesAr_decreasing[k] <= thisdate) {
//		theMostRecentEventDate = eventDatesAr_decreasing[k];
//		break;
//	    }
//	int theMostRecentTradingDateAfterEvent_index = local_null;
//	try {
//	    theMostRecentTradingDateAfterEvent_index = getIndex(date, theMostRecentEventDate);
//	} catch (G.Value_Not_In_Array_Exception e) {
//	    for (int k = 0; k < date.length; k++) {
//		if (date[k] == theMostRecentEventDate) {
//		    theMostRecentTradingDateAfterEvent_index = k;
//		    break;
//		}
//		if (date[k] < theMostRecentEventDate) {
//		    theMostRecentTradingDateAfterEvent_index = k - 1;
//		    break;
//		}
//	    }
//	}
//
//
//	int m = theMostRecentTradingDateAfterEvent_index;
//	if (m == local_null) return "";
//
//	return parse_Str(datai <= m ? m - datai : null_int);
//    }
//
//    public String daysUntilNextEvent_Str(int datei, int[] eventDatesAr_decreasing) throws G.Value_Not_In_Array_Exception, My_null_exception {
//	if (eventDatesAr_decreasing == null)
//	    return "";
//
//	int thisdate = get(date, datei);
//
//	int theNextEventDate = 0;
//	for (int k = eventDatesAr_decreasing.length - 1; k >= 0; k--)
//	    if (eventDatesAr_decreasing[k] >= thisdate) {
//		theNextEventDate = eventDatesAr_decreasing[k];
//		break;
//	    }
//
//
//	int theMostRecentTradingDateBeforeEvent_index = Integer.MAX_VALUE;
//	try {
//	    theMostRecentTradingDateBeforeEvent_index = getIndex(date, theNextEventDate);	//?? is minus one correct here?
//	} catch (G.Value_Not_In_Array_Exception e) {
//	    for (int k = date.length - 1; k >= 0; k--) {
//		if (date[k] > theNextEventDate) {
//		    theMostRecentTradingDateBeforeEvent_index = k + 1;
//		    break;
//		}
//	    }
//	}
//
//	int m = theMostRecentTradingDateBeforeEvent_index;
//
//	return parse_Str(datei >= m ? datei - m : null_int);
//    }
//
//    /* WASTEFUL opportunity to make more efficient */
//    public class shortInterest_daysSinceLast extends VarOld {
//	private shortInterest_daysSinceLast(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	/* WASTEFUL opportunity to make more efficient */
//	@Override public String valueStr(int i) {
//	    try {
//		if (stock.si == null)
//		    return "";
//		return daysSinceMostRecentEvent_Str(i, stock.si.settlementDates);
//	    } catch (My_null_exception ex) {
//		ex.printStackTrace();
//		System.out.println(stock.ticker + " " + date[i]);
//		System.exit(0);
//	    }
//	    return null;
//	}
//    }
//
//    public class split1_daysSince extends VarOld {
//	private split1_daysSince(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		if (stock.splits == null || stock.splits.split1dates == null || stock.splits.split1dates.length == 0)
//		    return "";
//		return daysSinceMostRecentEvent_Str(i, stock.splits.split1dates);
//	    } catch (My_null_exception ex) {
//		ex.printStackTrace();
//		System.out.println(stock.ticker + " " + date[i]);
//		System.exit(0);
//	    }
//	    return null;
//	}
//
//    }
//
//    public class split2_daysSince extends VarOld {
//	private split2_daysSince(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		if (stock.splits == null || stock.splits.split2dates == null || stock.splits.split2dates.length == 0)
//		    return "";
//		return daysSinceMostRecentEvent_Str(i, stock.splits.split2dates);
//	    } catch (My_null_exception ex) {
//		ex.printStackTrace();
//		System.out.println(stock.ticker + " " + date[i]);
//		System.exit(0);
//	    }
//	    return null;
//	}
//
//    }
//
//    public class news_daysSince extends VarOld {
//	private news_daysSince(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    try {
//		if (stock.newsDates == null)
//		    return "";
//		return daysSinceMostRecentEvent_Str(i, stock.newsDates);
//	    } catch (My_null_exception ex) {
//		ex.printStackTrace();
//		System.out.println(stock.ticker + " " + date[i]);
//		System.exit(0);
//	    }
//	    return null;
//	}
//
//    }
//
//    public class noaa_ny_prcp extends VarOld {
//	private noaa_ny_prcp(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override
//	public String valueStr(int i) {
//	    try {
//		float thisdate = get(date, i);
//		return parse_Str(weatherMap_NY.get(Math.round(thisdate)).prcp);
//	    } catch (Exception ex) {
//		return "";
//	    }
//	}
//    }
//
//    public class noaa_ny_tmax extends VarOld {
//	private noaa_ny_tmax(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override
//	public String valueStr(int i) {
//	    try {
//		float thisdate = get(date, i);
//		return parse_Str(weatherMap_NY.get(Math.round(thisdate)).tmax);
//	    } catch (Exception ex) {
//		return "";
//	    }
//	}
//    }
//
//    public class numDaysUntilSecReport extends VarOld {
//	private numDaysUntilSecReport(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override
//	public String valueStr(int i) {
//	    try {
//		return daysUntilNextEvent_Str(i, stock.secDates);
//	    } catch (Exception ex) {
//		ex.printStackTrace();
//		System.out.println("### " + stock.ticker + " " + date[i]);
//		System.exit(0);
//	    }
//	    return null;
//	}
//    }
//
//    public class numDaysSinceSecReport extends VarOld {
//	private numDaysSinceSecReport(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override
//	public String valueStr(int i) {
//	    try {
//		return daysSinceMostRecentEvent_Str(i, stock.secDates);
//	    } catch (Exception ex) {
//		ex.printStackTrace();
//		System.out.println(stock.ticker + " " + date[i]);
//		System.exit(0);
//	    }
//	    return null;
//	}
//    }
//
//    public class eps extends VarOld {
//	private eps(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override
//	public String valueStr(int i) {
//	    try {
//		return parse_Str(getFundamentalValue(i, stock.f.eps));
//	    } catch (Exception ex) {
//		return "";
//	    }
//	}
//    }
//
//    public String get_futCh_i_duration(int i, int duration) {
//	return get_futCh_i_duration(i, 0, duration);
//    }
//
//    /* this is so we can get the pct change of a time span in the future.  not starting with today.*/
//    public String get_futCh_i_duration(int i, int wait_duration, int duration) {
//	try {
//	    return parse_Str(G.pctChange(get(close, daysFromNow(i, duration)), get(close, daysFromNow(i, wait_duration))));
//	} catch (Exception ex) {
//	    return "";
//	}
//    }
//
//    public class futCh_intraDay extends VarOld {
//	private futCh_intraDay(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override
//	public String valueStr(int i) {
//	    try {
//		return parse_Str(G.pctChange(get(close, daysFromNow(i, 1)), get(open, daysFromNow(i, 1))));
//	    } catch (Exception ex) {
//		return "";
//	    }
//	}
//    }
//
//    public class futCh_day extends VarOld {
//	private futCh_day(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 1);
//	}
//
//    }
//    
//
//    public class futCh_day2 extends VarOld {
//	private futCh_day2(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 1, 2);
//	}
//    }
//
//    public class futCh_2day extends VarOld {
//	private futCh_2day(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 2);
//	}
//    }
//
//    public class futCh_week extends VarOld {
//	private futCh_week(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 5);
//	}
//    }
//
//    public class futCh_2week extends VarOld {
//	private futCh_2week(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 10);
//	}
//    }
//
//    public class futCh_month extends VarOld {
//	private futCh_month(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 20);
//	}
//    }
//
//    public class futCh_3month extends VarOld {
//	private futCh_3month(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 60);
//	}
//    }
//
//    public class futCh_6month extends VarOld {
//	private futCh_6month(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 120);
//	}
//    }
//
//    public class futCh_year extends VarOld {
//	private futCh_year(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 240);
//	}
//    }
//
//    public class futCh_2year extends VarOld {
//	private futCh_2year(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public String valueStr(int i) {
//	    return get_futCh_i_duration(i, 480);
//	}
//    }
//
//    public class ag_3 extends VarOld {
//
//	private ag_3(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__ag_3();
//	}
//    }
//
//    public class ema3 extends VarOld {
//	private ema3(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__ema(ema3, 3);
//	}
//    }
//
//    public class ema5 extends VarOld {
//	private ema5(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__ema(ema5, 5);
//	}
//    }
//
//    public class ema9 extends VarOld {
//	private ema9(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	    
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__ema(ema9, 9);
//	}
//    }
//
//    public class ema20 extends VarOld {
//	private ema20(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__ema(ema20, 20);
//	}
//    }
//
//    public class ema50 extends VarOld {
//	private ema50(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {	//this won't exist.  data will be created upon construction
//	    calculate__ema(ema50, 50);
//	}
//    }
//
//    public class ema200 extends VarOld {
//	private ema200(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__ema(ema200, 200);
//	}
//    }
//
//    public class ema3v5 extends VarOld {
//	private ema3v5(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__emaXvY(ema3, ema5, ema3v5);
//	}
//    }
//
//    public class ema3v9 extends VarOld {
//	private ema3v9(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__emaXvY(ema3, ema9, ema3v9);
//	}
//    }
//
//    public class ema3v20 extends VarOld {
//	private ema3v20(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__emaXvY(ema3, ema20, ema3v20);
//	}
//    }
//
//    public class ema3v50 extends VarOld {
//	private ema3v50(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__emaXvY(ema3, ema50, ema3v50);
//	}
//    }
//
//    public class ema3v200 extends VarOld {
//	private ema3v200(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__emaXvY(ema3, ema200, ema3v200);
//	}
//    }
//
//    public class PE extends VarOld {
//	private PE(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__PX(PE, stock.f.eps);
//	}
//    }
//
//    public class PEG extends VarOld {
//	private PEG(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__PEG();
//	}
//    }
//
//    public class PB extends VarOld {
//	private PB(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__PX(PB, stock.f.bps);
//	}
//    }
//
//    public class PC extends VarOld {
//	private PC(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__PX(PC, stock.f.cps);
//	}
//    }
//
//    public class ch_day extends VarOld {
//	private ch_day(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__pastPctCh(ch_day, 1);
//	}
//    }
//
//    public class ch_week extends VarOld {
//	private ch_week(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__pastPctCh(ch_week, 5);
//	}
//    }
//
//    public class ch_month extends VarOld {
//	private ch_month(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__pastPctCh(ch_month, 20);
//	}
//    }
//
//    public class ch_3month extends VarOld {
//	private ch_3month(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__pastPctCh(ch_3month, 60);
//	}
//    }
//
//    public class ch_6month extends VarOld {
//	private ch_6month(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__pastPctCh(ch_6month, 120);
//	}
//    }
//
//    public class ch_year extends VarOld {
//	private ch_year(int var_i, int acci, float[] ar) {
//	    super(var_i, acci, ar);
//	}
//
//	@Override public void fillDataArray() throws My_null_exception {
//	    calculate__pastPctCh(ch_year, 240);
//	}
//    }
//
//
//    private void calculate__ag_3() throws My_null_exception {
//	float[] price_diff = new_filled_array(null_float, len);
//
//	for (int i = 0; i < len - 1; i++)
//	    if (!isnull(close, i) && !isnull(close, yesterday(i)))
//		price_diff[i] = today(close, i) - yesterday(close, i);
//
//	int duration = 3;
//	for (int i = 0; i < len - duration; i++) {
//	    try {
//		if (isnull(ag_3, i)) {
//		    float sum = 0;
//		    for (int k = i; k < i + duration; k++) {
//			float change = get(price_diff, k);		    //throws here
//			sum += change;
//		    }
//		    ag_3[i] = sum / duration;
//		} else {
//		    float currentGain = 0;
//		    float change = get(price_diff, i);
//		    if (change > 0) {
//			currentGain = change;
//		    }
//		    ag_3[i] = (yesterday(ag_3, i) * (duration - 1) + currentGain) / duration;
//		}
//	    } catch (G.My_null_exception e) {
//	    }
//	}
//    }
//
//    private void calculate__ema(float[] emaX, int timePeriod) {
////	    System.out.println("here");
//	//	     SMA: 10 period sum / 10 
//	//	     Multiplier: (2 / (Time periods + 1) ) = (2 / (10 + 1) ) = 0.1818 (18.18%) ..... EMA_multiplier(duration)
//	//	     EMA: {Close - EMA(previous day)} x multiplier + EMA(previous day). 
//
//	for (int i = len - timePeriod; i >= 0; i--) {	//is it wasteful to repeat loop for each var?
//	    try {
//		float previousDayValue;
//		if (isnull(emaX, yesterday(i))) {
//		    float sum = 0;
//		    float count = 0;
//		    for (int k = i; k < i + timePeriod; k++) {
//			sum += get(close, k);
//			count++;
//		    }
//		    previousDayValue = sum / count;
//		} else {
//		    previousDayValue = yesterday(emaX, i);
//		}
//		emaX[i] = (get(close, i) - previousDayValue) * EMA_multiplier(timePeriod) + previousDayValue;
//		if (is_weird(get(emaX, i))) {
//		    emaX[i] = null_float;
//		}
//	    } catch (G.My_null_exception e) {
//	    }
//	}
//    }
//
//
//    private static float EMA_multiplier(int timePeriod) {
//	return 2f / (timePeriod + 1f);
//    }
//
//    private void calculate__emaXvY(float[] emaX, float[] emaY, float[] emaXvY) throws My_null_exception {
//
//	for (int i = 0; i < len; i++) {
//	    if (!isnull(emaX, i) && !isnull(emaY, i)) {
//		emaXvY[i] = (get(emaX, i) - get(emaY, i)) / get(emaY, i);
//		emaXvY[i] = get(emaXvY, i) * 100;
//	    }
//	}
//    }
//
//    //TODO untested
//    private float getFundamentalValue(int data_i, float[] ms_vals) throws G.My_null_exception {
//
//	//array of ms dates
//	//pick ms_date such that date is:
//	//	within 1 year prior to (firstOfTHeMonth(msDate)) 
//
//	for (int k = 0; k < stock.f.date.length; k++) {
//	    if (!isnull(stock.f.date, k)) {
//		int msDate = get(stock.f.date, k);
//		int msEndDate = null_int;
//		int endDate_arithmetic = getIntDate_firstOfNextMonth(getIntDate_nextYear(msDate));
//		if (k + 1 < stock.f.date.length && !isnull(stock.f.date, k + 1)) {
//		    int next_msDate = get(stock.f.date, k + 1);
//		    int endDate_nextSec = getIntDate_firstOfNextMonth(next_msDate);
//		    msEndDate = Math.min(endDate_arithmetic, endDate_nextSec);
//		}
//		int msStartDate = getIntDate_firstOfNextMonth(msDate);
//		if (isnull(msEndDate))
//		    msEndDate = endDate_arithmetic;
//
//		float dailyDataDate = get(date, data_i);
//
//		if (dailyDataDate >= msStartDate && dailyDataDate <= msEndDate) {
//		    return get(ms_vals, k);
//		}
//	    }
//	}
//	throw new G.My_null_exception("null float");
//    }
//
//    private int getIntDate_nextYear(int x) {
//	int year = year(x);
//	int month = month(x);
//	int day = day(x);
//
//	year++;
//
//	if (day == 29 && month == 2)
//	    day = 28;
//
//	return dateInt(year, month, day);
//    }
//
//    private int getIntDate_aYearAgo(int x) {
//	int year = year(x);
//	int month = month(x);
//	int day = day(x);
//
//	year--;
//
//	if (day == 29 && month == 2)
//	    day = 28;
//
//	return dateInt(year, month, day);
//    }
//
//    private int getIntDate_firstOfLastMonth(int x) {
//	int year = year(x);
//	int month = month(x);
//	int day = 1;
//
//	month--;
//	if (month == 0) {
//	    month = 12;
//	    year--;
//	}
//	return dateInt(year, month, day);
//    }
//
//    private int getIntDate_firstOfThisMonth(int x) {
//	int year = year(x);
//	int month = month(x);
//	int day = 1;
//
//	return dateInt(year, month, day);
//    }
//
//    private int getIntDate_firstOfNextMonth(int x) {
//	int year = year(x);
//	int month = month(x);
//	int day = 1;
//
//	month++;
//	if (month == 13) {
//	    month = 1;
//	    year++;
//	}
//
//	return dateInt(year, month, day);
//    }
//
//    private int year(int x) {
//	return x / 10000;
//    }
//
//    private int month(int x) {
//	return (x / 100) % 100;
//    }
//
//    private int day(int x) {
//	return x % 100;
//    }
//
//    private int dateInt(int year, int month, int day) {
//	return year * 10000 + month * 100 + day;
//    }
//
//
//    private void calculate__PX(float[] PX, float[] ms_xpsAr) {
//
//	if (ms_xpsAr != null)
//	    for (int i = 0; i < len; i++) {
//		try {
//		    float xps = getFundamentalValue(i, ms_xpsAr);
//		    if (!isnull(close, i) && !isnull(xps))
//			PX[i] = get(close, i) / xps;
//		} catch (My_null_exception ex) {
//		}
//	    }
//    }
//
//    private void calculate__PEG() {
//	if (stock.f.eps != null)
//	    for (int i = 0; i < len; i++) {
//		try {
//		    float eps = getFundamentalValue(i, stock.f.eps);
//		    float growth = getFundamentalValue(i, stock.f.epsGrowthPctYoy);
//		    if (!isnull(close, i) && !isnull(eps) && !isnull(growth))
//			PEG[i] = get(close, i) / eps / growth;
//		} catch (My_null_exception e) {
//		}
//	    }
//    }
//
//    private void calculate__pastPctCh(float[] ch_x, int nDays) {
//
//	for (int i = 0; i < len - nDays; i++) {
//	    try {
//		float current_close = get(close, i);
//		float past_close = daysAgo(i, close, nDays);
//		ch_x[i] = (current_close - past_close) / past_close;
//		ch_x[i] = get(ch_x, i) * 100;
//	    } catch (G.My_null_exception e) {
//	    }
//	}
//    }
//
//
//}
