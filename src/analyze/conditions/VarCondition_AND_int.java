package analyze.conditions;
import analyze.comparators.MyComparator;
import analyze.filter_tools.ConditionDetails;
import analyze.filter_tools.SubconditionText;
import categories.category_earnings_response.Y;
import objects.Macro;
import objects.Macro_int;
import objects.Stock;
import utilities.G;
import static utilities.G.get;

 public class VarCondition_AND_int extends VarCondition {

    public int[] ar;

    public VarCondition_AND_int(ConditionDetails conditionDetails)  throws MyComparator.BadComparatorSymbol {
	super(conditionDetails);
	for (SubconditionText subcon : conditionDetails.subconditionStrs) {
	    Subcondition s = new Subcondition_int(subcon, conditionDetails.arrayDataType);
	    subconditions.add(s);
	}
    }

    @Override
    public void setArray(Stock stock) {
	ar = stock.vars.get(varClass).ar_int;
    }

    @Override
    public boolean isMet(int i) {
	try {
	    for (Subcondition s : subconditions)
		if (!s.isMet(get(ar, i)))
		    return false;
	    return true;
	} catch (G.My_null_exception ex) {
	    return false;
	}
    }

    @Override
    public void setDataArray(Y y) {
	ar = y.vars.get(varClass).ar_int;
    }

    @Override
    public void clearDataArray() {
	ar = null;
    }

    /** for macro.  all macros use float [] data */
    public void setArray(Macro m) {
	ar = ((Macro_int)m).dataArrays.get(super.metric);
    }
}
