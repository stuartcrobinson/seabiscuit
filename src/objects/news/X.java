package objects.news;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import objects.Stock;
import utilities.G;
import static utilities.G.get;
import supers.SuperX;
import supers.Var;

public final class X extends SuperX implements supers.SuperXInterface, supers.XInterface_uses_Vars {

    private Map<Integer, List<News>> map__cookedDate_news;	///* contains list because one day might have lots of news released.  want them lumped per day */
    objects.prices.X prices_X;

    public X() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(null);
	fill_emptyVarsMap();
    }

    public X(String ticker) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	super(ticker, null);
	fill_emptyVarsMap();
    }

    public void calculate_data_origination(objects.prices.X prices_X) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.No_DiskData_Exception {
	setAttributes(prices_X);
	fill_data__origination(this);
    }

    public void setAttributes(objects.prices.X prices_X) throws IOException, ParseException, G.No_DiskData_Exception {
	this.prices_X = prices_X;
	List<News> newslist = News.readFromDisk(ticker);
	this.map__cookedDate_news = convert_RawNewsList_to_cookedDatesMap(newslist);
    }


    @Override
    public void setXFiles() throws IOException {
	setXFile_vars();
    }

    @Override
    public void setXFile_vars() throws IOException {
	varsFile = G.newChildTickerFile(G.XNews_vars, ticker);
    }

    @Override
    public void fill_data__origination(SuperX x) throws IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	fill_vars(x, length(prices_X));
    }

    public Map<Integer, List<News>> convert_RawNewsList_to_cookedDatesMap(List<News> newslist) throws ParseException {

	Map<Integer, List<News>> map = new LinkedHashMap();

	//for each report in rawlist, determine cooked date
	//put in map for this date.  (if date exists, add to existing list.  else make new list, put for that date) 
	for (News news : newslist) {
	    int cookedDate = getCookedDate(news);

	    if (map.containsKey(cookedDate))
		map.get(cookedDate).add(news);
	    else
		map.put(cookedDate, new ArrayList(Arrays.asList(new News[]{news})));
	}
	return map;
    }

    private static int getCookedDate(News news) throws ParseException {

	if (news.time == null || news.time.isEmpty())
	    return Integer.parseInt(news.date);		//if no time, then assume listed date is cooked date (date of first market close after event)

	int hour = Integer.parseInt(news.time.substring(0, 2));

	if (hour < 16)
	    return Integer.parseInt(news.date);
	else
	    return Integer.parseInt(G.getNextWeekdayDate(news.date));
    }

    @Override @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public void calculate_vars_origination(SuperX x, boolean doUpdateForCurrentPrices) {
	if (doUpdateForCurrentPrices)
	    return;

//	int iterMax = doUpdateForCurrentPrices ? 1 : Integer.MAX_VALUE;

	float[] _numTotalArticles = x.vars.get(News_numTotalArticles.class).ar_float;
	float[] _numTotalReliableSources = x.vars.get(News_numTotalReliableSources.class).ar_float;
	float[] _numPressReleases = x.vars.get(News_numPressReleases.class).ar_float;
	float[] _numTitlesContainCo = x.vars.get(News_numTitlesContainCo.class).ar_float;

	int[] daysSinceAny = x.vars.get(News_daysSinceAny.class).ar_int;
	int[] daysSinceReliableSource = x.vars.get(News_daysSinceReliableSource.class).ar_int;
	int[] daysSincePressRelease = x.vars.get(News_daysSincePressRelease.class).ar_int;
	int[] daysSinceTitleContainsCo = x.vars.get(News_daysSinceTitleContainsCo.class).ar_int;

	int len = length(prices_X);

	Object[] dummy_any = new Object[len];
	Object[] dummy_reliable = new Object[len];
	Object[] dummy_pressRelease = new Object[len];
	Object[] dummy_titleContainsCo = new Object[len];

	int[] date = date(prices_X);

//	for (int i = 0; i < Math.min(iterMax, len); i++) {
	for (int i = 0; i < len; i++) {
	    try {
		_numTotalArticles[i] = 0;				//NOTE:  setting num articles to 0 for every day will skew data to more recent dates.  figure out how get really old news dates data?
		_numTotalReliableSources[i] = 0;
		_numPressReleases[i] = 0;
		_numTitlesContainCo[i] = 0;

		List<News> list = map__cookedDate_news.get(get(date, i));	//wont be news for every day

		if (list != null) {
		    _numTotalArticles[i] = list.size();

		    for (News news : list) {
			_numTotalReliableSources[i] += news.isUnreliableSource ? 0 : 1;
			_numPressReleases[i] += news.isPressRelease ? 1 : 0;
			_numTitlesContainCo[i] += news.titleContainsCompanyName ? 1 : 0;
		    }
		    dummy_any[i] = _numTotalArticles[i] > 0 ? new Object() : null;
		    dummy_reliable[i] = _numTotalReliableSources[i] > 0 ? new Object() : null;
		    dummy_pressRelease[i] = _numPressReleases[i] > 0 ? new Object() : null;
		    dummy_titleContainsCo[i] = _numTitlesContainCo[i] > 0 ? new Object() : null;
		}
	    } catch (G.My_null_exception ex) {
	    }
	}

	G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(daysSinceAny, dummy_any, doUpdateForCurrentPrices);
	G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(daysSinceReliableSource, dummy_reliable, doUpdateForCurrentPrices);
	G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(daysSincePressRelease, dummy_pressRelease, doUpdateForCurrentPrices);
	G.calculateNumIndicesSinceNonNullEventAr_forDecreasingDates(daysSinceTitleContainsCo, dummy_titleContainsCo, doUpdateForCurrentPrices);
    }

    @Override
    public void fill_varClasses() {
	varClasses.add(X.News_numTotalArticles.class);
	varClasses.add(X.News_numTotalReliableSources.class);
	varClasses.add(X.News_numPressReleases.class);
	varClasses.add(X.News_numTitlesContainCo.class);

	varClasses.add(X.News_daysSinceAny.class);
	varClasses.add(X.News_daysSinceReliableSource.class);
	varClasses.add(X.News_daysSincePressRelease.class);
	varClasses.add(X.News_daysSinceTitleContainsCo.class);
    }

    public static class News_numTotalArticles extends Var {
	public News_numTotalArticles() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
//	    initializeFloatValuesToZero = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class News_numTotalReliableSources extends Var {
	public News_numTotalReliableSources() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
//	    initializeFloatValuesToZero = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class News_numPressReleases extends Var {
	public News_numPressReleases() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
//	    initializeFloatValuesToZero = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class News_numTitlesContainCo extends Var {
	public News_numTitlesContainCo() {
	    arrayDataType = Type.FLOAT;
	    isForCatAveComparison = true;
//	    initializeFloatValuesToZero = true;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class News_daysSinceAny extends Var {
	public News_daysSinceAny() {
	    arrayDataType = Type.INTEGER;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class News_daysSinceReliableSource extends Var {
	public News_daysSinceReliableSource() {
	    arrayDataType = Type.INTEGER;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class News_daysSincePressRelease extends Var {
	public News_daysSincePressRelease() {
	    arrayDataType = Type.INTEGER;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    public static class News_daysSinceTitleContainsCo extends Var {
	public News_daysSinceTitleContainsCo() {
	    arrayDataType = Type.INTEGER;
	}

	@Override
	public void calculateData(SuperX x, boolean doUpdateForCurrentPrices) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

    @Override //not needed.  news is not adjusted for current prices
    public void setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(Stock stock) {
    }
}
