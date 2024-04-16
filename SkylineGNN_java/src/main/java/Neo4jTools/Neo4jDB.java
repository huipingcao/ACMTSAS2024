package Neo4jTools;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import utilities.ParsedOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;



public class Neo4jDB {
    public GraphDatabaseService graphDB;
    public String DB_PATH;
    public static ArrayList<String> propertiesName = new ArrayList<>();
    public String dbname;


    public static void main(String args[]) {
        String sub_db_name = "sub_ny_USA_Level0";
//        String dbconfPath = "/home/hchen/IntelliJProjects/java_SkylineGNN/conf/neo4j.conf";

        Neo4jDB neo4j = new Neo4jDB(sub_db_name);
        neo4j.startDB(false);
        long pre_n = neo4j.getNumberofNodes();
        long pre_e = neo4j.getNumberofEdges();
        System.out.println(pre_n + "  " + pre_e);
        neo4j.closeDB();
    }

    public Neo4jDB() {
        this.DB_PATH = ParsedOptions.neo4jdbPath;
        System.out.println(this.DB_PATH);

    }

    public Neo4jDB(String subDBName) {
        this.DB_PATH = ParsedOptions.neo4jdbPath + "/" + subDBName + "/databases/graph.db";
        this.dbname = subDBName;
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    public void startDB(boolean getProperties) {

        GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(this.DB_PATH)).loadPropertiesFromFile("/home/hchen/IntelliJProjects/java_SkylineGNN/conf/neo4j.conf");
//        GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(this.DB_PATH)).loadPropertiesFromFile(dbconfPath);
//        builder.loadPropertiesFromFile(conFile)
//        builder.setConfig(GraphDatabaseSettings.pagecache_memory, "8G");
        this.graphDB = builder.newGraphDatabase();

        if (this.graphDB != null) {
//            registerShutdownHook(graphDB);
        } else {
            System.out.println("connect to neo4j DB (" + this.DB_PATH + ") failure !!!!");
            System.exit(0);
        }

        if (getProperties) {
//            this.getPropertiesName();
            propertiesName.clear();
            propertiesName.add("EDistence");
            propertiesName.add("MetersDistance");
            propertiesName.add("RunningTime");
        }

//        registerShutdownHook(this.graphDB);
    }

    public void closeDB() {
//        System.out.println("The DB is shutdown, " + this.graphDB);
        this.graphDB.shutdown();
    }

    public void deleleDB() {
        try {
            File f = new File(this.DB_PATH);
            if (f.exists()) {
                FileUtils.deleteDirectory(new File(this.DB_PATH));
                System.out.println("Delete the neo4j db (" + this.DB_PATH + ") success !!!!");
            }
        } catch (IOException e) {
            System.err.println("Delete the neo4j db (" + this.DB_PATH + ") fail !!!!");
        }
    }

    public long getNumberofEdges() {
        long result = 0;
        try (Transaction tx = this.graphDB.beginTx()) {
            ResourceIterable<Relationship> r = this.graphDB.getAllRelationships();
            tx.success();
            result = r.stream().count();
        }
        return result;
    }

    public long getNumberofNodes() {
        long result = 0;
        try (Transaction tx = this.graphDB.beginTx()) {
            ResourceIterable<Node> n = this.graphDB.getAllNodes();
            tx.success();
            result = n.stream().count();
        }
        return result;
    }

    public Node getRandomNode() {
        try (Transaction tx = this.graphDB.beginTx()) {
            ResourceIterable<Node> nIterable = this.graphDB.getAllNodes();
            ResourceIterator<Node> nIter = nIterable.iterator();

            if (nIter.hasNext()) {
                Node n = nIter.next();
                tx.success();
                return n;
            } else {
                tx.success();
                return null;
            }
        }
    }

    private int getRandomIntNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public ArrayList<Long> getNeighborsIdList(long id) {
        ArrayList<Long> result = new ArrayList<>();
        try (Transaction tx = this.graphDB.beginTx()) {
            Node sn = graphDB.getNodeById(id);

            Iterable<Relationship> relIterable = sn.getRelationships(Direction.BOTH);
            Iterator<Relationship> relIter = relIterable.iterator();

            while (relIter.hasNext()) {
                Relationship rel = relIter.next();
                result.add(rel.getOtherNode(sn).getId());
            }
            tx.success();
        }
        return result;
    }

