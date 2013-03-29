package org.feit.jokesmk;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class JokesSearch extends Activity {
	private ListView mListView;
	private JokesListAdapter mListAdapter;
	private JokesProcessor mJokesProcessor;
	private List<Joke> mJokes;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jokes_search);
		mJokesProcessor = new JokesProcessor(this);
		mListView = (ListView) findViewById(R.id.jokes_list);
		mListView.setEmptyView(findViewById(R.id.no_jokes));
		mJokes = new ArrayList<Joke>();
		mListAdapter = new JokesListAdapter(this, mJokes);
	}
}
