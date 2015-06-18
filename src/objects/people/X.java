package objects.people;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import objects.Stock;
import utilities.G;
import supers.Era;
import supers.SuperX;

public final class X extends SuperX implements supers.XInterface_uses_Eras {


    public X() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(People_EraDataRow.class);
//	fill_emptyVarsMap();
    }

    public X(String ticker) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(ticker, People_EraDataRow.class);
//	fill_emptyVarsMap();
    }

    public void calculate_data_origination() throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
//	try {
	fill_data__origination(this);
//	} catch (G.No_Data_Exception ex) {
//	}
    }

    @Override
    public void setXFiles() throws IOException {
	setXFile_Eras();
    }

    @Override
    public void setXFile_Eras() throws IOException {
	erasFile = G.newChildTickerFile(G.XPeople_eras, ticker);
    }

    @Override
    public final void fill_data__origination(SuperX x) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	fill_eras();
    }

    @Override
    public void fill_eras() throws IOException, IllegalAccessException, G.No_DiskData_Exception {

	// there is only one era here .... in the future we will have past quaerters' data in other folders somehow.  deal w/ that later

	int minDate = Era.getDefaultSingleEraMinDate();
	int maxDate = Era.getDefaultSingleEraMaxDate();
	List<Person> people = Person.readPeopleFile(ticker);
	if (people.size() < 4)
	    throw new G.No_DiskData_Exception();


//	System.out.println("gergid8urgi ppl size: " + people.size());
	try {
	    People_EraDataRow datarow = new People_EraDataRow(people);

	    eras = new ArrayList();
	    Era era = new Era(minDate, maxDate, datarow);
	    eras.add(era);
	} catch (Exception e) {
	    System.out.println(ticker);
	    System.out.println(people.size() + " <-- people.size()");
	    e.printStackTrace();
	    System.exit(0);
	}
    }

    public static class People_EraDataRow extends Era.EraDataRow {
	//TempX --> People --> Ticker --> Era#.csv (dates, age1, age2, ageCEO, ageAvAll, duration1, durationCEO,  isFemale1, isFemale2, pctFemale5, pctFemaleAll, isFemaleCEO, 
	//pctPhD3, isCEO_PhD, pctMD3, isCEO_MD, pctDr3, isCEO_Dr, isCEO_Harvard, 
	//ceo_numConn_BoardMembers, ceo_numConn_Organizations, totalNumPeople, avgNumBoardRelationships (for ppl whose relationships is greater than 0 = board members), 
	//totalNumberBoardMembers

	//i just made all these public ... will that mess anythnig up?  public for valuesMap to recognize.
	public byte age1 = G.null_byte;
	public byte age2 = G.null_byte;
	public byte ageCEO = G.null_byte;
	public float ageAvAll = G.null_float;
	public byte duration1 = G.null_byte;
	public byte durationCEO = G.null_byte;
	public byte isFemale1 = G.null_byte;
	public byte isFemale2 = G.null_byte;
	public byte isFemaleCEO = G.null_byte;
	public float pctFemaleAll = G.null_float;
	public float pctPhD3 = G.null_float;
	public byte iSPhDCEO = G.null_byte;
	public float pctMD3 = G.null_float;
	public byte iSMDCEO = G.null_byte;
	public float pctDr3 = G.null_float;
	public byte isDrCEO = G.null_byte;
	public byte iSHarvardCEO = G.null_byte;
	public int ceo_numConn_BoardMembers = G.null_int;
	public int ceo_numConn_Organizations = G.null_int;
	public int totalNumPeople = G.null_int;
	public float avgNumBoardRelationships = G.null_float;
	public int totalNumBoardMembers = G.null_int;


	/** unwritten!.  TODO! set variables here.  calculate stuff from list of Persons*/
	private People_EraDataRow(List<Person> people) throws IllegalArgumentException, IllegalAccessException {
	    Person CEO = getCEO(people);

//	    System.out.println("@#R@#R " + people.size());
//	    System.out.println("@#R@#R " + people.get(0).outputLine());
//	    System.out.println("@#R@#R " + people.get(0).since);
//	    System.out.println("@#R@#R " + G.current_year);

	    duration1 = (byte)(G.current_year - people.get(0).since);
	    isFemale1 = G.parse_byte(people.get(0).isFemale);
	    isFemale2 = G.parse_byte(people.get(1).isFemale);

	    age1 = people.get(0).age;
	    age2 = people.get(1).age;
	    if (CEO != null) {
		ageCEO = CEO.age;
		durationCEO = (byte)(G.current_year - CEO.since);
		isFemaleCEO = G.parse_byte(CEO.isFemale);
		iSPhDCEO = G.parse_byte(CEO.isPhD);
		iSMDCEO = G.parse_byte(CEO.isMD);
		isDrCEO = G.parse_byte(CEO.isDr);
		if (CEO.bloomberg_EducationInfo != null)
		    iSHarvardCEO = G.parse_byte(CEO.bloomberg_EducationInfo.toLowerCase().contains("harvard"));
		ceo_numConn_BoardMembers = CEO.bloomberg_numConnections_boardMembers;
		ceo_numConn_Organizations = CEO.bloomberg_numConnections_organizations;
	    }
	    float sumAge = 0;
	    float sumIsFemale = 0;
	    float sumBoardMemberConnections = 0;

	    float count_all = 0;
	    float count_boardMembers = 0; //if board relationships > 0

	    /* for avgAll: age, females and numBoardConnections */

	    for (Person p : people) {
		count_all++;
		if (p.bloomberg_numConnections_boardMembers > 0) {
		    count_boardMembers++;
		    sumBoardMemberConnections += p.bloomberg_numConnections_boardMembers;
		}

		sumAge += p.age;
		sumIsFemale += p.isFemale ? 1 : 0;
	    }
	    ageAvAll = sumAge / count_all;
	    avgNumBoardRelationships = sumBoardMemberConnections / count_boardMembers;
	    totalNumBoardMembers = Math.round(count_boardMembers);
	    pctFemaleAll = 100 * sumIsFemale / count_all;


	    int count3 = 0;

	    float count_phd = 0;
	    float count_md = 0;
	    float count_dr = 0;

	    for (Person p : people) {
		count3++;
		if (p.isPhD)
		    count_phd++;
		if (p.isMD)
		    count_md++;
		if (p.isDr)
		    count_dr++;
		if (count3 == 3)
		    break;
	    }
	    pctPhD3 =100 *  count_phd / count3;
	    pctMD3 =100 *  count_md / count3;
	    pctDr3 =100 *  count_dr / count3;

	    totalNumPeople = people.size();


//	    //testing
//	    G.asdf("a43t3w4tg making ppl valuesmap!");
	    valuesMap = Era.EraDataRow.make_valuesMap(this);
	}

	@Override
	public String toOutputStringEDR() {
	    return ""
		    + G.parse_Str(age1) + G.edrDelim
		    + G.parse_Str(age2) + G.edrDelim
		    + G.parse_Str(ageCEO) + G.edrDelim
		    + G.parse_Str(ageAvAll) + G.edrDelim
		    + G.parse_Str(duration1) + G.edrDelim
		    + G.parse_Str(durationCEO) + G.edrDelim
		    + G.parse_Str(isFemale1) + G.edrDelim
		    + G.parse_Str(isFemale2) + G.edrDelim
		    + G.parse_Str(isFemaleCEO) + G.edrDelim
		    + G.parse_Str(pctFemaleAll) + G.edrDelim
		    + G.parse_Str(pctPhD3) + G.edrDelim
		    + G.parse_Str(iSPhDCEO) + G.edrDelim
		    + G.parse_Str(pctMD3) + G.edrDelim
		    + G.parse_Str(iSMDCEO) + G.edrDelim
		    + G.parse_Str(pctDr3) + G.edrDelim
		    + G.parse_Str(isDrCEO) + G.edrDelim
		    + G.parse_Str(iSHarvardCEO) + G.edrDelim
		    + G.parse_Str(ceo_numConn_BoardMembers) + G.edrDelim
		    + G.parse_Str(ceo_numConn_Organizations) + G.edrDelim
		    + G.parse_Str(totalNumPeople) + G.edrDelim
		    + G.parse_Str(avgNumBoardRelationships) + G.edrDelim
		    + G.parse_Str(totalNumBoardMembers);
	}

	@Override
	public void assimilateValues(String[] ar) throws IllegalArgumentException, IllegalAccessException {
	    age1 = G.parse_byte(ar[0]);
	    age2 = G.parse_byte(ar[1]);
	    ageCEO = G.parse_byte(ar[2]);
	    ageAvAll = G.parse_float(ar[3]);
	    duration1 = G.parse_byte(ar[4]);
	    durationCEO = G.parse_byte(ar[5]);
	    isFemale1 = G.parse_byte(ar[6]);
	    isFemale2 = G.parse_byte(ar[7]);
	    isFemaleCEO = G.parse_byte(ar[8]);
	    pctFemaleAll = G.parse_float(ar[9]);
	    pctPhD3 = G.parse_float(ar[10]);
	    iSPhDCEO = G.parse_byte(ar[11]);
	    pctMD3 = G.parse_float(ar[12]);
	    iSMDCEO = G.parse_byte(ar[13]);
	    pctDr3 = G.parse_float(ar[14]);
	    isDrCEO = G.parse_byte(ar[15]);
	    iSHarvardCEO = G.parse_byte(ar[16]);
	    ceo_numConn_BoardMembers = G.parse_int(ar[17]);
	    ceo_numConn_Organizations = G.parse_int(ar[18]);
	    totalNumPeople = G.parse_int(ar[19]);
	    avgNumBoardRelationships = G.parse_float(ar[20]);
	    totalNumBoardMembers = G.parse_int(ar[21]);


	    valuesMap = Era.EraDataRow.make_valuesMap(this);
	}

	public People_EraDataRow() {
	}

	/** null if not found */
	private Person getCEO(List<Person> people) {
	    for (Person p : people) {
		if (p.reuters_position == null)
		    return null;
//		try {
		if (p.reuters_position.toLowerCase().contains("chief executive officer"))
		    return p;
//		} catch (Exception e) {
//		    System.out.println(p.reuters_name);
//		    System.out.println(p.reuters_position);
//		    e.printStackTrace();
//		    System.exit(0);
//
//		}
	    }
	    return null;
	}
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
