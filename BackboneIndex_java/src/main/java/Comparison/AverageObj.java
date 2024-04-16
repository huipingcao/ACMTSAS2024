package Comparison;

import utilities.ParsedOptions;

import java.text.DecimalFormat;

public class AverageObj {
    public String mode = "gnn_backbone";

    public double dtw;
    public int bbs_result_size;
    public int gnn_result_size;
    public int backbone_result_size;
    public long bbs_running_time;
    public long gnn_running_time;
    public long backbone_running_time;
    public double[] avg_bbs_cost = new double[ParsedOptions.cost_dimension];
    public double[] avg_gnn_cost = new double[ParsedOptions.cost_dimension];
    public double[] avg_backbone_cost = new double[ParsedOptions.cost_dimension];
    public double[] avg_cost = new double[ParsedOptions.cost_dimension];
    public double max_cosine;
    public double max_cosine_average;
    public double max_distance_average;
    public double highway_nodes_coverage;


    public double avg_dtw;
    public double avg_bbs_result_size;
    public double avg_gnn_result_size;
    public double avg_backbone_result_size;
    public double avg_bbs_running_time;
    public double avg_gnn_running_time;
    public double avg_backbone_running_time;
    public double avg_max_cosine;
    public double avg_max_cosine_average;
    public double avg_distance_each;
    public double avg_highway_nodes_coverage;

    public int query_num;
    public int finised_query;

    public void calculation() {
        avg_dtw = dtw / finised_query;
        avg_bbs_result_size = bbs_result_size * 1.0 / finised_query;
        avg_gnn_result_size = gnn_result_size * 1.0 / finised_query;
        avg_backbone_result_size = backbone_result_size * 1.0 / finised_query;
        avg_bbs_running_time = bbs_running_time * 1.0 / finised_query;
        avg_gnn_running_time = gnn_running_time * 1.0 / finised_query;
        avg_backbone_running_time = backbone_running_time * 1.0 / finised_query;
        if (mode == "bbs_backbone") {
            for (int i = 0; i < ParsedOptions.cost_dimension; i++) {
                avg_bbs_cost[i] = avg_bbs_cost[i] / finised_query;
                avg_backbone_cost[i] = avg_backbone_cost[i] / finised_query;
                avg_cost[i] = avg_backbone_cost[i] / avg_bbs_cost[i];
            }
        }
        if (mode == "gnn_backbone") {
            for (int i = 0; i < ParsedOptions.cost_dimension; i++) {
                avg_gnn_cost[i] = avg_gnn_cost[i] / finised_query;
                avg_backbone_cost[i] = avg_backbone_cost[i] / finised_query;
                avg_cost[i] = avg_backbone_cost[i] / avg_gnn_cost[i];
            }
        }
        avg_max_cosine = max_cosine / finised_query;
        avg_max_cosine_average = max_cosine_average / finised_query;
        avg_distance_each = max_distance_average / finised_query;
        avg_highway_nodes_coverage = highway_nodes_coverage / finised_query;


    }

    @Override
    public String toString() {
        calculation();
        DecimalFormat df = new DecimalFormat("#.###########");

        if (mode == "bbs_backbone") {
            return finised_query + "/" + query_num + " " + df.format(this.avg_dtw) + " " + df.format(this.avg_bbs_result_size) + " " + df.format(this.avg_backbone_result_size) + " " + df.format(this.avg_bbs_running_time)
                    + " " + df.format(this.avg_backbone_running_time) + " " +
                    df.format(this.avg_bbs_cost[0]) + " " + df.format(this.avg_bbs_cost[1]) + " " + df.format(this.avg_bbs_cost[2]) + " " +
                    df.format(this.avg_backbone_cost[0]) + " " + df.format(this.avg_backbone_cost[1]) + " " + df.format(this.avg_backbone_cost[2]) + " " +
                    df.format(this.avg_cost[0]) + " " + df.format(this.avg_cost[1]) + " " + df.format(this.avg_cost[2]) + " " +
                    df.format(this.avg_max_cosine) + " " + df.format(this.avg_max_cosine_average) + " " + df.format(this.avg_distance_each) + " " +
                    df.format(this.avg_highway_nodes_coverage);
        }
        else{
            return finised_query + "/" + query_num + " " + df.format(this.avg_dtw) + " " + df.format(this.avg_gnn_result_size) + " " + df.format(this.avg_backbone_result_size) + " " + df.format(this.avg_gnn_running_time)
                    + " " + df.format(this.avg_backbone_running_time) + " " +
                    df.format(this.avg_gnn_cost[0]) + " " + df.format(this.avg_gnn_cost[1]) + " " + df.format(this.avg_gnn_cost[2]) + " " +
                    df.format(this.avg_backbone_cost[0]) + " " + df.format(this.avg_backbone_cost[1]) + " " + df.format(this.avg_backbone_cost[2]) + " " +
                    df.format(this.avg_cost[0]) + " " + df.format(this.avg_cost[1]) + " " + df.format(this.avg_cost[2]) + " " +
                    df.format(this.avg_max_cosine) + " " + df.format(this.avg_max_cosine_average) + " " + df.format(this.avg_distance_each) + " " +
                    df.format(this.avg_highway_nodes_coverage);
        }
    }
}
