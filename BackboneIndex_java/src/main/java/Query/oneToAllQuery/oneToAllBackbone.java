package Query.oneToAllQuery;

import Comparison.Compare.performanceResult;
import Query.BackBoneIndex;
import Query.backbonePath;
import Query.bbs;
import Query.myNode;
import utilities.ParsedOptions;
import utilities.myLogger;

import java.util.*;
import java.util.logging.Logger;

public class oneToAllBackbone {
    public BackBoneIndex bk_idx;
    public bbs b;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private String db_name;
    public performanceResult p_monitor;

    HashMap<Long, ArrayList<backbonePath>> source_to_highway_results = new HashMap<>(); //the temporary results from source node to highways
    HashMap<Long, HashMap<Long, ArrayList<backbonePath>>> destination_to_highway_results = new HashMap<>();
    public HashMap<Long, ArrayList<backbonePath>> results = new HashMap<>();

    public oneToAllBackbone(String db_name) {
        results.clear();
        p_monitor = new performanceResult();

        this.db_name = db_name;
        Date date = new Date();
        long timestamp = date.getTime();
        myLogger.configTheLogger("oneToAll_Backbone_" + db_name + "_" + ParsedOptions.numQuery, timestamp);

        bk_idx = new BackBoneIndex(db_name);
        b = new bbs(bk_idx.target_db_name);
        LOGGER.info("Finish the initialization ......................................................");
        LOGGER.info("--------------------------------------------------------------------------------");
    }

    public HashMap<Long, ArrayList<backbonePath>> query(long sid) {
        results.clear();
        long start_rt = System.currentTimeMillis();
        findTheIndexInTheLayerIndexUpwards(sid);
        findTheindexInTheLayerFromOtherNodes(sid);
        LOGGER.info("result size :" + results.size());
        LOGGER.info("source to highway result size :" + source_to_highway_results.size());
        LOGGER.info("dest to highway result size :" + destination_to_highway_results.size());
        findAtTheHighestLevel(sid);
        p_monitor.runningtime = System.currentTimeMillis() - start_rt;
        LOGGER.info("Overall query time is " + p_monitor.runningtime + " ms");
        return results;

//        for (long dest_id : result.keySet()) {
//            System.out.println(dest_id+"  "+result.get(dest_id).size());
//        }
    }

