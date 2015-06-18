package analyze.conditions;
import analyze.filter_tools.SubconditionText;
import analyze.comparators.MyComparator;
import analyze.filter_tools.Screen;
import supers.Var;

public class Subcondition_boolean extends Subcondition {

    boolean boundary;
    MyComparator c;

    public Subcondition_boolean(SubconditionText subcon, Var.Type arrayDataType) throws MyComparator.BadComparatorSymbol {
	this.boundary = Boolean.parseBoolean(subcon.boundaryStr.trim());
	this.c = MyComparator.getCorrectMyComparator(subcon.comparatorStr, arrayDataType);
    }

    @Override
    public boolean isMet(float x) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
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
	return c.compare(x, boundary);
    }

    @Override
    public String getOutputString() {
	return c.getOutputString() + Screen.condSubSubDelimiter + boundary;
    }

}
