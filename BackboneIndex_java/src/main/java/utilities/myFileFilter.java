package utilities;

import java.io.File;
import java.io.FileFilter;

public class myFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (pathname.getName().endsWith(".idx")) {
            return true;
        }
        return false;
    }
}
