package analyze.filter_tools;
import java.util.ArrayList;
import java.util.List;
import categories.C;
import supers.Var;

/** screen line is like this <br><br>     metricName  $ {@literal <}= 9  $ {@literal >}=   100.2     */
public final class ConditionDetails {

//    public enum MetricType {
//	Var,
//	CER,
//	ERA,
//	Macro
//    }

    public String metricName;

    /** if this is not null, then this condition is a catComp condition! */
    public C.CategoryType categoryType;
    public boolean isCatComp;
    public List<SubconditionText> subconditionStrs;
    /** for Var: it is the Var class.  for Era, it is the eradatarow class (i think) */
    public Class myclass;

    /** null unless a Var metric I THINK (????)*/
    public Var.Type arrayDataType;

    public boolean logical_operator_is_OR;
    boolean isSellMacro;


    public static List<SubconditionText> getConditionDetailsStrs(List<String> subConditionStrs) {

	List<SubconditionText> conditionDetails = new ArrayList(subConditionStrs.size());

	for (String sub : subConditionStrs) {
	    conditionDetails.add(new SubconditionText(sub));
	}
	return conditionDetails;
    }

    ConditionDetails(String metricName, C.CategoryType categoryType, List<SubconditionText> subconditionStrs, Class myclass, Var.Type arrayDataType, boolean logical_operator_is_OR, boolean isSellMacro) {//, MetricType mt) {
	this.metricName = metricName;
	this.categoryType = categoryType;
	this.isCatComp = this.categoryType != null;
	this.subconditionStrs = subconditionStrs;
	this.myclass = myclass;
	this.arrayDataType = arrayDataType;
	this.logical_operator_is_OR = logical_operator_is_OR;
	this.isSellMacro = isSellMacro;
    }
}
