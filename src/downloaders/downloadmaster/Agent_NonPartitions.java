package downloaders.downloadmaster;
import java.io.IOException;
import java.text.ParseException;
import downloaders.regular_other.Replace_Weather;
import downloaders.regular_stocks_stuff.UdFlow_Earnings_Estimize;
import downloaders.regular_stocks_stuff.UdFlow_Earnings_Yahoo;
import downloaders.regular_stocks_stuff.UdFlow_Earnings_Zacks;

public class Agent_NonPartitions {

    public static void main(String[] args) throws InterruptedException, ParseException, IOException {

	UdFlow_Earnings_Yahoo.go(args);
	UdFlow_Earnings_Estimize.go(args);
	UdFlow_Earnings_Zacks.go(args);

	Replace_Weather.initiateWeatherDataNOAAOnlineRequest();

	Thread.sleep(1000 * 60 * 5);
	
	try {
	    Replace_Weather.downloadCompletedNOAA_file();
	    return;
	} catch (IOException ex) {
	}

	Thread.sleep(1000 * 60 * 15);
	try {
	    Replace_Weather.downloadCompletedNOAA_file();
	    return;
	} catch (IOException ex) {
	}

	Thread.sleep(1000 * 60 * 60 * 4);
	try {
	    Replace_Weather.downloadCompletedNOAA_file();
	    return;
	} catch (IOException ex) {
	}

	Thread.sleep(1000 * 60 * 60 * 4);
	try {
	    Replace_Weather.downloadCompletedNOAA_file();
	    return;
	} catch (IOException ex) {
	}
    }


}
