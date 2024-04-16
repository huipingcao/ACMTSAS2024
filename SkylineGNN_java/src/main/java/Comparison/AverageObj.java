package Comparison;

import utilities.ParsedOptions;

import java.text.DecimalFormat;

public class AverageObj {
    public double backbone_dtw;
    public double gnn_dtw;

    public int bbs_result_size;
    public int backbone_result_size;
    public int gnn_result_size;

    public double[] avg_bbs_cost = new double[ParsedOptions.cost_dimension];
    public double[] avg_gnn_cost = new double[ParsedOptions.cost_dimension];
    public double[] avg_backbone_cost = new double[ParsedOptions.cost_dimension];

    public double[] total_bbs_cost = new double[ParsedOptions.cost_dimension];
    public double[] total_gnn_cost = new double[ParsedOptions.cost_dimension];
    public double[] total_backbone_cost = new double[ParsedOptions.cost_dimension];

    public double[] avg_backbone_ratio = new double[ParsedOptions.cost_dimension];
    public double[] avg_gnn_ratio = new double[ParsedOptions.cost_dimension];


    public double backbone_max_cosine;
    public double backbone_max_cosine_average;
    public double backbone_max_distance_average;
    public double gnn_max_cosine;
    public double gnn_max_cosine_average;
    public double gnn_max_distance_average;


    public double avg_bbs_result_size;
    public double avg_backbone_result_size;
    public double avg_gnn_result_size;

    public double backbone_avg_dtw;
    public double backbone_avg_max_cosine;
    public double backbone_avg_max_cosine_average;
    public double backbone_avg_distance_each;

    public double gnn_avg_dtw;
    public double gnn_avg_max_cosine;
    public double gnn_avg_max_cosine_average;
    public double gnn_avg_distance_each;

    public int query_num;

    public void calculation() {
        backbone_avg_dtw = backbone_dtw / query_num;
        gnn_avg_dtw = gnn_dtw / query_num;

        avg_bbs_result_size = bbs_result_size * 1.0 / query_num;
        avg_backbone_result_size = backbone_result_size * 1.0 / query_num;
        avg_gnn_result_size = gnn_result_size * 1.0 / query_num;

        for (int i = 0; i < ParsedOptions.cost_dimension; i++) {
            avg_bbs_cost[i] = total_bbs_cost[i] / bbs_result_size;
            avg_backbone_cost[i] = total_backbone_cost[i] / backbone_result_size;
            avg_gnn_cost[i] = total_gnn_cost[i] / gnn_result_size;

            avg_backbone_ratio[i] = avg_backbone_cost[i] / avg_bbs_cost[i];
            avg_gnn_ratio[i] = avg_gnn_cost[i] / avg_bbs_cost[i];
        }

        backbone_avg_max_cosine = backbone_max_cosine / query_num;
        backbone_avg_max_cosine_average = backbone_max_cosine_average / query_num;
        backbone_avg_distance_each = backbone_max_distance_average / query_num;

        gnn_avg_max_cosine = gnn_max_cosine / query_num;
        gnn_avg_max_cosine_average = gnn_max_cosine_average / query_num;
        gnn_avg_distance_each = gnn_max_distance_average / query_num;


    }

    @Override
    public String toString() {
        calculation();
        DecimalFormat df = new DecimalFormat("#.####");
        return "query_num:" + query_num + "\n"
                + "(backbone_avg_dtw|gnn_avg_dtw) " + df.format(this.backbone_avg_dtw) + "\t" + df.format(this.gnn_avg_dtw) + '\n'
                + "(avg_bbs_result_size|avg_backbone_result_size|avg_gnn_result_size) " + df.format(this.avg_bbs_result_size) + "\t" + df.format(this.avg_backbone_result_size) + "\t" + df.format(this.avg_gnn_result_size) + '\n'
                + "(avg_bbs_cost) "+ df.format(this.avg_bbs_cost[0]) + "\t" + df.format(this.avg_bbs_cost[1]) + "\t" + df.format(this.avg_bbs_cost[2]) + '\n'
                + "(avg_backbone_cost) " + df.format(this.avg_backbone_cost[0]) + "\t" + df.format(this.avg_backbone_cost[1]) + "\t" + df.format(this.avg_backbone_cost[2]) + '\n'
                + "(avg_gnn_cost) " + df.format(this.avg_gnn_cost[0]) + "\t" + df.format(this.avg_gnn_cost[1]) + "\t" + df.format(this.avg_gnn_cost[2]) + '\n'
                + "(avg_backbone_ratio) " + df.format(this.avg_backbone_ratio[0]) + "\t" + df.format(this.avg_backbone_ratio[1]) + "\t" + df.format(this.avg_backbone_ratio[2]) + '\n'
                + "(avg_gnn_ratio) " + df.format(this.avg_gnn_ratio[0]) + "\t" + df.format(this.avg_gnn_ratio[1]) + "\t" + df.format(this.avg_gnn_ratio[2]) + '\n'
                + "(backbone_avg_max_cosine|gnn_avg_max_cosine) " + df.format(this.backbone_avg_max_cosine) + "\t" + df.format(this.gnn_avg_max_cosine) + '\n'
                + "(backbone_avg_max_cosine_average|gnn_avg_max_cosine_average) " + df.format(this.backbone_avg_max_cosine_average) + "\t" + df.format(this.gnn_avg_max_cosine_average) + '\n'
                + "(backbone_avg_distance_each|gnn_avg_distance_each) " + df.format(this.backbone_avg_distance_each) + "\t" + df.format(this.gnn_avg_distance_each);
    }
}

