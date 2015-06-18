package categories;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import objects.Stock;
import supers.Var;
import objects.profile.Profile_EraDataRow;
import utilities.G;

/** category helper */
public class C {
    private static String categoryDelimiter = "——";

    public static String getCategoryFullName(CategoryType ct, Profile_EraDataRow profile_edr, String ticker) {
	if (ct == CategoryType.GOOGLE_RELATED_COMPANIES) {
	    String cfn = getCategoryFullName(ct, ticker);
	    return cfn;
	}
	String cn = profile_edr.categories.get(ct);
	if (cn == null || cn.equals("null") || cn.isEmpty())
	    return null;
	String cfn = getCategoryFullName(ct, cn);
	return cfn;
    }

    public static String getCategoryFullName(C.CategoryType c, String catName) {
	return c.toString() + C.categoryDelimiter + catName;
    }

    public static C.CategoryType getCategoryType_from_catFullName(String catFullName) {
	return C.CategoryType.valueOf(catFullName.split("\\" + C.categoryDelimiter)[0]);
    }

    public static String getCategoryCleanName_from_catFullName(String catFullName) {
	try {
	    return catFullName.split("\\" + C.categoryDelimiter)[1];
	} catch (Exception e) {
	    System.out.println("WTF WEIRD PROBLEm !! catFullName: " + catFullName);
	    throw e;
	}
    }

    public static String getCleanedCategoryName(String catRawName) {
	if (catRawName == null) return null;
	catRawName = catRawName.replaceAll(" ", "");
	return catRawName.replaceAll("\\W", "_");
    }

    public static Map<Class, Var> get_vars_catAveable(Map<Class, Var> vars) {

	Map<Class, Var> vars_catAveable = new LinkedHashMap(vars);

	for (Iterator<Map.Entry<Class, Var>> i = vars_catAveable.entrySet().iterator(); i.hasNext();) {
	    Map.Entry<Class, Var> element = i.next();
	    if (!element.getValue().isForCatAveComparison) {
		i.remove();
	    }
	}
	return vars_catAveable;
    }

    public static Map<String, Set<Stock>> get__catFullName_stocks__map(Map<String, Set<String>> categoryFullName_tickers__map, List<Stock> allStocks) {

	Map<String, Stock> ticker_stock__map = Stock.get__ticker_stock__map(allStocks);

	Map<String, Set<Stock>> catFullName_stocks__map = new LinkedHashMap(categoryFullName_tickers__map.size());

	for (Map.Entry<String, Set<String>> entry : categoryFullName_tickers__map.entrySet()) {
	    String catFullName = entry.getKey();
	    Set<String> tickers = entry.getValue();

	    Set<Stock> stocks = new LinkedHashSet(tickers.size());

	    for (String ticker : tickers)
		stocks.add(ticker_stock__map.get(ticker));

	    catFullName_stocks__map.put(catFullName, stocks);
	}
	return catFullName_stocks__map;
    }

    public static Map<String, CatAves> get__cfn_catAves__map(List<Stock> catAves) {
	Map<String, CatAves> map = new LinkedHashMap();
	for (Stock stock : catAves) {

	    CatAves ca = (CatAves)stock;
	    map.put(ca.name, ca);
	}
	return map;
    }


    /** per cat type, there's an extra column "n" for number of stocks included in average */
    public static String getMegaTableCatAvesHeaders(Set<CategoryType> catTypes, boolean printTempVars, Set<Class> x_objects_to_use) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, Var.TriedToPutBadDataTypeInVarDataArray {

	
	if (!x_objects_to_use.contains(objects.profile.X.class)) 
	    return "";
	
	StringBuilder sb = new StringBuilder();

	CatAves dummyCatAves = new CatAves(19690720, "", null, x_objects_to_use);

	for (C.CategoryType ct : catTypes) {
	    for (Var var : dummyCatAves.vars_catAveable.values()) {
		if (var.isTemp && !printTempVars)
		    continue;
		sb.append("ca").append(lowercaseExceptFirstLetter(ct.nickname)).append("_").append(var.getName()).append(G.megaTableDelim);
	    }
	    sb.append("nStocksInCategory").append(G.megaTableDelim);
	}
	return sb.toString();
    }


    public static String getCategoryName(Stock stock, CategoryType ct, Map<String, Profile_EraDataRow> profiles) {

	String ticker = stock.name;

	if (ct == C.CategoryType.GOOGLE_RELATED_COMPANIES)
	    return ticker;

	Profile_EraDataRow pedr = profiles.get(ticker);
	return pedr.categories.get(ct);

    }

    public static String getCategoryFullName(Stock stock, CategoryType ct, Map<String, Profile_EraDataRow> profiles) {
	String cn = getCategoryName(stock, ct, profiles);
	return getCategoryFullName(ct, cn);
    }

    public static String lowercaseExceptFirstLetter(String nickname) {
	return nickname.substring(0, 1).toUpperCase() + nickname.substring(1, nickname.length()).toLowerCase();
    }


    public enum CategoryType {
	GOOGLE_RELATED_COMPANIES("rc"),
	GOOGLE_INDUSTRY("gi"),
	GOOGLE_SECTOR("gs"),
	YAHOO_INDUSTRY("yi"),
	YAHOO_SECTOR("ys");

	public static CategoryType parse(String str) {
	    str = str.toLowerCase();
	    
	    if (str.equals("grc"))
		str = "rc";

	    if (str.charAt(0) == 'v')
		str = str.substring(1, str.length());
	    
//	    G.asdf("34tw4trrsdf str is " + str);

	    for (C.CategoryType ct : C.catTypesToAnalyze) {
		if (str.equals(ct.nickname))
		    return ct;
	    }
	    return null;
	}

	public final String nickname;

	CategoryType(String nm) {
	    this.nickname = nm;
	}
    }
    public static Set<C.CategoryType> catTypesToAnalyze;// = get__catTypesToAnalyze();

//    /** TODO -- set this in master!  set the ones i want to exclude */
//    private static Set<C.CategoryType> get__catTypesToAnalyze() {
////	return new LinkedHashSet(Arrays.asList(C.CategoryType.values()));
//	return new LinkedHashSet(Arrays.asList(new CategoryType[]{CategoryType.YAHOO_SECTOR}));
//    }

    /** SHRINKING is the set of all tickers per a given category (eg yahoo industry: mining).  this is a map of SHRINKINGS per category full name.  use this in conjunction with the List<Seasons> to calculate per-category earnings responses.     */
    public static Map<String, Set<String>> getMap__categoryFullName_tickers(Map<String, Profile_EraDataRow> profiles) {
	Map<String, Set<String>> map__catFullName_tickers = new LinkedHashMap();
	for (Map.Entry<String, Profile_EraDataRow> entry : profiles.entrySet()) {
	    String ticker = entry.getKey();
	    Profile_EraDataRow profile_edr = entry.getValue();
	    List<String> catFullNameKeys = new ArrayList(CategoryType.values().length);

	    for (CategoryType ctype : CategoryType.values()) {
		String cfn = C.getCategoryFullName(ctype, profile_edr, ticker);
		if (cfn != null)
		    catFullNameKeys.add(cfn);//G.getCategoryFullNameKey(CategoryAssetclass.GOOGLE_INDUSTRY, profile.google_industry));
	    }
	    for (String catFullNameKey : catFullNameKeys) {
		if (!map__catFullName_tickers.containsKey(catFullNameKey)) {
		    map__catFullName_tickers.put(catFullNameKey, new HashSet());
		}
		map__catFullName_tickers.get(catFullNameKey).add(ticker);
	    }
	}
	return map__catFullName_tickers;
    }


}
