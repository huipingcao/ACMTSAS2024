package Index;

public class NodeCoefficient {
    public double coefficient;
    public int first_hop_neighbors;
    public int second_hop_neighbors;

    public NodeCoefficient(double coefficient, int first_hop_neighbors, int second_hop_neighbors) {
        this.coefficient = coefficient;
        this.first_hop_neighbors = first_hop_neighbors;
        this.second_hop_neighbors = second_hop_neighbors;
    }

    public int getNumberOfTwoHopNeighbors() {
        return this.first_hop_neighbors + this.second_hop_neighbors;
    }


}
