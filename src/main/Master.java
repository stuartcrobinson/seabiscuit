package main;
import utilities.G;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Scanner;
import analyze.filter_tools.ResultsHandler;
import analyze.filter_tools.Screen;
import analyze.filter_tools.Screen.ThereMustBeEraDataRowFieldsWithNonUniqueNames;
import analyze.filter_tools.Screen.ThereMustBeTwoMacroVariablesWithSameName;
import analyze.filter_tools.TemplateScreen;
import categories.C;
import downloaders.regular_other.Replace_Weather;
import java.util.Date;
import main.DataManager.From;
import objects.Weather;
import supers.Var;

//-Xmx5500m

/** NOTE:  prices data is nullified for the 10 days before and after stock splits.  because both yahoo and google prices are unreliable around splits */
public class Master {

    public static void main(String[] asdf) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, IOException, Weather.NoWeatherDataFound, FileNotFoundException, InterruptedException, Replace_Weather.WeatherTimeoutException, ThereMustBeEraDataRowFieldsWithNonUniqueNames, ThereMustBeTwoMacroVariablesWithSameName, ClassNotFoundException, G.No_DiskData_Exception, Var.TriedToPutBadDataTypeInVarDataArray {
	G.initialize();
	screen();
    }

    public static void screen() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ParseException, IOException, Weather.NoWeatherDataFound, FileNotFoundException, InterruptedException, Replace_Weather.WeatherTimeoutException, ThereMustBeEraDataRowFieldsWithNonUniqueNames, ThereMustBeTwoMacroVariablesWithSameName, ClassNotFoundException, G.No_DiskData_Exception, Var.TriedToPutBadDataTypeInVarDataArray {
	long timer0 = (new Date()).getTime();
	DataManager data = new DataManager();
	data.excludeCategoryTypes(C.CategoryType.GOOGLE_INDUSTRY, C.CategoryType.GOOGLE_SECTOR, C.CategoryType.YAHOO_INDUSTRY, C.CategoryType.YAHOO_SECTOR);
//	data.setCatAveableVars(objects.finance.X.PFCF.class);
	data.setMinDate(20050101);
	data.x_objects_to_use__by_class(objects.prices.X.class, objects.finance.X.class, objects.earnings.X.class);//, objects.sec.X.class);//, objects.profile.X.class);//, objects.news.X.class, objects.sec.X.class, objects.profile.X.class);//, objects.short_interest.X.class);//,,,,objects.splits.X.class, objects.short_interest.X.class,  objects.people.X.class); //, objects.profile.X.class, objects.finance.X.class, objects.people.X.class,  objects.earnings.X.class, objects.news.X.class, objects.profile.X.class, objects.sec.X.class, objects.short_interest.X.class, objects.finance.X.class, objects.people.X.class, objects.short_interest.X.class, objects.earnings.X.class,  ,  objects.news.X.class, objects.profile.X.class, objects.sec.X.class,
//	data.set_subset_offset_and_fractionDenominator(0, 2);
	data.setMinMaxTickers("AAN", "AAP");
	data.load(From.RAW);
//	data.updateFromWeb();
//	data.printMegaTable();
//	System.exit(0);
	data.prepareForScreen();
	Scanner input = new Scanner(System.in);
	String nextLine;
	System.out.println("filter file location: " + G.filter_file);
	System.out.println(((double)((new Date()).getTime() - timer0) / 1000.0) + " seconds. " + "enter \"quit\"  or q to quit");
	do {
	    try {
		long timer = (new Date()).getTime();
		System.out.println("per exec: " + data.parametersOutputString());
		Screen screen = new Screen(G.filter_file, data);
		screen.runFilter();
		ResultsHandler.calculateAndDisplayResults(screen);
		System.out.print(G.parse_Str_two0s((double)((new Date()).getTime() - timer) / 1000.0) + "s");
	    } catch (Exception e) {
		TemplateScreen.writeTemplateFile(data);
		System.out.println("problem with filter file: " + G.filter_file.getCanonicalPath());
		System.out.println("check out template: " + G.filter_templatefile.getCanonicalPath());
		e.printStackTrace();
	    }
	    nextLine = input.nextLine();
	} while (!nextLine.equals("'") && !nextLine.equals("q") && !nextLine.equals("quit"));
    }
}


//next use selldates with buy VTI restriction
//what happens when minHold date hits a sell date out of range???
//TODO -- it is a bad and dangerous idea to be deleting prices less than 1.  i need to put that as condition in filter.  not manipulate data preemptively.
//TODO -- when running portfolio - make averaging values use the investments' buy and sell prices!  not the y-value
// TODO - build set of days that pass certain macro-level SELL conditions.  and then only sell on those dates.  like when VTIpctChDay >= 0
// TODO -  harvest options prices
//todo -- find filter that isn't so market-dependent
//NEXT, dust off the IB code!
