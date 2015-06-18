package objects.news;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import objects.Symbol;
import utilities.G;

public final class News implements Comparable {

    public static void main(String[] args) throws IOException {
	combineAllNewsLists();
    }

    public static void combineAllNewsLists() throws IOException {

	for (String ticker : Symbol.getTickersList()) {

	    System.out.println(ticker);

	    File googleNews = G.getNewsGoogleFile(ticker);
	    File yahooNews = G.getNewsYahooFile(ticker);
	    File combinedNews = G.getNewsCombinedFile(ticker);

	    combinedNews.getParentFile().mkdirs();

	    long gLM = googleNews.lastModified();
	    long yLM = yahooNews.lastModified();
	    long cLM = combinedNews.lastModified();


	    if (!(cLM > gLM && cLM > yLM)) {
		System.out.println("      combining news lists ...");
		combineNewsListsOnDisk(ticker);
	    }
	    else {
		G.asdf("news lists already combined");
	    }
	}

    }

    private static List<News> getCombinedNewsList(String ticker) throws IOException {

	File googleNews = G.getNewsGoogleFile(ticker);
	File yahooNews = G.getNewsYahooFile(ticker);
	File combinedNews = G.getNewsCombinedFile(ticker);

	combinedNews.getParentFile().mkdirs();

	long gLM = googleNews.lastModified();
	long yLM = yahooNews.lastModified();
	long cLM = combinedNews.lastModified();


	if (cLM > gLM && cLM > yLM) {
	    System.out.println("    reading combined news list ...");
	    return readFromDisk(combinedNews);
	} else {
	    return combineNewsListsOnDisk(ticker);
	}

    }

    private static List<News> combineNewsListsOnDisk(String ticker) throws IOException {

	File combinedNews = G.getNewsCombinedFile(ticker);
	List<News> newsList = new ArrayList();

	List<News> newsList1 = readFromDisk(G.getNewsYahooFile(ticker));
	List<News> newsList2 = readFromDisk(G.getNewsGoogleFile(ticker));

//	newsList.addAll(newsList1);
//	newsList.addAll(newsList2);
//
//	newsList = preen(newsList);

	newsList = combineNewsLists(newsList1, newsList2);

	News.writeToDisk(newsList, combinedNews);

	return newsList;
    }

    private static List<News> combineNewsLists(List<News> newsList1, List<News> newsList2) {

	Map<String, News> cmap = new TreeMap(Collections.reverseOrder());

	for (News n : newsList1) {
	    cmap.put(key(n), n);
	}
	for (News news2 : newsList2) {
	    News news1 = cmap.get(key(news2));
	    if (news1 != null) {		    //contains!  should we remove OR not insertnew ??? 

		boolean insert_2 = true;

		if (news1.isPressRelease == news2.isPressRelease || !news2.isPressRelease) {
		    if (!news2.pubTimeHasBeenInvestigated)
			insert_2 = false;
		    if (news1.pubTimeHasBeenInvestigated && news2.pubTimeHasBeenInvestigated)
			insert_2 = false;
		}

		if (insert_2) {
		    cmap.put(key(news2), news2);
		}
	    }
	}
	return new ArrayList(cmap.values());

    }

    private static String key(News n) {
	return n.date + n.title;
    }


    /**  G.sdf_date */
    public String date;
    /** military time, G.sdf_militaryTime */
    public String time;
    public boolean isPressRelease;
    public String source;
    public String title;
    public boolean titleContainsCompanyName;

    public String aggregator;   //y or g
    public String url;

    public String datetime;

    public boolean isUnreliableSource;

    public boolean pubTimeHasBeenInvestigated;

    String[] shittySources = new String[]{
	"thestreet.com",
	"yahoo.com/video",
	"cnn.com",
	"fool.com",
	"seekingalpha.com",
	"cnbc.com",
	"barrons.com",
	"investopedia.com",
	"zacks.com",
	"forbes.com"};

    public static List<News> readFromDisk(String ticker) throws IOException {
//
//	File googleNews = G.getNewsGoogleFile(ticker);
//	File yahooNews = G.getNewsYahooFile(ticker);
//	File combinedNews = G.getNewsCombinedFile(ticker);
//
//	combinedNews.getParentFile().mkdirs();
//
//	long gLM = googleNews.lastModified();
//	long yLM = yahooNews.lastModified();
//	long cLM = combinedNews.lastModified();
//
//
//	if (cLM > gLM && cLM > yLM) {
//	    System.out.println("    reading combined news list ...");
//	    return readFromDisk(combinedNews);
//	} else {
//	    return combineNewsListsOnDisk(ticker);
//	}

	return getCombinedNewsList(ticker);
    }

