package supers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import utilities.G;
import categories.C;
import java.util.LinkedHashMap;

/** DO NOT USE BOOLEAN.  use byte 0/1 for boolean subjects.  public constructors must set the Type arrayDataType.  use byte for boolean data */
public abstract class Var implements VarInterface {

    /** temporary variables get deleted after a stock is loaded.  stuff like ema3, ema5, high, low, etc, when updating for current prices, they have to be recreated */
    public boolean isTemp = false;
//    public boolean initializeFloatValuesToZero;

    public void clearData() {

	switch (arrayDataType) {
	    case FLOAT:
		ar_float = null;
		return;
	    case INTEGER:
		ar_int = null;
		return;
	    case STRING:
		ar_String = null;
		return;
	    case BYTE:
		ar_byte = null;
		return;
	    case BOOLEAN:
		ar_boolean = null;
		return;
	}
    }

    /** TODO this needs to be moved out of Var becuase it is used by era and macro!!!! */
    public enum Type {
	FLOAT, INTEGER, BYTE, STRING, BOOLEAN
    }

    public int[] ar_int;
    public float[] ar_float;
    public String[] ar_String;
    public boolean[] ar_boolean;
    public byte[] ar_byte;

    public Type arrayDataType;

    /** do all catAveCOmp vars need to be float? */
    public boolean isForCatAveComparison;
    public boolean isDependentVariable;

    /** initialized right before we calculate the comparisons.  only used if true == isForCatAveComparison I THINK????!!*/
    public Map<C.CategoryType, float[]> catComparisonArrays;

    /** used by category earnings response.  puts the classes in varClasses and initializes the vars map with data-empty values */
    public static void initializeVars(List<Class> varClasses, Map<Class, Var> vars, Class... classes) throws InstantiationException, IllegalAccessException {
	varClasses.addAll(Arrays.asList(classes));
	for (Class c : varClasses) {
	    Var var = (Var)c.newInstance();
	    Var.putInMap(vars, var);
	}
    }

    /** initialize to null_float */
    void initializeCatComparisonArrays(Set<C.CategoryType> catTypesToCompare, int stockDataLength) {
	catComparisonArrays = new LinkedHashMap(catTypesToCompare.size());
	for (C.CategoryType c : catTypesToCompare)
	    catComparisonArrays.put(c, G.new_null_float_ar(stockDataLength));
    }

    /** used for category averaging and data origination (so.... that means NOT from xfiles? i think */
    public void initializeArray(int newArrayLength) {
//	if (initializeFloatValuesToZero) {
//	    ar_float = new float[newArrayLength];
//	    return;
//	}
	switch (arrayDataType) {
	    case FLOAT:
		ar_float = G.new_null_float_ar(newArrayLength);
		break;
	    case INTEGER:
		ar_int = G.new_null_int_ar(newArrayLength);
		break;
	    case STRING:
		ar_String = G.new_null_String_ar(newArrayLength);
		break;
	    case BYTE:
		ar_byte = G.new_null_byte_ar(newArrayLength);
		break;
	    case BOOLEAN:
		ar_boolean = G.new_null_boolean_ar(newArrayLength);
		break;
	}
    }

    public String getName() {
	return G.getClassShortName(this.getClass());
    }

    public static void putInMap(Map<Class, Var> vars, Var var) {
	vars.put(var.getClass(), var);
    }

    /** output is just for code asthetic */
    private boolean appendDataArrayValues(char d, StringBuilder sb) {
	switch (arrayDataType) {
	    case FLOAT:
		//		System.out.println("wefw fl len: " + ar_float.length);
		return G.append(sb, d, ar_float);
	    case INTEGER:
		//		System.out.println("wefw in len: " + ar_int.length);
		return G.append(sb, d, ar_int);
	    case STRING:
		//		System.out.println("wefw st len: " + ar_String.length);
		return G.append(sb, d, ar_String);
	    case BYTE:
		//		System.out.println("wefw by len: " + ar_byte.length);
		return G.append(sb, d, ar_byte);
	    case BOOLEAN:
		//		System.out.println("wefw bo len: " + ar_boolean.length);
		return G.append(sb, d, ar_boolean);
	}
	return true;
    }

    public String get(C.CategoryType ct, int i) {
	float[] ar = catComparisonArrays.get(ct);
	if (ar == null)
	    return "";
	return G.parse_Str(ar[i]);
    }

    public String get(int i) {
	switch (arrayDataType) {
	    case FLOAT:
		return G.parse_Str(ar_float[i]);
	    case INTEGER:
		return G.parse_Str(ar_int[i]);
	    case STRING:
		return G.parse_Str(ar_String[i]);
	    case BYTE:
		return G.parse_Str(ar_byte[i]);
	    case BOOLEAN:
		return G.parse_Str(ar_boolean[i]);
	}
	return null;
    }

