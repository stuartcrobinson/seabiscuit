package supers;
import java.io.IOException;
import java.text.ParseException;
import utilities.G;

public interface XInterface_uses_Eras {
    public void fill_eras() throws IOException, ParseException, IllegalAccessException, G.No_DiskData_Exception;

    public void setXFile_Eras() throws IOException;
}
