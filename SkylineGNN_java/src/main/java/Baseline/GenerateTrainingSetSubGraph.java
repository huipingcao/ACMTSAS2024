package Baseline;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import utilities.ParsedOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class GenerateTrainingSetSubGraph {
    public static void main(String args[]) {
        GenerateTrainingSetSubGraph g = new GenerateTrainingSetSubGraph();
        g.generating();
    }

    public void generating() {
        int sub_size = Integer.parseInt(ParsedOptions.sub_k);
        System.out.println(ParsedOptions.GraphInfoPath);
        for (int i = 0; i < ParsedOptions.number_sub_graphs; i++) {
            createSubGraphs c = new createSubGraphs();
            String sub_db_name = c.createChangeGraphDB(ParsedOptions.db_name, ParsedOptions.GraphInfoPath, sub_size, i);

            File db_folder = new File(ParsedOptions.output_landmark_index_folder);
            try {
                if (db_folder.exists()) {
                    System.out.println("delete the folder : " + db_folder);
                    FileUtils.deleteDirectory(db_folder);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            BaselineList bbs = new BaselineList(sub_db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);
            bbs.init_queries();
            ArrayList<Pair<Long, Long>> queries = new ArrayList<>();
            queries.addAll(bbs.query_list);
            for (Pair<Long, Long> q: queries) {
                Baseline bbs_thread = new Baseline(sub_db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);

                bbs_thread.current_src = q.getKey();
                bbs_thread.current_dest = q.getValue();
                bbs_thread.result_folder = ParsedOptions.resultFolder;
                bbs_thread.postfix = sub_db_name+"_"+i;


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
                    executor.shutdownNow();
                }
            }
        }
    }

    public void savePathsInformation(ArrayList<path> results, String path_output) {
        try {
            File result_file = new File(path_output);
            if (!result_file.getParentFile().exists()) {
                result_file.getParentFile().mkdirs();
            }

            FileWriter myWriter = new FileWriter(path_output, true);
            for (path p : results) {
                myWriter.write(p + " \n");
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file. " + path_output);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
