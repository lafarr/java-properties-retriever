package retriever;

import java.util.HashSet;
import java.util.Set;

class Node {
    private Set<Node> children;
    private String key, value;

    protected Node(String key, String value) {
        this.key = key;
        this.value = value;
        this.children = new HashSet<>();
    }

    // This is to be used for a root node, so we can traverse the entire graph
    // in one go, even if the top level nodes are not connected to each other
    protected Node() {
        this.children = new HashSet<>();
    }

    protected Set<Node> getChildren() {
        return children;
    }

    protected void addEdge(Node to) {
        this.children.add(to);
    }

    protected String getKey() {
        return key;
    }

    protected String getValue() {
        return value;
    }

    protected void setValue(String value) {
        this.value = value;
    }
}