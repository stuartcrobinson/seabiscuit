package analyze.comparators;

public class MyComparator_boolean_E extends MyComparator {

    public MyComparator_boolean_E() {
    }

    @Override
    public boolean compare(float x, float y) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean compare(int x, int y) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean compare(String x, String y) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean compare(byte x, byte y) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean compare(boolean x, boolean y) {
	return x == y;
    }

    @Override
    public String getOutputString() {
	return "==";
    }

}
