package Index.Operations;

import Comparison.BaselineMethods.myNode;
import Comparison.BaselineMethods.myNodePriorityQueue;
import Comparison.BaselineMethods.path;
import Index.Landmark.Landmark;
import Neo4jTools.Line;
import Neo4jTools.Neo4jDB;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Baseline {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final static int data_dimension = 3;

    public ArrayList<path> queryOnline(long src, long dest, Neo4jDB neo4j) {

        HashMap<Long, myNode> tmpStoreNodes = new HashMap();
        ArrayList<path> results = new ArrayList<>();

        initilizeSkylinePath(src, dest, results, neo4j);

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            myNode snode = new myNode(src, neo4j);
            myNodePriorityQueue mqueue = new myNodePriorityQueue();
            tmpStoreNodes.put(snode.id, snode);
            mqueue.add(snode);
            snode.inqueue = false;

            while (!mqueue.isEmpty()) {



                myNode v = mqueue.pop();
                v.inqueue = false;


                for (int i = 0; i < v.skyPaths.size(); i++) {
                    path p = v.skyPaths.get(i);

                    if (!p.expaned) {
                        p.expaned = true;
                        ArrayList<path> new_paths = p.expand(neo4j);
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

                            if (np.endNode == dest) {
                                addToSkyline(np, results);
                            } else if (!dominatebyresult) {
                                boolean add_succ = next_n.addToSkyline(np);

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

        neo4j.closeDB();
        return results;
    }

    private void initilizeSkylinePath(long srcNode, long destNode, ArrayList<path> results, Neo4jDB neo4j) {
        results.clear();

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            Node destination = neo4j.graphDB.getNodeById(destNode);
            Node startNode = neo4j.graphDB.getNodeById(srcNode);


            for (String property_name : Neo4jDB.propertiesName) {
                PathFinder<WeightedPath> finder = GraphAlgoFactory
                        .dijkstra(PathExpanders.forTypeAndDirection(Line.Linked, Direction.BOTH), property_name);
                WeightedPath paths = finder.findSinglePath(startNode, destination);
                if (paths != null) {
                    path np = new path(paths);
                    System.out.println(np);
                    addToSkyline(np, results);
                }
            }
            tx.success();
        }
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
}
