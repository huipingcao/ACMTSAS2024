package Query.partition;

import Index.NodeCluster;
import Index.NodeClusters;
import Neo4jTools.Neo4jDB;
import org.neo4j.graphdb.*;

import java.util.*;

public class BFSPartition {

    private final Neo4jDB neo4j;
    private final int cluster_size;
    private final int min_size;
    private final GraphDatabaseService graphdb;

    public BFSPartition(Neo4jDB neo4j, int cluster_size, int min_size) {
        this.neo4j = neo4j;
        this.cluster_size = cluster_size;
        this.min_size = min_size;
        this.graphdb = neo4j.graphDB;
    }

    public NodeClusters partition() {
        System.out.println("BFS Partition is being called ....................................");
        NodeClusters node_clusters = new NodeClusters();
        try (Transaction tx = this.graphdb.beginTx()) {

            HashSet<Node> visited_nodes = new HashSet<>();
            long cn = this.neo4j.getNumberofNodes();

            while (visited_nodes.size() < cn) {
                Node first_node = getUnvisitedRondomNodes(visited_nodes);
//                System.out.println("BFS Start from first " + first_node);
                int cluster_id = node_clusters.getNextClusterID();
                NodeCluster cluster = BFS(first_node, cluster_id, visited_nodes);
                node_clusters.clusters.put(cluster.cluster_id, cluster);
            }

            tx.success();
        }

        return node_clusters;

    }

    private Node getUnvisitedRondomNodes(HashSet<Node> visited_nodes) {
        ResourceIterable<Node> nodes_iterable = neo4j.graphDB.getAllNodes();
        ResourceIterator<Node> nodes_iter = nodes_iterable.iterator();
        while (nodes_iter.hasNext()) {
            Node node = nodes_iter.next();
            if (!visited_nodes.contains(node)) {
                return node;
            }
        }
        return null;

    }

    private NodeCluster BFS(Node first_node, int cluster_id, HashSet<Node> visited_nodes) {

        NodeCluster cluster = new NodeCluster(cluster_id, this.cluster_size); //create a new cluster

        int start_num_node = visited_nodes.size();

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            Queue<Node> q = new LinkedList<>();
            q.add(first_node);

            visited_nodes.add(first_node);


            while (!q.isEmpty()) {
                Node n = q.poll();

                cluster.addToCluster(n.getId());

                for (Relationship rel : n.getRelationships(Direction.BOTH)) {
                    Node other_n = rel.getOtherNode(n);

                    if ((visited_nodes.size() - start_num_node) < this.min_size && !visited_nodes.contains(other_n)) {
                        visited_nodes.add(other_n);
                        q.add(other_n);
                    }
                }
            }
            tx.success();
        }

        System.out.println("BFS: "+(visited_nodes.size() - start_num_node) + "  " + cluster.node_list.size());

        return cluster;
    }
}
