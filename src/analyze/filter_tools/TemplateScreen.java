package analyze.filter_tools;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import main.DataManager;
import utilities.G;
import analyze.filter_tools.Screen.ThereMustBeEraDataRowFieldsWithNonUniqueNames;
import analyze.filter_tools.Screen.ThereMustBeTwoMacroVariablesWithSameName;
import categories.C;
import downloaders.regular_other.Replace_Weather;
import objects.Weather;
import supers.SuperX;
import supers.Var;

/**  a Filter object is the worker be that uses a Screen to screen the data.  private Screen screen is the only attribute that changes between screens.  Filter is constant and stable otherwise */
public final class TemplateScreen {

    /** a descriptor value */
    final public static short ALL = -1;
    private DataManager data;

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, IOException, Weather.NoWeatherDataFound, FileNotFoundException, InterruptedException, Replace_Weather.WeatherTimeoutException, ThereMustBeEraDataRowFieldsWithNonUniqueNames, ThereMustBeTwoMacroVariablesWithSameName {
	TemplateScreen.writeTemplateFile();
    }

    private void loadMacroDummydata() throws Weather.NoWeatherDataFound, IOException, FileNotFoundException, InterruptedException, Replace_Weather.WeatherTimeoutException, ParseException, ThereMustBeTwoMacroVariablesWithSameName, ThereMustBeEraDataRowFieldsWithNonUniqueNames, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	data = new DataManager();
	data.loadMacroDummyData();
	data.build_metaData();
    }

    private void loadData(DataManager data) {
	this.data = data;

    }

    private String getFilterTemplateStr(String x1, String x2, String x3, String x4, String x5, String x6, String x7) {
	return String.format("%-32s%-3s%-4s%-5s%-3s%-4s%-5s%-30s", x1, x2, x3, x4, x5, x6, x7, Screen.dComment + " comments");
    }

    /** can't be static cos it needs to know about macro data.  this should be here in Filter because Filter knows how the screen should be formatted.  Filter parses the filter file. */
    private void printScreenTemplate() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, IOException {

	List<String> lines = new ArrayList();

	String resultsDatespansLine = buildTemplate_resultsDatespansLine();
	String yVarLine = buildTemplate_yVarLine();

	List<String> macroConditionLines = buildTemplate_macroConditionLines();
	List<String> eraConditionLines = buildTemplate_eraConditionLines();
	List<String> stockVarConditionLines = buildTemplate_varConditionLines();
	List<String> cerVarConditionLines = buildTemplate_cerConditionLines();
	List<String> catCompConditionLines = buildTemplate_catCompConditionLines();

	lines.add("0, 3	# subset by date info!  (offset, fraction denominator).  eg, it loops through dates and only keeps those where (count + offset) % fractionDenominator == 0.");
	lines.add("1, 5	# subset by co info!  (offset, fraction denominator).  eg, it loops through cos and only keeps those where (count + offset) % fractionDenominator == 0.");
	lines.add("all_tickers #	list specific tickers separated by commas, or \"all_tickers\"");
	lines.add("yes #print hits to a file?");
	lines.add("research #\"production\" will tell you today's picks!  takes longer.  use research if not getting todays picks");
	lines.add(yVarLine);
	lines.addAll(macroConditionLines);
	lines.add("7  # how many hits to keep per day.  keeping the ones with the highest volume.  put \"all\" to keep all per day.");
	lines.add("");
	lines.addAll(eraConditionLines);
	lines.add("");
	lines.addAll(stockVarConditionLines);
	lines.add("");
	lines.addAll(cerVarConditionLines);
	lines.add("");
	lines.addAll(catCompConditionLines);

	G.filter_templatefile.getParentFile().mkdirs();

	Files.write(G.filter_templatefile.toPath(), lines, StandardCharsets.UTF_8);
    }

    private String buildTemplate_yVarLine() {
	StringBuilder sb = new StringBuilder();
	for (Var var : data.meta.demoStock.vars.values()) {
	    if (var.isDependentVariable)
		sb.append(var.getName()).append(Screen.d1).append(" ");
	}
	return sb.toString();
    }

    private String buildTemplate_resultsDatespansLine() {
	String str = "-1, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, Mon, Tues, Wed, Thurs, Fri";
	String str2 = str.replaceAll(",", "\\" + Screen.d1);
	return str2;
    }

    private List<String> buildTemplate_macroConditionLines() {

	List<String> lines = new ArrayList();
	for (Object st : new TreeSet(new ArrayList(data.macrometric_macro__map.keySet()))) {
	    lines.add(getFilterTemplateStr((String)st, Screen.d2, ">=", "-2.1", Screen.d2, "<=", "3.5"));
	}
	return lines;
    }

    private List<String> buildTemplate_eraConditionLines() {

	List<String> lines = new ArrayList();
	for (Object st : data.meta.alledrs__fieldname_edrclass__map.keySet()) {				    //this used to be a linkedhashset made from an arraylist made from this .keyset.  i deleted all that crap -- was it important? i hope not
	    lines.add(getFilterTemplateStr((String)st, Screen.d2, ">=", "-2.1", Screen.d2, "<=", "3.5"));
	}
	return lines;
    }

    private List<String> buildTemplate_varConditionLines() {
	List<String> lines = new ArrayList();
	for (SuperX x : data.meta.demoStock.xs) {	//Var var : demoStock.vars.values()) {
	    if (x.vars != null) {
		lines.addAll(getLinesForVars(x.vars.values()));
	    }
	    lines.add("");					    //wtf is this for? -- oh a blank line
	}
	return lines;
    }

    private List<String> buildTemplate_cerConditionLines() {
	List<String> lines = new ArrayList();
	if (data.meta.demoY != null) {
	    lines.addAll(getCatCompLinesForVars(data.meta.demoY.vars.values(), ""));
	    lines.add("");
	}
	return lines;
    }

    private List<String> buildTemplate_catCompConditionLines() {

	if (!data.x_objects_to_use.contains(objects.profile.X.class))
	    return new ArrayList();

	List<String> lines = new ArrayList();
	for (SuperX x : data.meta.demoStock.xs) {
	    if (x.vars_catAveable != null) {
		lines.addAll(getCatCompLinesForVars(x.vars_catAveable.values(), "v"));
		lines.add("");
	    }
	}
	return lines;

    }

    /** TODO -- convert this to taking in data = DataManager so it makes a template filter with the actual valid variables being used for the current run */
    public static void writeTemplateFile() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, IOException, Weather.NoWeatherDataFound, FileNotFoundException, InterruptedException, Replace_Weather.WeatherTimeoutException, ThereMustBeEraDataRowFieldsWithNonUniqueNames, ThereMustBeTwoMacroVariablesWithSameName {
	TemplateScreen filter = new TemplateScreen();
	filter.loadMacroDummydata();
	filter.printScreenTemplate();
    }

    /** TODO -- convert this to taking in data = DataManager so it makes a template filter with the actual valid variables being used for the current run */
    public static void writeTemplateFile(DataManager data) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, IOException, Weather.NoWeatherDataFound, FileNotFoundException, InterruptedException, Replace_Weather.WeatherTimeoutException, ThereMustBeEraDataRowFieldsWithNonUniqueNames, ThereMustBeTwoMacroVariablesWithSameName {
	TemplateScreen filter = new TemplateScreen();
	filter.loadData(data);
	filter.printScreenTemplate();
    }

    private List< String> getLinesForVars(Collection<Var> vars) {

	List<String> lines = new ArrayList();
	Map<String, Var> name_var__map = new TreeMap();

	for (Var var : vars) {
	    if (!var.getName().startsWith("fut") && !var.isTemp) { //fut
//		if (var.)
		name_var__map.put(var.getName(), var);
	    }
	}

	for (Var var : name_var__map.values()) {
	    String st = var.getName();
	    lines.add(makeLine(st, var));
	}
	return lines;
    }

    /** var name and c.cattype are separated by Screen.d0 (currently a space) */
    private Collection< String> getCatCompLinesForVars(Collection<Var> vars, String prefix) {

	List<String> lines = new ArrayList();
	Map<String, Var> name_var__map = new TreeMap();

	for (Var var : vars)
	    name_var__map.put(var.getName(), var);

	for (Var var : name_var__map.values()) {
	    String varName = var.getName();

	    for (C.CategoryType ct : C.catTypesToAnalyze) {
		String st = prefix + C.lowercaseExceptFirstLetter(ct.nickname) + Screen.d0 + varName;
		lines.add(makeLine(st, var));
	    }
	}
	return lines;

    }

    private String makeLine(String st, Var var) {
	switch (var.arrayDataType) {
	    case FLOAT:
		return getFilterTemplateStr(st, Screen.d2, ">=", "-2.1", Screen.d2, "<=", "3.5");
	    case INTEGER:
		return getFilterTemplateStr(st, Screen.d2, ">=", "-2", Screen.d2, "<=", "3");
	    case STRING:
		return getFilterTemplateStr(st, Screen.d2, "contains", " this text ", Screen.d2, "!contains", " other text ");
	    case BYTE:
		return getFilterTemplateStr(st, Screen.d2, ">=", "-2", Screen.d2, "<=", "3 " + Screen.dComment + " use this for booleans!");
	    case BOOLEAN:
		return getFilterTemplateStr(st, Screen.d2, "==", "true", Screen.d2, "==", "false" + Screen.dComment + " not fully supported!!");
	}
	throw new UnsupportedOperationException();
    }


}
