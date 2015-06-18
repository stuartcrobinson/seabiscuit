package downloaders.regular_stocks_stuff;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utilities.G;
import utilities.HttpDownloadUtility;
import static utilities.G.cleanUpHtmlSyntax;
import static utilities.G.getTagNode;
import static utilities.G.initialize;
import static utilities.G.isnull;
import static utilities.G.parse_byte;
import static utilities.G.parse_int;
import static utilities.G.parse_long;
import static utilities.HttpDownloadUtility.getPageSource;
import objects.people.Person;

//TODO this is stupid.  it's download reuters and bloomberg ppl simultaneously.  download them separately, then combine later.  

//NOTE:  i don't have a great way of telling when a failure is an inernet problem vs. invalid ticker :(  keep an eye out for exceptions
public class UdSttc_People {

    public static void main(String[] args) throws IOException, ParseException, InterruptedException, XPathExpressionException, XPatherException, EmptyTableException {
//	go(args);
////	go(new String[]{"0"});
//	go(new String[]{"1"});
	go(new String[]{"2"});
////	go(new String[]{"3"});
//	go(args);
//	go(args);
    }

    /** updates every 20? days */
    public static void go(String[] args) throws IOException, ParseException, InterruptedException, XPathExpressionException, XPatherException, EmptyTableException {
	Integer input = initialize("Download_People.go", args, new File[]{G.peopleDir, G.peopleCompletedDummyDir});

	for (String ticker : G.getIncompleteTickersSubset(args, G.peopleCompletedDummyDir)) {
	    System.out.print("d_ppl " + input + ": " + ticker + ": " + reutersUrl(ticker) + "\t" + bloombergOverviewUrl(ticker) + "  --  ");

	    if (downloadPeople(ticker)) {
		G.notateCompletion(G.getPeopleUpdatedDummyFile(ticker));
		System.out.print("success!\n");
	    } else System.out.println();
	}
    }