    private void appendCatComparisonArrayValues(char d, StringBuilder sb) {

	if (catComparisonArrays != null) {
	    for (Entry<C.CategoryType, float[]> entry : catComparisonArrays.entrySet()) {
		C.CategoryType ct = entry.getKey();
		float[] ar = entry.getValue();

		sb.append(G.varSubDelim);
		sb.append(ct.toString());
		G.append(sb, d, ar);
	    }
	}
    }

    public static void writeVarsToDisk(Map<Class, Var> vars, File varsFile) throws IOException {

//	System.out.println("in write vars to disk! num vars: " + (vars == null ? 0 : vars.size()) + ", file: " + varsFile.getCanonicalPath());
//	System.out.println("in write vars to disk! num vars: ");
//	System.out.println(vars == null ? 0 : vars.size() + ", file: " + varsFile.getCanonicalPath());

//	System.out.println("creating " + varsFile.getParentFile() + ", success? " + varsFile.getParentFile().mkdirs());
//	System.out.println("4t4t34t4 varsfile: " + varsFile.getCanonicalPath());
	varsFile.getParentFile().mkdirs();
	if (vars == null) return;
	try (PrintWriter pw = new PrintWriter(new FileWriter(varsFile))) {
	    for (Var var : vars.values()) {
		if (!var.isTemp)
		    pw.println(var.toOutputXfileLine());
	    }
	}
    }

    /** classname then data, horizontally, separated by tabs.  THEN : then category name then data, etc like this : <br><br>
     spaces are tabs:  <br><br>
     varClassName  x1 x2 x3 x4 x5 ... xN : C.CategoryType1 x1 x2 x3 x4 x5 x6 : C.CategoryTyp2 x1 x2 x3 x4...
     */
    public String toOutputXfileLine() {

	char d = G.varDelim;	//delimiter

	String className = this.getClass().getName();

	StringBuilder sb = new StringBuilder();
	sb.append(className);
	try {
	    appendDataArrayValues(d, sb);
	} catch (Exception e) {						//testing, temp
	    System.out.println("WTF WHY EXCEPTION ??!?!?!?!");
	    e.printStackTrace();
	    System.exit(0);
	}
	appendCatComparisonArrayValues(d, sb);
	return sb.toString();				//dont trim!! that would delete my delimiters!!  screw up array lengths
    }

    private static void set_var_data_from_xFileLine(Map<Class, Var> vars, String xFileLine) throws ClassNotFoundException {

	List<String[]> lineSegmentArrays = getXFileLineSegmentArrays(xFileLine);

//	System.out.println("length of first segment: " + lineSegmentArrays.get(0).length);

	Class c = getVarClass(lineSegmentArrays);


	try {
	    vars.get(c).setData(lineSegmentArrays);
	} catch (Exception e) {
	    System.out.println("################################################################################################");
	    System.out.println("34t4rew: vars size: " + vars.size());
	    System.out.println("34t4retyrew: class name: " + c.getName());
	    System.out.println("BEFORE");
	    for (Map.Entry<Class, Var> entry : vars.entrySet()) {
		System.out.println(entry.getKey() + " __:__ " + entry.getValue().getName());
	    }
	    System.out.println("AFTER");


	    e.printStackTrace();
	    System.exit(0);

	}

    }

    private static List<String[]> getXFileLineSegmentArrays(String xFileLine) {

	String[] xFileVarDataLineSegments = xFileLine.split("\\" + G.varSubDelim, -1);		//first element is main data, remaining elements are catAveComparisons.  

	List<String[]> lineSegmentArrays = new ArrayList(xFileVarDataLineSegments.length);

	for (String str : xFileVarDataLineSegments) {
	    lineSegmentArrays.add(str.split("\\" + G.varDelim, -1));	    //I THINK PROBLEM WITH BACKSLASHES AND TABCHAR!!!  no idon't think so. i tested and seems fine like this :(
	}
	return lineSegmentArrays;
    }

    private void setData(List<String[]> lineSegmentArrays) {

	String[] mainData_lineSegmentArray = lineSegmentArrays.get(0);
	set_mainDataArray(mainData_lineSegmentArray);

	int len = lineSegmentArrays.size();

	if (len > 1) {
	    List<String[]> catComparison_lineSegmentArrays = lineSegmentArrays.subList(1, len);
	    set_catComparisonsArrays(catComparison_lineSegmentArrays);
	}
    }

