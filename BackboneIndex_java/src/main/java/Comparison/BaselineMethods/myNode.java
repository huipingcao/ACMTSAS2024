package Comparison.BaselineMethods;


import Neo4jTools.Neo4jDB;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;

public class myNode {
    public long id;
    public ArrayList<path> skyPaths;
    public double distance_q;
    public double[] locations;
    public boolean inqueue;
    public Neo4jDB neo4j;
    public long callAddToSkylineFunction = 0;

    public myNode(myNode node, long current_id, Neo4jDB neo4j) {
        this.id = current_id;
        this.locations = new double[2];
        skyPaths = new ArrayList<>();
        this.neo4j = neo4j;
        inqueue = false;
        setLocations(node);

    }

    public myNode(long nodeid, Neo4jDB neo4j) {
        this.id = nodeid;
        this.locations = new double[2];
        this.distance_q = 0;
        skyPaths = new ArrayList<>();
        inqueue = false;
        this.neo4j = neo4j;

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            locations[0] = (double) neo4j.graphDB.getNodeById(this.id).getProperty("lat");
            locations[1] = (double) neo4j.graphDB.getNodeById(this.id).getProperty("log");
            tx.success();
        }

        path dp = new path(this);
        skyPaths.add(dp);

    }

    public double[] getLocations() {
        return locations;
    }

    public void setLocations(myNode start_node) {
        try (Transaction tx = neo4j.graphDB.beginTx()) {
            locations[0] = (double) neo4j.graphDB.getNodeById(this.id).getProperty("lat");
            locations[1] = (double) neo4j.graphDB.getNodeById(this.id).getProperty("log");
            this.distance_q = Math.sqrt(Math.pow(locations[0] - start_node.locations[0], 2) + Math.pow(locations[1] - start_node.locations[1], 2));
            tx.success();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean addToSkyline(path np) {
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
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] * (1) > estimatedCosts[i]) {
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
}