    /** reuters is good enough for now.  don't worry about bloomberg.  site is too freaking unreliable.  only benefit is a few more board members. and # board relationships */
    private static List<Person> getReuterPeople(String ticker) throws TickerDoesntExistInReutersException, IOException, XPatherException, EmptyTableException, TryAgainLaterException {

	System.out.println("HERE");
	List<Person> people = new ArrayList();

	String reutersUrl = reutersUrl(ticker);

	String source = null;
	try {
	    //	try {
	    source = getPageSource(reutersUrl);
	} catch (InterruptedException | IOException ex) {
	    System.out.println("here99");
	    G.recordFailure(G.getPeopleFailedLinksFile(ticker), ticker, reutersUrl);
	    throw new TryAgainLaterException();
	}
	if (source.contains("Page Not Found")) {
	    System.out.println("page not found");
	    throw new TryAgainLaterException();
	}
	if (source.contains("No search results match the term ")) {
	    System.out.println("No search results match the term ");
	    throw new TickerDoesntExistInReutersException();
	}
//	System.out.println(source);
//	} catch (Exception e) {
//	    Person.recordFailedUrl(ticker, reutersUrl);
//	    throw e;
//	}

	if (source.contains("Stock Quotes & Company News"))
	    return people;

	TagNode node = getTagNode(source);

	Object[] table_summary = node.evaluateXPath(
		"//*[@class='column1 gridPanel grid8']/div[@id=\"companyNews\"][1]/div/div[@class=\"moduleBody\"]//tbody");
	Object[] table_basicComp = node.evaluateXPath(
		"//*[@class='column2 gridPanel grid4']/div[@id=\"companyNews\"][1]/div/div[@class=\"moduleBody\"]//tbody");//table_Biographies
	Object[] table_Biographies = node.evaluateXPath(
		"//*[@class='column1 gridPanel grid8']/div[@id=\"companyNews\"][2]/div/div[@class=\"moduleBody\"]//tbody");//table_Biographies
//		"//div[@class='module' and contains(., 'Biographies')]//tbody");	//TODO -- replace the above two w/ 'Summary' and 'Compensation' respectively
	//oh yeah ... this is why we don't use TagNode anymore ... it doesn't support standard xpath functions like "and" and "contains" etc
	/*	column1 gridPanel grid8	*/

	if (table_summary.length > 0 && table_basicComp.length > 0) {
	    TagNode tableSummaryNode = (TagNode)table_summary[0];
	    TagNode tableCompensationNode = (TagNode)table_basicComp[0];
	    TagNode tableBiographiesNode = (TagNode)table_Biographies[0];

	    Object[] nameCells = tableSummaryNode.evaluateXPath("//tr[position() > 1]/td[1]");
	    Object[] ageCells = tableSummaryNode.evaluateXPath("//tr[position() > 1]/td[2]");
	    Object[] sinceCells = tableSummaryNode.evaluateXPath("tr[position() > 1]/td[3]");
	    Object[] curPosnCells = tableSummaryNode.evaluateXPath("tr[position() > 1]/td[4]");

	    Object[] nameCompCells = tableCompensationNode.evaluateXPath("//tr[position() > 1]/td[1]");
	    Object[] fiscalYearTotalCells = tableCompensationNode.evaluateXPath("//tr[position() > 1]/td[2]");

	    Object[] nameBiographyCells = tableBiographiesNode.evaluateXPath("//tr[position() > 1]/td[1]");
	    Object[] bioBiographyCells = tableBiographiesNode.evaluateXPath("//tr[position() > 1]/td[2]");

	    if (nameCells.length == 0) {
		System.out.format("%nempty table %s", reutersUrl);
		throw new EmptyTableException();
	    }

	    for (int j = 0; j < nameCells.length; j++) {
		String name = cleanUpHtmlSyntax(((TagNode)nameCells[j]).getText().toString());
		String age = cleanUpHtmlSyntax(((TagNode)ageCells[j]).getText().toString());
		String since = cleanUpHtmlSyntax(((TagNode)sinceCells[j]).getText().toString());
		String curPos = cleanUpHtmlSyntax(((TagNode)curPosnCells[j]).getText().toString());
		String nameComp = cleanUpHtmlSyntax(((TagNode)nameCompCells[j]).getText().toString());
		String fiscalYearTotal = cleanUpHtmlSyntax(((TagNode)fiscalYearTotalCells[j]).getText().toString().replace("--", "").replace(",", ""));
		String nameBio = cleanUpHtmlSyntax(((TagNode)nameBiographyCells[j]).getText().toString());
		String bioBio = cleanUpHtmlSyntax(((TagNode)bioBiographyCells[j]).getText().toString());

		bioBio = bioBio.replaceAll("\\.", "").toLowerCase().replaceAll(",", " ").replaceAll("  ", " ").trim();


		String[] bioAr = bioBio.split(" ");

		boolean isFemale = determineIsFemaleFromBio(bioAr);
		boolean isMD = determineIsMDFromBio(bioAr);
		boolean isPhD = determineIsPhDFromBio(bioAr);
		boolean isDr = determineIsDrFromBio(bioAr) || isMD || isPhD;

		if (!name.equals(nameComp) || !name.equals(nameBio)) {
		    System.out.println("names don't match!");
		    System.exit(0);
		}

		Person man = new Person(j + 1, name, age, since, curPos, fiscalYearTotal, isFemale, isMD, isPhD, isDr);
		man.url_reuters = reutersUrl;
//		System.out.println(man.outputLine().replaceAll("\\\t", ",\n") + "\n" + bioBio + "\n");

//		System.out.println(G.parse_Str(isFemale) + " " + G.parse_Str(isMD) + " " + G.parse_Str(isPhD) + " " + G.parse_Str(isDr) + " " + bioBio);

		people.add(man);
	    }

	}
	return people;
    }

    private static boolean determineIsFemaleFromBio(String[] ar) {
	if (ar[0].equals("ms") || ar[0].equals("mrs") || ar[0].equals("miss"))
	    return true;

	for (String st : ar) {
	    if (st.equals("her") || st.equals("she") || st.equals("hers"))
		return true;
	}
	return false;
    }

