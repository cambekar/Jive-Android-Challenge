package com.example.chetantweettest;

/**
 * @author chetanambekar
 * File: Const.java
 * Description: Generic Constants file. We do not need to put this
 * in res/values/strings.xml since they do not need to be 
 * localized. 
 *
 */
public class Const {

	public final static String TAG = "ChetanTweetTest";

	public static String AUTEHNTICATION_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";

	public static String REQUEST_TOKEN = "https://api.twitter.com/oauth/request_token";

	public static String ACCESS_TOKEN = "https://api.twitter.com/oauth/access_token";

	public static String AUTHORIZE ="https://api.twitter.com/oauth/authorize";

	public static final String CONSUMER_KEY = "BSaXFKtCkhZ8LoJifTY5A";

	public static final String CONSUMER_SECRET = "ufwoZdERJgcyKqCZoyrkxKzUrkyeU48LY8Cl28qiJ8";

	public static String CALLBACKURL = "app://twitter2";

	public static String GET_HOME_TIMELINE = "http://api.twitter.com/1.1/statuses/home_timeline.json";

	public static String INTENT_REQUEST_TOKENS_RETRIEVED = "com.example.chetantweettest.requestTokensReceived";

	public static String INTENT_RETRIVE_USER_CREDENTIALS = "com.example.chetantweettest.userCredentials";

	public static String RESULT = "result";

	public static String SHARED_PREFS_KEY = "chetantweettest_prefs";

	public static String USER_KEY = "user_key";

	public static String USER_SECRET = "user_secret";

}
