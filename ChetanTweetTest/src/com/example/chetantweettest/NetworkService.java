package com.example.chetantweettest;


import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONObject;

import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * @author chetanambekar
 * File: NetworkService.java
 * Description: NetworkService is the main background service which 
 * provides the following APIS:
 * API: Clients must call this API to retrive Request Token from the server. 
 * On success/failure Intent IINTENT_REQUEST_TOKENS_RETRIEVED
 * will be sent out with String Extra "result" set to "true"/"false".
 * void login();
 * 
 * API: Clients must call this API to retrive user credentials (retrive Access tokens) 
 * before attempting to fetch streams. On success/failure Intent INTENT_RETRIVE_USER_CREDENTIALS
 * will be sent out with String Extra "result" set to "true"/"false".
 * void retriveUserCredentials(in Uri uri);
 * 
 * API: To start fetching trending tweets. The API returns a unique ID to the caller
 * in this case hashCode of the Thread created to perform the task. Clients can use this 
 * ID to cancel a requested Stream if they desire. 
 * The API takes as input IStreamUpdateListener which it uses to return the tweeter feed 
 * once it is sucessfully downloaded, thus allowing this api to remain asynchronous.
 * At anytime at most 3 requests will be in progress. This is acheieved
 * by using an ExecutorService and setting the FixedThreadPoolCount of 3 on the executor.
 * Any additional requests will
 * remain queued up until one of the running request finishes. 
 * int fetchStream(IStreamUpdateListener s);
 * 
 * API: To cancel a previously requested Stream. Stream either queued due to 
 * max downloads (3) in progress or currently downloading in both cases will be
 * cancelled.
 * boolean cancelStream(int StreamID);
 * CB From Network Service: 
 * To be implemented by clients to receive Stream after successful download.
 * void streamUpdate(String stream);
 * 
 * Within itself this class has two AsynTasks: AuthenticationTask -> to retrieveRequestToken
 *	and RetriveTask -> to retrieveAccessToken.
 *	The FetchStream Operation is performed in the a seperate Runnable(FetchStreamRunnable)
 *	and executed using a ExecutorService. Each time a request is made to get the new stream 
 *	the runnable of type FetchStreamRunnable is created and submitted to ExecutorService. 
 *	In return a Future is obtained from the ExecutorService. This future (Value) along with 
 *	hashCode of the Thread (key) is stored in the HastTable activeOrPendingStreams.
 *	The Key is removed from the HashTable if:
 *	1) The stream is successfully downloaded and job is completed
 *	2) A stream is cancelled using the cancelStream(int) API.
 *
 *	The Service also sends out the following intents to update clients on it's progress.
 *	Const.INTENT_REQUEST_TOKENS_RETRIEVED -> Once retriveRequestToken is performed and 
 *	result is store as String Extra on the intent.
 *	Const.INTENT_RETRIVE_USER_CREDENTIALS -> Once retrieveAccessToken is performed and 
 *	result is store as String Extra on the intent.
 *
 */

public class NetworkService extends Service {

	private OAuthHelper oAuthHelper; 
	private AuthenticationTask authtask;
	private RetriveTask retriveTask;

	//Maximum of 3 Runnables to be executed at any given time. The remaining will remain queued up.
	private ExecutorService executor = Executors.newFixedThreadPool(3);

	//HashTable to keep track of current active stream downloads or pending ones. 
	private Hashtable<Integer,Future> activeOrPendingStreams = new Hashtable<Integer,Future>();


	@Override
	public IBinder onBind(Intent intent) {

		return networkBinder;
	}


	@Override
	public void onCreate() {

		super.onCreate();
		oAuthHelper = OAuthHelper.getInstance();

	}


