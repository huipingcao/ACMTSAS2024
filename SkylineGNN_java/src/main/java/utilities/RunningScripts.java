// https://lightrun.com/java/how-to-export-a-jar-from-intellij/

package utilities;

import Baseline.Baseline;
import Baseline.GenerateTrainingSet;
import Baseline.GenerateTrainingSetSubGraph;
import Comparison.DTWComparison;
import Neo4jTools.CreateDB;
import Neo4jTools.subGraphArea;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RunningScripts {
    private String method;

    public static void main(String args[]) throws IOException {
        RunningScripts r = new RunningScripts();
        if (r.readParameters(args)) {
            r.execute();
        }
    }

    private void execute() throws IOException {
        switch (this.method) {
            case "test":
                System.out.println("=======This is a test for RunningScripts===============");
            case "createDB":  // worked
                long start_time_db = System.currentTimeMillis();
                CreateDB c = new CreateDB();
                c.createChangeGraphDB();
                long end_time_db = System.currentTimeMillis();
                System.out.println("Total time spent:\t" + (end_time_db - start_time_db) + " milliseconds");
                break;
            case "GenerateSubGraph":  // worked
                long start_time_sub = System.currentTimeMillis();
                subGraphArea s = new subGraphArea();
                s.generateSubGraphs();
                long end_time_sub = System.currentTimeMillis();
                System.out.println("Total time spent:\t" + (end_time_sub - start_time_sub) + " milliseconds");
                break;
            case "GenerateQueryPairs": //created by ying  // worked
                // generating query pair bank - double size of needed # of queries
                long start_time_query = System.currentTimeMillis();
                Baseline bbs_query = new Baseline(ParsedOptions.db_name, ParsedOptions.numQuery, new ArrayList<>(), true);
                bbs_query.init_queries();
                long end_time_query = System.currentTimeMillis();
                System.out.println("Total time spent:\t" + (end_time_query - start_time_query) + " milliseconds");
                break;
            case "GenerateBaselineResults":  // worked   by 2022.11
                // generate results - half from src query pairs file, half from query pair bank
                long start_time_bbs = System.currentTimeMillis();
                GenerateTrainingSet g_single = new GenerateTrainingSet();
                g_single.generating();
                long end_time_bbs = System.currentTimeMillis();
                System.out.println("Total time spent:\t" + (end_time_bbs - start_time_bbs) + " milliseconds");
                break;
            case "GenerateBaselineResultsSubGraphs":
                GenerateTrainingSetSubGraph g = new GenerateTrainingSetSubGraph();
                g.generating();
                break;
            case "compareBBS":   // worked   by 2022.12
                long start_time_comparebbs = System.currentTimeMillis();
                Query q = new Query();
                q.query();
                long end_time_comparebbs = System.currentTimeMillis();
                System.out.println("Total time spent:\t" + (end_time_comparebbs - start_time_comparebbs) + " milliseconds");
                break;
            case "DTWComparison":    // worked   by 2023.1
                long start_time_DTW = System.currentTimeMillis();
                DTWComparison dtwComparison = new DTWComparison();
                dtwComparison.comparision();
                long end_time_DTW = System.currentTimeMillis();
                System.out.println("Total time spent:\t" + (end_time_DTW - start_time_DTW) + " milliseconds");
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
            options.addOption("indexFolder", "indexFolder", true, "the place where to store the index");

            options.addOption("sub_K", "subK", true, "number of nodes that the subgraph generated");
            options.addOption("outGraphInfo", "outGraphInfo", true, "the place where stores the output information of nodes and edges");
            options.addOption("num_subs", true, "Number of sub_graphs is generated");

            //BBS queries
            options.addOption("nLandMark", "number_andmark", true, "the number of the landmark");
            options.addOption("lLandMark", "list_landmarks", true, "the list of the landmark, split by comma. ");
            options.addOption("cLandMark", "create_new", false, "the flag of creating the new landmarks or read from the existed landmarks");
            options.addOption("landmarkIndexFolder", true, "the place where to store the landmark index for given level neo4j db");
            options.addOption("resultFolder", true, "the place stores the paths returned by BBS and BackBone Query");
            options.addOption("compareResultFolder", true, "the place stores the comparison result paths returned by BBS and BackBone Query");
            options.addOption("queryPairFolder", true, "the path stores the query pairs returned by random generation");
            options.addOption("srcQueryPairFile", true, "the file stores the source query pairs for bbs trainingSet generation");
            options.addOption("numQuery", true, "number of queries.");


            options.addOption("logFolder", true, "the folder to store the log files, log files start with the classname");

            options.addOption("h", "help", false, "print the help of this command");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = null;
            cmd = parser.parse(options, args);

            System.out.println("=============Args===============");
            String method_str = cmd.getOptionValue("m");
            System.out.println(method_str);

            String neo4jPath_str = cmd.getOptionValue("neo4jdb");
            String GraphInfo_str = cmd.getOptionValue("GraphInfo");
            String dbname = cmd.getOptionValue("dbname");

            //parameters for generation sub-graphs
            String sub_k_str = cmd.getOptionValue("sub_K");
            String num_subs_str = cmd.getOptionValue("num_subs");
            String out_graphinfor_str = cmd.getOptionValue("outGraphInfo");

            //BBS Baseline
            String nLandMark_str = cmd.getOptionValue("nLandMark");
            String lLandMark_str = cmd.getOptionValue("lLandMark");
            String numQuery_str = cmd.getOptionValue("numQuery");
            String landmark_index_folder_str = cmd.getOptionValue("landmarkIndexFolder");
            String resultFolder_str = cmd.getOptionValue("resultFolder");
            String compareResultFolder_str = cmd.getOptionValue("compareResultFolder");
            String queryPairFolder = cmd.getOptionValue("queryPairFolder");
            String srcQueryPairFile = cmd.getOptionValue("srcQueryPairFile");

            String logFile_str = cmd.getOptionValue("logFolder");
            String index_folder_str = cmd.getOptionValue("indexFolder");


            if (cmd.hasOption("h")) {
                return printHelper(options);
            } else {
                if (method_str == null) {
                    this.method = "createDB";
                } else if (method_str.equals("createDB") || method_str.equals("GenerateSubGraph")
                        || method_str.equals("GenerateBaselineResults") || method_str.equals("GenerateQueryPairs")
                        || method_str.equals("compareBBS") || method_str.equals("DTWComparison")) {
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

                if (num_subs_str != null) {
                    ParsedOptions.number_sub_graphs = Integer.parseInt(num_subs_str);
                }

                if (out_graphinfor_str != null) {
                    ParsedOptions.output_graphInfo_folder = out_graphinfor_str;
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
                if (compareResultFolder_str != null) {
                    ParsedOptions.compareResultFolder = compareResultFolder_str;
                }

                if (queryPairFolder != null) {
                    ParsedOptions.queryPairFolder = queryPairFolder;
                }

                if (srcQueryPairFile != null) {
                    ParsedOptions.srcQueryPairFile = srcQueryPairFile;
                }

                if (numQuery_str != null) {
                    ParsedOptions.numQuery = Integer.parseInt(numQuery_str);
                }

                if (index_folder_str != null) {
                    ParsedOptions.indexFolder = index_folder_str;
                }

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
