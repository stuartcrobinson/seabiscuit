package analyze.conditions;
import analyze.comparators.MyComparator;
import analyze.filter_tools.ConditionDetails;
import analyze.filter_tools.SubconditionText;
import categories.category_earnings_response.Y;
import objects.Macro;
import objects.Stock;
import utilities.G;
import static utilities.G.get;

class VarCondition_OR_String extends VarCondition {

    private String[] ar;
    VarCondition_OR_String(ConditionDetails conditionDetails)  throws MyComparator.BadComparatorSymbol {
	super(conditionDetails);
	for (SubconditionText subcon : conditionDetails.subconditionStrs) {
	    Subcondition s = new Subcondition_String(subcon, conditionDetails.arrayDataType);
	    subconditions.add(s);
	}
    }

    @Override
    public void setArray(Stock stock) {
	ar = stock.vars.get(varClass).ar_String;
    }


    @Override
    public boolean isMet(int i) {
	try {
	    for (Subcondition s : subconditions)
		if (s.isMet(get(ar, i)))
		    return true;
	    return false;
	} catch (G.My_null_exception ex) {
	    return false;
	}
    }

    @Override
    public void setDataArray(Y y) {
	ar = y.vars.get(varClass).ar_String;
    }

    @Override
    public void clearDataArray() {
	ar = null;
    }

    @Override
    public void setArray(Macro macro) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
