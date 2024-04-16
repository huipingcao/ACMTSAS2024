package Comparison.BaselineMethods;

import Comparison.Compare.performanceResult;
import Neo4jTools.Neo4jDB;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import utilities.ParsedOptions;
import utilities.myLogger;

import java.util.*;
import java.util.logging.Logger;

public class oneToAllBBS {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private String db_name;
    public HashMap<Long, ArrayList<path>> results = new HashMap<>();
    public performanceResult p_monitor;


    public oneToAllBBS(String dbname) {
        this.db_name = dbname;
        p_monitor = new performanceResult();

        Date date = new Date();
        long timestamp = date.getTime();
        myLogger.configTheLogger("oneToAllBBS_" + dbname + "_" + ParsedOptions.numQuery, timestamp);
    }

    public HashMap<Long, ArrayList<path>> queryOnline(long src) {

        long total_bbs_time = 0;
        long start_bbs_time = System.currentTimeMillis();
        boolean execute_result = true;

        //initialize the db object
        Neo4jDB neo4j = new Neo4jDB(db_name);
        neo4j.startDB(true);
        GraphDatabaseService graphdb = neo4j.graphDB;
        LOGGER.info("Executing the one to all skyline query from " + src + " on the graph " + graphdb);
        LOGGER.info("There are " + neo4j.getNumberofNodes() + " nodes and " + neo4j.getNumberofEdges() + " edges at " + neo4j.DB_PATH);

        HashSet<Long> nodes_list = neo4j.getNodes();

        HashMap<Long, myNode> tmpStoreNodes = new HashMap();


        long quer_running_time = System.nanoTime();


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

                long current_bbs_time = System.currentTimeMillis();
                total_bbs_time += (current_bbs_time - start_bbs_time);
                start_bbs_time = current_bbs_time;

                // 900000 = 15 mins
                //1800000 = 30 mins
                if (total_bbs_time >= 900000) {
                    execute_result = false;
                    LOGGER.info("Quit, can not finish the query within one hour. ");
                    break;
                }else{
//                    System.out.println(total_bbs_time);
                }


                myNode v = mqueue.pop();
                v.inqueue = false;


                for (int i = 0; i < v.skyPaths.size(); i++) {
                    path p = v.skyPaths.get(i);

                    if (!p.expaned) {
                        p.expaned = true;

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
            tx.success();
        }

        p_monitor.runningtime = execute_result ? (System.nanoTime() - quer_running_time) / 1000000 : 9999999;
        LOGGER.info("OneToAll bbs|" + src + ">" + "| Query time : " + p_monitor.runningtime + " " + tmpStoreNodes.size());

        for (Map.Entry<Long, myNode> e : tmpStoreNodes.entrySet()) {
            number_addtoskyline += e.getValue().callAddToSkylineFunction;
        }

        LOGGER.info("bbs|" + src + ">" + "|add to skyline running time : " + addtoskyline_rt / 1000000);
        LOGGER.info("bbs|" + src + ">" + "|expansion time by using the neo4j object : " + expansion_rt / 1000000);
        LOGGER.info("bbs|" + src + ">" + "|coverage of the nodes : " + tmpStoreNodes.size() * 1.0 / nodes_list.size() * 100 + "%");
        LOGGER.info("bbs|" + src + ">" + "|# of times to add to skyline function (each node): " + number_addtoskyline);
        neo4j.closeDB();

        results.clear();

        for (long nid : tmpStoreNodes.keySet()) {
            if (nid == src) {
                continue;
            }
            results.put(nid, tmpStoreNodes.get(nid).skyPaths);
        }

        return results;
    }

    public static void main(String args[]) {
        oneToAllBBS one = new oneToAllBBS("C9_NY_15K_Level0");
        one.queryOnline(0l);
    }
}