    private void set_catComparisonsArrays(List<String[]> catComparisonXfileStrArrays) {

	catComparisonArrays = new HashMap<>(catComparisonXfileStrArrays.size());

	for (String[] ar : catComparisonXfileStrArrays) {


//	    System.out.println("a243tawe4fawef: " + ar[0]);
	    C.CategoryType ct = C.CategoryType.valueOf(ar[0]);

	    float[] ar1 = Var.getAr_float_fromXfilLineSegmentStrArray(ar);

	    catComparisonArrays.put(ct, ar1);
	}
    }

    private static Class getVarClass(List<String[]> lineSegmentArrays) throws ClassNotFoundException {
	return Class.forName(lineSegmentArrays.get(0)[0]);
    }

    /** ignore the first element -- that is the class name */
    void set_mainDataArray(String[] ar) {
	switch (arrayDataType) {
	    case FLOAT:
		ar_float = getAr_float_fromXfilLineSegmentStrArray(ar);
		break;
	    case INTEGER:
		ar_int = getAr_int_fromXfilLineSegmentStrArray(ar);
		break;
	    case STRING:
		ar_String = getAr_String_fromXfilLineSegmentStrArray(ar);
		break;
	    case BYTE:
		ar_byte = getAr_byte_fromXfilLineSegmentStrArray(ar);
		break;
	    case BOOLEAN:
		ar_boolean = getAr_boolean_fromXfilLineSegmentStrArray(ar);
		break;
	}
    }

    /** skips first element, that is the descriptive name (Var class or C.CategoryType) */
    private static float[] getAr_float_fromXfilLineSegmentStrArray(String[] ar) {

	float[] ar1 = new float[ar.length - 1];

	for (int i = 1; i < ar.length; i++)
	    ar1[i - 1] = G.parse_float(ar[i]);

	return ar1;
    }

    /** skips first element, that is the descriptive name (Var class or C.CategoryType) */
    private static int[] getAr_int_fromXfilLineSegmentStrArray(String[] ar) {

	int[] ar1 = new int[ar.length - 1];

	for (int i = 1; i < ar.length; i++)
	    ar1[i - 1] = G.parse_int(ar[i]);

	return ar1;
    }

    /** skips first element, that is the descriptive name (Var class or C.CategoryType) */
    private static String[] getAr_String_fromXfilLineSegmentStrArray(String[] ar) {

	String[] ar1 = new String[ar.length - 1];

	for (int i = 1; i < ar.length; i++)
	    ar1[i - 1] = ar[i];

	return ar1;
    }

    /** skips first element, that is the descriptive name (Var class or C.CategoryType) */
    private static boolean[] getAr_boolean_fromXfilLineSegmentStrArray(String[] ar) {

	boolean[] ar1 = new boolean[ar.length - 1];

	for (int i = 1; i < ar.length; i++)
	    ar1[i - 1] = G.parse_boolean(ar[i]);

	return ar1;
    }

    /** skips first element, that is the descriptive name (Var class or C.CategoryType) */
    private static byte[] getAr_byte_fromXfilLineSegmentStrArray(String[] ar) {

	byte[] ar1 = new byte[ar.length - 1];

	for (int i = 1; i < ar.length; i++)
	    ar1[i - 1] = G.parse_byte(ar[i]);

	return ar1;
    }


    public static void readFromDisk_intoPreInitializedMap(File varsFile, Map<Class, Var> vars) throws FileNotFoundException, IOException, ClassNotFoundException {

//	System.out.println("about to read vars from: " + varsFile.getCanonicalPath());

	//todo later -- move this stuff in to Var?  to be more like readErasFromFile(...)
	try (BufferedReader br = new BufferedReader(new FileReader(varsFile))) {
	    String line;
	    while ((line = br.readLine()) != null) {

//		System.out.println("   trying to set var array for line:   " + line);
		set_var_data_from_xFileLine(vars, line);
	    }
	}

//	//testing
//	for (Map.Entry<Class, Var> entry : vars.entrySet()) {
//	    System.out.println("CLASS, IN VARS MAP:                                            " + entry.getKey());
//	    Var var = entry.getValue();
//	    System.out.println("CLASS, IN VARS MAP:                    " + var.arrayDataType);
//	    System.out.println("CLASS, IN VARS MAP:  int array len                  " + var.ar_int.length);
//	    System.out.println("CLASS, IN VARS MAP:  flo array len                  " + var.ar_float.length);
//	}
    }


    public static class TriedToPutBadDataTypeInVarDataArray extends Exception {
	public TriedToPutBadDataTypeInVarDataArray() {
	}
    }

}
