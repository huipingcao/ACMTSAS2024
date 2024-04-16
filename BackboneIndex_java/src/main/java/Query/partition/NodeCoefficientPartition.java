package Query.partition;

import Index.NodeCluster;
import Index.NodeClusters;
import Index.NodeCoefficient;
import Neo4jTools.Line;
import Neo4jTools.Neo4jDB;
import org.neo4j.graphdb.*;
import utilities.ParsedOptions;
import utilities.myClusterQueue;
import utilities.myNode;

import java.util.*;
import java.util.stream.Collectors;

public class NodeCoefficientPartition {

    private final Neo4jDB neo4j;
    private final int cluster_size;
    private final int min_size;
    private final GraphDatabaseService graphdb;

    public NodeCoefficientPartition(Neo4jDB neo4j, int cluster_size, int min_size) {
        this.neo4j = neo4j;
        this.cluster_size = cluster_size;
        this.min_size = min_size;
        this.graphdb = neo4j.graphDB;
    }

    public void NodeCoefficient() {
        HashMap<Long, NodeCoefficient> node_coefficient_list = getNodesCoefficientList();
        for (Map.Entry<Long, NodeCoefficient> e : node_coefficient_list.entrySet()) {
            System.out.println(e.getKey() + "   " + e.getValue().coefficient);
        }

    }

