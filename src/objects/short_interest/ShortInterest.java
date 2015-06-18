package objects.short_interest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import objects.Macro;
import objects.Macro_float;
import objects.Macro_int;
import objects.prices.Prices;
import utilities.G;
import static utilities.G.isnull;
import static utilities.G.parse_Str;
import static utilities.G.parse_float;
import static utilities.G.parse_int;

public class ShortInterest implements Comparable {

    static void setDisseminationDatesInSI_list(List<ShortInterest> sis, Map<Integer, Integer> map__settlementDate_disseminationDate) {
	for (ShortInterest si : sis) {
	    Integer dissDate = map__settlementDate_disseminationDate.get(si.settlementDate);
	    si.disseminationDate = dissDate == null ? G.null_int : dissDate;
	}
    }

    public static Macro getMacro(int minDate, TreeSet<Integer> validDates) throws ParseException {

//	TreeSet<Integer> validDates = null;
//	try {
//	    validDates = Prices.getValidDates(minDate, "AA", "AAPL", "AAON");
//	} catch (G.No_DiskData_Exception ex) {
//
//	    ex.printStackTrace();
//	    G.asdf("missing prices for aa or aapl or aaon.  necessary for building validdates set");
//	    System.exit(0);
//	}

	TreeSet<Integer> validDates_plusFutureDates = new TreeSet(validDates);

	for (int k = 0; k < 10; k++) {
	    int maxdate = validDates_plusFutureDates.first();
	    int nextBusinessDay = G.getNextWeekDay(maxdate);
	    validDates_plusFutureDates.add(nextBusinessDay);
	}

	int[] dates = G.new_null_int_ar(validDates_plusFutureDates.size());

	int i = 0;
	for (Integer validDate : validDates_plusFutureDates)
	    dates[i++] = validDate;

	TreeSet<ShortInterestCalendarRow> sirows = ShortInterestCalendarRow.getRows();

//	for (ShortInterestCalendarRow row : sirows) {
//	    row.println();
//	}

	Set<Integer> settlementDates = ShortInterestCalendarRow.getSettlementDates(sirows);
	Set<Integer> dueDates = ShortInterestCalendarRow.getDueDates(sirows);
	Set<Integer> disseminationDates = ShortInterestCalendarRow.getDisseminationDates(sirows);

//	G.asdf(settlementDates);
//	System.exit(0);

	Object[] nullExceptOn_settlementDate = Macro.getDatelyArray_nonNullOn_specificDates(dates, settlementDates);
	Object[] nullExceptOn_dueDate = Macro.getDatelyArray_nonNullOn_specificDates(dates, dueDates);
	Object[] nullExceptOn_disseminationDate = Macro.getDatelyArray_nonNullOn_specificDates(dates, disseminationDates);

	Map<String, int[]> dataArrays = new LinkedHashMap(6);
	dataArrays.put("m_SI_date", dates);
	dataArrays.put("m_daysUntilSettlementDate", G.calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(nullExceptOn_settlementDate));
	dataArrays.put("m_daysSinceSettlementDate", G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(nullExceptOn_settlementDate));
	dataArrays.put("m_daysUntilDueDate", G.calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(nullExceptOn_dueDate));
	dataArrays.put("m_daysSinceDueDate", G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(nullExceptOn_dueDate));
	dataArrays.put("m_daysUntilDisseminationDate", G.calculateNumIndicesUntilNonNullEventAr_forDecreasingDates(nullExceptOn_disseminationDate));
	dataArrays.put("m_daysSinceDisseminationDate", G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(nullExceptOn_disseminationDate));

	Macro shortInterestMacro = new Macro_int(Macro.Names.shortInterest, dates, dataArrays);

	return shortInterestMacro;
    }


    public int settlementDate;
    public int disseminationDate;
    public float shortInterest;
    public float avgDailyShareVolume;
    public float daysToCover;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    public ShortInterest(String lineSt) throws ParseException, G.No_DiskData_Exception {
	String[] line = lineSt.split(",");
	settlementDate = parse_int(line[0]);
	shortInterest = parse_float(line[1]);
	avgDailyShareVolume = parse_float(line[2]);
	daysToCover = parse_float(line[3]);
	if (isnull(settlementDate))
	    throw new G.No_DiskData_Exception();
    }

