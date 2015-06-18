package objects.people;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import utilities.G;
import static utilities.G.getPeopleFile;
import static utilities.G.null_byte;
import static utilities.G.null_float;
import static utilities.G.null_int;
import static utilities.G.null_long;
import static utilities.G.null_short;
import static utilities.G.parse_Str;
import static utilities.G.parse_boolean;
import static utilities.G.parse_byte;
import static utilities.G.parse_float;
import static utilities.G.parse_int;
import static utilities.G.parse_long;
import static utilities.G.parse_short;

public class Person {
    
    //TODO split this up in to reutersInfo and bloombergInfo? idk maybe doesn't make sense.

    /** starts at 1.  fairly pointless?  */
    public byte rank = null_byte;
    public String reuters_name;
    public byte age = null_byte;
    public short since = null_short;
    public String reuters_position;
    public float compensation = null_float;
    public boolean isFemale;			//check femalesExclusive file.  how?  should it be a global set? read by default?
    public boolean isMD;
    public boolean isPhD;
    public boolean isDr;

    public String bloomberg_name;
    public String bloomberg_position;
    public long bloomberg_compensation = null_long;
    public String bloomberg_EducationInfo;
    public String bloomberg_BoardMembershipInfo;
    public int bloomberg_numConnections_boardMembers = null_int;
    public int bloomberg_numConnections_organizations = null_int;
    public int bloomberg_numConnections_industries = null_int;

    //don't output these.  just for testing
    public String url_reuters;
    public String url_bloomberg;

    //rank name	age	since	position	compensation
    public Person(String line) {
	try {
	String[] ar = line.split("\t");

	//from reuters
	this.rank = parse_byte(ar[0]);
	this.isFemale = parse_boolean(ar[1]);
	this.isMD = parse_boolean(ar[2]);
	this.isPhD = parse_boolean(ar[3]);
	this.isDr = parse_boolean(ar[4]);
	this.age = parse_byte(ar[5]);
	this.since = parse_short(ar[6]);
	this.reuters_name = parse_Str(ar[7]);
	this.reuters_position = parse_Str(ar[8]);
	this.compensation = parse_float(ar[9]);

	//from bloomberg
	this.bloomberg_numConnections_boardMembers = parse_int(ar[10]);
	this.bloomberg_numConnections_organizations = parse_int(ar[11]);
	this.bloomberg_numConnections_industries = parse_int(ar[12]);
	this.bloomberg_name = ar[13];
	this.bloomberg_compensation = parse_long(ar[14]);
	this.bloomberg_EducationInfo = ar[15];
	this.bloomberg_BoardMembershipInfo = ar[16];
	}
	catch (java.lang.ArrayIndexOutOfBoundsException e){ //wtf why would we be missing data ..................... must have been exceptoin while making output line!:(
	}
    }

    public String outputLine() {
	return ""
		+ parse_Str(rank) + "\t"
		+ parse_Str(isFemale) + "\t"
		+ parse_Str(isMD) + "\t"
		+ parse_Str(isPhD) + "\t"
		+ parse_Str(isDr) + "\t"
		+ parse_Str(age) + "\t"
		+ parse_Str(since) + "\t"
		+ parse_Str(reuters_name) + "\t"
		+ parse_Str(reuters_position) + "\t"
		+ parse_Str(compensation) + "\t"
		+ parse_Str(bloomberg_numConnections_boardMembers) + "\t"
		+ parse_Str(bloomberg_numConnections_organizations) + "\t"
		+ parse_Str(bloomberg_numConnections_industries) + "\t"
		+ parse_Str(bloomberg_name) + "\t"
		+ parse_Str(bloomberg_compensation) + "\t"
		+ parse_Str(bloomberg_EducationInfo) + "\t"
		+ parse_Str(bloomberg_BoardMembershipInfo);
    }


    /** from reuters */
    public Person(int rank, String name, String age, String since, String position, String compensation, boolean isFemale, boolean isMD, boolean isPhD, boolean isDr) {
	this.rank = (byte)rank;
	this.reuters_name = parse_Str(name);
	this.age = parse_byte(age);
	this.since = parse_short(since);
	this.reuters_position = parse_Str(position);
	this.compensation = parse_float(compensation);
	this.isFemale = isFemale;
	this.isMD = isMD;
	this.isPhD = isPhD;
	this.isDr = isDr;
    }

    @Override
    public String toString() {
	return rank + " -- " + reuters_name + " -- " + age + " -- " + since + " -- " + reuters_position + " -- " + compensation;
    }

    public static List<Person> readPeopleFile(String ticker) throws FileNotFoundException, IOException, G.No_DiskData_Exception {

	File peopleFile = getPeopleFile(ticker);
	
	if (!peopleFile.exists())
	    throw new G.No_DiskData_Exception();
	
//	System.out.println("GE(*G(E* people file: " + peopleFile);//debugging
	    
	List<Person> people = new ArrayList<>();
	try (BufferedReader br = new BufferedReader(new FileReader(peopleFile))) {
	    String line;
	    while ((line = br.readLine()) != null) {
//		System.out.println("dirugidfug line: " + line);
		people.add(new Person(line));
	    }
	} catch (Exception e) {
	}
	return people;
    }

    public static void writePeople(List<Person> people, String ticker) throws IOException {
	if (!people.isEmpty())
	    Files.write(getPeopleFile(ticker).toPath(), Person.getOutputLines(people), StandardCharsets.UTF_8);
    }

    public static List<String> getOutputLines(List<Person> people) {

	    //TODO start here!  but what about other management-related stuff??? 
	//	    like board members?  number of board members?  board members' relationships?  store these as managers, but mark per person if they are employee and/or board member?
	//		add attributes like # of relationships.... cap of main company? etc.... look at bloomberg datas
	//get list of reuters managers first, then scrape bloomberg page.  supplement reuters data (in managers list) then add board members
	//update:  ignore bloomberg for now.  that site is terrible

	List<String> outputLines = new ArrayList();

	for (Person person : people) {
	    outputLines.add(person.outputLine());
	}
	return outputLines;
    }

}
