package com.example.chetantweettest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/**
 * @author chetanambekar
 * File: ListGenerator.java
 * Description: Helper class to provide two methods
 * generateList(String stream) - Takes the downloaded stream as input 
 * creates a JSONArray out of it parses the fields userName, tweet and
 * profile pic, creates an obj of TweetInfo and stored tweetinfo into
 * the List.
 * 
 * getFeedList() - returns List<TweetInfo> created by generateList.
 *
 */
public class ListGenerator {

	private List<TweetInfo> feedList = new ArrayList<TweetInfo>();
	private JSONArray array;
	private JSONObject obj, user;

	public List<TweetInfo> getFeedList() {
		return feedList;
	}

	public List<TweetInfo> generateList(String stream){


		try {
			array = new JSONArray(stream);
			String tweetText, userName, userProfileUrl;
			for (int i = 0; i < array.length(); i++) {
				obj = array.getJSONObject(i);
				tweetText = obj.getString("text");
				user = obj.getJSONObject("user");
				userName = user.getString("name");
				userProfileUrl = user.getString("profile_image_url_https");

				TweetInfo aInfo = new TweetInfo();
				feedList.add(aInfo);

				aInfo.setTweettext(tweetText);
				aInfo.setUserName(userName);
				aInfo.setUserProfileURL(userProfileUrl);

			}
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			//Cleaning up
			array = null;
			obj=null;
			user = null;
		}

		return feedList;

	}
}
