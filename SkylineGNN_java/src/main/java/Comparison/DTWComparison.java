package Comparison;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import utilities.ParsedOptions;
import utilities.myLogger;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;


public class DTWComparison {

    private final static int saving_bbs_logs_path = 0;
    private final static int logging_status = 1;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    String bbs_result_prefix = "bbs_";
    String backbone_result_prefix = "backbone_";
    String gnn_result_prefix = "gnn_";
    String db_name = ParsedOptions.db_name;

//    String getBbs_result_postfix = ParsedOptions.db_name;
    String getBbs_result_postfix = "C9_NY_NONE_5K";
//    String getBbs_result_postfix = "L_CAL_NONE";

//    This is the result file from bbs method. It has results for 10000 queries, results of 8000 are used for training. Saved in graph data folder C9_NY_NONE_5K
    String bbs_result_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_results_bbs/C9_NY_NONE_5K_test_1000";
//    String bbs_result_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_results_bbs/L_CAL_NONE_test_1000";


//    String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K/answered100/" + gnn_model + ".mapping";
//    String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K_TSP/answered100/" + ParsedOptions.gnn_model + ".mapping";
//    String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K/query300/" + gnn_model + ".mapping";
//    String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/C9_NY_NONE_5K_TSP_L1-6/" + gnn_model + ".mapping";
//    String mapped_node_file = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_mapped/L_CAL_NONE_TSP/" + gnn_model + ".mapping";

    String compared_result_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/new_results_gnn_backbone/C9_NY_NONE_5K_TSP/answered100/answered100_epoch300_query500_128_128_32_1_EmbedTrue_Transformer_ConLossTrue/20230519_175742";


//    ===================================================================================
//    String bbs_result_prefix = "bbs_";
//    String backbone_result_prefix = "backbone_";
//    String gnn_result_prefix = "gnn_";
//    String getBbs_result_postfix = "C9_NY_15K";
//    String db_name = "C9_NY_15K";
//    String bbs_result_path = "/home/gqxwolf/mydata/projectData/skylineGNN/results_back/C9_NY_15K/results";
//    String compared_result_path = "/home/gqxwolf/mydata/projectData/skylineGNN/comparisonResults/C9_NY_15K/256_256_32_1_EmbedFalse_GCN_ConLossFalse";
//    String mapped_node_file = "/home/gqxwolf/mydata/projectData/skylineGNN/Mapped/C9_NY_15K/256_256_32_1_EmbedFalse_GCN_ConLossFalse.mapping";
//    ================================================================================


    DecimalFormat df = new DecimalFormat("#.###");

    private double residents = 0.0000001;

    boolean sorted_result = true;

    public static void main(String args[]) throws ParseException, IOException {
        DTWComparison dtw = new DTWComparison();
        dtw.comparision();
    }

