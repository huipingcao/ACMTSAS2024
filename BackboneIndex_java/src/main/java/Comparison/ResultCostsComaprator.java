package Comparison;

import java.util.Comparator;

public class ResultCostsComaprator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        double[] cost1 = (double[]) o1;
        double[] cost2 = (double[]) o2;

        int n = cost1.length;
        for (int i = 0; i < n; i++) {
            if (cost1[i] > cost2[i]) {
                return 1;
            } else if (cost1[i] < cost2[i]) {
                return -1;
            }
        }
        return 0;
    }
}
