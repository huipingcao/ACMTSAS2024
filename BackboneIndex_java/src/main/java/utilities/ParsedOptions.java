package utilities;

import java.util.ArrayList;

public class ParsedOptions {

    //Test environment
//    public static String db_name = "C9_NY_NONE_5K";
//    public static String sub_k = "5";
//    public static String neo4jdbPath = "/home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Neo4jDB";
//    public static String GraphInfoPath = "/home/gqxwolf/mydata/shared_git/BackboneIndex/Data/" + db_name;
//    public static String output_graphIndo_foler = "/home/gqxwolf/mydata/shared_git/BackboneIndex/Data/" + db_name + "_" + sub_k + "K"; //the place to store the nodes and edge informations
//    public static String indexFolder = "/home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Index/";
//    public static String logFolder = "/home/gqxwolf/mydata/shared_git/BackboneIndex/Data/logs";
//    public static String output_landmark_index_folder = "/home/gqxwolf/mydata/shared_git/BackboneIndex/Data/Index/landmarks";
//    public static String resultFolder = "/home/gqxwolf/mydata/shared_git/BackboneIndex/Data/results";
//    public static String propertyFile = "/home/gqxwolf/mydata/shared_git/BackboneIndex/Data/logging.properties";


//    public static String graph = "L_NA";
//    public static String db_name = "L_NA_NONE_Level5"; // 175563057 milliseconds
//    public static String sub_k = "5";
//    public static String base_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data";
//    public static String neo4jdbPath = base_path + "/" + graph + "/db";
////    public static String GraphInfoPath = base_path + "/" + graph + "/" + db_name;
//    public static String GraphInfoPath = base_path + "/" + graph + "/L_NA_NONE";
////    public static String output_graphIndo_foler = base_path + "/" + graph + "/" + db_name;
//    public static String output_graphIndo_foler = base_path + "/" + graph + "/L_NA_NONE";
////    public static String indexFolder = base_path + "/BackBoneIndex/" + db_name;
//    public static String indexFolder = base_path + "/BackBoneIndex/L_NA_NONE";
//
//    public static String output_landmark_index_folder = base_path + "/landmarks";
//    public static String resultFolder = base_path + "/results_bbs/" + db_name;


    public static String sub_k = "5";
    public static String db_name = "C9_NY_NONE_15K_Level2";
    public static String base_path = "/home/hchen/IntelliJProjects/java_SkylineGNN/Data";
    public static String neo4jdbPath = base_path + "/C9_NY" + "/db";
//    public static String GraphInfoPath = base_path + "/C9_NY/" + db_name;
    public static String GraphInfoPath = base_path + "/C9_NY/C9_NY_NONE/C9_NY_NONE_15K";
//    public static String output_graphIndo_foler = base_path + "/C9_NY/" + db_name;
    public static String output_graphIndo_foler = base_path + "/C9_NY/C9_NY_NONE/C9_NY_NONE_15K";
//    public static String indexFolder = base_path + "/BackBoneIndex/" + db_name;
    public static String indexFolder = base_path + "/BackBoneIndex/C9_NY_NONE_15K";
    public static String output_landmark_index_folder = base_path + "/landmarks/C9_NY";
    public static String resultFolder = base_path + "/results_bbs/C9_NY/" + db_name;


    public static String logFolder = base_path + "/logs";
    public static String propertyFile = base_path + "/logging.properties";
    public static String logProperties = base_path + "/logging.properties";

    public static int number_landmark = 3;
    public static ArrayList<Long> landmark_idx_list = null;
    public static boolean createNewLandmarks = true;
    public static int numQuery = 3000;
    public static Integer timeout = 180000;

    public static double percentage = 0.01;
    public static int min_size = 200;
    public static int degreeHandle = 0;
    public static final int cost_dimension = 3;
    public static String timestamp;
    public static double p_ind = 30;
    public static int msize = 30;  // "m_{min} in the paper, constraining the minimum number of nodes in a cluster"
    public static String clustering_method = "node";
    public static boolean dtw_normal = true;
    public static String method = "IndexBuilding";
    public static String info_type = "distribution";
    public static Boolean info_verb = false;
    public static String savedFolder = null;

}
