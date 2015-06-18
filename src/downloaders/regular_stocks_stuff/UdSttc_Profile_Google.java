package downloaders.regular_stocks_stuff;

import java.util.List;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import utilities.G;
import utilities.G.GoogleBlockedMeException;
import utilities.HttpDownloadUtility;
import objects.profile.GoogleProfile;

public class UdSttc_Profile_Google {

    public static void main(String[] args) throws InterruptedException, IOException, ParseException, GoogleBlockedMeException {
	go(args);
    }

    public static void go(String[] args) throws InterruptedException, IOException, ParseException, GoogleBlockedMeException {
	Integer input = G.initialize("Download_GoogleProfile.go", args, G.googleRelatedCompaniesDir, G.googleFactsetCategoriesDir);

	for (String ticker : G.getIncompleteTickersSubset(args, G.googleProfileCompletedDummyDir)) {
	    System.out.print("d_grc " + input + ": " + ticker + " -- ");

	    if (downloadGoogleProfileYeah(ticker)) {
		G.notateCompletion(G.getGoogleProfileUpdatedDummyFile(ticker));
		System.out.print("success!\n");
	    } else System.out.println();
	}
    }

    private static GoogleProfile downloadGoogleProfile(String ticker) throws InterruptedException, GoogleBlockedMeException, TickerDNEInGoogleProfiles, GoogleHasThisTickerButNotCatsOrRelatedCos, GoogleIsntSureWhatStockThisTickerRefersTo {

	Matcher GRCmatcher, factSetCatMatcher;
	Pattern pattern1 = Pattern.compile("(?s),\\{id:\"\\d+\",values:\\[\"([^\"]+)\"");
	Pattern pattern2 = Pattern.compile("(?s)<td class=symbol>\\s*<a [^>]+>([^<]+)<");	//https://www.google.com/finance?q=acsf

	/*
	 <td class=symbol>
	 <a id=rct-4 href="/finance?q=NASDAQ:OXLC&ei=R23rVOHNL9SG8QbV64GYBQ" >OXLC</a>
	 */
	/*Sector: <a id=sector href="?catid=us-TRBC:54&amp;ei=1nTrVNHtNJCX8Abz3YDgBQ" >Non-Cyclical Consumer Goods &amp; Services</a> &gt; Industry: <a href="?catid=us-TRBC:5410202010&amp;ei=1nTrVNHtNJCX8Abz3YDgBQ" >Food Processing - NEC</a>	*/
	Pattern factSetCategoryPattern = Pattern.compile("(?s).*Sector: <a id=sector[^>]+>([^<]+)</a>[^>]+>([^<]+)<.*");
	//factset categories seem to be only shown when google related companies are listed on main page.  perhaps they will exist on main page when grc's are not.  they seem to only exist on the main page tho


	List<String> relatedCompanies = new ArrayList();
	String sector = "", industry = "";

	String[] possibleUrls = new String[]{
	    "https://www.google.com/finance?q=:" + ticker, //extra colon needed to catch "a"
	    "https://www.google.com/finance?q=" + ticker,
	    "https://www.google.com/finance?q=:" + ticker.replaceAll("-", "."),
	    "https://www.google.com/finance?q=" + ticker.replaceAll("-", ".")
	};

	boolean badTicker = true;

	for (String url : possibleUrls) {
	    String source = "";
	    try {
		source = HttpDownloadUtility.getPageSource(url); //.replaceAll("\\s+", " ");
	    } catch (IOException e) {
		if (e.getMessage().contains("Server returned HTTP response code: 503"))
		    throw new G.GoogleBlockedMeException();
	    }
	    if (!source.contains("produced no matches."))
		badTicker = false;

	    GRCmatcher = pattern1.matcher(source);
	    while (GRCmatcher.find())
		relatedCompanies.add(GRCmatcher.group(1));

	    factSetCatMatcher = factSetCategoryPattern.matcher(source);
	    if (factSetCatMatcher.matches()) {
		sector = G.cleanUpHtmlSyntax(factSetCatMatcher.group(1));
		industry = G.cleanUpHtmlSyntax(factSetCatMatcher.group(2));
	    }

	    if (relatedCompanies.isEmpty()) {
		GRCmatcher = pattern2.matcher(source);
		while (GRCmatcher.find())
		    relatedCompanies.add(GRCmatcher.group(1));

		if (!relatedCompanies.isEmpty()) relatedCompanies.remove(0);			//this format lists the original company with related companies.
	    }
	    if (!relatedCompanies.isEmpty()) break;


	    if (source.contains("Link to this view") && relatedCompanies.isEmpty() && sector.isEmpty() && industry.isEmpty())
		throw new GoogleHasThisTickerButNotCatsOrRelatedCos();

	}

	if (relatedCompanies.isEmpty() && sector.isEmpty() && industry.isEmpty())
	    throw new GoogleIsntSureWhatStockThisTickerRefersTo();
	if (badTicker)
	    throw new TickerDNEInGoogleProfiles();
	
	System.out.print(" " + sector + "  |  " + industry + "  |  " + relatedCompanies + "  -- ");

	GoogleProfile gp = new GoogleProfile(ticker, relatedCompanies, sector, industry);
	return gp;

    }

