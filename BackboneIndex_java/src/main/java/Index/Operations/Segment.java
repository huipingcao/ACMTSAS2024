package Index.Operations;

import Neo4jTools.Line;
import Neo4jTools.Neo4jDB;
import org.neo4j.cypher.internal.compiler.v2_3.No;
import org.neo4j.graphdb.*;
import utilities.ParsedOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Segment {
    public long start_node = -1;
    public long end_node = -1;
    public ArrayList<Long> rels = new ArrayList<>();
    public double costs[] = new double[ParsedOptions.cost_dimension];
    public ArrayList<Long> normal_node_list = new ArrayList<>();

    public void addNormalRel(long edge_id) {
        rels.add(edge_id);
    }

    public void addTerminateRel(long edge_id, long terminate_node_id, boolean isStartTerminate) {
        if (isStartTerminate) {
            rels.add(0, edge_id);
            start_node = terminate_node_id;
        } else {
            rels.add(edge_id);
            end_node = terminate_node_id;
        }
    }

    /**
     * Add the rel_id to the list, if the add_before is true, add the rel at the beginning of the list
     *
     * @param rel_id
     * @param add_before
     */
    public void addNormalRel(long rel_id, boolean add_before) {
        if (add_before) {
            this.rels.add(0, rel_id);
        } else {
            addNormalRel(rel_id);
        }
    }

    public void updateCosts(Neo4jDB neo4jdb) {
//        System.out.println(this.start_node+"   "+this.end_node);
        try (Transaction tx = neo4jdb.graphDB.beginTx()) {
            for (long rel_id : rels) {
                double cost[] = new double[ParsedOptions.cost_dimension];
                Relationship r = neo4jdb.graphDB.getRelationshipById(rel_id);
//                System.out.println(r);
                cost[0] = (double) r.getProperty("EDistence");
                cost[1] = (double) r.getProperty("MetersDistance");
                cost[2] = (double) r.getProperty("RunningTime");

                this.costs[0] += cost[0];
                this.costs[1] += cost[1];
                this.costs[2] += cost[2];
            }
            tx.success();
        }
    }


    @Override
    public String toString() {
        return "{" +
                "start_node=" + start_node +
                ", end_node=" + end_node +
                ", costs=" + Arrays.toString(costs) +
                '}';
    }

    public HashMap<Long, HashMap<Long, ArrayList<double[]>>> buildIndexForNodes(Neo4jDB neo4j, boolean addToNormalNodeList) {
        HashMap<Long, HashMap<Long, ArrayList<double[]>>> node_index_in_segment = new HashMap<>();

        double[] temp_costs = new double[ParsedOptions.cost_dimension];

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            long left_node_id = start_node;

            for (int idx = 0; idx < rels.size() - 1; idx++) { //do not need to process the last rels

                Relationship r = neo4j.graphDB.getRelationshipById(rels.get(idx));
                long node_id = r.getOtherNodeId(left_node_id);
//                System.out.println(r+"   "+node_id);
                double cost[] = new double[ParsedOptions.cost_dimension];

                cost[0] = (double) r.getProperty("EDistence");
                cost[1] = (double) r.getProperty("MetersDistance");
                cost[2] = (double) r.getProperty("RunningTime");

                double[] to_Ss_cost = new double[ParsedOptions.cost_dimension];
                double[] to_Sd_cost = new double[ParsedOptions.cost_dimension];

                for (int i = 0; i < to_Ss_cost.length; i++) {
                    to_Ss_cost[i] = temp_costs[i] + cost[i];
                    temp_costs[i] = to_Ss_cost[i];
                }

                for (int i = 0; i < to_Sd_cost.length; i++) {
                    to_Sd_cost[i] = costs[i] - to_Ss_cost[i];
                }

                HashMap<Long, ArrayList<double[]>> highway_infors = new HashMap<>();
                ArrayList<double[]> costs_to_s = new ArrayList<>();
                costs_to_s.add(to_Ss_cost);
                highway_infors.put(this.start_node, costs_to_s);
                ArrayList<double[]> costs_to_d = new ArrayList<>();
                costs_to_d.add(to_Sd_cost);
                highway_infors.put(this.end_node, costs_to_d);

                node_index_in_segment.put(node_id, highway_infors);

                left_node_id = node_id;

                if (addToNormalNodeList) {
                    this.normal_node_list.add(node_id);
                }
            }
            tx.success();
        }

        return node_index_in_segment;
    }

    public long contract(Neo4jDB neo4j) {
        long new_rel_id = -1;
        try (Transaction tx = neo4j.graphDB.beginTx()) {
            if (start_node != end_node) {
                Node s_node = neo4j.graphDB.getNodeById(start_node);
                Node e_node = neo4j.graphDB.getNodeById(end_node);
                Relationship new_rel = s_node.createRelationshipTo(e_node, Line.Linked);
                new_rel.setProperty("EDistence", costs[0]);
                new_rel.setProperty("MetersDistance", costs[1]);
                new_rel.setProperty("RunningTime", costs[2]);

                new_rel_id = new_rel.getId();
            }

            for (long rel_id : rels) {
                Relationship r = neo4j.graphDB.getRelationshipById(rel_id);
                r.delete();
            }

            for (long n_id : normal_node_list) {
                try {
                    Node n = neo4j.graphDB.getNodeById(n_id);
                    if (n.getDegree(Direction.BOTH) == 0) {
                        n.delete();
                    }
                } catch (NotFoundException e) {
                }
            }
            tx.success();
        }

        return new_rel_id;
    }


    public ArrayList<Segment> split(Neo4jDB neo4j) {
//        System.out.println(normal_node_list.size() + "@@@" + rels.size());
        ArrayList<Segment> splited_segments = new ArrayList<>();

        try (Transaction tx = neo4j.graphDB.beginTx()) {

            ArrayList<Long> node_list = this.normal_node_list;

            Segment segment = new Segment();
            long node_id = neo4j.CheckNodeById(this.start_node);

            if (node_id != -1) {
                segment.start_node = node_id;
            }


            for (int idx = 0; idx < node_list.size(); idx++) {

                node_id = node_list.get(idx);
                long rel_id = rels.get(idx);

//            System.out.println(idx + " " + node_id + "~" + rel_id);


                if (rel_id != -1 & neo4j.CheckNodeById(node_id) != -1) {//normal node

                    if (segment.start_node == -1) {
                        segment.start_node = node_id;
                        continue;
                    }

                    segment.normal_node_list.add(node_id);
                    segment.rels.add(rel_id);
                } else if (rels.get(idx) == -1) {
                    if (segment.normal_node_list.isEmpty()) {
                        segment.end_node = segment.start_node;
                    } else {
                        segment.end_node = segment.normal_node_list.remove(segment.normal_node_list.size() - 1);
                    }

                    if (segment.start_node != segment.end_node) {
                        splited_segments.add(segment);
                    }

                    segment = new Segment();
                    if (neo4j.CheckNodeById(node_id) != -1) {
                        segment.start_node = node_id;
                    }
                }
            }

            if (segment.normal_node_list.isEmpty()) {
                segment.end_node = segment.start_node;
            } else {
                segment.end_node = segment.normal_node_list.remove(segment.normal_node_list.size() - 1);
            }

            if (segment.start_node != segment.end_node) {
                splited_segments.add(segment);
            }

            tx.success();
        }

        return splited_segments;
    }

    public ArrayList<Long> getAllNode() {
        ArrayList<Long> node_list = new ArrayList<>();
        node_list.add(start_node);
        node_list.addAll(normal_node_list);
        node_list.add(end_node);
        return node_list;
    }

    public void updateRels(Neo4jDB neo4j) {

        rels.clear();
        int last_idx = this.normal_node_list.size() - 1;

        //no normal node list and rels
        if (last_idx == -1) {
            rels.add(neo4j.getRelationShipByStartAndEndNodeID(this.start_node, this.end_node));
            return;
        }

        long rel = neo4j.getRelationShipByStartAndEndNodeID(this.start_node, normal_node_list.get(0));
        this.rels.add(rel);


        for (int i_idx = 0; i_idx < normal_node_list.size(); i_idx++) {
            long nid = normal_node_list.get(i_idx);
            if (i_idx != normal_node_list.size() - 1) {
                rel = neo4j.getRelationShipByStartAndEndNodeID(nid, normal_node_list.get(i_idx + 1));
                this.rels.add(rel);
            }
        }
//
        rel = neo4j.getRelationShipByStartAndEndNodeID(normal_node_list.get(last_idx), this.end_node);
        this.rels.add(rel);


    }

    public boolean hasNoExistingEdges(Neo4jDB neo4j) {
        for (long rel : rels) {
            if (rel == -1) {
                return true;
            }
        }

        try (Transaction tx = neo4j.graphDB.beginTx()) {
            try {
                neo4j.graphDB.getNodeById(start_node);
                neo4j.graphDB.getNodeById(end_node);
            } catch (NotFoundException e) {
                tx.success();
                System.out.println("This segment's start and end node is removed !!!!! think about it. ");
                System.exit(0);
            }

            for (long nid : this.normal_node_list) {
                try {
                    neo4j.graphDB.getNodeById(nid);
                } catch (NotFoundException e) {
                    tx.success();
                    return true;
                }
            }
            tx.success();
        }

        return false;
    }
}
