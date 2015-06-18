package analyze.conditions;
import analyze.comparators.MyComparator;
import analyze.filter_tools.ConditionDetails;

public class EraCondition_AND extends EraCondition {

    public EraCondition_AND(ConditionDetails conditionDetails) throws MyComparator.BadComparatorSymbol {
	super(conditionDetails);
    }


    @Override
    public boolean isMet(float value) {
	for (Subcondition subcon : subconditions) {
	    if (!subcon.isMet(value))
		return false;
	}
	return true;
    }
}
