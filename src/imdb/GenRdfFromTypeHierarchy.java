package imdb;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GenRdfFromTypeHierarchy {

	private static Set<String> visitedTypes;

	private static Map<String, String> sonOf;

	private static Map<String, Integer> triples;

	public static void visit(String subject, String predicate, String type) {
		visitedTypes.add(type);
		if (!sonOf.containsKey(type)) {
			return;
		}
		String[] fatherTypes = sonOf.get(type).split(" ");
		for (String fatherType : fatherTypes) {
			if (visitedTypes.contains(fatherType)) {
				continue;
			}
			visit(subject, predicate, fatherType);
		}
	}

	public static void main(String[] args) throws Exception {
		sonOf = new HashMap<>();
		String line;
		BufferedReader br0 = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/yagoTaxonomy.tsv"));
		while ((line = br0.readLine()) != null) {
			line = line.trim();
			if (!line.startsWith("<") || !line.endsWith(">")) {
				continue;
			}
			line = line.substring(1, line.length() - 1);
			line = line.replaceAll("\trdfs:subClassOf\t", "\t");
			String[] parts = line.split(">\t<");
			String x = null;
			String y = null;
			try {
				x = parts[1];
				y = parts[2];
			} catch (Exception ex) {
				continue;
			}
//			x = x.replaceAll("wikicat_", "wikicategory_");
			if (sonOf.containsKey(x)) {
				y = sonOf.get(x) + " " + y;
			}
			sonOf.put(x, y);
//			System.out.println(x + " " + y);
		}
		br0.close();

		triples = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/rev.imdb.tsv"));
		List<String> lines = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			line = line.substring(1, line.length() - 1);
			lines.add(line);
		}
		br.close();

		Map<String, String> types = new HashMap<>();
		for (String line0 : lines) {
			line = line0;
			String[] parts = line.split(">\t<");
			if (parts[1].equals("type")) {
				String ret = parts[2];
				if (types.containsKey(parts[0])) {
					ret = types.get(parts[0]) + " " + ret;
				}
				types.put(parts[0], ret);
			}
		}

		for (String line0 : lines) {
			line = line0;
			String[] parts = line.split(">\t<");
			visitedTypes = new HashSet();
			if (types.containsKey(parts[2])) {
				String[] type0 = types.get(parts[2]).split(" ");
				for (String type1 : type0) {
					visit(parts[0], parts[1], type1);
				}
			} else {
				visit(parts[0], parts[1], parts[2]);				
			}
//			System.out.println(parts[0] + " " + visitedTypes.size());
			for (String type : visitedTypes) {
				String key = "<" + parts[0] + ">\t<" + parts[1] + ">\t<" + type + ">";
				int cnt = 0;
				if (triples.containsKey(key)) {
					cnt = triples.get(key);
				}
				triples.put(key, cnt + 1);
			}
		}
		br.close();
//		System.out.println(triples.size());

		Map<String, Integer> predicate2Int = new HashMap<>();
		br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/rev.imdb.tsv"));
		while ((line = br.readLine()) != null) {
			line = line.substring(1, line.length() - 1);
			String[] parts = line.split(">\t<");
			int count2 = 0;
			if (predicate2Int.containsKey(parts[1])) {
				count2 = predicate2Int.get(parts[1]);
			}
			predicate2Int.put(parts[1], count2 + 1);
		}
		br.close();

		Map<String, Integer> predicateObject2Int = new HashMap<>();
		for (String line0 : triples.keySet()) {
			String line1 = line0;
			line0 = line0.substring(1, line0.length() - 1);
			String[] parts = line0.split(">\t<");

			int count = 0;
			String key = parts[1] + " " + parts[2];
			if (predicateObject2Int.containsKey(key)) {
				count = predicateObject2Int.get(key);
			}
			try {
			predicateObject2Int.put(key, count + triples.get(line1));
			} catch (Exception ex) {
				System.err.println(key);
				System.err.println(line0);
			}
		}

		Map<String, Integer> type2IntSorted = new TreeMap<>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				String s1 = o1.split(" ")[0];
				String s2 = o2.split(" ")[0];
				if (s1.compareTo(s2) < 0) {
					return -1;
				}
				if (s1.compareTo(s2) > 0) {
					return 1;
				}
				int v1 = predicateObject2Int.get(o1);
				int v2 = predicateObject2Int.get(o2);
				if (v1 > v2) {
					return -1;
				}
				if (v1 < v2) {
					return 1;
				}
				return o1.compareTo(o2);
			}
			
		});
		BufferedWriter wr0 = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/filter.type.txt"));
		Set<String> selectedPO = new HashSet<>();
		type2IntSorted.putAll(predicateObject2Int);
		for (String str : type2IntSorted.keySet()) {
			int v = predicateObject2Int.get(str);
			double r = v * 1.0 / predicate2Int.get(str.split(" ")[0]);
			if (v < 2) {
				continue;
			}
			if (r < 1e-3) {
				continue;
			}
			if (r > 0.8) {
				continue;
			}
			selectedPO.add(str);
			wr0.write(str + " " + v + " " + r + "\n");
		}
		wr0.close();

		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/filter.type.imdb.tsv"));
		for (String line0 : triples.keySet()) {
			line = line0;
			line = line.substring(1, line.length() - 1);
			String[] parts = line.split(">\t<");
			if (!selectedPO.contains(parts[1] + " " + parts[2])) {
				continue;
			}
			wr.write(line0 + "\n");
		}
		wr.close();
	}

}
