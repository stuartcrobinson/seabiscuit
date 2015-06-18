package analyze.conditions;
import analyze.comparators.MyComparator;
import java.util.ArrayList;
import java.util.List;
import analyze.filter_tools.ConditionDetails;
import analyze.filter_tools.Screen;
import categories.C;

/** varCondition knows the data type */
public abstract class VarCondition implements VarConditionInterface {

    public Class varClass;
    public String metric;

    public C.CategoryType categoryType;
    public Boolean isCatComp;
    public List<Subcondition> subconditions;

    public boolean logical_operator_is_OR;

    public VarCondition(ConditionDetails conditionDetails) {

	this.metric = conditionDetails.metricName;
	this.categoryType = conditionDetails.categoryType;
	this.isCatComp = this.categoryType != null;

	this.varClass = conditionDetails.myclass;
	this.subconditions = new ArrayList();

	this.logical_operator_is_OR = conditionDetails.logical_operator_is_OR;
    }

    public String getOutputString() {
	String optionalclassprefix = categoryType == null ? "" : (categoryType.nickname + " ");

	String s = optionalclassprefix + metric + Screen.condSubSubDelimiter;

//	for (Subcondition sc : subconditions) {
//	    s += sc.getOutputString() + Screen.condSubDelimiter;
//	}

	for (int i = 0; i < subconditions.size(); i++) {
	    Subcondition sc = subconditions.get(i);
	    String logicalOperator = logical_operator_is_OR ? " OR " : " AND ";
	    if (i > 0)
		s += logicalOperator + sc.getOutputString();
	    else
		s += sc.getOutputString();

	}
	return s;
//	return s.substring(0, s.length() - 2);
    }


    public static VarCondition create_new_varCondition(ConditionDetails conditionDetails) throws MyComparator.BadComparatorSymbol {

	if (conditionDetails.logical_operator_is_OR)
	    switch (conditionDetails.arrayDataType) {
		case FLOAT:
		    return new VarCondition_OR_float(conditionDetails);
		case INTEGER:
		    return new VarCondition_OR_int(conditionDetails);
		case STRING:
		    return new VarCondition_OR_String(conditionDetails);
		case BYTE:
		    return new VarCondition_OR_byte(conditionDetails);
		case BOOLEAN:
		    return new VarCondition_OR_boolean(conditionDetails);
		default:
		    throw new AssertionError(conditionDetails.arrayDataType.name());
	    }
	else
	    switch (conditionDetails.arrayDataType) {
		case FLOAT:
		    return new VarCondition_AND_float(conditionDetails);
		case INTEGER:
		    return new VarCondition_AND_int(conditionDetails);
		case STRING:
		    return new VarCondition_AND_String(conditionDetails);
		case BYTE:
		    return new VarCondition_AND_byte(conditionDetails);
		case BOOLEAN:
		    return new VarCondition_AND_boolean(conditionDetails);
		default:
		    throw new AssertionError(conditionDetails.arrayDataType.name());
	    }
    }


}