    private static boolean downloadGoogleProfileYeah(String ticker) throws IOException, InterruptedException, ParseException, GoogleBlockedMeException {

	boolean success___dont_need_to_try_again_later = true;

	if (G.fileIsMoreThanThisManyDaysOld(G.getGoogleRelatedCompaniesFile(ticker), 10)) {
	    GoogleProfile gp;
	    try {
		gp = downloadGoogleProfile(ticker);
	    } catch (TickerDNEInGoogleProfiles ex) {
		System.out.print("------ bad ticker -- ");
		return true;
	    } catch (GoogleHasThisTickerButNotCatsOrRelatedCos ex) {
		System.out.print("------ no related cos or cats for this ticker --");
		return true;
	    } catch (GoogleIsntSureWhatStockThisTickerRefersTo ex) {
		System.out.print("------ found the ticker, but isn't sure what stock it goes with.  probably not a public US company or security --");
		return true;
	    }
	    if (gp == null)
		return false;		//try again later.  something weird happened.  not sure how to determine if a ticker is valid or not since we're trying 4 different urls ... why is this so different from gettin ggoogle news?  oh ... wait idk... wtf.  so idfk.  just try again with the missing ones a couple times.  wasted time but whatever, this can be done at any time
	    gp.writeToFile();
	}
	return success___dont_need_to_try_again_later;
    }

    public static class TickerDNEInGoogleProfiles extends Exception {
	public TickerDNEInGoogleProfiles() {
	}
    }

    private static class GoogleHasThisTickerButNotCatsOrRelatedCos extends Exception {
	public GoogleHasThisTickerButNotCatsOrRelatedCos() {
	}
    }

    private static class GoogleIsntSureWhatStockThisTickerRefersTo extends Exception {
	public GoogleIsntSureWhatStockThisTickerRefersTo() {
	}
    }
}


    //note:  google page lists upcoming earnings releases
