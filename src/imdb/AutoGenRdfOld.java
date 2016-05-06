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

public class AutoGenRdfOld {

	private static Map<String, Integer> triples;

	public static void addKey(String key) {
		int cnt = 0;
		if (triples.containsKey(key)) {
			cnt = triples.get(key);
		}
		triples.put(key, cnt + 1);
	}

	public static void main(String[] args) throws Exception {


		if (args.length == 0) {
			args = new String[3];
//			args[0] = "/home/htran/Research_Work/Data/rev.imdb.tsv";
//			args[1] = "/home/htran/Research_Work/Data/filter.type.txt";
//			args[2] = "/home/htran/Research_Work/Data/filter.type.imdb.tsv";

			args[0] = "/home/htran/Research_Work/Data/yago/football.yago.facts.tsv";
			args[1] = "/home/htran/Research_Work/Data/yago/filter.football.yago.types.tsv";
			args[2] = "/home/htran/Research_Work/Data/yago/filter.football.yago.facts.tsv";
		}

		String line;

		triples = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		List<String> lines = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			line = line.substring(1, line.length() - 1);
			lines.add(line);
		}
		br.close();

		//  Create type set for each object
		Map<String, Set<String>> object2TypeSet = new HashMap<>();
		for (String line0 : lines) {
			line = line0;
			String[] parts = line.split(">\t<");
			if (parts[1].equals("type")) {
				Set<String> typeSet = new HashSet<>();
				if (object2TypeSet.containsKey(parts[0])) {
					typeSet = object2TypeSet.get(parts[0]);
				}
				typeSet.add(parts[2]);
				object2TypeSet.put(parts[0], typeSet);
			}
		}

		//  Count frequency for each predicate-object pair
		final Map<String, Integer> predicateObject2Int = new HashMap<>();
		for (String line0 : lines) {
			String line1 = line0;
			String[] parts = line0.split(">\t<");

			int count = 0;
			String key = parts[1] + " " + parts[2];
			if (predicateObject2Int.containsKey(key)) {
				count = predicateObject2Int.get(key);
			}
			predicateObject2Int.put(key, count + 1);
		}

		for (String line0 : lines) {
			line = line0;
			String[] parts = line.split(">\t<");
			if (object2TypeSet.containsKey(parts[2])) {
				Set<String> typeSet = object2TypeSet.get(parts[2]);
				for (String type1 : typeSet) {
					String key = "<" + parts[0] + ">\t<" + parts[1] + ">\t<" + type1 + ">";
					addKey(key);
				}
			}
		}

		//  Count frequency for predicate
		Map<String, Integer> predicate2Int = new HashMap<>();

		for (String line0 : lines) {
			String[] parts = line0.split(">\t<");

			int count2 = 0;
			if (predicate2Int.containsKey(parts[1])) {
				count2 = predicate2Int.get(parts[1]);
			}
			predicate2Int.put(parts[1], count2 + 1);
		}

		predicateObject2Int.clear();
		for (String line0 : triples.keySet()) {
			String line1 = line0;
			line0 = line0.substring(1, line0.length() - 1);
			String[] parts = line0.split(">\t<");

			int count = 0;
			String key = parts[1] + " " + parts[2];
			if (predicateObject2Int.containsKey(key)) {
				count = predicateObject2Int.get(key);
			}
			predicateObject2Int.put(key, count + triples.get(line1));

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
					return 1;
				}
				if (v1 < v2) {
					return -1;
				}
				return o1.compareTo(o2);
			}

		});

		BufferedWriter wr0 = new BufferedWriter(new FileWriter(args[1]));
		Set<String> selectedPO = new HashSet<>();
		type2IntSorted.putAll(predicateObject2Int);
		for (String str : type2IntSorted.keySet()) {
//			System.err.println(str);
			int v = predicateObject2Int.get(str);
			if (str.split(" ")[0].equals("type")) {
				continue;
			}
			double r = v * 1.0 / predicate2Int.get(str.split(" ")[0]);
//			wr0.write(str + " " + v + " " + r + "\n");
//			if (v < 2) {
//				continue;
//			}
//			if (r < 0.01) {
//				continue;
//			}
//			if (r > 0.2) {
//				continue;
//			}
//			selectedPO.add(str);
			wr0.write(str + " " + v + " " + r + "\n");
		}
		wr0.close();

		/*
		BufferedWriter wr = new BufferedWriter(new FileWriter(args[2]));
		// cai nay moi them vao
		for (String line0 : lines) {
			line = line0;
			String[] parts = line.split(">\t<");
			if (predicateObject2Int.containsKey(parts[1] + " " + parts[2])) {
				if (predicateObject2Int.get(parts[1] + " " + parts[2]) > 100) {
					wr.write("<" + line + ">");
				}
			}
			if (object2TypeSet.containsKey(parts[2])) {
				String[] type0 = object2TypeSet.get(parts[2]).split(" ");
				for (String type1 : type0) {
					String key = "<" + parts[0] + ">\t<" + parts[1] + ">\t<" + type1 + ">";
					addKey(key);
//					System.out.println(key);
				}
			}
		}

		for (String line0 : triples.keySet()) {
			line = line0;
			line = line.substring(1, line.length() - 1);
			String[] parts = line.split(">\t<");
			if (!selectedPO.contains(parts[1] + " " + parts[2])) {
//				System.err.println(parts[1] + " " + parts[2]);
				continue;
			}
			wr.write(line0 + "\n");
		}
		wr.close();*/
	}

}
