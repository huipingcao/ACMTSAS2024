package utilities;

import Index.NodeCoefficient;
import Neo4jTools.Line;
import Neo4jTools.Neo4jDB;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.*;

import java.util.*;
import java.util.stream.Collectors;

public class GraphInformation {
    String db_name = "";
    TreeMap<Pair<Integer, Integer>, ArrayList<Long>> degree_pairs = new TreeMap(new PairComparator());

    public static void main(String args[]) throws ParseException {
        GraphInformation gi = new GraphInformation();
//        gi.loadParameters(args);

        for (int i = 0; i <= 12; i++) {
            gi.db_name = "C9_NY_22K_Level" + i;
            gi.getDistributionOfDegree();
//            gi.getDegreePairs();
        }
//        gi.db_name = "C9_NY_22K_Leve8";
//        gi.getDistributionOfDegree();

    }

    /**
     * Calculated the degree pair of each edge,
     * one distinct degree pair p contains:
     * the list of the edges whose degree pair of the start node and end node is equal to the given key p
     */
    public void getDegreePairs() {
        Neo4jDB neo4j = new Neo4jDB(ParsedOptions.db_name);
        neo4j.startDB(true);
        GraphDatabaseService graphdb = neo4j.graphDB;
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("Statistic the distribution of the graph :" + graphdb);
        System.out.println(neo4j.DB_PATH + "  number of nodes:" + neo4j.getNumberofNodes() + "   number of edges : " + neo4j.getNumberofEdges());
        degree_pairs.clear();
        try (Transaction tx = graphdb.beginTx()) {
            ResourceIterable<Relationship> rels = graphdb.getAllRelationships();
            ResourceIterator<Relationship> rels_iter = rels.iterator();
            while (rels_iter.hasNext()) {
                Relationship r = rels_iter.next();
                int start_r = r.getStartNode().getDegree(Direction.BOTH);
                int end_r = r.getEndNode().getDegree(Direction.BOTH);

                if (start_r > end_r) {
                    int t = end_r;
                    end_r = start_r;
                    start_r = t;
                }

                Long rel_id = r.getId();
                Pair<Integer, Integer> p = new MutablePair<>(start_r, end_r);
                ArrayList<Long> a;
                if (this.degree_pairs.containsKey(p)) {
                    a = this.degree_pairs.get(p);
                } else {
                    a = new ArrayList<>();
                }
                a.add(rel_id);
                this.degree_pairs.put(p, a);
            }

            tx.success();
        }
        degree_pairs.forEach((k, v) -> {
            System.out.println(k + "   " + v.size());
        });
        neo4j.closeDB();
    }