    private static boolean determineIsDrFromBio(String[] ar) {
	return ar[0].equals("dr");
    }

    private static boolean determineIsMDFromBio(String[] ar) {

	for (int i = 0; i < ar.length && i < 10; i++)
	    if (ar[i].equals("md"))
		return true;

	return false;
    }

    private static boolean determineIsPhDFromBio(String[] ar) {

	for (int i = 0; i < ar.length && i < 10; i++)
	    if (ar[i].equals("phd"))
		return true;

	return false;
    }


    private static void supplementPeopleWithBloombergPeople(String ticker, List<Person> people) throws InterruptedException, IOException, XPathExpressionException, BloombergTryAgainLater, BloombergFailedDontTryAgainLater, G.TryAgainLater {

	if (people.isEmpty()) return;

	Map<String, String> id_name_map = new LinkedHashMap();

	addBloombergPersonIDsFromPage_toMap(people, id_name_map, bloombergOverviewUrl(ticker), ticker, "overview");
	addBloombergPersonIDsFromPage_toMap(people, id_name_map, bloombergBoardMemsUrl(ticker), ticker, "boardmem");

	//now go to webpages for each personID and get info!
	for (Entry<String, String> entry : id_name_map.entrySet()) {
	    try {
		updatePeopleWithNewBloombergPerson(ticker, entry.getKey(), entry.getValue(), people);
	    } catch (Exception e) {
		System.out.println("STRANGE ERROR -- FIX THIS LATER!!! happens when person's website gets redirected to a generic website.  null node when trying to get business board info i think. for: " + entry.getKey() + " " + entry.getValue());
		e.printStackTrace();
		G.recordFailure(G.getPeopleFailedLinksFile(ticker), ticker, entry.getKey(), entry.getValue(), reutersUrl(ticker), bloombergBoardMemsUrl(ticker));
		throw new G.TryAgainLater();
	    }
	}
    }

    /** only adding information on people that are already listed from reuters */
    private static void addBloombergPersonIDsFromPage_toMap(List<Person> people, Map<String, String> id_name_map, String url, String ticker, String onScreenDisplayDescriptor) throws InterruptedException, IOException, XPathExpressionException, BloombergTryAgainLater, BloombergFailedDontTryAgainLater {

	Document doc;
	String source;
	try {
//	    doc = HttpDownloadUtility.getWebpageDocument(url);
	    source = HttpDownloadUtility.getPageSource(url);
	    doc = HttpDownloadUtility.getWebpageDocument_fromSource(source);
	} catch (Exception e) {
	    G.recordFailure(G.getPeopleFailedLinksFile(ticker), ticker, url);
	    throw new BloombergTryAgainLater();
	}
	if (source.contains(" returned 0 public company results"))
	    throw new BloombergFailedDontTryAgainLater();

	XPath xPath = XPathFactory.newInstance().newXPath();
	NodeList links = (NodeList)xPath.compile("//span[@itemprop='member']/a").evaluate(doc, XPathConstants.NODESET);////*[@id='news-main']/div

	for (int i = 0; i < links.getLength(); i++) {

	    Node link = links.item(i);
	    String fullComplexName = link.getTextContent();

	    //see if this name is in people.  else, skip
	    if (!nameInPeopleList(fullComplexName, people))
		continue;

	    String href = (String)xPath.compile("./@href").evaluate(link, XPathConstants.STRING);

	    href = href.split("personId=")[1];
	    href = href.split("&")[0];
	    String personID = href;
	    id_name_map.put(personID, fullComplexName);
	}

    }

