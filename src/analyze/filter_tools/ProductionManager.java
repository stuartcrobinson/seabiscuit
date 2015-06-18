package analyze.filter_tools;
import java.util.Set;

public abstract class ProductionManager implements ProductionManagerInterface {

    static ProductionManager parseInput(String line) {
	if (line.toLowerCase().contains("doshowtodayshits"))
	    return new ProductionManager_Active();
	else
	    return new ProductionManager_Inactive();
    }
    Set<Hit> todaysHits;
    public Screen screen;

    public ProductionManager() {
    }

    @Deprecated
    /** check this for ranking mechanism */
    void setRankingDetails(Screen screen) {
	this.screen = screen;
    }


}
