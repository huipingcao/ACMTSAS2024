package Index;

public class UFnode {
    public long parentID;
    long nodeID;
    public int rank;
    public int size; //size of the subtree, include current node;

    public UFnode(long nodeID) {
        this.nodeID = nodeID;
        this.parentID = nodeID;
        this.rank = 0;
        this.size = 1;
    }

    @Override
    public String toString() {
        return "UFnode{" +
                "parentID=" + parentID +
                ", nodeID=" + nodeID +
                ", rank=" + rank +
                ", size=" + size +
                '}';
    }
}