    public static String extractFirstAndLastName(String st) {
	String originalSt = st;

	if (st.charAt(1) == ',') {
	    st = st.charAt(0) + "." + st.substring(2, st.length());
	}
	st = st.toLowerCase().replaceAll("\\.", " ").replaceAll(",", " , ").replaceAll("  ", " ").trim() + " ";

	// Klaus-Christian Kleinfeld Ph.D., rer. pol., Dipl.-Kfm.
	String[] suffix = new String[]{
	    "jr",
	    "sr",
	    "ii",
	    "kg",
	    "ch",
	    "bcom",
	    "iii",
	    "cpa",
	    ",",
	    "comm",
	    "fca",
	    "ca",
	    "dipl",
	    "kfm",
	    "md",
	    "phd",
	    "ph",
	    "cpa",
	    "msf",
	    "jd",
	    "esq"};	//this could be expanded to any 2-letter combos where both are consonants


	for (String suf : suffix)
	    st = st.split(" " + suf + " ")[0];


	List<String> nameWords = new ArrayList();
	String[] ar1 = st.split(" ");

	for (String asdf : ar1) {
	    nameWords.add(asdf);
	}

	//remove initials
	while (true) {
	    for (int i = 0; i < nameWords.size(); i++) {
		String s = nameWords.get(i);
		if (s.length() < 2) {
		    nameWords.remove(i);
		    break;
		}
	    }
	    break;
	}

	try {
	    String first = nameWords.get(0);		    //Exception in thread "main" java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
	    String last = nameWords.get(nameWords.size() - 1);
	    return first + " " + last;
	} catch (java.lang.IndexOutOfBoundsException e) {
	    System.out.println("WEIRD NAME PROBLEM!! with " + originalSt);
	    e.printStackTrace();
	    return originalSt;
	}
    }


    private static boolean nameInPeopleList(String bloombergName, List<Person> people) {


	for (Person person : people) {

	    if (twoNamesAreTheSamePerson(bloombergName, person.reuters_name))
		return true;

	}
	return false;
    }

    private static void updatePeopleWithNewBloombergPerson(String ticker, String id, String bloomberg_fullName, List<Person> people) throws IOException, InterruptedException, XPathExpressionException, BloombergTryAgainLater {

	String first_last_name = extractFirstAndLastName(bloomberg_fullName);
	String url = "http://www.bloomberg.com/research/stocks/people/person.asp?personId=" + id + "&ticker=" + ticker;	    //why don't i just use the dang listed url?  i could be wrong when reconstructing a url
	String source;

	try {
	    source = HttpDownloadUtility.getPageSource(url);
	} //should throw exception and end function if fails
	catch (IOException e) {
	    throw new BloombergTryAgainLater();
	}

	int tries = 0;
	while ((source.contains("List of Public Companies Worldwide, Letter") || source.contains("File not found."))) {
	    if (tries++ > 10) {
		System.out.println("giving up :( on " + url);
		throw new BloombergTryAgainLater();		//note:  there shouldn't be any situation where the webpage fails and we DONT try again later.  because we're going off of scraped bloomberg data 

	    }
	    tries++;
	    System.out.println("//didn't load. wait and try again after this many tries: " + tries + " " + url);
	    Thread.sleep(1000);
	    source = HttpDownloadUtility.getPageSource(url);
	}

//	if ((source.contains("List of Public Companies Worldwide, Letter") || source.contains("File not found."))) {
//	    System.out.println("giving up :( on " + url);
//	    return;
//	}
	Document doc = HttpDownloadUtility.getWebpageDocument_fromSource(source);

//	String allInfoTableXpath = "//*[@id=\"columnLeft\"]/div[2]/table[3]/tbody/tr/td[1]";
//	XPath xPath = XPathFactory.newInstance().newXPath();
//	Node table = (Node)xPath.compile(allInfoTableXpath).evaluate(doc, XPathConstants.NODE);////*[@id='news-main']/div

	String boardMembershipInfo = getBoardMembership(doc);
	String educationInfo = getEducation(doc);
	int[] affiliationsAr = getIntAffiliationsAray(doc);
	long bloomberg_compensation = getBloombergCompensation(doc);
	byte bloomberg_age = getBloombergAge(doc);

	for (Person person : people) {
	    String complexName = person.reuters_name;
	    String man_firstLast = extractFirstAndLastName(complexName);
	    if (man_firstLast.equals(first_last_name)) {
		person.url_bloomberg = url;
		person.bloomberg_name = bloomberg_fullName;
		person.bloomberg_compensation = bloomberg_compensation;
		person.bloomberg_EducationInfo = educationInfo;
		person.bloomberg_BoardMembershipInfo = boardMembershipInfo;
		person.bloomberg_numConnections_boardMembers = affiliationsAr[0];
		person.bloomberg_numConnections_organizations = affiliationsAr[1];
		person.bloomberg_numConnections_industries = affiliationsAr[2];

		if (isnull(person.age) && !isnull(bloomberg_age))
		    person.age = bloomberg_age;

		//testing
		System.out.println(""
			+ person.outputLine() + " "
			+ person.url_bloomberg + "     " + person.url_reuters);	//.replaceAll("\\t", ",\n")
	    }
	}
    }

