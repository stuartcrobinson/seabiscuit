package analyze.conditions;
import analyze.filter_tools.SubconditionText;
import analyze.comparators.MyComparator;
import analyze.filter_tools.Screen;
import supers.Var;
import utilities.G;

public class Subcondition_float extends Subcondition {

    float boundary;
    MyComparator c;

    public Subcondition_float(SubconditionText subcon, Var.Type arrayDataType) throws MyComparator.BadComparatorSymbol {
	this.boundary = Float.parseFloat(subcon.boundaryStr.trim());
	this.c = MyComparator.getCorrectMyComparator(subcon.comparatorStr, arrayDataType);
    }

    @Override
    public boolean isMet(float x) {
	return c.compare(x, boundary);
    }

    @Override	//i want to keep these exceptions in here!
    public boolean isMet(byte x) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isMet(int x) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isMet(String x) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isMet(boolean x) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getOutputString() {
	return c.getOutputString() + Screen.condSubSubDelimiter + G.parse_Str(boundary);
    }
}
