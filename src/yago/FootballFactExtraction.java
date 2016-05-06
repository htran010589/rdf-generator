package yago;

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

import utils.TextFileReader;

public class FootballFactExtraction {

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/yago/yagoSimpleTypes.tsv"));
		String line;
		Set<String> subjects = new HashSet<>();
		while ((line = br.readLine()) != null) {
			if (!line.contains("football")) {
				continue;
			}
			line = line.trim();
			String str = line.substring(1, line.indexOf('>'));
//			System.out.println(str + "***" + line);
			subjects.add(str);
		}
		br.close();

//		br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/yago/yagoSimpleTypes.tsv"));
//		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/yago/football.yago.types.tsv"));
//		while ((line = br.readLine()) != null) {
//			line = line.trim();
//			String str = line.substring(1, line.indexOf('>'));
//			if (!subjects.contains(str)) {
//				continue;
//			}
//			line = line.replace("rdf:type", "<type>");
//			wr.write(line + "\n");
//		}
//		wr.close();
//		br.close();

		br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/yago/yagoFacts.tsv"));
		Map<String, Integer> ma = new HashMap<>();
		List<String> lines = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			line = line.trim();
			line = line.substring(1, line.length() - 1);
//			System.out.println(line);
			String[] parts = line.split(">\t<");
			if (parts.length != 4) {
				continue;
			}
			if (!subjects.contains(parts[1])) {
				continue;
			}
			if (!parts[2].equals("playsFor")) {
				continue;
			}
			int cnt = 0;
			if (ma.containsKey(parts[3])) {
				cnt = ma.get(parts[3]);
			}
			ma.put(parts[3], cnt + 1);
			lines.add("<" + parts[1] + ">\t<" + parts[2] + ">\t<" + parts[3] + ">");
		}
		br.close();

		Map<String, Integer> ma1 = new TreeMap<>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int v1 = ma.get(o1);
				int v2 = ma.get(o2);
				if (v1 > v2) {
					return -1;
				}
				if (v1 < v2) {
					return 1;
				}
				return o1.compareTo(o2);
			}
			
		});
		ma1.putAll(ma);

//		BufferedWriter wr0 = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/yago/top.clubs"));
		Set<String> topClubs = new HashSet<>();
		for (String line1 : TextFileReader.readLines("/home/htran/Research_Work/Data/yago/top.clubs")) {
			if (line1.startsWith("--")) {
				continue;
			}
			topClubs.add(line1.split("\t")[0]);
		}
//		int cnt = 0;
//		for (String str : ma1.keySet()) {
//			int v = ma.get(str);
//			wr0.write(str + "\t" + v + "\n");
//			if (v < 2) {
//				continue;
//			}
//			if (v < 300) {
//				continue;
//			}
////			cnt++;
////			if (cnt > 100) {
////				break;
////			}
//			topClubs.add(str);
//		}
//		wr0.close();
//		System.err.println("DONE top club");
		System.out.println(topClubs);

		subjects.clear();
		br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/yago/yagoFacts.tsv"));
		while ((line = br.readLine()) != null) {
			line = line.trim();
			line = line.substring(1, line.length() - 1);
			String[] parts = line.split(">\t<");
			if (parts.length != 4) {
				continue;
			}
			if (!parts[2].equals("playsFor")) {
				continue;
			}
			if (!topClubs.contains(parts[3])) {
				continue;
			}
			subjects.add(parts[1]);
		}
		br.close();
		
		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/yago/football.yago.facts.tsv"));
		br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/yago/yagoFacts.tsv"));
		while ((line = br.readLine()) != null) {
			line = line.trim();
			line = line.substring(1, line.length() - 1);
//			System.out.println(line);
			String[] parts = line.split(">\t<");
			if (parts.length != 4) {
				continue;
			}
			if (!subjects.contains(parts[1])) {
				continue;
			}
			if ("actedIn directed graduatedFrom hasAcademicAdvisor influences isPoliticianOf wroteMusicFor".contains(parts[2])) {
				continue;
			}
			if ("playsFor isAffiliatedTo".contains(parts[2])) {
				if (!topClubs.contains(parts[3])) {
					continue;
				}
			}
			wr.write("<" + parts[1] + ">\t<" + parts[2] + ">\t<" + parts[3] + ">\n");
		}
		br.close();
		wr.close();
	}

}