    public static boolean twoNamesAreTheSamePerson(String name1, String name2) {

	name1 = name1.toLowerCase();
	name2 = name2.toLowerCase();

	if (extractFirstAndLastName(name1).equals(extractFirstAndLastName(name2)))
	    return true;
	if (name1.contains(extractFirstAndLastName(name2)))
	    return true;
	if (name2.contains(extractFirstAndLastName(name1)))
	    return true;
	return false;
    }

    private static String getEducationOrBoardmembership_fromSubsequentNodes(Node n) {
	Node sib = n.getNextSibling();
	String boardMembershipInfo = "";
	while (!sib.getNodeName().equals("h2")) {
	    String content = G.cleanUpHtmlSyntax(sib.getTextContent());
	    if (!content.isEmpty() && !content.contains("There is no ")) {
		boardMembershipInfo = boardMembershipInfo + content.replaceAll(",", "").replaceAll(";", "").trim();
		boolean lastLineOfEntry = false;
		try {
		    if (sib.getAttributes().getNamedItem("style").getTextContent().contains("padding-bottom:15px;"))
			lastLineOfEntry = true;
		} catch (Exception e) {
		}
		if (lastLineOfEntry) boardMembershipInfo = boardMembershipInfo + "; ";
		if (!lastLineOfEntry) boardMembershipInfo = boardMembershipInfo + ", ";

	    }
	    sib = sib.getNextSibling();
	}
	return boardMembershipInfo;
    }

    /** This person is connected to 4 board members in 1 different organizations across 3 different industries.See Board Relationships */
    private static int[] getIntAffiliationsAray(Document doc) throws XPathExpressionException {
	//	This person is connected to 1 Board Members in 0 different organizations across 1 different industries.
	//	This person is connected to 4 board members in 1 different organizations across 3 different industries.See Board Relationships
	//	//*[@id="columnLeft"]/div[2]/table[2]/tbody/tr[1]/td[3]	 */
	XPath xPath = XPathFactory.newInstance().newXPath();

	String execprofile = (String)xPath.compile("//*[@id=\"columnLeft\"]/div[2]/table[2]/tbody/tr[1]/td[3]").evaluate(doc, XPathConstants.STRING);
	String x = execprofile.trim()
		.replace("This person is connected to ", "")
		.replace(" Board Members in ", ",")
		.replace(" board members in ", ",")
		.replace(" different organizations across ", ",")
		.replace(" different industries.", "")
		.replace("See Board Relationships", "")
		.replaceAll(" ", "");
	String[] ar = x.split(",");
	int[] intAr = new int[]{parse_int(ar[0]), parse_int(ar[1]), parse_int(ar[2])};
	return intAr;
    }

    private static long getBloombergCompensation(Document doc) throws XPathExpressionException {
	XPath xPath = XPathFactory.newInstance().newXPath();

	//	//*[@id="columnLeft"]/div[2]/table[2]/tbody/tr[2]/td[2]
	String comp = (String)xPath.compile("//*[@id=\"columnLeft\"]/div[2]/table[2]/tbody/tr[2]/td[2]/text()").evaluate(doc, XPathConstants.STRING);
	return parse_long(comp.replace("$", "").replaceAll(",", ""));
    }

