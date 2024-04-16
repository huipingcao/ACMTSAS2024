package utilities;

import Backbone.QueryProcess;
import Backbone.backbonePath;
import Baseline.BaselineList;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Query {
    private final static int SAVING_backbone = 1;
    private final static int SAVING_gnn = 1;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    long gnn_running_time = 0;
    long bbs_running_time = 0;

    public static void main(String args[]) {
        Query q = new Query();
        q.query();
    }

    public void query() {
        Date date = new Date();
        long timestamp = date.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
//        myLogger.configTheLogger("compareGNNBaseline_" + ParsedOptions.db_name + "_" + ParsedOptions.numQuery + df.format(timestamp));

//        String gnn_model = "answered100_epoch300_query500_128_128_32_1_EmbedTrue_Transformer_ConLossTrue";
        String result_folder = ParsedOptions.compareResultFolder + "/" + ParsedOptions.db_name + "/" + ParsedOptions.gnn_model + "/" + df.format(timestamp);

//        String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K/answered100/" + gnn_model + ".mapping";
//        String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K_TSP/answered100/" + gnn_model + ".mapping";
//        String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K/query300/" + gnn_model + ".mapping";
//        String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K_TSP_L1-6/" + gnn_model + ".mapping";
//        String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/L_CAL_NONE/" + gnn_model + ".mapping";
//        String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/L_CAL_NONE_TSP/" + gnn_model + ".mapping";

        ArrayList<Pair<Long, Long>> queries = new ArrayList<>();
        HashMap<Pair<Long, Long>, ArrayList<Long>> t_node_map = new HashMap<>();
        HashMap<Pair<Long, Long>, Double> model_time_map = new HashMap<>();


        try (BufferedReader br = new BufferedReader(new FileReader(ParsedOptions.mapped_node_file))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] s_info = line.split("\\{|\\}");
                long src = Long.parseLong(s_info[0].split(" ")[0]);
                long dest = Long.parseLong(s_info[0].split(" ")[1]);
                ArrayList<Long> t_nodes = new ArrayList<>();

                for (String t_s : s_info[1].split(",")) {
                    t_nodes.add(Long.parseLong(t_s.trim()));
                }

//                System.out.println(s_info[2].trim().split(" ")[0]);
                double model_time = Double.parseDouble(s_info[2].trim().split(" ")[0]);
                Pair<Long, Long> q = new MutablePair<>(src, dest);
                queries.add(q);
//                LOGGER.info("Initialized the query list ..................................................................");
                t_node_map.put(q, t_nodes);
                model_time_map.put(q, model_time);
//                if(src==208&&dest==3248){
//                    System.out.println(line);
//                }
            }
            // line is not visible here.
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.exit(0);
        int index = 1;

        float total_avg_backbone_path_len = 0;  // added by ying

        for (Pair<Long, Long> q : queries) {
            long src = q.getKey();
            long dest = q.getValue();
            System.out.println(t_node_map.get(q).size()+" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            String backbone_paths_output_file = result_folder + "/backbone_" + src + "_" + dest + "_" + ParsedOptions.db_name + ".log";
            String gnn_paths_output_file = result_folder + "/gnn_" + src + "_" + dest + "_" + ParsedOptions.db_name + ".log";
            System.out.println("gnn_paths_output_file: " + gnn_paths_output_file);


            // =================== test =======================
//            QueryProcess bq = new QueryProcess(ParsedOptions.db_name, ParsedOptions.number_landmark);
//            ArrayList<backbonePath> result = bq.query(src, dest, index);
//
//            int n_result = 0;
//            float avg_path_len_per_result = 0;
//            int len_per_result = 0;
//            for (backbonePath p : result) {
//                n_result += 1;
//                len_per_result += p.highwayList.size();
//            }
//            avg_path_len_per_result += len_per_result / n_result;
//            total_avg_backbone_path_len += avg_path_len_per_result;
            // ================================================

            if (SAVING_backbone==1) {
                if (!new File(backbone_paths_output_file).exists()) {
                    long start_ms = System.currentTimeMillis();
                    QueryProcess bq = new QueryProcess(ParsedOptions.db_name, ParsedOptions.number_landmark);
                    ArrayList<backbonePath> result = bq.query(src, dest, index);

                    bbs_running_time += (System.currentTimeMillis() - start_ms);

                    bq.savePathsInformation(result, backbone_paths_output_file);

                } else {
                    System.out.println(index + " : " + backbone_paths_output_file + " existing !!! skipped !!!!");
                }
            }
            if (SAVING_gnn==1) {
                if (!new File(gnn_paths_output_file).exists()) {
                    long start_ms = System.currentTimeMillis();
                    BaselineList bbs_thread = new BaselineList(ParsedOptions.db_name, ParsedOptions.numQuery, new ArrayList<>(), true, true, ParsedOptions.number_landmark);
                    bbs_thread.current_src = src;
                    bbs_thread.current_dest = dest;
                    bbs_thread.result_folder = result_folder;
                    bbs_thread.postfix = ParsedOptions.db_name;
                    bbs_thread.methed_name = "gnn";
                    bbs_thread.t_nodes = t_node_map.get(q);
                    bbs_thread.index = index;

                    gnn_running_time += (System.currentTimeMillis() - start_ms);

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
                    } catch (ExecutionException |
                             InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        executor.shutdownNow();
                    }
                } else {
                    System.out.println(index + " : " + gnn_paths_output_file + " existing !!! skipped !!!!");
                }
            }

            index++;
        }

//        System.out.println("total_avg_backbone_path_len: " + (total_avg_backbone_path_len));

        System.out.println("Total: " + index + " " + ((float)gnn_running_time));
        System.out.println("Total: " + index + " " + ((float)bbs_running_time));
    }


}

