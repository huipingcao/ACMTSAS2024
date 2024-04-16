package Comparison;

import java.io.File;
import java.io.FileFilter;

public class ResultLogFileFilter implements FileFilter {
    String graph_size;
    String method_name;

    public ResultLogFileFilter(String graph_size, String method_name) {
        this.graph_size = graph_size;
        this.method_name = method_name;
    }

    public ResultLogFileFilter(String method_name) {
        this.method_name = method_name;
    }

    public ResultLogFileFilter() {

    }

    @Override
    public boolean accept(File pathname) {
        if (method_name == null && graph_size == null) {
            return pathname.getName().endsWith(".log");
        } else if (graph_size != null && method_name != null) {
            return pathname.getName().endsWith(".log")
                    && pathname.getName().contains("_" + this.graph_size + "_")
                    && pathname.getName().startsWith(method_name);
        } else {
            return pathname.getName().endsWith(".log")
                    && pathname.getName().startsWith(method_name);
        }
    }
}