    public static void writeToDisk(List<News> newsList, File combinedNews) throws FileNotFoundException, IOException {
	Files.write(combinedNews.toPath(), News.getOutputLines(newsList), StandardCharsets.UTF_8);
    }

    public static List<News> readFromDisk(File newsFile) throws IOException {
	List<News> newsList = new ArrayList();

	if (newsFile.exists()) {
	    List<String> lines = Files.readAllLines(newsFile.toPath());

	    for (String line : lines)
		newsList.add(new News(line));
	}

	return newsList;
    }

    public boolean openArticleToScrapePubtimeLater;
    public boolean openArticleToScrapePubDateLater;
    public int pubTimeFetchAttemps;
    public boolean pubTimeFetchTotalFailure;


    public News(String outputFileLine) {
	String[] ar = outputFileLine.split("\t");

	date = ar[0];
	time = ar[1].trim();
	isPressRelease = Boolean.parseBoolean(ar[2]);
	aggregator = ar[3];
	source = ar[4];
	title = ar[5];
	titleContainsCompanyName = Boolean.parseBoolean(ar[6]);
	url = ar[7];
	datetime = ar[8];
	isUnreliableSource = Boolean.parseBoolean(ar[9]);
	pubTimeHasBeenInvestigated = Boolean.parseBoolean(ar[10]);
	openArticleToScrapePubtimeLater = Boolean.parseBoolean(ar[11]);
	openArticleToScrapePubDateLater = Boolean.parseBoolean(ar[12]);
	pubTimeFetchAttemps = Integer.parseInt(ar[13]);
	pubTimeFetchTotalFailure = Boolean.parseBoolean(ar[14]);

    }


    public String outputLine() {
	return ""
		+ date + "\t"
		+ (time.isEmpty() ? "     " : time) + "\t"
		+ isPressRelease + "\t"
		+ aggregator + "\t"
		+ source + "\t"
		+ title + "\t"
		+ titleContainsCompanyName + "\t"
		+ url + "\t"
		+ datetime + "\t"
		+ isUnreliableSource + "\t"
		+ pubTimeHasBeenInvestigated + "\t"
		+ openArticleToScrapePubtimeLater + "\t"
		+ openArticleToScrapePubDateLater + "\t"
		+ pubTimeFetchAttemps + "\t"
		+ pubTimeFetchTotalFailure;
    }

    public String onScreenOutputLine() {
	return onScreenOutputLine(false);
    }

    public String onScreenOutputLine(boolean isSub) {
	String lines;
	if (isSub)
	    lines = " __________________sub____  ";
	else
	    lines = " _________________________  ";

	return lines + date + " " + source + " | " + title + " | " + url;
    }

    public News(String date, String time, boolean isPressRelease, String aggregator, String source, String title, boolean titleContainsCompanyName, String url, boolean pubTimeHasBeenInvestigated) {
	this.date = date;
	this.time = time;
	this.isPressRelease = isPressRelease;
	this.aggregator = aggregator;
	this.source = source;
	this.title = title;
	this.titleContainsCompanyName = titleContainsCompanyName;
	this.url = url;
	datetime = date + "_" + time;
	isUnreliableSource = determineIsShittySource(url);
	this.pubTimeHasBeenInvestigated = pubTimeHasBeenInvestigated;
	pubTimeFetchAttemps = 0;						//redundant i think since this is default value
	pubTimeFetchTotalFailure = false;					//redundant i think since this is default value
    }


    public static List<String> getOutputLines(List<News> newsList) {

	newsList = preen(newsList);

	List<String> outputLines = new ArrayList();

	for (News news : newsList) {
	    outputLines.add(news.outputLine());
	}
	return outputLines;
    }

    @Override
    public int compareTo(Object o2) {
	return date.compareTo(((News)o2).date);
    }

    public boolean determineIsShittySource(String url) {
	for (String urlFragment : shittySources)
	    if (url.contains(urlFragment))
		return true;

	return false;
    }

