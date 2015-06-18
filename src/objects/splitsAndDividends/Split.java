package objects.splitsAndDividends;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import utilities.G;
import static utilities.G.isnull;
import static utilities.G.null_byte;
import static utilities.G.null_int;
import static utilities.G.parse_Str;
import static utilities.G.parse_byte;
import static utilities.G.parse_int;

/** a good split is when n1 > n2.  called a fwd split.  makes the price go down per share. */
public class Split {

    /** is this the cooked date?  are all splits cooKED dateS???   uh i hope so*/
    public int date = null_int;
    public byte n1 = null_byte;
    public byte n2 = null_byte;

    public Split(String line) throws G.No_DiskData_Exception {
	String[] lineAr = line.split(",");
	this.date = parse_int(lineAr[0]);
	this.n1 = parse_byte(lineAr[1]);
	this.n2 = parse_byte(lineAr[2]);
	if (isnull(date)) throw new G.No_DiskData_Exception();
    }

    public Split(String dateStr, String n1Str, String n2Str) {
	this.date = Integer.parseInt(dateStr);
	this.n1 = Byte.parseByte(n1Str);
	this.n2 = Byte.parseByte(n2Str);
    }

    public String outputLine() {
	return ""
		+ parse_Str(date) + ","
		+ parse_Str(n1) + ","
		+ parse_Str(n2);
    }

    public static List<Split> readFromDisk(String ticker) throws IOException, G.No_DiskData_Exception {
	File file = G.getSplitsFile(ticker);
	List<Split> splits = new ArrayList();
	if (file.exists()) {
	    List<String> lines = Files.readAllLines(file.toPath());
	    for (String line : lines)
		splits.add(new Split(line));
	}
	return splits;
    }

    public static void writeToDisk(List<Split> splits, String ticker) throws IOException {

	File file = G.getSplitsFile(ticker);

	if (!splits.isEmpty())
	    Files.write(file.toPath(), Split.getOutputLines(splits), StandardCharsets.UTF_8);

    }

    public static List<String> getOutputLines(List<Split> splits) {

	List<String> outputLines = new ArrayList();
	splits = removeDuplicates(splits);
	splits = sortByTypeThenTimestamp(splits);

	for (Split split : splits) {
	    outputLines.add(split.outputLine());
	}
	return outputLines;
    }


    private static List<Split> removeDuplicates(List<Split> list) {
	for (Iterator<Split> iterA = list.iterator(); iterA.hasNext();) {
	    Split A = iterA.next();

	    boolean removeA = false;
	    for (Iterator<Split> iterB = list.iterator(); iterB.hasNext();) {
		Split B = iterB.next();

		if (A != B && A.date == B.date && A.n1 == B.n1 && A.n2 == B.n2)
		    removeA = true;
	    }
	    if (removeA)
		iterA.remove();
	}

	return list;
    }

    private static List<Split> sortByTypeThenTimestamp(List<Split> list) {

	Collections.sort(list, new Comparator<Split>() {	//reverse
	    @Override
	    public int compare(Split o1, Split o2) {

		return Integer.compare(o2.date, o1.date);
	    }
	});
	return list;
    }

    boolean isGood() {
	return n1 > n2;
    }


}
