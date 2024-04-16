package Comparison.BaselineMethods;

import org.apache.commons.lang3.tuple.Pair;
import utilities.ParsedOptions;
import utilities.myLogger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class GenerateTrainningPaths {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String args[]) {
        ParsedOptions.numQuery = 10000;

        GenerateTrainningPaths g = new GenerateTrainningPaths();
        g.generate();
    }

    private void generate() {
        String bbs_db_name = ParsedOptions.db_name + "_Level0";
        Date date = new Date();
        long timestamp = date.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String result_folder = ParsedOptions.resultFolder + "/" + ParsedOptions.db_name + "/training_GNN";
        myLogger.configTheLogger("compareBBSBackbone_" + ParsedOptions.db_name + "_" + ParsedOptions.numQuery+"_training");


        Baseline bbs = new Baseline(bbs_db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);
        bbs.init_queries();

        for (Pair<Long, Long> q : bbs.query_list) {
            ArrayList<path> results = bbs.queryOnline(q.getKey(), q.getValue());
            LOGGER.info("The size of the finnal result is " + results.size());
            String paths_output_file = result_folder + "/bbs_" + q.getKey() + "_" + q.getValue() + "_" + timestamp + ".log";
            bbs.savePathsInformation(results, paths_output_file);
            System.out.println("-----------------------------------------------------------------------------------------");
        }

    }
}
