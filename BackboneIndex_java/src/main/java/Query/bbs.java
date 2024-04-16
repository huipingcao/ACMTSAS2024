package Query;

import Index.Landmark.Landmark;
import Neo4jTools.Neo4jDB;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class bbs {
    private final String db_name;
    public HashSet<Long> node_list = new HashSet<>();
    public Landmark ldm_idx;


    public bbs(String db_name) {
        this.db_name = db_name;
        ldm_idx = new Landmark(this.db_name);
        getNodeList();
    }

    private void getNodeList() {
        Neo4jDB neo4j = new Neo4jDB(this.db_name);
        neo4j.startDB(false);
        this.node_list = neo4j.getNodes();
        neo4j.closeDB();
    }


    public boolean landmark_bbs(long source_node, long dest_node, HashMap<Long, ArrayList<backbonePath>> source_nodes_list,
                                HashMap<Long, ArrayList<backbonePath>> all_possible_dest_node_with_skypaths,
                                ArrayList<backbonePath> results) {

        long total_bbs_time = 0;
        long start_bbs_time = System.currentTimeMillis();

        Neo4jDB neo4j = new Neo4jDB(this.db_name);
        neo4j.startDB(true);
        HashMap<Long, myNode> tmpStoreNodes = new HashMap();
        try (Transaction tx = neo4j.graphDB.beginTx()) {
            myNodePriorityQueue mqueue = new myNodePriorityQueue();

            for (Map.Entry<Long, ArrayList<backbonePath>> source_e : source_nodes_list.entrySet()) {
                myNode snode = new myNode(source_node, dest_node, source_e, all_possible_dest_node_with_skypaths, neo4j);
                mqueue.add(snode);
                tmpStoreNodes.put(snode.id, snode);

            }


            while (!mqueue.isEmpty()) {

                long current_bbs_time = System.currentTimeMillis();
                total_bbs_time += (current_bbs_time - start_bbs_time);
                start_bbs_time = current_bbs_time;

                if (total_bbs_time >= 3600000) {
                    neo4j.closeDB();
                    return false;
                }


                myNode v = mqueue.pop();
                v.inqueue = false;
//                System.out.println("[Queue]: pop out the node " + v.id);

                for (int i = 0; i < v.skyPaths.size(); i++) {
                    backbonePath p = v.skyPaths.get(i);
//                    System.out.println("        "+p.p.expanded+" "+p.p.possible_destination.size());

                    if (!p.p.expanded) {
                        p.p.expanded = true;

                        if (ldm_idx.landmark_index.size() != 0) {
                            updateThePathDestinationList(p, results);
                        }

                        //Still can be expand to any of the destination
                        if (!p.p.possible_destination.isEmpty()) {

                            ArrayList<backbonePath> new_paths = p.expand(neo4j);
//                            ArrayList<backbonePath> flat_new_paths = flatindex.expand(p);
//                            new_paths.addAll(flat_new_paths);

                            for (backbonePath new_bp : new_paths) {

                                if (new_bp.hasCycle || dominatedByResult(new_bp.costs, results)) {
                                    continue;
                                }

                                myNode next_n;
                                if (tmpStoreNodes.containsKey(new_bp.destination)) {
                                    next_n = tmpStoreNodes.get(new_bp.destination);
                                } else {
                                    next_n = new myNode(source_node, dest_node, new_bp.destination, all_possible_dest_node_with_skypaths, neo4j);
                                    tmpStoreNodes.put(next_n.id, next_n);
                                }

                                if (new_bp.p.possible_destination.containsKey(next_n.id)) {
                                    for (backbonePath d_skyline_bp : new_bp.p.possible_destination.get(next_n.id)) {
                                        backbonePath final_bp = new backbonePath(new_bp, d_skyline_bp, true);
                                        addToSkyline(results, final_bp);
                                    }

                                    new_bp.p.possible_destination.remove(next_n.id);

                                    if (new_bp.p.possible_destination.size() != 0) {
                                        if (next_n.addToSkyline(new_bp) && !next_n.inqueue) {
                                            mqueue.add(next_n);
                                        }
                                    }
                                } else if (next_n.addToSkyline(new_bp) && !next_n.inqueue) {
                                    mqueue.add(next_n);
                                }
                            }
                        }
                    }
                }
            }
            tx.success();
        }

        neo4j.closeDB();
        return true;
    }

    public HashMap<Long, myNode> no_landmark_bbs(long source_node, HashMap<Long, ArrayList<backbonePath>> source_nodes_list) {

        long total_bbs_time = 0;
        long start_bbs_time = System.currentTimeMillis();

        Neo4jDB neo4j = new Neo4jDB(this.db_name);
        neo4j.startDB(true);
        HashMap<Long, myNode> tmpStoreNodes = new HashMap();

//        System.out.println(neo4j.getNumberofNodes() + "  " + neo4j.getNumberofEdges());

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            myNodePriorityQueue mqueue = new myNodePriorityQueue();

            for (Map.Entry<Long, ArrayList<backbonePath>> source_e : source_nodes_list.entrySet()) {
                myNode snode = new myNode(source_node, -1, source_e, null, neo4j);
                mqueue.add(snode);
                tmpStoreNodes.put(snode.id, snode);
            }


            while (!mqueue.isEmpty()) {

                long current_bbs_time = System.currentTimeMillis();
                total_bbs_time += (current_bbs_time - start_bbs_time);
                start_bbs_time = current_bbs_time;

                if (total_bbs_time >= 3600000) {
                    neo4j.closeDB();
                    return null;
                }


                myNode v = mqueue.pop();
                v.inqueue = false;

                for (int i = 0; i < v.skyPaths.size(); i++) {
                    backbonePath p = v.skyPaths.get(i);

                    if (!p.p.expanded) {
                        p.p.expanded = true;


                        ArrayList<backbonePath> new_paths = p.expand(neo4j);

                        for (backbonePath new_bp : new_paths) {
                            myNode next_n;
                            if (tmpStoreNodes.containsKey(new_bp.destination)) {
                                next_n = tmpStoreNodes.get(new_bp.destination);
                            } else {
                                next_n = new myNode(source_node, -1, new_bp.destination, null, neo4j);
                                tmpStoreNodes.put(next_n.id, next_n);
                            }

                            if (next_n.addToSkyline(new_bp) && !next_n.inqueue) {
                                mqueue.add(next_n);
                            }
                        }
                    }
                }
            }
            tx.success();
        }

        neo4j.closeDB();

//        for (long id : tmpStoreNodes.keySet()) {
//            System.out.println(id + " " + tmpStoreNodes.get(id).skyPaths.size());
//        }

        return tmpStoreNodes;
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

    private void updateThePathDestinationList(backbonePath p, ArrayList<backbonePath> results) {
        ArrayList<Long> deleted_dest_nodes = new ArrayList<>();

        for (long dest_highway_node_id : p.p.possible_destination.keySet()) {
//            System.out.println("    [possible destination]: " + dest_highway_node_id);

            ArrayList<backbonePath> dest_skyline = p.p.possible_destination.get(dest_highway_node_id);

            for (int dp_idx = 0; dp_idx < dest_skyline.size(); ) {
                backbonePath dest_skyline_bp = dest_skyline.get(dp_idx); //the skyline paths from dest_highways to destination node
                double[] p_l_costs = getLowerBound(p.costs, p.destination, dest_skyline_bp);
                if (dominatedByResult(p_l_costs, results)) {
                    dest_skyline.remove(dp_idx);//remove if dominate
                } else {
                    dp_idx++;
                }
            }

            //all of the skyline paths from dest_highway nodes to dest nodes can not be a candidate results from this path p to destination node
            if (dest_skyline.size() == 0) {
                deleted_dest_nodes.add(dest_highway_node_id);
            }
        }

        for (long deleted_node : deleted_dest_nodes) {
            p.p.possible_destination.remove(deleted_node);
        }
    }


    private double[] getLowerBound(double[] costs, long src, backbonePath dest_dp) {
        double[] estimated_costs = new double[3];

        for (int i = 0; i < estimated_costs.length; i++) {
            estimated_costs[i] = Double.NEGATIVE_INFINITY;
        }

        for (long landmark : ldm_idx.landmark_index.keySet()) {
            double[] src_cost = ldm_idx.landmark_index.get(landmark).get(src); //the source node (the destination of the current path) to landmark
            double[] dest_cost = ldm_idx.landmark_index.get(landmark).get(dest_dp.destination); // the dest_highways to landmark
            for (int i = 0; i < estimated_costs.length; i++) {
                double value = Math.abs(src_cost[i] - dest_cost[i]);
                if (value > estimated_costs[i]) {
                    estimated_costs[i] = value;
                }
            }
        }

        for (int i = 0; i < estimated_costs.length; i++) {
            estimated_costs[i] += costs[i] + dest_dp.costs[i];
        }
        return estimated_costs;
    }


    private boolean dominatedByResult(double[] estimated_costs, ArrayList<backbonePath> results) {

        if (results == null) { //no results from dest to the this highway node at the highest level
            return false;
        }

        for (backbonePath rp : results) {
            if (checkDominated(rp.costs, estimated_costs)) {
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

}