    private void findAtTheHighestLevel(long sid) {
        HashMap<Long, ArrayList<backbonePath>> source_list = filterPossibleNodeInList(source_to_highway_results, b.node_list);

        long bbs_rt = System.currentTimeMillis();
        HashMap<Long, myNode> execute_result = b.no_landmark_bbs(sid, source_list);

        LOGGER.info("execute_result " + (execute_result == null) + " " + execute_result.size());


        for (Map.Entry<Long, HashMap<Long, ArrayList<backbonePath>>> dest_node : destination_to_highway_results.entrySet()) {
            long dest_node_id = dest_node.getKey();
            HashMap<Long, ArrayList<backbonePath>> skyline_dest_highways = destination_to_highway_results.get(dest_node_id); //skyline from dest to highway nodes

            HashSet<Long> hlist = new HashSet<>(skyline_dest_highways.keySet());
            for (long highway_id : hlist) {
                if (execute_result.containsKey(highway_id)) {
                    for (backbonePath new_bp : execute_result.get(highway_id).skyPaths) { //skyline paths from src->highway at the highest level.
                        for (backbonePath d_skyline_bp : skyline_dest_highways.get(highway_id)) { //skyline from dest to highway at the highest level
                            backbonePath final_bp = new backbonePath(new_bp, d_skyline_bp, true);
                            addToResultSet(final_bp, results); //new skylines from src to dest
                        }
                    }
                }
            }

        }

        int i = 0;
        for (long id : execute_result.keySet()) {
            if (!results.containsKey(id)) {
                results.put(id, execute_result.get(id).skyPaths);
            }
        }

        LOGGER.info("number of results sets : " + results.size() + "   running time of finding results at highest level: " + (execute_result != null ? (System.currentTimeMillis() - bbs_rt) : 9999999) + " ms ");
        LOGGER.info("================================================================================");
        return;

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

    private void findTheindexInTheLayerFromOtherNodes(long sid) {
        destination_to_highway_results.clear();


        for (int l = 0; l <= bk_idx.target_idx_level; l++) {
            LOGGER.info("Find the index information at level " + l);
            LOGGER.info("==================================================================================");

            HashSet<Long> node_list_atLevel = bk_idx.readNodeListAtLevel(l);

            for (long n_id : node_list_atLevel) {

                if (n_id == sid) {
                    continue;
                }

                backbonePath destDummyResult = new backbonePath(n_id);
                if (!destination_to_highway_results.containsKey(n_id)) {//no infomation
                    ArrayList<backbonePath> temp_src_list = new ArrayList<>();
                    temp_src_list.add(destDummyResult);

                    HashMap<Long, ArrayList<backbonePath>> tmp_dest_map = new HashMap<>();
                    tmp_dest_map.put(n_id, temp_src_list);

                    destination_to_highway_results.put(n_id, tmp_dest_map);
                }
            }


            LOGGER.info("read the highway index at level " + l + ", there are " + node_list_atLevel.size() + " records (" + destination_to_highway_results.size() + ")");


            HashSet<Long> dhList = new HashSet<>(destination_to_highway_results.keySet());

            for (long dest_id : dhList) {
                HashMap<Long, ArrayList<backbonePath>> current_highways = destination_to_highway_results.get(dest_id); //highways of dest_id
                Set<Long> current_highwayList = new HashSet<>(current_highways.keySet());

                for (long d_id : current_highwayList) {
                    HashMap<Long, ArrayList<double[]>> highwaysOfDestNode = bk_idx.readHighwayNodes(l, d_id);//get highways of d_id
                    if (highwaysOfDestNode != null) {
                        for (long h_node : highwaysOfDestNode.keySet()) {//h_node is highway node of the did, it's the destination node to the next level
                            ArrayList<double[]> cost_from_dest_to_highway = highwaysOfDestNode.get(h_node); //costs from did to h_node
                            if (cost_from_dest_to_highway != null || !cost_from_dest_to_highway.isEmpty()) {
                                ArrayList<backbonePath> bps_dest_to_did = current_highways.get(d_id); //path from dest_id to did

                                if (source_to_highway_results.containsKey(h_node)) { //if a new path, src->h_node->d_id->dest_id, can be found, form new backbone paths
                                    ArrayList<backbonePath> bps_src_to_h_node = source_to_highway_results.get(h_node); //backbone path from src to h_nodes
                                    for (backbonePath old_path : bps_dest_to_did) {
                                        for (backbonePath src_path : bps_src_to_h_node) {
                                            for (double[] costs : cost_from_dest_to_highway) {
                                                backbonePath new_result_bp = new backbonePath(src_path, old_path, costs);
                                                ArrayList<backbonePath> skyline_src_to_dest_id = results.get(dest_id);
                                                if (skyline_src_to_dest_id == null) {
                                                    skyline_src_to_dest_id = new ArrayList<>();
                                                    results.put(dest_id, skyline_src_to_dest_id);
                                                }
                                                addToSkyline(skyline_src_to_dest_id, new_result_bp);
                                            }
                                        }
                                    }
                                }
                                for (backbonePath old_path : bps_dest_to_did) {
                                    for (double[] costs : cost_from_dest_to_highway) {
                                        backbonePath new_bp = new backbonePath(h_node, costs, old_path); //the new path from the dest_id -> did ->h_node
                                        if (!new_bp.hasCycle) {
                                            boolean flag = addToResultSet(new_bp, current_highways);
                                        }
                                    }
                                }


                            }
                        }
                    }
                }
            }
            LOGGER.info("Finish process from backwards direction at level " + l);
        }

    }


    public void findTheIndexInTheLayerIndexUpwards(long sid) {
        source_to_highway_results.clear();
        backbonePath sourceDummyResult = new backbonePath(sid);
        ArrayList<backbonePath> temp_src_list = new ArrayList<>();
        temp_src_list.add(sourceDummyResult);
        source_to_highway_results.put(sid, temp_src_list);

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

                            for (backbonePath old_path : bps_src_to_sid) {
                                for (double[] costs : cost_from_src_to_highway) {
                                    backbonePath new_bp = new backbonePath(h_node, costs, old_path); //the new path from the sid->old_highway->new_highway
                                    if (!new_bp.hasCycle) {
                                        boolean flag = addToResultSet(new_bp, source_to_highway_results);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LOGGER.info("        There is no index for the " + s_id + " at level " + l);
                }
            }
            LOGGER.info("size of the the source highway skyline path information is " + source_to_highway_results.size());
        }
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

    public boolean addToSkyline(ArrayList<backbonePath> bp_list, backbonePath bp) {
        int i = 0;

        if (bp_list == null) {
            bp_list = new ArrayList<>();
            bp_list.add(bp);
            return true;
        } else if (bp_list.isEmpty()) {
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

    public static void main(String args[]) {
        oneToAllBackbone one = new oneToAllBackbone(ParsedOptions.db_name);
        one.query(0l);
    }

}
