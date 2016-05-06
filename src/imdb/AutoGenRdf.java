package imdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AutoGenRdf {

	public static final int PO_MIN_FREQUENCY = 100;

	public static final int TOP_PREDICATE = 2;

	public static final double MIN_PREDICATE_TYPE_RATIO = 0.01;

	public static final double MAX_PREDICATE_TYPE_RATIO = 0.75;

	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			args = new String[2];
//			args[0] = "/home/htran/Research_Work/Data/rev.imdb.tsv";
//			args[1] = "/home/htran/Research_Work/Data/filter.imdb.tsv";

			args[0] = "/home/htran/Research_Work/Data/yago/football.yago.facts.tsv";
			args[1] = "/home/htran/Research_Work/Data/yago/filter.football.yago.facts.tsv";
		}

		String inputLine;
		BufferedReader rdfReader = new BufferedReader(new FileReader(args[0]));
		List<String> lines = new ArrayList<>();
		while ((inputLine = rdfReader.readLine()) != null) {
			String line = inputLine;
			line = line.substring(1, line.length() - 1);
			line = line.replaceAll(">\t<", "\t");
			lines.add(line);
		}
		rdfReader.close();

		//  Create type set for each object
		Map<String, Set<String>> object2TypeSet = new HashMap<>();
		for (String line : lines) {
			String[] parts = line.split("\t");
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
		for (String line : lines) {
			String[] parts = line.split("\t");

			int count = 0;
			String key = parts[1] + "\t" + parts[2];
			if (predicateObject2Int.containsKey(key)) {
				count = predicateObject2Int.get(key);
			}
			predicateObject2Int.put(key, count + 1);
		}

		//  Create triples: subject-predicate-type, each triple with corresponding frequency
		Map<String, Integer> sptTriples = new HashMap<>();
		for (String line : lines) {
			String[] parts = line.split("\t");
			if (object2TypeSet.containsKey(parts[2])) {
				Set<String> typeSet = object2TypeSet.get(parts[2]);
				for (String type : typeSet) {
					String key = parts[0] + "\t" + parts[1] + "\t" + type;
					int count = 0;
					if (sptTriples.containsKey(key)) {
						count = sptTriples.get(key);
					}
					sptTriples.put(key, count + 1);
				}
			}
		}

		//  Count frequency for each predicate
		Map<String, Integer> predicate2Int = new HashMap<>();
		for (String line : lines) {
			String[] parts = line.split("\t");
			int count = 0;
			if (predicate2Int.containsKey(parts[1])) {
				count = predicate2Int.get(parts[1]);
			}
			predicate2Int.put(parts[1], count + 1);
		}

		//  Count frequency for each predicate-type pair
		Map<String, Integer> predicateType2Int = new HashMap<>();
		for (String line : sptTriples.keySet()) {
			String[] parts = line.split("\t");
			int count = 0;
			String key = parts[1] + "\t" + parts[2];
			if (predicateType2Int.containsKey(key)) {
				count = predicateType2Int.get(key);
			}
			predicateType2Int.put(key, count + sptTriples.get(line));
		}
		
		Set<String> outputLines = new HashSet<>();
		BufferedWriter rdfWriter = new BufferedWriter(new FileWriter(args[1]));
		for (String line : lines) {
			String[] parts = line.split("\t");
			if (parts[1].equals("type")) {
				continue;
			}
			double ratio = predicateObject2Int.get(parts[1] + "\t" + parts[2]) * 1.0 / predicate2Int.get(parts[1]);
			if (ratio > MAX_PREDICATE_TYPE_RATIO) {
				continue;
			}
			if (predicateObject2Int.get(parts[1] + "\t" + parts[2]) > PO_MIN_FREQUENCY) {
				String outputLine = "<" + parts[0] + ">\t<" + parts[1] + ">\t<" + parts[2] + ">\n";
				if (outputLines.contains(outputLine)) {
					continue;
				}
				rdfWriter.write(outputLine);
				outputLines.add(outputLine);
			}
			if (object2TypeSet.containsKey(parts[2])) {
				Set<String> typeSet = object2TypeSet.get(parts[2]);
				List<String> typeList = new ArrayList<>(typeSet);
				Collections.sort(typeList, new Comparator<String>() {
					@Override
					public int compare(String s1, String s2) {
						int f1 = predicateType2Int.get(parts[1] + "\t" + s1);
						int f2 = predicateType2Int.get(parts[1] + "\t" + s2);
						if (f1 < f2) {
							return -1;
						}
						if (f1 > f2) {
							return 1;
						}
						return s1.compareTo(s2);
					}
				});
				int count = 0;
				for (String type : typeList) {
					String outputLine = "<" + parts[0] + ">\t<" + parts[1] + ">\t<" + type + ">\n";
					if (outputLines.contains(outputLine)) {
						continue;
					}
					count++;
					if (count > TOP_PREDICATE) {
						continue;
					}
					ratio = predicateType2Int.get(parts[1] + "\t" + type) * 1.0 / predicate2Int.get(parts[1]);
					if (predicateType2Int.get(parts[1] + "\t" + type) < PO_MIN_FREQUENCY) {
						continue;
					}
					if (ratio < MIN_PREDICATE_TYPE_RATIO || ratio > MAX_PREDICATE_TYPE_RATIO) {
						continue;
					}
					rdfWriter.write(outputLine);
					outputLines.add(outputLine);

//					rdfWriter.write(ratio + "\t" + predicateType2Int.get(parts[1] + "\t" + type) + "\n");
				}
			}
		}
		rdfWriter.close();
	}

}
