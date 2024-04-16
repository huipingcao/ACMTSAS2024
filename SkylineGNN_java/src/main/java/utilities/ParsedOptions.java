package utilities;

import java.util.ArrayList;

public class ParsedOptions {

    public static String base_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data/";

// ======================================================================================================
    public static String db_name = "C9_NY_NONE_15K";
//    public static String db_name = "C9_NY_NONE_5K_Level3";
    public static String neo4jdbPath = base_path + "new_graphs/C9_NY_NONE_15K/db";
    public static String GraphInfoPath = base_path + "new_graphs/C9_NY_NONE_15K";
    public static String output_graphInfo_folder = base_path + "new_graphs/C9_NY_NONE_15K";
    public static String resultFolder = base_path + "new_results_bbs/C9_NY_NONE_15K_test_1000";
//    public static String resultFolder = base_path + "_prev/results_bbs/C9_NY_NONE_5K_TSP_Level0-6/" + db_name;
    public static String indexFolder = base_path + "BackBoneIndex/C9_NY_NONE_15K/";

    public static String output_landmark_index_folder = base_path + "landmarks/C9_NY";
    public static String compareResultFolder = base_path + "new_results_gnn_backbone";
    public static String gnn_model = "answered100_epoch300_query500_128_128_32_1_EmbedTrue_Transformer_ConLossTrue.mapping";
    public static String mapped_node_file = base_path + "new_mapped/C9_NY_NONE_5K/answered100/" + gnn_model;


// ======================================================================================================
//    public static String db_name = "C9_NY_NONE_5K_TSP";
//    public static String neo4jdbPath = base_path + "new_graphs/C9_NY_NONE_5K_TSP/db";
//    public static String GraphInfoPath = base_path + "new_graphs/C9_NY_NONE_5K_TSP";
//    public static String output_graphInfo_folder = base_path + "new_graphs/C9_NY_NONE_5K_TSP";
//    public static String resultFolder = base_path + "new_results_bbs/C9_NY_NONE_5K_test_1000";
//    public static String indexFolder = base_path + "BackBoneIndex/C9_NY_NONE_5K/";
//
//    public static String output_landmark_index_folder = base_path + "landmarks/C9_NY";
//    public static String compareResultFolder = base_path + "new_results_gnn_backbone";

// ======================================================================================================
//    public static String db_name = "C9_NY_NONE_5K_TSP_L1-6";
//    public static String neo4jdbPath = base_path + "new_graphs/C9_NY_NONE_5K_TSP_L1-6/db";
//    public static String GraphInfoPath = base_path + "new_graphs/C9_NY_NONE_5K_TSP_L1-6";
//    public static String output_graphInfo_folder = base_path + "new_graphs/C9_NY_NONE_5K_TSP_L1-6";
//    public static String resultFolder = base_path + "new_results_bbs/C9_NY_NONE_5K_test_1000";
//    public static String indexFolder = base_path + "BackBoneIndex/C9_NY_NONE_5K/";
//
//    public static String output_landmark_index_folder = base_path + "landmarks/C9_NY";
//    public static String compareResultFolder = base_path + "new_results_gnn_backbone";

// ======================================================================================================
//    public static String db_name = "L_CAL_NONE";
//    public static String neo4jdbPath = base_path + "new_graphs/L_CAL_NONE/db";
//    public static String GraphInfoPath = base_path + "new_graphs/L_CAL_NONE";
//    public static String output_graphInfo_folder = base_path + "new_graphs/L_CAL_NONE";
//    public static String resultFolder = base_path + "new_results_bbs/L_CAL_NONE_test_1000";
//    public static String indexFolder = base_path + "BackBoneIndex/L_CAL_NONE/";
//
//    public static String output_landmark_index_folder = base_path + "landmarks";
//    public static String compareResultFolder = base_path + "new_results_gnn_backbone";

// ======================================================================================================
//    public static String db_name = "L_CAL_NONE_TSP";
//    public static String neo4jdbPath = base_path + "new_graphs/L_CAL_NONE_TSP/db";
//    public static String GraphInfoPath = base_path + "new_graphs/L_CAL_NONE_TSP";
//    public static String output_graphInfo_folder = base_path + "new_graphs/L_CAL_NONE_TSP";
//    public static String resultFolder = base_path + "new_results_bbs/L_CAL_NONE_test_1000";
//    public static String indexFolder = base_path + "BackBoneIndex/L_CAL_NONE/";
//
//    public static String output_landmark_index_folder = base_path + "landmarks";
//    public static String compareResultFolder = base_path + "new_results_gnn_backbone";

// ======================================================================================================
    public static String sub_k = "5";
    public static String srcQueryPairFile = "C9_NY_NONE_5K_Level3_10000_query_pairs.txt";

    public static String queryPairFolder = base_path + "queries";
//    public static String logFolder = base_path + "new_logs/DTW/" + db_name + "/answered100";
    public static String logFolder = base_path + "new_logs/DTW/" + db_name;
    public static String propertyFile = base_path + "logging.properties";

    public static int number_landmark = 3;
    public static ArrayList<Long> landmark_idx_list = null;
    public static boolean createNewLandmarks = false;
    public static int numQuery = 3000;

    public static int number_sub_graphs = 1;

    public static long timeout=180000;

    public static final int cost_dimension = 3;
    public static boolean dtw_normal = true;

}
