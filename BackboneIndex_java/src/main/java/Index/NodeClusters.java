package Index;

import Neo4jTools.Neo4jDB;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class NodeClusters {
    public HashMap<Integer, NodeCluster> clusters = new HashMap<>();

    public NodeClusters() {
        //add a noise cluster to store the noise nodes
        NodeCluster noise_cluster = new NodeCluster(0, Integer.MAX_VALUE);
        this.clusters.put(noise_cluster.cluster_id, noise_cluster);

    }

    public boolean isInClusters(long node_id) {
        for (int cluster_id : clusters.keySet()) {
            if (this.clusters.get(cluster_id).isInCluster(node_id)) {
                return true;
            }
        }

        return false;
    }

    public int getNextClusterID() {
        return clusters.size();
    }

    public int getClusterIdByRelId(Long rel_id) {
        for (int cluster_id : clusters.keySet()) {
            if (this.clusters.get(cluster_id).isInCluster(rel_id)) {
                return cluster_id;
            }
        }
        return -1;
    }

    public int getNumberOfClusters() {
        return this.clusters.size();
    }

    public void mergeSmallCluster(int num_minor, Neo4jDB neo4j) {

        //If the total number of the node is less than given num_minor don;t need the merge process
        if(neo4j.getNumberofNodes()<=num_minor){
            return;
        }

        HashSet<Integer> merged_cluster_ids = new HashSet<>();
        for (Map.Entry<Integer, NodeCluster> cc : clusters.entrySet()) {
            NodeCluster c = cc.getValue();

            if (c.node_list.size() < num_minor) {

                if (c.cluster_id != 0) {
                    System.out.print(c.cluster_id + "  # of nodes : " + c.node_list.size() + "  # of borders: " + c.border_node_list.size() + "  :");
                    c.border_dist.forEach((k, v) -> {
                        System.out.print(k + "-->" + v + "|");
                    });
                    System.out.println();
                }

                boolean needToMerge = true;
                while (needToMerge) {
                    updatedBorderInformation(c, neo4j);
                    NodeCluster adj_cluster = null;
                    // get the adj cluster which the current cluster c adjacent most
                    adj_cluster = getMajorityAdjCluster(c.border_dist);

//                    boolean single_adj_cluster = c.border_dist.size() == 1;

                    if (adj_cluster == null || adj_cluster.isEmpty()) {
                        break;
                    }

                    adj_cluster.node_list.addAll(c.node_list);
                    c.clear();
                    if (adj_cluster.cluster_id != 0) {
                        System.out.println("        the cluster " + c.cluster_id + " is merged to " + adj_cluster.cluster_id);
                    } else {
                        System.out.println("        the cluster " + c.cluster_id + " is merged to the noise cluster.");
                    }
                    merged_cluster_ids.add(c.cluster_id);

                    if (adj_cluster != null && adj_cluster.node_list.size() < num_minor) {
                        c = adj_cluster; //if the merged cluster size is still less than num_minor.
                        needToMerge = true;
                    } else {
                        needToMerge = false;
                    }

                }
            } else {
                System.out.println(c.cluster_id + "  # of nodes : " + c.node_list.size() + "  # of borders: " + c.border_node_list.size() + " -- not needed to do the merge ");
            }
        }

        for (int cluster_id : merged_cluster_ids) {
            this.clusters.remove(cluster_id);
        }

        System.out.println("============================================");
        for (Map.Entry<Integer, NodeCluster> cc : clusters.entrySet()) {
            NodeCluster c = cc.getValue();
            updatedBorderInformation(c, neo4j);

            if (c.node_list.size() < num_minor) {
                if (c.node_list.size() == 0 && c.cluster_id == 0) {
                    System.out.println("Noise cluster's size if zero !!!!!!!!!!!!!!!!!!!!");
                } else {
                    if (c.cluster_id != 0) {
                        System.out.print(c.cluster_id + "  " + c.node_list.size() + ":");
                        System.out.println("Merge process failure !!!!!!!!!!!!!!!");
                        System.exit(0);
                    }
                }
            }

//            if (c.node_list.size() < num_minor) {
            if (c.cluster_id != 0) {
                System.out.print(c.cluster_id + "  " + c.node_list.size() + ":");
                c.border_dist.forEach((k, v) -> {
                    System.out.print(k + "-->" + v + "|");
                });
                System.out.println();
            }
        }
        System.out.println("============================================");


    }

    /**
     * Get the adj cluster which has the maximum number of border nodes with it.
     * Also, the adj cluster should not be merged before. Which means the adj should be merge to other cluster before.
     * <p>
     *
     * @param border_dist
     * @return
     */
    private NodeCluster getMajorityAdjCluster(HashMap<Integer, Integer> border_dist) {
        int max = Integer.MIN_VALUE;
        int adj_id = -1;
        for (int adj_cluster_id : border_dist.keySet()) {
            if (border_dist.get(adj_cluster_id) > max) {
                max = border_dist.get(adj_cluster_id);
                adj_id = adj_cluster_id;
            }
        }

        return this.clusters.get(adj_id);
    }

    /**
     * update the border information of the clusters in node cluster list
     *
     * @param neo4j
     */
    public void updateClustersBorders(Neo4jDB neo4j) {
        for (Map.Entry<Integer, NodeCluster> cc : clusters.entrySet()) {

            NodeCluster c = cc.getValue();

            c.border_node_list.clear();
            c.list_b.clear();
            c.border_dist.clear();

            try (Transaction tx = neo4j.graphDB.beginTx()) {
                for (long node_id : c.node_list) {

                    boolean connect_to_other_cluster = false;

                    ArrayList<Long> n_list = neo4j.getNeighborsIdList(node_id);

                    for (long n : n_list) {
                        if (!c.node_list.contains(n)) {

                            HashSet<Integer> border_list = getClusterIdByNodeId(n);

                            for (int adj_cluster_id : border_list) {
                                if (adj_cluster_id != -1) {
                                    if (c.border_dist.containsKey(adj_cluster_id)) {
                                        c.border_dist.put(adj_cluster_id, c.border_dist.get(adj_cluster_id) + 1);
                                    } else {
                                        c.border_dist.put(adj_cluster_id, 1);
                                    }
                                }
                            }

                            if (!connect_to_other_cluster) {
                                connect_to_other_cluster = true;
                            }
                        }
                    }

                    if (connect_to_other_cluster) {
                        c.border_node_list.add(node_id);
                    }
                }
                tx.success();
            }
            c.list_b = new ArrayList<>(c.border_node_list);
        }


        /**
         *  Print the cluster's information after the updating
         */
//        for (Map.Entry<Integer, NodeCluster> cc : clusters.entrySet()) {
//            int cluster_id = cc.getKey();
//            NodeCluster c = cc.getValue();
//            System.out.print(c.cluster_id + " ( " + cluster_id + " )   cluster size:" + c.node_list.size() +"  Border size :"+c.border_node_list.size() +" : ");
//            c.border_dist.forEach((k, v) -> {
//                System.out.print(k + "-->" + v + "|");
//            });
//            System.out.println();
//        }

        System.out.println("Updated the clusters ........................................................");
    }

    private HashSet<Integer> getClusterIdByNodeId(long node_id) {
        HashSet<Integer> border_list = new HashSet<>();
        for (int cluster_id : clusters.keySet()) {
            if (this.clusters.get(cluster_id).isInCluster(node_id)) {
                border_list.add(cluster_id);
            }
        }
        return border_list;
    }

    /**
     * update the border information of current cluster
     *
     * @param c
     * @param neo4j
     */
    private void updatedBorderInformation(NodeCluster c, Neo4jDB neo4j) {
        c.border_node_list.clear();
        c.list_b.clear();
        c.border_dist.clear();

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            for (long node_id : c.node_list) {
                boolean connect_to_other_cluster = false;

                ArrayList<Long> n_list = neo4j.getNeighborsIdList(node_id);

                for (long n : n_list) {
                    if (!c.node_list.contains(n)) {
                        HashSet<Integer> border_list = getClusterIdByNodeId(n);
                        for (int adj_cluster_id : border_list) {
                            if (adj_cluster_id != -1) {
                                if (c.border_dist.containsKey(adj_cluster_id)) {
                                    c.border_dist.put(adj_cluster_id, c.border_dist.get(adj_cluster_id) + 1);
                                } else {
                                    c.border_dist.put(adj_cluster_id, 1);
                                }
                            }
                        }

                        if (!connect_to_other_cluster) {
                            connect_to_other_cluster = true;
                        }
                    }
                }

                if (connect_to_other_cluster) {
                    c.border_node_list.add(node_id);
                }
            }
            tx.success();
        }
        c.list_b = new ArrayList<>(c.border_node_list);
    }
}
