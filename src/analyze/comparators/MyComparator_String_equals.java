package analyze.comparators;

class MyComparator_String_equals extends MyComparator {

    public MyComparator_String_equals() {
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
	return x.equals(y);
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
	return "==";
    }


}
