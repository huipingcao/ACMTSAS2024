package Comparison.Compare;

import Comparison.BaselineMethods.Baseline;
import Query.QueryProcess;
import Query.backbonePath;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.Pair;
import utilities.ParsedOptions;
import utilities.TimeoutTask;
import utilities.myLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.logging.Logger;


public class compareBBSBackbone {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String args[]) throws ParseException {
        compareBBSBackbone c = new compareBBSBackbone();
        c.compare();
    }

    public void compare() {
        ArrayList<Pair<Long, Long>> queries = new ArrayList<>();
        Date date = new Date();
        long timestamp = date.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        myLogger.configTheLogger("compareBBSBackbone_" + ParsedOptions.db_name + "_" + ParsedOptions.numQuery, timestamp);

        String result_folder = ParsedOptions.resultFolder + "/" + ParsedOptions.db_name + "/" + df.format(timestamp);
        if (!new File(result_folder).exists()) {
            LOGGER.info("The results storage folder doesn't existed, created. " + result_folder);
            new File(result_folder).mkdir();
        }

        String bbs_db_name = ParsedOptions.db_name + "_Level0";
        Baseline bbs = new Baseline(bbs_db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);
        bbs.init_queries();
        queries.addAll(bbs.query_list);
        LOGGER.info("Initialized the query list ..................................................................");
        for (Pair<Long, Long> q : queries) {

            Baseline bbs_thread = new Baseline(bbs_db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);

            bbs_thread.current_src = q.getKey();
            bbs_thread.current_dest = q.getValue();
            bbs_thread.result_folder = result_folder;
            bbs_thread.timestamp = timestamp;
            bbs_thread.postfix = String.valueOf(timestamp);


//            Timer timer = new Timer();
            Thread t = new Thread(bbs_thread);
//            timer.schedule(new TimeoutTask(t, timer, bbs_thread), ParsedOptions.timeout);
//
//            t.start();
//            try {
//                t.join();
//            } catch (InterruptedException e) {
//                System.out.println("1111111111111111111111111111111");
//                e.printStackTrace();
//            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future future = executor.submit(bbs_thread);
            try {
                future.get(ParsedOptions.timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                LOGGER.info("Thread time out in " + ParsedOptions.timeout + " ms");
                LOGGER.info("bbs|" + bbs_thread.current_src + ">" + bbs_thread.current_dest + "|Query time : " + 9999999);
                if (bbs_thread.neo4j != null) {
                    bbs_thread.neo4j.closeDB();
                }
                future.cancel(true);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                executor.shutdownNow();
            }
        }

        LOGGER.info("Conducted the query by using the backbone index .......");
        LOGGER.info("-----------------------------------------------------------------------------------------");

        QueryProcess bq = new QueryProcess(ParsedOptions.db_name, ParsedOptions.number_landmark);
        for (Pair<Long, Long> q : bbs.query_list) {
            ArrayList<backbonePath> result = bq.query(q.getKey(), q.getValue());
            String paths_output_file = result_folder + "/backbone_" + q.getKey() + "_" + q.getValue() + "_" + timestamp + ".log";
            bq.savePathsInformation(result, paths_output_file);
        }
    }
}
