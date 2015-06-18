package analyze.conditions;
import categories.category_earnings_response.Y;
import objects.Macro;
import objects.Stock;

interface VarConditionInterface {
    void setArray(Stock stock);
    void setArray(Macro macro);
    public void setDataArray(Y y);
    public void clearDataArray();

    public boolean isMet(int i);
}
