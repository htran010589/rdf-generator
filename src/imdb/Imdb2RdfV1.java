package imdb;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Set;
import java.util.TreeSet;
	
public class Imdb2RdfV1 {

	public static void main(String[] args) throws Exception {
		Set<String> s = new TreeSet<>();
		BufferedReader br = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/shady_imdb_table_export.sql"));
//		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/imdb.rdf"));
		String line;
		while ((line = br.readLine()) != null) {
			if (!line.startsWith("Insert into \"imdb_deanna\" (ARG1,RELATION,ARG2) values (")) {
				continue;
			}
			line = line.substring("Insert into \"imdb_deanna\" (ARG1,RELATION,ARG2) values (".length());
			if (!line.endsWith(");")) {
				continue;
			}
			line = line.substring(0, line.length() - 2);
			line = line.replace("','", ">\t<");
			line = line.replaceFirst("'", "<");
			line = line.substring(0, line.length() - 1) + ">";
			String[] parts = line.split(">\t<");
			s.add(parts[1]);
//			wr.write(line + "\n");
		}
		br.close();
//		wr.close();
		for (String ss : s) {
			System.out.println(ss);
		}
	}

}
