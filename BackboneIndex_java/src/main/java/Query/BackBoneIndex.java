package Query;

import Neo4jTools.Neo4jDB;
import utilities.ParsedOptions;
import utilities.idxFileFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class BackBoneIndex {
    String system_home_folder = System.getProperty("user.home");

    String db_name;
    String index_files_folder;

    boolean is_highest_graph_empty = false;
    int index_level = -1; //the highest index level on the disk
    int graph_level = -1; //the highest level graph after the index construction
    public String target_db_name = ""; //the target graph which is the final level of the construction, that is used to build
    public int target_idx_level = -1;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public BackBoneIndex(String db_name) {
        this.db_name = db_name;
        this.index_files_folder = ParsedOptions.indexFolder + "/" + db_name;
        LOGGER.info("Read the index from the folder " + this.index_files_folder);

        this.index_level = getlevel();
        this.graph_level = this.index_level + 1;

        this.target_idx_level = this.index_level;

        setHighestGraphEmpty();
        setHighestIndex();

        LOGGER.info("Highest graph level is " + this.graph_level + ", is empty :" + this.is_highest_graph_empty);
        LOGGER.info("Read the index from the folder of level : " + target_idx_level);
        LOGGER.info("Read the graph from the folder of level : " + target_db_name);
    }

    private void setHighestIndex() {
//        File highest_folder = new File(index_files_folder + "/level" + this.index_level);
//        System.out.println(highest_folder.listFiles().length);

        if (is_highest_graph_empty) {
            target_idx_level = this.index_level - 1;
        } else {
            File highest_folder = new File(index_files_folder + "/level" + this.index_level);
            if (highest_folder.listFiles().length == 0) {
                this.target_db_name = this.db_name + "_Level" + (graph_level - 1);
                this.target_idx_level = this.index_level - 1;
            } else {
                this.target_db_name = this.db_name + "_Level" + graph_level;
                this.target_idx_level = this.index_level;
            }
        }
    }

    private int getlevel() {
        int level = 0;
        File index_dir = new File(this.index_files_folder);
        for (File f : index_dir.listFiles(new levelFileNameFilter())) {
            String fname = f.getName();
            int c_level = Integer.parseInt(fname.substring(fname.indexOf("level") + 5));
            if (c_level > level) {
                level = c_level;
            }
        }
        return level;
    }

    public void setHighestGraphEmpty() {
        String highest_db_name = this.db_name + "_Level" + graph_level;
        Neo4jDB neo4j = new Neo4jDB(highest_db_name);
        neo4j.startDB(false);
        this.is_highest_graph_empty = neo4j.isGraphEmpty();
        neo4j.closeDB();

        if (is_highest_graph_empty) {
            this.target_db_name = this.db_name + "_Level" + (this.graph_level - 1);
        } else {
            this.target_db_name = highest_db_name;
        }
    }

    /**
     * Read the highway nodes list of source_node in given level
     *
     * @param level
     * @param source_node
     * @return the list of the highway node of source node in given level
     */
    public HashSet<Long> getHighwayNodeAtLevel(int level, long source_node) {
        HashMap<Long, ArrayList<double[]>> highwaynodes = readHighwayNodes(level, source_node);
        if (highwaynodes == null) {
            return null;
        } else {
            return new HashSet<>(highwaynodes.keySet());
        }
    }

    /**
     * @param highway_node the given highway node that is the highway of the source node
     * @param level        the given index level
     * @param source_node  the source node
     * @return the skyline costs from source node to the highway node in the given level
     */
    public ArrayList<double[]> readHighwaysInformation(long highway_node, int level, long source_node) {
        HashMap<Long, ArrayList<double[]>> highwaynodes = readHighwayNodes(level, source_node);
        if (highwaynodes == null) {
            return null;
        } else {
            ArrayList<double[]> skyline_costs = highwaynodes.get(highway_node);
            return skyline_costs;
        }
    }

    public HashMap<Long, ArrayList<double[]>> readHighwayNodes(int level, long source_node) {
        HashMap<Long, ArrayList<double[]>> highway_information = new HashMap<>();
        String idx_file = this.index_files_folder + "/level" + level + "/" + source_node + ".idx";
//        LOGGER.info("Get the highway nodes and costs of the Node ["+source_node+"]  by reading the index file from "+idx_file);

        //if there is no source_node.idx in this level, which means there is no highway information of the source node in this level
        if (!new File(idx_file).exists()) {
            return null;
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(idx_file));
            String line;
            while ((line = reader.readLine()) != null) {
                long highway_node_id = Long.parseLong(line.split(" ")[0]);
                double[] costs = new double[ParsedOptions.cost_dimension];

                costs[0] = Double.parseDouble(line.split(" ")[1]);
                costs[1] = Double.parseDouble(line.split(" ")[2]);
                costs[2] = Double.parseDouble(line.split(" ")[3]);

                ArrayList<double[]> costs_list;
                if (highway_information.containsKey(highway_node_id)) {
                    costs_list = highway_information.get(highway_node_id);
                } else {
                    costs_list = new ArrayList<>();
                }
                costs_list.add(costs);
                highway_information.put(highway_node_id, costs_list);

            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return highway_information;
    }

    public HashSet<Long> readNodeListAtLevel(int level) {
        HashSet<Long> nodeList = new HashSet<>();
        File idx_folder = new File(this.index_files_folder + "/level" + level + "/");
        for (File f : idx_folder.listFiles(new idxFileFilter())) {
            long node_id = Long.parseLong(f.getName().replace(".idx", ""));
//            System.out.println(f.getName()+"  ==> "+ node_id);
            nodeList.add(node_id);
        }
        return nodeList;
    }

    public static class levelFileNameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().startsWith("level");
        }

    }

}
