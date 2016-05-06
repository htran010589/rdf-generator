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
import java.util.TreeSet;

public class Version2 {

	private static Set<String> visitedTypes;

	private static Map<String, String> sonOf;

	private static Set<String> triples;

	public static void visit(String subject, String predicate, String type) {
		visitedTypes.add(type);
		triples.add("<" + subject + ">\t<" + predicate + ">\t<" + type + ">");
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

		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/nospec.yagotype.imdb.tsv"));
		triples = new TreeSet<>();
		BufferedReader br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/added.imdb.tsv"));
		List<String> lines = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			line = line.substring(1, line.length() - 1);
			lines.add(line);
			String[] parts = line.split(">\t<");
			visitedTypes = new HashSet();
			visit(parts[0], parts[1], parts[2]);
//			System.out.println(parts[0] + " " + visitedTypes.size());
			for (String type : visitedTypes) {
				if (!sonOf.containsKey(type)) {
					continue;
				}
				wr.write("<" + parts[0] + ">\t<" + parts[1] + ">\t<" + type + ">\n");
			}
		}
		br.close();
		wr.close();

		/* This is for stats
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
		for (String line0 : triples) {
			line0 = line0.substring(1, line0.length() - 1);
			String[] parts = line0.split(">\t<");

			int count = 0;
			String key = parts[1] + " " + parts[2];
			if (predicateObject2Int.containsKey(key)) {
				count = predicateObject2Int.get(key);
			}
			predicateObject2Int.put(key, count + 1);
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
		type2IntSorted.putAll(predicateObject2Int);
		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/types.tmp.txt"));
		for (String str : type2IntSorted.keySet()) {
			int v = predicateObject2Int.get(str);
//			if (!predicate2Int.containsKey(str)) {
//				System.err.println(str);
//			}
			double r = v * 1.0 / predicate2Int.get(str.split(" ")[0]);
//			if (v < 1000) {
//				continue;
//			}
//			if (str.contains("actor") || str.contains("winner") || str.contains("writer") || str.contains("director"))
			wr.write(str + "\t" + r + "\n");
		}
		wr.close();
		*/
	}

}
