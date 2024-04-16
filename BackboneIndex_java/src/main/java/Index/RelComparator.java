package Index;

import org.neo4j.graphdb.Relationship;

import java.util.Comparator;

public class RelComparator implements Comparator<Relationship> {
    String property_type = "";

    public RelComparator(String property_type) {
        this.property_type = property_type;
    }

    @Override
    public int compare(Relationship o1, Relationship o2) {
        double value1 = (double) o1.getProperty(property_type);
        double value2 = (double) o2.getProperty(property_type);
        return (int) (value1 - value2);
    }
}
