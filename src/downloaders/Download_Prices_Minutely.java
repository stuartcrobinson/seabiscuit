package downloaders;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;
import utilities.G;
import utilities.HttpDownloadUtility;

public class Download_Prices_Minutely {

    //ignore this for now... or should i?
    
    
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
	go(args);
    }

    /* input int 0, 1, 2, or 3 for subdivision of tickers list */
    public static void go(String[] args) throws IOException, InterruptedException, ParseException {
	Integer input = G.initialize("download yahoo minutely historical prices", args);

	for (String ticker : G.getIncompleteTickersSubset(args, G.pricesMinutelyCompletedDummyDir)) {
	    System.out.println("d_pM " + input + ": " + ticker);

	    if (downloadMinutelyPrices(ticker)) {
		G.notateCompletion(G.getPricesMinutelyUpdatedDummyFile(ticker));
	    }
	}
    }

    /** returns true if don't try again later (save success dummy).  false if try again later. save failure link. */
    private static boolean downloadMinutelyPrices(String ticker) throws IOException, InterruptedException, ParseException {

	//dont make a fodler if theres no data!!!
	String url = "http://www.google.com/finance/getprices?q=" + ticker.toUpperCase() + "&i=60&p=15d&f=d,o,h,l,c,v";

	//if G.getPricesMinutelyFile(ticker) is old enough or DNE. ....wait .... where is previous file?? maybe all data should be in same folder per ticker.  
	//	title of file: the date downloaded
	//	to determine if need download again, sort files in folder by modified date.  take biggest date.  if that date is from more than 10 days ago, download new!

	if (!timeToDownloadNewPricesMinutely(ticker))
	    return true;


//	String source = HttpDownloadUtility.getPageSource(url);
	List<String> lines = HttpDownloadUtility.getFile(url);
	/* dataless file:
	 EXCHANGE%3DOTCMKTS
	 MARKET_OPEN_MINUTE=570
	 MARKET_CLOSE_MINUTE=960
	 INTERVAL=60
	 COLUMNS=DATE,CLOSE,HIGH,LOW,OPEN,VOLUME
	 DATA=

	 */
	if (lines.size() < 10) {
	    if (lines.get(0).contains("EXCHANGE"))
		return true;
	    else
		G.recordFailure(G.getPricesMinutelyFailedLinksFile(ticker), ticker, url);
	}
	G.getPricesMinutelyFile(ticker).getParentFile().mkdirs();
	FileUtils.writeLines(G.getPricesMinutelyFile(ticker), lines);
	return true;
	    //determine behavior for bad ticker
	//determine behavior for freak accident / bad internet

	    //next, (after downloading) - figure out how to interpert timestamps / time-offsets?

	//cos like, do i need to store the time/date of download?  can i trust the file's modified() time/date for that?
	    /*
	
	 wtf does this stuff mean?
	
	 ticker must be in all caps
	 http://www.google.com/finance/getprices?q=AAPL&i=60&p=15d&f=d,o,h,l,c,v
	 http://www.google.com/finance/getprices?q=BRK.A&i=60&p=15d&f=d,o,h,l,c,v
	 http://www.google.com/finance/getprices?q=A&i=60&p=15d&f=d,o,h,l,c,v
	 http://www.google.com/finance/getprices?q=INOV&i=60&p=15d&f=d,o,h,l,c,v
	
	
	 EXCHANGE%3DNASDAQ
	 MARKET_OPEN_MINUTE=570
	 MARKET_CLOSE_MINUTE=960
	 INTERVAL=60
	 COLUMNS=DATE,CLOSE,HIGH,LOW,OPEN,VOLUME
	 DATA=
	 TIMEZONE_OFFSET=-300
	 a1424356200,128.41,128.48,128.41,128.48,335266
	 1,128.63,128.64,128.4,128.42,305909
	 2,128.58,128.7,128.57,128.63,270165
	 3,128.71,128.75,128.5951,128.6,270065
	 4,128.559,128.72,128.54,128.71,171514
	 5,128.47,128.57,128.33,128.5501,336353
	 6,128.56,128.57,128.41,128.47,148900
	 7,128.4956,128.56,128.46,128.54,149683
    
		
	 */


    }

    /** true if most recent file in ticker dir is older than 10 days  old */
    private static boolean timeToDownloadNewPricesMinutely(String ticker) throws IOException, ParseException {
	File[] files = G.getPricesMinutelyTickerDir(ticker).listFiles();

	if (files == null || files.length == 0)
	    return true;	//no files - time do download some!



	TreeSet<File> set = new TreeSet(new Comparator<File>() {    //sorted by date increasing
	    @Override
	    public int compare(File o1, File o2) {
		return (new Date(o1.lastModified())).compareTo(new Date(o2.lastModified()));
	    }
	});
	set.addAll(Arrays.asList(files));


	File mostRecentFile = set.last();

	return G.fileIsMoreThanThisManyDaysOld(mostRecentFile, 10);
    }

}
/*
 from old eclipse workspace:

 package intraDayWorld;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;

 import org.apache.commons.io.IOUtils;

 //proxy option
 //anonymouse.  estimated total time:  5.5 to 8 hours
 //

 //RUN THIS AGAIN AND GET ALL DATA!  o,l,h,c,v

 //9,7,8,16

 //should take about 2 hours
 public class DataDownloaderSCR {
 public static void main(String[] args) throws IOException {
 //http://www.marketcalls.in/database/google-realtime-intraday-backfill-data.html
 //1.  open C:\Users\User\Documents\stocks\symbols.txt
 //2.  iterate through these stock symbols.  
 //		2b.  download each yahoo historical chart, save file with that name in C:\Users\User\Documents\stocks\data

 final long startTime = System.currentTimeMillis();

 Date dNow = new Date( );
 SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd hh.mm a");
 String dateStr = ft.format(dNow);


 String dirStr = "C:\\Users\\User\\Documents\\stocks\\data\\minutely\\"+ dateStr;
 String dataDirStr = dirStr +"\\rawSourceData";
 String tickersListOutStr = dirStr +"\\symbols.npp";

 File dir = new File(dirStr);
 File dataDir = new File(dataDirStr);

 dir.mkdir();
 dataDir.mkdir();

 List<String> tickersList = Files.readAllLines(
 Paths.get("C:\\Users\\User\\Documents\\stocks\\archive\\Data Downloader\\Google Intraday.txt"),
 StandardCharsets.UTF_8);
 Files.write(Paths.get(tickersListOutStr), tickersList, StandardCharsets.UTF_8);

 int j = 0;
 int i = tickersList.size()+1;
 for (String tickerInfoStr : tickersList){
 i--;
 j++;
 if (j % 100 == 0) 
 System.out.println("loop "+ j + ".  "
 + (System.currentTimeMillis() - startTime)/1000.0 
 + " seconds" );
 String [] tickerInfo = tickerInfoStr.split(",");
 String symbol = tickerInfo[0];
 String exchange = tickerInfo[1];

 String googleFinance_URL = 	"http://www.google.com/finance/getprices?" +
 "q=" + symbol + "&x=" + exchange + "&i=60&p=15d&f=d,o,l,h,c,v";


 //TODO what happens when it hits a read time out?  does it try it again?  or just lose the data....

 boolean failed = false;

 do {
 try {
 URL url = new URL(googleFinance_URL);			//"http://anonymouse.org/cgi-bin/anon-www.cgi/" +
 URLConnection con = url.openConnection();
 //			con.setConnectTimeout(20_000);
 //			con.setReadTimeout(20_000);

 InputStream is = con.getInputStream();
 List<String> source = IOUtils.readLines(is);
 IOUtils.closeQuietly(is);

 System.out.format("%6d %12s %5s%n", i, symbol, exchange);//(symbol +" "+ exchange + " "+ source.size());

 String outputFileNameStr = dataDir +"\\"+ symbol +"_"+ exchange +".ggl";

 Files.write(Paths.get(outputFileNameStr), source,  StandardCharsets.UTF_8);
 failed = false;
 }catch(Exception e){
 System.err.println(e);
 failed = true;
 }
 }while (failed);
 }
 System.out.println((System.currentTimeMillis() - startTime)/1000.0 +" seconds" );

 }
 }

 //go ahead and format the data now. //NO
 //NO
 //NO -- format data later.  just save raw google files.  they are so efficient.
 //
 //
 //Pattern linePattern = Pattern.compile(".*=([,\\w]*).*");
 //Matcher lineMatcher;
 //
 //
 //			int len = source.size();
 //			String row0 = source.get(0);
 //			String row1 = source.get(1);
 //			String row2 = source.get(2);
 //			String row3 = source.get(3);
 //			String row4 = source.get(4);
 //			String row5 = source.get(5);
 //			String row6 = source.get(6);
 //
 //
 //			lineMatcher = linePattern.matcher(row1);									
 //			lineMatcher.find();							
 //			String MARKET_OPEN = lineMatcher.group(1);
 //
 //			lineMatcher = linePattern.matcher(row2);									
 //			lineMatcher.find();							
 //			String MARKET_CLOSE = lineMatcher.group(1);
 //
 //			lineMatcher = linePattern.matcher(row3);									
 //			lineMatcher.find();							
 //			String INTERVAL = lineMatcher.group(1);
 //
 //			lineMatcher = linePattern.matcher(row6);									
 //			lineMatcher.find();							
 //			String TIMEZONE_OFFSET = lineMatcher.group(1);
 //
 //			String priceSetOutputFileName 
 //			= 		symbol +"_"+ 
 //					exchange +"_"+ 
 //					MARKET_OPEN +"_"+ 
 //					MARKET_CLOSE +"_"+ 
 //					INTERVAL +"_"+ 
 //					TIMEZONE_OFFSET +".csv";
 //
 //			for (int i = 7; i < len; i++){
 //				if (source.get(i).startsWith("a")){
 //					//do date stuff
 //				}
 //
 //			}






 //		
 //		String fileURL_body = "http://ichart.finance.yahoo.com/table.csv?s=";
 //		String saveDir = "C:\\Users\\User\\Documents\\stocks\\data2";
 //		String symbolsFileName = "C:\\Users\\User\\Documents\\stocks\\symbols.txt";
 //
 //		try {
 //			BufferedReader reader = new BufferedReader(new FileReader(symbolsFileName));
 //			String symbol;
 //			while ((symbol = reader.readLine()) != null)   {
 //
 //				String fileURL = fileURL_body + symbol;
 //				String fileName = symbol + ".csv";
 //
 //				HttpDownloadUtility.downloadFile(fileURL, saveDir, fileName);
 //			}
 //			reader.close();
 //		} catch (Exception ex) {
 //			ex.printStackTrace();
 //		}

 */
