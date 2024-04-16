package Query;

import java.util.Comparator;
import java.util.PriorityQueue;

public class myNodePriorityQueue {
    PriorityQueue<myNode> queue;

    public myNodePriorityQueue() {
        NodeComparator nc = new NodeComparator();
        this.queue = new PriorityQueue<>(nc);
    }

    public boolean add(myNode p) {
        p.inqueue = true;
        return this.queue.add(p);
    }

    public int size() {
        return this.queue.size();
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    public myNode pop() {
        myNode n = this.queue.poll();
        n.inqueue = false;
        return n;
    }
}


class NodeComparator implements Comparator<myNode> {
    @Override
    public int compare(myNode o1, myNode o2) {
        if (o1.distance_q == o2.distance_q) {
            return 0;
        } else if (o1.distance_q > o2.distance_q) {
            return 1;
        } else {
            return -1;
        }
    }
}
