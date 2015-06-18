package objects;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import utilities.G;
import downloaders.regular_other.Replace_Weather;
import java.util.TreeMap;
import static utilities.G.nyWeatherFile;
import static utilities.G.parse_int;
import static utilities.G.parse_short;
//import static stuff.Make_Squished.timer;

public class Weather {


    //NOTE:  this is a MACRO data.  independent of stocks.  this is my only one right now but i would like to have others (macro economic data)
    //does this need an X class? no!  stock objects (of the future) should NOT contain a reference to macro data!  have a separate MACRO object or something.
    //	    would be totally wasteful cos each stock would have duplicate reference/data
    //later, put this in containing MACRO object.  or make Weather a subclass of a "Macro" class?  when going through data, be able to be like:
    //	    int thetempOnThisDate = Macro.weather.tmax.get(date)
    //i think this is totally unrelated to stocks and the conventional "X" objects so lets ignore this for now
    //STATION,DATE,PRCP,TMAX,TMIN
    int date;
    short prcp, tmax, tmin, tave;

    public Weather(int dateArg, String[] line) {
	date = dateArg;
	prcp = parse_short(line[2]);
	tmax = parse_short(line[3]);
	tmin = parse_short(line[4]);
	tave = (short)Math.round((tmax + tmin) / 2.0);
    }

  public  static Map<Integer, Weather> get__date_weather_map_NOAA_HISTORIC() throws FileNotFoundException, NoWeatherDataFound, IOException {

	Map<Integer, Weather> weatherMap_NY = new LinkedHashMap<>();
	try (BufferedReader weatherReader = new BufferedReader(new FileReader(nyWeatherFile))) {
	    String line, trash = weatherReader.readLine();
	    while ((line = weatherReader.readLine()) != null) {
		String[] lineAr = line.split(",");
		Integer date = parse_int(lineAr[1]);
		weatherMap_NY.put(date, new Weather(date, lineAr));
	    }
	    if (weatherMap_NY.isEmpty()) {
		throw new NoWeatherDataFound("no historical weather");
	    }
	}

	return weatherMap_NY;
    }

    public static Map<Integer, Weather> get__date_weather_map(Set<Integer> validDates) throws FileNotFoundException, NoWeatherDataFound, IOException, InterruptedException, Replace_Weather.WeatherTimeoutException, ParseException {
	System.out.println("getting historical ny weather...");
	
	Map<Integer, Weather> weatherMap_NY = get__date_weather_map_NOAA_HISTORIC();
	
	System.out.println("getting remaining recent ny weather...");
	//check to see if we need to open a browser to get missing weather 
	boolean openBrowser = false;
	for (Integer date : validDates) {
	    if (!weatherMap_NY.containsKey(date)) {
		openBrowser = true;
		break;
	    }
	}
	WebDriver driver = null;
	if (openBrowser)
	    driver = new ChromeDriver();
	for (Integer date : validDates) {
	    if (!weatherMap_NY.containsKey(date)) {
		Weather w = Replace_Weather.getDatesWeather(driver, date);
		weatherMap_NY.put(w.date, w);
		System.out.println(w);
	    }
	}

//	timer.lapPrint();
	return weatherMap_NY;
    }

    /** dates in no particular order. 
     * @param validDates are necessary to determine if recent dates need to be downloaded */
    public static Macro getMacro(Map<Integer, Weather> weatherMap_NY) throws NoWeatherDataFound, IOException, FileNotFoundException, InterruptedException, Replace_Weather.WeatherTimeoutException, ParseException {
//	Map<Integer, Weather> weatherMap_NY = get__date_weather_map(validDates);

	int len = weatherMap_NY.keySet().size();

	int[] dates = G.new_null_int_ar(len);
	float[] prcps = G.new_null_float_ar(len);
	float[] tmaxs = G.new_null_float_ar(len);
	float[] tmins = G.new_null_float_ar(len);
	float[] taves = G.new_null_float_ar(len);

	int i = 0;
	for (Map.Entry<Integer, Weather> entry : weatherMap_NY.entrySet()) {
	    int dateInt = entry.getKey();
	    Weather w = entry.getValue();

	    dates[i] = dateInt;
	    prcps[i] = w.prcp;
	    tmaxs[i] = w.tmax;
	    tmins[i] = w.tmin;
	    taves[i] = w.tave;
	    i++;
	}

	Map<String, float[]> dataArrays = new LinkedHashMap();
	dataArrays.put("prcp", prcps);
	dataArrays.put("tmax", tmaxs);
	dataArrays.put("tmin", tmins);
	dataArrays.put("tave", taves);

	String descriptor = Macro.Names.weather;

	Macro macro = new Macro_float(descriptor, dates, dataArrays);

	return macro;
    }

    @Override
    public String toString() {
	return String.format("date: %d, tmin: %d, tmax: %d, prcp: %d", date, tmin, tmax, prcp);
    }

    public Weather(int date, short hi, short lo, short rain) {
	this.date = date;
	this.tmax = hi;
	this.tmin = lo;
	this.prcp = rain;
    }

    public static class NoWeatherDataFound extends Exception {
	public NoWeatherDataFound(String no_historical_weather) {
	}
    }

}
