package Index.Operations;

import Index.ClusterSpanningTree;
import Index.NodeCluster;
import Index.NodeClusters;
import Index.PairComparator;
import Neo4jTools.BNode;
import Neo4jTools.Line;
import Neo4jTools.Neo4jDB;
import Query.BackBoneIndex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.*;
import utilities.*;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class updatesTest {

    private String index_files_folder;
    private String db_name;
    private String back_db_name;
    private int index_level;
    private int graph_level;
    private int target_idx_level;
    private boolean is_highest_graph_empty;
    private String target_db_name;

    HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Long>>>> cluster_info = new HashMap<>(); // level==> <sub_level, <cluster_id, List of nodes>>
    HashMap<Integer, HashMap<Integer, ArrayList<singlePath>>> single_edge_info = new HashMap<>(); // level==> <sub_level, <cluster_id, List of nodes>>
    HashMap<Integer, ArrayList<ArrayList<Long>>> single_segement_info = new HashMap<>(); // level==> <sub_level, <cluster_id, List of nodes>>

    TreeMap<Pair<Integer, Integer>, HashSet<Long>> degree_pairs = new TreeMap(new PairComparator());
    private Neo4jDB neo4j;
    private String previous_db_name;


    public updatesTest() {
        this.db_name = ParsedOptions.db_name;
        this.index_files_folder = ParsedOptions.indexFolder + "/" + db_name;
    }

    public static void main(String args[]) {
//        ParsedOptions.indexFolder = ParsedOptions.indexFolder + ParsedOptions.db_name;
//        BackboneIndexUpdateVersion backboneupdate = new BackboneIndexUpdateVersion();
//        backboneupdate.build();

        updatesTest update = new updatesTest();
        update.copyIndexFolder();
        update.addNewVertexTest();
    }

    private void copyIndexFolder() {
        String index_backup_folder = ParsedOptions.indexFolder + db_name + "_backup";
        String target_folder = ParsedOptions.indexFolder + db_name;

        File src_db_folder = new File(index_backup_folder);
        File dest_db_folder = new File(target_folder);

        System.out.println(src_db_folder);
        System.out.println(dest_db_folder);

        try {


            if (dest_db_folder.exists()) {
                FileUtils.deleteDirectory(dest_db_folder);
            }

            FileUtils.copyDirectory(src_db_folder, dest_db_folder);
            System.out.println("copy db from " + src_db_folder + " to " + dest_db_folder);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addNewVertexTest() {
        this.back_db_name = copyToBackUpDB(0);
        long new_n_id = getNewNode();

        this.neo4j = new Neo4jDB(this.back_db_name);
        neo4j.startDB(true);

        this.index_level = getlevel();
        this.graph_level = this.index_level + 1;
        this.target_idx_level = this.index_level;

        setHighestGraphEmpty();
        setHighestIndex();

        System.out.println("index Level:" + this.target_idx_level + "   graph Level:" + this.target_db_name);
        try (Transaction tx = neo4j.graphDB.beginTx()) {

            InitClusterList();
            System.out.println("=====================================================================");
            InitSingleEdgeBefore();
            System.out.println("=====================================================================");
            InitSingleSegment();
            System.out.println("=====================================================================");


            long start_update = System.currentTimeMillis();
            CheckClusterNodeList(new_n_id, neo4j);
            System.out.println((System.currentTimeMillis() - start_update) + "ms");

            tx.success();
        }

        neo4j.closeDB();


    }

    private void CheckClusterNodeList(long new_n_id, Neo4jDB neo4j) {
        HashSet<Long> affect_node_list = new HashSet<>();

        for (int i = 0; i <= this.target_idx_level; i++) {

            System.out.println(i + " ===========================");

            ArrayList<Long> n_ids = new ArrayList<>();
            try {
                Node new_Node = neo4j.graphDB.getNodeById(new_n_id);
                if (new_Node != null) {
                    Iterator<Relationship> rels = new_Node.getRelationships(Direction.BOTH).iterator();
                    while (rels.hasNext()) {
                        Node n_node = neo4j.graphDB.getNodeById(rels.next().getOtherNodeId(new_n_id));
                        System.out.println(n_node);
                        n_ids.add(n_node.getId());
                    }
                }
            } catch (NotFoundException e) {
                System.out.println("There is no neighbor nodes for the new created node with IDs " + new_n_id);
            }

            this.previous_db_name = copyToBackUpDB();
            Neo4jDB p_neo4j = new Neo4jDB(this.previous_db_name);
            p_neo4j.startDB(true);


            for (int j = 0; j < this.cluster_info.get(i).size(); j++) {
                HashSet<Long> sub_step_deletedNodes = new HashSet<>();
                HashSet<Long> sub_step_deletedEdges = new HashSet<>();
                HashSet<Long> sub_step_updated_index_nodes = new HashSet<>();

                if (this.single_edge_info.get(i) != null && this.single_edge_info.get(i).get(j + 1) != null) {
                    ArrayList<singlePath> s_paths = this.single_edge_info.get(i).get(j + 1);
                    for (singlePath p : s_paths) {
                        boolean hasNodes = false;
                        for (long node_id : n_ids) {
                            if (p.node_list.contains(node_id)) {
                                hasNodes = true;
                            }
                        }

                        if (hasNodes) {
                            HashSet<Long> deleted_nodes = new HashSet<>(p.node_list);
                            deleted_nodes.add(new_n_id);
                            HashSet<Long> remained_nodes = new HashSet<>(p.remained_nodes);
                            HashSet<Long> deleted_edges = neo4j.getEdges(deleted_nodes);
                            indexBuildSingleEdgesAtLevel(neo4j, i, deleted_edges, deleted_nodes, remained_nodes);

                            for (long rel_id : deleted_edges) {
                                neo4j.graphDB.getRelationshipById(rel_id).delete();
                                sub_step_deletedEdges.add(rel_id);
                            }

                            for (long n_id : deleted_nodes) {
                                if (neo4j.graphDB.getNodeById(n_id).getDegree(Direction.BOTH) == 0) {
                                    neo4j.graphDB.getNodeById(n_id).delete();
                                    sub_step_deletedNodes.add(n_id);
                                }
                            }
                        }
                    }
                }

                HashSet<Long> single_node_list = new HashSet<>(sub_step_deletedNodes);

                System.out.println("Checked the single edges at:" + i + " sub_step:" + j);

                NodeClusters clusters = getAffectedClusters(affect_node_list, i, j + 1);

                /***** new cluster ****/
                if (!n_ids.isEmpty()) {
                    int target_cluster_id = getTargetClusterIDs(new HashSet<>(n_ids), i, j + 1, true).get(0);

                    if (target_cluster_id != -1) {
                        HashSet<Long> nodeList = this.cluster_info.get(i).get(j + 1).get(target_cluster_id);
                        NodeCluster cluster = new NodeCluster(target_cluster_id);
                        HashSet<Long> current_nodes = getNodeListAtCurrentLevel();

                        removeExistedIndex(i, nodeList);

                        for (long nid : this.cluster_info.get(i).get(j + 1).get(target_cluster_id)) {
                            if (current_nodes.contains(nid)) {
                                cluster.addToCluster(nid); //add all the nodes of clusters
                            }
                        }

                        cluster.addToCluster(new_n_id);
                        if (!clusters.clusters.containsKey(target_cluster_id)) {
                            clusters.clusters.put(target_cluster_id, cluster);
                        }
                    }
                }

                affect_node_list.clear();

                for (Map.Entry<Integer, NodeCluster> e : clusters.clusters.entrySet()) {
                    int k = e.getKey();

                    if (k == 0) {
                        continue;
                    }

                    NodeCluster v = e.getValue();

                    System.out.print("Cluster :" + k + "  nodes_size:" + v.node_list.size() + " border_size:" + v.border_node_list.size());
                    ClusterSpanningTree tree = new ClusterSpanningTree(neo4j, true, v.node_list);
                    tree.EulerTourStringWiki();
                    int num_removed_edges = tree.rels.size() - tree.SpTree.size();
                    System.out.println("     size of spanning tree : " + tree.SpTree.size() + "      # of rels:" + tree.rels.size() + " ---- removed " + (num_removed_edges) + " edges");
                    System.out.println("--------------------------------------------------------------------------------------------------------------");

                    if(num_removed_edges==0){
                        continue;
                    }

                    v.addRels(tree.rels);
                    v.num_removed_edges = num_removed_edges;

                    try (Transaction tx = neo4j.graphDB.beginTx()) {
                        for (long rel_id : tree.rels) {
                            Relationship rel = neo4j.graphDB.getRelationshipById(rel_id);
                            if (!tree.SpTree.contains(rel.getId())) {
                                sub_step_deletedEdges.add(rel.getId());
                                deleteRelationshipFromDB(rel, sub_step_deletedNodes, neo4j);
                            }
                        }
                        tx.success();
                    }

                    getDegreePairs();
                    HashSet<Long> cluster_deletedEdges = removeSingletonEdgesInCluster(v.cluster_id, v, sub_step_deletedNodes);
                    sub_step_deletedEdges.addAll(cluster_deletedEdges);

                    HashSet<Long> updated_index_nodes = indexBuildAtLevel(i, sub_step_deletedEdges, v, p_neo4j);
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + v.node_list.size());
                    affect_node_list.addAll(v.node_list);
                    sub_step_updated_index_nodes.addAll(updated_index_nodes);
                }

                updateSingleNodeIndexes(i, single_node_list, sub_step_updated_index_nodes);

                if (j != 0) {
                    updateExistedIndexInCurrentLevel(i, sub_step_updated_index_nodes);
                }
            }

            if (single_segement_info.get(i) != null) {
                try (Transaction tx = neo4j.graphDB.beginTx()) {
                    HashSet<Long> updated_node_list = new HashSet<>();
                    System.out.println("Process single segments at level " + i + "  " + neo4j.getNumberofNodes() + " " + neo4j.getNumberofEdges());

                    for (ArrayList<Long> s : single_segement_info.get(i)) {
                        boolean hasNodes = false;
                        for (long node_id : affect_node_list) {
                            if (s.contains(node_id)) {
                                hasNodes = true;
                                break;
                            }
                        }

                        if (hasNodes) {
                            Segment segment = new Segment();

                            segment.normal_node_list.addAll(s);

                            segment.normal_node_list.remove(0);
                            segment.normal_node_list.remove(segment.normal_node_list.size() - 1);

                            segment.start_node = s.get(0);
                            segment.end_node = s.get(s.size() - 1);

                            segment.updateRels(neo4j);

                            removeExistedIndex(i, segment);
//
                            if (!segment.hasNoExistingEdges(neo4j)) {
//                                System.out.println("####" + segment.normal_node_list);
                                segment.updateCosts(neo4j);
                                HashMap<Long, HashMap<Long, ArrayList<double[]>>> nodes_in_segment_index = segment.buildIndexForNodes(neo4j, false);
                                long new_rel_id = segment.contract(neo4j);
                                for (long n_id : nodes_in_segment_index.keySet()) {
                                    updated_node_list.add(n_id);
                                }
                                insertIndex(i, nodes_in_segment_index);
                            } else {
//                                System.out.println(s.size() + "$$$" + s);
//
//                                System.out.println(segment.normal_node_list.size() + "@@@" + segment.normal_node_list);
//                                System.out.println(segment.rels.size() + "###" + segment.rels);

                                ArrayList<Segment> splited_segements = segment.split(neo4j);
                                for (Segment s1 : splited_segements) {
                                    s1.updateRels(neo4j);
//                                    System.out.println("===" + s1.start_node + "," + s1.normal_node_list + "," + s1.end_node);
//                                    System.out.println("~~~" + s1.rels);

                                    s1.updateCosts(neo4j);
                                    HashMap<Long, HashMap<Long, ArrayList<double[]>>> nodes_in_segment_index = s1.buildIndexForNodes(neo4j, false);
                                    long new_rel_id = s1.contract(neo4j);
                                    for (long n_id : nodes_in_segment_index.keySet()) {
                                        updated_node_list.add(n_id);
                                    }
                                    insertIndex(i, nodes_in_segment_index);
                                }
                            }
                        }
                    }

                    if (updated_node_list.size() != 0) {
                        updateExistedIndexInCurrentLevel(i, updated_node_list);
                    }

                    tx.success();
                }
            }

            p_neo4j.closeDB();
        }
    }

    private void removeExistedIndex(int level, HashSet<Long> node_list) {
//        System.out.println("remove the created edge indexes at level " + level + " .......................................");
        String index_folder = ParsedOptions.indexFolder + db_name + "/level" + level;


        for (long n_id : node_list) {
            File idx_file = new File(index_folder + "/" + n_id + ".idx");

            if (!idx_file.exists()) {
                continue;
            }

            HashMap<Long, ArrayList<double[]>> updated_skyline = new HashMap<>();

            HashMap<Long, ArrayList<double[]>> skylines = readIndex(n_id, level);

            if (skylines == null) {
                continue;
            }

            for (long highway : node_list) {
                skylines.remove(highway);
            }


            if (skylines != null && !skylines.isEmpty()) {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath()));
                    for (Map.Entry<Long, ArrayList<double[]>> e : updated_skyline.entrySet()) {
                        long target_node_id = e.getKey();
                        if (target_node_id == n_id) {
                            continue;
                        }
                        ArrayList<double[]> costs = e.getValue();
                        for (double[] c : costs) {
                            writer.write(target_node_id + " " + c[0] + " " + c[1] + " " + c[2] + "\n");
                        }
                    }

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                idx_file.delete();
            }
        }
    }

    private void removeExistedIndex(int level, Segment segment) {
//        System.out.println("remove the created edge indexes at level " + level + " .......................................");
        String index_folder = ParsedOptions.indexFolder + db_name + "/level" + level;


        for (long n_id : segment.normal_node_list) {
            File idx_file = new File(index_folder + "/" + n_id + ".idx");

            if (!idx_file.exists()) {
                continue;
            }

            HashMap<Long, ArrayList<double[]>> updated_skyline = new HashMap<>();

            HashMap<Long, ArrayList<double[]>> skylines = readIndex(n_id, level);

            if (skylines == null) {
                continue;
            }

            skylines.remove(segment.start_node);
            skylines.remove(segment.end_node);


            if (skylines != null && !skylines.isEmpty()) {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath()));
                    for (Map.Entry<Long, ArrayList<double[]>> e : updated_skyline.entrySet()) {
                        long target_node_id = e.getKey();
                        if (target_node_id == n_id) {
                            continue;
                        }
                        ArrayList<double[]> costs = e.getValue();
                        for (double[] c : costs) {
                            writer.write(target_node_id + " " + c[0] + " " + c[1] + " " + c[2] + "\n");
                        }
                    }

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                idx_file.delete();
            }
        }
    }

    private NodeClusters getAffectedClusters(HashSet<Long> affect_node_list, int level, int sub_step) {
        NodeClusters clusters = new NodeClusters();

        ArrayList<Integer> target_cluster_ids = new ArrayList<>();
        if (!affect_node_list.isEmpty()) {
            target_cluster_ids.addAll(getTargetClusterIDs(affect_node_list, level, sub_step, false));
            for (int c_id : target_cluster_ids) {
                NodeCluster c = new NodeCluster(c_id);
                HashSet<Long> current_nodes = getNodeListAtCurrentLevel();
                HashSet<Long> old_nodes = this.cluster_info.get(level).get(sub_step).get(c_id);

                removeExistedIndex(level, old_nodes);


                for (long nid : old_nodes) {
                    if (current_nodes.contains(nid)) {
                        c.addToCluster(nid); //add all the nodes of clusters
                    }
                }
                clusters.clusters.put(c_id, c);
                System.out.println("        add the cluster " + c_id + " to node_clusters, affected");
            }
        }

        return clusters;
    }

    private ArrayList<Integer> getTargetClusterIDs(HashSet<Long> n_ids, int level, int sub_level, boolean selectMax) {
        ArrayList<Integer> cluster_ids = new ArrayList<>();

        HashMap<Integer, HashSet<Long>> clusters = this.cluster_info.get(level).get(sub_level);
        HashMap<Integer, Integer> cluster_dist = new HashMap<>();

        for (Map.Entry<Integer, HashSet<Long>> cluster : clusters.entrySet()) {
            int cluster_id = cluster.getKey();

            if (cluster_id == 0) {
                continue;
            }

            for (long n_id : n_ids) {
                if (cluster.getValue().contains(n_id)) {
                    if (cluster_dist.containsKey(cluster_id)) {
                        cluster_dist.put(cluster_id, cluster_dist.get(cluster_id) + 1);
                    } else {
                        cluster_dist.put(cluster_id, 1);
                    }
                }
            }
        }

        if (selectMax) {
            int target_cluster_id = -1;
            int max_cluster = -1;
            for (Map.Entry<Integer, Integer> e : cluster_dist.entrySet()) {
                if (e.getValue() > max_cluster) {
                    max_cluster = e.getValue();
                    target_cluster_id = e.getKey();
                }
            }
            cluster_ids.add(target_cluster_id);
        } else {
            cluster_ids.addAll(cluster_dist.keySet());
        }

        return cluster_ids;
    }

    private void InitClusterList() {

        String cluster_folder = index_files_folder + "/clusters";
        for (int i = 0; i <= this.target_idx_level; i++) {
            File level_folder = new File(cluster_folder + "/" + i);
            System.out.println(level_folder.getAbsolutePath());

            int max_sub_level = 1;
            for (File sub_folder : level_folder.listFiles((dir, name) -> !name.endsWith(".idx"))) {
                if (max_sub_level < Integer.parseInt(sub_folder.getName())) {
                    max_sub_level = Integer.parseInt(sub_folder.getName());
                }
            }

            HashMap<Integer, HashMap<Integer, HashSet<Long>>> sub_level_cluster = new HashMap<>();

            for (int j = 1; j <= max_sub_level; j++) {
                File sub_cluster_folder = new File(level_folder + "/" + j);
                System.out.println("    " + sub_cluster_folder.getAbsolutePath());

                HashMap<Integer, HashSet<Long>> cluster = new HashMap<>();

                for (File cluster_f : sub_cluster_folder.listFiles((dir, name) -> !name.endsWith(".idx"))) {
//                    System.out.println("        ~~~~~~ "+cluster_f.getName());
                    BufferedReader reader;
                    String[] nodeList_str = null;
                    try {
                        reader = new BufferedReader(new FileReader(cluster_f));
                        String line = reader.readLine();
                        while (line != null) {
                            nodeList_str = line.split(" ");
                            // read next line
                            line = reader.readLine();
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    HashSet<Long> node_list = new HashSet<>();
                    if (nodeList_str != null) {
                        for (String str_node_id : nodeList_str) {
                            node_list.add(Long.parseLong(str_node_id));
                        }
                    }
                    cluster.put(Integer.parseInt(cluster_f.getName()), node_list);
//                    System.out.println(Integer.parseInt(cluster_f.getName())+"  "+node_list.size());
                }

                sub_level_cluster.put(j, cluster);
            }
            this.cluster_info.put(i, sub_level_cluster);
        }

    }

    private void InitSingleEdgeBefore() {

        String cluster_folder = index_files_folder + "/clusters";

        for (int i = 0; i <= this.target_idx_level; i++) {
            File level_folder = new File(cluster_folder + "/" + i);

            int max_sub_level = 1;
            for (File sub_folder : level_folder.listFiles((dir, name) -> !name.endsWith(".idx"))) {
                if (max_sub_level < Integer.parseInt(sub_folder.getName())) {
                    max_sub_level = Integer.parseInt(sub_folder.getName());
                }
            }

            HashMap<Integer, ArrayList<singlePath>> single_edges_sub_level = new HashMap<>();

            for (int j = 1; j <= max_sub_level; j++) {
                File sub_cluster_folder = new File(level_folder + "/" + j);

                for (File cluster_f : sub_cluster_folder.listFiles((dir, name) -> name.endsWith(".idx"))) {

                    ArrayList<singlePath> s_paths = new ArrayList<>();

                    System.out.println("        ~~~~~~ level " + i + "  sub_step:" + j + "   single edge file:" + cluster_f.getName());
                    BufferedReader reader;
                    String[] nodeList_str = null;
                    String[] r_node_str = null;

                    try {
                        reader = new BufferedReader(new FileReader(cluster_f));
                        String line = reader.readLine();
                        while (line != null) {
                            nodeList_str = line.split("\\|")[0].split(" ");
                            r_node_str = line.split("\\|")[1].trim().split(" ");
                            singlePath sp = new singlePath();

                            ArrayList<Long> node_list = new ArrayList<>();
                            ArrayList<Long> r_node = new ArrayList<>();

                            if (nodeList_str != null) {
                                for (String str_node_id : nodeList_str) {
                                    node_list.add(Long.parseLong(str_node_id));
                                }
                            }

                            if (r_node_str != null) {
                                for (String str_node_id : r_node_str) {
                                    r_node.add(Long.parseLong(str_node_id));
                                }
                            }

                            sp.node_list.addAll(node_list);
                            sp.remained_nodes.addAll(r_node);

                            s_paths.add(sp);

                            line = reader.readLine();
                        }
                        reader.close();

                        single_edges_sub_level.put(j, s_paths);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }

            this.single_edge_info.put(i, single_edges_sub_level);
        }

    }

    private void InitSingleSegment() {
        String cluster_folder = index_files_folder + "/clusters";

        for (int i = 0; i <= this.target_idx_level; i++) {
            File level_folder = new File(cluster_folder + "/" + i);
            ArrayList<ArrayList<Long>> s_paths = new ArrayList<>();

            for (File cluster_f : level_folder.listFiles((dir, name) -> name.endsWith(".idx"))) {
                System.out.println("        ~~~~~~ level " + i + "   single segment file:" + cluster_f.getName());
                BufferedReader reader;
                String[] nodeList_str = null;
                try {
                    reader = new BufferedReader(new FileReader(cluster_f));
                    String line = reader.readLine();
                    while (line != null) {
                        nodeList_str = line.split(" ");
                        ArrayList<Long> node_list = new ArrayList<>();
                        if (nodeList_str != null) {
                            for (String str_node_id : nodeList_str) {
                                node_list.add(Long.parseLong(str_node_id));
                            }
                        }

                        s_paths.add(node_list);

                        line = reader.readLine();
                    }
                    reader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.single_segement_info.put(i, s_paths);
            }
        }
    }

    private long getNewNode() {
        Neo4jDB neo4j = new Neo4jDB(this.back_db_name);
        neo4j.startDB(true);
        System.out.println(neo4j.graphDB);
        GraphDatabaseService graphdb = neo4j.graphDB;
        System.out.println(neo4j.getNumberofNodes() + "  " + neo4j.getNumberofEdges());

        long max_id = -1;

        double[] range_lng = new double[2];
        double[] range_lat = new double[2];

        range_lat[0] = range_lng[0] = Double.POSITIVE_INFINITY;
        range_lat[1] = range_lng[1] = Double.NEGATIVE_INFINITY;

        Node new_n;

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            ResourceIterator<Node> node_iter = neo4j.graphDB.getAllNodes().iterator();

            while (node_iter.hasNext()) {
                Node n = node_iter.next();
                if (n.getId() >= max_id) {
                    max_id = n.getId();
                }

                double lat = (double) n.getProperty("lat");
                double lng = (double) n.getProperty("log");

                if (lng > range_lng[1]) {
                    range_lng[1] = lng;
                }

                if (lng < range_lng[0]) {
                    range_lng[0] = lng;
                }

                if (lat > range_lat[1]) {
                    range_lat[1] = lat;
                }

                if (lat < range_lat[0]) {
                    range_lat[0] = lat;
                }

                if (n.getId() != Long.parseLong((String) n.getProperty("name"))) {
                    System.out.println("Error");
                }

            }

            long new_node_id = max_id + 1;
            double new_lat = getRandomNumberInRange(range_lat[0], range_lat[1]);
            double new_lng = getRandomNumberInRange(range_lng[0], range_lng[1]);

//            new_lat = 40.67050725740107;
//            new_lng = -74.33094292274396;

            new_n = graphdb.createNode(BNode.BusNode);
            new_n.setProperty("name", new_node_id);
            new_n.setProperty("lat", new_lat);
            new_n.setProperty("log", new_lng);

            System.out.println(new_n + " " + new_node_id + " [" + new_lat + " " + new_lng + "]");

//            int new_edges = (int) getRandomNumberInRange(0, 5);
            int new_edges = 5;

            node_iter = neo4j.graphDB.getAllNodes().iterator();

            Queue<Node> queue = new PriorityQueue<>((o1, o2) -> {
                double lat_n = (double) new_n.getProperty("lat");
                double lng_n = (double) new_n.getProperty("log");

                double lat_o1 = (double) o1.getProperty("lat");
                double lng_o1 = (double) o1.getProperty("log");

                double lat_o2 = (double) o2.getProperty("lat");
                double lng_o2 = (double) o2.getProperty("log");

                double d1 = Math.sqrt(Math.pow(lat_n - lat_o1, 2) + Math.pow(lng_n - lng_o1, 2));
                double d2 = Math.sqrt(Math.pow(lat_n - lat_o2, 2) + Math.pow(lng_n - lng_o2, 2));

                if (d1 > d2) {
                    return 1;
                } else if (d1 < d2) {
                    return -1;
                } else {
                    return 0;
                }
            });

            while (node_iter.hasNext()) {
                Node n = node_iter.next();
                if (n.getId() != new_node_id) {
                    queue.add(n);
                }
            }

            for (int i = 0; i < new_edges; i++) {
                Node nnnn = queue.poll();

                double lat_n = (double) new_n.getProperty("lat");
                double lng_n = (double) new_n.getProperty("log");

                double lat_o1 = (double) nnnn.getProperty("lat");
                double lng_o1 = (double) nnnn.getProperty("log");

                double d = Math.sqrt(Math.pow(lat_n - lat_o1, 2) + Math.pow(lng_n - lng_o1, 2));

                double cost1 = getGussianRandomValue(d * 2, d * 0.3) * 10000;
                double cost2 = getRandomNumberInRange(0, 100);
                double cost3 = getRandomNumberInRange(0, 100);


                Relationship rel = new_n.createRelationshipTo(nnnn, Line.Linked);
                rel.setProperty("EDistence", cost1);
                rel.setProperty("MetersDistance", cost2);
                rel.setProperty("RunningTime", cost3);
                System.out.println(nnnn + " " + d + " " + cost1 + " " + cost2 + " " + cost3);

            }

            tx.success();
        }


        neo4j.closeDB();

        return new_n.getId();
    }

    private String copyToBackUpDB(int level) {
        String src_db_name = this.db_name + "_Level" + level;
        String dest_db_name = this.db_name + "_Level" + level + "_backup";

        File src_db_folder = new File(ParsedOptions.neo4jdbPath + "/" + src_db_name);
        File dest_db_folder = new File(ParsedOptions.neo4jdbPath + "/" + dest_db_name);

        System.out.println(src_db_folder);
        System.out.println(dest_db_folder);

        try {


            File db_folder = new File(ParsedOptions.neo4jdbPath + "/" + src_db_name + "/databases/graph.db");
            File[] transaction_files = db_folder.listFiles(new TransactionFileFilter());
            for (File t_file : transaction_files) {
                System.out.println("Deleting the transaction file " + t_file.getAbsolutePath());
                t_file.delete();
            }

            if (dest_db_folder.exists()) {
                FileUtils.deleteDirectory(dest_db_folder);
            }
            FileUtils.copyDirectory(src_db_folder, dest_db_folder);
            System.out.println("copy db from " + src_db_folder + " to " + dest_db_folder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dest_db_name;
    }

    private String copyToBackUpDB() {
        String src_db_name = this.back_db_name;
        String dest_db_name = this.back_db_name + "_previous";

        File src_db_folder = new File(ParsedOptions.neo4jdbPath + "/" + src_db_name);
        File dest_db_folder = new File(ParsedOptions.neo4jdbPath + "/" + dest_db_name);

        System.out.println(src_db_folder);
        System.out.println(dest_db_folder);

        try {


            File db_folder = new File(ParsedOptions.neo4jdbPath + "/" + src_db_name + "/databases/graph.db");
            File[] transaction_files = db_folder.listFiles(new TransactionFileFilter());
            for (File t_file : transaction_files) {
                System.out.println("Deleting the transaction file " + t_file.getAbsolutePath());
                t_file.delete();
            }

            if (dest_db_folder.exists()) {
                FileUtils.deleteDirectory(dest_db_folder);
            }
            FileUtils.copyDirectory(src_db_folder, dest_db_folder);
            System.out.println("copy db from " + src_db_folder + " to " + dest_db_folder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dest_db_name;
    }

    private double getRandomNumberInRange(double min, double max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextDouble() * (max - min) + min;
    }

    private double getGussianRandomValue(double mean, double sd) {
        Random r = new Random();
        double value = r.nextGaussian() * sd + mean;

        while (value <= 0) {
            value = r.nextGaussian() * sd + mean;
        }

        return value;
    }

    private int getlevel() {
        int level = 0;
        File index_dir = new File(this.index_files_folder);
        for (File f : index_dir.listFiles(new BackBoneIndex.levelFileNameFilter())) {
            String fname = f.getName();
            int c_level = Integer.parseInt(fname.substring(fname.indexOf("level") + 5));
            if (c_level > level) {
                level = c_level;
            }
        }
        return level;
    }

    public void setHighestGraphEmpty() {
        String highest_db_name = this.db_name + "_Level" + graph_level;
        Neo4jDB neo4j = new Neo4jDB(highest_db_name);
        neo4j.startDB(false);
        this.is_highest_graph_empty = neo4j.isGraphEmpty();
        neo4j.closeDB();

        if (is_highest_graph_empty) {
            this.target_db_name = this.db_name + "_Level" + (this.graph_level - 1);
        } else {
            this.target_db_name = highest_db_name;
        }
    }

    private void setHighestIndex() {
//        File highest_folder = new File(index_files_folder + "/level" + this.index_level);
//        System.out.println(highest_folder.listFiles().length);

        if (is_highest_graph_empty) {
            target_idx_level = this.index_level - 1;
        } else {
            File highest_folder = new File(index_files_folder + "/level" + this.index_level);
            if (highest_folder.listFiles().length == 0) {
                this.target_db_name = this.db_name + "_Level" + (graph_level - 1);
                this.target_idx_level = this.index_level - 1;
            } else {
                this.target_db_name = this.db_name + "_Level" + graph_level;
                this.target_idx_level = this.index_level;
            }
        }
    }

    private void indexBuildSingleEdgesAtLevel(Neo4jDB neo4j, int level, HashSet<Long> sub_step_deletedEdges, HashSet<Long> sub_step_deletedNodes, HashSet<Long> remained_nodes) {
        System.out.println("update single edge index at level " + level + " ===============================================================  ");
        String sub_folder_str = ParsedOptions.indexFolder + db_name + "/level" + level;
        System.out.println("update the single index at " + sub_folder_str);


        //check the node remained in after the deletion (remained in current layer graph)
        try (Transaction tx = neo4j.graphDB.beginTx()) {
            for (long n_id : sub_step_deletedNodes) {
//                System.out.println(n_id);

                HashMap<Long, ArrayList<double[]>> skylines = readIndex(n_id, level);
//                if (skylines != null) {
//                    for (Map.Entry<Long, ArrayList<double[]>> e : skylines.entrySet()) {
//                        System.out.println(n_id + "-->" + e.getKey() + " ");
//                        for (double[] costs : e.getValue()) {
//                            System.out.println("        " + costs[0] + "," + costs[1] + "," + costs[2]);
//                        }
//                    }
//                }

                HashMap<Long, myQueueNode> tmpStoreNodes = new HashMap();
                myQueueNode snode = new myQueueNode(n_id, neo4j);
                myNodePriorityQueue mqueue = new myNodePriorityQueue();
                tmpStoreNodes.put(snode.id, snode);

                mqueue.add(snode);

                while (!mqueue.isEmpty()) {
                    myQueueNode v = mqueue.pop();
//                    System.out.println(v.id);
                    for (int i = 0; i < v.skyPaths.size(); i++) {
                        path p = v.skyPaths.get(i);
//                        System.out.println(p);
                        if (!p.expaned) {
                            p.expaned = true;
                            ArrayList<path> new_paths;
                            //expand only use the removed single edges
                            new_paths = p.expand(neo4j, sub_step_deletedEdges, sub_step_deletedEdges);
                            for (path np : new_paths) {
                                myQueueNode next_n;
//                                System.out.println("    " + np);
                                if (tmpStoreNodes.containsKey(np.endNode)) {
                                    next_n = tmpStoreNodes.get(np.endNode);
                                } else {
                                    next_n = new myQueueNode(snode, np.endNode, neo4j);
                                    tmpStoreNodes.put(next_n.id, next_n);
                                }

                                if (next_n.addToSkyline(np) && !next_n.inqueue) {
                                    mqueue.add(next_n);
                                    next_n.inqueue = true;
                                }
                            }
                        }
                    }
                }

                int sum = 0;
                for (Map.Entry<Long, myQueueNode> e : tmpStoreNodes.entrySet()) {
                    ArrayList<path> sk = e.getValue().skyPaths;
                    //remove the index of the self connection that node only has one skyline path and the skyline path is to itself
                    if (!(sk.size() == 1 && sk.get(0).costs[0] == 0 && sk.get(0).costs[1] == 0 && sk.get(0).costs[2] == 0)) {
                        for (path p : sk) {
                            if (remained_nodes.contains(p.endNode)) {
//                                System.out.println(e.getKey() + "    " + p);
                                sum++;
                            }
                        }
                    }
                }

//                System.out.println("sum:" + sum);

                if (sum != 0) {
                    /**clean the built index file*/
                    File idx_file = new File(sub_folder_str + "/" + n_id + ".idx");
                    System.out.println(sub_folder_str + "/" + n_id + ".idx");
                    //previous has the skyline information from n_id to other nodes (deleted in previous sub step)
                    BufferedWriter writer = new BufferedWriter(new FileWriter(idx_file));

                    for (Map.Entry<Long, myQueueNode> e : tmpStoreNodes.entrySet()) {
                        long target_node_id = e.getKey();
                        if (remained_nodes.contains(target_node_id)) {
//                            System.out.println("            ~~~" + target_node_id);
                            myQueueNode node_obj = e.getValue();
                            ArrayList<path> skys = node_obj.skyPaths;

                            if (skylines != null) {
                                if (skylines.containsKey(n_id)) {
                                    ArrayList<double[]> old_sky_costs = skylines.get(n_id);
                                    for (path p : skys) {
                                        if (p.endNode != n_id) {
                                            if (remained_nodes.size() != 0 && remained_nodes.contains(p.endNode)) { // not the max_level graph, have reminding nodes on next level
                                                addToSkyline(old_sky_costs, p.costs);
                                            }
                                        }
                                    }
                                } else {
                                    ArrayList<double[]> old_sky_costs = new ArrayList<>();
                                    for (path p : skys) {
                                        /** the end node of path is a highway, the node is still appear in next level, also, the path is not a dummy path of source node **/
                                        if (p.endNode != n_id) {
                                            if (remained_nodes.size() != 0 && remained_nodes.contains(p.endNode)) { // not the max_level graph, have reminding nodes on next level
                                                old_sky_costs.add(p.costs);
                                            }
                                        }
                                    }
                                    skylines.put(target_node_id, old_sky_costs);
                                }

                                for (Map.Entry<Long, ArrayList<double[]>> sk_e : skylines.entrySet()) {
                                    for (double[] c : sk_e.getValue()) {
                                        writer.write(sk_e.getKey() + " " + c[0] + " " + c[1] + " " + c[2] + "\n");
                                    }
                                }
                            } else {
                                for (path p : skys) {
                                    /** the end node of path is a highway, the node is still appear in next level, also, the path is not a dummy path of source node **/
                                    if (p.endNode != n_id) {
                                        if (remained_nodes.size() != 0 && remained_nodes.contains(p.endNode)) { // not the max_level graph, have reminding nodes on next level
                                            writer.write(target_node_id + " " + p.costs[0] + " " + p.costs[1] + " " + p.costs[2] + "\n");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    writer.close();
                }
            }
            tx.success();
        } catch (IOException e) {
            neo4j.closeDB();
            e.printStackTrace();
        }
    }

    private HashMap<Long, ArrayList<double[]>> readIndex(long n_id, int level) {
        HashMap<Long, ArrayList<double[]>> skylines = new HashMap<>();
        String sub_folder_str = ParsedOptions.indexFolder + db_name + "/level" + level;
        File idx_file = new File(sub_folder_str + "/" + n_id + ".idx");
        try {
            if (idx_file.exists()) {
                BufferedReader b = new BufferedReader(new FileReader(idx_file));
                String readLine = "";
                while ((readLine = b.readLine()) != null) {
                    String[] infos = readLine.split(" ");
                    double[] costs = new double[infos.length - 1];
                    long target_node = Long.parseLong(infos[0]);
                    costs[0] = Double.parseDouble(infos[1]);
                    costs[1] = Double.parseDouble(infos[2]);
                    costs[2] = Double.parseDouble(infos[3]);

                    if (skylines.containsKey(target_node)) {
                        ArrayList<double[]> skyline_costs = skylines.get(target_node);
                        skyline_costs.add(costs);
                        skylines.put(target_node, skyline_costs);
                    } else {
                        ArrayList<double[]> skyline_costs = new ArrayList<>();
                        skyline_costs.add(costs);
                        skylines.put(target_node, skyline_costs);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (skylines.size() != 0) {
            return skylines;
        } else {
            return null;
        }
    }

    public boolean addToSkyline(ArrayList<double[]> skyline_costs, double[] costs) {
        int i = 0;

        if (skyline_costs.isEmpty()) {
            skyline_costs.add(costs);
            return true;
        } else {
            boolean can_insert_np = true;
            for (; i < skyline_costs.size(); ) {
                if (checkDominated(skyline_costs.get(i), costs)) {
                    can_insert_np = false;
                    break;
                } else {
                    if (checkDominated(costs, skyline_costs.get(i))) {
                        skyline_costs.remove(i);
                    } else {
                        i++;
                    }
                }
            }
            if (can_insert_np) {
                skyline_costs.add(costs);
                return true;
            }
        }
        return false;
    }

    private boolean checkDominated(double[] costs, double[] estimatedCosts) {
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] > estimatedCosts[i]) {
                return false;
            }
        }
        return true;
    }

    private void deleteRelationshipFromDB(Relationship r, HashSet<Long> deletedNodes, Neo4jDB neo4jDB) {
        try (Transaction tx = neo4jDB.graphDB.beginTx()) {
            r.delete();
            Node sNode = r.getStartNode();
            Node eNode = r.getEndNode();

            /**If one node become isolated, remove it from the graph database.**/
            if (sNode.getDegree(Direction.BOTH) == 0) {
                deletedNodes.add(sNode.getId());
                sNode.delete();
            }
            if (eNode.getDegree(Direction.BOTH) == 0) {
                deletedNodes.add(eNode.getId());
                eNode.delete();
            }
            tx.success();
        }
    }

    private HashSet<Long> indexBuildAtLevel(int level, HashSet<Long> de, NodeCluster cluster, Neo4jDB p_neo4j) {

        long rt_over_all = 0;

        HashSet<Long> updated_index_nodes = new HashSet<>();

        //check the node remained in after the deletion (remained in current layer graph)
        HashSet<Long> remained_nodes = getNodeListAtLevel(cluster.node_list, this.neo4j);

        //if the last layer graph is empty, don't build the index.
        if (remained_nodes.size() == 0) {
            return updated_index_nodes;
        }

        try (Transaction tx = p_neo4j.graphDB.beginTx()) {
            int node_idx = 1;

            for (long n_id : cluster.node_list) {

                long build_start = System.currentTimeMillis();

                HashMap<Long, ArrayList<double[]>> skylines = readIndex(n_id, level);

                HashMap<Long, myQueueNode> tmpStoreNodes = new HashMap();
                myQueueNode snode = new myQueueNode(n_id, p_neo4j);
                myNodePriorityQueue mqueue = new myNodePriorityQueue();
                tmpStoreNodes.put(snode.id, snode);
                mqueue.add(snode);


                while (!mqueue.isEmpty()) {
                    myQueueNode v = mqueue.pop();

                    for (int i = 0; i < v.skyPaths.size(); i++) {
                        path p = v.skyPaths.get(i);

                        if (!p.expaned) {
                            p.expaned = true;

                            ArrayList<path> new_paths;
                            //the expansion only use the edges that 1) are deleted in current sub-step 2) the edge in this cluster
                            new_paths = p.expand(p_neo4j, de, cluster.rels);

                            for (path np : new_paths) {
                                myQueueNode next_n;
                                if (tmpStoreNodes.containsKey(np.endNode)) {
                                    next_n = tmpStoreNodes.get(np.endNode);
                                } else {
                                    next_n = new myQueueNode(snode, np.endNode, p_neo4j);
                                    tmpStoreNodes.put(next_n.id, next_n);
                                }

                                if (next_n.addToSkyline(np) && !next_n.inqueue) {
                                    mqueue.add(next_n);
                                    next_n.inqueue = true;
                                }
                            }
                        }
                    }
                }

                long rt_used_in_expansion = System.currentTimeMillis() - build_start;

                int sum = 0;
                for (Map.Entry<Long, myQueueNode> e : tmpStoreNodes.entrySet()) {
                    ArrayList<path> sk = e.getValue().skyPaths;
                    //remove the index of the self connection that node only has one skyline path and the skyline path is to itself
                    if (!(sk.size() == 1 && sk.get(0).costs[0] == 0 && sk.get(0).costs[1] == 0 && sk.get(0).costs[2] == 0)) {
                        for (path p : sk) {
                            if (remained_nodes.contains(p.endNode)) {
                                sum++;
                            }
                        }
                    }
                }

                if (sum != 0) {
                    updated_index_nodes.add(n_id);

                    /**clean the built index file*/
                    String sub_folder_str = ParsedOptions.indexFolder + db_name + "/level" + level;
                    File idx_file = new File(sub_folder_str + "/" + n_id + ".idx");

                    //previous has the skyline information from n_id to other nodes (deleted in previous sub step)
                    if (skylines != null) {
                        for (Map.Entry<Long, myQueueNode> e : tmpStoreNodes.entrySet()) {
                            if (remained_nodes.contains(e.getKey())) {
                                long target_node_id = e.getKey();
                                myQueueNode node_obj = e.getValue();
                                ArrayList<path> skys = node_obj.skyPaths;
                                for (path p : skys) {
                                    /** the end node of path is a highway, the node is still appear in next level, also, the path is not a dummy path of source node **/
                                    if (p.endNode != n_id) {
                                        if (remained_nodes.size() != 0 && remained_nodes.contains(p.endNode)) { // not the max_level graph, have reminding nodes on next level
                                            double[] costs = new double[3];
                                            costs[0] = p.costs[0];
                                            costs[1] = p.costs[1];
                                            costs[2] = p.costs[2];

                                            if (skylines.containsKey(target_node_id)) {
                                                ArrayList<double[]> skyline_costs = skylines.get(target_node_id);
                                                addToSkyline(skyline_costs, costs);
                                                skylines.put(target_node_id, skyline_costs);
                                            } else {
                                                ArrayList<double[]> skyline_costs = new ArrayList<>();
                                                skyline_costs.add(costs);
                                                skylines.put(target_node_id, skyline_costs);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        //Do not need to append, because the skyline object is update, the idx need to be rewritten.
                        BufferedWriter writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath(), false));
                        for (Map.Entry<Long, ArrayList<double[]>> e : skylines.entrySet()) {
                            long target_node_id = e.getKey();
                            ArrayList<double[]> skys = e.getValue();
                            for (double[] costs : skys) {
                                if (target_node_id != n_id) {
                                    writer.write(target_node_id + " " + costs[0] + " " + costs[1] + " " + costs[2] + "\n");
                                }
                            }
                        }
                        writer.close();
                    } else {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath()));
                        for (Map.Entry<Long, myQueueNode> e : tmpStoreNodes.entrySet()) {
                            long target_node_id = e.getKey();
                            myQueueNode node_obj = e.getValue();
                            ArrayList<path> skys = node_obj.skyPaths;
                            for (path p : skys) {
                                /** the end node of path is a highway, the node is still appear in next level, also, the path is not a dummy path of source node **/
                                if (p.endNode != n_id) {
                                    if (remained_nodes.size() != 0 && remained_nodes.contains(p.endNode)) { // not the max_level graph, have reminding nodes on next level
                                        writer.write(target_node_id + " " + p.costs[0] + " " + p.costs[1] + " " + p.costs[2] + "\n");
                                    }
                                }
                            }
                        }
                        writer.close();
                    }
                }

                tx.success();
            }
            tx.success();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return updated_index_nodes;
    }

    private HashSet<Long> getNodeListAtLevel(HashSet<Long> node_list, Neo4jDB neo4j) {
        HashSet<Long> result = new HashSet<>();
        try (Transaction tx = neo4j.graphDB.beginTx()) {
            for (long node_id_in_list : node_list) {
                try {
                    long node_id = neo4j.graphDB.getNodeById(node_id_in_list).getId();
                    result.add(node_id);
                } catch (NotFoundException nofundexpection) {
//                   System.err.println("Can not find the node (" + i++ + ")" + src_node_id + " in the cluster in " + neo4j.graphDB);
                    //nodes are not find mean them are removed at current level
                }
            }
            tx.success();
        }

        return result;
    }

    private HashSet<Long> removeSingletonEdgesInCluster(int cluster_id, NodeCluster cluster, HashSet<Long> deletedNodes) {
        long rt_start_remove_single_in_cluster = System.nanoTime();
        int sum_single = 0;

        long pre_edge_num = neo4j.getNumberofEdges();
        long pre_node_num = neo4j.getNumberofNodes();
        long pre_degree_num = degree_pairs.size();

        HashSet<Long> deletedEdges = new HashSet<>();

        int pre_deleted = deletedEdges.size();
        int count = -1;

        while (count != 0) {
            try (Transaction tx = neo4j.graphDB.beginTx()) {
                //if the degree pair whose key or value is equal to 1, it means it is a single edge
                HashSet<Pair<Integer, Integer>> degree_keys = new HashSet<>(this.degree_pairs.keySet());

                for (Pair<Integer, Integer> e : degree_keys) {
                    if (e.getValue() == 1 || e.getKey() == 1) {

                        ArrayList<Long> list = new ArrayList<>(this.degree_pairs.get(e));

                        for (long rel_id : list) {
                            if (cluster.rels.contains(rel_id)) {
                                Relationship r = neo4j.graphDB.getRelationshipById(rel_id);
                                //Only remove the edges in the cluster
                                if (cluster.node_list.contains(r.getStartNodeId()) && cluster.node_list.contains(r.getEndNodeId())) {
                                    sum_single++;
                                    updateDegreePairs(rel_id);
                                    deleteRelationshipFromDB(r, deletedNodes, neo4j);
                                    deletedEdges.add(r.getId());

                                }
                            }
                        }

                        tx.success();
                    }
                }
                tx.success();
            } catch (NotFoundException e) {
                e.printStackTrace();
                System.out.println("no property found exception ");
                System.exit(0);
            }
            int after_deleted = deletedEdges.size();
            count = after_deleted - pre_deleted;
            pre_deleted = after_deleted;
        }

        System.out.println("delete single in cluster (" + cluster_id + "): pre:" + pre_node_num + " " + pre_edge_num + " " + pre_degree_num + " " +
                "single_edges:" + sum_single + " " +
                "post:" + neo4j.getNumberofNodes() + " " + neo4j.getNumberofEdges() + " " +
                "dgr_paris:" + degree_pairs.size() + " in " + (System.nanoTime() - rt_start_remove_single_in_cluster) * 1.0 / 1000000 + " ms ");
        return deletedEdges;
    }

    private void getDegreePairs() {
        long start_rt_getDP = System.currentTimeMillis();
        this.degree_pairs.clear();
        try (Transaction tx = neo4j.graphDB.beginTx()) {
            ResourceIterable<Relationship> rels = neo4j.graphDB.getAllRelationships();
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
                HashSet<Long> a;
                if (this.degree_pairs.containsKey(p)) {
                    a = this.degree_pairs.get(p);
                } else {
                    a = new HashSet<>();
                }
                a.add(rel_id);
                this.degree_pairs.put(p, a);
            }

            tx.success();
        }
        System.out.println("Finish the Call of the Degree Pairs Getting function in " + (System.currentTimeMillis() - start_rt_getDP) + "  ms");
    }

    private void updateDegreePairs(long rel_id) {

        try (Transaction tx = this.neo4j.graphDB.beginTx()) {
            Relationship r = this.neo4j.graphDB.getRelationshipById(rel_id);
            int start_node_degree = r.getStartNode().getDegree(Direction.BOTH);
            int end_node_degree = r.getEndNode().getDegree(Direction.BOTH);

            Pair<Integer, Integer> key = null;
            if (start_node_degree == 1) {
                key = new MutablePair<>(start_node_degree, end_node_degree);
            } else if (end_node_degree == 1) {
                key = new MutablePair<>(end_node_degree, start_node_degree);
            } else {
                System.out.println("The updated degree pairs function error ===========================================");
                System.exit(0);
            }


            //the single edge is removed
            HashSet<Long> key_rel_list = this.degree_pairs.get(key);
            key_rel_list.remove(rel_id);

            if (key_rel_list.isEmpty()) {
                this.degree_pairs.remove(key);
            }


            //is there any probability of the degree pair <1,1>
            long non_single_node_id = start_node_degree != 1 ? r.getStartNodeId() : r.getEndNodeId();
            int non_degree = this.neo4j.graphDB.getNodeById(non_single_node_id).getDegree(Direction.BOTH);
            HashSet<Relationship> n_e = this.neo4j.getNeighborsRelObjList(non_single_node_id);

            //iterate the neighbors of non single node, which rel id is not equals to rel_id
            for (Relationship n_r : n_e) {
                long r_id = n_r.getId();

                if (r_id == rel_id) {
                    continue;
                }

                int d1 = n_r.getStartNode().getDegree(Direction.BOTH);
                int d2 = n_r.getEndNode().getDegree(Direction.BOTH);

                Pair<Integer, Integer> old_key = null;

                if (d1 > d2) {
                    old_key = new MutablePair<>(d2, d1);
                } else {
                    old_key = new MutablePair<>(d1, d2);
                }


                HashSet<Long> rel_list = this.degree_pairs.get(old_key);

                rel_list.remove(r_id); //remove the r_id from the list of the old_key

                if (rel_list.isEmpty()) {
                    this.degree_pairs.remove(old_key);
                }

                /**
                 * Update the list of new keys
                 * p_key's key is always less or equals to than p_key's value, so there is no case to deal with p_key.getKey() > p_key.getValue()
                 */
                Pair<Integer, Integer> new_key = null;
                if (old_key.getKey() == old_key.getValue() && old_key.getKey() == non_degree) {
                    new_key = new MutablePair<>(non_degree - 1, non_degree);
                } else if (old_key.getKey() < old_key.getValue()) {
                    if (old_key.getKey() == non_degree) {
                        new_key = new MutablePair<>(non_degree - 1, old_key.getValue());
                    } else {
                        new_key = new MutablePair<>(old_key.getKey(), non_degree - 1);
                    }
                }

                if (new_key.getKey() != 0 || new_key.getValue() != 0) {
                    if (this.degree_pairs.containsKey(new_key)) {
                        this.degree_pairs.get(new_key).add(r_id);
                    } else {
                        HashSet<Long> l = new HashSet<>();
                        l.add(r_id);
                        this.degree_pairs.put(new_key, l);
                    }
                } else {
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
                }
            }

            tx.success();
        }
    }

    private void updateSingleNodeIndexes(int level, HashSet<Long> single_node_list, HashSet<Long> sub_step_updated_index_nodes) {
        System.out.println("Updating the single edge indexes .......................................");
        HashSet<Long> remained_nodes = getNodeListAtCurrentLevel();

        for (long n_id : single_node_list) {

            String sub_folder_str = ParsedOptions.indexFolder + db_name + "/level" + level;
            File idx_file = new File(sub_folder_str + "/" + n_id + ".idx");

            HashMap<Long, ArrayList<double[]>> updated_skyline = new HashMap<>();

            HashMap<Long, ArrayList<double[]>> skylines = readIndex(n_id, level);

            if (skylines == null) {
                continue;
            }

            for (Map.Entry<Long, ArrayList<double[]>> e : skylines.entrySet()) {
                long high_way_node = e.getKey();

                ArrayList<double[]> costs;

                if (sub_step_updated_index_nodes.contains(high_way_node)) {
                    HashMap<Long, ArrayList<double[]>> updates_skyline = readIndex(high_way_node, level);
                    for (Map.Entry<Long, ArrayList<double[]>> add_e : updates_skyline.entrySet()) {
                        long target_node_id = add_e.getKey();

                        if (updated_skyline.containsKey(target_node_id)) {
                            costs = updated_skyline.get(target_node_id);
                        } else {
                            costs = new ArrayList<>();
                        }

                        for (double[] old_costs : e.getValue()) {
                            for (double[] add_costs : add_e.getValue()) {

                                double[] new_costs = new double[3];
                                new_costs[0] = old_costs[0] + add_costs[0];
                                new_costs[1] = old_costs[1] + add_costs[1];
                                new_costs[2] = old_costs[2] + add_costs[2];
                                addToSkyline(costs, new_costs);
                            }
                        }

                        updated_skyline.put(target_node_id, costs); // index from n_id --> highway node  --> target node

                        if (remained_nodes.contains(high_way_node)) {
                            ArrayList<double[]> old_costs = e.getValue();
                            updated_skyline.put(high_way_node, old_costs);
                        }
                    }
                } else {
                    costs = e.getValue();
                    updated_skyline.put(high_way_node, costs);
                }
            }

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath()));
                for (Map.Entry<Long, ArrayList<double[]>> e : updated_skyline.entrySet()) {
                    long target_node_id = e.getKey();
                    if (target_node_id == n_id) {
                        continue;
                    }
                    ArrayList<double[]> costs = e.getValue();
                    for (double[] c : costs) {
                        writer.write(target_node_id + " " + c[0] + " " + c[1] + " " + c[2] + "\n");
                    }
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private HashSet<Long> getNodeListAtCurrentLevel() {
        HashSet<Long> nodeList = new HashSet<>();

        GraphDatabaseService graphdb_level = this.neo4j.graphDB;

        try (Transaction tx = graphdb_level.beginTx()) {
            ResourceIterable<Node> allnodes_iteratable = graphdb_level.getAllNodes();
            ResourceIterator<Node> allnodes_iter = allnodes_iteratable.iterator();
            while (allnodes_iter.hasNext()) {
                Node node = allnodes_iter.next();
                nodeList.add(node.getId());
            }
            tx.success();
        }
        return nodeList;
    }

    private void updateExistedIndexInCurrentLevel(int level, HashSet<Long> sub_step_updated_index_nodes) {
        System.out.println("Updating the created edge indexes at level " + level + " .......................................");
        HashSet<Long> remained_nodes = getNodeListAtCurrentLevel();
        String index_folder = ParsedOptions.indexFolder + db_name + "/level" + level;
        List<Long> idx_list = Arrays.stream(new File(index_folder).listFiles(new idxFileFilter())).map(e -> {
            String fname = e.getName();
            return Long.parseLong(fname.substring(0, fname.lastIndexOf(".idx")));
        }).collect(Collectors.toList());

        //existing  indexes --> nid --> nid's highway nodes
        for (long n_id : idx_list) {
            File idx_file = new File(index_folder + "/" + n_id + ".idx");

            HashMap<Long, ArrayList<double[]>> updated_skyline = new HashMap<>();

            HashMap<Long, ArrayList<double[]>> skylines = readIndex(n_id, level);

            if (skylines == null) {
                continue;
            }

            for (Map.Entry<Long, ArrayList<double[]>> e : skylines.entrySet()) {
                long high_way_node = e.getKey();

                ArrayList<double[]> costs;

                if (sub_step_updated_index_nodes.contains(high_way_node)) {
                    HashMap<Long, ArrayList<double[]>> updates_skyline = readIndex(high_way_node, level);
                    if (updates_skyline == null) {
//                        System.out.println(sub_step_updated_index_nodes.contains(high_way_node));
//                        System.out.println(high_way_node);
//                        System.out.println("Find highway node error!!!!!");
//                        System.exit(0);
                        continue;
                    }

                    for (Map.Entry<Long, ArrayList<double[]>> add_e : updates_skyline.entrySet()) {
                        long target_node_id = add_e.getKey();

                        if (updated_skyline.containsKey(target_node_id)) {
                            costs = updated_skyline.get(target_node_id);
                        } else {
                            costs = new ArrayList<>();
                        }


                        for (double[] old_costs : e.getValue()) {
                            for (double[] add_costs : add_e.getValue()) {
                                double[] new_costs = new double[3];
                                new_costs[0] = old_costs[0] + add_costs[0];
                                new_costs[1] = old_costs[1] + add_costs[1];
                                new_costs[2] = old_costs[2] + add_costs[2];
                                addToSkyline(costs, new_costs);
                            }
                        }

                        updated_skyline.put(target_node_id, costs); // index from n_id --> highway node  --> target node

                        if (remained_nodes.contains(high_way_node)) {
                            ArrayList<double[]> old_costs = e.getValue();
                            updated_skyline.put(high_way_node, old_costs);
                        }
                    }
                } else {
                    costs = e.getValue();
                    updated_skyline.put(high_way_node, costs);
                }
            }

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath()));
                for (Map.Entry<Long, ArrayList<double[]>> e : updated_skyline.entrySet()) {
                    long target_node_id = e.getKey();
                    if (target_node_id == n_id) {
                        continue;
                    }
                    ArrayList<double[]> costs = e.getValue();
                    for (double[] c : costs) {
                        writer.write(target_node_id + " " + c[0] + " " + c[1] + " " + c[2] + "\n");
                    }
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        //nid ---> existing  indexes --> existing highway nodes
//        for (long n_id : sub_step_updated_index_nodes) {
//            File idx_file = new File(index_folder + "/" + n_id + ".idx");
//
//            HashMap<Long, ArrayList<double[]>> skylines = readIndex(n_id, level);
//
//            if (skylines == null) {
//                continue;
//            }
//
//            HashMap<Long, ArrayList<double[]>> updated_skyline = new HashMap<>();
//
//            for (Map.Entry<Long, ArrayList<double[]>> e : skylines.entrySet()) {
//                long highway_node = e.getKey();
//                ArrayList<double[]> costs;
//                if (idx_list.contains(highway_node)) {
//                    HashMap<Long, ArrayList<double[]>> updates_skyline = readIndex(highway_node, level);
//
//                    if (updates_skyline == null || updates_skyline.isEmpty()) {
//                        continue;
//                    }
//
//                    for (Map.Entry<Long, ArrayList<double[]>> add_e : updates_skyline.entrySet()) {
//                        long target_node_id = add_e.getKey();
//
//                        if (updated_skyline.containsKey(target_node_id)) {
//                            costs = updated_skyline.get(target_node_id);
//                        } else {
//                            costs = new ArrayList<>();
//                        }
//
//
//                        for (double[] old_costs : e.getValue()) {
//                            for (double[] add_costs : add_e.getValue()) {
//                                double[] new_costs = new double[3];
//                                new_costs[0] = old_costs[0] + add_costs[0];
//                                new_costs[1] = old_costs[1] + add_costs[1];
//                                new_costs[2] = old_costs[2] + add_costs[2];
//                                addToSkyline(costs, new_costs);
//                            }
//                        }
//
//                        updated_skyline.put(target_node_id, costs); // index from n_id --> highway node  --> target node
//
//                        if (remained_nodes.contains(highway_node)) { // index from n_id --> highway node
//                            ArrayList<double[]> old_costs = e.getValue();
//                            updated_skyline.put(highway_node, old_costs);
//                        }
//                    }
//                } else {
//                    costs = e.getValue();
//                    updated_skyline.put(highway_node, costs);
//                }
//            }
//
//
//            BufferedWriter writer = null;
//            try {
//                writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath()));
//                for (Map.Entry<Long, ArrayList<double[]>> e : updated_skyline.entrySet()) {
//                    long target_node_id = e.getKey();
//                    if (target_node_id == n_id) {
//                        continue;
//                    }
//                    ArrayList<double[]> costs = e.getValue();
//                    for (double[] c : costs) {
//                        writer.write(target_node_id + " " + c[0] + " " + c[1] + " " + c[2] + "\n");
//                    }
//                }
//
//                writer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void insertIndex(int level, HashMap<Long, HashMap<Long, ArrayList<double[]>>> nodes_in_segment_index) {
        //TODO: Add the function to removed the existing index.
        try {
            for (long n_id : nodes_in_segment_index.keySet()) {
                String sub_folder_str = ParsedOptions.indexFolder + db_name + "/level" + level;
                File idx_file = new File(sub_folder_str + "/" + n_id + ".idx");
                if (!idx_file.exists()) {
                    BufferedWriter writer = null;
                    writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath()));
                    for (Map.Entry<Long, ArrayList<double[]>> e : nodes_in_segment_index.get(n_id).entrySet()) {
                        long highway_nodes = e.getKey();
                        ArrayList<double[]> skyline_costs = e.getValue();
                        for (double[] costs : skyline_costs) {
                            writer.write(highway_nodes + " " + costs[0] + " " + costs[1] + " " + costs[2] + "\n");
                        }
                    }
                    writer.close();
                } else {
                    HashMap<Long, ArrayList<double[]>> skylines = readIndex(n_id, level);
                    if (skylines == null) {
                        continue;
//                        System.out.println(idx_file);
//                        System.out.println("the skyline of "+n_id+" at level "+level+" is null");
//                        System.exit(0);
                    }
                    for (long highway : nodes_in_segment_index.get(n_id).keySet()) {
                        if (skylines.containsKey(highway)) {
                            ArrayList<double[]> skyline_costs = skylines.get(highway);
                            ArrayList<double[]> segment_costs = nodes_in_segment_index.get(n_id).get(highway);
                            for (double[] c : segment_costs) {
                                addToSkyline(skyline_costs, c);
                            }
                            skylines.put(highway, skyline_costs);
                        } else {
                            ArrayList<double[]> segment_costs = nodes_in_segment_index.get(n_id).get(highway);
                            ArrayList<double[]> skyline_costs = new ArrayList<>();
                            skyline_costs.addAll(segment_costs);
                            skylines.put(highway, skyline_costs);
                        }
                    }

                    BufferedWriter writer = null;
                    writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath()));

                    for (Map.Entry<Long, ArrayList<double[]>> e : skylines.entrySet()) {
                        long highway_nodes = e.getKey();
                        for (double[] costs : e.getValue()) {
                            writer.write(highway_nodes + " " + costs[0] + " " + costs[1] + " " + costs[2] + "\n");
                        }
                    }
                    writer.close();

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