    public ArrayList<Long> getNeighborsRelIdList(long node_id) {
        HashSet<Long> result = new HashSet<>();
        try (Transaction tx = this.graphDB.beginTx()) {
            Node sn = graphDB.getNodeById(node_id);

            Iterable<Relationship> relIterable = sn.getRelationships(Direction.BOTH);
            Iterator<Relationship> relIter = relIterable.iterator();

            while (relIter.hasNext()) {
                Relationship rel = relIter.next();
                result.add(rel.getId());
            }
            tx.success();
        }
        ArrayList<Long> returned_list = new ArrayList<>();
        returned_list.addAll(result);
        return returned_list;
    }


    public HashSet<Relationship> getNeighborsRelObjList(long node_id) {
        HashSet<Relationship> result = new HashSet<>();
        try (Transaction tx = this.graphDB.beginTx()) {
            Node sn = graphDB.getNodeById(node_id);

            Iterable<Relationship> relIterable = sn.getRelationships(Direction.BOTH);
            Iterator<Relationship> relIter = relIterable.iterator();

            while (relIter.hasNext()) {
                Relationship rel = relIter.next();
                result.add(rel);
            }
            tx.success();
        }
        return result;
    }

    public ArrayList<Node> getNeighborsNodeList(long id) {
        ArrayList<Node> result = new ArrayList<>();
        try (Transaction tx = this.graphDB.beginTx()) {

            Node sn = graphDB.getNodeById(id);

            Iterable<Relationship> relIterable = sn.getRelationships(Direction.BOTH);
            Iterator<Relationship> relIter = relIterable.iterator();

            while (relIter.hasNext()) {
                Relationship rel = relIter.next();
                result.add(rel.getOtherNode(sn));
            }
            tx.success();
        }
        return result;
    }


    public Node getoutgoingNode(Relationship rel, Node v) {
        Node en = null;
        if (rel.getEndNode().getId() == v.getId()) {
            en = rel.getStartNode();
        } else {
            en = rel.getEndNode();
        }
        return en;
    }


    public ArrayList<Relationship> getoutgoingEdge(Node v) {
        ArrayList<Relationship> relationships = new ArrayList<>();
        try (Transaction tx = this.graphDB.beginTx()) {
            Iterable<Relationship> rel_Iterable = v.getRelationships(Direction.BOTH);
            Iterator<Relationship> rel_Iter = rel_Iterable.iterator();
            while (rel_Iter.hasNext()) {
                Relationship rel = rel_Iter.next();
                relationships.add(rel);
            }
            tx.success();
        }
        return relationships;
    }

    public void listallEdges() {
        try (Transaction tx = this.graphDB.beginTx()) {
            ResourceIterable<Relationship> rel_Iterable = graphDB.getAllRelationships();
            ResourceIterator<Relationship> rel_Iter = rel_Iterable.iterator();
            while (rel_Iter.hasNext()) {
                Relationship rel = rel_Iter.next();
                System.out.println(rel);
            }
            tx.success();
        }
    }

    public void listallNodes() {
        try (Transaction tx = this.graphDB.beginTx()) {
            ResourceIterable<Node> n_Iterable = graphDB.getAllNodes();
            ResourceIterator<Node> n_Iter = n_Iterable.iterator();
            while (n_Iter.hasNext()) {
                Node n = n_Iter.next();
                System.out.println(n);
            }
            tx.success();
        }
    }

    public long getRelationShipByStartAndEndNodeID(long nodeid, long next_node_id) {
//        System.out.println(this.graphDB);
        long rid = -1;
        Transaction tx = this.graphDB.beginTx();
        try {
            Node start_node = this.graphDB.getNodeById(nodeid);
            Iterable<Relationship> rel_Iterable = start_node.getRelationships(Direction.BOTH);
            Iterator<Relationship> rel_Iter = rel_Iterable.iterator();
            while (rel_Iter.hasNext()) {
                Relationship rel = rel_Iter.next();
                if (rel.getEndNodeId() == next_node_id || rel.getStartNodeId() == next_node_id) {
                    rid = rel.getId();
                    break;
                }
            }
            tx.success();
        } catch (NotFoundException e) {
            tx.success();
            return -1;
        }
        return rid;
    }

    public void getPropertiesName() {
        propertiesName.clear();
        try (Transaction tx = graphDB.beginTx()) {

            Iterable<Relationship> rels = graphDB.getNodeById(1).getRelationships(Line.Linked, Direction.BOTH);
            if (rels.iterator().hasNext()) {
                Relationship rel = rels.iterator().next();
//                System.out.println(rel);
                Map<String, Object> pnamemap = rel.getAllProperties();
//                System.out.println(pnamemap.size());
                for (Map.Entry<String, Object> entry : pnamemap.entrySet()) {
                    System.out.println(entry.getKey());
                    propertiesName.add(entry.getKey());
                }
            } else {
                System.err.println("There is no edge from or to this node " + graphDB.getNodeById(0).getId());
            }

//            System.out.println(propertiesName.size());
            tx.success();
        }
    }

