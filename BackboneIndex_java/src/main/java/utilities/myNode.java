package utilities;

public class myNode {
    public long id;
    //    Node node;
    double coefficient;
    boolean visited = false;
    boolean inqueue = false;

    public myNode(long node_id, double coefficient) {
        this.id = node_id;
        this.coefficient = coefficient;
    }

    @Override
    public String toString() {
        return "myNode{" +
                "node_id=" + id +
                ", coefficient=" + coefficient +
                ", visited=" + visited +
                ", inqueue=" + inqueue +
                '}';
    }
}
