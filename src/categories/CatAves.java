package categories;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import utilities.G;
import supers.SuperX;
import supers.Var;
import objects.Stock;

public class CatAves extends Stock {


    public Set<Stock> stocksInTheCategory;
    Seasons seasons;

//    public Map<Class, Var> vars_catAveable;

    /** htf do i get stocksInTheCategory ? */
    public CatAves(int minDate, String categoryFullName, Set<Stock> stocksInTheCategory, Set<Class> x_objects_to_use) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, Var.TriedToPutBadDataTypeInVarDataArray {
	super(categoryFullName, x_objects_to_use);
	initialize(minDate);
	this.stocksInTheCategory = stocksInTheCategory;

//	System.out.println("hr6u56u678i67u6 catFullName: " + categoryFullName + ", vars size: " + vars.size());
//	vars_catAveable = C.get_vars_catAveable(vars);

//	G.asdf("awefawefawefawefffff");
//	G.asdf(vars_catAveable.keySet());
//	System.exit(0);
	if (stocksInTheCategory != null && !stocksInTheCategory.isEmpty())
	    calculate_data_from_categoryAveraging();
    }


    private void calculate_data_from_categoryAveraging() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, Var.TriedToPutBadDataTypeInVarDataArray {

	for (SuperX x : xs)
	    x.calculate_data_from_categoryAveraging(seasons, stocksInTheCategory);
    }

    private void initialize(int minDate) throws ParseException {
	seasons = new Seasons(minDate);
	date = ArrayUtils.toPrimitive(seasons.datesArray, G.null_int);
	date_i__map = G.get__date_i__map(date);
//	System.out.println("5y45t date i map size: " + date_i__map.size());
    }

    /** Set<Stock> stocksInTheCategory should be connected to updated current prices data */
    public void updateForCurrentPrices() {
	if (stocksInTheCategory.isEmpty())
	    return;
	for (SuperX x : xs)
	    x.update_category_averaging_for_currentPrices(seasons, stocksInTheCategory);
    }


    public static Map<C.CategoryType, Map<String, CatAves>> build___catType__catName_catAves__maps(int minDate, Map<String, Set<Stock>> catFullName_stocks__map, Set<Class> x_objects_to_use) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, IOException, Var.TriedToPutBadDataTypeInVarDataArray {

	System.out.println("building cataves ...");
	Map<C.CategoryType, Map<String, CatAves>> catType__catName_catAves__map = new LinkedHashMap(C.catTypesToAnalyze.size());

	//initialize output maps
	for (C.CategoryType ct : C.catTypesToAnalyze)
	    catType__catName_catAves__map.put(ct, new LinkedHashMap());


	for (Map.Entry<String, Set<Stock>> entry : catFullName_stocks__map.entrySet()) {
	    String cfn = entry.getKey();
//	    System.out.println("                               698yu986uy cfn: " + cfn);
	    Set<Stock> stocks = entry.getValue();
	    C.CategoryType catType = C.getCategoryType_from_catFullName(cfn);

	    if (C.catTypesToAnalyze.contains(catType)) {

		String catName = C.getCategoryCleanName_from_catFullName(cfn);
		CatAves catAve = new CatAves(minDate, cfn, stocks, x_objects_to_use);
		Map<String, CatAves> map = catType__catName_catAves__map.get(catType);
		map.put(catName, catAve);
	    }
	}

	return catType__catName_catAves__map;
    }

//    public static void updateForCurrentPrices(Map<C.CategoryType, Map<String, CatAves>> catType__catName_catAves__map) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, Var.TriedToPutBadDataTypeInVarDataArray {
//	for (C.CategoryType ct : C.catTypesToAnalyze)
//	    for (Map.Entry<String, CatAves> entry : catType__catName_catAves__map.get(ct).entrySet())
//		entry.getValue().updateForCurrentPrices();
//    }

    /** builds from file.  this is stupid.  i'm never going to use this.  i need to be writing the outputter, just for checking numbers!!!!!!!!! */
    public static Map<C.CategoryType, Map<String, CatAves>> readFromDisk___catType__catName_catAves__maps(Map<String, Set<Stock>> catFullName_stocks__map, List<Stock> allStocks, List<Stock> catAves) {

	Map<String, CatAves> cfn_catAves__map = C.get__cfn_catAves__map(catAves);


	Map<C.CategoryType, Map<String, CatAves>> map = new LinkedHashMap(C.catTypesToAnalyze.size());

	//initialize output maps
	for (C.CategoryType ct : C.catTypesToAnalyze)
	    map.put(ct, new LinkedHashMap());


	for (Map.Entry<String, Set<Stock>> entry : catFullName_stocks__map.entrySet()) {
	    String cfn = entry.getKey();
//	    System.out.println("                               698yu986uy cfn: " + cfn);
//	    Set<Stock> stocks = entry.getValue();
	    C.CategoryType catType = C.getCategoryType_from_catFullName(cfn);
	    String catName = C.getCategoryCleanName_from_catFullName(cfn);

	    map.get(catType).put(catName, cfn_catAves__map.get(cfn));
	}

	return map;
    }

    public static void writeToDisk(Map<C.CategoryType, Map<String, CatAves>> catType__catName_catAves__map) throws IOException {
	for (Map<String, CatAves> map : catType__catName_catAves__map.values()) {
	    for (CatAves ca : map.values()) {
		ca.write_xFiles();
	    }
	}
    }


}
