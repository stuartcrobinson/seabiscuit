package analyze.filter_tools;

public class SubconditionText {
    public String comparatorStr;
    public String boundaryStr;

    public SubconditionText(String sub) {

	String[] ar = sub.trim().split("\\s");
	comparatorStr = ar[0];
	boundaryStr = "";
	for (int i = 1; i < ar.length; i++) {	    //handles string variables with mulitple words and spaces
	    boundaryStr += ar[i] + " ";
	}
	boundaryStr = boundaryStr.trim();
    }

    public String testingOutput() {
	return "comparator: " + comparatorStr + ", boundary: " + boundaryStr;
    }
}
