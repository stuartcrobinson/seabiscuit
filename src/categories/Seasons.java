package categories;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import objects.Macro;
import objects.Macro_int;
import org.apache.commons.lang3.ArrayUtils;
import utilities.G;

/** sortable by date increasing.  do i want to change to decreasing??   this stuff gets used by cataves and CERS and doesn't get changed!*/

public final class Seasons {

    public static Macro getMacro(int minDate) throws ParseException {

	Seasons seasons = new Seasons(minDate);

	Map<String, int[]> dataArrays = new LinkedHashMap();

	int[] dates_ints = ArrayUtils.toPrimitive(seasons.datesArray);

//	float[] dates_floats = G.to_float(dates_ints);
	
	dataArrays.put("m_date", dates_ints);

	Macro dates_macro = new Macro_int(Macro.Names.dates, dates_ints, dataArrays);

	return dates_macro;
    }

    /** filled with dates up to today */
    public List<Season> seasonList;
    public Integer[] datesArray;
    public ArrayList<Integer> datesList;
    public int numDays;
    public Map<Integer, Integer> date_i__map;

    public Seasons(int minDate) throws ParseException {
	seasonList = getSeasons(minDate);
	setOtherVariables();
    }

//    public Seasons() throws ParseException {
//	seasonList = getSeasons(null);
//	setOtherVariables();
//    }

    private void setOtherVariables() {
	datesArray = getDatesArray();
	datesList = getDatesList();
	numDays = datesArray.length;
	date_i__map = get__date_i__map();

    }


    /** dates sorted increasing */
    public List<Season> getSeasons(int minDate) throws ParseException {
	Integer startingYear = startingYear = minDate / 10000;

	List<Season> seasonsList_ = new ArrayList();

	for (int year = startingYear; year <= G.current_year; year++) {

	    int expandedYear = year * 10_000;

	    seasonsList_.add(new Season(expandedYear + 101, expandedYear + 331));
	    seasonsList_.add(new Season(expandedYear + 401, expandedYear + 631));
	    seasonsList_.add(new Season(expandedYear + 701, expandedYear + 931));
	    seasonsList_.add(new Season(expandedYear + 1001, expandedYear + 1231));
	}

	Collections.sort(seasonsList_);

	int dateInt = seasonsList_.get(0).minDate;

	int dateIntMax = seasonsList_.get(seasonsList_.size() - 1).maxDate;	//what if this is the last day of the year?  that's okay.  remember to use inclusive bounds

	do {
	    insertDate(dateInt, seasonsList_);
	    dateInt = G.getNextWeekDay(dateInt);
	} while (dateInt <= dateIntMax && dateInt <= G.currentDate);

	return seasonsList_;

    }

    private void insertDate(int date, List<Season> seasonsList_) {
	for (Season ssn : seasonsList_)
	    if (ssn.shouldContain(date))
		ssn.dates.add(G.toNonPrimitive(date));
    }

    /** increasing */
    private Integer[] getDatesArray() {

	ArrayList<Integer> dateList = getDatesList();
	return dateList.toArray(new Integer[dateList.size()]);
    }

    private Map<Integer, Integer> get__date_i__map() {
	return G.get__date_i__map(datesArray);
    }


    /** sorted INCREASING */
    private ArrayList<Integer> getDatesList() {

	SortedSet<Integer> dates = new TreeSet();

	for (Season season : seasonList) {
	    dates.addAll(season.dates);
	}
	return new ArrayList(dates);

    }

    public static class Season implements Comparable {


	int minDate, maxDate;
	/** increasing */
	public SortedSet<Integer> dates;

	Season(int minDate, int maxDate) {
	    this.minDate = minDate;
	    this.maxDate = maxDate;
	    dates = new TreeSet();
	}

	public Season() {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	boolean shouldContain(int date) {
	    return date >= minDate && date <= maxDate;
	}

	@Override
	public int compareTo(Object o) {
	    return Integer.compare(minDate, ((Season)o).minDate);
	}


    }
}