    public ShortInterest(int dateInt, float shortInterest, float aveDailyShareVol) {
	this.settlementDate = dateInt;
	this.shortInterest = shortInterest;
	this.avgDailyShareVolume = aveDailyShareVol;
	if (!isnull(this.avgDailyShareVolume) && !isnull(this.shortInterest))
	    this.daysToCover = this.shortInterest / this.avgDailyShareVolume;
	else
	    this.daysToCover = G.null_float;
    }

    public String outputLine() {
	return ""
		+ parse_Str(settlementDate) + ","
		+ parse_Str(shortInterest) + ","
		+ parse_Str(avgDailyShareVolume) + ","
		+ parse_Str(daysToCover);
    }

    @Override //reverse
    public int compareTo(Object o) {
	return Integer.compare(((ShortInterest)o).settlementDate, settlementDate);
    }

    public static List<ShortInterest> readFromDisk(String ticker) throws IOException, ParseException, G.No_DiskData_Exception {

	File file = G.getShortInterestFile(ticker);
	List<ShortInterest> sis = new ArrayList();
	if (file.exists()) {
	    List<String> lines = Files.readAllLines(file.toPath());
	    for (String line : lines)
		sis.add(new ShortInterest(line));
	}
	return sis;
    }

    public static void writeToDisk(List<ShortInterest> shortInterests, String ticker) throws IOException {

	File file = G.getShortInterestFile(ticker);

	if (!shortInterests.isEmpty())
	    Files.write(file.toPath(), ShortInterest.getOutputLines(shortInterests), StandardCharsets.UTF_8);

    }

    public static List<String> getOutputLines(List<ShortInterest> sis) {

	List<String> outputLines = new ArrayList();
	sis = removeDuplicates(sis);
	Collections.sort(sis);		//in reverse order of date

	for (ShortInterest si : sis) {
	    outputLines.add(si.outputLine());
	}
	return outputLines;
    }


    private static List<ShortInterest> removeDuplicates(List<ShortInterest> list) {
	List<Integer> indicesToRemove = new ArrayList();

	for (int A = 0; A < list.size(); A++) {
	    if (!indicesToRemove.contains(A)) {
		ShortInterest oA = list.get(A);

		for (int B = 0; B < list.size(); B++) {
		    if (!indicesToRemove.contains(B)) {
			ShortInterest oB = list.get(B);

			if (A != B && oA.settlementDate == oB.settlementDate)
			    indicesToRemove.add(B);
		    }
		}
	    }
	}

	if (G.listContainsDuplicates(indicesToRemove)) {
	    System.out.println("AAAGHGHGHGHGH indicesToRemove contains duplicates! \n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!"
		    + "\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!\n!!!!!"
		    + "wtf!!!!");
	    System.exit(0);
	}

	if (!indicesToRemove.isEmpty()) {
	    Collections.sort(indicesToRemove, Collections.reverseOrder());
	    System.out.println(indicesToRemove);
	    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%    REMOVING     %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	    System.out.println("found " + indicesToRemove.size() + " items to remove from list!");

	    for (int i : indicesToRemove) {
		System.out.println(list.get(i).outputLine() + "\n-----------");
		list.remove(i);
	    }
	}

	return list;
    }

    Integer getCookedDisseminationDate() throws ParseException {
//	return G.getNextWeekDay(settlementDate);
	if (isnull(disseminationDate))
	    return G.null_int;
	return G.getNextWeekDay(disseminationDate);
    }

    /** http://www.nyxdata.com/Data-Products/NYSE-MKT-Short-Interest */
    public static Map<Integer, Integer> getDisseminationDates_from_separateFile() {
	Map<Integer, Integer> map__settlementDate_disseminationDate = new LinkedHashMap();


	Set<ShortInterestCalendarRow> sirows = ShortInterestCalendarRow.getRows();

	for (ShortInterestCalendarRow sirow : sirows)
	    map__settlementDate_disseminationDate.put(sirow.settlementDate, sirow.disseminationDate);

	return map__settlementDate_disseminationDate;
    }

    public static int getSIDateFromSettlementDatesAndDissDatesFileDateStr(String date) throws ParseException {

	SimpleDateFormat sisdf1 = new SimpleDateFormat("MM/dd/yy");
	SimpleDateFormat sisdf2 = new SimpleDateFormat("MMM/dd/yy");

	try {
	    return Integer.parseInt(G.sdf_date.format(sisdf1.parse(date)));
	} catch (ParseException ex) {
	    return Integer.parseInt(G.sdf_date.format(sisdf2.parse(date)));
	}
    }

}