    public void saveGraphToTextFormation(String textFilePath) {

        try (Transaction tx = graphDB.beginTx()) {

            File dataF = new File(textFilePath);
//            try {
//                FileUtils.deleteDirectory(dataF);
//                dataF.mkdirs();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            System.out.println(textFilePath);


            BufferedWriter Nodewriter = new BufferedWriter(new FileWriter(textFilePath + "/NodeInfo.txt"));
            BufferedWriter Edgewriter = new BufferedWriter(new FileWriter(textFilePath + "/SegInfo.txt"));

            //Get nodes' information and save
            ResourceIterable<Node> nodes_iterable = graphDB.getAllNodes();
            ResourceIterator<Node> nodes_iter = nodes_iterable.iterator();
            while (nodes_iter.hasNext()) {
                Node n = nodes_iter.next();
//                System.out.println(n.getId()+" "+n.getProperty("lat")+" "+n.getProperty("log"));
                Nodewriter.write(n.getId() + " " + n.getProperty("lat") + " " + n.getProperty("log") + "\n");
            }
            Nodewriter.close();


            //Get nodes' information and save
            ResourceIterable<Relationship> edges_iterable = graphDB.getAllRelationships();
            ResourceIterator<Relationship> edges_iter = edges_iterable.iterator();
            while (edges_iter.hasNext()) {
                Relationship r = edges_iter.next();
//                System.out.println(r.getStartNodeId()+" "+r.getEndNodeId()+" "+r.getProperty("EDistence")+" "+r.getProperty("MetersDistance")+" "+r.getProperty("RunningTime"));
                Edgewriter.write(r.getStartNodeId() + " " + r.getEndNodeId() + " " + r.getProperty("EDistence") + " " + r.getProperty("MetersDistance") + " " + r.getProperty("RunningTime") + "\n");
            }

            Edgewriter.close();
            tx.success();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashSet<Long> getNodes() {
        HashSet<Long> nodes_list = new HashSet<>();
        try (Transaction tx = this.graphDB.beginTx()) {
            ResourceIterator<Node> nodes_iter = this.graphDB.getAllNodes().iterator();
            while (nodes_iter.hasNext()) {
                nodes_list.add(nodes_iter.next().getId());
            }
            tx.success();
        }
        return nodes_list;
    }

    public HashSet<Long> getEdges() {
        HashSet<Long> edges_list = new HashSet<>();
        try (Transaction tx = this.graphDB.beginTx()) {
            ResourceIterator<Relationship> edges_iter = this.graphDB.getAllRelationships().iterator();
            while (edges_iter.hasNext()) {
                edges_list.add(edges_iter.next().getId());
            }
            tx.success();
        }
        return edges_list;
    }

    /**
     * get the rels that the start node and end node are in the given nodes list
     *
     * @param nodes_list
     * @return
     */
    public HashSet<Long> getEdges(HashSet<Long> nodes_list) {
        HashSet<Long> result = new HashSet<>();
        try (Transaction tx = this.graphDB.beginTx()) {
            for (long n : nodes_list) {
                Iterator<Relationship> r = this.graphDB.getNodeById(n).getRelationships(Line.Linked, Direction.BOTH).iterator();
                while (r.hasNext()) {
                    Relationship rel = r.next();
                    if (nodes_list.contains(rel.getStartNodeId()) && nodes_list.contains(rel.getEndNodeId())) {
                        result.add(rel.getId());
                    }
                }
            }
            tx.success();
        }
        return result;
    }

    public boolean isGraphEmpty() {
        return getNumberofNodes() == 0;
    }

    public Node getNodeById(long node_id) {
        try (Transaction tx = graphDB.beginTx()) {
            Node n = graphDB.getNodeById(node_id);
            tx.success();
            return n;
        } catch (NotFoundException e) {
            return null;
        }
    }


    public int CheckNodeById(long node_id) {
        Transaction tx = graphDB.beginTx();
        try {
            graphDB.getNodeById(node_id);
            tx.success();
            return (int) node_id;
        } catch (NotFoundException e) {
            tx.success();
            return -1;
        }
    }

    public double[] getCostsOfRelationShip(Relationship rel) {
        double[] cost;
        try (Transaction tx = graphDB.beginTx()) {
            int n = propertiesName.size();
            cost = new double[n];

            for (int i = 0; i < n; i++) {
                String p_type = propertiesName.get(i);
                cost[i] = (double) rel.getProperty(p_type);
            }
            tx.success();
        }

        return cost;
    }


}
