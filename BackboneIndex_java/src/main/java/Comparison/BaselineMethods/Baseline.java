package Comparison.BaselineMethods;

import Index.Landmark.Landmark;
import Neo4jTools.Line;
import Neo4jTools.Neo4jDB;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;
import utilities.ParsedOptions;
import utilities.myLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Baseline implements Runnable {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final static int data_dimension = 3;

    static String p_db_name;
    static int p_number_queries;
    static ArrayList<Pair<Long, Long>> p_query_list = new ArrayList<>();
    static boolean p_init;
    static boolean p_lnd;
    static int p_lnd_n;
    public String postfix;

    String db_name;
    int num_queries;
    public ArrayList<Pair<Long, Long>> query_list = new ArrayList<>();
    boolean init;
    boolean lnd;
    int lnd_n;

    Landmark ldm_idx;

    public long current_src;
    public long current_dest;
    public String result_folder;
    public long timestamp;
    public Neo4jDB neo4j;
    public String methed_name = "bbs";


    public Baseline(String db_name) {
        this.db_name = db_name;
    }

    public Baseline(String db_name, int number_queries, ArrayList<Pair<Long, Long>> query_list, boolean init, boolean lnd, int lnd_n) {
        this.db_name = db_name;
        this.num_queries = number_queries;
        this.query_list = query_list;
        this.init = init;
        this.lnd = lnd;
        this.lnd_n = lnd_n;
    }


    /**
     * Initialize the queries, if there is no or no enough query pairs are given,
     * Generate them randomly.
     */
    public void init_queries() {
        LOGGER.info("==========================================================================");
        Neo4jDB neo4j = new Neo4jDB(db_name);
        neo4j.startDB(true);
        GraphDatabaseService graphdb = neo4j.graphDB;
        LOGGER.info(neo4j.DB_PATH + "  number of nodes:" + neo4j.getNumberofNodes() + "   number of edges : " + neo4j.getNumberofEdges());
        ArrayList<Long> node_list = new ArrayList<>(neo4j.getNodes());


        int needed_new_queries = 0;
        if (this.query_list.size() < this.num_queries) {
            needed_new_queries = num_queries - query_list.size();
        } else {
            num_queries = query_list.size();
        }


        for (int i = 0; i < needed_new_queries; i++) {
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

        if (this.lnd) {
            init_landmark();
        } else {
            ldm_idx = null;
        }

        neo4j.closeDB();
    }

    public void init_landmark() {
        String landmark_folder_str = this.db_name;
        LOGGER.info("Read the landmark index from the folder :" + landmark_folder_str);
        ldm_idx = new Landmark(landmark_folder_str);
        ldm_idx.readLandmarkIndex(this.lnd_n, null, false);
    }


    public <T> T getRandomNodes(ArrayList<T> nodelist) {
        Random r = new Random();
        int idx = r.nextInt(nodelist.size());
        return nodelist.get(idx);
    }

    public ArrayList<path> queryOnline(long src, long dest) {

        long total_bbs_time = 0;
        long start_bbs_time = System.currentTimeMillis();
//        boolean execute_result = true;

        //initialize the db object
        neo4j = new Neo4jDB(db_name);
        neo4j.startDB(true);
        GraphDatabaseService graphdb = neo4j.graphDB;
        LOGGER.info("Executing the query from " + src + " to " + dest + " on the graph " + graphdb);

        HashSet<Long> nodes_list = neo4j.getNodes();

        HashMap<Long, myNode> tmpStoreNodes = new HashMap();
        ArrayList<path> results = new ArrayList<>();


        long quer_running_time = System.nanoTime();

        if (init) {
//            System.out.println((neo4j.graphDB == null) + "," + Thread.currentThread().isInterrupted()+","+Thread.currentThread().getName());
            initilizeSkylinePath(src, dest, results, neo4j);
            LOGGER.info("size of the initialized skyline results " + results.size());
        }


        long addtoskyline_rt = 0;
        long number_addtoskyline = 0;
        long upperbound_find_rt = 0;
        long check_dominate_result_rt = 0;
        long expansion_rt = 0;

        try (Transaction tx = graphdb.beginTx()) {
            myNode snode = new myNode(src, neo4j);
            myNodePriorityQueue mqueue = new myNodePriorityQueue();
            tmpStoreNodes.put(snode.id, snode);
            mqueue.add(snode);
            snode.inqueue = false;

            while (!mqueue.isEmpty()) {

//                long current_bbs_time = System.currentTimeMillis();
//                total_bbs_time += (current_bbs_time - start_bbs_time);
//                start_bbs_time = current_bbs_time;
//
//                if (total_bbs_time >= ParsedOptions.timeout) {
//                    execute_result = false;
//                    LOGGER.info("Quit, can not finish the query within one hour. ");
//                    break;
//                }


                myNode v = mqueue.pop();
                v.inqueue = false;


                double[] p_l_costs = new double[data_dimension];

                if (ldm_idx != null && ldm_idx.landmark_index.size() != 0) {
                    long upperbound_find_rt_start = System.nanoTime();
                    p_l_costs = getLowerBound(v.id, dest);
                    upperbound_find_rt += System.nanoTime() - upperbound_find_rt_start;
                }


                for (int i = 0; i < v.skyPaths.size(); i++) {
                    path p = v.skyPaths.get(i);

                    if (!p.expaned) {
                        p.expaned = true;

                        boolean isDominatedByResult = false;

                        if (ldm_idx != null && ldm_idx.landmark_index.size() != 0) {

                            double[] estimated_costs = new double[data_dimension];

                            long upperbound_find_rt_start = System.nanoTime();
                            for (int k = 0; k < estimated_costs.length; k++) {
                                estimated_costs[k] = p_l_costs[k] + p.costs[k];
                            }
                            upperbound_find_rt += System.nanoTime() - upperbound_find_rt_start;

                            long dominate_rt_start = System.nanoTime();
                            if (dominatedByResult(estimated_costs, results)) {
                                isDominatedByResult = true;
                            }
                            check_dominate_result_rt += System.nanoTime() - dominate_rt_start;
                        }

                        if (isDominatedByResult) {
                            continue;
                        }

                        long st_expansion_rt = System.nanoTime();
                        ArrayList<path> new_paths = p.expand(neo4j);
                        expansion_rt += (System.nanoTime() - st_expansion_rt);


                        for (path np : new_paths) {
                            myNode next_n;
                            if (tmpStoreNodes.containsKey(np.endNode)) {
                                next_n = tmpStoreNodes.get(np.endNode);
                            } else {
                                next_n = new myNode(snode, np.endNode, neo4j);
                                tmpStoreNodes.put(next_n.id, next_n);
                            }

                            if (np.hasCycle()) {
                                continue;
                            }

                            boolean dominatebyresult = false;
//                            long dominate_rt_start = System.nanoTime();
//                            boolean dominatebyresult = dominatedByResult(np);
//                            check_dominate_result_rt += System.nanoTime() - dominate_rt_start;

                            if (np.endNode == dest) {
                                long addtoskyline_start = System.nanoTime();
                                addToSkyline(np, results);
                                addtoskyline_rt += System.nanoTime() - addtoskyline_start;
                            } else if (!dominatebyresult) {
                                long addtoskyline_start = System.nanoTime();
                                boolean add_succ = next_n.addToSkyline(np);
                                addtoskyline_rt += System.nanoTime() - addtoskyline_start;

                                if (add_succ && !next_n.inqueue) {
                                    mqueue.add(next_n);
                                    next_n.inqueue = true;
                                }
                            }
                        }
                    }
                }
            }
            tx.success();
        }

        LOGGER.info("bbs|" + src + ">" + dest + "|Query time : " + (System.nanoTime() - quer_running_time) / 1000000 );

        for (Map.Entry<Long, myNode> e : tmpStoreNodes.entrySet()) {
            number_addtoskyline += e.getValue().callAddToSkylineFunction;
        }

        LOGGER.info("bbs|" + src + ">" + dest + "|add to skyline running time : " + addtoskyline_rt / 1000000);
        LOGGER.info("bbs|" + src + ">" + dest + "|check domination by result time : " + check_dominate_result_rt / 1000000);
        LOGGER.info("bbs|" + src + ">" + dest + "|upper-bound calculation time  : " + upperbound_find_rt / 1000000);
        LOGGER.info("bbs|" + src + ">" + dest + "|expansion time by using the neo4j object : " + expansion_rt / 1000000);
        LOGGER.info("bbs|" + src + ">" + dest + "|coverage of the nodes : " + tmpStoreNodes.size() * 1.0 / nodes_list.size());
        LOGGER.info("bbs|" + src + ">" + dest + "|# of times to add to skyline function (each node): " + number_addtoskyline);

        neo4j.closeDB();
        return results;
    }

    private void initilizeSkylinePath(long srcNode, long destNode, ArrayList<path> results, Neo4jDB neo4j) {

//        if (neo4j.graphDB == null) {
//            LOGGER.info("initilizeSkylinePath, neo4j object is null");
//            return;
//        }

//        System.out.println("funciton "+(neo4j.graphDB == null) + "," + Thread.currentThread().isInterrupted()+","+Thread.currentThread().getName());

        int i = 0;
        results.clear();
//        this.iniLowerBound = new double[3];

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            Node destination = neo4j.graphDB.getNodeById(destNode);
            Node startNode = neo4j.graphDB.getNodeById(srcNode);


            for (String property_name : Neo4jDB.propertiesName) {
                PathFinder<WeightedPath> finder = GraphAlgoFactory
                        .dijkstra(PathExpanders.forTypeAndDirection(Line.Linked, Direction.BOTH), property_name);
                WeightedPath paths = finder.findSinglePath(startNode, destination);
                if (paths != null) {
                    path np = new path(paths);
//                    this.iniLowerBound[i++] = paths.weight();
                    addToSkyline(np, results);
                }
            }
            tx.success();
        }

        for (path p : results) {
            LOGGER.info("[init path] " + p.toShortDisplay());
        }
        LOGGER.info("===================================================================================");
    }

    private boolean addToSkyline(path np, ArrayList<path> results) {
        int i = 0;
        if (results.isEmpty()) {
            results.add(np);
            return true;
        } else {
            boolean can_insert_np = true;
            for (; i < results.size(); ) {
                if (checkDominated(results.get(i).costs, np.costs)) {
                    can_insert_np = false;
                    break;
                } else {
                    if (checkDominated(np.costs, results.get(i).costs)) {
                        results.remove(i);
                    } else {
                        i++;
                    }
                }
            }

            if (can_insert_np) {
                results.add(np);
                return true;
            }
        }
        return false;
    }

    private double[] getLowerBound(long src, long dest) {
        double[] estimated_costs = new double[3];

        for (int i = 0; i < estimated_costs.length; i++) {
            estimated_costs[i] = Double.NEGATIVE_INFINITY;
        }

        for (long landmark : ldm_idx.landmark_index.keySet()) {
            double[] src_cost = ldm_idx.landmark_index.get(landmark).get(src);
            double[] dest_cost = ldm_idx.landmark_index.get(landmark).get(dest);
            for (int i = 0; i < estimated_costs.length; i++) {
                double value = Math.abs(src_cost[i] - dest_cost[i]);
                if (value > estimated_costs[i]) {
                    estimated_costs[i] = value;
                }
            }
        }
        return estimated_costs;
    }


    private boolean dominatedByResult(double estimated_costs[], ArrayList<path> results) {
        long rt_check_dominatedByresult = System.nanoTime();
        for (path rp : results) {
            if (checkDominated(rp.costs, estimated_costs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * if all the costs of the target path is less than the estimated costs of the wanted path, means target path dominate the wanted path
     *
     * @param costs          the target path
     * @param estimatedCosts the wanted path
     * @return if the target path dominates the wanted path, return true. other wise return false.
     */
    private boolean checkDominated(double[] costs, double[] estimatedCosts) {
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] * (1) > estimatedCosts[i]) {
                return false;
            }
        }
        return true;
    }

    public void savePathsInformation(ArrayList<path> results, String path_output) {
        try {
            File result_file = new File(path_output);
            if (!result_file.getParentFile().exists()) {
                result_file.getParentFile().mkdirs();
            }

            FileWriter myWriter = new FileWriter(path_output, true);
            for (path p : results) {
                myWriter.write(p + " \n");
            }
            myWriter.close();
            LOGGER.info("Successfully wrote to the file. " + path_output);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        init_landmark();
        ArrayList<path> results = queryOnline(this.current_src, this.current_dest);
        LOGGER.info("The size of the finnal result is " + results.size());
        String paths_output_file = result_folder + "/"+this.methed_name+"_" + current_src + "_" + current_dest + "_" + postfix + ".log";
        savePathsInformation(results, paths_output_file);
        LOGGER.info("-----------------------------------------------------------------------------------------");
    }
}
