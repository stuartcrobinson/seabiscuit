package objects;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import utilities.G;
import static utilities.G.symbolsFile;

public class Symbol implements Comparable {


    //Note: this class is irrelevant to the final data processor.  this is only useful (prerequisite) for downloading.  
    //{"NYSE", "NASDAQ", "AMEX", "OTCBB"}
    public String ticker, name, eoddata_exchange;
    public boolean isOptionable = false;
    public boolean noGood = false;

    /** remove common words like Inc, Co, Fund, etc */
    public String abridgedName;

    public Symbol(String symbolsFileLine) {
	/*
	 ticker	exchange	isOptionable	name
	 A	NYSE	1	Agilent Technologies
	 AA	NYSE	1	Alcoa Inc
	 */
	String[] lineAr = symbolsFileLine.split("\t");
	ticker = lineAr[0];
	eoddata_exchange = lineAr[1];
	isOptionable = lineAr[2].equals("1");
	name = lineAr[3];

	abridgedName = makeAbridgedName(name);
    }

    public Symbol(String exchange, Set<String> optionable_tickers, String line) {
	eoddata_exchange = exchange;
	String[] lineAr = line.split("\\t");
	ticker = lineAr[0];
	name = lineAr[1].replace(",", "");

	if (ticker.contains("-"))
	    noGood = true;

	ticker = ticker.replace(".", "-");	    //this is the yahoo format.


	if (ticker.endsWith(".OB"))
	    ticker = ticker.replace(".OB", "");

	isOptionable = optionable_tickers.contains(ticker) || optionable_tickers.contains(ticker.replace(",", ""));

	abridgedName = makeAbridgedName(name);
    }

    @Override
    public int compareTo(Object symbol2) {
	return ticker.compareTo(((Symbol)symbol2).ticker);
    }

    public String fullOutputString() {
	return ticker + "\t" + eoddata_exchange + "\t" + isOptionable + "\t" + name;
    }

    public static List<Symbol> getSymbolsList() throws IOException {
	List<Symbol> symbols = new ArrayList();
	List<String> lines = Files.readAllLines(G.symbolsInfoFile.toPath(), StandardCharsets.UTF_8);
	for (String line : lines)
	    symbols.add(new Symbol(line));
	return symbols;
    }


    public static String getStockName(List<Symbol> symbols, String ticker) {

	for (Symbol symbol : symbols)
	    if (symbol.ticker.toLowerCase().equals(ticker.toLowerCase()))
		return symbol.name;
	return null;
    }

    private static String makeAbridgedName(String name) {
	String[] wordsToRemove = new String[]{".", "capital", "management", "group", "llc", "the", "co", "company", "inc", "group", "a",
	    "and", "fund", "ETF", "iShares", "trust", "holdings", "industries", "incorporated", "income", "&",
	    "technology", "corporation", "income", "lp", "corp", "investment", "pfd"};
	for (String word : wordsToRemove)
	    word = word.toLowerCase();

	name = name.toLowerCase();
	name = name.replaceAll("\\W^ ", "");
	name = name.replaceAll("_", "");

	List<String> nameList = new ArrayList(Arrays.asList(name.split(" ")));

	for (String word : wordsToRemove)
	    if (nameList.contains(word))
		nameList.remove(word);

	return String.join(" ", nameList).trim();
    }

    public static String getCompanyName(List<Symbol> symbols, String ticker) {
	for (Symbol symbol : symbols) {
	    if (symbol.ticker.toLowerCase().equals(ticker.toLowerCase())) {
		return symbol.name;
	    }
	}
	return "";
    }

    /** tickers with hyphens.  BRK-A */
    public static List<String> getTickersList() throws IOException {
	return Files.readAllLines(symbolsFile.toPath(), StandardCharsets.UTF_8);
    }

    public static List<String> getTickersList(String min, String max) throws IOException {

	Set<String> list = new LinkedHashSet(getTickersList());

	list.removeIf(new Predicate() {
	    @Override
	    public boolean test(Object t) {
		return t.toString().compareTo(min) < 0 || t.toString().compareTo(max) > 0;
	    }
	});

	return new ArrayList(list);
    }
}
