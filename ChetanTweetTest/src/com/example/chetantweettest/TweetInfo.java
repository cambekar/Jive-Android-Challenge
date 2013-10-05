package com.example.chetantweettest;

/**
 * @author chetanambekar
 * File: TweetInfo.java
 * Description: Objects of TweetInfo will be created and stored
 * in an ArrayList by the ListGenerator. ListGenerator will call
 * its setter methods to set userName, the tweet and profile pic URL
 * on to this object after parsing the JSONArray acquired from the 
 * downloaded stream.
 * CallBackActivity will retrieve these objs from the ArrayList created by
 * the listGenerator and display the information. 
 *
 */
public class TweetInfo {

	private String userName;
	private String tweettext;
	private String userProfileURL;

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getTweettext() {
		return tweettext;
	}
	public void setTweettext(String tweettext) {
		this.tweettext = tweettext;
	}
	public String getUserProfileURL() {
		return userProfileURL;
	}
	public void setUserProfileURL(String userProfileURL) {
		this.userProfileURL = userProfileURL;
	}


}
