package analyze.conditions;
import analyze.comparators.MyComparator;
import java.util.ArrayList;
import java.util.List;
import analyze.filter_tools.ConditionDetails;
import analyze.filter_tools.Screen;
import analyze.filter_tools.SubconditionText;
import objects.Stock;
import supers.Era;
import supers.Era.EraDataRow;

public abstract class EraCondition implements EraConditionInterface {

//    public static EraCondition create_new_varCondition(ConditionDetails conditionDetails) {
//	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    public static EraCondition create_new_varCondition(ConditionDetails conditionDetails)  throws MyComparator.BadComparatorSymbol {
	if (conditionDetails.logical_operator_is_OR)
	    return new EraCondition_OR(conditionDetails);
	else
	    return new EraCondition_AND(conditionDetails);
    }


    Class eraDataRowClass;
    String metric;

    List<Subcondition> subconditions;
    boolean logical_operator_is_OR;


    public String getOutputString() {
	String s = metric + Screen.condSubDelimiter;

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
    }

    public EraCondition(ConditionDetails conditionDetails) throws MyComparator.BadComparatorSymbol {

	this.metric = conditionDetails.metricName;
	this.eraDataRowClass = conditionDetails.myclass;

	subconditions = new ArrayList();

	for (SubconditionText subcon : conditionDetails.subconditionStrs) {
	    Subcondition s = new Subcondition_float(subcon, conditionDetails.arrayDataType);
	    subconditions.add(s);
	}
	this.logical_operator_is_OR = conditionDetails.logical_operator_is_OR;
    }

    public List<Era> getPassingEras(Stock stock) {
	List<Era> eras = stock.eras.get(eraDataRowClass);

//	System.out.println("fawefawefasdffef " + eraDataRowClass);

	List<Era> passingEras = new ArrayList(eras.size());	    //eras is null during screen using isFemaleCEO  -- why?????

	for (Era era : eras) {

	    EraDataRow edr = era.eraDataRow;

	    float value = edr.valuesMap.get(metric);

	    boolean allSubconditionsWereMet = true;
	    for (Subcondition sub : subconditions) {
		if (!sub.isMet(value)) {
		    allSubconditionsWereMet = false;
		}
	    }
	    if (allSubconditionsWereMet)
		passingEras.add(era);

	}
	return passingEras;

	//so, there is nothing that connects a date with an era value.  that is not used/needed for filtering.  but we want to do that for making megatable.  
	//so we'll make a map like <eradatarowmetricclass, Map<dateInt, eradatarow>>
    }


}
