package main;
import utilities.G;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import analyze.filter_tools.Screen.ThereMustBeEraDataRowFieldsWithNonUniqueNames;
import categories.category_earnings_response.Y;
import objects.Macro;
import objects.Stock;
import supers.SuperX;


/** this is stuff used to identify variables (as var, era, macro, or  catdata).  it is created from a DataManager object.  and sits inside DataManager. */
public class MetaData {


    /** TODO -- allvars__name ... needs to include catData vars also!!!!!!  so, to check metric type, we need separate small sets.  like the macroMetrics, below.  pre-initialized.  initialized before data loaded or screen loaded */
    public Map<String, Class> allvars__name_class__map, alledrs__fieldname_edrclass__map;

    Set<String> macroMetrics, varMetrics, eraMetrics, cersMetrics;

    public Stock demoStock;
    public Y demoY;

//    String getMegaTableMacro() {
//
//	String returner = "";
//
//	for (String str : macroMetrics) {
//	    returner += str + G.megaTableDelim;
//	}
//	return returner;
//    }

    public MetaData(DataManager data) throws ThereMustBeEraDataRowFieldsWithNonUniqueNames, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

	macroMetrics = new LinkedHashSet(data.macrometric_macro__map.keySet());
	varMetrics = new LinkedHashSet();
	eraMetrics = new LinkedHashSet();
	cersMetrics = new LinkedHashSet();

	allvars__name_class__map = new LinkedHashMap<>();
	alledrs__fieldname_edrclass__map = new LinkedHashMap<>();

	//i'm changing this to use actual loaded data - instead of dummy data ... wait ... should i have to...?
	demoStock = new Stock(data.x_objects_to_use);
	if (data.x_objects_to_use.contains(objects.profile.X.class))
	    demoY = new Y();
	else
	    demoY = null;   //null here is indicator not to load anything dealing w/ categories (cos we don't have profile data!)
	load_className_maps_for_eras_and_vars(demoStock, demoY);
    }

    /** demoY as null here is indicator not to load anything dealing w/ categories (cos we don't have profile data!) */
    private void load_className_maps_for_eras_and_vars(Stock demoStock, Y demoY) throws ThereMustBeEraDataRowFieldsWithNonUniqueNames {
	int totalNumFields = 0;
	for (SuperX x : new SuperX[]{demoStock.people_X, demoStock.earnings_X, demoStock.finance_X}) {
	    if (x != null) {
		List<Field> fields = G.getNonPrivateDeclaredFields(x);// x.eraDataRowClass.getDeclaredFields();

		totalNumFields += fields.size();
		for (Field f : fields) {
		    alledrs__fieldname_edrclass__map.put(f.getName(), x.eraDataRowClass);
		    eraMetrics.add(f.getName());
		}
	    }
	}
	if (alledrs__fieldname_edrclass__map.size() != totalNumFields) {
	    System.out.println("eras.size() != totalNumFields");
	    throw new ThereMustBeEraDataRowFieldsWithNonUniqueNames();
	}

	for (Class varclass : demoStock.vars.keySet()) {
	    String shortName = G.getClassShortName(varclass);
	    allvars__name_class__map.put(G.getClassShortName(varclass), varclass);
	    varMetrics.add(shortName);
	}

	if (demoY != null) {
	    for (Class vclass : demoY.vars.keySet()) {
		String shortName = G.getClassShortName(vclass);
		allvars__name_class__map.put(G.getClassShortName(vclass), vclass);
		cersMetrics.add(shortName);
	    }
	}
    }


    public boolean metricIs_var(String metricName) {
	return allvars__name_class__map.containsKey(metricName);
    }

    public boolean metricIs_cer(String metricName) {
	return cersMetrics.contains(metricName);
    }

    public boolean metricIs_era(String metricName) {
	return alledrs__fieldname_edrclass__map.containsKey(metricName);
    }

    public boolean metricIs_macro(String metricName) {
//	G.asdf("awefawefegreg here metric: " + metricName);
	return macroMetrics.contains(metricName);
    }

    public Class getVarClass(String varName) {
	return allvars__name_class__map.get(varName);
    }


}
