package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TextFileReader {

	public static List<String> readLines(String filePath) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line;
		List<String> lines = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();

		return lines;
	}

}