    public NodeClusters CoefficientPartition() {

        HashSet<Long> visited_nodes = new HashSet<>();

        NodeClusters node_clusters = new NodeClusters();

        HashMap<Long, NodeCoefficient> node_coefficient_list = getNodesCoefficientList();

        node_coefficient_list = node_coefficient_list.entrySet()
                .stream()
                .sorted((i1, i2)
                        -> {
                    if (i1.getValue().coefficient - i2.getValue().coefficient == 0) {
                        return 0;
                    } else {
                        return i1.getValue().coefficient - i2.getValue().coefficient > 0 ? 1 : -1;
                    }
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        long cn = neo4j.getNumberofNodes();

//        HashMap<Long, NodeCoefficient> sorted_coefficient = CollectionOperations.sortHashMapByValue(node_coefficient_list);
//        sorted_coefficient.forEach((k, v) -> System.out.println(k + "  " + v.coefficient));

        TreeMap<Integer, Integer> node_neighbor_number_distribution = new TreeMap<>(); //the distribution of the two-neighbor coefficient
        System.out.println("Number of second hop neighbors, occurrences");

        for (Map.Entry<Long, NodeCoefficient> node_coeff : node_coefficient_list.entrySet()) {
            if (node_neighbor_number_distribution.containsKey(node_coeff.getValue().getNumberOfTwoHopNeighbors())) {
                node_neighbor_number_distribution.put(node_coeff.getValue().getNumberOfTwoHopNeighbors(), node_neighbor_number_distribution.get(node_coeff.getValue().getNumberOfTwoHopNeighbors()) + 1);
            } else {
                node_neighbor_number_distribution.put(node_coeff.getValue().getNumberOfTwoHopNeighbors(), 1);
            }
        }

        int dis_indicator = 0;
        int noise_indicator = 0;
        for (Map.Entry<Integer, Integer> e : node_neighbor_number_distribution.entrySet()) {
            dis_indicator += e.getValue();
            if ((1.0 * dis_indicator / node_coefficient_list.size()) * 100 > ParsedOptions.p_ind) {
                break;
            } else {
                noise_indicator = e.getKey();
            }
        }
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(node_coefficient_list.size() + "            indicator ::: " + noise_indicator);

        int num_visited = 1;
        long whole_rt;
        long iter_rt_start;
        iter_rt_start = whole_rt = System.currentTimeMillis();


        for (Map.Entry<Long, NodeCoefficient> node_coeff : node_coefficient_list.entrySet()) {
            try (Transaction tx = this.neo4j.graphDB.beginTx()) {
                if (num_visited % 10000 == 0) {
                    long ten_thousand_inter_rt = System.currentTimeMillis() - iter_rt_start;
                    iter_rt_start = System.currentTimeMillis();
                    System.out.println("Process nodes in cluster finding: " + num_visited + "/" + cn + "in " + ten_thousand_inter_rt + " (" + (System.currentTimeMillis() - whole_rt) + " ) ms ......................................");
                }

                num_visited++;


                long node_id = node_coeff.getKey();

                if (visited_nodes.contains(node_id)) {
                    continue;
                }

                //if node coefficient is less than the noise indicator indicated, treat is as the noise node
                if (node_coeff.getValue().getNumberOfTwoHopNeighbors() <= noise_indicator) {
                    //save the node to visited_nodes structure, the node_id must not be in the visited_nodes structure
//                    myNode next_node = new myNode(node_id, node_coefficient_list.get(node_id).coefficient);
                    visited_nodes.add(node_id);
                    node_clusters.clusters.get(0).addToCluster(node_id);
                    continue;
                }


                double coefficient = node_coefficient_list.get(node_id).coefficient;

                NodeCluster cluster = new NodeCluster(node_clusters.getNextClusterID(), this.cluster_size); //create a new cluster

                myClusterQueue queue = new myClusterQueue();

                myNode m_node = new myNode(node_id, coefficient);

                queue.add(m_node);

                //expand from current node_id
                while (!queue.isEmpty()) {
                    myNode n = queue.pop();

                    if (node_clusters.clusters.get(0).node_list.contains(n.id)) { // change the label of the node from noise to label c
                        node_clusters.clusters.get(0).node_list.remove(n.id);
                        cluster.addToCluster(n.id);
                    } else if (visited_nodes.contains(n.id)) { // if n already has label
                        continue;
                    } else {
                        cluster.addToCluster(n.id);
                        visited_nodes.add(n.id);
                    }

                    ArrayList<Node> neighbors = neo4j.getNeighborsNodeList(n.id);

                    boolean can_add_to_queue = !cluster.oversize(this.min_size); //check the size of the cluster, do not allow the cluster to large

                    for (Node neighbor_node : neighbors) {
                        if (can_add_to_queue) { //still has size to add new node
                            NodeCoefficient n_coff = node_coefficient_list.get(neighbor_node.getId());
                            myNode next_node = new myNode(neighbor_node.getId(), n_coff.coefficient);
                            if (n_coff.getNumberOfTwoHopNeighbors() > noise_indicator) { //used the number of the second hop neighbors to check whether its the noise node
                                queue.add(next_node);
                            }
                        }
                    }
                }

                node_clusters.clusters.put(cluster.cluster_id, cluster);
                tx.success();
            }
        }

        System.out.println("found # of clusters : " + node_clusters.clusters.size());
//        System.out.println("=================================================================================");
//        System.out.println("The distribution of the two-hop node coefficient :   ");
//        node_neighbor_number_distribution.forEach((k, v) -> System.out.println(k + "  " + v));
//        System.out.println("=================================================================================");
        return node_clusters;
    }

    public HashMap<Long, NodeCoefficient> getNodesCoefficientList() {

        HashMap<Long, NodeCoefficient> nodes_coefficient_list = new HashMap<>();
        try (Transaction tx = graphdb.beginTx()) {

            ResourceIterator<Node> nodes_iter = graphdb.getAllNodes().iterator();

            while (nodes_iter.hasNext()) {
                Node c_node = nodes_iter.next();

                Iterator<Relationship> rel_iter = c_node.getRelationships(Line.Linked, Direction.BOTH).iterator();
                HashSet<Node> neighbors = new HashSet<>();
                while (rel_iter.hasNext()) {
                    neighbors.add(rel_iter.next().getOtherNode(c_node));
                }


                HashSet<Node> second_neighbors = new HashSet<>();
                for (Node n_node : neighbors) {
                    Iterator<Relationship> sec_rel_iter = n_node.getRelationships(Line.Linked, Direction.BOTH).iterator();
                    while (sec_rel_iter.hasNext()) {
                        Node sec_nb = sec_rel_iter.next().getOtherNode(n_node);
                        if (!neighbors.contains(sec_nb) && sec_nb.getId() != c_node.getId()) {
                            second_neighbors.add(sec_nb);
                        }
                    }
                }


                int num_connected_neighbors = getNumberOfConnectedNeighbors(neighbors);
                int num_connected_sec_neighbors = getNumberOfConnectedNeighbors(second_neighbors);
                int num_connected_one_with_sec_neighbors = getNumberOfConnectedNeighbors(neighbors, second_neighbors);


                double coefficient1 = 1.0 * num_connected_neighbors / (neighbors.size() * (neighbors.size() - 1));
                double coefficient2 = 1.0 * num_connected_one_with_sec_neighbors / (neighbors.size() * (neighbors.size() - 1));
                double coefficient3 = 1.0 * num_connected_sec_neighbors / (second_neighbors.size() * (second_neighbors.size() - 1));

                coefficient1 = Double.isNaN(coefficient1) ? 1 : 1 - coefficient1;
                coefficient2 = Double.isNaN(coefficient2) ? 1 : 1 - coefficient2; // the lower the better.
                coefficient3 = Double.isNaN(coefficient3) ? 1 : 1 - coefficient3;

//                if (coefficient1 != -1 && coefficient2 != -1 && coefficient3 != -1) {
//                    System.out.println(c_node.getId() + "  " + c_node.getDegree(Direction.BOTH) + "  " + coefficient1 + "  " + num_connected_one_with_sec_neighbors + " "
//                            + neighbors.size() + " " + second_neighbors.size() + " " + coefficient3 + "   " + coefficient2);
//                }
                NodeCoefficient n_coff = new NodeCoefficient(coefficient2, neighbors.size(), second_neighbors.size());
                nodes_coefficient_list.put(c_node.getId(), n_coff);
            }

            tx.success();
        }

        return nodes_coefficient_list;
    }

    private int getNumberOfConnectedNeighbors(HashSet<Node> neighbors) {
        int connctions = 0;
        for (Node s_n : neighbors) {
            Iterator<Relationship> rel_iter = s_n.getRelationships(Line.Linked, Direction.BOTH).iterator();
            while (rel_iter.hasNext()) {
                Node other_node = rel_iter.next().getOtherNode(s_n);
                if (neighbors.contains(other_node)) {
                    connctions++;
                }
            }
        }
        return connctions;
    }

    /**
     * Return the number of neighbor nodes are connected sec_neighbors.
     * 1) find each pair of the second level neighbors of each nodes in the neighbors.
     * 2) count the number of common the second level neighbors of each pair.
     *
     * @param neighbors        first level neighbors
     * @param second_neighbors second level neighbors
     * @return
     */
    private int getNumberOfConnectedNeighbors(HashSet<Node> neighbors, HashSet<Node> second_neighbors) {
        int connections = 0;

        ArrayList<Node> n_list = new ArrayList<>(neighbors);
        HashSet<Long> sec_neighbors_node_ids = new HashSet<>();
        second_neighbors.forEach(n -> sec_neighbors_node_ids.add(n.getId()));

        for (int i = 0; i < n_list.size(); i++) {
            for (int j = i + 1; j < n_list.size(); j++) {
                Iterator<Relationship> i_rel_iter = n_list.get(i).getRelationships(Line.Linked, Direction.BOTH).iterator();
                Iterator<Relationship> j_rel_iter = n_list.get(j).getRelationships(Line.Linked, Direction.BOTH).iterator();

                HashSet<Node> i_n_nodes = new HashSet<>();
                HashSet<Node> j_n_nodes = new HashSet<>();

                while (i_rel_iter.hasNext()) {
                    i_n_nodes.add(i_rel_iter.next().getOtherNode(n_list.get(i)));
                }

                while (j_rel_iter.hasNext()) {
                    j_n_nodes.add(j_rel_iter.next().getOtherNode(n_list.get(j)));
                }

                for (Node i_n : i_n_nodes) {
                    for (Node j_n : j_n_nodes) {
                        if (i_n.getId() == j_n.getId() && sec_neighbors_node_ids.contains(i_n.getId())) {
                            connections++;
                        }
                    }
                }
            }
        }
        return connections;
    }

}
