package utilities;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

//a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
public class PairComparator implements Comparator<Pair<Integer, Integer>> {
    String order = "";

    public PairComparator() {
        this.order = "asc";
    }

    public PairComparator(String order) {
        this.order = order;
    }

    @Override
    public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
        if (order.equals("asc")) {
            if ((o1.getKey() + o1.getValue()) - (o2.getKey() + o2.getValue()) != 0) {
                return (o1.getKey() + o1.getValue()) - (o2.getKey() + o2.getValue());
            } else {
                return o1.getKey() - o2.getKey();
            }//sort in asc order
        } else if (order.equals("desc")) {
            if ((o1.getKey() + o1.getValue()) - (o2.getKey() + o2.getValue()) != 0) {
                return (o2.getKey() + o2.getValue()) - (o1.getKey() + o1.getValue());
            } else {
                return o2.getKey() - o1.getKey();
            }//sort in desc order
        } else {
            return 0;
        }
    }
}
