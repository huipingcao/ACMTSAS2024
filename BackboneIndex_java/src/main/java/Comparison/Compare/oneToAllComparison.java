package Comparison.Compare;

import Comparison.AverageObj;
import Comparison.BaselineMethods.oneToAllBBS;
import Comparison.BaselineMethods.path;
import Comparison.DTWComparison;
import Query.backbonePath;
import Query.oneToAllQuery.oneToAllBackbone;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class oneToAllComparison {

    DecimalFormat df = new DecimalFormat("#.###");

    public static void main(String args[]) {
        oneToAllComparison comparison = new oneToAllComparison();

        int node_number = 10000;
        String db_name = "C9_NY_10K";
        long random_src_id = getRandomNumberInRange(0, node_number - 1);

        comparison.compare(db_name, random_src_id, node_number);

    }

    private void compare(String db_name, long src_id, int querynum) {
        oneToAllBBS one_bbs = new oneToAllBBS(db_name + "_Level0");
        one_bbs.queryOnline(src_id);
        HashMap<Long, ArrayList<path>> bbs_results = one_bbs.results;

        oneToAllBackbone one_backbone = new oneToAllBackbone(db_name);
        one_backbone.query(src_id);
        HashMap<Long, ArrayList<backbonePath>> backbone_results = one_backbone.results;
        AverageObj average = new AverageObj();

        average.query_num = querynum - 1;

        for (long nid : bbs_results.keySet()) {
            ArrayList<path> bbs_paths = bbs_results.get(nid);
            ArrayList<backbonePath> backbone_paths = backbone_results.get(nid);

            ArrayList<double[]> bbs_paths_cost = getArrayListCostBBS(bbs_paths);
            ArrayList<double[]> backbone_paths_cost = getArrayListCostBackbone(backbone_paths);


            DTWComparison dtw = new DTWComparison();

            double dtw_distance = dtw.calculcateTheDTWDist(bbs_paths_cost, backbone_paths_cost);
//
            double[] avg_bbs = dtw.averageDoubleArray(bbs_paths_cost);
            double[] avg_backbone = dtw.averageDoubleArray(backbone_paths_cost);

            double max_cosine = dtw.getMaximumCosine(bbs_paths_cost, backbone_paths_cost);
            double max_cosine_average = dtw.getMaximumCosineEach(bbs_paths_cost, backbone_paths_cost);
            double max_distance_average = dtw.getMaxDistanceEach(bbs_paths_cost, backbone_paths_cost);

            System.out.println(src_id + "   >>>>>>>>>>> " + nid
                    + " " + df.format(dtw_distance)
                    + "|  |" + bbs_results.size() + "  " + backbone_results.size()
                    + "  [" + df.format(avg_bbs[0]) + "," + df.format(avg_bbs[1]) + "," + df.format(avg_bbs[2]) + "]"
                    + "  [" + df.format(avg_backbone[0]) + "," + df.format(avg_backbone[1]) + "," + df.format(avg_backbone[2]) + "]"
                    + "  [" + df.format(avg_backbone[0] / avg_bbs[0]) + "," + df.format(avg_backbone[1] / avg_bbs[1]) + "," + df.format(avg_backbone[2] / avg_bbs[2]) + "]"
                    + "| " + max_cosine + ", " + max_cosine_average + ", " + max_distance_average);

            average.dtw += dtw_distance;

            average.bbs_result_size += bbs_results.size();
            average.backbone_result_size += backbone_results.size();
            average.avg_bbs_cost[0] += avg_bbs[0];
            average.avg_bbs_cost[1] += avg_bbs[1];
            average.avg_bbs_cost[2] += avg_bbs[2];
            average.avg_backbone_cost[0] += avg_backbone[0];
            average.avg_backbone_cost[1] += avg_backbone[1];
            average.avg_backbone_cost[2] += avg_backbone[2];
            average.avg_cost[0] += (avg_backbone[0] / avg_bbs[0]);
            average.avg_cost[1] += (avg_backbone[1] / avg_bbs[1]);
            average.avg_cost[2] += (avg_backbone[2] / avg_bbs[2]);
            average.max_cosine += max_cosine;
            average.max_cosine_average += max_cosine_average;
            average.max_distance_average += max_distance_average;
        }

        System.out.print(one_bbs.p_monitor.runningtime + " " + one_backbone.p_monitor.runningtime+" ");
        System.out.println(average);

    }

    private ArrayList<double[]> getArrayListCostBackbone(ArrayList<backbonePath> backbone_paths) {
        ArrayList<double[]> cost_list = new ArrayList<>();
        for (backbonePath p : backbone_paths) {
            cost_list.add(p.costs);
        }

        return cost_list;
    }

    private ArrayList<double[]> getArrayListCostBBS(ArrayList<path> paths) {
        ArrayList<double[]> cost_list = new ArrayList<>();
        for (path p : paths) {
            cost_list.add(p.costs);
        }

        return cost_list;
    }


    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
