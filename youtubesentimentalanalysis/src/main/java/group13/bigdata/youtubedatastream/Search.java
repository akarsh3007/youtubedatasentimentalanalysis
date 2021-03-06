package group13.bigdata.youtubedatastream;

import group13.bigdata.youtubedatastream.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.net.*;
import org.json.*;

/**
 * Print a list of videos matching a search term.
 *
 * @author Sharan Girdhani
 */
public class Search {

	/**
	 * Define a global variable that identifies the name of a file that contains
	 * the developer's API key.
	 */
	private static YoutubeDataProducer producer;

	public static void main(String[] args) {
		try {

			// initailize producer class to initalize config
			producer = new YoutubeDataProducer();
			// Prompt the user to enter a query term.
			String queryTerm = getInputQuery();

			URL url = new URL(Constants.VIDEO_BASE_URL + queryTerm + "&key=" + Constants.API_KEY);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer json = new StringBuffer();
			String line;

			while ((line = reader.readLine()) != null) {
				json.append(line);
			}
			reader.close();

			JSONObject jo = null;

			List<Video> videoList = new ArrayList<Video>();

			try {
				jo = new JSONObject(json.toString());
				JSONArray items = jo.getJSONArray(Constants.ITEMS);

				for (int i = 0; i < items.length(); i++) {
					JSONObject obj = items.getJSONObject(i);
					String video_title = obj.getJSONObject(Constants.SNIPPET).getString(Constants.VIDEO_TITLE);
					String video_desc = obj.getJSONObject(Constants.SNIPPET).getString(Constants.VIDEO_DESC);
					String video_id = items.getJSONObject(i).getJSONObject(Constants.ID).getString(Constants.VIDEO_ID);
					ArrayList<Comment> commentList = new ArrayList<Comment>();

					URL url2 = new URL(Constants.COMMENT_BASE_URL + Constants.NUMBER_OF_COMMENTS_PER_VIDEO + "&videoId="
							+ video_id + "&key=" + Constants.API_KEY);
					BufferedReader reader2 = new BufferedReader(new InputStreamReader(url2.openStream()));
					StringBuilder json2 = new StringBuilder();
					String line2 = null;

					while ((line2 = reader2.readLine()) != null) {
						json2.append(line2);
					}
					reader2.close();

					JSONObject comments = null;
					try {
						comments = new JSONObject(json2.toString());
						json2.setLength(0);
						JSONArray items2 = comments.getJSONArray(Constants.ITEMS);
						// System.out.println(items);

						for (int j = 0; j < items2.length(); j++) {
							String comment_text = null;
							boolean isOriginalText = items2.getJSONObject(j).getJSONObject(Constants.SNIPPET)
									.getJSONObject(Constants.TOP_LEVEL_COMMENT).getJSONObject(Constants.SNIPPET)
									.has(Constants.COMMEXT_TEXT);
							if (isOriginalText) {
								comment_text = items2.getJSONObject(j).getJSONObject(Constants.SNIPPET)
										.getJSONObject(Constants.TOP_LEVEL_COMMENT).getJSONObject(Constants.SNIPPET)
										.getString(Constants.COMMEXT_TEXT);

								comment_text = comment_text.replaceAll("[^a-zA-Z\\s]", "").trim();
								Comment com = null;
								if (!comment_text.isEmpty()){
									//System.out.println(comment_text);
								 com = new Comment(comment_text);
								}
								StringJoiner joiner = new StringJoiner("|");
								joiner.add(video_id).add(video_title).add(video_desc).add(comment_text);
								try {
									Thread.sleep(300);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if(!comment_text.isEmpty()){
								producer.produceYouTubeData(Constants.TOPIC_NAME,
								 joiner.toString());
								commentList.add(com);
								}

							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Video video = new Video(video_id, video_title, video_desc, commentList);
					videoList.add(video);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			System.err.println("There was an error reading " + ": " + e.getCause() + " : " + e.getMessage());
			System.exit(1);
		}
	}

	/*
	 * Prompt the user to enter a query term and return the user-specified term.
	 */
	private static String getInputQuery() throws IOException {

		String inputQuery = "";

		System.out.print("Please enter a search term: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		inputQuery = bReader.readLine();
		inputQuery = inputQuery.replace(" ", "+");
		if (inputQuery.length() < 1) {
			// Use the string "Big Data" as a default.
			inputQuery = "Big+Data";
		}
		return inputQuery;
	}
}