package analyze.conditions;
import analyze.comparators.MyComparator;
import analyze.filter_tools.ConditionDetails;

public class EraCondition_OR extends EraCondition {

    public EraCondition_OR(ConditionDetails conditionDetails) throws MyComparator.BadComparatorSymbol {
	super(conditionDetails);
    }

    @Override
    public boolean isMet(float value) {
	for (Subcondition subcon : subconditions) {
	    if (subcon.isMet(value))
		return true;
	}
	return false;
    }
}
