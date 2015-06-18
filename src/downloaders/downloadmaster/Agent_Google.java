package downloaders.downloadmaster;
import java.io.IOException;
import java.text.ParseException;
import utilities.G.GoogleBlockedMeException;
import downloaders.regular_stocks_stuff.UdFlow_News_Google;
import downloaders.regular_stocks_stuff.UdSttc_Profile_Google;

public class Agent_Google {

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {

	for (int i = 0; i < 5; i++) {
	    try {
		UdFlow_News_Google.go(args);
	    } catch (GoogleBlockedMeException ex) {
		Thread.sleep(1000 * 60 * 45);
	    }
	}

	for (int i = 0; i < 5; i++) {
	    try {
		UdSttc_Profile_Google.go(null);
	    } catch (GoogleBlockedMeException ex) {
		Thread.sleep(1000 * 60 * 45);
	    }
	}


    }


}
