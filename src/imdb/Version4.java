package imdb;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

public class Version4 {
	public static void main(String[] args) throws Exception {
		String line;
		BufferedReader br0 = new BufferedReader(new FileReader("/home/htran/Research_Work/Data/imdb.tsv"));
		BufferedWriter wr = new BufferedWriter(new FileWriter("/home/htran/Research_Work/Data/new.imdb.tsv"));
		while ((line = br0.readLine()) != null) {
			wr.write(line + "\n");
			line = line.substring(1, line.length() - 1);
//			System.out.println(line);
			String[] parts = line.split(">\t<");
			String x = "";
			if (parts[1].equals("created")) {
				x = "createdBy";
			} else if (parts[1].equals("directed")) {
				x = "directedBy";
			} else if (parts[1].equals("hasWonPrize")) {
				x = "achievedBy";
			} else if (parts[1].equals("influences")) {
				x = "influencedBy";
			} else if (parts[1].equals("wrote")) {
				x = "writtenBy";
			}
			if (x.isEmpty()) {
				continue;
			}
			wr.write("<" + parts[2] + ">\t<" + x + ">\t<" + parts[0] + ">\n");
		}
		br0.close();
		wr.close();
	}

}