	@Override
	public void onDestroy() {

		super.onDestroy();
		//Cleaning up
		oAuthHelper = null; 
		authtask = null;
		retriveTask = null;
		activeOrPendingStreams = null;
		if(executor != null) executor.shutdown();
		executor = null;
	}


	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);
	}


	private final INetworkService.Stub networkBinder = new INetworkService.Stub() {

		@Override
		public void login() throws RemoteException {

			//AsyncTask created to perform NetworkOperations on separate thread other than main.
			authtask = new AuthenticationTask();
			authtask.execute(new String[] {});

		}

		@Override
		public void retriveUserCredentials(Uri uri) throws RemoteException {

			//AsyncTask created to perform NetworkOperations on separate thread other than main.
			retriveTask = new RetriveTask();
			retriveTask.execute(uri);

		}

		@Override
		public int fetchStream(IStreamUpdateListener streamUpdateListener) throws RemoteException {

			//Creating a new thread of each request, getting a uniqueID for each request, submitting the
			//runnable to ExecutorService, storing the threadID and Future in HashTable and returning 
			//ThreadID to caller.
			Runnable mStreamRunnable = new FetchStreamRunnable(streamUpdateListener,NetworkService.this);
			int threadID = mStreamRunnable.hashCode();
			Future threadFuture = executor.submit(mStreamRunnable);
			addStreamToRunningList(threadID,threadFuture);		
			return threadID;

		}

		@Override
		public boolean cancelStream(int streamID) throws RemoteException {

			Log.d(Const.TAG,"Cancelling Stream with ID:"+streamID);

			return _cancelStreamAndCleanTable(streamID);
		}


	};

	//Getter method to get the table of current active or pending streams.
	public Hashtable<Integer,Future> getActiveOrPendingStreams() {
		return activeOrPendingStreams;
	}

	//Keeping the Setter and local hashTable object private. We don't want it to be overwritten.
	private void setActiveOrPendingStreams(Hashtable<Integer,Future> activeOrPendingStreams) {
		this.activeOrPendingStreams = activeOrPendingStreams;
	}

	//AsyncTask to retrive Request Token from the server.
	private class AuthenticationTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String...params) {
			String response = "false";


			try {
				CommonsHttpOAuthConsumer httpOauthConsumer = oAuthHelper
						.getHttpOauthConsumer();
				String authUrl = oAuthHelper.getHttpOauthprovider().retrieveRequestToken(
						httpOauthConsumer, Const.CALLBACKURL);

				response = "true";
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				NetworkService.this.startActivity(intent);


			} catch (Exception e) {
				Log.e(Const.TAG, "OAuth Failed while retriving tokens "+e.getMessage());

			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d(Const.TAG,"Authentication result is:"+result);
			sendIntent(Const.INTENT_REQUEST_TOKENS_RETRIEVED, result);

		}
	}

	//AsyncTask to retrive and set Access tokens.
	private class RetriveTask extends AsyncTask<Uri, Void, String> {


		@Override
		protected String doInBackground(Uri... arg0) {
			String response = "false";

			Uri uri = arg0[0];
			if (uri != null && uri.toString().startsWith(Const.CALLBACKURL)) {

				String verifier = uri
						.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);

				try {

					CommonsHttpOAuthConsumer httpOauthConsumer = oAuthHelper
							.getHttpOauthConsumer();
					oAuthHelper.getHttpOauthprovider().retrieveAccessToken(
							httpOauthConsumer, verifier);

					String userKey = httpOauthConsumer.getToken();
					String userSecret = httpOauthConsumer.getTokenSecret();


					SharedPreferences settings = getBaseContext()
							.getSharedPreferences(Const.SHARED_PREFS_KEY, 0);

					SharedPreferences.Editor editor = settings.edit();
					editor.putString(Const.USER_KEY, userKey);
					editor.putString(Const.USER_SECRET, userSecret);
					editor.commit();
					response = "true";

				} catch (Exception e) {
					Log.e(Const.TAG, "Execption while retriving User Credentials "+e.getMessage());

				}
			} else {
				Log.e(Const.TAG, "Unknown Call back");
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d(Const.TAG,"Retrive of user credentials result is:"+result);
			sendIntent(Const.INTENT_RETRIVE_USER_CREDENTIALS,result);

		}

	}

	private void addStreamToRunningList(int threadID, Future threadFuture){
		//We don't need to check if threadID exists, we assume each hashCode of Runnable gives us 
		//a distinct value.
		getActiveOrPendingStreams().put(threadID,threadFuture);
	}

	private boolean _cancelStreamAndCleanTable(int streamID){

		Future threadFuture = getActiveOrPendingStreams().get(streamID);
		if(threadFuture == null){
			Log.e(Const.TAG,"Error canceling Stream, Stream doesn't exist");
			return false;
		}else{
			boolean result = threadFuture.cancel(true);
			if(result){
				//We want to remove the threadID from the HashTable only if 
				//it was successfully cancelled. If not cancelled then threadID
				//will be removed after completion.
				getActiveOrPendingStreams().remove(streamID);
			}
			return result;
		}

	}


	//Helper method to send out Intents.
	private void sendIntent(String msg, String result){

		Intent resultIntent = new Intent(msg);
		resultIntent.putExtra(Const.RESULT, result);
		sendBroadcast(resultIntent);
	}



}