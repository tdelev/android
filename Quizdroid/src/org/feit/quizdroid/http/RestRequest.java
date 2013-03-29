package org.feit.quizdroid.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.feit.quizdroid.Answer;
import org.feit.quizdroid.Player;
import org.feit.quizdroid.Question;
import org.feit.quizdroid.Quiz;
import org.feit.quizdroid.Score;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RestRequest {
	static final String TAG = "RestRequest";

	public static List<Quiz> getQuizes(Player player) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = Constants.QUIZES_URL;
		Log.d(TAG, url);
		url = url + "?did=" + player.deviceId;
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			List<Quiz> result = parseQuizesResponse(json);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Quiz getQuiz(Quiz quiz, Player player) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = Constants.QUIZ_URL;
		HttpPost httpPost = new HttpPost(url);
		final HttpParams params = createParamsForPosting();
		httpClient.setParams(params);
		NameValuePair quiz_id = new BasicNameValuePair("quiz_id", String
				.valueOf(quiz.id));
		NameValuePair name = new BasicNameValuePair("name", player.getName());
		NameValuePair device_id = new BasicNameValuePair("device_id",
				player.deviceId);
		NameValuePair imsi = new BasicNameValuePair("imsi", player.imsi);
		NameValuePair model = new BasicNameValuePair("model", player.model);
		NameValuePair release = new BasicNameValuePair("release",
				player.release);
		List<NameValuePair> listParams = new ArrayList<NameValuePair>();
		listParams.add(quiz_id);
		listParams.add(name);
		listParams.add(device_id);
		listParams.add(imsi);
		listParams.add(model);
		listParams.add(release);
		Log.d(TAG, url);
		UrlEncodedFormEntity requestEntity = null;
		try {
			requestEntity = new UrlEncodedFormEntity(listParams, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		httpPost.setEntity(requestEntity);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			Log.d(TAG, "code: " + response.getStatusLine().getStatusCode());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			quiz = parseQuizResponse(json, quiz);
			return quiz;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Score postAnswers(Quiz quiz, Player player) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = Constants.POST_URL;
		HttpPost httpPost = new HttpPost(url);
		final HttpParams params = createParamsForPosting();
		httpClient.setParams(params);
		NameValuePair quiz_id = new BasicNameValuePair("quiz_id", String
				.valueOf(quiz.id));
		NameValuePair name = new BasicNameValuePair("name", player.getName());
		NameValuePair device_id = new BasicNameValuePair("device_id",
				player.deviceId);
		NameValuePair imsi = new BasicNameValuePair("imsi", player.imsi);
		StringBuffer answers = new StringBuffer();
		for (Question q : quiz.questions)
			for (Answer a : q.answers) {
				if (a.selected) {
					answers.append(",");
					answers.append(a.id);
				}
			}
		String a = answers.substring(1);
		NameValuePair na = new BasicNameValuePair("answers", a);
		List<NameValuePair> listParams = new ArrayList<NameValuePair>();
		listParams.add(quiz_id);
		listParams.add(name);
		listParams.add(device_id);
		listParams.add(imsi);
		listParams.add(na);
		UrlEncodedFormEntity requestEntity = null;
		try {
			requestEntity = new UrlEncodedFormEntity(listParams, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		httpPost.setEntity(requestEntity);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			Score score = parseScoreResponse(json);
			return score;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static List<Quiz> parseQuizesResponse(String json) {
		try {
			JSONArray quizes = new JSONArray(json);
			int size = quizes.length();
			Log.d(TAG, "size: " + size);
			List<Quiz> result = new ArrayList<Quiz>(size);
			for (int i = 0; i < size; i++) {
				JSONObject q = quizes.getJSONObject(i);
				int id = q.getInt("id");
				String name = q.getString("name");
				Date date_launch = new Date(q.getLong("date_launch"));
				Date date_finish = new Date(q.getLong("date_finish"));
				boolean played = q.getBoolean("played");
				int score = q.getInt("score");
				Quiz quiz = new Quiz(id, name, date_launch, date_finish,
						played, score);
				result.add(quiz);
			}
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Quiz parseQuizResponse(String json, Quiz quiz) {
		try {
			JSONArray questions = new JSONArray(json);
			int size = questions.length();
			for (int i = 0; i < size; i++) {
				JSONObject q = questions.getJSONObject(i);
				int id = q.getInt("id");
				String text = q.getString("question");
				Question question = new Question(id, text);
				JSONArray answers = q.getJSONArray("answers");
				for (int j = 0; j < answers.length(); j++) {
					JSONObject a = answers.getJSONObject(j);
					Answer answer = new Answer();
					answer.id = a.getInt("answer_id");
					answer.text = a.getString("answer_text");
					question.answers.add(answer);
				}
				quiz.questions.add(question);
			}
			return quiz;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Score parseScoreResponse(String json) {
		try {
			JSONObject s = new JSONObject(json);
			Score score = new Score();
			score.score = s.getInt("score");
			score.time = s.getInt("time");
			return score;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static HttpParams createParamsForPosting() {
		final HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		HttpProtocolParams.setUseExpectContinue(params, false); // solves the
		// '417' issue
		return params;

	}
}
