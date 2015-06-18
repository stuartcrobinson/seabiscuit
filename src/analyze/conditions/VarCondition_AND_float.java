package analyze.conditions;
import analyze.comparators.MyComparator;
import analyze.filter_tools.ConditionDetails;
import analyze.filter_tools.SubconditionText;
import categories.category_earnings_response.Y;
import objects.Macro;
import objects.Macro_float;
import objects.Stock;
import utilities.G;
import static utilities.G.get;

public class VarCondition_AND_float extends VarCondition {

    public float[] ar;

//    public String name_used_only_for_macroMetric;

    public VarCondition_AND_float(ConditionDetails conditionDetails) throws MyComparator.BadComparatorSymbol {
	super(conditionDetails);
	for (SubconditionText subcon : conditionDetails.subconditionStrs) {
	    Subcondition s = new Subcondition_float(subcon, conditionDetails.arrayDataType);
	    subconditions.add(s);
	}
    }

    @Override
    public void setArray(Stock stock) {
	if (isCatComp)
	    ar = stock.vars.get(varClass).catComparisonArrays.get(categoryType);
	else
	    ar = stock.vars.get(varClass).ar_float;
    }

    @Override
    public void setDataArray(Y y) {
	ar = y.vars.get(varClass).ar_float;
    }

    /** for macro.  all macros use float [] data */
    public void setArray(Macro m) {
	ar = ((Macro_float)m).dataArrays.get(super.metric);
    }

    @Override
    public void clearDataArray() {
	ar = null;
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

}
