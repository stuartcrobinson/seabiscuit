package analyze.comparators;

class MyComparator_float_LtE extends MyComparator {


    public MyComparator_float_LtE() {
    }

    @Override
    public boolean compare(float x, float y) {
	return x <= y;
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
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getOutputString() {
	return "<=";
    }

}
