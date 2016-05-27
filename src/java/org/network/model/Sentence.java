package org.network.model;

/**
 *
 * @author Mateusz Kaproń
 */

import java.util.ArrayList;
import java.util.List;

public class Sentence {

	private List<String> words = new ArrayList<>();

	public List<String> getWords() {
		return words;
	}

	public void setWords(List<String> words) {
		this.words = words;
	}

	public void addWord(String word) {
		this.words.add(word);
	}
	
	@Override
	public String toString() {
		String sent = "";
		for (String word: words) {
			sent = sent.concat(" ").concat(word);
		}
		return sent;
	}
}

