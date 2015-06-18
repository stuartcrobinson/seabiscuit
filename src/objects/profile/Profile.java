package objects.profile;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import objects.Stock;

public class Profile {


    /** map key: ticker, value: profileEraDataRow.  this works because there is only 1 profile era per stock (maybe we should update this later.  do profiles change over time?  very slowly */
    @Deprecated
    public static Map<String, Profile_EraDataRow> getAllFromStocks(List<Stock> stocks) {

	return Profile_EraDataRow.getAllFromStocks(stocks);
    }

}
