package objects.sec;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import utilities.G;

public final class SECReport {

    public static void removeDatesGtE_to_date(List<SECReport> secList, String date) {

	for (Iterator<SECReport> iter = secList.iterator(); iter.hasNext();) {
	    SECReport secr = iter.next();

	    if (secr.date.compareTo(date) >= 0)
		iter.remove();
	}
    }

    /**  edgar's format */
    public String timestamp;
    /** sdf_date yearmoda */
    public String date;
    /** military time, G.sdf_militaryTime */
    public String time;
    public String type;

    public static List<SECReport> readFromDisk(String ticker) throws IOException, G.No_DiskData_Exception {
	List<SECReport> secList = new ArrayList();

	if (G.getSecDatesFile(ticker).exists()) {
	    List<String> lines = Files.readAllLines(G.getSecDatesFile(ticker).toPath());

	    for (String line : lines)
		secList.add(new SECReport(line));
	} else throw new G.No_DiskData_Exception();
	return secList;
    }

    public SECReport(String outputFileLine) {
	String[] ar = outputFileLine.split("\t");

	date = ar[0];
	time = ar[1];
	timestamp = ar[2];
	type = ar[3];
    }

    public String outputLine() {
	return ""
		+ date + "\t"
		+ time + "\t"
		+ timestamp + "\t"
		+ type;
    }

    public SECReport(Node report) throws XPathExpressionException, ParseException {

	XPath xPath = XPathFactory.newInstance().newXPath();

	String timestamp_ = (String)xPath.compile("./updated/text()").evaluate(report, XPathConstants.STRING);
	String type_ = (String)xPath.compile("./@term").evaluate(report, XPathConstants.STRING);

	String date_ = timestamp_.substring(0, 10).replaceAll("-", "");

	//	     2015-02-03T18:31:32-05:00
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	Date d = sdf.parse(timestamp_);


	String time_ = G.sdf_militaryTime.format(d);
	String date2 = G.sdf_date.format(d);

	if (!date_.equals(date2)) {
	    System.out.println("big problem -dates don't match in sec. " + timestamp_ + " " + date_ + " " + date2);
	    System.exit(0);
	}

	this.date = date_;
	this.time = time_;
	this.timestamp = timestamp_;
	this.type = type_;
    }

    public static List<String> getOutputLines(List<SECReport> secList) {

	secList = removeDuplicates(secList);
	secList = sortByTypeThenTimestamp(secList);

	List<String> outputLines = new ArrayList();

	for (SECReport sec : secList) {
	    outputLines.add(sec.outputLine());
	}
	return outputLines;
    }

    /** avoid thiS! it takes forevER! */
    private static List<SECReport> removeDuplicates(List<SECReport> list) {

	for (Iterator<SECReport> iterA = list.iterator(); iterA.hasNext();) {
	    SECReport secA = iterA.next();

	    boolean doRemoveIter1 = false;
	    for (Iterator<SECReport> iterB = list.iterator(); iterB.hasNext();) {
		SECReport secB = iterB.next();


		if (secA != secB && secA.timestamp.equals(secB.timestamp) && secA.type.equals(secB.type)) {
		    doRemoveIter1 = true;
		    break;
		}
	    }
	    if (doRemoveIter1) iterA.remove();
	}

	return list;
    }

    /** sort by date in reverse for newest on top */
    private static List<SECReport> sortByTypeThenTimestamp(List<SECReport> list) {
	Collections.sort(list, new Comparator<SECReport>() {
	    public int compare(SECReport o1, SECReport o2) {
		return o1.type.compareTo(o2.type);
	    }
	});
	Collections.sort(list, new Comparator<SECReport>() {
	    public int compare(SECReport o1, SECReport o2) {
		return o2.timestamp.compareTo(o1.timestamp);
	    }
	}); //reverse
	return list;
    }

}


/*

 diff types of SEC reports.  convert these to byte numeric names ? maybe not worth it.  per SECReport.X, store a list of all the different report types that were released per day, like:


 different types of SEC reports:

 10-K
 10-K/A
 10-KT
 10-Q
 10-Q/A
 11-K
 15-12B
 15-12G
 20-F
 20-F/A
 25-NSE
 3
 3/A
 4
 4/A
 40-F
 424B2
 424B3
 424B4
 424B5
 424B7
 425
 5
 6-K
 6-K/A
 8-A12B
 8-K
 8-K/A
 8-K12B
 ARS
 CERTNAS
 CERTNYS
 CORRESP
 CT ORDER
 D
 D/A
 DEF 14A
 DEFA14A
 DFAN14A
 DRS
 DRS/A
 DRSLTR
 EFFECT
 F-3ASR
 FWP
 IRANNOTICE
 NO ACT
 NT 10-K
 POS AM
 PRE 14A
 PREC14A
 PRER14A
 RW
 S-1
 S-1/A
 S-1MEF
 S-3
 S-3/A
 S-3ASR
 S-4
 S-4/A
 S-8
 S-8 POS
 SC 13D
 SC 13D/A
 SC 13G
 SC 13G/A
 SC TO-C
 SC TO-I
 SC TO-I/A
 SC TO-T
 SC TO-T/A
 SD
 UPLOAD
 */
