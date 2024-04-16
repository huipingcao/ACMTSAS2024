package Query;

import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class path {
    public boolean expanded;
    public ArrayList<Long> rels;
    public HashMap<Long, ArrayList<backbonePath>> possible_destination;

    /**
     * initialize the path by using the backbone path from sid to highway at the highest level
     *
     * @param bp                           the backbone path from sid to highway node at the highest level
     * @param destination_highways_results the list of destination node with its skyline paths, the possible last components (destination highway to destination node) could form the final results.
     */
    public path(backbonePath bp, HashMap<Long, ArrayList<backbonePath>> destination_highways_results) {
        this.expanded = false;
        this.rels = new ArrayList<>();

        for (int i = 0; i < bp.highwayList.size() - 1; i++) {
            rels.add(null);
        }

        possible_destination = new HashMap<>();
        if (destination_highways_results != null) {
            for (Map.Entry<Long, ArrayList<backbonePath>> e : destination_highways_results.entrySet()) {
                ArrayList<backbonePath> skyline_bps = new ArrayList<>(e.getValue());
                long dest_highway_node = e.getKey();
                possible_destination.put(dest_highway_node, skyline_bps);
            }
        }
    }

    public path(path p) {
        this.rels = new ArrayList<>(p.rels);
        this.expanded = false;
        possible_destination = new HashMap<>();
        for (Map.Entry<Long, ArrayList<backbonePath>> e : p.possible_destination.entrySet()) {
            ArrayList<backbonePath> skyline_bps = new ArrayList<>(e.getValue());
            long dest_highway_node = e.getKey();
            possible_destination.put(dest_highway_node, skyline_bps);
        }
    }

    public path(path old_path, Relationship rel) {
        rels = new ArrayList<>();
        rels.addAll(old_path.rels);
        this.rels.add(rel.getId());

        this.expanded = false;

        possible_destination = new HashMap<>();
        for (Map.Entry<Long, ArrayList<backbonePath>> e : old_path.possible_destination.entrySet()) {
            ArrayList<backbonePath> skyline_bps = new ArrayList<>(e.getValue());
            long dest_highway_node = e.getKey();
            possible_destination.put(dest_highway_node, skyline_bps);
        }
    }
}