    public void comparision() throws IOException {
        Date date = new Date();
        long timestamp = date.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        if (logging_status!=0) {myLogger.configTheLogger(ParsedOptions.db_name + "_" + ParsedOptions.gnn_model + "_" + df.format(timestamp));}

        if (logging_status!=0) {
            LOGGER.info("db_name: " + ParsedOptions.db_name + '\n'
                    + "neo4jdbPath: " + ParsedOptions.neo4jdbPath + '\n'
                    + "GraphInfoPath: " + ParsedOptions.GraphInfoPath + '\n'
                    + "indexFolder: " + ParsedOptions.indexFolder + '\n'
                    + "bbs_result_path: " + bbs_result_path + '\n'
                    + "compared_result_path: " + compared_result_path + '\n'
                    + "mapped_node_file" + ParsedOptions.mapped_node_file + '\n'
                    + "output_landmark_index_folder: " + ParsedOptions.output_landmark_index_folder + '\n'
                    + "number_landmark: " + ParsedOptions.number_landmark + '\n'
                    + "logFolder: " + ParsedOptions.logFolder + '\n'
                    + "createNewLandmarks: " + ParsedOptions.createNewLandmarks + '\n'
                    + "number_sub_graphs: " + ParsedOptions.number_sub_graphs + '\n'
                    + "timeout: " + ParsedOptions.timeout + '\n'
                    + "cost_dimension: " + ParsedOptions.cost_dimension + '\n'
                    + "dtw_normal: " + ParsedOptions.dtw_normal + '\n'
            );
        }


        ArrayList<Pair<Long, Long>> queries = new ArrayList<>();
        HashMap<Pair<Long, Long>, ArrayList<Long>> t_node_map = new HashMap<>();
        HashMap<Pair<Long, Long>, Double> model_time_map = new HashMap<>();

        double f1_score = 0.0, f1_sub_score = 0.0, f1_micro = 0.0, f1_macro = 0.0;
        double pre = 0.0, sub_pre = 0.0, recall = 0.0, recall_sub = 0.0, roc = 0.0, roc_s = 0.0;
        int predict_length = 0, sub_lent = 0;
        double time = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(ParsedOptions.mapped_node_file))) {

            for (String line; (line = br.readLine()) != null; ) {
                String[] s_info = line.split("\\{|\\}");
                long src = Long.parseLong(s_info[0].split(" ")[0]);
                long dest = Long.parseLong(s_info[0].split(" ")[1]);
                ArrayList<Long> t_nodes = new ArrayList<>();
                ArrayList<Long> sub_nodes = new ArrayList<>();

                for (String t_s : s_info[1].split(",")) {
                    t_nodes.add(Long.parseLong(t_s.trim()));
                }

//                 info in .mapping file (comment by ying)
//                .format(int(src), int(dest), sub_node_set, sub_graph_finding_time+model_exectue_time, target_node_list,
//                        f1_score_1, f1_score_1_s, f1_score_2, f1_score_3,pre_score,pre_score_s,rec_score,rec_score_s)

                double model_time = Double.parseDouble(s_info[2].trim().split(" ")[0]);
//                System.out.println(model_time);
                Pair<Long, Long> q = new MutablePair<>(src, dest);
                queries.add(q);
                t_node_map.put(q, t_nodes);
                model_time_map.put(q, model_time);

//                System.out.println("======comparision()======");
//                System.out.println(s_info[2]);
//                System.out.println("s_info:");
//                System.out.println("-  " + s_info[2].trim().split("\\[|\\]")[1] + "   " + (s_info[2].trim().split("\\[|\\]")[1].equals("")));
//                System.out.println("-  " + s_info[2].trim().split("\\[|\\]")[2]);

//                7896 585 {...} 2.768214 [...]
//                src dest {sub_node_set} sub_graph_finding_time+model_exectue_time target_node_list

                if (!s_info[2].trim().split("\\[|\\]")[1].equals("")) {
//                    System.out.println("sub_nodes:");
                    for (String t_s : s_info[2].trim().split("\\[|\\]")[1].split(",")) {
//                        System.out.print(t_s + ", ");
                        sub_nodes.add(Long.parseLong(t_s.trim()));
                    }
                }

                predict_length += t_nodes.size();
                sub_lent += sub_nodes.size();

//                System.out.println("\nmetries: " + s_info[2].trim().split("\\[|\\]")[2].trim());

                String[] metries = s_info[2].trim().split("\\[|\\]")[2].trim().split(" ");

                f1_score += Double.parseDouble(metries[0]);
                f1_sub_score += Double.parseDouble(metries[1]);
                f1_micro += Double.parseDouble(metries[2]);
                f1_macro += Double.parseDouble(metries[3]);
                pre += Double.parseDouble(metries[4]);
                sub_pre += Double.parseDouble(metries[5]);
                recall += Double.parseDouble(metries[6]);
                recall_sub += Double.parseDouble(metries[7]);
                roc += Double.parseDouble(metries[8]);
                roc_s += Double.parseDouble(metries[9]);
//                System.out.println(src+" "+dest+" : "+model_time);
                time += model_time;
//                System.out.println("Scores: " + f1_score + ", " + f1_sub_score + ", " + f1_micro + ", " + f1_macro
//                        + ", " + pre + ", " + sub_pre + ", " + recall + ", " + recall_sub);
//                System.exit(0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        AverageObj average = new AverageObj();
        average.query_num = queries.size();
        int index = 0;

        for (Pair<Long, Long> query : queries) {
            Long source = query.getKey();
            Long destination = query.getValue();

            String bbs_methods_log_file_path = bbs_result_path + "/" + bbs_result_prefix + source + "_" + destination + "_" + getBbs_result_postfix + ".log";

            ArrayList<double[]> bbs_results = readBBSResultsFromDisk(bbs_methods_log_file_path);
//            System.out.println("bbs_methods_log_file_path: " + bbs_methods_log_file_path);
//            System.out.println("bbs_results: " + bbs_results);

            // record bbs logs into a file (added by ying
            if (saving_bbs_logs_path!=0) {
                File file = new File(bbs_result_path + "/bbs_log_files.txt");
                FileWriter fw = new FileWriter(file, true);
                fw.write(bbs_methods_log_file_path + "\n");
                fw.close();
            }


//            String backbone_methods_log_file_path = compared_result_path + "/" + backbone_result_prefix + source + "_" + destination + "_" + getBbs_result_postfix + ".log";
            String backbone_methods_log_file_path = compared_result_path + "/" + backbone_result_prefix + source + "_" + destination + "_" + ParsedOptions.db_name + ".log";
            ArrayList<double[]> backbone_results = readBackboneResultsFromDisk(backbone_methods_log_file_path);
//            System.out.println("backbone_methods_log_file_path: " + backbone_methods_log_file_path);
//            System.out.println("backbone_results: " + backbone_results);

            String gnn_methods_log_file_path = compared_result_path + "/" + gnn_result_prefix + source + "_" + destination + "_" + ParsedOptions.db_name + ".log";
            ArrayList<double[]> gnn_results = readBBSResultsFromDisk(gnn_methods_log_file_path);
//            System.out.println("gnn_methods_log_file_path: " + gnn_methods_log_file_path);
//            System.out.println("backbone_results: " + backbone_results);

            if ((bbs_results.isEmpty() || backbone_results.isEmpty() || gnn_results.isEmpty())
                || (bbs_results.size()==0 || backbone_results.size()==0 || gnn_results.size()==0)) {
                continue;
            }

//            double highway_coverage = FindCoverageHighwayNode(bbs_methods_log_file_path, backbone_methods_log_file_path, source, destination);
//
            double backbone_dtw_distance = calculcateTheDTWDist(bbs_results, backbone_results);
            System.out.println("backbone_dtw_distance: " + backbone_dtw_distance);
            double gnn_dtw_distance = calculcateTheDTWDist(bbs_results, gnn_results);
            System.out.println("gnn_dtw_distance: " + gnn_dtw_distance);
////
//            double speedup = 1.0 * execution_performance.get(query).bbs_running_time / execution_performance.get(query).backbone_running_time;
            double[] avg_bbs = averageDoubleArray(bbs_results);
            double[] avg_backbone = averageDoubleArray(backbone_results);
            double[] avg_gnn = averageDoubleArray(gnn_results);

            double[] total_bbs = TotalDoubleArray(bbs_results);
            double[] total_backbone = TotalDoubleArray(backbone_results);
            double[] total_gnn = TotalDoubleArray(gnn_results);
//
            double backbone_max_cosine = getMaximumCosine(bbs_results, backbone_results);
            double gnn_max_cosine = getMaximumCosine(bbs_results, gnn_results);

            double backbone_max_cosine_average = getMaximumCosineEach(bbs_results, backbone_results);
            double gnn_max_cosine_average = getMaximumCosineEach(bbs_results, gnn_results);

            double backbone_max_distance_average = getMaxDistanceEach(bbs_results, backbone_results);
            double gnn_max_distance_average = getMaxDistanceEach(bbs_results, gnn_results);

//            System.out.println(index + " : " + source + "   >>>>>>>>>>>   " + destination + " |" + df.format(backbone_dtw_distance) + "," + df.format(gnn_dtw_distance)
//                    + "|  " + bbs_results.size() + "  " + backbone_results.size() + " " + gnn_results.size()
//                    + "  [" + df.format(avg_backbone[0]) + "," + df.format(avg_backbone[1]) + "," + df.format(avg_backbone[2]) + "]"
//                    + "  [" + df.format(avg_gnn[0]) + "," + df.format(avg_gnn[1]) + "," + df.format(avg_gnn[2]) + "]"
//                    + "  [" + df.format(avg_backbone[0] / avg_bbs[0]) + "," + df.format(avg_backbone[1] / avg_bbs[1]) + "," + df.format(avg_backbone[2] / avg_bbs[2]) + "]"
//                    + "  [" + df.format(avg_gnn[0] / avg_bbs[0]) + "," + df.format(avg_gnn[1] / avg_bbs[1]) + "," + df.format(avg_gnn[2] / avg_bbs[2]) + "]"
//                    + "| " + df.format(backbone_max_cosine) + ", " + df.format(gnn_max_cosine) + "|"
//                    + "| " + df.format(backbone_max_cosine_average) + ", " + df.format(gnn_max_cosine_average) + "|"
//                      + "  [" + df.format(avg_bbs[0]) + "," + df.format(avg_bbs[1]) + "," + df.format(avg_bbs[2]) + "]"
//                    + "  [" + df.format(avg_backbone[0]) + "," + df.format(avg_backbone[1]) + "," + df.format(avg_backbone[2]) + "]"
//                    + "  [" + df.format(avg_gnn[0]) + "," + df.format(avg_gnn[1]) + "," + df.format(avg_gnn[2]) + "]"
//                    + "  [" + df.format(avg_backbone[0] / avg_bbs[0]) + "," + df.format(avg_backbone[1] / avg_bbs[1]) + "," + df.format(avg_backbone[2] / avg_bbs[2]) + "]"
//                    + "  [" + df.format(avg_gnn[0] / avg_bbs[0]) + "," + df.format(avg_gnn[1] / avg_bbs[1]) + "," + df.format(avg_gnn[2] / avg_bbs[2]) + "]"
//                    + "| " + df.format(backbone_max_cosine) + ", " + df.format(gnn_max_cosine) + "|"     0.4094111515151515      0.6444552626262625      0.9627838383838389      0.44874612121212115     0.6382037171717173      0.6705425353535356      0.3291781818181818 0.6730621010101008       0.6610870000000001      0.8286655151515155
//                    + "| " + df.format(backbone_max_cosine_average) + ", " + df.format(gnn_max_cosine_average) + "|"
//                    + "| " + df.format(backbone_max_distance_average) + ", " + df.format(gnn_max_distance_average) + "|");

            System.out.println(index + " : " + source + "   >>>>>>>>>>>   " + destination
                    + "\n|(backbone_dtw_distance, gnn_dtw_distance) " + backbone_dtw_distance + "," + gnn_dtw_distance
                    + "|  " + bbs_results.size() + "  " + backbone_results.size() + " " + gnn_results.size()
                    + "  [(avg_backbone) " + avg_backbone[0] + "," + avg_backbone[1] + "," + avg_backbone[2] + "]"
                    + "  [(avg_gnn) " + avg_gnn[0] + "," + avg_gnn[1] + "," + avg_gnn[2] + "]"
                    + "  [(avg_bbs) " + avg_bbs[0] + "," + avg_bbs[1] + "," + avg_bbs[2] + "]"
                    + "  [(avg_backbone/avg_bbs) " + (avg_backbone[0] / avg_bbs[0]) + "," + avg_backbone[1] / avg_bbs[1] + "," + (avg_backbone[2] / avg_bbs[2]) + "]"
                    + "  [(avg_gnn/avg_bbs) " + (avg_gnn[0] / avg_bbs[0]) + "," + (avg_gnn[1] / avg_bbs[1]) + "," + (avg_gnn[2] / avg_bbs[2]) + "]"
                    + "|(backbone_max_cosine, gnn_max_cosine) " + backbone_max_cosine + ", " + gnn_max_cosine + "|"
                    + "|(backbone_max_cosine_average, gnn_max_cosine_average) " + backbone_max_cosine_average + ", " + gnn_max_cosine_average + "|"
                    + "|(backbone_max_distance_average, gnn_max_distance_average) " + backbone_max_distance_average + ", " + gnn_max_distance_average + "|");

            if (logging_status!=0) {
                LOGGER.info(index + " : " + source + "   >>>>>>>>>>>   " + destination
                        + "\n|(backbone_dtw_distance, gnn_dtw_distance) " + backbone_dtw_distance + "," + gnn_dtw_distance
                        + "|  " + bbs_results.size() + "  " + backbone_results.size() + " " + gnn_results.size()
                        + "  [(avg_backbone) " + avg_backbone[0] + "," + avg_backbone[1] + "," + avg_backbone[2] + "]"
                        + "  [(avg_gnn) " + avg_gnn[0] + "," + avg_gnn[1] + "," + avg_gnn[2] + "]"
                        + "  [(avg_bbs) " + avg_bbs[0] + "," + avg_bbs[1] + "," + avg_bbs[2] + "]"
                        + "  [(avg_backbone/avg_bbs) " + (avg_backbone[0] / avg_bbs[0]) + "," + avg_backbone[1] / avg_bbs[1] + "," + (avg_backbone[2] / avg_bbs[2]) + "]"
                        + "  [(avg_gnn/avg_bbs) " + (avg_gnn[0] / avg_bbs[0]) + "," + (avg_gnn[1] / avg_bbs[1]) + "," + (avg_gnn[2] / avg_bbs[2]) + "]"
                        + "|(backbone_max_cosine, gnn_max_cosine) " + backbone_max_cosine + ", " + gnn_max_cosine + "|"
                        + "|(backbone_max_cosine_average, gnn_max_cosine_average) " + backbone_max_cosine_average + ", " + gnn_max_cosine_average + "|"
                        + "|(backbone_max_distance_average, gnn_max_distance_average) " + backbone_max_distance_average + ", " + gnn_max_distance_average + "|");}

            index++;
//
            average.backbone_dtw += backbone_dtw_distance;
            average.gnn_dtw += gnn_dtw_distance;
//
            average.bbs_result_size += bbs_results.size();
            average.backbone_result_size += backbone_results.size();
            average.gnn_result_size += gnn_results.size();

            average.total_bbs_cost[0] += total_bbs[0];
            average.total_bbs_cost[1] += total_bbs[1];
            average.total_bbs_cost[2] += total_bbs[2];

            average.total_backbone_cost[0] += total_backbone[0];
            average.total_backbone_cost[1] += total_backbone[1];
            average.total_backbone_cost[2] += total_backbone[2];

            average.total_gnn_cost[0] += total_gnn[0];
            average.total_gnn_cost[1] += total_gnn[1];
            average.total_gnn_cost[2] += total_gnn[2];

            average.backbone_max_cosine += backbone_max_cosine;
            average.backbone_max_cosine_average += backbone_max_cosine_average;
            average.backbone_max_distance_average += backbone_max_distance_average;

            average.gnn_max_cosine += gnn_max_cosine;
            average.gnn_max_cosine_average += gnn_max_cosine_average;
            average.gnn_max_distance_average += gnn_max_distance_average;

        }

        System.out.println("============================================================================================================================");
        if (logging_status!=0) {LOGGER.info("============================================================================================================================");}
        int n = queries.size();
        System.out.println(" (average) " + average + ",\n"
                + " (total_predict_path_length_in_gnn) " + (predict_length) + ",\n"
                + " (predict_length / n) " + (predict_length / n) + ",\n"
                + " (total_sub_len_in_gnn) " + (sub_lent) + ",\n"
                + " (sub_len / n) " + (sub_lent / n) + ",\n"
                + " (f1_score / n) " + (f1_score / n) + "," + " (f1_sub_score / n) " + (f1_sub_score / n) + ",\n"
                + " (f1_micro / n) " + (f1_micro / n) + " (f1_macro / n) " + (f1_macro / n) + ",\n"
                + " (pre / n) " + (pre / n) + "," + " (sub_pre / n) " + (sub_pre / n) + ",\n"
                + " (recall / n) " + (recall / n) + "," + " (recall_sub / n) " + (recall_sub / n) + ",\n"
                + " (roc / n) " + (roc / n) + "," + " (roc_s / n) " + (roc_s / n) + ",\n"
                + " (gnn_total_time) " + (time) + " milliseconds"
        );
//        System.exit(0);
        if (logging_status!=0) {LOGGER.info(" (average) " + average + ",\n"
                + " (total_predict_path_length_in_gnn) " + (predict_length) + ",\n"
                + " (predict_length / n) " + (predict_length / n) + ",\n"
                + " (total_sub_len_in_gnn) " + (sub_lent) + ",\n"
                + " (sub_len / n) " + (sub_lent / n) + ",\n"
                + " (f1_score / n) " + (f1_score / n) + "," + " (f1_sub_score / n) " + (f1_sub_score / n) + ",\n"
                + " (f1_micro / n) " + (f1_micro / n) + " (f1_macro / n) " + (f1_macro / n) + ",\n"
                + " (pre / n) " + (pre / n) + "," + " (sub_pre / n) " + (sub_pre / n) + ",\n"
                + " (recall / n) " + (recall / n) + "," + " (recall_sub / n) " + (recall_sub / n) + ",\n"
                + " (roc / n) " + (roc / n) + "," + " (roc_s / n) " + (roc_s / n) + ",\n"
                + " (gnn_total_time) " + (time) + " milliseconds"
            );
        }
    }

    private ArrayList<double[]> readBBSResultsFromDisk(String filepath) {
        ArrayList<double[]> results = new ArrayList<>();
        try {
            File f = new File(filepath);
//            System.out.println("Read the bbs log file "+filepath);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
            while ((readLine = b.readLine()) != null) {
//                System.out.println("=====readBBSResultsFromDisk()=====");
//                System.out.println(readLine.split(",")[1].trim().substring(1,readLine.split(",")[1].trim().indexOf("]")).trim());
                String[] cost_infos = readLine.split(",")[1].trim().substring(1, readLine.split(",")[1].trim().indexOf("]")).trim().split(" ");
                double[] costs = new double[cost_infos.length];
                costs[0] = Double.parseDouble(cost_infos[0]);
                costs[1] = Double.parseDouble(cost_infos[1]);
                costs[2] = Double.parseDouble(cost_infos[2]);
                results.add(costs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sorted_result) {
            Collections.sort(results, new ResultCostsComaprator());
        }
//        results.stream().forEach(v -> System.out.println(v[0] + " " + v[1] + " " + v[2]));
        return results;
    }

    /**
     * @param filepath the path information of the backbone query results
     * @return the costs of the paths
     */
    private ArrayList<double[]> readBackboneResultsFromDisk(String filepath) {
        ArrayList<double[]> results = new ArrayList<>();
        try {
            File f = new File(filepath);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
            while ((readLine = b.readLine()) != null) {
                String[] cost_infos = readLine.split(" ")[3].substring(1, readLine.split(" ")[3].length() - 1).split(",");
                double[] costs = new double[cost_infos.length];
                costs[0] = Double.parseDouble(cost_infos[0]);
                costs[1] = Double.parseDouble(cost_infos[1]);
                costs[2] = Double.parseDouble(cost_infos[2]);
                results.add(costs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sorted_result) {
            Collections.sort(results, new ResultCostsComaprator());
        }
        return results;
    }

    public double calculcateTheDTWDist(ArrayList<double[]> bbs_results, ArrayList<double[]> approx_results) {
        int n = bbs_results.size();
        int m = approx_results.size();

        ArrayList<double[]> max_min = getMaxMix(bbs_results, approx_results);

//        System.out.println("=====calculcateTheDTWDist()=====");
//        System.out.println(max_min.get(0)[0] + "," + max_min.get(0)[1] + "," + max_min.get(0)[2]);
//        System.out.println(max_min.get(1)[0] + "," + max_min.get(1)[1] + "," + max_min.get(1)[2]);

        if (bbs_results.size() == 1 && approx_results.size() == 1 && IsMaxMinEqual(max_min)) {
            return 0;
        }


        double[][] dtw = new double[1 + n][1 + m];
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dtw[i][j] = Double.POSITIVE_INFINITY;
            }
        }

        dtw[0][0] = 0;

        class sortAxis implements Comparator<double[]> {
            @Override
            public int compare(double[] o1, double[] o2) {
                int n = o1.length;
                for (int i = 0; i < n; i++) {
                    if (o1[i] != o2[i]) {
                        return o1[i] - o2[i] < 0 ? -1 : 1;
                    }
                }
                return 0;
            }
        }

        Collections.sort(bbs_results, new sortAxis());
        Collections.sort(approx_results, new sortAxis());

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double dist = distance(bbs_results.get(i - 1), approx_results.get(j - 1), max_min, ParsedOptions.dtw_normal);
                dtw[i][j] = dist + findMin(dtw[i - 1][j - 1], dtw[i - 1][j], dtw[i][j - 1]);
            }
        }

        return dtw[n][m];
    }

    private ArrayList<double[]> getMaxMix(ArrayList<double[]> bbs_results, ArrayList<double[]> approx_results) {

        int n = bbs_results.get(0).length;


        ArrayList<double[]> bbs_max_min = getMaxMixValues(bbs_results);
        ArrayList<double[]> backbone_max_min = getMaxMixValues(approx_results);

        ArrayList<double[]> max_min = new ArrayList<>();
        double[] max_array = new double[n];
        double[] min_array = new double[n];

        for (int i = 0; i < n; i++) {
            max_array[i] = Math.max(bbs_max_min.get(0)[i], backbone_max_min.get(0)[i]);
            min_array[i] = Math.min(bbs_max_min.get(1)[i], backbone_max_min.get(1)[i]);
        }

        max_min.add(max_array);
        max_min.add(min_array);

        return max_min;
    }

    private ArrayList<double[]> getMaxMixValues(ArrayList<double[]> results) {
        int n = results.get(0).length;
        double[] max = new double[n];
        double[] min = new double[n];

        for (int i = 0; i < n; i++) {
            max[i] = Double.NEGATIVE_INFINITY;
            min[i] = Double.POSITIVE_INFINITY;
        }

        for (double[] cost : results) {
            for (int i = 0; i < n; i++) {
                double value = cost[i];

                if (value > max[i]) {
                    max[i] = value;
                }

                if (value < min[i]) {
                    min[i] = value;
                }
            }
        }
        ArrayList<double[]> result = new ArrayList<>();
        result.add(max);
        result.add(min);
        return result;
    }

    private double distance(double[] bbs_costs, double[] backbone_costs, ArrayList<double[]> max_min, boolean dtw_normal) {
        double distance = 0;
        if (dtw_normal) {
            for (int i = 0; i < bbs_costs.length; i++) {
                if (max_min.get(0)[i] == max_min.get(1)[i]) {
                    double bbs_value = (bbs_costs[i] - max_min.get(1)[i]) / max_min.get(1)[i];
                    double backbone_value = (backbone_costs[i] - max_min.get(1)[i]) / max_min.get(1)[i];
                    distance += (bbs_value - backbone_value) * (bbs_value - backbone_value);
                } else {
                    double bbs_value = (bbs_costs[i] - max_min.get(1)[i]) / (max_min.get(0)[i] - max_min.get(1)[i]);
                    double backbone_value = (backbone_costs[i] - max_min.get(1)[i]) / (max_min.get(0)[i] - max_min.get(1)[i]);
                    distance += (bbs_value - backbone_value) * (bbs_value - backbone_value);
                }
            }

        } else {
            for (int i = 0; i < bbs_costs.length; i++) {
                distance += (bbs_costs[i] - backbone_costs[i]) * (bbs_costs[i] - backbone_costs[i]);
            }
        }

//        System.out.println("=====distance()=====");
//        System.out.println(Math.sqrt(distance));
        return Math.sqrt(distance);
    }

    private double findMin(double... values) {
        double minValue = Double.MAX_VALUE;
        for (double value : values) {
            minValue = Math.min(minValue, value);
        }
        return minValue;
    }

    private boolean IsMaxMinEqual(ArrayList<double[]> max_min) {
        double[] max = max_min.get(0);
        double[] min = max_min.get(1);

        for (int i = 0; i < max.length; i++) {
            if (max[i] != min[i]) return false;
        }

        return true;
    }

    public double[] averageDoubleArray(ArrayList<double[]> results) {
        double[] avg_result = new double[3];
        int n = results.size();
        for (double[] c : results) {
            avg_result[0] += c[0];
            avg_result[1] += c[1];
            avg_result[2] += c[2];
        }

        avg_result[0] /= n;
        avg_result[1] /= n;
        avg_result[2] /= n;

//        System.out.println("=====averageDoubleArray()=====");
//        System.out.println(avg_result);
        return avg_result;
    }

    public double[] TotalDoubleArray(ArrayList<double[]> results) {
        double[] total_result = new double[3];
        for (double[] c : results) {
            total_result[0] += c[0];
            total_result[1] += c[1];
            total_result[2] += c[2];
        }
        return total_result;
    }


    public double getMaximumCosine(ArrayList<double[]> bbs_results, ArrayList<double[]> approx_results) {
        double similarity = 0.0;

        ArrayList<double[]> max_min = getMaxMix(bbs_results, approx_results);

        if (bbs_results.size() == 1 && approx_results.size() == 1 && IsMaxMinEqual(max_min)) {
            return 1;
        }

        for (double[] bbs_c : bbs_results) {
            for (double[] backbone_c : approx_results) {
                double c_s = getCosineSimilarity(bbs_c, backbone_c, max_min, ParsedOptions.dtw_normal);
                if (c_s > similarity) {
                    similarity = c_s;
                }

            }
        }

        return similarity;
    }

    private double getCosineSimilarity(double[] bbs_c, double[] backbone_c, ArrayList<double[]> max_min, boolean normalization) {
        int n = bbs_c.length;
        double sum_Denominator = 0;
        double bbs_s = 0;
        double backbone_s = 0;

//        System.out.println("========================================================");
//        System.out.println("=====getCosineSimilarity()=====");
//        System.out.println(bbs_c[0] + "," + bbs_c[1] + "," + bbs_c[2]);
//        System.out.println(backbone_c[0] + "," + backbone_c[1] + "," + backbone_c[2]);

        if (normalization) {
            for (int i = 0; i < n; i++) {
                double bbs_value, backbone_value;
                if (max_min.get(0)[i] == max_min.get(1)[i]) {
                    bbs_value = (bbs_c[i] - max_min.get(1)[i]) / max_min.get(1)[i] + residents;
                    backbone_value = (backbone_c[i] - max_min.get(1)[i]) / max_min.get(1)[i] + residents;
                } else {
                    bbs_value = (bbs_c[i] - max_min.get(1)[i]) / (max_min.get(0)[i] - max_min.get(1)[i]) + residents;
                    backbone_value = (backbone_c[i] - max_min.get(1)[i]) / (max_min.get(0)[i] - max_min.get(1)[i]) + residents;
                }
                sum_Denominator += (bbs_value * backbone_value);
                bbs_s += (bbs_value * bbs_value);
                backbone_s += (backbone_value * backbone_value);

//                System.out.println("bbs_value+backbone_value+sum_Denominator+bbs_s+backbone_s");
//                System.out.println(bbs_value+" "+backbone_value+" "+sum_Denominator+" "+bbs_s+" "+backbone_s);
            }
        } else {
            for (int i = 0; i < n; i++) {
                sum_Denominator += (bbs_c[i] * backbone_c[i]);
                bbs_s += (bbs_c[i] * bbs_c[i]);
                backbone_s += (backbone_c[i] * backbone_c[i]);
            }
        }

        return (sum_Denominator) / (Math.sqrt(bbs_s) * Math.sqrt(backbone_s));
    }

    public double getMaximumCosineEach(ArrayList<double[]> bbs_results, ArrayList<double[]> backbone_results) {
        ArrayList<double[]> max_min = getMaxMix(bbs_results, backbone_results);
        double sum_sim = 0.0;

//        System.out.println("=====getMaximumCosineEach()=====");
//        System.out.println(max_min.get(0)[0] + "," + max_min.get(0)[1] + "," + max_min.get(0)[2]);
//        System.out.println(max_min.get(1)[0] + "," + max_min.get(1)[1] + "," + max_min.get(1)[2]);


        if (bbs_results.size() == 1 && backbone_results.size() == 1 && IsMaxMinEqual(max_min)) {
            return 1;
        }

        for (double[] bbs_c : bbs_results) {
            double similarity = Double.NEGATIVE_INFINITY;
            for (double[] backbone_c : backbone_results) {
                double c_s = getCosineSimilarity(bbs_c, backbone_c, max_min, ParsedOptions.dtw_normal);
                if (c_s > similarity) {
                    similarity = c_s;
                }
            }
            sum_sim += similarity;
        }

        return sum_sim / bbs_results.size();
    }


    public double getMaxDistanceEach(ArrayList<double[]> bbs_results, ArrayList<double[]> backbone_results) {
        ArrayList<double[]> max_min = getMaxMix(bbs_results, backbone_results);
        double sum_distance = 0.0;


        if (bbs_results.size() == 1 && backbone_results.size() == 1 && IsMaxMinEqual(max_min)) {
            return 1;
        }

        for (double[] bbs_c : bbs_results) {
//        System.out.println(max_min.get(0)[0] + "," + max_min.get(0)[1] + "," + max_min.get(0)[2]);
//        System.out.println(max_min.get(1)[0] + "," + max_min.get(1)[1] + "," + max_min.get(1)[2]);

            double distance = Double.POSITIVE_INFINITY;
            for (double[] backbone_c : backbone_results) {
                double c_distance = distance(bbs_c, backbone_c, max_min, ParsedOptions.dtw_normal);
                if (c_distance < distance) {
                    distance = c_distance;
                }
            }
            sum_distance += distance;
        }

        return sum_distance / bbs_results.size();
    }

}