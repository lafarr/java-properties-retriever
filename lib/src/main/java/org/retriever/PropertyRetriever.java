package retriever;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.io.IOException;

public class PropertyRetriever {
	// Hide the implicit constructor
	private PropertyRetriever() {
	}

	private static void fillValues(Node n) {
		ArrayDeque<Node> queue = new ArrayDeque<>();
		for (Node parent : n.getChildren()) {
			queue.addLast(parent);
		}
		while (!queue.isEmpty()) {
			Node curr = queue.pollFirst();
			String pattern = "\\$\\{" + curr.getKey() + "\\}";
			for (Node cc : curr.getChildren()) {
				cc.setValue(cc.getValue().replaceAll(pattern, curr.getValue()));
				queue.addLast(cc);
			}
		}
	}

	private static void resolveVariables(Map<String, Node> graph) {
		Map<String, Boolean> hasParent = new HashMap<>();
		for (String key : graph.keySet()) {
			hasParent.put(key, false);
		}

		for (Node n : graph.values()) {
			for (Node x : n.getChildren()) {
				hasParent.put(x.getKey(), true);
			}
		}

		ArrayList<Node> disjointSets = new ArrayList<>();
		for (Entry<String, Boolean> entry : hasParent.entrySet()) {
			if (!entry.getValue()) {
				disjointSets.add(graph.get(entry.getKey()));
			}
		}

		// We need this so we can access all nodes in one BFS, even if top level nodes
		// aren't always connected
		Node x = new Node();
		for (Node n : disjointSets) {
			x.addEdge(n);
		}

		fillValues(x);
	}

	/**
	 * Recursively retrieves and resolves the values to every property defined in the property file
	 * given by the {@code InputStream}. 
	 * @param propertiesFileStream An {@code InputStream} of the given properties file
	 * @return A {@code Map<String, String>} of the (fully resolved) properties in the properties file
	 * @throws IOException If <a href="https://docs.oracle.com/javase/7/docs/api/java/util/Properties.html#load(java.io.InputStream)">java.util.Properties.load(propertiesFileStream)</a> throws an exception
	 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/util/Properties.html#load(java.io.InputStream)">java.util.Properties.load(InputStream)</a>
	 */
	public static Map<String, String> retrieveProperties(InputStream propertiesFileStream) throws IOException {
		Properties properties = new Properties();
		properties.load(propertiesFileStream);

		Map<String, Node> graph = new HashMap<>();

		for (Object prop : properties.keySet()) {
			graph.put((String) prop, new Node((String) prop, properties.getProperty((String) prop)));
		}

		Pattern pattern = Pattern.compile("\\$\\{([A-za-z0-9_.]+)\\}");
		for (String key : graph.keySet()) {
			Matcher matcher = pattern.matcher(properties.getProperty(key));
			while (matcher.find()) {
				graph.get(matcher.group(1).trim()).addEdge(graph.get(key.trim()));
			}
		}

		resolveVariables(graph);
		HashMap<String, String> rval = new HashMap<>();
		for (Entry<String, Node> entry : graph.entrySet()) {
			rval.put(entry.getKey(), entry.getValue().getValue());
		}
		return rval;
	}
}