    public String onScreenOutputBlock() {
	return outputLine().replaceAll("\t", "\n");
    }


    public static class Aggregators {
	public static final String Y = "y", G = "g";
    }


    public static class MyDateTime {
	public String date, time;

	public MyDateTime() {
	}

	public MyDateTime(String date, String time) {
	    this.date = date;
	    this.time = time;
	}

	@Override
	public String toString() {
	    return date + " " + time;
	}
    }

    public static class NoDateFoundException extends Exception {
	public NoDateFoundException() {
	}
    }

    /** text normalizer. <br>this text used a differetn whitespace for google vs. yahoo results.  so the texts weren't matching in skip(). <br>
     so remove whitespace and nonword characters and makes lowercase for comparisons.
    
     */
    public static Object normal(String title) {
	return title.toLowerCase().replaceAll("\\s", "").replaceAll("\\W", "");

    }

    public static List<News> finalSort(List<News> newsList) {

	Collections.sort(newsList, (News news1, News news2) -> news2.time.compareTo(news1.time)); //reverse
	Collections.sort(newsList, (News news1, News news2) -> news2.date.compareTo(news1.date)); //reverse
	Collections.sort(newsList, (News news1, News news2) -> news2.aggregator.compareTo(news1.aggregator));//reverse
	return newsList;
    }

    /** TODO THIS IS STUPID.  replace with iter.remove() style.  remove duplicates or inferior copies based on having timestamp or being press release.  */
    public static List<News> preen(List<News> newsList) {

	List<Integer> indicesToRemove = new ArrayList();

	for (int i1 = 0; i1 < newsList.size(); i1++) {
	    if (!indicesToRemove.contains(i1)) {
		News news1 = newsList.get(i1);

		for (int i2 = 0; i2 < newsList.size(); i2++) {
		    if (!indicesToRemove.contains(i2)) {
			News news2 = newsList.get(i2);

			if (i2 != i1 && !indicesToRemove.contains(i2) && news1.title.equals(news2.title) && (news1.date.equals(news2.date) || news1.url.equals(news2.url))) {

			    if (news1.isPressRelease == news2.isPressRelease || !news2.isPressRelease) {
				if (!news2.pubTimeHasBeenInvestigated)
				    indicesToRemove.add(i2);
				if (news1.pubTimeHasBeenInvestigated && news2.pubTimeHasBeenInvestigated)
				    indicesToRemove.add(i2);
			    }
			}
		    }
		}
	    }
	}
	if (G.listContainsDuplicates(indicesToRemove)) {
	    System.out.println("AAAGHGHGHGHGH indicesToRemove contains duplicates! wtf!!!!");
	    System.exit(0);
	}
	if (!indicesToRemove.isEmpty()) {
	    Collections.sort(indicesToRemove, Collections.reverseOrder());
	    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%    REMOVING     %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	    System.out.println("found " + indicesToRemove.size() + " news items to remove from list!");

	    for (int i : indicesToRemove) {
		System.out.println("REMOVING: " + newsList.get(i).outputLine().replaceAll("\t", "\n"));
		newsList.remove(i);
	    }
	    System.out.println("%%%%%%%%%%%%%%%%%%%%%%----- finished removing -----%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}

	return News.finalSort(newsList);
    }


    public static boolean doesNewsTitleContainCompanyName(List<Symbol> symbols, String newsTitle, String ticker) {

	for (Symbol symbol : symbols)
	    if (symbol.ticker.toLowerCase().equals(ticker.toLowerCase()))
		return doesNewsTitleContainCompanyName(symbol, newsTitle, ticker);
	return false;
    }

    public static boolean doesNewsTitleContainCompanyName(Symbol symbol, String newsTitle, String ticker) {
	return doesNewsTitleContainCompanyName(symbol.abridgedName, newsTitle, ticker);
    }

    public static boolean doesNewsTitleContainCompanyName(String abridgedName, String newsTitle, String ticker) {
	String firstWordOfCompany = abridgedName.split(" ")[0];

	if (newsTitle.toLowerCase().contains(firstWordOfCompany.toLowerCase()))
	    return true;

	String[] titleStrs = newsTitle.split("[ \\(\\)]");
	for (String str : titleStrs)
	    if (str.toLowerCase().equals(ticker.toLowerCase()))
		return true;
	return false;
    }
}
