package Neo4jTools;


import org.neo4j.graphdb.*;
import utilities.ParsedOptions;

import java.io.*;
import java.util.*;

public class subGraphArea {

    String source_db_name = ParsedOptions.db_name + "_Level0";
    String output_folder = ParsedOptions.output_graphIndo_foler;
    String EdgesPath = output_folder + "/SegInfo.txt";
    String NodePath = output_folder + "/NodeInfo.txt";
    private int numberofK = Integer.parseInt(ParsedOptions.sub_k);

    public static void main(String args[]) {
        subGraphArea sga = new subGraphArea();
        sga.selectSubGraph();
        System.out.println("========================================================================================================");
    }

    public void selectSubGraph() {
        System.out.println(source_db_name);
        System.out.println(output_folder);
        System.out.println(EdgesPath);
        System.out.println(NodePath);

        if (!new File(this.output_folder).exists()) {
            File f = new File(this.output_folder);
            f.mkdirs();
            System.out.println("Create the new Folder " + output_folder);
        } else {
            System.out.println("The folder is existed : " + output_folder);
        }

        Neo4jDB neo4j = new Neo4jDB(source_db_name);
        System.out.println("====================================================================");
        neo4j.startDB(true);
        System.out.println(neo4j.DB_PATH);
        System.out.println("number of nodes of " + ParsedOptions.db_name + " :" + neo4j.getNumberofNodes());
        System.out.println("number of edges of " + ParsedOptions.db_name + " :" + neo4j.getNumberofEdges());
        System.out.println("====================================================================");

        HashSet<Relationship> edges = new HashSet<>();
        HashSet<Long> nodes = new HashSet<>();

        int target_graph_size = numberofK * 1000;

        try (Transaction tx = neo4j.graphDB.beginTx()) {

            ArrayList<Node> nodelist = new ArrayList<>();
            ResourceIterable<Node> nodes_iterable = neo4j.graphDB.getAllNodes();
            ResourceIterator<Node> nodes_iter = nodes_iterable.iterator();
            while (nodes_iter.hasNext()) {
                Node node = nodes_iter.next();
                nodelist.add(node);
            }
            tx.success();

            Node first_node = getRondomNodes(nodelist);

            Queue<Node> q = new LinkedList<>();
            q.add(first_node);

            System.out.println("Start node ------> [" + first_node + "]  to Run BFS graph traverse");
            nodes.add(first_node.getId());
            while (!q.isEmpty()) {
                Node n = q.poll();
                for (Relationship rel : n.getRelationships(Direction.BOTH)) {
                    Node other_n = rel.getOtherNode(n);

                    if (nodes.size() < target_graph_size && !nodes.contains(other_n.getId())) {
                        nodes.add(other_n.getId());
                        q.add(other_n);
                    }

                    if (nodes.contains(rel.getStartNodeId()) && nodes.contains(rel.getEndNodeId())) {
                        edges.add(rel);
                    }
                }
            }


            System.out.println(edges.size());
            System.out.println(nodes.size());

            writeToDisk(nodes, edges, neo4j);
            tx.success();
        }

        neo4j.closeDB();

    }

    private void writeToDisk(HashSet<Long> nodes, HashSet<Relationship> edges, Neo4jDB neo4j) {
        File e_file = new File(this.EdgesPath);
        File n_file = new File(this.NodePath);

        if (e_file.exists()) {
            e_file.delete();
        }

        if (n_file.exists()) {
            n_file.delete();
        }

        HashMap<Long, Long> node_id_mapping = new HashMap<>();

        try (FileWriter fw = new FileWriter(NodePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);
             Transaction tx = neo4j.graphDB.beginTx()) {

            System.out.println(NodePath);
            TreeSet<Long> tm = new TreeSet<>(new LongComparator());
            tm.addAll(nodes);
            long mapped_id = 0;
            for (long node_id : tm) {

                node_id_mapping.put(node_id, mapped_id++);

                StringBuffer sb = new StringBuffer();
                Node node = neo4j.graphDB.getNodeById(node_id);
                sb.append(node_id_mapping.get(node_id)).append(" ");
                sb.append(node.getProperty("lat")).append(" ");
                sb.append(node.getProperty("log")).append(" ");
                out.println(sb.toString().trim());
            }
            tx.success();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try (FileWriter fw = new FileWriter(EdgesPath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);
             Transaction tx = neo4j.graphDB.beginTx()) {
            System.out.println(EdgesPath);

            for (Relationship e : edges) {
                StringBuffer sb = new StringBuffer();
                long snodeId = node_id_mapping.get(e.getStartNodeId());
                long enodeId = node_id_mapping.get(e.getEndNodeId());
                sb.append(snodeId).append(" ");
                sb.append(enodeId).append(" ");
                for (String p : neo4j.propertiesName) {
                    double cost = (double) e.getProperty(p);
                    sb.append(cost).append(" ");
                }
                out.println(sb.toString().trim());
            }

            tx.success();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Node getRondomNodes(ArrayList<Node> nodelist) {
        Random r = new Random();
        int idx = r.nextInt(nodelist.size());
        return nodelist.get(idx);
    }
}
