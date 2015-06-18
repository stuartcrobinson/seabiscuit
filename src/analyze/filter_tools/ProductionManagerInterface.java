package analyze.filter_tools;
import objects.Stock;

interface ProductionManagerInterface {

    public void manage(float rank1Value, float rank2Value, String name, int dateInt, float vol, float close, Stock stock);

    public void rankAndShrink();
}
