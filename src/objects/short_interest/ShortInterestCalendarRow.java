package objects.short_interest;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import utilities.G;

public class ShortInterestCalendarRow implements Comparable {

    int settlementDate;
    int dueDate;
    int disseminationDate;

    static SimpleDateFormat MMsddsyy = new SimpleDateFormat("MM/dd/yy");
    static SimpleDateFormat MMM_dd_yyyy = new SimpleDateFormat("MMM dd yyyy");
    static SimpleDateFormat MMMsdd_yyyy = new SimpleDateFormat("MMM/dd yyyy");
    static SimpleDateFormat MMsdd_yyyy = new SimpleDateFormat("MM/dd yyyy");
    static SimpleDateFormat MMMhdd_yyyy = new SimpleDateFormat("MMM-dd yyyy");

    public static TreeSet<ShortInterestCalendarRow> getRows() {

	List<String> lines = null;
	try {
	    lines = Files.readAllLines(G.shortInterestDisseminationDatesFile.toPath());
	} catch (IOException ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}

	try {
	    TreeSet<ShortInterestCalendarRow> sirows = new TreeSet(Collections.reverseOrder());

	    for (String line : lines) {
//		G.asdf("asdfaewefefff " + line);
		if (line.contains("(")) continue;
		if (line.toLowerCase().contains("date")) continue;
		String[] ar = line.split("\t");

		for (int i = 0; i < ar.length; i++) {			    //removing citations like "[2]" in Nov[2]
		    if (ar[i].contains("["))
			ar[i] = ar[i].split("\\[")[0];
		    if (ar[i].trim().toLowerCase().equals("sept"))
			ar[i] = "sep";
		}

		if (ar.length < 4) continue;
		String yearstr = ar[0];
		int year = Integer.parseInt(yearstr);

		ShortInterestCalendarRow sirow = new ShortInterestCalendarRow();


		if (year >= 2014) {
		    sirow.settlementDate = getSiFileRowDate_for_2014_2015(yearstr, ar[1]);
		    sirow.dueDate = getSiFileRowDate_for_2014_2015(yearstr, ar[2]);
		    sirow.disseminationDate = getSiFileRowDate_for_2014_2015(yearstr, ar[3]);
		}
		if (year >= 2009 && year <= 2013) {
		    String monthstr = ar[1].toLowerCase();

		    sirow.settlementDate = getSiFileRowDate_for_2008_2013(yearstr, monthstr, ar[2]);
		    sirow.dueDate = getSiFileRowDate_for_2008_2013(yearstr, monthstr, ar[3]);
		    sirow.disseminationDate = getSiFileRowDate_for_2008_2013(yearstr, monthstr, ar[4]);
		}
		if (year <= 2008) {
		    String monthstr = ar[1].toLowerCase();

		    sirow.settlementDate = getSiFileRowDate_for_2008_2013(yearstr, monthstr, ar[3]);
		    sirow.dueDate = getSiFileRowDate_for_2008_2013(yearstr, monthstr, ar[4]);
		    sirow.disseminationDate = getSiFileRowDate_for_2008_2013(yearstr, monthstr, ar[5]);
		}
		sirows.add(sirow);
	    }
	    return sirows;
	} catch (ParseException ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
	return null;


    }

    public void println() {
	System.out.println("ge98rgu9rseug     " + settlementDate + "\t" + dueDate + "\t" + disseminationDate);
    }

    @Override
    public int compareTo(Object o) {
	return Integer.compare(settlementDate, ((ShortInterestCalendarRow)o).settlementDate);
    }


    /**first try "january 23"  then "January 23 2014" then "Jan-23" then cry */
    private static int getSiFileRowDate_for_2014_2015(String year, String st) throws ParseException {
	try {
	    return Integer.parseInt(G.sdf_date.format(MMM_dd_yyyy.parse(st + " " + year)));
	} catch (ParseException ex) {
	    try {
		return Integer.parseInt(G.sdf_date.format(MMM_dd_yyyy.parse(st)));
	    } catch (ParseException ex1) {
		return Integer.parseInt(G.sdf_date.format(MMMhdd_yyyy.parse(st + " " + year)));
	    }
	}
    }

    /** first try "MONTH CELL YEAR" (as "Feb 23 2013")  _____then_____ "CELL year" (as "9/4 2014") _____then_____ CELL (as 1/13/14) */
    private static int getSiFileRowDate_for_2008_2013(String yearstr, String monthstr, String st) throws ParseException {

	try {
	    return Integer.parseInt(G.sdf_date.format(MMM_dd_yyyy.parse(monthstr + " " + st + " " + yearstr)));
	} catch (ParseException ex) {
	    try {
		return Integer.parseInt(G.sdf_date.format(MMsdd_yyyy.parse(st + " " + yearstr)));
	    } catch (ParseException ex1) {
		return Integer.parseInt(G.sdf_date.format(MMsddsyy.parse(st)));
	    }
	}
    }

    static Set<Integer> getSettlementDates(TreeSet<ShortInterestCalendarRow> sirows) {
	Set<Integer> dates = new LinkedHashSet(sirows.size());
	for (ShortInterestCalendarRow row : sirows) {
	    dates.add(row.settlementDate);
	}
	return dates;
    }

    static Set<Integer> getDueDates(TreeSet<ShortInterestCalendarRow> sirows) {
	Set<Integer> dates = new LinkedHashSet(sirows.size());
	for (ShortInterestCalendarRow row : sirows) {
	    dates.add(row.dueDate);
	}
	return dates;
    }

    static Set<Integer> getDisseminationDates(TreeSet<ShortInterestCalendarRow> sirows) {

	Set<Integer> dates = new LinkedHashSet(sirows.size());
	for (ShortInterestCalendarRow row : sirows) {
	    dates.add(row.disseminationDate);
	}
	return dates;
    }
}