//NOTE:  check out citigroup's related companies:
//https://www.google.com/finance?q=:c
//they're all ETF's -- makes me think that google's related companies is calculated by
//	price correlations, not similary business models.  
//          WEIRD!       google url's don't work like i've been using them. 
//	TODO go back and fix news urls.  actually news articles get to "a" just fine w/out extra crap.  i think main pages to to "search" which is a little screwy
// this works:		https://www.google.com/finance?q=aapl
// but this does not!!!:	https://www.google.com/finance?q=a
//	this does:		https://www.google.com/finance?q=NYSE:A
//so does this (wtf):	https://www.google.com/finance?q=NASDAQ:A
//and this:			https://www.google.com/finance?q=amex:A
//and this:			https://www.google.com/finance?q=wowwowwow:A
//and thsi:			https://www.google.com/finance?q=stock:brk.b
//and thsi:			https://www.google.com/finance?q=stock:aapl
//wrong!!!
//https://www.google.com/finance?q=bac-b
//	    FUCK!  
//		    Arbor Realty Trust Inc(NYSE:ABR)
//		google thinks this company's ticker is ABR, yahoo says abrn
//TODO https://www.google.com/finance?q=acsf
//this page has good data!  download it!  needs separate regex
//	    FUCK!!! our symbols are not quite right!  afge should be afg.  abrn should be abr.  (for google and morningstar)
//why did ASX fail??
//if blank results, try url w/ or w/out the :
//sometimes i think the pages just dont load. random.
//asb-w?????
/* google blocking notes.   probably copied above, also -scr


	
 boolean success___dont_need_to_try_again_later = true;

 List<News> newsList = News.readNewsFile(G.getGoogleNewsFile(ticker));

 String lastRecordedDate = newsList.isEmpty() ? null : newsList.get(0).date;

 String urlTemplate = "https://www.google.com/finance/com999999pany_news?q=" + tickerKey + "&start=0&num=10000";
 String googleNewsUrl = urlTemplate.replace(G.tickerKey, ticker.replace("-", "."));
 String source = HttpDownloadUtility.getPageSource(googleNewsUrl);
	



 run:
 0
 Download_News2.getAllHistoricalNews
 d_hp null: ajasdgasdg342aw4t4wTWT$#$#$#$Tx: 
 failed to load page. try again in 1 seconds...https://www.google.com/finance/com999999pany_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
 failed 2nd time. try again in 3 seconds...https://www.google.com/finance/com999999pany_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
 failed the 3rd time. giving up https://www.google.com/finance/com999999pany_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
 java.io.FileNotFoundException: https://www.google.com/finance/com999999pany_news?q=ajasdgasdg342aw4t4wTWT$#$#$#$Tx&start=0&num=10000
 at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1834)
 at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1439)
 at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:254)
 at stuff.HttpDownloadUtility.getWebInputStream(HttpDownloadUtility.java:119)
 at stuff.HttpDownloadUtility.getWebInputStream(HttpDownloadUtility.java:89)
 at stuff.HttpDownloadUtility.getPageSource(HttpDownloadUtility.java:132)
 at stuff.downloaders.UdFlow_News_Google.downloadGoogleNews(UdFlow_News_Google.java:613)
 at stuff.downloaders.UdFlow_News_Google.go(UdFlow_News_Google.java:69)
 at stuff.downloaders.UdFlow_News_Google.main(UdFlow_News_Google.java:50)
 BUILD SUCCESSFUL (total time: 5 seconds)




 ------------------------------

 blocked around 1:40 pm

 i
 1786 89639 true true
 1787 89639 true true
 1788 89639 true true
 1789 89639 true true
 1790 89639 true true
 1791 89639 true true
 1792 89639 true true
 1793 89639 true true
 1794 89639 true true
 1795 89639 true true
 1796 89639 true true
 1797 89639 true true
 1798 89397 true true
 failed to load page. try again in 1 seconds...https://www.google.com/finance/company_news?q=ajx&start=0&num=10000
 failed 2nd time. try again in 3 seconds...https://www.google.com/finance/company_news?q=ajx&start=0&num=10000
 failed the 3rd time. giving up https://www.google.com/finance/company_news?q=ajx&start=0&num=10000
 java.io.IOException: Server returned HTTP response code: 503 for URL: https://ipv4.google.com/sorry/IndexRedirect?continue=https://www.google.com/finance/company_news%3Fq%3Dajx%26start%3D0%26num%3D10000&q=CGMSBMZWHRMYrfuBqAUiGQDxp4NL7D8YzDrouJzwCR4Lr1D6CwG_5Uk
 at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1838)
 at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1439)
 at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:254)
 at stuff.HttpDownloadUtility.getWebInputStream(HttpDownloadUtility.java:119)
 at stuff.HttpDownloadUtility.getWebInputStream(HttpDownloadUtility.java:89)
 at stuff.HttpDownloadUtility.getPageSource(HttpDownloadUtility.java:132)
 at stuff.downloaders.UdFlow_News_Google.getGoogleNewsPageSource(UdFlow_News_Google.java:594)
 at stuff.Test.main(Test.java:19)
 BUILD SUCCESSFUL (total time: 16 minutes 45 seconds)


 */
