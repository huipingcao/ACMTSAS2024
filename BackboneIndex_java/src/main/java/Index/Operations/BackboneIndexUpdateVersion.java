package Index.Operations;

import Index.ClusterSpanningTree;
import Index.NodeCluster;
import Index.NodeClusters;
import Index.PairComparator;
import Index.components.Segment;
import Neo4jTools.Neo4jDB;
import Query.partition.BFSPartition;
import Query.partition.NodeCoefficientPartition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.*;
import org.neo4j.unsafe.impl.batchimport.cache.idmapping.string.Radix;
import utilities.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class BackboneIndexUpdateVersion {


    private String base_db_name;
    private Neo4jDB neo4j;
    private GraphDatabaseService graphdb;
    private long cn;
    private long numberOfEdges;

    //When check if the cluster is oversized, it checks the size of the nodes in the cluster with max(min_size, cluster_size)
    private int min_size; // the threshold of the size of the cluster that is used to decide the cluster need to process (deleted or index building) when the size of the cluster is larger than the given min_size threshold
    private int cluster_size; // capacity of each cluster
    private double percentage;

    //Pair <sid_degree,did_degree> -> list of the relationship id that the degrees of the start node and end node are the response given pair of key
    TreeMap<Pair<Integer, Integer>, HashSet<Long>> degree_pairs = new TreeMap(new PairComparator());

    //deleted edges record the relationship deleted in each layer, the index of each layer is based on the expansion on previous level graph
    ArrayList<HashSet<Long>> deletedEdges_layer = new ArrayList<>();

    public BackboneIndexUpdateVersion() {
        this.base_db_name = ParsedOptions.db_name;
        this.percentage = ParsedOptions.percentage;
        this.min_size = this.cluster_size = ParsedOptions.min_size;
        System.out.println("Index folder" + ParsedOptions.indexFolder);
    }

    public void build() {
        initLevel();
        createIndexFolder();
        construction();
    }


    private void initLevel() {
        String sub_db_name = this.base_db_name + "_Level0";
        neo4j = new Neo4jDB(sub_db_name);
        System.out.println(neo4j.DB_PATH);
        neo4j.startDB(false);
        graphdb = neo4j.graphDB;
        getDegreePairs();

        cn = neo4j.getNumberofNodes();
        numberOfEdges = neo4j.getNumberofEdges();
        neo4j.closeDB();
        System.out.println("Initialization: there are " + cn + " nodes and " + numberOfEdges + " edges" + "   " + this.min_size);
    }

    /**
     * Calculated the degree pair of each edge,
     * one distinct degree pair p contains:
     * the list of the edges whose degree pair of the start node and end node is equal to the given key p
     */
    private void getDegreePairs() {
        long start_rt_getDP = System.currentTimeMillis();
        this.degree_pairs.clear();
        try (Transaction tx = graphdb.beginTx()) {
            ResourceIterable<Relationship> rels = this.graphdb.getAllRelationships();
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

    private void createIndexFolder() {
        String folder = ParsedOptions.indexFolder;

        File idx_folder = new File(folder);
        try {
            if (idx_folder.exists()) {
                System.out.println("delete the folder : " + folder);
                FileUtils.deleteDirectory(idx_folder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        idx_folder.mkdirs();
        System.out.println("Create the index folder : " + folder);
    }

    private void construction() {
        long construction_time = System.currentTimeMillis();

        int currentLevel = 0;

        boolean nodes_deleted = false;
        boolean straightSegment = true;

        do {
            int upperlevel = currentLevel + 1;
            System.out.println("===============  level:" + upperlevel + " ==============");

            //copy db from previous level
            copyToHigherDB(currentLevel, upperlevel);

            //handle the degrees pairs
            nodes_deleted = handleUpperLevelGraph(upperlevel);


            if (ParsedOptions.degreeHandle == 0) {
                if (cn != 0 && !nodes_deleted) {

                    String sub_db_name = this.base_db_name + "_Level" + upperlevel;

                    neo4j = new Neo4jDB(sub_db_name);
                    neo4j.startDB(false);
                    graphdb = neo4j.graphDB;

                    HashSet<Long> updated_segment_idx_nodes = handleTwoDegreePairSegments(currentLevel);
                    if (updated_segment_idx_nodes.size() != 0) {
                        updateExistedIndexInCurrentLevel(currentLevel, updated_segment_idx_nodes);
                    }

                    if (updated_segment_idx_nodes.size() != 0) {
                        nodes_deleted = true;
                    }

                    System.out.println("Flags : " + nodes_deleted + " current level:" + currentLevel);
                    cn = neo4j.getNumberofNodes();
                    neo4j.closeDB();
                }
            }

            currentLevel = upperlevel;
        } while (nodes_deleted);

        System.out.println("finish the index finding, the current level is " + (currentLevel - 1) + "  with number of nodes :" + cn);
        System.out.println("Overall running time : " + (System.currentTimeMillis() - construction_time) + " ms");
    }

    /**
     * Copy the graph from the src_level to the dest_level,
     * the dest level graph used to shrink and build index on upper level.
     *
     * @param src_level
     * @param dest_level
     */
    private void copyToHigherDB(int src_level, int dest_level) {
        String src_db_name = this.base_db_name + "_Level" + src_level;
        String dest_db_name = this.base_db_name + "_Level" + dest_level;

        File src_db_folder = new File(ParsedOptions.neo4jdbPath + "/" + src_db_name);
        File dest_db_folder = new File(ParsedOptions.neo4jdbPath + "/" + dest_db_name);

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
    }

    private boolean handleUpperLevelGraph(int currentLevel) {
        String sub_db_name = this.base_db_name + "_Level" + currentLevel;


        neo4j = new Neo4jDB(sub_db_name);
        neo4j.startDB(false);
        graphdb = neo4j.graphDB;
        long pre_n = neo4j.getNumberofNodes();
        long pre_e = neo4j.getNumberofEdges();
        System.out.println("deal with level " + currentLevel + " graph at " + neo4j.DB_PATH + "  " + pre_n + " nodes and " + pre_e + " edges");


        // record the nodes that are deleted in this layer.
        HashSet<Long> deletedNodes = new HashSet<>();
        // record the edges that are deleted in this layer, the index is build based on it
        HashSet<Long> deletedEdges = new HashSet<>();
        boolean deleted;

        HashSet<Long> sub_step_deletedEdges = new HashSet<>(); // the deleted edges in this sub step
        HashSet<Long> sub_step_deletedNodes = new HashSet<>(); // the deleted nodes in this sub step

        int sub_step_number = 1;
        int level = currentLevel - 1; //used to build the index for level, which means the index is build from level to current_level

        do {

            sub_step_deletedEdges.clear();
            sub_step_deletedNodes.clear();

            getDegreePairs();
            System.out.println("Updated the degree pair at level " + currentLevel);
            checkIndexFolderExisted(level);

            HashSet<Long> single_node_list = new HashSet<>();
//            if (currentLevel == 1) {
            sub_step_deletedEdges.addAll(removeSingletonEdgesInForests(sub_step_deletedNodes));
            indexBuildSingleEdgesAtLevel(level, sub_step_deletedEdges, sub_step_deletedNodes);
            ArrayList<ArrayList<Long>> spaths = buildSingleEdgePaths(level, sub_step_number, sub_step_deletedEdges);


            //the nodes are removed during the deletion of the single edges, the target nodes are always the remained nodes
            single_node_list.addAll(sub_step_deletedNodes);
            System.out.println("The # of single edges in this sub_step  :" + single_node_list.size() + "  At level " + currentLevel);
//            }


            System.out.println("Finish removing the single edges at the begining of level :::   " + currentLevel);


            System.out.println("#####");

//            String textFilePath = prefix + folder_name + "/non-single/level" + currentLevel + "/";
//            neo4j.saveGraphToTextFormation(textFilePath);

            //remove the edges in each cluster
            NodeClusters process_clusters = removeLowerDegreePairEdgesByThreshold(sub_step_deletedNodes, sub_step_deletedEdges);
            buildClusterFolder(level, sub_step_number, process_clusters);
            deleted = !sub_step_deletedEdges.isEmpty();


            System.out.println("Removing the edges in level " + currentLevel + "  with degree threshold  : ");
            long post_n = neo4j.getNumberofNodes();
            long post_e = neo4j.getNumberofEdges();
            System.out.println("~~~~~~~~~~~~~ pre:" + pre_n + " " + pre_e + "  post:" + post_n + " " + post_e + "   # of deleted Edges:" + sub_step_deletedEdges.size() + "   # of processed clusters " + process_clusters.getNumberOfClusters());

            getDegreePairs();

            System.out.println("Add the deleted edges of the level " + currentLevel + "  to the deletedEdges_layer structure. Build the index for level " + level);

            HashSet<Long> sub_step_updated_index_nodes = new HashSet<>();

            if (deletedEdges.size() == 0 && !deleted) {
                System.out.println("Can not remove the any edges in current level " + currentLevel + " stop the index construction ");
                break;
            } else {
                for (Map.Entry<Integer, NodeCluster> cluster_entry : process_clusters.clusters.entrySet()) {
                    //jump the noise cluster
                    if (cluster_entry.getKey() == 0 || cluster_entry.getValue().num_removed_edges == 0) {
                        continue;
                    }

                    NodeCluster cluster = cluster_entry.getValue();

                    System.out.println("-----------------------------------------------------");
                    HashSet<Long> cluster_deletedEdges = removeSingletonEdgesInCluster(cluster.cluster_id, cluster, sub_step_deletedNodes);
                    System.out.println("-----------------------------------------------------");
                    sub_step_deletedEdges.addAll(cluster_deletedEdges);

                    long rt_index_build_st = System.currentTimeMillis();

                    //build the index only using the edges that are removed in the cluster/
                    HashSet<Long> updated_index_nodes = indexBuildAtLevel(level, sub_step_deletedEdges, cluster);

                    long rt_index_build = System.currentTimeMillis() - rt_index_build_st;

                    System.out.println("index building " + rt_index_build + "(ms) --- on cluster:" + cluster.cluster_id + "   cluster size: " + cluster.node_list.size() + "   # of rels in cluster:" + cluster.rels.size() + "   # of border nodes:" + cluster.border_node_list.size());

                    sub_step_updated_index_nodes.addAll(updated_index_nodes);
                }
            }

            deletedEdges.addAll(sub_step_deletedEdges);
            deletedNodes.addAll(sub_step_deletedNodes);

            updateSingleNodeIndexes(level, single_node_list, sub_step_updated_index_nodes);

            if (sub_step_number != 1) {
                updateExistedIndexInCurrentLevel(level, sub_step_updated_index_nodes);
            }


            getDegreePairs();

            sub_step_number++;

            long numberOfNodes = neo4j.getNumberofNodes();
            post_n = numberOfNodes;
            post_e = neo4j.getNumberofEdges();
            cn = numberOfNodes;
            System.out.println(" ~~~~~~~~~~~~~ pre:" + pre_n + " " + pre_e + "  post:" + post_n + " " + post_e + "   # of deleted Edges:" + sub_step_deletedEdges.size() + " in the level   (min_size:" + min_size + ")  " + deleted);
            if (cn == 0 && sub_step_number == 1) { //empty graph only after one iteration, don't need the last layer index
                System.out.println("There are 0 nodes left after removing, do not need to build index for current level (the index from level:" + level + " to " + currentLevel + ")");
                deleted = false;
            }
        } while (deleted && deletedEdges.size() <= this.percentage * this.numberOfEdges && sub_step_deletedEdges.size() != 0);

        if (ParsedOptions.degreeHandle == 1) {
            if (cn != 0) {
                HashSet<Long> updated_segment_idx_nodes = handleTwoDegreePairSegments(level);
                updateExistedIndexInCurrentLevel(level, updated_segment_idx_nodes);
            } else {
                System.out.println(" The # of remained nodes is 0, do not need to handle degree <2,2> edges. ");
            }
        }

        long post_n = neo4j.getNumberofNodes();
        long post_e = neo4j.getNumberofEdges();
        cn = post_n;
        neo4j.closeDB();

        /**
         *
         * Case 1 : All the nodes and edges are removed, stop the index building process.
         * Case 2 : Removed some edges but can not reach the limitation (threshold), stop the index building process.
         * Case 3 : Can not remove any edges in this step, stop the index building process.
         * Case 4 : number of deleted edges are larger than given threshold, it means there are enough edges are removed
         *          from current level then move to next level.
         */
        if (cn == 0) { //case 1
            System.out.println("There are 0 nodes left after removing, stop the index construction process.");
            System.out.println("---------------------------------------------------");
            return false;
        } else if (0 < deletedEdges.size() && deletedEdges.size() <= this.percentage * this.numberOfEdges) { //case 2
            System.out.println("Can not remove enough edges, stop the index construction process. Deleted edges in current level : " + deletedEdges.size() + "  deleted nodes in current level : " + deletedNodes.size());
            System.out.println("There are " + post_n + " nodes and " + post_e + " edges are level !!!!!!!");
            return false;
        } else if (0 == deletedEdges.size() && 0 == sub_step_deletedEdges.size() && !deleted) { //case 3
            System.out.println("Can not remove any edges, stop the index construction process. Deleted edges in current level : " + deletedEdges.size() + "  deleted nodes in current level : " + deletedNodes.size());
            System.out.println("There are " + post_n + " nodes and " + post_e + " edges are level !!!!!!!");
            return false;
        } else if (deletedEdges.size() > this.percentage * this.numberOfEdges) { //case 4
            this.deletedEdges_layer.add(deletedEdges);
            System.out.println("Deleted # of edges in this level : " + deletedEdges.size() + "  \nthe # of edges are removed in the last step in this level is : " + sub_step_deletedEdges.size());
            System.out.println("There are " + post_n + " nodes and " + post_e + " edges are level !!!!!!!");
            System.out.println("---------------------------------------------------");
            return true;
        } else { //other cases
            System.out.println(deleted + " " + deletedEdges.size() + " " + deletedNodes.size() + " " + sub_step_deletedEdges.size() + " " + sub_step_deletedNodes.size());
            System.out.println("The case other than the listed four case, need to re-think it !!!!!!!!!!!!!!!");
            System.exit(0);
            return false;
        }
    }

    private void buildClusterFolder(int level, int sub_step_number, NodeClusters process_clusters) {
        String cluster_index_foler = ParsedOptions.indexFolder + "/clusters/" + level + "/" + sub_step_number;

        File folder = new File(cluster_index_foler);
        if (!folder.exists()) {
            folder.mkdirs();
            System.out.println("Create the index folder : " + folder);
        }

        try {
            for (Map.Entry<Integer, NodeCluster> cluster_entry : process_clusters.clusters.entrySet()) {
                int cluster_id = cluster_entry.getKey();
                NodeCluster cluster = cluster_entry.getValue();
                System.out.println("              cluster information: " + cluster_id + " " + cluster.node_list.size());

                BufferedWriter writer = new BufferedWriter(new FileWriter(cluster_index_foler + "/" + cluster_id));
                for (long nid : cluster.node_list) {
                    writer.write(nid + " ");
                }
                writer.close();
            }

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    private void checkIndexFolderExisted(int indexLevel) {

        //create the folder for the index of the backbone (highways)
        String sub_folder_str = ParsedOptions.indexFolder + "/level" + indexLevel;
        File sub_folder_f = new File(sub_folder_str);
        if (!sub_folder_f.exists()) {
            sub_folder_f.mkdirs();
        }

        //create the folder for the index of information in each cluster
        String sub_inner_index_folder_str = ParsedOptions.indexFolder + "/inner/level" + indexLevel;
        File sub_inner_index_f = new File(sub_inner_index_folder_str);
        if (!sub_inner_index_f.exists()) {
            sub_inner_index_f.mkdirs();
        }
    }

    /**
     * Remove single edges in current level, and put the deleted node in to @deletedNodes.
     *
     * @param deletedNodes the removed nodes
     * @return the removed edges
     */
    private HashSet<Long> removeSingletonEdgesInForests(HashSet<Long> deletedNodes) {
        int sum_single = 0;

        HashSet<Long> deletedEdges = new HashSet<>();

        while (hasSingletonPairs()) {
            long pre_edge_num = neo4j.getNumberofEdges();
            long pre_node_num = neo4j.getNumberofNodes();
            long pre_degree_num = degree_pairs.size();

            try (Transaction tx = graphdb.beginTx()) {
                //if the degree pair whose key or value is equal to 1, it means it is a single edge


                HashSet<Pair<Integer, Integer>> degree_keys = new HashSet<>();
                degree_keys.addAll(this.degree_pairs.keySet());

                long update_time = 0, delete_time = 0;

                for (Pair<Integer, Integer> e : degree_keys) {
                    if (e.getValue() == 1 || e.getKey() == 1) {
                        ArrayList<Long> list = new ArrayList<>();
                        list.addAll(this.degree_pairs.get(e));
                        for (long rel_id : list) {
                            sum_single++;
                            Relationship r = graphdb.getRelationshipById(rel_id);

                            long update_st = System.nanoTime();
                            updateDegreePairs(rel_id);
                            update_time += (System.nanoTime() - update_st);

                            long delete_st = System.nanoTime();
                            deleteRelationshipFromDB(r, deletedNodes);
                            delete_time += System.nanoTime() - delete_st;

                            deletedEdges.add(r.getId());

                            if (sum_single % 50000 == 0) {
                                tx.success();
                            }

                            int echo = 100000;
                            if (sum_single % echo == 0) {
                                System.out.println("Removed Single Edges:" + sum_single +
                                        " " + (update_time * 1.0 / (1000000 * echo)) + "ms" +
                                        " " + (delete_time * 1.0 / (1000000 * echo)) + "ms" +
                                        "  ............................");
                                update_time = delete_time = 0;
                            }
                        }
                    }
                    tx.success();
                }

                System.out.println("delete single in forests: pre:" + pre_node_num + " " + pre_edge_num + " " + pre_degree_num + " " +
                        "single_edges:" + sum_single + " " +
                        "post:" + neo4j.getNumberofNodes() + " " + neo4j.getNumberofEdges() + " " +
                        "dgr_paris:" + degree_pairs.size());

                tx.success();
                System.out.println("Finished the submission of the operations ............................... remained size of the degree pair " + this.degree_pairs.size());
            } catch (NotFoundException e) {
                System.out.println("no property found exception !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                e.printStackTrace();
                System.exit(0);
            }

            System.out.println("==================================");

//            getDegreePairs();
        }


        return deletedEdges;
    }

    private ArrayList<ArrayList<Long>> buildSingleEdgePaths(int level, int sub_step_number, HashSet<Long> deletedEdges) {

        //find all the information in previous level graph, because the information is removed in current layer graph
        String graph_db_folder = this.base_db_name + "_Level" + level;
        Neo4jDB neo4j_level = new Neo4jDB(graph_db_folder);

        neo4j_level.startDB(true);

        HashSet<Long> remained_nodes = getNodeListAtCurrentLevel();

        ArrayList<ArrayList<Long>> single_edge_paths = new ArrayList<>();
        ArrayList<HashSet<Long>> single_paths = new ArrayList<>();

        try (Transaction tx = neo4j_level.graphDB.beginTx()) {
            for (long rel_id : deletedEdges) {
                Relationship rel = neo4j_level.graphDB.getRelationshipById(rel_id);
                long start_id = rel.getStartNodeId();
                long end_id = rel.getEndNodeId();

                ArrayList<Long> p = new ArrayList<>();
                p.add(start_id);
                p.add(end_id);

//                if(start_id==4435 || end_id==4435){
//                    System.out.println("===============================");
//                    System.exit(0);
//                }
//                ArrayList<Long> left_p = new ArrayList<>();
//                ArrayList<Long> right_p = new ArrayList<>();
//
//                for (int idx = 0; idx < single_edge_paths.size(); ) {
//                    ArrayList<Long> p = single_edge_paths.get(idx);
//                    if (p.get(0) == start_id || p.get(p.size() - 1) == start_id) {
//                        left_p = p;
//                        single_edge_paths.remove(idx);
//                    } else if (p.get(0) == end_id || p.get(p.size() - 1) == end_id) {
//                        right_p = p;
//                        single_edge_paths.remove(idx);
//                    } else {
//                        idx++;
//                    }
//                }
//
//                ArrayList<Long> p = new ArrayList<>();
//                if (right_p.isEmpty() && left_p.isEmpty()) {
//                    p.add(start_id);
//                    p.add(end_id);
//                } else if (!left_p.isEmpty() && right_p.isEmpty()) {
//                    if (left_p.get(0) == start_id) {
//                        left_p.add(0, end_id);
//                    } else if (left_p.get(left_p.size() - 1) == start_id) {
//                        left_p.add(end_id);
//                    }
//                    p.addAll(left_p);
//                } else if (left_p.isEmpty() && !right_p.isEmpty()) {
//                    if (right_p.get(0) == end_id) {
//                        right_p.add(0, start_id);
//                    } else if (right_p.get(right_p.size() - 1) == end_id) {
//                        right_p.add(start_id);
//                    }
//
//                    p.addAll(right_p);
//                } else if (!left_p.isEmpty() && !right_p.isEmpty()) {
//                    if (left_p.get(0) == start_id) {
//                        if (right_p.get(right_p.size() - 1) == end_id) {
//                            right_p.addAll(left_p);
//                        } else if (right_p.get(0) == end_id) {
//                            Collections.reverse(right_p);
//                            right_p.addAll(left_p);
//                        }
//
//                        p.addAll(right_p);
//                    } else if (left_p.get(left_p.size() - 1) == start_id) {
//                        if (right_p.get(right_p.size() - 1) == end_id) {
//                            Collections.reverse(right_p);
//                            left_p.addAll(right_p);
//                        } else if (right_p.get(0) == end_id) {
//                            left_p.addAll(right_p);
//                        }
//
//                        p.addAll(left_p);
//                    }
//                }
                single_edge_paths.add(p);
            }

            int before = 0;
            int after = 0;

//            do {
//                before = single_edge_paths.size();
//                mergePaths(single_edge_paths);
//                after = single_edge_paths.size();
//            }
//            while (before != after);


            for (ArrayList<Long> p : single_edge_paths) {
                HashSet<Long> new_p = new HashSet<>(p);
                single_paths.add(new_p);
            }

//            System.out.println(before + " ===> " + after + "  " + single_edge_paths.size() + "  " + single_paths.size());
            System.out.println("--------------------------------------------------");

            do {
                before = single_paths.size();
                mergeNonSinglePaths(single_paths);
                after = single_paths.size();
                System.out.println(before + " ===> " + after);
//                System.exit(0);
            } while (before != after);

            tx.success();
        }


        if (!single_paths.isEmpty()) {
            String cluster_index_foler = ParsedOptions.indexFolder + "/clusters/" + level + "/" + sub_step_number;

            File folder = new File(cluster_index_foler);
            if (!folder.exists()) {
                folder.mkdirs();
                System.out.println("Create the index folder : " + folder);
            }

            String single_path_idx_file = ParsedOptions.indexFolder + "/clusters/" + level + "/" + sub_step_number + "/before_single_path.idx";
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(single_path_idx_file)));
                for (HashSet<Long> p : single_paths) {

                    StringBuffer sb = new StringBuffer();
                    for (long node_id : p) {
                        sb.append(node_id).append(" ");
                    }

                    sb.append("| ");

                    for (long node_id : p) {
                        if (remained_nodes.contains(node_id)) {
                            sb.append(node_id).append(" ");
                        }
                    }
                    writer.write(sb.toString() + "\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("############# write single paths " + single_edge_paths.size() + " (Before) to " + single_path_idx_file);
        }
        neo4j_level.closeDB();
//        System.exit(0);
        return single_edge_paths;
    }

    private void mergeNonSinglePaths(ArrayList<HashSet<Long>> single_paths) {
        HashSet<Integer> merged_path_idx = new HashSet<>();

        for (int idx = 0; idx < single_paths.size(); idx++) {

            if (merged_path_idx.contains(idx)) {
                continue;
            }

            HashSet<Long> p = single_paths.get(idx);
            HashSet<Integer> added_idx = new HashSet<>();

            for (long node_id : p) {
                for (int m_idx = (idx + 1); m_idx < single_paths.size(); m_idx++) {
                    if (m_idx != idx && !merged_path_idx.contains(m_idx)) {
                        HashSet<Long> other_p = single_paths.get(m_idx);
                        if (other_p.contains(node_id)) {
                            added_idx.add(m_idx);
                            merged_path_idx.add(m_idx);
                        }
                    }
                }
            }

//            System.out.println(single_paths.size());


            for (int a_idx : added_idx) {
                p.addAll(single_paths.get(a_idx));
//                if (p.contains(4435l)) {
//                    System.out.println("merged to "+p);
//                }
            }
        }

//        for (HashSet<Long> p : single_paths) {
//            if (p.contains(4435l)) {
//                System.out.println(p);
//            }
//        }


        int removed_record = 0;
        ArrayList<Integer> list = new ArrayList<>(merged_path_idx);
        Collections.sort(list);
//        System.out.println(list);
        for (int m_idx : list) {
//            if(single_paths.get(m_idx - removed_record).contains(4435l)){
//                System.out.println(single_paths.get(m_idx - removed_record));
//            }
            single_paths.remove(m_idx - removed_record);
            removed_record++;
        }

//        System.out.println("=====================================");
//
//        for (HashSet<Long> p : single_paths) {
//            if (p.contains(4435l)) {
//                System.out.println(p);
//            }
//        }
//        System.exit(0);
    }

    private void mergePaths(ArrayList<ArrayList<Long>> single_edge_paths) {
        HashSet<Integer> merged_path_idx = new HashSet<>();

        for (int idx = 0; idx < single_edge_paths.size(); idx++) {
            ArrayList<Long> p = single_edge_paths.get(idx);

            for (int m_idx = idx + 1; m_idx < single_edge_paths.size(); m_idx++) {

                if (m_idx != idx && !merged_path_idx.contains(m_idx)) {
                    ArrayList<Long> other_p = single_edge_paths.get(m_idx);
                    if (other_p.get(0) == (p.get(0))) {
                        Collections.reverse(p);
                        p.remove(p.size() - 1);
                        p.addAll(other_p);
                        merged_path_idx.add(m_idx);
                    } else if (other_p.get(0) == p.get(p.size() - 1)) {
                        other_p.remove(0);
                        p.addAll(other_p);
                        merged_path_idx.add(m_idx);
                    } else if (other_p.get(other_p.size() - 1) == p.get(0)) {
                        Collections.reverse(p);
                        other_p.remove(other_p.size() - 1);
                        Collections.reverse(other_p);
                        p.addAll(other_p);
                        merged_path_idx.add(m_idx);
                    } else if (other_p.get(other_p.size() - 1) == p.get(p.size() - 1)) {
                        other_p.remove(other_p.size() - 1);
                        Collections.reverse(other_p);
                        p.addAll(other_p);
                        merged_path_idx.add(m_idx);
                    }
                }
            }
        }


        int removed_record = 0;
        ArrayList<Integer> list = new ArrayList<>();
        list.addAll(merged_path_idx);
        Collections.sort(list);
//        System.out.println(list);
        for (int m_idx : list) {
            single_edge_paths.remove(m_idx - removed_record);
            removed_record++;
        }

    }

    private boolean hasSingletonPairs() {
        System.out.print("Calling the function of the checking single pairs  =====> ");
        for (Map.Entry<Pair<Integer, Integer>, HashSet<Long>> e : this.degree_pairs.entrySet()) {
            if (e.getKey().getValue() == 1 || e.getKey().getKey() == 1) {
                System.out.println("Still have single edges ?  " + true + "  " + this.degree_pairs.size());
                return true;
            }
        }
        System.out.println("Still have single edges ?  " + false + "  " + this.degree_pairs.size());
        return false;
    }

    /**
     * Update the degree pair structure after removing <code>rel_id</code>
     * 1) find the key of the <code>rel_id</code>
     * 2) remove <code>rel_id</code> from degree pair where the key is equals to key of the <code>rel_id</code>
     * 3) Find the outgoing edges of end node of <code>rel_id</code> whose degree is 1.
     * 4) Update the information of outgoing edges in the degree pair
     *
     * @param rel_id the removed single edge
     */
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

    private void deleteRelationshipFromDB(Relationship r, HashSet<Long> deletedNodes) {
        try (Transaction tx = this.neo4j.graphDB.beginTx()) {
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

    /**
     * find the skyline paths from each deleted node in sub_step_deletedNodes to the remained nodes in current level
     *
     * @param level
     * @param sub_step_deletedEdges
     * @param sub_step_deletedNodes
     */
    private void indexBuildSingleEdgesAtLevel(int level, HashSet<
            Long> sub_step_deletedEdges, HashSet<Long> sub_step_deletedNodes) {
        System.out.println("Build single edge index at level " + level + " ===============================================================  ");

        String sub_folder_str = ParsedOptions.indexFolder + "/level" + level;
        System.out.println("Build the single index at " + sub_folder_str);

        //find all the information in previous level graph, because the information is removed in current layer graph
        String graph_db_folder = this.base_db_name + "_Level" + level;
        Neo4jDB neo4j_level = new Neo4jDB(graph_db_folder);
        neo4j_level.startDB(true);
        GraphDatabaseService graphdb_level = neo4j_level.graphDB;

        //check the node remained in after the deletion (remained in current layer graph)
        HashSet<Long> remained_nodes = getNodeListAtCurrentLevel();

        try (Transaction tx = graphdb_level.beginTx()) {
            for (long n_id : sub_step_deletedNodes) {
                HashMap<Long, myQueueNode> tmpStoreNodes = new HashMap();
                myQueueNode snode = new myQueueNode(n_id, neo4j_level);
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
                            new_paths = p.expand(neo4j_level, sub_step_deletedEdges, sub_step_deletedEdges);
                            for (path np : new_paths) {
                                myQueueNode next_n;
//                                System.out.println("    " + np);
                                if (tmpStoreNodes.containsKey(np.endNode)) {
                                    next_n = tmpStoreNodes.get(np.endNode);
                                } else {
                                    next_n = new myQueueNode(snode, np.endNode, neo4j_level);
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

                if (sum != 0) {
                    /**clean the built index file*/
                    File idx_file = new File(sub_folder_str + "/" + n_id + ".idx");

                    //previous has the skyline information from n_id to other nodes (deleted in previous sub step)

                    BufferedWriter writer = new BufferedWriter(new FileWriter(idx_file.getAbsolutePath(), true));
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
        } catch (IOException e) {
            neo4j_level.closeDB();
            e.printStackTrace();
        }
        neo4j_level.closeDB();
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

    private NodeClusters removeLowerDegreePairEdgesByThreshold
            (HashSet<Long> deletedNodes, HashSet<Long> deletedEdges) {
        NodeClusters result_clusters = new NodeClusters();

        System.out.println(neo4j.DB_PATH);
        System.out.println(neo4j.getNumberofNodes());

        NodeClusters node_clusters;
        if (ParsedOptions.clustering_method.equals("node")) {
            NodeCoefficientPartition node_partition = new NodeCoefficientPartition(neo4j, this.cluster_size, this.min_size);
            node_clusters = node_partition.CoefficientPartition();
        } else if (ParsedOptions.clustering_method.equals("bfs")) {
            BFSPartition bsf_partition = new BFSPartition(neo4j, this.cluster_size, this.min_size);
            node_clusters = bsf_partition.partition();
        } else {
            node_clusters = null;
        }


        if (ParsedOptions.msize != 0) {
            node_clusters.mergeSmallCluster(ParsedOptions.msize, neo4j);
        }

        System.out.println(neo4j.getNumberofNodes() + "   " + neo4j.getNumberofEdges());
//        System.out.println(node_clusters.clusters.get(0).node_list.size());
//        neo4j.saveGraphToTextFormation(ParsedOptions.output_graphIndo_foler + "/" + ParsedOptions.db_name + "/" + ParsedOptions.db_name + "_Level0");
//        for (Map.Entry<Integer, NodeCluster> e : node_clusters.clusters.entrySet()) {
//            for (Long c : e.getValue().node_list) {
//                System.out.println(e.getKey() + " " + c);
//            }
//        }
//
//        System.exit(0);

        HashSet<Long> dijrels = new HashSet<>();

        for (Map.Entry<Integer, NodeCluster> e : node_clusters.clusters.entrySet()) {
            int k = e.getKey();
            NodeCluster v = e.getValue();

            if (k == 0 && v.node_list.size() != 0) {
                result_clusters.clusters.put(k, v);
            } else if (v.node_list.size() >= this.min_size && k != 0) {
//                System.out.println("=================================================================================");
                System.out.print("Cluster :" + k + "  nodes_size:" + v.node_list.size() + " border_size:" + v.border_node_list.size());
                ClusterSpanningTree tree = new ClusterSpanningTree(neo4j, true, v.node_list);

                tree.EulerTourStringWiki();
                int num_removed_edges = tree.rels.size() - tree.SpTree.size();
                System.out.print("     size of spanning tree : " + tree.SpTree.size() + "      # of rels:" + tree.rels.size() + " ---- removed " + (num_removed_edges) + " edges");

                long bbs_rt_start = System.currentTimeMillis();

//                tree.SpanningTreeWithAllProperties();
//                tree.BBS(v.border_node_list);
//                tree.Dijkstra(v.border_node_list);

                System.out.println("  " + (System.currentTimeMillis() - bbs_rt_start) + " ms");
                System.out.println("====================================================================================");

                NodeCluster cluster = new NodeCluster(k, this.cluster_size);
                cluster.addAll(v);
                cluster.addRels(tree.rels);
                cluster.num_removed_edges = num_removed_edges;
                result_clusters.clusters.put(k, cluster);
//                cluster.printAllNodes();

                try (Transaction tx = neo4j.graphDB.beginTx()) {
                    for (long rel_id : tree.rels) {
                        Relationship rel = neo4j.graphDB.getRelationshipById(rel_id);
                        if (!tree.SpTree.contains(rel.getId())) {
                            deletedEdges.add(rel.getId());
                            deleteRelationshipFromDB(rel, deletedNodes);
                        }
                    }

                    tx.success();
                }
            }
        }
        System.out.println(neo4j.getNumberofNodes() + "   " + neo4j.getNumberofEdges());
        return result_clusters;
    }

    /**
     * Remove the single edges from given cluster, return the deleted edges.
     * The deleted nodes information are updated in the deletedNodes structure.
     * In order to check if one rel is in current cluster, checking if the start node and end node are in the node list
     *
     * @param cluster_id   the cluster id
     * @param cluster      the cluster
     * @param deletedNodes the structure that is used to store the deleted nodes
     * @return the removed rel in this cluster
     */
    private HashSet<Long> removeSingletonEdgesInCluster(int cluster_id, NodeCluster
            cluster, HashSet<Long> deletedNodes) {
        long rt_start_remove_single_in_cluster = System.nanoTime();
        int sum_single = 0;

        long pre_edge_num = neo4j.getNumberofEdges();
        long pre_node_num = neo4j.getNumberofNodes();
        long pre_degree_num = degree_pairs.size();

        HashSet<Long> deletedEdges = new HashSet<>();

        int pre_deleted = deletedEdges.size();
        int count = -1;

        while (count != 0) {
            try (Transaction tx = graphdb.beginTx()) {
                //if the degree pair whose key or value is equal to 1, it means it is a single edge
                HashSet<Pair<Integer, Integer>> degree_keys = new HashSet<>(this.degree_pairs.keySet());

                for (Pair<Integer, Integer> e : degree_keys) {
                    if (e.getValue() == 1 || e.getKey() == 1) {

                        ArrayList<Long> list = new ArrayList<>(this.degree_pairs.get(e));

                        for (long rel_id : list) {
                            if (cluster.rels.contains(rel_id)) {
                                Relationship r = graphdb.getRelationshipById(rel_id);
                                //Only remove the edges in the cluster
                                if (cluster.node_list.contains(r.getStartNodeId()) && cluster.node_list.contains(r.getEndNodeId())) {
                                    sum_single++;
                                    updateDegreePairs(rel_id);
                                    deleteRelationshipFromDB(r, deletedNodes);
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

    private void updateExistedIndexInCurrentLevel(int level, HashSet<Long> sub_step_updated_index_nodes) {
        System.out.println("Updating the created edge indexes at level " + level + " .......................................");
        HashSet<Long> remained_nodes = getNodeListAtCurrentLevel();
        String index_folder = ParsedOptions.indexFolder + "/level" + level;
        List<Long> idx_list = Arrays.stream(new File(index_folder).listFiles(new idxFileFilter())).map(e -> {
            String fname = e.getName();
            return Long.parseLong(fname.substring(0, fname.lastIndexOf(".idx")));
        }).collect(Collectors.toList());

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
                        System.out.println(sub_step_updated_index_nodes.contains(high_way_node));
                        System.out.println(high_way_node);
                        System.out.println("Find highway node error!!!!!");
                        System.exit(0);
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

    /**
     * @param n_id  the node id
     * @param level the index level
     * @return <target node ---><skyline paths from n_id to target node >> the skyline index of the node id in given level
     */
    private HashMap<Long, ArrayList<double[]>> readIndex(long n_id, int level) {
        HashMap<Long, ArrayList<double[]>> skylines = new HashMap<>();
        String sub_folder_str = ParsedOptions.indexFolder + "/level" + level;
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

    /**
     * Find the index at @level, which means the information that is abstracted from previous level to current level
     *
     * @param level   the level of the current index
     * @param de      the edges are deleted in current sub step
     * @param cluster which cluster the index needs to be built in
     * @return the list of node id that is update its highway index information, used to update single highway index
     */
    private HashSet<Long> indexBuildAtLevel(int level, HashSet<Long> de, NodeCluster cluster) {

        long rt_over_all = 0;

        HashSet<Long> updated_index_nodes = new HashSet<>();

        //find all the information in previous level graph, because the information is removed in current layer graph
        String graph_db_folder = this.base_db_name + "_Level" + level;
        Neo4jDB neo4j_level = new Neo4jDB(graph_db_folder);
        neo4j_level.startDB(true);
        GraphDatabaseService graphdb_level = neo4j_level.graphDB;

        //check the node remained in after the deletion (remained in current layer graph)
        HashSet<Long> remained_nodes = getNodeListAtLevel(cluster.node_list);

        //if the last layer graph is empty, don't build the index.
        if (remained_nodes.size() == 0) {
            neo4j_level.closeDB();
            return updated_index_nodes;
        }

        try (Transaction tx = graphdb_level.beginTx()) {
            int node_idx = 1;

            for (long n_id : cluster.node_list) {

                long build_start = System.currentTimeMillis();

                HashMap<Long, ArrayList<double[]>> skylines = readIndex(n_id, level);

                HashMap<Long, myQueueNode> tmpStoreNodes = new HashMap();
                myQueueNode snode = new myQueueNode(n_id, neo4j_level);
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
                            new_paths = p.expand(neo4j_level, de, cluster.rels);

                            for (path np : new_paths) {
                                myQueueNode next_n;
                                if (tmpStoreNodes.containsKey(np.endNode)) {
                                    next_n = tmpStoreNodes.get(np.endNode);
                                } else {
                                    next_n = new myQueueNode(snode, np.endNode, neo4j_level);
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

                long rt_used_in_chekcing_sum = System.currentTimeMillis() - build_start - rt_used_in_expansion;

                if (sum != 0) {
                    updated_index_nodes.add(n_id);

                    /**clean the built index file*/
                    String sub_folder_str = ParsedOptions.indexFolder + "/level" + level;
                    File idx_file = new File(sub_folder_str + "/" + n_id + ".idx");

                    //previous has the skyline information from n_id to other nodes (deleted in previous sub step)
                    if (skylines != null) {
                        for (Map.Entry<Long, myQueueNode> e : tmpStoreNodes.entrySet()) {
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

                long rt_used_in_writing_to_file = System.currentTimeMillis() - build_start - rt_used_in_expansion - rt_used_in_chekcing_sum;
                long iter_rt = System.currentTimeMillis() - build_start;
                rt_over_all += iter_rt;
                tx.success();
            }
            tx.success();
        } catch (IOException e) {
            e.printStackTrace();
        }
        neo4j_level.closeDB();
        return updated_index_nodes;
    }

    /**
     * Find the list of node that is remained in current layer, by given a specific cluster
     *
     * @param node_list the node list in a cluster
     * @return the list of node that is remained in current layer.
     */
    private HashSet<Long> getNodeListAtLevel(HashSet<Long> node_list) {
        HashSet<Long> result = new HashSet<>();
//        long src_node_id = -1;
//        int i = 1;
        try (Transaction tx = neo4j.graphDB.beginTx()) {
            for (long node_id_in_list : node_list) {
//                src_node_id = node_id_in_list;
                try {
                    long node_id = neo4j.graphDB.getNodeById(node_id_in_list).getId();
//                    System.out.println(i++ + "  " + node_id);
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

    private void updateSingleNodeIndexes(int level, HashSet<
            Long> single_node_list, HashSet<Long> sub_step_updated_index_nodes) {
        System.out.println("Updating the single edge indexes .......................................");
        HashSet<Long> remained_nodes = getNodeListAtCurrentLevel();

        for (long n_id : single_node_list) {

            String sub_folder_str = ParsedOptions.indexFolder + "/level" + level;
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

    /**
     * Merge the <2,2> degree edges by using current neo4j object.
     *
     * @param level the level where to build the index, not the current processing graph's level
     * @return
     */
    private HashSet<Long> handleTwoDegreePairSegments(int level) {

        int checklevel = 3;
        boolean debugged = false;


        System.out.println("Call the function handleTwoDegreePairSegments to remove the <2,2> pair edges in level " + level);
        System.out.println("The operated graph  :" + neo4j.graphDB);


        if (debugged && level == checklevel) {
            this.degree_pairs.forEach((v, k) -> {
                System.out.println(v + "   " + k.size());
            });
        }

        HashSet<Long> updated_node_list = new HashSet<>();


        //if only a circle left, jump out the loop.
        if (neo4j.getNumberofNodes() == neo4j.getNumberofEdges()) {
            return updated_node_list;
        }

        Pair<Integer, Integer> t_degree_pair_key = new MutablePair<>(2, 2);
        ArrayList<Long> rels_ids = new ArrayList<>();


        HashSet<Long> two_degree_edges = this.degree_pairs.get(t_degree_pair_key);
        if (two_degree_edges == null || two_degree_edges.size() == 0) {
            return updated_node_list;
        } else {
            rels_ids.addAll(two_degree_edges);
        }

        HashMap<Long, Boolean> visited = new HashMap<>();
        rels_ids.forEach(r -> visited.put(r, false));
        System.out.println("There are " + rels_ids.size() + " <2,2> rels.");

        if (debugged && level == checklevel) {
            rels_ids.forEach(e -> System.out.println(e));
        }

        ArrayList<Segment> single_paths = new ArrayList<>();

        for (long r_id : rels_ids) {
            if (visited.get(r_id)) {
                continue;
            } else {
                try (Transaction tx = neo4j.graphDB.beginTx()) {

                    long st_contract = System.currentTimeMillis();

                    visited.put(r_id, true);

                    if (debugged && level == checklevel) {
                        System.out.println(r_id);
                    }

                    Segment s = new Segment();

                    Relationship rel = neo4j.graphDB.getRelationshipById(r_id);

                    assert rel.getStartNode().getDegree(Direction.BOTH) == 2 && rel.getEndNode().getDegree(Direction.BOTH) == 2;

                    Node start_node = rel.getStartNode();
                    Node end_node = rel.getEndNode();

                    ArrayList<Relationship> outgoing_list = neo4j.getoutgoingEdge(start_node);
                    Relationship outgoing_edge_from_start_node = outgoing_list.get(0).getId() == r_id ? outgoing_list.get(1) : outgoing_list.get(0);

                    outgoing_list = neo4j.getoutgoingEdge(end_node);
                    Relationship outgoing_edge_from_end_node = outgoing_list.get(0).getId() == r_id ? outgoing_list.get(1) : outgoing_list.get(0);

                    boolean isTerminateEdge_start = isTerminateEdge(outgoing_edge_from_start_node);
                    boolean isTerminateEdge_end = isTerminateEdge(outgoing_edge_from_end_node);

                    if (debugged && level == checklevel) {
                        System.out.println("    " + outgoing_edge_from_start_node + "[" + isTerminateEdge_start + "]  <<-- " + rel + "[" + isTerminateEdge(rel) + "] -->>" + outgoing_edge_from_end_node + "[" + isTerminateEdge_end + "]");
                    }

                    if (isTerminateEdge_start || isTerminateEdge_end) {
                        long start_node_id;
                        Relationship start_rel;
                        if (isTerminateEdge_start) {
                            start_rel = outgoing_edge_from_start_node;
                        } else {
                            start_rel = outgoing_edge_from_end_node;
                        }

                        start_node_id = start_rel.getStartNode().getDegree(Direction.BOTH) != 2 ? start_rel.getStartNodeId() : start_rel.getEndNodeId();
                        s.addTerminateRel(start_rel.getId(), start_node_id, true);
                        visited.put(start_rel.getId(), true);

                        if (debugged && level == checklevel) {
                            System.out.println("    " + start_node_id + " " + start_rel);
                        }


                        Relationship be = rel;
                        Relationship next_e;

                        if (isTerminateEdge_start) {
                            next_e = outgoing_edge_from_end_node;
                        } else {
                            next_e = outgoing_edge_from_start_node;
                        }

                        while (!isTerminateEdge(be)) {
                            s.addNormalRel(be.getId());
                            visited.put(be.getId(), true);

                            if (debugged && level == checklevel) {
                                System.out.println("                " + be + "   " + next_e);
                            }

                            Node common_node = getCommonNodeID(be, next_e);

                            if (common_node == null) {
                                System.out.println("Find common node id error !!!!!!! ");
                                System.exit(0);
                            }

                            be = next_e;
                            ArrayList<Relationship> next_outgoing_list = neo4j.getoutgoingEdge(next_e.getOtherNode(common_node));
                            if (next_outgoing_list.size() == 2) {
                                next_e = next_outgoing_list.get(0).getId() == be.getId() ? next_outgoing_list.get(1) : next_outgoing_list.get(0);
                            } else {
                                break;
                            }
                        }

                        long end_node_id;

                        end_node_id = be.getStartNode().getDegree(Direction.BOTH) != 2 ? be.getStartNodeId() : be.getEndNodeId();
                        s.addTerminateRel(be.getId(), end_node_id, false);
                        visited.put(be.getId(), true);
                        if (debugged && level == checklevel) {
                            System.out.println("    " + end_node_id + " " + be);
                        }

                    } else {
                        long start_node_id, end_node_id;

                        //backward finding process
                        Relationship be = rel;
                        Relationship previous_e = outgoing_edge_from_start_node;

//                        int count = 1;
                        while (!isTerminateEdge(be)) {
                            s.addNormalRel(be.getId(), true);
                            visited.put(be.getId(), true);
                            Node common_node = getCommonNodeID(be, previous_e);
//                            System.out.println("    " + be + " " + previous_e + " " + common_node);

                            be = previous_e;
                            ArrayList<Relationship> prev_outgoing_list = neo4j.getoutgoingEdge(previous_e.getOtherNode(common_node));
                            if (prev_outgoing_list.size() == 2) {
                                previous_e = prev_outgoing_list.get(0).getId() == be.getId() ? prev_outgoing_list.get(1) : prev_outgoing_list.get(0);
                            } else {
                                break;
                            }
                        }
//                        System.out.println("    " + be + " " + previous_e);
                        start_node_id = be.getStartNode().getDegree(Direction.BOTH) == 2 ? be.getEndNodeId() : be.getStartNodeId();
                        s.addTerminateRel(be.getId(), start_node_id, true);
                        visited.put(be.getId(), true);

//                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`");

                        //forward finding process
                        be = rel;
                        Relationship next_e = outgoing_edge_from_end_node;
                        int count = 0;
                        while (!isTerminateEdge(be)) {
                            //Jump the insertion of the rel, since it is already added in the backwards finding process
                            if (count == 0) {
                                count++;

                                Node common_node = getCommonNodeID(be, next_e);
//                                System.out.println("    " + be + " " + next_e + " " + common_node);

                                be = next_e;
                                ArrayList<Relationship> next_outgoing_list = neo4j.getoutgoingEdge(next_e.getOtherNode(common_node));

                                if (next_outgoing_list.size() == 2) {
                                    next_e = next_outgoing_list.get(0).getId() == be.getId() ? next_outgoing_list.get(1) : next_outgoing_list.get(0);
                                    continue;
                                } else {
                                    break;
                                }
                            }

                            s.addNormalRel(be.getId());
                            visited.put(be.getId(), true);

                            Node common_node = getCommonNodeID(be, next_e);
//                            System.out.println("    " + be + " " + next_e + " " + common_node);

                            be = next_e;
                            ArrayList<Relationship> next_outgoing_list = neo4j.getoutgoingEdge(next_e.getOtherNode(common_node));
                            if (next_outgoing_list.size() == 2) {
                                next_e = next_outgoing_list.get(0).getId() == be.getId() ? next_outgoing_list.get(1) : next_outgoing_list.get(0);
                            } else {
                                break;
                            }
                        }
                        end_node_id = be.getStartNode().getDegree(Direction.BOTH) == 2 ? next_e.getEndNodeId() : next_e.getStartNodeId();
//                        System.out.println("    " + be + " " + next_e + " ");
                        s.addTerminateRel(be.getId(), end_node_id, false);
                        visited.put(be.getId(), true);
                    }

                    long end_expansion = System.currentTimeMillis();

                    s.updateCosts(neo4j);
                    long end_calculate_costs = System.currentTimeMillis();
//
                    HashMap<Long, HashMap<Long, ArrayList<double[]>>> nodes_in_segment_index = s.buildIndexForNodes(neo4j);

                    long end_building_index = System.currentTimeMillis();

                    long new_rel_id = s.contract(neo4j);
//
//
                    for (long n_id : nodes_in_segment_index.keySet()) {
                        updated_node_list.add(n_id);
                    }
                    insertIndex(level, nodes_in_segment_index);

                    long end_writing_to_disk = System.currentTimeMillis();
//
                    System.out.println("segment " + s + " is created, there are " + s.rels.size() + " edges are contracted and  "
                            + s.normal_node_list.size() + " nodes are removed . " +
                            (new_rel_id != -1 ? "new created rel id is " + new_rel_id : " contract to a single node, [" + s.start_node + "," + s.end_node + "], do not need to create new edge.") +
                            " total_rt:" + (end_writing_to_disk - st_contract) +
                            " expansion_rt:" + (end_expansion - st_contract) +
                            " cost_rt:" + (end_calculate_costs - end_expansion) +
                            " index_rt:" + (end_building_index - end_calculate_costs) +
                            " write_rt:" + (end_writing_to_disk - end_building_index));


                    tx.success();
                    single_paths.add(s);
                }
            }
        }


        if (!single_paths.isEmpty()) {
            String cluster_index_foler = ParsedOptions.indexFolder + "/clusters/" + level + "/";

            File folder = new File(cluster_index_foler);
            if (!folder.exists()) {
                folder.mkdirs();
                System.out.println("Create the index folder : " + folder);
            }

            String single_path_idx_file = ParsedOptions.indexFolder + "/clusters/" + level + "/single_segments.idx";
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(single_path_idx_file)));
                for (Segment s : single_paths) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(s.start_node).append(" ");
                    for (long node_id : s.normal_node_list) {
                        sb.append(node_id).append(" ");
                    }
                    sb.append(s.end_node);
                    writer.write(sb.toString() + "\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("############# write single segments " + single_paths.size() + " to " + single_path_idx_file);

        }

        return updated_node_list;
    }

    /**
     * Add the outgoing edge e information to the segment s.
     * If the degree pair of the outgoing edge is <2,2>, e is add as normal component of s. Return the false.
     * Else, it means the edge e is connect to one termination node which degree is higher than 2. e is added as a terminated edge to s. Return true.
     *
     * @param outgoing_edge the edge e
     * @return return true if the edge is added a termination edge, else return false, if the edge is added as a normal edge
     */
    private boolean isTerminateEdge(Relationship outgoing_edge) {
        return outgoing_edge.getStartNode().getDegree(Direction.BOTH) != 2 || outgoing_edge.getEndNode().getDegree(Direction.BOTH) != 2;
    }

    /**
     * Get the common node id (the connected node) of two given relationships/edges
     *
     * @param be
     * @param next_e
     * @return
     */
    private Node getCommonNodeID(Relationship be, Relationship next_e) {
        long be_s_id = be.getStartNodeId();
        long be_e_id = be.getEndNodeId();
        long next_s_id = next_e.getStartNodeId();
        long next_e_id = next_e.getEndNodeId();

        if (be_s_id == next_s_id) {
            return be.getStartNode();
        } else if (be_s_id == next_e_id) {
            return be.getStartNode();
        } else if (be_e_id == next_s_id) {
            return be.getEndNode();
        } else if (be_e_id == next_e_id) {
            return be.getEndNode();
        }
        return null;
    }

    /**
     * @param level                  the level where to store the index
     * @param nodes_in_segment_index <node_id, <highway node id,  <costs from node to the highway node >>>
     */
    private void insertIndex(int level, HashMap<Long, HashMap<Long, ArrayList<double[]>>>
            nodes_in_segment_index) {
        try {
            for (long n_id : nodes_in_segment_index.keySet()) {
                String sub_folder_str = ParsedOptions.indexFolder + "/level" + level;
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
