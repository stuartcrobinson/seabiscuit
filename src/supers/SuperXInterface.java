package supers;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Set;
import utilities.G;
import categories.Seasons;
import java.util.Map;
import objects.Stock;

public interface SuperXInterface {


//    public void calculate_data_from_raw_files() throws Exception, IOException, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, G.EmptyHPException;
    public void calculate_data_from_xFiles() throws IOException, ParseException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException;

    public void calculate_data_from_categoryAveraging(Seasons seasons, Set<Stock> stocksInTheCategory) throws Var.TriedToPutBadDataTypeInVarDataArray, IllegalAccessException, InstantiationException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException;


//    void setAttributes(Object...x) throws IOException, ParseException, G.No_Data_Exception, Exception ;
    public void setXFiles() throws IOException;

    /** origination is creating the values from the first time.  from raw input files or objects.  because sometimes we will need to fill vars or eras or both.  note: to "fill" is to initialize AND calculate */
    public void fill_data__origination(SuperX x) throws IOException, G.No_DiskData_Exception, ParseException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException;

    public void fill_varClasses();		    //it's unfortunate that this has to be in here.  that it is required even if we're only using Eras data. since SuperX needs it.  but it's okay. 

    /** this method either contains the calculation code to calculate the array data, or it calls the Var.calculateData(x) method.  sometimes it is more efficient to calculate several variables at once */
    void calculate_vars_origination(SuperX x, boolean doUpdateForCurrentPrices);

    public void setAttributesFollowingXfileLoad_needed_for_currentPrices_dataUpdate(Stock stock);
}