    private static byte getBloombergAge(Document doc) throws XPathExpressionException {
	XPath xPath = XPathFactory.newInstance().newXPath();
	String ageXpath = "//*[@id=\"columnLeft\"]/div[2]/table[2]/tbody/tr[2]/td[1]";
	String age = (String)xPath.compile(ageXpath).evaluate(doc, XPathConstants.STRING);
	return parse_byte(age);
    }

    private static String getBoardMembership(Document doc) throws XPathExpressionException {
	XPath xPath = XPathFactory.newInstance().newXPath();
//	try {
	return getEducationOrBoardmembership_fromSubsequentNodes(
		(Node)xPath.compile("//h2[@class='sectionTitle' and contains(., 'Board Members')]").evaluate(doc, XPathConstants.NODE));
//	} catch (Exception e){
//	    System.out.println
//	    e.printStackTrace();
//	}
    }

    private static String getEducation(Document doc) throws XPathExpressionException {
	XPath xPath = XPathFactory.newInstance().newXPath();
	return getEducationOrBoardmembership_fromSubsequentNodes(
		(Node)xPath.compile("//h2[@class='sectionTitle' and contains(., 'Education')]").evaluate(doc, XPathConstants.NODE));
    }

    private static String reutersUrl(String ticker) {
	String reutersUrlBase = "http://www.reuters.com/finance/stocks/companyOfficers?symbol=";
	return reutersUrlBase + ticker;
    }

    private static String bloombergOverviewUrl(String ticker) {/*	
	 bloomberg accepts brk.a
	 http://www.bloomberg.com/research/stocks/people/people.asp?ticker=BRK/A
	 http://www.bloomberg.com/research/stocks/people/board.asp?ticker=BRK/A
	 */

	return "http://www.bloomberg.com/research/stocks/people/people.asp?ticker=" + ticker.replace("-", ".");
    }

    private static String bloombergBoardMemsUrl(String ticker) {
	return "http://www.bloomberg.com/research/stocks/people/board.asp?ticker=" + ticker.replace("-", ".");
    }

    private static boolean downloadPeople(String ticker) throws ParseException, IOException, InterruptedException, XPathExpressionException, XPatherException, EmptyTableException {

	boolean success___dont_need_to_try_again_later = true;

	if (timeToUpdatePersonFile(ticker)) {

	    try {

		List<Person> people = getReuterPeople(ticker);
		supplementPeopleWithBloombergPeople(ticker, people);
		Person.writePeople(people, ticker);

	    } catch (TickerDoesntExistInReutersException | BloombergFailedDontTryAgainLater ex) {
		System.out.println("here1");
		return true;
	    } catch (TryAgainLaterException | BloombergTryAgainLater | G.TryAgainLater ex) {
		return false;
	    }


	}

	return success___dont_need_to_try_again_later;
    }

    public static boolean timeToUpdatePersonFile(String ticker) throws ParseException, IOException {
	File file = G.getPeopleFile(ticker);
	if (!file.exists())
	    return true;
	return G.fileIsMoreThanThisManyDaysOld(file, 20);


    }

    public static class TickerDoesntExistInReutersException extends Exception {
	public TickerDoesntExistInReutersException() {
	}
    }

    public static class EmptyTableException extends Exception {
	public EmptyTableException() {
	}
    }

    /** use G.TryAgainLater instead */
    @Deprecated
    public static class TryAgainLaterException extends Exception {
	public TryAgainLaterException() {
	}
    }

    /** use G.TryAgainLater instead */
    @Deprecated
    public static class BloombergTryAgainLater extends Exception {
	public BloombergTryAgainLater() {
	}
    }

    public static class BloombergFailedDontTryAgainLater extends Exception {
	public BloombergFailedDontTryAgainLater() {
	}
    }

    public static class UnparseableName_Exception extends Exception {
	public UnparseableName_Exception() {
	}
    }


}
