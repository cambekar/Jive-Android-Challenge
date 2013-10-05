package com.example.chetantweettest;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author chetanambekar
 * File: FetchStreamRunnable.java
 * Description: FetchStreamRunnable class implements Runnable.
 * A new instance of FetchStreamRunnable is created each time a
 * new request is made to download the treanding tweets.
 * The Runnable ID is used as its primary key and stored in 
 * a HashTable along with its Future assigned when submitted to the
 * ExecutorService. This ID is used to monitor, handle, cancel streams.
 *
 */
public class FetchStreamRunnable implements Runnable {

	private IStreamUpdateListener mStreamUpdateListener;
	private NetworkService mNetService;
	private OAuthHelper oAuthHelper;

	public FetchStreamRunnable(IStreamUpdateListener s, NetworkService c){
		mStreamUpdateListener = s;
		mNetService = c;
		oAuthHelper = OAuthHelper.getInstance();

	}

	public void run() {
		_fetchStream(mStreamUpdateListener);
	}

	private void _fetchStream(IStreamUpdateListener s) {

		Log.d(Const.TAG,"Starting to fetch stream for threadID :"+this.hashCode());

		HttpGet get = new HttpGet(Const.GET_HOME_TIMELINE);
		HttpParams params = new BasicHttpParams();

		HttpProtocolParams.setUseExpectContinue(params, false);
		get.setParams(params);

		try {
			SharedPreferences settings = mNetService.getBaseContext().getSharedPreferences(
					Const.SHARED_PREFS_KEY, 0);
			String userKey = settings.getString(Const.USER_KEY, "");
			String userSecret = settings.getString(Const.USER_SECRET, "");

			CommonsHttpOAuthConsumer httpOauthConsumer = new CommonsHttpOAuthConsumer(
					Const.CONSUMER_KEY, Const.CONSUMER_SECRET);

			httpOauthConsumer.setTokenWithSecret(userKey, userSecret);
			httpOauthConsumer.sign(get);

			DefaultHttpClient client = new DefaultHttpClient();
			String response = client.execute(get, new BasicResponseHandler());

			Log.d(Const.TAG,"Stream Successfully fetched for threadID :"+this.hashCode());

			//Threads job is done. removing it from HashTable.
			mNetService.getActiveOrPendingStreams().remove(this.hashCode());

			//Stream downloaded sending stream to client using the registered CB
			s.streamUpdate(response);

		} catch (Exception e) {

			Log.e(Const.TAG,"Exception in fetching stream for threadID: "+this.hashCode());
			e.printStackTrace();
		} finally{
			//Cleaning up
			get = null;
			params = null;
			mStreamUpdateListener = null;
			mNetService = null;
			oAuthHelper = null;

		}

	}



}
