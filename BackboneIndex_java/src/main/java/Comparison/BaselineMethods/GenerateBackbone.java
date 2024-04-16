package Comparison.BaselineMethods;

import Query.QueryProcess;
import Query.backbonePath;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import utilities.ParsedOptions;
import utilities.myLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class GenerateBackbone {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public void generating() {
        Date date = new Date();
        long timestamp = date.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");

        myLogger.configTheLogger("compareBackbone_" + ParsedOptions.db_name + "_Generating_", timestamp);
        ArrayList<Pair<Long, Long>> queries = new ArrayList<>();

        System.out.println(ParsedOptions.resultFolder);

        for (File f : new File(ParsedOptions.resultFolder).listFiles()) {
            if (f.getName().endsWith("log")) {
                long start_id = Long.parseLong(f.getName().split("_")[1]);
                long dest_id = Long.parseLong(f.getName().split("_")[2]);
                System.out.println(f.getName() + " " + start_id + " " + dest_id);
                MutablePair<Long, Long> q = new MutablePair<Long, Long>(start_id, dest_id);
                queries.add(q);
            }
        }

        LOGGER.info("Conducted the query by using the backbone index .......");
        LOGGER.info("-----------------------------------------------------------------------------------------");

        String result_folder = "/home/gqxwolf/mydata/projectData/BackBone/results/" + ParsedOptions.db_name + "/" + df.format(timestamp);
//        String result_folder = "/home/gqxwolf/mydata/projectData/BackBone/results/" + ParsedOptions.db_name + "/20211116_164104";

        QueryProcess bq = new QueryProcess(ParsedOptions.db_name, ParsedOptions.number_landmark);
        int index = 1;
        for (Pair<Long, Long> q : queries) {
//            File t_f = new File(result_folder + "/backbone_" + q.getKey() + "_" + q.getValue() + "_1637106064255.log");
//            if(t_f.exists()){
//                LOGGER.info(t_f.getName()+" existing, skip  !!!!!!!!!!!!!!!!!!!!!");
//                continue;
//            }
            LOGGER.info(index++ + ":" + q.getKey() + " ===> " + q.getValue());
            ArrayList<backbonePath> result = bq.query(q.getKey(), q.getValue());
            String paths_output_file = result_folder + "/backbone_" + q.getKey() + "_" + q.getValue() + "_" + timestamp + ".log";
//            String paths_output_file = result_folder + "/backbone_" + q.getKey() + "_" + q.getValue() + "_1637106064255" + ".log";
            bq.savePathsInformation(result, paths_output_file);
        }
    }
}
