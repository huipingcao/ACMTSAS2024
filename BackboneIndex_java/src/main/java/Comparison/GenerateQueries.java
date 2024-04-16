package Comparison;

import Neo4jTools.Neo4jDB;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.GraphDatabaseService;
import utilities.ParsedOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GenerateQueries {

    public void generateQueries(String db_name, int number_of_quiers) {
        ArrayList<Pair<Long, Long>> query_list = new ArrayList<>();

        Neo4jDB neo4j = new Neo4jDB(db_name);
        neo4j.startDB(true);
        GraphDatabaseService graphdb = neo4j.graphDB;
        System.out.println(neo4j.DB_PATH + "  number of nodes:" + neo4j.getNumberofNodes() + "   number of edges : " + neo4j.getNumberofEdges());
        ArrayList<Long> node_list = new ArrayList<>(neo4j.getNodes());

        for (int i = 0; i < number_of_quiers; i++) {
            long start_id = getRandomNodes(node_list);
            long end_id = getRandomNodes(node_list);
            Pair<Long, Long> q = new MutablePair<>(start_id, end_id);
            while (start_id == end_id || query_list.contains(q)) {
                start_id = getRandomNodes(node_list);
                end_id = getRandomNodes(node_list);
                q = new MutablePair<>(start_id, end_id);
            }
            query_list.add(q);
        }

        neo4j.closeDB();

        File querys_f = new File("queries.txt");

        if (querys_f.exists()) {
            querys_f.delete();
        }

        FileWriter fw = null;
        try {
            fw = new FileWriter(querys_f);
            for (Pair<Long, Long> q : query_list) {
                fw.write(q.getKey() + " " + q.getValue() + "\n");
                System.out.println("[" + q.getKey() + " ," + q.getValue() + "]");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T getRandomNodes(ArrayList<T> nodelist) {
        Random r = new Random();
        int idx = r.nextInt(nodelist.size());
        return nodelist.get(idx);
    }
}
