package com.example.chetantweettest;

import java.util.ArrayList;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author chetanambekar
 * File: CallBackActivity.java
 * Description: This is the CallBackActivity once the app has been
 * granted access by the server, Twitter in this case. Upon getting 
 * the acess this Activity uses its binder to the NetworkService to 
 * retrive Access Tokens which can be use to fetch streams. 
 * 
 * The Activity has a broadcastListener which listens on Intent
 * INTENT_RETRIVE_USER_CREDENTIALS and upon successful retrival 
 * calls INetworkService API fetchStreams and provides the 
 * CB handle to get steamUpdated(). NOTE: This call be done on main thread
 * since the api fetchStream is asynchronous and returns immediately.A ThreadID 
 * is returned which can be used to cancel that particular request. At 
 * this point a ProgressDialouge is shown to the User. 
 * 
 * Upon sucessfully downloading the stream the Activity makes use of 
 * helper class ListGenerator which parses the stream and creates a
 * ArrayList of the TweetInfo. Currently TweetInfo contains useName,
 * the tweet and URL to profile picture.
 * 
 * After the the list is generated the activity uses sendMessage API of
 * the handler to get control back into Main Thread since this processing
 * of the list was done is the CB thread. 
 * 
 * Once control is gained back onto the Main Thread where the intital 
 * View was set the list items are updated and latest trending tweets are
 * available.
 * 
 * At any given point multiple calls can be made to fetchStream, especially
 * in the scenario when the user is constantly scrolling. INetworkService 
 * also provides a cancelStream api to cancel the stream using the ThreadID
 * received from fetchStream. This could most often be used when the user
 * is contantly scrolling in opposite directions rendering the previous request
 * of no use.
 *
 *
 */

public class CallBackActivity extends ListActivity {

	public class HandlerMessages {
		public final static int FEED_LIST_GENERATED = 1;
		//TODO: Handler currently only handles success conditions. Need to handle 
		//feed download failure

	};

	private OAuthHelper oAuthHelper;
	private INetworkService netService;
	private NetworkServiceConnection netConnection;
	private MyBroadcastReceiver myBroadcastReceiver;
	private Uri uri;
	private ArrayList<TweetInfo> mFeedList = null;
	private AppInfoAdapter mFeedInfoAdapter;
	private Resources mRes;
	private ListGenerator listGenerator;
	private ProgressDialog fetchTweetPD;


