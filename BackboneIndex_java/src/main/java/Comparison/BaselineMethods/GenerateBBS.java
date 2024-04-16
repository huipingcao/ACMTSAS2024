package Comparison.BaselineMethods;

import org.apache.commons.lang3.tuple.Pair;
import utilities.ParsedOptions;
import utilities.myLogger;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class GenerateBBS {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public void generating() {
        myLogger.configTheLogger("compareBBS_" + ParsedOptions.db_name + "_Generating_"+ParsedOptions.numQuery);

        Baseline bbs = new Baseline(ParsedOptions.db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);
        bbs.init_queries();
        ArrayList<Pair<Long, Long>> queries = new ArrayList<>();
        queries.addAll(bbs.query_list);
        for (Pair<Long, Long> q : queries) {
            Baseline bbs_thread = new Baseline(ParsedOptions.db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);

            bbs_thread.current_src = q.getKey();
            bbs_thread.current_dest = q.getValue();
            bbs_thread.result_folder = ParsedOptions.resultFolder;
            bbs_thread.postfix = ParsedOptions.db_name;


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
    }
}
