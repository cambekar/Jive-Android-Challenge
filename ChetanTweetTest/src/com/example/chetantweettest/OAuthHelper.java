package com.example.chetantweettest;


import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

/**
 * @author chetanambekar
 * File: OAuthHelper.java
 * Description: OAuthHelper class provides a singleton Design Pattern. The
 * class encapsulates objects of OAuthProvider and CommonsHttpOAuthConsumer.
 * The singleton design patter is choosen so as to have a single instance of
 * of OAuthProvider and CommonsHttpOAuthConsumer throughout the 
 * implementation. 
 *
 */
public class OAuthHelper {

	//Making the instance volatile so as to ensure the most updated 
	//value is read from the Main Memory. 
	private volatile static OAuthHelper instance = null;

	private OAuthProvider httpOauthprovider;

	private CommonsHttpOAuthConsumer httpOauthConsumer;

	//Helper method to get OAuthProvider's instance
	public OAuthProvider getHttpOauthprovider() {
		return httpOauthprovider;
	}

	//Helper method to get CommonsHttpOAuthConsumer's instance
	public CommonsHttpOAuthConsumer getHttpOauthConsumer() {
		return httpOauthConsumer;
	}

	//Setter methods for above two objects are not exposed.

	private OAuthHelper() {
		// Prevents instantation by clients.
	}

	//Making the method synchronized so that in the event of multiple
	//threads accessing the method the possibility 2 instances of OAuthHelper 
	//beiing created is avoided.
	public synchronized static OAuthHelper getInstance() {
		if (instance == null) {
			instance = new OAuthHelper();
			instance.httpOauthprovider = new CommonsHttpOAuthProvider(Const.REQUEST_TOKEN,
					Const.ACCESS_TOKEN, Const.AUTHORIZE);

			instance.httpOauthConsumer = new CommonsHttpOAuthConsumer(
					Const.CONSUMER_KEY, Const.CONSUMER_SECRET);
		}
		return instance;
	}
}