    private boolean loadParameters(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("g", "graph_str", true, "the string of graph database");
        options.addOption("h", "help", false, "print the help of this command");
        options.addOption("d", "distributionDegree", false, "Get the degree distribution of the graph");


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String g_str = cmd.getOptionValue("g");


        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            String header = "Get the basic statistic information for the given graph:";
            formatter.printHelp("java -jar GraphInformation_by_area.jar", header, options, "", true);
            return false;
        } else {
            if (g_str == null) {
                db_name = "LFF_CAL_Level0";
            } else {
                db_name = "";
                String[] infors = g_str.split("_");
                for (int i = 0; i < infors.length; i++) {
                    if (i == 0 || i == 1) {
                        db_name += infors[i].toUpperCase() + "_";
                    } else if (i != infors.length - 1) {
                        db_name += infors[i] + "_";
                    } else {
                        db_name += infors[i];
                    }
                }
//                System.out.println(this.db_name);
            }


            if (cmd.hasOption("d")) {
                getDistributionOfDegree();
            }

            return true;
        }
    }

    public void getDistributionOfDegree() {

        Neo4jDB neo4j = new Neo4jDB(ParsedOptions.db_name);
        neo4j.startDB(true);
        GraphDatabaseService graphdb = neo4j.graphDB;
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("Statistic the distribution of the graph :" + graphdb);
        System.out.println(neo4j.DB_PATH + "  number of nodes:" + neo4j.getNumberofNodes() + "   number of edges : " + neo4j.getNumberofEdges());

        TreeMap<Integer, Integer> dist = new TreeMap<>();

        try (Transaction tx = graphdb.beginTx()) {
            ResourceIterator<Node> iter = graphdb.getAllNodes().iterator();
            while (iter.hasNext()) {
                Node n = iter.next();
                int degree = n.getDegree(Direction.BOTH);
                if (dist.containsKey(degree)) {
                    dist.put(degree, dist.get(degree) + 1);
                } else {
                    dist.put(degree, 1);
                }
            }
        }

        for (Map.Entry<Integer, Integer> e : dist.entrySet()) {
            System.out.println("degree: " + e.getKey() + " <----> # of nodes:" + e.getValue());
        }

        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        neo4j.closeDB();
    }

    public void getCoefficientDistribution(int type, boolean verb) {
        Neo4jDB neo4j = new Neo4jDB(ParsedOptions.db_name);
        neo4j.startDB(true);
        GraphDatabaseService graphdb = neo4j.graphDB;
        HashMap<Long, NodeCoefficient> node_coefficient_list = getNodesCoefficientList(graphdb);

        if (type == 0) {//two-hop neighbors
            TreeMap<Integer, Integer> node_neighbor_number_distribution = new TreeMap<>(); //the distribution of the two-neighbor coefficient
            for (Map.Entry<Long, NodeCoefficient> node_coeff : node_coefficient_list.entrySet()) {
                int key = node_coeff.getValue().getNumberOfTwoHopNeighbors();
                if (node_neighbor_number_distribution.containsKey(key)) {
                    node_neighbor_number_distribution.put(key, node_neighbor_number_distribution.get(key) + 1);
                } else {
                    node_neighbor_number_distribution.put(key, 1);
                }
            }
            printTreeMap(node_neighbor_number_distribution);
        } else if (type == 1) {
            TreeMap<Double, Integer> node_neighbor_number_distribution = new TreeMap<>(); //the distribution of the two-neighbor coefficient
            for (Map.Entry<Long, NodeCoefficient> node_coeff : node_coefficient_list.entrySet()) {
                double key = node_coeff.getValue().coefficient;
                if (node_neighbor_number_distribution.containsKey(key)) {
                    node_neighbor_number_distribution.put(key, node_neighbor_number_distribution.get(key) + 1);
                } else {
                    node_neighbor_number_distribution.put(key, 1);
                }
            }
            printTreeMap(node_neighbor_number_distribution);
        }

        if (verb) {
            node_coefficient_list = node_coefficient_list.entrySet()
                    .stream()
                    .sorted((i1, i2)
                            -> {
                        if (type == 1) {
                            if (i1.getValue().coefficient - i2.getValue().coefficient == 0) {
                                return 0;
                            } else {
                                return i1.getValue().coefficient - i2.getValue().coefficient > 0 ? 1 : -1;
                            }
                        } else {
                            if (i1.getValue().getNumberOfTwoHopNeighbors() - i2.getValue().getNumberOfTwoHopNeighbors() == 0) {
                                return 0;
                            } else {
                                return i1.getValue().getNumberOfTwoHopNeighbors() - i2.getValue().getNumberOfTwoHopNeighbors() > 0 ? 1 : -1;
                            }
                        }
                    })
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));

            for (Map.Entry<Long, NodeCoefficient> e : node_coefficient_list.entrySet()) {
                if (type == 0) {
                    System.out.println(e.getKey() + " - " + e.getValue().getNumberOfTwoHopNeighbors());

                } else if (type == 1) {
                    System.out.println(e.getKey() + " - " + e.getValue().coefficient);
                }
            }
        }

        neo4j.closeDB();
    }

    private <T> void printTreeMap(TreeMap<T, Integer> node_neighbor_number_distribution) {
        System.out.println("number of distinct value: " + node_neighbor_number_distribution.size());
        int index = 1;
        for (Map.Entry<T, Integer> e : node_neighbor_number_distribution.entrySet()) {
            String key_str="";
            if (e.getKey() instanceof Double) {
                key_str = String.format("%.3f", e.getKey());
            }else {
                key_str = String.valueOf(e.getKey());
            }
            System.out.println(index++ + " : " + key_str + " =====> " + e.getValue());
        }
    }

    public HashMap<Long, NodeCoefficient> getNodesCoefficientList(GraphDatabaseService graphdb) {
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

//                if (coefficient2 == 0 && num_connected_one_with_sec_neighbors!=0) {
//                    System.out.println(coefficient2 + "," + neighbors.size() + "," + second_neighbors.size()+","+num_connected_one_with_sec_neighbors);
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
