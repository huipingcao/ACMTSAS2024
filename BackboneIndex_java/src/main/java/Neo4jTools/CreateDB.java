package Neo4jTools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import utilities.ParsedOptions;

public class CreateDB {
    public static void main(String args[]){
        CreateDB c = new CreateDB();
        c.createChangeGraphDB();
    }

    public void createChangeGraphDB() {
        String sub_db_name = ParsedOptions.db_name + "_Level0";

        String nodeFilePath = ParsedOptions.GraphInfoPath + "/NodeInfo.txt";
        String EdgeFilePath = ParsedOptions.GraphInfoPath + "/SegInfo.txt";

        String folder = ParsedOptions.neo4jdbPath + "/" + ParsedOptions.db_name + "_Level0";

        File db_folder = new File(folder);
        try {
            if (db_folder.exists()) {
                System.out.println("delete the folder : " + folder);
                FileUtils.deleteDirectory(db_folder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        Neo4jDB neo4j = new Neo4jDB(sub_db_name);
        neo4j.deleleDB();
        System.out.println("====================================================================");
        neo4j.startDB(false);
        System.out.println(neo4j.DB_PATH);
        System.out.println("====================================================================");

        System.out.println("node file path :" + nodeFilePath);
        System.out.println("edge file path :" + EdgeFilePath);
        GraphDatabaseService graphdb = neo4j.graphDB;

        System.out.println(neo4j.getNumberofNodes() + "   " + neo4j.getNumberofEdges());
        neo4j.closeDB();

        int num_node = 0, num_edge = 0;
        ArrayList<String> ss = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(nodeFilePath));
            String line = null;
            while ((line = br.readLine()) != null) {

                ss.add(line);

                if (num_node % 50000 == 0) {
                    System.out.println(num_node + " nodes was created");
                    process_batch_nodes(ss, sub_db_name);
                    ss.clear();
                }
                num_node++;
            }
            process_batch_nodes(ss, sub_db_name);
            ss.clear();
            System.out.println(num_node + " edges were created");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("===============================================================");
        System.out.println("there are # of nodes that are created (" + num_node + ")");
        System.out.println("===============================================================");


        neo4j = new Neo4jDB(sub_db_name);
        System.out.println("====================================================================");
        neo4j.startDB(false);
        graphdb = neo4j.graphDB;

        HashMap<Pair<Integer, Integer>, double[]> edges = new HashMap<>(num_node * 3, 0.9f);

        HashSet<ImmutablePair<Integer, Integer>> created_pair_nodes = new HashSet<ImmutablePair<Integer, Integer>>(num_node * 3, 0.9f);

        BufferedReader br = null;
        int line_number = 0;
        try {
            br = new BufferedReader(new FileReader(EdgeFilePath));
            String line = null;
            ss.clear();
            while ((line = br.readLine()) != null) {
                int sid = Integer.parseInt(line.split(" ")[0]);
                int did = Integer.parseInt(line.split(" ")[1]);
                double c1 = Double.parseDouble(line.split(" ")[2]);
                double c2 = Double.parseDouble(line.split(" ")[3]);
                double c3 = Double.parseDouble(line.split(" ")[4]);

                c1 = c1 <= 0 ? 1 : c1;
                c2 = c2 <= 0 ? 1 : c2;
                c3 = c3 <= 0 ? 1 : c3;

                double[] costs = new double[]{c1, c2, c3};
                Pair<Integer, Integer> relations = new MutablePair<>(sid, did);


                if (c1 <= 0 || c2 <= 0 || c3 <= 0) {
                    System.out.println("============================+++++++++++++++++++++++  generated wrong weights of the edges ");
                }


                StringBuilder str = new StringBuilder();
                str.append(sid).append(" "); //sid
                str.append(did).append(" "); //did
                str.append(c1).append(" "); //c1
                str.append(c2).append(" "); //c2
                str.append(c3); //c3
                ss.add(str.toString());


                edges.put(relations, costs);
                if (line_number % 10000 == 0) {
                    process_batch_edges(ss, graphdb);
                    ss.clear();
                    System.out.println(line_number + " edges were created");
                }

                line_number++;
            }

            process_batch_edges(ss, graphdb);
            ss.clear();
            System.out.println(line_number + " edges were created");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("there are total " + num_node + " nodes and " + line_number + " edges" + " and undirected edges ");
        System.out.println("====================================================================");

        System.out.println(" # of nodes in the db : " + neo4j.getNumberofNodes() + "   # of edges in the DB: " + neo4j.getNumberofEdges());

        neo4j.closeDB();
    }

    private void process_batch_nodes(ArrayList<String> ss, GraphDatabaseService graphdb) {
        try (Transaction tx = graphdb.beginTx()) {
            for (String line : ss) {
                String[] attrs = line.split(" ");
                String id = attrs[0];
                double lat = Double.parseDouble(attrs[1]);
                double log = Double.parseDouble(attrs[2]);
                createNode(id, lat, log, graphdb);
            }
            tx.success();
        }
    }

    private void process_batch_nodes(ArrayList<String> ss, String db_name) {

        Neo4jDB neo4j = new Neo4jDB(db_name);
        neo4j.startDB(false);

        GraphDatabaseService graphdb = neo4j.graphDB;
        try (Transaction tx = graphdb.beginTx()) {
            for (String line : ss) {
                String[] attrs = line.split(" ");
                String id = attrs[0];
                double lat = Double.parseDouble(attrs[1]);
                double log = Double.parseDouble(attrs[2]);
                createNode(id, lat, log, graphdb);
            }
            tx.success();
        }
        neo4j.closeDB();
    }

    private Node createNode(String id, double lat, double log, GraphDatabaseService graphdb) {
        Node n = graphdb.createNode(BNode.BusNode);
        n.setProperty("name", id);
        n.setProperty("lat", lat);
        n.setProperty("log", log);
        if (n.getId() != Long.valueOf(id)) {
            System.out.println("id not match  " + n.getId() + " ->  " + id);
            System.exit(0);

        }
        return n;
    }

    private void process_batch_edges(ArrayList<String> ss, GraphDatabaseService graphdb) {
        try (Transaction tx = graphdb.beginTx()) {
            for (String line : ss) {
                String attrs[] = line.split(" ");
                String src = attrs[0];
                String des = attrs[1];
                double EDistence = Double.parseDouble(attrs[2]);
                double MetersDistance = Double.parseDouble(attrs[3]);
                double RunningTime = Double.parseDouble(attrs[4]);
                createRelation(src, des, EDistence, MetersDistance, RunningTime, graphdb);
            }
            tx.success();
        }
    }


    private void createRelation(String src, String des, double eDistence, double metersDistance, double runningTime, GraphDatabaseService graphdb) {
        try {
            Node srcNode = graphdb.getNodeById(Long.valueOf(src));
            Node desNode = graphdb.getNodeById(Long.valueOf(des));

            Relationship rel = srcNode.createRelationshipTo(desNode, Line.Linked);
            rel.setProperty("EDistence", eDistence);
            rel.setProperty("MetersDistance", metersDistance);
            rel.setProperty("RunningTime", runningTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
