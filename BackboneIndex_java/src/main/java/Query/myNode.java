package Query;

import Neo4jTools.Neo4jDB;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class myNode {
    public long source_node_id;
    public long dest_node_id;
    public long id;
    public ArrayList<backbonePath> skyPaths; // destination node --> skyline (from source node to destination node)
    public double distance_q;
    public double[] locations;
    public boolean inqueue;
    public Neo4jDB neo4j;
    public long callAddToSkylineFunction = 0;
    public double degree = 0;


    /**
     * @param source_node_id               the source node of the skyline paths, also it's the query source node
     * @param source_skyline_paths         the skyline paths from the query point to the key of the source_skyline_paths
     * @param destination_highways_results the skyline paths from the destination highways to destination node
     * @param neo4j                        the neo4j object
     */
    public myNode(long source_node_id, long dest_node_id, Map.Entry<Long, ArrayList<backbonePath>> source_skyline_paths, HashMap<Long, ArrayList<backbonePath>> destination_highways_results, Neo4jDB neo4j) {
        this.source_node_id = source_node_id;
        this.dest_node_id = dest_node_id;
        this.id = source_skyline_paths.getKey();
        this.locations = new double[2];
        this.distance_q = 0;
        skyPaths = new ArrayList<>();
        inqueue = false;
        this.neo4j = neo4j;

        if (destination_highways_results != null) {
            setLocations(destination_highways_results);
        }
//        System.out.println(destination_highways_results.size());

        for (backbonePath bp : source_skyline_paths.getValue()) {
            path dp = new path(bp, destination_highways_results);
            backbonePath high_bp = new backbonePath(bp, dp, neo4j);
//            System.out.println("                "+high_bp.p.possible_destination.size());
            this.skyPaths.add(high_bp);
        }
    }


    public myNode(long source_node_id, long dest_node_id, long highway_source_id, HashMap<Long, ArrayList<backbonePath>> destination_highways_results, Neo4jDB neo4j) {
        this.source_node_id = source_node_id;
        this.dest_node_id = dest_node_id;
        this.id = highway_source_id;
        this.locations = new double[2];
        this.distance_q = 0;
        skyPaths = new ArrayList<>();
        inqueue = false;
        this.neo4j = neo4j;
        if (destination_highways_results != null) {
            setLocations(destination_highways_results);
        }
    }

    /**
     * calculate the distance from the query point to current point
     *
     * @param destination_highways_results
     */
    public void setLocations(HashMap<Long, ArrayList<backbonePath>> destination_highways_results) {
        distance_q = Double.POSITIVE_INFINITY;
        try (Transaction tx = neo4j.graphDB.beginTx()) {
            for (long dest_node_id : destination_highways_results.keySet()) {
                locations[0] = (double) neo4j.graphDB.getNodeById(this.id).getProperty("lat");
                locations[1] = (double) neo4j.graphDB.getNodeById(this.id).getProperty("log");
                double end_location_lat = (double) neo4j.graphDB.getNodeById(dest_node_id).getProperty("lat");
                double end_location_lng = (double) neo4j.graphDB.getNodeById(dest_node_id).getProperty("log");

                double tmp_distance_q = Math.sqrt(Math.pow(locations[0] - end_location_lat, 2) + Math.pow(locations[1] - end_location_lng, 2));
                if (tmp_distance_q < distance_q) {
                    this.distance_q = tmp_distance_q;
                }
            }
            tx.success();
        }
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean addToSkyline(backbonePath np) {
        this.callAddToSkylineFunction++;
        int i = 0;
        if (skyPaths.isEmpty()) {
            this.skyPaths.add(np);
            return true;
        } else {
            boolean can_insert_np = true;
            for (; i < skyPaths.size(); ) {
                if (checkDominated(skyPaths.get(i).costs, np.costs)) {
                    can_insert_np = false;
                    break;
                } else {
                    if (checkDominated(np.costs, skyPaths.get(i).costs)) {
                        this.skyPaths.remove(i);
                    } else {
                        i++;
                    }
                }
            }

            if (can_insert_np) {
                this.skyPaths.add(np);
                return true;
            }
        }
        return false;
    }

    private boolean checkDominated(double[] costs, double[] estimatedCosts) {
        /**** Simple Concept of dominate***/
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] > estimatedCosts[i]) {
                return false;
            }
        }
        return true;
    }


    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof myNode)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        myNode c = (myNode) o;

        // Compare the data members and return accordingly
        return c.id == this.id;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" node id = " + id + "  source_id = " + source_node_id + "  size of skyline = " + skyPaths.size());
        return sb.toString();
    }
}
