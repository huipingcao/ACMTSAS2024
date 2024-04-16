package Comparison;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import utilities.ParsedOptions;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DTWComparisonGNN {
    String gnn_result_prefix = "gnn_";
    String backbone_result_prefix = "backbone_";
    DecimalFormat df = new DecimalFormat("#.###");
    String running_logs_folders = ParsedOptions.logFolder;
    boolean sorted_result = true;

    String db_name = "C9_NY_NONE_5K";
    String datetime = "20221205_121813";
    String gnn_model = "64_128_16_1_EmbedTrue_Transformer_ConLossTrue";
    private double residents = 0.0000001;


    public static void main(String args[]) throws ParseException {
        DTWComparisonGNN dtw = new DTWComparisonGNN();
        dtw.comparision();

    }

    public void comparision() {
        String results_path = ParsedOptions.resultFolder + "/" + ParsedOptions.db_name + "/" + gnn_model + "/" + ParsedOptions.timestamp;
        System.out.println(results_path);

        AverageObj average = new AverageObj();

        File result_folder = new File(results_path);
        String timestamp = ParsedOptions.timestamp;

        String[] path_infos = results_path.split("/");
        String date = path_infos[path_infos.length - 1].split("_")[0];
        String time = path_infos[path_infos.length - 1].split("_")[1];

        System.out.println("Date and time :" + date + "    " + time);

        HashSet<Pair<String, String>> queries = new HashSet<>();

        int[] query_dist = new int[4];

        for (File log_file : result_folder.listFiles(new ResultLogFileFilter(gnn_result_prefix))) {

            String log_file_name = log_file.getName();

            String source = log_file_name.split("_")[1];
            String destination = log_file_name.split("_")[2];


            if (timestamp.equals("")) {
                String timestamp_str = log_file_name.split("_")[3].substring(0, log_file_name.split("_")[3].lastIndexOf("."));
                timestamp = timestamp_str;
            }

            Pair<String, String> query = new MutablePair<>(source, destination);
            queries.add(query);
        }


        File log_file = findPerformanceLogFile(date, time);
        System.out.println(log_file);
        HashMap<Pair<String, String>, RunningInforObj> execution_performance = readRunningInformation(queries, log_file);
        String filename = log_file.getName();
        int numberOfQuery = Integer.parseInt(filename.substring(filename.lastIndexOf("_") + 1, filename.indexOf("[") - 1));


        long yourSeconds = Long.parseLong(timestamp);
        Date d = new Date(yourSeconds);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println("Expeirments are conducted at :" + sf.format(d));
        System.out.println("Number of queries : " + numberOfQuery);


        average.query_num = numberOfQuery;
        average.finised_query = execution_performance.size();

        int index = 1;
        for (Pair<String, String> query : queries) {
            String source = query.getKey();
            String destination = query.getValue();
//
            if (!execution_performance.containsKey(query)) {
                continue;
            }

//
//            //The result of the backbone query running log files
            String backbone_methods_log_file_path = results_path + "/" + backbone_result_prefix + source + "_" + destination + "_" + timestamp + ".log";
//            System.out.println(backbone_methods_log_file_path);
            ArrayList<double[]> backbone_results = readBackboneResultsFromDisk(backbone_methods_log_file_path);
//            System.out.println("====================================================");
            String gnn_methods_log_file_path = results_path + "/" + gnn_result_prefix + source + "_" + destination + "_" + timestamp + ".log";
            ArrayList<double[]> gnn_results = readBBSResultsFromDisk(gnn_methods_log_file_path);

            double highway_coverage = FindCoverageHighwayNode(gnn_methods_log_file_path, backbone_methods_log_file_path, source, destination);

            double dtw_distance = calculcateTheDTWDist(gnn_results, backbone_results);
//
            double speedup = 1.0 * execution_performance.get(query).gnn_running_time / execution_performance.get(query).backbone_running_time;
            double[] avg_gnn = averageDoubleArray(gnn_results);
            double[] avg_backbone = averageDoubleArray(backbone_results);

            double max_cosine = getMaximumCosine(gnn_results, backbone_results);
            double max_cosine_average = getMaximumCosineEach(gnn_results, backbone_results);

            double max_distance_average = getMaxDistanceEach(gnn_results, backbone_results);

//
//
            System.out.println(index + " : " + source + "   >>>>>>>>>>>   " + destination + " " + df.format(dtw_distance)
                    + "|  |" + gnn_results.size() + "  " + backbone_results.size()
                    + "|  |" + execution_performance.get(query).gnn_running_time + "  " + execution_performance.get(query).backbone_running_time
                    + "|  |" + speedup
                    + "  [" + df.format(avg_gnn[0]) + "," + df.format(avg_gnn[1]) + "," + df.format(avg_gnn[2]) + "]"
                    + "  [" + df.format(avg_backbone[0]) + "," + df.format(avg_backbone[1]) + "," + df.format(avg_backbone[2]) + "]"
                    + "  [" + df.format(avg_backbone[0] / avg_gnn[0]) + "," + df.format(avg_backbone[1] / avg_gnn[1]) + "," + df.format(avg_backbone[2] / avg_gnn[2]) + "]"
                    + "| " + max_cosine + ", " + max_cosine_average + ", " + max_distance_average + "," + highway_coverage);

//            if (index == 52) {
//                System.exit(0);
//            }

            index++;

            average.dtw += dtw_distance;

            average.gnn_result_size += gnn_results.size();
            average.backbone_result_size += backbone_results.size();
            average.gnn_running_time += (execution_performance.get(query).gnn_running_time);
            average.backbone_running_time += (execution_performance.get(query).backbone_running_time);
            average.avg_gnn_cost[0] += avg_gnn[0];
            average.avg_gnn_cost[1] += avg_gnn[1];
            average.avg_gnn_cost[2] += avg_gnn[2];
            average.avg_backbone_cost[0] += avg_backbone[0];
            average.avg_backbone_cost[1] += avg_backbone[1];
            average.avg_backbone_cost[2] += avg_backbone[2];
            average.avg_cost[0] += (avg_backbone[0] / avg_gnn[0]);
            average.avg_cost[1] += (avg_backbone[1] / avg_gnn[1]);
            average.avg_cost[2] += (avg_backbone[2] / avg_gnn[2]);
            average.max_cosine += max_cosine;
            average.max_cosine_average += max_cosine_average;
            average.max_distance_average += max_distance_average;
            average.highway_nodes_coverage += highway_coverage;
        }


        System.out.println("============================================================================================================================");
        System.out.println(average);
    }

    private double FindCoverageHighwayNode(String gnn_methods_log_file_path, String backbone_methods_log_file_path, String source_node, String destination_node) {
        long source_id = Long.parseLong(source_node);
        long dest_id = Long.parseLong(destination_node);

        Pattern pattern = Pattern.compile("\\(\\d+\\)");
        HashSet<Long> exact_nodes = new HashSet<>();

        try {
            File f = new File(gnn_methods_log_file_path);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
            while ((readLine = b.readLine()) != null) {
//                System.out.println(readLine);
                Matcher matcher = pattern.matcher(readLine);
                while (matcher.find()) {
                    long node_id = Long.parseLong(matcher.group(0).replace("(", "").replace(")", ""));
                    if (node_id != source_id && node_id != dest_id) {
                        exact_nodes.add(node_id);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(exact_nodes.size());
        double sum_coverage = 0;
        double bacbkbone_paths_size = 0;
        try {
            File f = new File(backbone_methods_log_file_path);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
            while ((readLine = b.readLine()) != null) {
                bacbkbone_paths_size++;
                int same_node_counter = 0;
                int size = 0;
//                System.out.println(readLine);
                Matcher matcher;

                if (readLine.contains("(")) {
                    pattern = Pattern.compile("\\(\\d+\\)");
                    matcher = pattern.matcher(readLine);
                    while (matcher.find()) {
                        String node_str = matcher.group(0).replace("(", "").replace(")", "");
                        long node_id = Long.parseLong(node_str);
                        if (node_id != source_id && node_id != dest_id) {
                            if (exact_nodes.contains(node_id)) {
                                same_node_counter++;
                            }
                            size++;
                        }

                    }
                } else {
                    pattern = Pattern.compile("\\[.*\\]");
                    matcher = pattern.matcher(readLine);
                    if (matcher.find()) {
                        String nodeList_str = matcher.group().split("]")[1].trim().replace("[", "").replace(",", "");
                        for (String node_str : nodeList_str.split(" ")) {
                            long node_id = Long.parseLong(node_str);
                            if (node_id != source_id && node_id != dest_id) {
                                if (exact_nodes.contains(node_id)) {
                                    same_node_counter++;
                                }
                                size++;
                            }
                        }
                    }
                }

                sum_coverage += size == 0 ? 0 : (double) same_node_counter / size;
//                System.out.println(sum_coverage + " " + same_node_counter + " " + size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(sum_coverage + " " + bacbkbone_paths_size + " " + (sum_coverage / bacbkbone_paths_size));

        return sum_coverage / bacbkbone_paths_size;
    }

    public double getMaxDistanceEach(ArrayList<double[]> gnn_results, ArrayList<double[]> backbone_results) {
        ArrayList<double[]> max_min = getMaxMix(gnn_results, backbone_results);
        double sum_distance = 0.0;


        if (gnn_results.size() == 1 && backbone_results.size() == 1 && IsMaxMinEqual(max_min)) {
            return 1;
        }

        for (double[] gnn_c : gnn_results) {
            double distance = Double.POSITIVE_INFINITY;
            for (double[] backbone_c : backbone_results) {
                double c_distance = distance(gnn_c, backbone_c, max_min, ParsedOptions.dtw_normal);
                if (c_distance < distance) {
                    distance = c_distance;
                }
            }
            sum_distance += distance;
        }

        return sum_distance / gnn_results.size();
    }

    public double getMaximumCosineEach(ArrayList<double[]> gnn_results, ArrayList<double[]> backbone_results) {


        ArrayList<double[]> max_min = getMaxMix(gnn_results, backbone_results);
        double sum_sim = 0.0;

//        System.out.println(max_min.get(0)[0] + "," + max_min.get(0)[1] + "," + max_min.get(0)[2]);
//        System.out.println(max_min.get(1)[0] + "," + max_min.get(1)[1] + "," + max_min.get(1)[2]);


        if (gnn_results.size() == 1 && backbone_results.size() == 1 && IsMaxMinEqual(max_min)) {
            return 1;
        }

        for (double[] gnn_c : gnn_results) {
            double similarity = Double.NEGATIVE_INFINITY;
            for (double[] backbone_c : backbone_results) {
                double c_s = getCosineSimilarity(gnn_c, backbone_c, max_min, ParsedOptions.dtw_normal);
                if (c_s > similarity) {
                    similarity = c_s;
                }
            }
            sum_sim += similarity;
        }

        return sum_sim / gnn_results.size();
    }

    public double getMaximumCosine(ArrayList<double[]> gnn_results, ArrayList<double[]> backbone_results) {
        double similarity = 0.0;

        ArrayList<double[]> max_min = getMaxMix(gnn_results, backbone_results);

        if (gnn_results.size() == 1 && backbone_results.size() == 1 && IsMaxMinEqual(max_min)) {
            return 1;
        }

        for (double[] gnn_c : gnn_results) {
            for (double[] backbone_c : backbone_results) {
                double c_s = getCosineSimilarity(gnn_c, backbone_c, max_min, ParsedOptions.dtw_normal);
                if (c_s > similarity) {
                    similarity = c_s;
                }

            }
        }

        return similarity;
    }

    private ArrayList<double[]> getMaxMix(ArrayList<double[]> gnn_results, ArrayList<double[]> backbone_results) {

        int n = gnn_results.get(0).length;


        ArrayList<double[]> gnn_max_min = getMaxMixValues(gnn_results);
        ArrayList<double[]> backbone_max_min = getMaxMixValues(backbone_results);

        ArrayList<double[]> max_min = new ArrayList<>();
        double[] max_array = new double[n];
        double[] min_array = new double[n];

        for (int i = 0; i < n; i++) {
            max_array[i] = Math.max(gnn_max_min.get(0)[i], backbone_max_min.get(0)[i]);
            min_array[i] = Math.min(gnn_max_min.get(1)[i], backbone_max_min.get(1)[i]);
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

    private double getCosineSimilarity(double[] gnn_c, double[] backbone_c, ArrayList<double[]> max_min, boolean normalization) {
        int n = gnn_c.length;
        double sum_Denominator = 0;
        double gnn_s = 0;
        double backbone_s = 0;
//        System.out.println();
//        System.out.println("========================================================");
//        System.out.println(gnn_c[0] + "," + gnn_c[1] + "," + gnn_c[2]);
//        System.out.println(backbone_c[0] + "," + backbone_c[1] + "," + backbone_c[2]);

        double[] gnn_vs = new double[n];
        double[] backbone_vs = new double[n];

        if (normalization) {
            for (int i = 0; i < n; i++) {
                double gnn_value, backbone_value;
                gnn_value = (gnn_c[i] - max_min.get(1)[i]) / (max_min.get(0)[i] - max_min.get(1)[i] + residents);
                backbone_value = (backbone_c[i] - max_min.get(1)[i]) / (max_min.get(0)[i] - max_min.get(1)[i] + residents);
                gnn_vs[i] = gnn_value;
                backbone_vs[i] = backbone_value;

//                }
                sum_Denominator += (gnn_value * backbone_value);
                gnn_s += (gnn_value * gnn_value);
                backbone_s += (backbone_value * backbone_value);
//                System.out.println(gnn_value+" "+backbone_value+" "+sum_Denominator+" "+gnn_s+" "+backbone_s);
            }
        } else {
            for (int i = 0; i < n; i++) {
                sum_Denominator += (gnn_c[i] * backbone_c[i]);
                gnn_s += (gnn_c[i] * gnn_c[i]);
                backbone_s += (backbone_c[i] * backbone_c[i]);
                gnn_vs[i] = gnn_c[i];
                backbone_vs[i] = backbone_c[i];
            }
        }


        double sum_gnn = 0, sum_backbone = 0;
        for (int i = 0; i < n; i++) {
            sum_gnn += gnn_vs[i] * gnn_vs[i];
            sum_backbone += backbone_vs[i] * backbone_vs[i];
        }

        sum_gnn = Math.sqrt(sum_gnn);
        sum_backbone = Math.sqrt(sum_backbone);
        sum_Denominator = 0;
        gnn_s = 0;
        backbone_s = 0;
        for (int i = 0; i < n; i++) {
            sum_Denominator += (gnn_vs[i] / (sum_gnn + residents) * backbone_vs[i] / (sum_backbone + residents));
            gnn_s += (gnn_vs[i] / (sum_gnn + residents) * gnn_vs[i] / (sum_gnn + residents));
            backbone_s += (backbone_vs[i] / (sum_backbone + residents) * backbone_vs[i] / (sum_backbone + residents));
//            System.out.println("="+sum_Denominator+" "+gnn_s+" "+backbone_s)
        }

//        System.out.println(gnn_vs[0] + "," + gnn_vs[1] + "," + gnn_vs[2]);
//        System.out.println(gnn_vs[0] / (sum_gnn + residents) + "," + gnn_vs[1] / (sum_gnn + residents) + "," + gnn_vs[2] / (sum_gnn + residents));
//        System.out.println(backbone_vs[0] + "," + backbone_vs[1] + "," + backbone_vs[2]);
//        System.out.println(backbone_vs[0] / (sum_backbone + residents) + "," + backbone_vs[1] / (sum_backbone + residents) + "," + backbone_vs[2] / (sum_backbone + residents));

        return (sum_Denominator) / (Math.sqrt(gnn_s) * Math.sqrt(backbone_s) + residents);


    }

    private File findPerformanceLogFile(String date, String time) {
        File log_folers = new File(this.running_logs_folders);
        for (File f : log_folers.listFiles()) {
            String file_name = f.getName();

            if (file_name.contains("[") && file_name.contains("]")) {

//                System.out.println(file_name);
                String datetime = file_name.substring(file_name.indexOf("[") + 1, file_name.indexOf("]"));
                String data_str = datetime.split("~")[0].replace("-", "");
                String time_str = datetime.split("~")[1].replace("-", "");

                if (data_str.equals(date) && time_str.equals(time)) {
                    return f;
                }

            }
        }

        return null;
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

        return avg_result;
    }

    private HashMap<Pair<String, String>, RunningInforObj> readRunningInformation(HashSet<Pair<String, String>> queries, File performance_log_file) {

        HashMap<Pair<String, String>, RunningInforObj> execution_performance = new HashMap<>();

        for (Pair<String, String> e : queries) {
            String src = e.getKey();
            String dest = e.getValue();
            RunningInforObj obj = new RunningInforObj();
            Pair<String, String> key = new MutablePair(src, dest);
//            System.out.println("Add to HashMap " + key);
            execution_performance.put(key, obj);
        }


        try {
            BufferedReader b = new BufferedReader(new FileReader(performance_log_file));
            String readLine = "";
            boolean candidateLogs = true;
            while ((readLine = b.readLine()) != null && candidateLogs) {
                String running_time_trim = readLine.substring(readLine.lastIndexOf(":") + 1).trim();

                if (readLine.contains("Query time : ")) {
//                    System.out.println(readLine);
                    String src = readLine.split(" ")[3].split("\\|")[1].split(">")[0];
                    String dest = readLine.split(" ")[3].split("\\|")[1].split(">")[1];
                    long gnn_running_time = Long.parseLong(running_time_trim);
//                    System.out.println(readLine+"");
//
                    Pair<Long, Long> key = new MutablePair(src, dest);
//                    System.out.println(src + "," + dest+"  "+gnn_running_time);

                    //keep the maximum running time value
                    if (execution_performance.containsKey(key) && execution_performance.get(key).gnn_running_time < gnn_running_time) {
                        execution_performance.get(key).gnn_running_time = gnn_running_time;
                    }

                }


                if (readLine.contains("overall running time")) {
//                    System.out.println(readLine);

                    String src = readLine.split(" ")[3].split("\\|")[1].split(">")[0];
                    String dest = readLine.split(" ")[3].split("\\|")[1].split(">")[1];
                    long backbone_running_time = Long.parseLong(running_time_trim.substring(0, running_time_trim.lastIndexOf("ms")).trim());

//                    System.out.println(backbone_running_time);
//                    System.out.println(src + "," + dest+"  "+backbone_running_time);


                    Pair<Long, Long> key = new MutablePair(src, dest);
                    if (execution_performance.containsKey(key)) {
                        execution_performance.get(key).backbone_running_time = backbone_running_time;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(execution_performance.size());

        ArrayList<Pair<String, String>> deleted_keys = new ArrayList<>();
        for (Map.Entry<Pair<String, String>, RunningInforObj> e : execution_performance.entrySet()) {
            if (e.getValue().backbone_running_time >= 9999999 || e.getValue().gnn_running_time >= 9999999) {
                deleted_keys.add(e.getKey());
            }
        }

        for (Pair<String, String> key : deleted_keys) {
            execution_performance.remove(key);
        }

//        System.out.println(execution_performance.size());

        return execution_performance;
    }

    public double calculcateTheDTWDist(ArrayList<double[]> gnn_results, ArrayList<double[]> backbone_results) {
        int n = gnn_results.size();
        int m = backbone_results.size();

        ArrayList<double[]> max_min = getMaxMix(gnn_results, backbone_results);

//        System.out.println(max_min.get(0)[0] + "," + max_min.get(0)[1] + "," + max_min.get(0)[2]);
//        System.out.println(max_min.get(1)[0] + "," + max_min.get(1)[1] + "," + max_min.get(1)[2]);


        if (gnn_results.size() == 1 && backbone_results.size() == 1 && IsMaxMinEqual(max_min)) {
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

        Collections.sort(gnn_results, new sortAxis());
        Collections.sort(backbone_results, new sortAxis());

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double dist = distance(gnn_results.get(i - 1), backbone_results.get(j - 1), max_min, ParsedOptions.dtw_normal);
                dtw[i][j] = dist + findMin(dtw[i - 1][j - 1], dtw[i - 1][j], dtw[i][j - 1]);
            }
        }


        return dtw[n][m];

    }

    private boolean IsMaxMinEqual(ArrayList<double[]> max_min) {
        double[] max = max_min.get(0);
        double[] min = max_min.get(1);

        for (int i = 0; i < max.length; i++) {
            if (max[i] != min[i]) return false;
        }

        return true;

    }

    private double findMin(double... values) {
        double minValue = Double.MAX_VALUE;
        for (double value : values) {
            minValue = Math.min(minValue, value);
        }
        return minValue;
    }

    private double distance(double[] gnn_costs, double[] backbone_costs, ArrayList<double[]> max_min, boolean dtw_normal) {
        double distance = 0;

        int n = gnn_costs.length;
        double[] gnn_vs = new double[n];
        double[] backbone_vs = new double[n];

        if (dtw_normal) {
            for (int i = 0; i < gnn_costs.length; i++) {
                double gnn_value = (gnn_costs[i] - max_min.get(1)[i]) / (Math.abs(max_min.get(0)[i] - max_min.get(1)[i] + residents));
                double backbone_value = (backbone_costs[i] - max_min.get(1)[i]) / (Math.abs(max_min.get(0)[i] - max_min.get(1)[i] + residents));
                distance += (gnn_value - backbone_value) * (gnn_value - backbone_value);
                gnn_vs[i] = gnn_value;
                backbone_vs[i] = backbone_value;
//                }
            }

        } else {
            for (int i = 0; i < gnn_costs.length; i++) {
                distance += (gnn_costs[i] - backbone_costs[i]) * (gnn_costs[i] - backbone_costs[i]);
                gnn_vs[i] = gnn_costs[i];
                backbone_vs[i] = backbone_costs[i];
            }
        }

        distance = 0;
        double sum_gnn = 0, sun_backbone = 0;
        for (int i = 0; i < n; i++) {
            sum_gnn += gnn_vs[i] * gnn_vs[i];
            sun_backbone += backbone_vs[i] * backbone_vs[i];
        }

        sum_gnn = Math.sqrt(sum_gnn);
        sun_backbone = Math.sqrt(sun_backbone);

        for (int i = 0; i < gnn_costs.length; i++) {
            distance += (gnn_vs[i] / (sum_gnn + residents) - backbone_vs[i] / (sun_backbone + residents)) * (gnn_vs[i] / (sum_gnn + residents) - backbone_vs[i] / (sun_backbone + residents));
        }

        return Math.sqrt(distance);
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
//                System.out.println(readLine);
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

//        System.exit(0);

        if (sorted_result) {
            Collections.sort(results, new ResultCostsComaprator());
        }
        return results;
    }

    private ArrayList<double[]> readBBSResultsFromDisk(String filepath) {
        ArrayList<double[]> results = new ArrayList<>();
        try {

            Pattern pattern = Pattern.compile("\\(\\d+\\)");

            File f = new File(filepath);
//            System.out.println("Read the gnn log file "+filepath);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
            while ((readLine = b.readLine()) != null) {

                Matcher matcher = pattern.matcher(readLine);
                int path_length = 0;
                while (matcher.find()) {
                    path_length++;
                }

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

    private int getNumberHops(String filepath) {
        ArrayList<MutablePair<Double, Integer>> results = new ArrayList<>();

        results.add(new MutablePair<>(Double.MAX_VALUE, Integer.MAX_VALUE));
        results.add(new MutablePair<>(Double.MAX_VALUE, Integer.MAX_VALUE));
        results.add(new MutablePair<>(Double.MAX_VALUE, Integer.MAX_VALUE));

        try {

            Pattern pattern = Pattern.compile("\\(\\d+\\)");

            File f = new File(filepath);
//            System.out.println("Read the gnn log file "+filepath);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine = "";
            while ((readLine = b.readLine()) != null) {

                Matcher matcher = pattern.matcher(readLine);
                int path_length = 0;
                while (matcher.find()) {
                    path_length++;
                }

//                System.out.println(readLine.split(",")[1].trim().substring(1,readLine.split(",")[1].trim().indexOf("]")).trim());
                String[] cost_infos = readLine.split(",")[1].trim().substring(1, readLine.split(",")[1].trim().indexOf("]")).trim().split(" ");
                double[] costs = new double[cost_infos.length];
                costs[0] = Double.parseDouble(cost_infos[0]);
                costs[1] = Double.parseDouble(cost_infos[1]);
                costs[2] = Double.parseDouble(cost_infos[2]);

                if (costs[0] < results.get(0).getKey()) {
                    results.remove(0);
                    results.add(0, new MutablePair<>(costs[0], path_length));
                }

                if (costs[1] < results.get(1).getKey()) {
                    results.remove(1);
                    results.add(1, new MutablePair<>(costs[1], path_length));
                }

                if (costs[2] < results.get(2).getKey()) {
                    results.remove(2);
                    results.add(2, new MutablePair<>(costs[2], path_length));
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        results.stream().forEach(v -> System.out.println(v[0] + " " + v[1] + " " + v[2]));
        int r = 0;
        for (MutablePair<Double, Integer> p : results) {
            r += p.getValue();
        }
        return r / 3;
    }

    private long getrunningtime(File performance_log_file, String s, String d) {
        try {
            BufferedReader b = new BufferedReader(new FileReader(performance_log_file));
            String readLine = "";
            boolean candidateLogs = true;
            while ((readLine = b.readLine()) != null && candidateLogs) {
                String running_time_trim = readLine.substring(readLine.lastIndexOf(":") + 1).trim();

                if (readLine.contains("Query time : ")) {
//                    System.out.println(readLine);
                    String src = readLine.split(" ")[3].split("\\|")[1].split(">")[0];
                    String dest = readLine.split(" ")[3].split("\\|")[1].split(">")[1];
                    long gnn_running_time = Long.parseLong(running_time_trim);

                    if (s.equals(src) && dest.equals(d)) {
                        return gnn_running_time;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
