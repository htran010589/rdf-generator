package imdb;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenRdfFromOnlyImdbType {

	public static final String WITH_TYPE_PREDICATES = "actedIn created createdBy directed directedBy hasChild hasPredecessor hasSuccessor influences influencedBy achievedBy isMarriedTo produced wrote writtenBy";

	public static void main(String[] args) throws Exception {
		String line;
		Set<String> ss = new HashSet<>();
		BufferedReader br0 = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/all.type.txt"));
		while ((line = br0.readLine()) != null) {
			if (line.equals("-----")) {
				break;
			}
			if (line.startsWith("--")) {
				continue;
			}
			if (line.isEmpty()) {
				continue;
			}
			line = line.split("\t")[0];
//			System.out.println("***" + line);
			ss.add(line);
		}
		br0.close();

		BufferedReader br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/rev.imdb.tsv"));
		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/new.all.type.nospec.imdb.tsv"));
		List<String> lines = new ArrayList<>();
		Map<String, String> object2Type = new HashMap<>();
		Map<String, Integer> type2Int = new HashMap<>();
		while ((line = br.readLine()) != null) {
			line = line.substring(1, line.length() - 1);
			lines.add(line);
			String[] parts = line.split(">\t<");
			if (parts[1].equals("type")) {
//				if (!ss.contains(parts[2])) {
//					System.err.println(line);
//					continue;
//				}
				String types = "";
				if (object2Type.containsKey(parts[0])) {
					types = object2Type.get(parts[0]);
				}
				if (types.contains(parts[2])) {
					continue;
				}
				if (!types.isEmpty()) {
					types = types + "\t";
				}
				types = types + parts[2];
				object2Type.put(parts[0], types);
			}
		}
		br.close();

		for (String line1 : lines) {
			String[] parts = line1.split(">\t<");
//			if (parts[1].contains("Language")) {
//				continue;
//			}
//			if (parts[1].equals("type")) {
//				if (!ss.contains(parts[2])) {
//					continue;
//				}
//			}
			if (parts[1].equals("type")) {
				continue;
			}
			if (!WITH_TYPE_PREDICATES.contains(parts[1])) {
				wr.write("<" + line1 + ">\n");
				continue;
			}
			String types = object2Type.get(parts[2]);
			if (types == null) {
				continue;
			}
			if (types.isEmpty()) {
				continue;
			}
			String[] x = types.split("\t");
			for (String y : x) {
				wr.write("<" + parts[0] + ">\t<" + parts[1] + ">\t<" + y + ">\n");
			}
		}
		wr.close();

	}

}
