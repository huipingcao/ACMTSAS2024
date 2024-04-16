package utilities;

import java.util.Comparator;
import java.util.PriorityQueue;


public class myNodePriorityQueue {
    PriorityQueue<myQueueNode> queue;

    public myNodePriorityQueue() {
        QueueNodeComparator nc = new QueueNodeComparator();
        this.queue = new PriorityQueue<>(nc);
    }

    public boolean add(myQueueNode p) {
        p.inqueue = true;
        return this.queue.add(p);
    }

    public int size() {
        return this.queue.size();
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    public myQueueNode pop() {
        myQueueNode n = this.queue.poll();
        n.inqueue = false;
        return n;
    }
}


class QueueNodeComparator implements Comparator<myQueueNode> {
    @Override
    public int compare(myQueueNode o1, myQueueNode o2) {
        if (o1.distance_q == o2.distance_q) {
            return 0;
        } else if (o1.distance_q > o2.distance_q) {
            return 1;
        } else {
            return -1;
        }
    }
}
