package org.network.server;

/**
 *
 * @author Mateusz Kaproń
 */

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.network.model.Sentence;

public class FileOperations {

	private List<Sentence> input = new ArrayList<>();
	private List<String> attributeList = new ArrayList<>();

	public List<Sentence> readDataFromFile(String file) {
		Path path = Paths.get(file);
		try {
			Scanner in = new Scanner(path);
			String line;
			
			String[] parts;
			String[] attributeLine = in.nextLine().split("\\s+");
			for(int i = 0; i<attributeLine.length; i++) {
				attributeList.add(attributeLine[i].toUpperCase());
			}
			while (in.hasNextLine()) {
				Sentence sentence = new Sentence();
				line = in.nextLine();
				parts = line.split("\\s+");
				
				for (String word: parts) {
					word = word.toUpperCase();
					if (word.endsWith(".")) {
						word = word.replace(".", "");
						sentence.addWord(word);
					} else {
						sentence.addWord(word);
					}
				}
				input.add(sentence);
			}
		} catch (IOException e) {
			System.out.println("Plik nie zostal wczytany poprawnie!");
			e.printStackTrace();
			System.exit(-1);
		}
		return input;
	}
	
	public List<String> getAttributeList() {
		return attributeList;
	}
}
