package downloaders.regular_other;

import java.io.IOException;
import java.text.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.reporters.Files;
import utilities.G;
import utilities.HttpDownloadUtility;
import static utilities.G.nyWeatherFile;
import static utilities.G.parse_float;
import static utilities.G.weatherDir;
import objects.Weather;

public class Replace_Weather {


    public static void main(String[] args) throws IOException, InterruptedException, WeatherTimeoutException, ParseException {
	G.initialize();

//	System.out.println(initiateWeatherDataNOAAOnlineRequest());
	downloadCompletedNOAA_file();


//	WebDriver driver = new ChromeDriver();
//
//	System.out.println(getDatesWeather(driver, currentDate_int() - 4));
//	System.out.println(getDatesWeather(driver, currentDate_int() - 3));
//	System.out.println(getDatesWeather(driver, currentDate_int() - 2));
//	System.out.println(getDatesWeather(driver, currentDate_int() - 1));
//	System.out.println(getDatesWeather(driver, currentDate_int() - 0));
//
//	driver.quit();
    }

    public static Weather getDatesWeather(int date) throws IOException, WeatherTimeoutException, InterruptedException, ParseException {
	return getDatesWeather(null, date);
    }

    public static Weather getDatesWeather(WebDriver driver, int date) throws IOException, InterruptedException, WeatherTimeoutException, ParseException {
	G.initialize();
//	 http://www.wunderground.com/personal-weather-station/dashboard?ID=KNYNEWYO71#history/s20150205/e20150205/mdaily
	String url = "http://www.wunderground.com/personal-weather-station/dashboard?ID=KNYNEWYO71#history/s" + date + "/e" + date + "/mdaily";

	String tableXpath = "//*[@id=\"history_daily_summary\"]/div[2]/div[1]/table";
	String highXpath = "//*[@id=\"history_daily_summary\"]/div[2]/div[1]/table/tbody/tr[1]/td[2]";
	String lowXpath = "//*[@id=\"history_daily_summary\"]/div[2]/div[1]/table/tbody/tr[1]/td[3]";
	String prcpXpath = "//*[@id=\"history_daily_summary\"]/div[2]/div[1]/table/tbody/tr[4]/td[2]";
	String currDateXpath = "//*[@id=\"history_daily_summary\"]/div[1]/div/h3/span[1]/span";

	String initialTableString = null;

	boolean quitDriverHere = false;
	if (driver == null) {
	    driver = new ChromeDriver();
	    quitDriverHere = true;
	    driver.get(url);
	} else {
	    try {
		initialTableString = driver.findElement(By.xpath(tableXpath)).getText();
	    } catch (org.openqa.selenium.NoSuchElementException e) {
	    }
	    driver.get(url);
	    while (driver.findElement(By.xpath(tableXpath)).getText().equals(initialTableString)) {
		Thread.sleep(500);
		if (G.secondsSinceInitialize() > 60) {
		    System.out.println("ERROR weather taking too long to load!");
		    throw new WeatherTimeoutException();
		}
	    }
	}

//	WebElement we_table = driver.findElement(By.xpath(tableXpath));
	WebElement we_high = driver.findElement(By.xpath(highXpath));
	WebElement we_low = driver.findElement(By.xpath(lowXpath));
	WebElement we_prcp = driver.findElement(By.xpath(prcpXpath));

	String hi = we_high.getText().replaceAll(" .*", "");
	String lo = we_low.getText().replaceAll(" .*", "");
	String rain = we_prcp.getText().replaceAll(" .*", "");

	float hi_fl = parse_float(hi);
	float lo_fl = parse_float(lo);
	float rain_fl = parse_float(rain);

	short hi_int = convertFrom_F_to_tenthsOfC(hi_fl);
	short lo_int = convertFrom_F_to_tenthsOfC(lo_fl);
	short rain_int = convertFrom_In_to_tenthsOfMM(rain_fl);
	if (quitDriverHere)
	    driver.quit();

	return new Weather(date, hi_int, lo_int, rain_int);

    }

    public static Weather getTodaysWeather() throws IOException, WeatherTimeoutException, InterruptedException, ParseException {
	return getDatesWeather(G.currentDate);
    }


