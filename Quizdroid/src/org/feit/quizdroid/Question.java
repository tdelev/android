package org.feit.quizdroid;

import java.util.ArrayList;
import java.util.List;

public class Question {
	public int id;
	public String text;
	
	public List<Answer> answers;
	
	public Question(int id, String text) {
		this.id = id;
		this.text =  text;
		answers = new ArrayList<Answer>();
	}
}
