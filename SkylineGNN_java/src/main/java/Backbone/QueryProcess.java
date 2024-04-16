package Backbone;

import utilities.ParsedOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class QueryProcess {
    public BackBoneIndex bk_idx;
    bbs b;

    HashMap<Long, ArrayList<backbonePath>> source_to_highway_results = new HashMap<>(); //the temporary results from source node to highways
    /**
     * The temporary results from destination node to highways
     * highway node's of dest_node, the skyline backbone paths from dest_nodes to highway
     */

    HashMap<Long, ArrayList<backbonePath>> destination_to_highway_results = new HashMap<>();


    public ArrayList<backbonePath> query(long src_id, long dest_id, int index) {
        long rt_start_query = System.currentTimeMillis();
        ArrayList<backbonePath> result = new ArrayList<>();
        findCommonLayer(src_id, dest_id);
        findTheIndexInTheLayerIndex(src_id, dest_id, result);
        boolean execute_result = findTheIndexAtThehighestLevel(src_id, dest_id, result);
        System.out.println(index+": backbone|" + src_id + ">" + dest_id + "|" + "number of results sets : " + result.size() + "   overall running time : " + (execute_result ? (System.currentTimeMillis() - rt_start_query) : 9999999) + " ms ");
        return result;

    }

    private boolean findTheIndexAtThehighestLevel(long src_id, long dest_id, ArrayList<backbonePath> result) {

        HashMap<Long, ArrayList<backbonePath>> all_possible_dest_node_with_skypaths = filterPossibleNodeInList(destination_to_highway_results, b.node_list);
        HashMap<Long, ArrayList<backbonePath>> source_list = filterPossibleNodeInList(source_to_highway_results, b.node_list);


        long bbs_rt = System.currentTimeMillis();
        boolean execute_result = b.landmark_bbs(src_id, dest_id, source_list, all_possible_dest_node_with_skypaths, result);
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
    }

    public static void main(String args[]){
        QueryProcess q = new QueryProcess(ParsedOptions.db_name,3);
    }

    private void findCommonLayer(long source_node, long destination_node) {
        for (int l = 0; l <= bk_idx.target_idx_level; l++) {
            HashSet<Long> src_highways = bk_idx.getHighwayNodeAtLevel(l, source_node);
            HashSet<Long> dest_highways = bk_idx.getHighwayNodeAtLevel(l, destination_node);
            if (src_highways != null && dest_highways != null) {
            } else {
                boolean a = src_highways == null, b = dest_highways == null;
            }
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

        //the destination to itself
        backbonePath destDummyResult = new backbonePath(destination_node);
        ArrayList<backbonePath> temp_dest_list = new ArrayList<>();
        temp_dest_list.add(destDummyResult);
        destination_to_highway_results.put(destination_node, temp_dest_list);

//        HashSet<Long> needs_to_add_to_source = new HashSet<>();
//        HashSet<Long> needs_to_add_to_destination = new HashSet<>();


        for (int l = 0; l <= bk_idx.target_idx_level; l++) {

            HashSet<Long> shList = new HashSet<>(source_to_highway_results.keySet());

            for (long s_id : shList) {
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
                }
            }


            if (result.size() != 0) {
//                printResult(result);
            }
        }


        for (int l = 0; l <= bk_idx.target_idx_level; l++) {

            HashSet<Long> dhList = new HashSet<>(destination_to_highway_results.keySet());
            for (long d_id : dhList) {
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

            if(result_file.exists()){
                result_file.delete();
            }


            if (!result_file.getParentFile().exists()) {
                result_file.getParentFile().mkdirs();
            }

            FileWriter myWriter = new FileWriter(path_output, true);
            for (backbonePath p : results) {
                myWriter.write(p + " \n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}


