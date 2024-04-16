package utilities;


import java.util.Comparator;
import java.util.PriorityQueue;

public class myClusterQueue {
    PriorityQueue<myNode> queue;

    public myClusterQueue() {
        NodeComparator nc = new NodeComparator();
        this.queue = new PriorityQueue<>(nc);
    }

    public boolean add(myNode p) {
        if (!p.inqueue) {
            p.inqueue=true;
            return this.queue.add(p);
        } else {
            return true;
        }
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
        if (o1.coefficient == o2.coefficient) {
            return 0;
        } else if (o1.coefficient > o2.coefficient) {
            return 1;
        } else {
            return -1;
        }
    }
}
