package utilities;

import Comparison.BaselineMethods.GenerateBBS;
import Comparison.BaselineMethods.GenerateBackbone;
import Comparison.Compare.compareBBSBackbone;
import Comparison.DTWComparison;
import Comparison.DTWComparisonGNN;
import Comparison.DTWComparisonNew;
import Index.BackboneIndex;
import Index.Landmark.Landmark;
import Neo4jTools.CreateDB;
import Neo4jTools.subGraphArea;
import org.apache.commons.cli.*;

import java.io.File;

public class RunningScripts {
    private String method;

    public static void main(String args[]) {
        RunningScripts r = new RunningScripts();
        if (r.readParameters(args)) {
            r.execute();
        }
    }

    private void execute() {
        switch (this.method) {
            case "createDB":
                CreateDB c = new CreateDB();
                c.createChangeGraphDB();
                break;
            case "GenerateSubGraph":
                subGraphArea s = new subGraphArea();
                s.selectSubGraph();
                break;
            case "IndexBuilding":
                long start_index_time = System.currentTimeMillis();
                BackboneIndex index = new BackboneIndex();
                index.build();
                long end_index_time = System.currentTimeMillis();
                System.out.println("Total time spent:\t" + (end_index_time - start_index_time) + " milliseconds");
                break;
            case "BuildLandMark":
                long start_landmark_time = System.currentTimeMillis();
                Landmark ldm = new Landmark(ParsedOptions.db_name, "LandMark_Building");
                ldm.readLandmarkIndex(ParsedOptions.number_landmark, ParsedOptions.landmark_idx_list, ParsedOptions.createNewLandmarks);
                long end_landmark_time = System.currentTimeMillis();
                System.out.println("Total time spent:\t" + (end_landmark_time - start_landmark_time) + " milliseconds");
                break;
            case "GenerateBBSResults":
                GenerateBBS gbbs = new GenerateBBS();
                gbbs.generating();
                break;
            case "GenerateBackboneResults":
                GenerateBackbone gbackbone = new GenerateBackbone();
                gbackbone.generating();
                break;
            case "Comparison":
                compareBBSBackbone comparison = new compareBBSBackbone();
                comparison.compare();
                break;
            case "DTWComparison":
                DTWComparison dtwComparison = new DTWComparison();
                dtwComparison.comparision();
                break;
            case "DTWComparisonGNN":
                DTWComparisonGNN dtwComparisonGNN = new DTWComparisonGNN();
                dtwComparisonGNN.comparision();
                break;
            case "DTWComparisonNew":
                DTWComparisonNew newdtw = new DTWComparisonNew();
                newdtw.comparision();
                break;
            case "GraphInformation":
                GraphInformation gi = new GraphInformation();
                if (ParsedOptions.info_type.equals("distribution")) {
                    System.out.println("get the information of the degree distribution");
                    gi.getDistributionOfDegree();
                } else if (ParsedOptions.info_type.equals("pair")) {
                    System.out.println("get the information of the degree pair");
                    gi.getDegreePairs();
                } else if (ParsedOptions.info_type.equals("twohop")) {
                    gi.getCoefficientDistribution(0, ParsedOptions.info_verb);
                } else if (ParsedOptions.info_type.equals("coef")) {
                    gi.getCoefficientDistribution(1, ParsedOptions.info_verb);
                } else {
                    System.out.println("Wrong Information Type");
                }
                break;
        }
    }

