package Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;


public class NodeCluster {
    public int cluster_id;
    public int max_size = 300;

    public HashSet<Long> node_list = new HashSet<>();
    public HashSet<Long> border_node_list = new HashSet<>();
    public ArrayList<Long> list_b = new ArrayList<>();

    public HashMap<Integer, Integer> border_dist = new HashMap<>(); //adjacent cluster id <--> number of nodes that connect to adjacent cluster
    public HashSet<Long> rels = new HashSet<>(); // the rel id of all the edges in current cluster
    public int num_removed_edges = 0;

    public HashSet<Long> non_border_node_list = new HashSet<>();
    public HashSet<Long> removed_edge_ids = new HashSet<>();

    public NodeCluster(int id) {
        this.cluster_id = id;
    }

    public NodeCluster(int id, int max_size) {
        this.cluster_id = id;
        this.max_size = max_size;
    }

    public boolean isInCluster(long node_id) {
        return node_list.contains(node_id);
    }

    public void addToCluster(long new_node_id) {
        this.node_list.add(new_node_id);
    }

    public boolean oversize() {
        return this.node_list.size() >= max_size;
    }

    public HashSet<Long> getBorderList() {
        return this.border_node_list;
    }

    public Long getRandomBorderNode() {
        return list_b.get(getRandomNumberInRange(0, this.border_node_list.size() - 1));
    }

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public boolean oversize(int cluster_size) {
        double threshold = Math.max(cluster_size, max_size);
        return this.node_list.size() >= threshold;
    }

    public void addAll(NodeCluster other) {
        cluster_id = other.cluster_id;
        max_size = other.max_size;

        node_list = new HashSet<>();
        node_list.addAll(other.node_list);

        border_node_list = new HashSet<>();
        border_node_list.addAll(other.border_node_list);

        list_b = new ArrayList<>();
        list_b.addAll(other.list_b);

        border_dist = new HashMap<>();
        border_dist.putAll(other.border_dist);

        rels = new HashSet<>();
        rels.addAll(other.rels);

        num_removed_edges = other.num_removed_edges;

        non_border_node_list = new HashSet<>();
        non_border_node_list.addAll(other.non_border_node_list);

        removed_edge_ids = new HashSet<>();
        removed_edge_ids.addAll(other.removed_edge_ids);

    }

    public void addAllNode(HashSet<Long> node_list) {
        this.node_list.addAll(node_list);
    }

    public void addRels(HashSet<Long> rels) {
        this.rels.clear();
        this.rels.addAll(rels);
    }

    public void printAllNodes() {
        System.out.println("Node id list ====================================================================================");
        for (long node_id : this.node_list) {
            System.out.println(node_id);
        }
        System.out.println("==================================================================================================");
    }

    /**
     * When c is the only adjacent cluster, the border of c does not need to update this cluster's border nodes.
     *
     * @param c the cluster need to be merged
     */
    public void mergeAll(NodeCluster c) {
        this.node_list.addAll(c.node_list);
    }

    public boolean isEmpty() {
        return node_list.isEmpty();
    }

    public void clear() {
        this.node_list.clear();
        this.border_node_list.clear();
        this.list_b.clear();
        this.border_dist.clear();
        this.rels.clear();
    }
}


