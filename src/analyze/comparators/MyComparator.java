package analyze.comparators;
import supers.Var;

public abstract class MyComparator implements MyComparatorInterface{
    public static MyComparator getCorrectMyComparator(String str, Var.Type arrayDataType) throws BadComparatorSymbol {
	switch (arrayDataType) {
	    case FLOAT:
		if (str.equals("<="))
		    return new MyComparator_float_LtE();
		if (str.equals(">="))
		    return new MyComparator_float_GtE();
		if (str.equals("=="))
		    return new MyComparator_float_E();
	    case INTEGER:
		if (str.equals("<="))
		    return new MyComparator_int_LtE();
		if (str.equals(">="))
		    return new MyComparator_int_GtE();
		if (str.equals("=="))
		    return new MyComparator_int_E();
	    case STRING:
		if (str.equals("contains"))
		    return new MyComparator_String_contains();
		if (str.equals("!contains"))
		    return new MyComparator_String_doesntContain();
		if (str.equals("=="))
		    return new MyComparator_String_equals();
	    case BYTE:
		if (str.equals("<="))
		    return new MyComparator_byte_LtE();
		if (str.equals(">="))
		    return new MyComparator_byte_GtE();
		if (str.equals("=="))
		    return new MyComparator_byte_E();
	    case BOOLEAN:
		if (str.equals("=="))
		    return new MyComparator_boolean_E();
	    default:
		throw new BadComparatorSymbol(str);
	}

    }

    public static class BadComparatorSymbol extends Exception {
	public BadComparatorSymbol(String str) {
	    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
    }

}
