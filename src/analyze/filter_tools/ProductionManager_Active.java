package analyze.filter_tools;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import objects.Stock;
import utilities.G;

public class ProductionManager_Active extends ProductionManager {

    public ProductionManager_Active() {
	todaysHits = new LinkedHashSet();
    }

    @Override
    public void manage(float rank1Value, float rank2Value, String name, int dateInt, float vol, float close, Stock stock) {
	if (G.currentDate == dateInt) {
	    Hit hit = new Hit(G.null_float, rank1Value, rank2Value, dateInt, vol, close, stock);
	    super.todaysHits.add(hit);
	}
    }


    @Override
    public void rankAndShrink() {

	Set<Hit> set1 = new TreeSet(screen.rank1Comparator);
	set1.addAll(todaysHits);

	int count = 0;
	for (Iterator<Hit> it = set1.iterator(); it.hasNext();) {
	    Hit hit = it.next();
	    count++;

	    if (count > screen.rank1KeepAmount)
		it.remove();
	}

	Set<Hit> set2 = new TreeSet(screen.rank2Comparator);
	set2.addAll(set1);

	count = 0;
	for (Iterator<Hit> it = set2.iterator(); it.hasNext();) {
	    Hit hit = it.next();
	    count++;

	    if (count > screen.rank2KeepAmount)
		it.remove();
	}

	todaysHits = set2;
    }

}
