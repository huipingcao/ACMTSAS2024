package utilities;

import java.io.File;
import java.io.FilenameFilter;

public class TransactionFileFilter implements FilenameFilter {
    String db_name = "";
    String method_name = "";
    String data_time = "";

    @Override
    public boolean accept(File dir, String name) {
        return name.startsWith("neostore.transaction.db");
    }
}
