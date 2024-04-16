package Baseline;

import org.apache.commons.lang3.tuple.Pair;
import utilities.ParsedOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class GenerateTrainingSet {
    public static void main(String args[]) {
        GenerateTrainingSet g = new GenerateTrainingSet();
//        g.generating();
    }

    public void generating() throws IOException {

//        File db_folder = new File(ParsedOptions.output_landmark_index_folder);
//        try {
//            if (db_folder.exists()) {
//                System.out.println("delete the folder : " + db_folder);
//                FileUtils.deleteDirectory(db_folder);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        Baseline bbs = new Baseline(ParsedOptions.db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);
        // Modified by ying >> replace new ArrayList<>() to read queries from file
        //TODO: count results till meet # required, ignore duplicates
        Baseline bbs = new Baseline(ParsedOptions.db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);
        bbs.read_queries(ParsedOptions.queryPairFolder + "/" + ParsedOptions.srcQueryPairFile);  // added by ying
        //        bbs.init_queries();     // comment out by ying  // separate the query generation step
        ArrayList<Pair<Long, Long>> queries = new ArrayList<>();
        queries.addAll(bbs.query_list);

        System.out.println("========Stored QueryList (generating)=======");
        for (Pair<Long, Long> longLongPair : queries) {
            System.out.println(longLongPair);
        }

        // timeout threshold for bbs searching
        for (Pair<Long, Long> q : queries) {
            Baseline bbs_thread = new Baseline(ParsedOptions.db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);

            bbs_thread.current_src = q.getKey();
            bbs_thread.current_dest = q.getValue();
            bbs_thread.result_folder = ParsedOptions.resultFolder;
            bbs_thread.postfix = ParsedOptions.db_name;

            // store timeout queries to file for callback (added by ying)
            String path_timeout_query_file = ParsedOptions.queryPairFolder + "/" + ParsedOptions.db_name + "_timeout_query_pairs.txt";
            System.out.println("path_timeout_query_file: " + path_timeout_query_file);
            File fObj = new File(path_timeout_query_file);
            FileWriter f_out = new FileWriter(fObj, true);
            String newLine = System.lineSeparator();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future future = executor.submit(bbs_thread);
            try {
                future.get(ParsedOptions.timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("Thread time out in " + ParsedOptions.timeout + " ms");
                System.out.println("bbs|" + bbs_thread.current_src + ">" + bbs_thread.current_dest + "|Query time : " + 9999999);
                if (bbs_thread.neo4j != null) {
                    bbs_thread.neo4j.closeDB();
                }
                future.cancel(true);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                // store timeout queries to file for callback (added by ying)
                f_out.append(Long.toString(bbs_thread.current_src)).append(" ").append(Long.toString(bbs_thread.current_dest)).append(newLine);
                f_out.close();

                executor.shutdownNow();
            }
        }
    }
}
