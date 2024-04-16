package Query;

import utilities.ParsedOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class QueryProcess {
    public BackBoneIndex bk_idx;
    bbs b;

    HashMap<Long, ArrayList<backbonePath>> source_to_highway_results = new HashMap<>(); //the temporary results from source node to highways
    /**
     * The temporary results from destination node to highways
     * highway node's of dest_node, the skyline backbone paths from dest_nodes to highway
     */

    HashMap<Long, ArrayList<backbonePath>> destination_to_highway_results = new HashMap<>();

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public ArrayList<backbonePath> query(long src_id, long dest_id) {
        long rt_start_query = System.currentTimeMillis();
        ArrayList<backbonePath> result = new ArrayList<>();
        LOGGER.info("=================================================================================");
        findCommonLayer(src_id, dest_id);
        LOGGER.info("=================================================================================");
        findTheIndexInTheLayerIndex(src_id, dest_id, result);
        LOGGER.info("=================================================================================");
        LOGGER.info("number of results sets : " + result.size() + "   running time nutil finding results in each level: " + (System.currentTimeMillis() - rt_start_query) + " ms ");
        LOGGER.info("=================================================================================");
        boolean execute_result = findTheIndexAtThehighestLevel(src_id, dest_id, result);
        LOGGER.info("backbone|" + src_id + ">" + dest_id + "|" + "number of results sets : " + result.size() + "   overall running time : " + (execute_result ? (System.currentTimeMillis() - rt_start_query) : 9999999) + " ms ");
        return result;

    }

    private boolean findTheIndexAtThehighestLevel(long src_id, long dest_id, ArrayList<backbonePath> result) {

        HashMap<Long, ArrayList<backbonePath>> all_possible_dest_node_with_skypaths = filterPossibleNodeInList(destination_to_highway_results, b.node_list);
        HashMap<Long, ArrayList<backbonePath>> source_list = filterPossibleNodeInList(source_to_highway_results, b.node_list);

        LOGGER.info("size of source_to_highway_results: " + source_to_highway_results.size() + "   size of destination_to_highway_results:" + destination_to_highway_results.size());
        LOGGER.info("size of source list :" + source_list.size() + "   " + "size of possible destination:" + all_possible_dest_node_with_skypaths.size());

        long bbs_rt = System.currentTimeMillis();
        boolean execute_result = b.landmark_bbs(src_id, dest_id, source_list, all_possible_dest_node_with_skypaths, result);
        LOGGER.info("number of results sets : " + result.size() + "   running time of finding results at highest level: " + (execute_result ? (System.currentTimeMillis() - bbs_rt) : 9999999) + " ms ");
        LOGGER.info("================================================================================");
        return execute_result;
    }

    private HashMap<Long, ArrayList<backbonePath>> filterPossibleNodeInList(HashMap<Long, ArrayList<backbonePath>> original_list, HashSet<Long> node_list) {
        HashMap<Long, ArrayList<backbonePath>> result = new HashMap<>();
        for (Map.Entry<Long, ArrayList<backbonePath>> e : original_list.entrySet()) {
            if (node_list.contains(e.getKey())) {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    public QueryProcess(String db_name, int number_landmark) {
        bk_idx = new BackBoneIndex(db_name);
        b = new bbs(bk_idx.target_db_name);
        b.ldm_idx.readLandmarkIndex(number_landmark, null, false);
        System.out.println(bk_idx.target_db_name);
        LOGGER.info("Finish the initialization ......................................................");
        LOGGER.info("--------------------------------------------------------------------------------");
    }

    public static void main(String args[]){
        QueryProcess q = new QueryProcess(ParsedOptions.db_name,3);
    }

    private void findCommonLayer(long source_node, long destination_node) {
        for (int l = 0; l <= bk_idx.target_idx_level; l++) {
            HashSet<Long> src_highways = bk_idx.getHighwayNodeAtLevel(l, source_node);
            HashSet<Long> dest_highways = bk_idx.getHighwayNodeAtLevel(l, destination_node);
            LOGGER.info("finding the common highway of source node and destination at level " + l);
            if (src_highways != null && dest_highways != null) {
                LOGGER.info("# of common highways is " + findCommandHighways(src_highways, dest_highways).size());
            } else {
                boolean a = src_highways == null, b = dest_highways == null;
                LOGGER.info("there is no highways of " + (a ? "    src node " : "") + (b ? " dest node" : ""));
            }
            LOGGER.info("Finish the finding of command highway of the source node and the destination node at each level");
            LOGGER.info("=======================================================");
        }
    }

    private HashSet<Long> findCommandHighways(Set<Long> src_set, Set<Long> dest_set) {
        HashSet<Long> commonset = new HashSet<>();
        for (long s_element : src_set) {
            if (dest_set.contains(s_element)) {
                commonset.add(s_element);
            }
        }
        return commonset;
    }

    private void findTheIndexInTheLayerIndex(long source_node, long destination_node, ArrayList<backbonePath> result) {

        source_to_highway_results.clear();
        destination_to_highway_results.clear();

        backbonePath sourceDummyResult = new backbonePath(source_node);
        ArrayList<backbonePath> temp_src_list = new ArrayList<>();
        temp_src_list.add(sourceDummyResult);
        source_to_highway_results.put(source_node, temp_src_list);

        //the destination to it self
        backbonePath destDummyResult = new backbonePath(destination_node);
        ArrayList<backbonePath> temp_dest_list = new ArrayList<>();
        temp_dest_list.add(destDummyResult);
        destination_to_highway_results.put(destination_node, temp_dest_list);

//        HashSet<Long> needs_to_add_to_source = new HashSet<>();
//        HashSet<Long> needs_to_add_to_destination = new HashSet<>();


        for (int l = 0; l <= bk_idx.target_idx_level; l++) {
            LOGGER.info("Find the index information at level " + l);
            LOGGER.info("==================================================================================");

            HashSet<Long> shList = new HashSet<>(source_to_highway_results.keySet());

            for (long s_id : shList) {
                LOGGER.info("find highway node from the node sid :" + s_id);
                HashMap<Long, ArrayList<double[]>> highwaysOfsrcNode = bk_idx.readHighwayNodes(l, s_id); //get highways of s_id
                if (highwaysOfsrcNode != null) {
                    for (long h_node : highwaysOfsrcNode.keySet()) {//h_node is highway node of the sid, it's the source node to the next level
                        ArrayList<double[]> cost_from_src_to_highway = highwaysOfsrcNode.get(h_node);
                        if (cost_from_src_to_highway != null || !cost_from_src_to_highway.isEmpty()) {
                            ArrayList<backbonePath> bps_src_to_sid = source_to_highway_results.get(s_id); // the backbone paths from source node to s_id;
                            if (h_node == destination_node) {
                                for (backbonePath old_path : bps_src_to_sid) {
                                    for (double[] costs : cost_from_src_to_highway) {
                                        backbonePath new_bp = new backbonePath(h_node, costs, old_path); //the new path from the sid->old_highway->new_highway
                                        addToSkyline(result, new_bp);
                                    }
                                }
                            } else {
                                for (backbonePath old_path : bps_src_to_sid) {
                                    for (double[] costs : cost_from_src_to_highway) {
                                        backbonePath new_bp = new backbonePath(h_node, costs, old_path); //the new path from the sid->old_highway->new_highway
                                        if (!dominatedByResult(new_bp, result) && !new_bp.hasCycle) {
                                            boolean flag = addToResultSet(new_bp, source_to_highway_results);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LOGGER.info("        There is no index for the " + s_id + " at level " + l);
                }
            }


            if (result.size() != 0) {
                LOGGER.info("the result at level" + l + ":");
//                printResult(result);
            }
            LOGGER.info("size of the the source highway skyline path information is " + source_to_highway_results.size());
        }

        LOGGER.info("dealing the downwards query ~~~~~~~~~~~~~~~~~~~~~~");

        for (int l = 0; l <= bk_idx.target_idx_level; l++) {
            LOGGER.info("Find the index information at level " + l);
            LOGGER.info("==================================================================================");

            HashSet<Long> dhList = new HashSet<>(destination_to_highway_results.keySet());
            for (long d_id : dhList) {
                LOGGER.info("find highway node from the node did :" + d_id);
                HashMap<Long, ArrayList<double[]>> highwaysOfDestNode = bk_idx.readHighwayNodes(l, d_id);//get highways of did
                if (highwaysOfDestNode != null) {
//                    highwaysOfDestNode.forEach((v, K) -> LOGGER.info("            highway node of " + d_id + " : " + v));
                    for (long h_node : highwaysOfDestNode.keySet()) {//h_node is highway node of the did, it's the destination node to the next level
                        ArrayList<double[]> cost_from_dest_to_highway = highwaysOfDestNode.get(h_node); //costs from did to h_node

                        if (cost_from_dest_to_highway != null || !cost_from_dest_to_highway.isEmpty()) {
                            //the backbone paths from destination to did
                            ArrayList<backbonePath> bps_dest_to_did = destination_to_highway_results.get(d_id);
                            if (h_node == source_node) {
                                for (backbonePath old_path : bps_dest_to_did) {
                                    for (double[] costs : cost_from_dest_to_highway) {
                                        backbonePath new_bp = new backbonePath(h_node, costs, old_path); //the new path from the did->old_highway->destination (the highway node)
                                        addToSkyline(result, new_bp);
                                    }
                                }
                            } else if (source_to_highway_results.containsKey(h_node)) {
                                //Todo: build the final results and add to skyline result set
                                ArrayList<backbonePath> bps_src_to_h_node = source_to_highway_results.get(h_node);
                                for (backbonePath old_path : bps_dest_to_did) {
                                    for (backbonePath src_path : bps_src_to_h_node) {
                                        for (double[] costs : cost_from_dest_to_highway) {
                                            backbonePath new_result_bp = new backbonePath(src_path, old_path, costs);
                                            addToSkyline(result, new_result_bp);
                                        }
                                    }

                                    for (double[] costs : cost_from_dest_to_highway) {
                                        backbonePath new_bp = new backbonePath(h_node, costs, old_path); //the new path from the destination->old_highway->new_highway
                                        if (!dominatedByResult(new_bp, result) && !new_bp.hasCycle) {
                                            boolean flag = addToResultSet(new_bp, destination_to_highway_results);
                                        }
                                    }
                                }
                            } else {
                                for (backbonePath old_path : bps_dest_to_did) {
                                    for (double[] costs : cost_from_dest_to_highway) {
                                        backbonePath new_bp = new backbonePath(h_node, costs, old_path); //the new path from the destination->old_highway->new_highway
                                        if (!dominatedByResult(new_bp, result) && !new_bp.hasCycle) {
                                            boolean flag = addToResultSet(new_bp, destination_to_highway_results);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public boolean addToSkyline(ArrayList<backbonePath> bp_list, backbonePath bp) {
        int i = 0;

        if (bp_list.isEmpty()) {
            bp_list.add(bp);
            return true;
        } else {
            boolean can_insert_np = true;
            for (; i < bp_list.size(); ) {
                if (checkDominated(bp_list.get(i).costs, bp.costs)) {
                    can_insert_np = false;
                    break;
                } else {
                    if (checkDominated(bp.costs, bp_list.get(i).costs)) {
                        bp_list.remove(i);
                    } else {
                        i++;
                    }
                }
            }
            if (can_insert_np) {
                bp_list.add(bp);
                return true;
            }
        }
        return false;
    }

    private boolean checkDominated(double[] costs, double[] estimatedCosts) {
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] > estimatedCosts[i]) {
                return false;
            }
        }

        return true;
    }

    private boolean dominatedByResult(backbonePath np, ArrayList<backbonePath> result) {
        for (backbonePath rp : result) {
            if (checkDominated(rp.costs, np.costs)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Add the new backbone path new_bp to the skyline backbone path set that is from source_node to end node (highway node) of the new_bp
     *
     * @param new_bp
     * @param highway_results
     * @return
     */
    private boolean addToResultSet(backbonePath new_bp, HashMap<Long, ArrayList<backbonePath>> highway_results) {
        long h_node = new_bp.destination;// h_node is the highway node of next layer.
        ArrayList<backbonePath> h_list = highway_results.get(h_node); //the list of backbone paths from source node to highways.
        if (h_list != null) {
            if (addToSkyline(h_list, new_bp)) { // h_list is updated during the process
//                highway_results.put(h_node, h_list);
                return true;
            } else {
                return false;
            }
        } else {
            h_list = new ArrayList<>();
            h_list.add(new_bp);
            highway_results.put(h_node, h_list);
            return true;
        }
    }

    public void savePathsInformation(ArrayList<backbonePath> results, String path_output) {
        try {
            File result_file = new File(path_output);
            if (!result_file.getParentFile().exists()) {
                result_file.getParentFile().mkdirs();
            }

            FileWriter myWriter = new FileWriter(path_output, true);
            for (backbonePath p : results) {
                myWriter.write(p + " \n");
            }
            myWriter.close();
            LOGGER.info("Successfully wrote to the file. " + path_output);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
