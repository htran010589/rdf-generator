package yago;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.TextFileReader;

public class FootballTypeExtraction {
	public static void main(String[] args) throws Exception {
		List<String> lines = TextFileReader.readLines("/home/htran/Research_Work/Data/yago/football.yago.facts.tsv");
		Set<String> objects = new HashSet<>();
		for (String line : lines) {
			line = line.substring(1, line.length() - 1);
			String[] parts = line.split(">\t<");
			objects.add(parts[2]);			
		}

		BufferedReader br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/yago/yagoSimpleTypes.tsv"));
		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/yago/football.yago.types.tsv"));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			String str = line.substring(1, line.indexOf('>'));
			line = line.replace("rdf:type", "<type>");
//			System.out.println(line + "**" + str);
			if (!objects.contains(str)) {
				continue;
			}
			wr.write(line + "\n");
		}
		br.close();
		wr.close();
	}
}