    private boolean readParameters(String[] args) {
        Options options = new Options();
        try {
            options.addOption("m", "method", true, "methods to execute, the default value is 'exact_improved'.");

            options.addOption("neo4jdb", "neo4jPath", true, "the place where stores the neo4j DB files");
            options.addOption("GraphInfo", "GraphInfo", true, "the place where stores the information of nodes and edges");
            options.addOption("dbname", "dbname", true, "name of the neo4j db ");
            options.addOption("sub_K", "subK", true, "number of nodes that the subgraph generated");
            options.addOption("outGraphInfo", "outGraphInfo", true, "the place where stores the output information of nodes and edges");
            options.addOption("indexFolder", "indexFolder", true, "the place where to store the index");
            options.addOption("savedFolder", "savedFolder", true, "the place to save the sub-graph folders");
            options.addOption("min_size", "min_size", true, "size of the cluster");
            options.addOption("percentage", true, "percentage of the edges must be removed from each level");
            options.addOption("degreeHandle", true, "two degree edges handling, [none,each,normal], default:normal");
            options.addOption("logFolder", true, "the folder to store the log files, log files start with the classname");
            options.addOption("pind", true, "p_index for the noise threshold");
            options.addOption("msize", true, "number of small nodes to merge");
            options.addOption("pmethod", true, "the method to partition the graphs");

            options.addOption("h", "help", false, "print the help of this command");

            //landmark parameters
            options.addOption("nLandMark", "number_andmark", true, "the number of the landmark");
            options.addOption("lLandMark", "list_landmarks", true, "the list of the landmark, split by comma. ");
            options.addOption("cLandMark", "create_new", false, "the flag of creating the new landmarks or read from the existed landmarks");
            options.addOption("landmarkIndexFolder", true, "the place where to store the landmark index for given level neo4j db");

            //Comparison
            options.addOption("numQuery", true, "number of queries.");
            options.addOption("resultFolder", true, "the place stores the paths returned by BBS and BackBone Query");
            options.addOption("timeout", true, "the timeout of the Baseline query in ms");

            //DTWComparison
            options.addOption("timestamp", true, "data time of the log file and results returned by bbs and backbone");
            options.addOption("dtwnormal", true, "normalization for cosine similarity");

            //GraphInformation
            options.addOption("infotype", true, "the information type: distribution, pair, twohop, coef");
            options.addOption("infoverb", true, "display the information of the coefficient in ascending order");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = null;
            cmd = parser.parse(options, args);

            String method_str = cmd.getOptionValue("m");

            String neo4jPath_str = cmd.getOptionValue("neo4jdb");
            String GraphInfo_str = cmd.getOptionValue("GraphInfo");
            String saveInfo_str = cmd.getOptionValue("savedFolder");
            String out_graphinfor_str = cmd.getOptionValue("outGraphInfo");
            String dbname = cmd.getOptionValue("dbname");
            String sub_k_str = cmd.getOptionValue("sub_K");
            String min_size_str = cmd.getOptionValue("min_size");
            String percentage_str = cmd.getOptionValue("percentage");
            String index_folder_str = cmd.getOptionValue("indexFolder");
            String degreeHandle_str = cmd.getOptionValue("degreeHandle");
            String logFile_str = cmd.getOptionValue("logFolder");
            String landmark_index_folder_str = cmd.getOptionValue("landmarkIndexFolder");

            String nLandMark_str = cmd.getOptionValue("nLandMark");
            String lLandMark_str = cmd.getOptionValue("lLandMark");

            String numQuery_str = cmd.getOptionValue("numQuery");
            String resultFolder_str = cmd.getOptionValue("resultFolder");
            String timeout_str = cmd.getOptionValue("timeout");

            String timestamp_str = cmd.getOptionValue("timestamp");
            String dtwnoraml_str = cmd.getOptionValue("dtwnormal");

            String pind_str = cmd.getOptionValue("pind");
            String mszie_str = cmd.getOptionValue("msize");
            String partition_method_str = cmd.getOptionValue("pmethod");

            String info_type_str = cmd.getOptionValue("infotype");
            String info_verb_str = cmd.getOptionValue("infoverb");

            if (cmd.hasOption("h")) {
                return printHelper(options);
            } else {
                if (method_str == null) {
                    this.method = "IndexBuilding";
                } else if (method_str.equals("createDB") || method_str.equals("GenerateSubGraph") || method_str.equals("IndexBuilding") || method_str.equals("BuildLandMark")
                        || method_str.equals("Comparison") || method_str.equals("DTWComparison") || method_str.equals("GraphInformation") || method_str.equals("GenerateBBSResults")
                        || method_str.equals("GenerateBackboneResults") || method_str.equals("DTWComparisonGNN") || method_str.equals("DTWComparisonNew")) {
                    this.method = method_str;
                } else {
                    return printHelper(options);
                }

                if (neo4jPath_str != null) {
                    ParsedOptions.neo4jdbPath = neo4jPath_str;
                }

                if (GraphInfo_str != null) {
                    ParsedOptions.GraphInfoPath = GraphInfo_str;
                }

                if (dbname != null) {
                    ParsedOptions.db_name = dbname;
                }

                if (sub_k_str != null) {
                    ParsedOptions.sub_k = sub_k_str;
                }

                if (out_graphinfor_str != null) {
                    ParsedOptions.output_graphIndo_foler = out_graphinfor_str;
                }

                if (saveInfo_str != null) {
                    ParsedOptions.savedFolder = saveInfo_str;
                }

                if (min_size_str != null) {
                    ParsedOptions.min_size = Integer.parseInt(min_size_str);
                    ParsedOptions.min_size = Integer.parseInt(min_size_str);
                }

                if (percentage_str != null) {
                    ParsedOptions.percentage = Double.parseDouble(percentage_str);
                }

                if (index_folder_str != null) {
                    ParsedOptions.indexFolder = index_folder_str;
                }

                if (degreeHandle_str != null) {
                    switch (degreeHandle_str) {
                        case "none":
                            ParsedOptions.degreeHandle = 2;
                            break;
                        case "each":
                            ParsedOptions.degreeHandle = 1;
                            break;
                        case "normal":
                            ParsedOptions.degreeHandle = 0;
                            break;
                    }
                }

                if (logFile_str != null) {
                    ParsedOptions.logFolder = logFile_str;
                    File folder_f = new File(ParsedOptions.logFolder);
                    if (!folder_f.exists()) {
                        folder_f.mkdirs();
                    }
                }


                if (nLandMark_str != null) {
                    ParsedOptions.number_landmark = Integer.parseInt(nLandMark_str);
                }

                if (lLandMark_str != null) {
                    String[] str_lists = lLandMark_str.split(",");
                    for (String str_idx_id : str_lists) {
                        ParsedOptions.landmark_idx_list.add(Long.parseLong(str_idx_id));
                    }
                }

                if (!options.hasOption("cLandMark")) {
                    ParsedOptions.createNewLandmarks = false;
                } else {
                    ParsedOptions.createNewLandmarks = true;
                }

                if (landmark_index_folder_str != null) {
                    ParsedOptions.output_landmark_index_folder = landmark_index_folder_str;
                }

                if (numQuery_str != null) {
                    ParsedOptions.numQuery = Integer.parseInt(numQuery_str);
                }

                if (resultFolder_str != null) {
                    ParsedOptions.resultFolder = resultFolder_str;
                }

                if (timeout_str != null) {
                    ParsedOptions.timeout = Integer.valueOf(timeout_str);
                }

                if (timestamp_str != null) {
                    ParsedOptions.timestamp = timestamp_str;
                }

                if (dtwnoraml_str != null) {
                    ParsedOptions.dtw_normal = Boolean.valueOf(dtwnoraml_str);
                }

                if (pind_str != null) {
                    ParsedOptions.p_ind = Double.parseDouble(pind_str);
                }

                if (mszie_str != null) {
                    ParsedOptions.msize = Integer.parseInt(mszie_str);
                }

                if (partition_method_str != null) {
                    ParsedOptions.clustering_method = partition_method_str;
                }

                if (info_type_str != null) {
                    ParsedOptions.info_type = info_type_str;
                }

                if (info_verb_str != null) {
                    ParsedOptions.info_verb = Boolean.valueOf(info_verb_str);
                }

                System.out.println(ParsedOptions.p_ind + "    " + ParsedOptions.msize + "    " + ParsedOptions.clustering_method+"  "+ParsedOptions.degreeHandle);
//                System.exit(0);
            }
        } catch (ParseException | NumberFormatException e) {
            e.printStackTrace();
            return printHelper(options);
        }

        return true;
    }

    private boolean printHelper(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        String header = "Run the code of BackboneIndex :";
        formatter.printHelp("java -jar BackboneIndex.jar", header, options, "", false);
        return false;
    }
}
