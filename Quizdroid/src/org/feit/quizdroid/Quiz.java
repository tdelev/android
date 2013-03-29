package org.feit.quizdroid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Quiz implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int id;
	public String name;
	public Date date_launch;
	public Date date_finish;
	public boolean played;
	public int score;
	public List<Question> questions;
	
	public Quiz(int id, String name, Date date_launch, Date date_finish, boolean played, int score) {
		this.id = id;
		this.name = name;
		this.date_launch = date_launch;
		this.date_finish = date_finish;
		this.played = played;
		this.score = score;
		questions = new ArrayList<Question>();
	}
}
