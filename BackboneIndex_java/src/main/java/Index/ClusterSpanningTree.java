package Index;

import Neo4jTools.Neo4jDB;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;


import java.util.*;

public class ClusterSpanningTree {

    public Neo4jDB neo4j = null;
    public HashSet<Long> SpTree; // the id of the edges that belongs to the Spanning tree
    public HashSet<Long> N_nodes; // the id of the nodes in the spanning tree, its same as the nodes of the graph

    int E = 0; // number of edges
    int N = 0; // number of nodes

    int connect_component_number = 0; // number of the connect component found in the graph
    HashMap<Long, UFnode> unionfind = new HashMap<>();
    boolean isSingle = false;
    boolean isEmpty = false;


    public HashSet<Long> rels = new HashSet<>(); //the rels in current clusters
    public HashSet<Long> Dijkstra_rels = new HashSet<>();
    public HashSet<Long> BBSRels = new HashSet<>();
    private HashSet<Long> properties_rel = new HashSet<>();

    public ClusterSpanningTree(Neo4jDB neo4j, boolean init, HashSet<Long> node_list) {
        this.neo4j = neo4j;
        SpTree = new HashSet<>();
        N_nodes = new HashSet<>(node_list);

        if (init) {
            initialization();
        }
    }


    /**
     * Initialization:
     * Get the number of edges
     * Get the number of nodes
     * The init number of connect components is the number of the nodes
     * each node whose root is the node id
     */
    private void initialization() {
//        System.out.println(neo4j.DB_PATH);
        long nn = this.N_nodes.size();
        this.rels = new HashSet<>(neo4j.getEdges(this.N_nodes));
//        System.out.println("number of nodes :" + nn + "   number of edges :" + rels.size());

        this.N = (int) nn;
        this.E = (int) (nn - 1);

        // At the beginning, the number of connected components is the number of nodes.
        // Each node is a connected component.
        this.connect_component_number = N;

        for (long n_id : this.N_nodes) {
            UFnode unode = new UFnode(n_id);
            unionfind.put(n_id, unode);
        }
    }

    public String EulerTourStringWiki() {
        KruskalMST();
        return null;
    }

    /**
     * Get the spanning tree of the graph.
     * Get number of components
     * Get the relationships that consists of the spanning tree
     */
    public void KruskalMST() {

        ArrayList<Long> degree_pair = getDegreePairs();

        try (Transaction tx = this.neo4j.graphDB.beginTx()) {
            SpTree = new HashSet<>();
            int e = 0;
//            Iterator<Long> rel_iterator = rels.iterator();
            Iterator<Long> rel_iterator = degree_pair.iterator();

            while (e < N - 1 && rel_iterator.hasNext()) {
                Relationship rel = this.neo4j.graphDB.getRelationshipById(rel_iterator.next());
                long src_id = rel.getStartNodeId();
                long dest_id = rel.getEndNodeId();
                long src_root = find(src_id);
                long dest_root = find(dest_id);
                if (src_root != dest_root) {
                    SpTree.add(rel.getId());
                    e++;
                    union(src_root, dest_root);
                    connect_component_number--;
                }
            }

            tx.success();
        }
    }

    /**
     * Union the lower rank component to the higher rank component
     * If the rank of two components are same, union the dest component to the src component.
     * Then increase the rank ot the src component.
     *
     * @param src_root  the root of src node in UF structure
     * @param dest_root the root of dest node in UF structure
     */
    private void union(long src_root, long dest_root) {
        if (unionfind.get(src_root).rank < unionfind.get(dest_root).rank) {
            unionfind.get(src_root).parentID = dest_root;
            unionfind.get(dest_root).size += unionfind.get(src_root).size;
        } else if (unionfind.get(src_root).rank > unionfind.get(dest_root).rank) {
            unionfind.get(dest_root).parentID = src_root;
            unionfind.get(src_root).size += unionfind.get(dest_root).size;
        } else {
            unionfind.get(dest_root).parentID = src_root;
            unionfind.get(src_root).rank++;
            unionfind.get(src_root).size += unionfind.get(dest_root).size;
        }
    }

    /**
     * Find the root of the node, only the root node's id is equal to its node id.
     *
     * @param src_id the node id
     * @return the root id of src_id
     */
    private long find(long src_id) {
        while (unionfind.get(src_id).parentID != src_id) {
            src_id = unionfind.get(src_id).parentID;
        }
        return src_id;
    }

    public ArrayList<Long> getDegreePairs() {
        TreeMap<Pair<Integer, Integer>, ArrayList<Long>> degree_pairs = new TreeMap(new PairComparator("desc")); //order the edge pair by desc order
        try (Transaction tx = this.neo4j.graphDB.beginTx()) {
            for (long r_id : this.rels) {
                Relationship rels = this.neo4j.graphDB.getRelationshipById(r_id);
                int start_r = rels.getStartNode().getDegree(Direction.BOTH);
                int end_r = rels.getEndNode().getDegree(Direction.BOTH);

                if (start_r > end_r) {
                    int t = end_r;
                    end_r = start_r;
                    start_r = t;
                }

                Long rel_id = rels.getId();
                Pair<Integer, Integer> p = new MutablePair<>(start_r, end_r);
                if (degree_pairs.containsKey(p)) {
                    ArrayList<Long> a = degree_pairs.get(p);
                    a.add(rel_id);
                    degree_pairs.put(p, a);
                } else {
                    ArrayList<Long> a = new ArrayList<>();
                    a.add(rel_id);
                    degree_pairs.put(p, a);
                }
            }

            tx.success();
        }
        ArrayList<Long> sorted_degree_pair_rel_list = new ArrayList<>();

        for (Map.Entry<Pair<Integer, Integer>, ArrayList<Long>> e : degree_pairs.entrySet()) {
            for (long rel_id : e.getValue()) {
                sorted_degree_pair_rel_list.add(rel_id);
            }
        }

        return sorted_degree_pair_rel_list;
    }
}
