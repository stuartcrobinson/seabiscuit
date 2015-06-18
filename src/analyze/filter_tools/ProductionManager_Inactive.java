package analyze.filter_tools;
import objects.Stock;

public class ProductionManager_Inactive extends ProductionManager {

    @Override
    public void manage(float rank1Value, float rank2Value, String name, int dateInt, float vol, float close, Stock stock) {
	//do nothing
    }

    @Override
    public void rankAndShrink() {
    }
}
