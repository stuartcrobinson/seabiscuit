package analyze.conditions;
import analyze.comparators.MyComparator;
import analyze.filter_tools.ConditionDetails;
import analyze.filter_tools.SubconditionText;
import categories.category_earnings_response.Y;
import objects.Macro;
import objects.Stock;

class VarCondition_AND_byte extends VarCondition {

    private byte[] ar;

    public VarCondition_AND_byte(ConditionDetails conditionDetails)  throws MyComparator.BadComparatorSymbol {
	super(conditionDetails);
	for (SubconditionText subcon : conditionDetails.subconditionStrs) {
	    Subcondition s = new Subcondition_byte(subcon, conditionDetails.arrayDataType);
	    subconditions.add(s);
	}
    }

    @Override
    public void setArray(Stock stock) {
	ar = stock.vars.get(varClass).ar_byte;
    }

    @Override
    public boolean isMet(int i) {
	for (Subcondition s : subconditions)
	    if (!s.isMet(ar[i]))
		return false;
	return true;
    }

    @Override
    public void setDataArray(Y y) {
	ar = y.vars.get(varClass).ar_byte;
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