	//Handler to update the View once List is downloaded and parsed successfully
	public Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			Log.d(Const.TAG, "Message Handler:" + msg.what);
			switch (msg.what) {
			case HandlerMessages.FEED_LIST_GENERATED:
				Log.d(Const.TAG, "HandlerMessages--->FEED_LIST_GENERATED");
				fetchTweetPD.dismiss();

				mFeedList = (ArrayList<TweetInfo>) listGenerator.getFeedList();


				Log.d(Const.TAG, "Parsed and generated List from stream"
						+ mFeedList.size());

				if (mFeedList != null && mFeedList.size() > 0) {
					mFeedInfoAdapter.clear();
					mFeedInfoAdapter.notifyDataSetChanged();

					for (int i = 0; i < mFeedList.size(); i++) {
						mFeedInfoAdapter.add(mFeedList.get(i));
					}
				}
				break;

			}
		}

	};

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_back);

		//Storing URI from data to verify callback from server 
		Intent myIntent = getIntent();
		uri = myIntent.getData();

		//Setting up services and views
		bindServices();
		init();

		//Creating broadcastListner and adding intent filters
		myBroadcastReceiver = new MyBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(Const.INTENT_RETRIVE_USER_CREDENTIALS);	
		registerReceiver(myBroadcastReceiver, intentFilter);

		//Singleton instance of OAuthHelper
		oAuthHelper = OAuthHelper.getInstance();

	}

	protected void onDestroy(){

		super.onDestroy();
		//Clean up
		unregisterReceiver(myBroadcastReceiver);
		unbindService(netConnection);
		oAuthHelper = null;
		netService = null;
		netConnection = null;
		myBroadcastReceiver = null;
		uri = null;
		mFeedList = null;
		mFeedInfoAdapter = null;
		mRes = null;
		listGenerator = null;
		fetchTweetPD = null;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
	}

	private void bindServices(){

		netConnection = new NetworkServiceConnection();
		Intent i = new Intent(this,NetworkService.class);
		bindService(i,netConnection, Context.BIND_AUTO_CREATE);
		Log.d(Const.TAG,"Binding Services done");


	}

	//Binding to Our Main NetworkService that does all the heavy lifting and threadManagement
	private class NetworkServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder boundService) {
			netService = INetworkService.Stub.asInterface((IBinder) boundService);
			Log.d(Const.TAG, "onServiceConnected() connected");
			try {
				netService.retriveUserCredentials(uri);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void onServiceDisconnected(ComponentName name) {
			netService = null;
			unregisterReceiver(myBroadcastReceiver);
			Log.d(Const.TAG, "onServiceDisconnected() disconnected");

		}
	}


	//Implemented IStreamUpdateListener CB to be notified when my requested stream is downloaded
	private IStreamUpdateListener mStreamUpdateCallback = new IStreamUpdateListener.Stub() {

		@Override
		public void streamUpdate(String stream) throws RemoteException {
			Log.d(Const.TAG,"Got Stream Update"+stream);


			mFeedList = (ArrayList<TweetInfo>) listGenerator.generateList(stream);

			handler.sendEmptyMessage(CallBackActivity.HandlerMessages.FEED_LIST_GENERATED);


		}

	};

	private void init() {
		Log.d(Const.TAG, "Creating Initial List of Tweets");
		mFeedList = new ArrayList<TweetInfo>();
		mFeedList.clear();
		mFeedInfoAdapter = new AppInfoAdapter(this, R.layout.activity_call_back,
				mFeedList);
		setListAdapter(mFeedInfoAdapter);
		mRes = getResources();
		listGenerator = new ListGenerator();

	}

	private class AppInfoAdapter extends ArrayAdapter<TweetInfo> {
		private ArrayList<TweetInfo> mFeedList;

		public long getItemId(int position) {
			int imax = mFeedList.size();
			if ((position < 0) || (position >= imax)) {
				Log.w(Const.TAG, "Position out of bounds in List Adapter");
				return -1;
			}
			TweetInfo aInfo = mFeedList.get(position);
			if (aInfo == null) {
				return -1;
			}
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			View v;

			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.main_list_item, null);
			v.setOnClickListener(new OnItemClickListener(position));

			TweetInfo mTweetInfo = mFeedList.get(position);
			if (mTweetInfo != null) {
				TextView userNameTextView = (TextView) v.findViewById(R.id.user_name);
				TextView tweetTextView = (TextView) v.findViewById(R.id.tweet);


				if (userNameTextView != null) {
					userNameTextView.setText(mTweetInfo.getUserName());
				}

				if (tweetTextView != null) {
					tweetTextView.setText(mTweetInfo.getTweettext());
				}


				ImageView iconHolder;

				iconHolder = (ImageView) v.findViewById(R.id.icon_main);

				Drawable myIcon = null;
				try{

					myIcon =  null;//TODO:get Profile Pic thumbnail using mTweetInfo.getUserProfileURL()
				}catch(Exception e){
				}

				if (myIcon != null) {
					iconHolder.setImageDrawable(myIcon); //TODO:Set the downloaded thumbnail
				} else {
					iconHolder.setImageResource(R.drawable.twitter);
				}
			}

			return v;
		}

		class OnItemClickListener implements OnClickListener {
			private int mPosition;

			OnItemClickListener(int position) {
				mPosition = position;
			}

			public void onClick(View view) {
				/* we should be able to base switch statement on View name */

				// View area was clicked
				onItemViewClicked(mPosition);

			}
		}

		private void onItemViewClicked(int position){

			Log.d(Const.TAG,"Twitter feed clicked");

		}

		public int getCount() {
			return mFeedList.size();
		}

		public AppInfoAdapter(Context c, int appNameResourceId,
				ArrayList<TweetInfo> aList) {
			super(c, appNameResourceId, aList);
			mFeedList = aList;
		}
	}


	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(Const.TAG,"Received Intent");
			String result = intent.getStringExtra(Const.RESULT);

			int threadID;
			if(result != null && result.equals("true"))
				try {
					threadID = netService.fetchStream(mStreamUpdateCallback);
					//Fetching stream, we will proceed with parsing the stream 
					//and creating out List after we get a callback in method streamUpdate();
					//Until then showing a progress dialogue.
					fetchTweetPD = new ProgressDialog(CallBackActivity.this);
					fetchTweetPD.setMessage(mRes.getString(R.string.fetching_feed));
					fetchTweetPD.show();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}


	}


}
