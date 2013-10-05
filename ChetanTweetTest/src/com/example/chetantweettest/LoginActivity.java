package com.example.chetantweettest;


import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author chetanambekar
 * File: LoginActivity.java
 * Description: Main Launcher Activity with one button click
 * to initiate login using INetworkService API login.
 *
 */
public class LoginActivity extends Activity {

	private Button loginButton;
	private INetworkService netService;
	private NetworkServiceConnection netConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		bindServices();
		initControls();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}


	protected void onDestroy(){
		super.onDestroy();
		//Cleaning up members
		unbindService(netConnection);
		netService = null;
		netConnection = null;
		loginButton = null;
	}

	private void bindServices(){

		netConnection = new NetworkServiceConnection();
		Intent i = new Intent(this,NetworkService.class);
		bindService(i,netConnection, Context.BIND_AUTO_CREATE);
		Log.d(Const.TAG,"Binding Services done");

	}

	private void initControls()
	{
		loginButton=(Button)findViewById(R.id.login);
		loginButton.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View  v) { 
				try {
					Log.d(Const.TAG,"Initiating the login using INetworkService API login");
					netService.login();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}


	private class NetworkServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder boundService) {
			netService = INetworkService.Stub.asInterface((IBinder) boundService);
			Log.d(Const.TAG, "LoginActivity connected to INetworkService");

		}

		public void onServiceDisconnected(ComponentName name) {
			netService = null;
			Log.d(Const.TAG, "LoginActivity disconnected from INetworkService");

		}
	}

}