    /** Check Order Status: https://www.ncdc.noaa.gov/cdo-web/orderstatus?id=509048&email=strobinso%40hotmail.com   506638*/
    public static String initiateWeatherDataNOAAOnlineRequest() throws IOException, InterruptedException, ParseException {
	G.initialize();
	weatherDir.mkdirs();
	nyWeatherFile.createNewFile();

	//tmin and tmax: (tenths of degrees C)
	//prcp:  tenths of mm

	WebDriver d = new FirefoxDriver();

	String url = "http://www.ncdc.noaa.gov/cdo-web/datasets/GHCND/stations/GHCND:USW00094728/detail";
	d.get(url);

	Thread.sleep(2000);
	d.findElement(By.xpath("//*[@id=\"content\"]/div[1]/div[1]/div/a")).click();

	Thread.sleep(2000);
	d.get("http://www.ncdc.noaa.gov/cdo-web/cart");
	Thread.sleep(3000);

	d.findElement(By.xpath("//*[@id=\"GHCND_CUSTOM_CSV\"]")).click();
	d.findElement(By.xpath("//*[@id=\"dateRangeContainer\"]/input")).click();

	Select we = new Select(d.findElement(By.xpath("//select[@class=\"ui-datepicker-year\"]")));
	we.selectByValue("1997");

	d.findElement(By.xpath("//table[@class=\"ui-datepicker-calendar\"]/tbody/tr[2]/td[1]/a")).click();
	d.findElement(By.xpath("//form[@id=\"noaa-daterange-form\"]/button[1]")).click();
	d.findElement(By.xpath("//*[@id=\"cartContinue\"]/button")).click();

	Thread.sleep(3000);

	d.findElement(By.id("customTextOptions1")).click();

	d.findElement(By.xpath("//*[@id=\"dataTypesContainer\"]/ul/li[1]/label")).click();
	Thread.sleep(1000);
	d.findElement(By.xpath("//*[@id=\"dataTypesContainer\"]/ul/li[2]/label")).click();
	Thread.sleep(1000);
	d.findElement(By.xpath("//*[@id=\"dataTypesContainer\"]/ul/li[3]/label")).click();
	Thread.sleep(1000);
	d.findElement(By.xpath("//*[@id=\"dataTypesContainer\"]/ul/li[4]/label")).click();
	Thread.sleep(1000);

	d.findElement(By.id("TMAX")).click();
	d.findElement(By.id("TMIN")).click();
	d.findElement(By.id("PRCP")).click();
	d.findElement(By.id("buttonContinue")).click();

	Thread.sleep(3000);

	d.findElement(By.xpath("//*[@id=\"email\"]")).sendKeys("strobinso@hotmail.com");
	d.findElement(By.xpath("//*[@id=\"emailConfirmation\"]")).sendKeys("strobinso@hotmail.com");
	d.findElement(By.xpath("//*[@id=\"buttonSubmit\"]")).click();

	Thread.sleep(3000);

	String orderID = d.findElement(By.xpath("//*[@id=\"receipt\"]/table[1]/tbody/tr[2]/td[2]")).getText().replace("Check order status", "").trim();

	Files.writeFile(orderID, G.weatherIDFile);
	System.out.print("orderID: " + orderID);

	d.quit();
	return orderID;
    }

    public static void downloadCompletedNOAA_file(String orderID) throws IOException, InterruptedException {
	String downloadURL = "http://www1.ncdc.noaa.gov/pub/orders/cdo/" + orderID + ".csv";

	System.out.println(downloadURL);

	weatherDir.mkdirs();
	HttpDownloadUtility.downloadFile(downloadURL, nyWeatherFile);
    }

    public static void downloadCompletedNOAA_file() throws IOException, InterruptedException {
	String orderID = Files.readFile(G.weatherIDFile).trim();
	downloadCompletedNOAA_file(orderID);
    }

    private static short convertFrom_In_to_tenthsOfMM(float x) {
	return (short)Math.round(x * 25.4f * 10);
    }

    private static short convertFrom_F_to_tenthsOfC(float x) {
	return (short)Math.round(((x - 32) * 5 / 9) * 10);
    }

    public static class CurrentWeather {
	int hi, lo, rain;

	public CurrentWeather(int hi_int, int lo_int, int rain_int) {
	    hi = hi_int;
	    lo = lo_int;
	    rain = rain_int;
	}
    }

    public static class WeatherTimeoutException extends Exception {
	public WeatherTimeoutException() {
	}
    }

}
