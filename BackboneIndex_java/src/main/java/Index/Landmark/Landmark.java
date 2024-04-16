package Index.Landmark;

import Neo4jTools.Line;
import Neo4jTools.Neo4jDB;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;
import utilities.ParsedOptions;
import utilities.myFileFilter;
import utilities.myLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Landmark {


    private String db_name;
    private int number_landmark;
    private ArrayList<Long> landmark_idx_list = new ArrayList<>();
    private boolean createNewLandmarks;
    /**
     * landmark nodes --> <dest nodes, <the value of shortest path from landmark nodes to dest nodes in each dimension>>
     **/
    public HashMap<Long, HashMap<Long, double[]>> landmark_index = new HashMap<>();
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String args[]){
        Landmark ldm = new Landmark(ParsedOptions.db_name, "LandMark_Building");
        ldm.readLandmarkIndex(ParsedOptions.number_landmark, ParsedOptions.landmark_idx_list, ParsedOptions.createNewLandmarks);
    }


    public Landmark(String db_name, String logger_name) {
        this.db_name = db_name;
        myLogger.configTheLogger(logger_name);
    }

    public Landmark(String db_name) {
        this.db_name = db_name;
    }

    public void readLandmarkIndex(int num_landmarks, ArrayList<Long> landmark_list_ids, boolean createNew) {
        LOGGER.info("The landmark index is building for graph : " + this.db_name);
        LOGGER.info("   number of landmarks is building : " + this.number_landmark);
        LOGGER.info("   list of landmarks is building : " + (this.landmark_idx_list == null ? "<>" : this.landmark_idx_list.toString()));
        LOGGER.info("   create new landmarks : " + this.createNewLandmarks);

        if (landmark_list_ids != null && landmark_list_ids.size() > num_landmarks) {
            num_landmarks = landmark_list_ids.size();
        }

        String landmark_index_folder_base = ParsedOptions.output_landmark_index_folder + "/";

        String landmark_index_folder = landmark_index_folder_base + this.db_name;
        File landmark_index_fl = new File(landmark_index_folder);
        LOGGER.info("Read the landmark index from the folder ====>> " + landmark_index_folder);

        boolean null_landmark_list = false;

        if (landmark_list_ids == null || landmark_list_ids.size() == 0) {
            null_landmark_list = true;
        }

        try {
            if (!landmark_index_fl.exists()) {
                FileUtils.forceMkdir(landmark_index_fl);
            }

            File[] idx_files = landmark_index_fl.listFiles(new myFileFilter());
            HashMap<Long, Boolean> readed_idx_file = new HashMap<>();
            HashMap<Long, File> lamdmark_idx_file_mapping = new HashMap<>();

            for (File idx_f : idx_files) {
                long landmark = Long.parseLong(idx_f.getName().substring(0, idx_f.getName().lastIndexOf(".")));
                //Read the specific landmark index file if the landmark nodes are given by landmark_list_ids
                if (!null_landmark_list && landmark_list_ids.contains(landmark)) {
                    BufferedReader b = new BufferedReader(new FileReader(idx_f));
                    String readLine = "";
                    HashMap<Long, double[]> landmark_contents = new HashMap<>();
                    while ((readLine = b.readLine()) != null) {
                        String[] infos = readLine.split(" ");
                        double[] costs = new double[infos.length - 1];

                        long target_node = Long.parseLong(infos[0]);
                        costs[0] = Double.parseDouble(infos[1]);
                        costs[1] = Double.parseDouble(infos[2]);
                        costs[2] = Double.parseDouble(infos[3]);

                        landmark_contents.put(target_node, costs);
                    }

                    this.landmark_index.put(landmark, landmark_contents);
                    readed_idx_file.put(landmark, true);
                    LOGGER.info("Finished the reading of landmark index for node [" + landmark + "]");
                } else {
                    readed_idx_file.put(landmark, false);
                }
                lamdmark_idx_file_mapping.put(landmark, idx_f);
            }

            //random read the landmark index
            if (!createNew && idx_files.length >= num_landmarks) {
                if (!null_landmark_list) {
                    ArrayList<Long> remained_landmarks = landmark_list_ids.stream().filter(node_id -> !landmark_index.containsKey(node_id)).collect(Collectors.toCollection(ArrayList::new));
                    buildLandmarkIndex(-1, remained_landmarks); //the remained_landmarks should not be empty
                }

                LOGGER.info("Begin to read the landmark index ........................................");
                while (this.landmark_index.size() < num_landmarks) {
                    Random r = new Random(System.currentTimeMillis());

                    //Keep the unread landmark node list, that is used to random pick
                    ArrayList<Long> unreaded_landmark = new ArrayList<>();
                    for (long landmark : readed_idx_file.keySet()) {
                        if (!readed_idx_file.get(landmark)) {
                            unreaded_landmark.add(landmark);
                        }
                    }

                    long landmark = unreaded_landmark.get(r.nextInt(unreaded_landmark.size()));
                    readed_idx_file.put(landmark, true); //mark this landmark is read. Don't read it again.
                    File ldm_file = lamdmark_idx_file_mapping.get(landmark);

                    BufferedReader b = new BufferedReader(new FileReader(ldm_file));
                    String readLine = "";
                    HashMap<Long, double[]> landmark_contents = new HashMap<>();
                    while ((readLine = b.readLine()) != null) {
                        String[] infos = readLine.split(" ");
                        double[] costs = new double[infos.length - 1];

                        long target_node = Long.parseLong(infos[0]);
                        costs[0] = Double.parseDouble(infos[1]);
                        costs[1] = Double.parseDouble(infos[2]);
                        costs[2] = Double.parseDouble(infos[3]);

                        landmark_contents.put(target_node, costs);
                    }

                    this.landmark_index.put(landmark, landmark_contents);
                    LOGGER.info("Finished the reading of landmark index for node [" + landmark + "]");
                }
                LOGGER.info("End the process of reading the landmark index ........................................");
            } else { //create the new landmark nodes
                buildLandmarkIndex(num_landmarks, landmark_list_ids);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLandmarkIndexToDisk(String idx_file_name, HashMap<Long, double[]> mapping) {
        FileWriter fileWriter = null;
        try {

            if (new File(idx_file_name).exists()) {
                new File(idx_file_name).delete();
            }

            fileWriter = new FileWriter(idx_file_name);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (Map.Entry<Long, double[]> landmark_element : mapping.entrySet()) {
                long dest_node_id = landmark_element.getKey();
                double[] costs = landmark_element.getValue();
                printWriter.println(dest_node_id + " " + costs[0] + " " + costs[1] + " " + costs[2]);

            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildLandmarkIndex(int num_landmarks, ArrayList<Long> landmark_list_ids) {
        String landmark_index_folder_base = ParsedOptions.output_landmark_index_folder + "/";
        String landmark_index_folder = landmark_index_folder_base + this.db_name;

        if (num_landmarks != -1) {
            this.landmark_index.clear();
        }

        Neo4jDB neo4j = new Neo4jDB(this.db_name);
        neo4j.startDB(true);
        GraphDatabaseService graphdb = neo4j.graphDB;
        LOGGER.info(neo4j.DB_PATH + "  number of nodes:" + neo4j.getNumberofNodes() + "   number of edges : " + neo4j.getNumberofEdges());

        try (Transaction tx = graphdb.beginTx()) {
            ArrayList<Node> nodelist = new ArrayList<>();

            ResourceIterable<Node> nodes_iterable = graphdb.getAllNodes();
            ResourceIterator<Node> nodes_iter = nodes_iterable.iterator();
            while (nodes_iter.hasNext()) {
                Node node = nodes_iter.next();
                nodelist.add(node);
            }

            ArrayList<Node> landmarks = getLandMarkNodeList(num_landmarks, landmark_list_ids, nodelist);
            for (Node lnode : landmarks) {
                HashMap<Long, double[]> index_from_landmark_to_dest = new HashMap<>();
                LOGGER.info("Build the index for the node " + lnode);
                int index = 0;

//                for (String property_name : Neo4jDB.propertiesName) {
//                    System.out.println("Attribute :  " + property_name);
//                }

                for (Node destination : nodelist) {
                    if ((++index) % 500 == 0) {
                        LOGGER.info(lnode + "    " + index + " ..............................");
                    }

                    int i = 0;
                    double[] min_costs = new double[3];
                    for (String property_name : Neo4jDB.propertiesName) {
                        PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.forTypeAndDirection(Line.Linked, Direction.BOTH), property_name);
                        WeightedPath paths = finder.findSinglePath(lnode, destination);
                        if (paths != null) {
                            min_costs[i] = paths.weight();
                            i++;
                        } else {
                            LOGGER.info("Can not find a shortest path from " + lnode + " to " + destination);
                            System.exit(0);
                        }
                    }

                    index_from_landmark_to_dest.put(destination.getId(), min_costs);
                }

                this.landmark_index.put(lnode.getId(), index_from_landmark_to_dest);

                HashMap<Long, double[]> mapping = landmark_index.get(lnode.getId());
                String idx_file_name = landmark_index_folder + "/" + lnode.getId() + ".idx";
                writeLandmarkIndexToDisk(idx_file_name, mapping);
                LOGGER.info("Finished the writing index to disk [" + lnode.getId() + "]    ");
            }

            tx.success();
        }

        neo4j.closeDB();
    }

    /**
     * Get the landmark node list,
     * 1) if the given list is empty, generate random node list with given number
     * 2) get the node objects list from given list
     *
     * @param num_landmarks
     * @param landmark_list_ids
     * @param nodelist
     * @return
     */
    private ArrayList<Node> getLandMarkNodeList(int num_landmarks, ArrayList<Long> landmark_list_ids, ArrayList<Node> nodelist) {
        ArrayList<Node> result_list = new ArrayList<>();


        HashMap<Long, Node> node_mapping = new HashMap<>();
        for (Node n : nodelist) {
            node_mapping.put(n.getId(), n);
        }

        if (landmark_list_ids == null || landmark_list_ids.size() == 0) {
            while (result_list.size() < num_landmarks) {
                Node landmarks_node = getRandomNodes(nodelist);
                if (!result_list.contains(landmarks_node)) {
                    result_list.add(landmarks_node);
                }
            }
        } else {
            for (long ldm_id : landmark_list_ids) {
                result_list.add(node_mapping.get(ldm_id));
            }
        }

        return result_list;
    }

    private <T> T getRandomNodes(ArrayList<T> nodelist) {
        Random r = new Random();
        int idx = r.nextInt(nodelist.size());
        return nodelist.get(idx);
    }
}
