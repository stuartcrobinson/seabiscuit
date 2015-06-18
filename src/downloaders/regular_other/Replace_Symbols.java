package downloaders.regular_other;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import utilities.G;
import utilities.HttpDownloadUtility;
import objects.Symbol;

public class Replace_Symbols {
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
	G.initialize("download symbols", args, G.symbolsDir);

	Set<String> optionable_tickers = new TreeSet<>();

	System.out.println("getting cboe tickers...");
	List<String> cboeAllOptionsLines = HttpDownloadUtility.getFile("http://www.cboe.com/publish/ScheduledTask/MktData/cboesymboldir2.csv");
	for (int i = 2; i < cboeAllOptionsLines.size(); i++) //skip two header lines
	    optionable_tickers.add(cboeAllOptionsLines.get(i).split(",")[1]);

	System.out.println("printing cboe...");
	try (PrintWriter pw = new PrintWriter(new FileWriter(G.symbolsFile_optionable))) {
	    for (String s : optionable_tickers)
		pw.println(s);
	}


	System.out.println("starting htmlunitdriver...");
	WebDriver driver = new HtmlUnitDriver();
	driver.get("http://eoddata.com/");

	System.out.println("logging in to eoddata...");
	driver.findElement(By.xpath("//*[@id=\"ctl00_cph1_lg1_txtEmail\"]")).sendKeys("strobinso@hotmail.com");
	driver.findElement(By.xpath("//*[@id=\"ctl00_cph1_lg1_txtPassword\"]")).sendKeys("isdisit");
	driver.findElement(By.xpath("//*[@id=\"ctl00_cph1_lg1_btnLogin\"]")).click();

	String url_base = "http://eoddata.com/Data/symbollist.aspx?e=";

	String[] eodddata_exhanges = new String[]{"NYSE", "NASDAQ", "AMEX"};//, "OTCBB"};

	List<Symbol> symbols = new ArrayList();

	for (String exchange : eodddata_exhanges) {
	    System.out.println(exchange);
	    driver.get(url_base + exchange);
	    try (BufferedReader br = new BufferedReader(new StringReader(driver.getPageSource()))) {
		String line, trash = br.readLine();
		while ((line = br.readLine()) != null) {
		    Symbol s = new Symbol(exchange, optionable_tickers, line);
		    if (!s.noGood)
			symbols.add(s);
		}
	    }
	}
	Collections.sort(symbols);

	System.out.println("printing tickers only file...");
	try (PrintWriter pw = new PrintWriter(new FileWriter(G.symbolsFile))) {
	    for (Symbol s : symbols)
		pw.println(s.ticker);
	}

	System.out.println("printing full symbols info file...");
	try (PrintWriter pw = new PrintWriter(new FileWriter(G.symbolsInfoFile))) {
	    for (Symbol s : symbols)
		pw.println(s.fullOutputString());
	}
    }
}


/*
 http://eoddata.com/Data/symbollist.aspx?e=NYSE
 http://eoddata.com/Data/symbollist.aspx?e=NASDAQ
 http://eoddata.com/Data/symbollist.aspx?e=AMEX
 http://eoddata.com/Data/symbollist.aspx?e=OTCBB
 http://eoddata.com/Data/symbollist.aspx?e=OPRA
 */
