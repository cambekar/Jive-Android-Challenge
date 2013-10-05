package com.example.chetantweettest;

import com.example.chetantweettest.IStreamUpdateListener;

interface INetworkService {
	
	//API: Clients must call this API to retrive Request Token from the server.
	void login();
	
	//API: Clients must call this API to retrive user credentials before
	//attempting to fetch streams. On success/failure Intent INTENT_RETRIVE_USER_CREDENTIALS
	//will be sent out with String Extra "result" set to "true"/"false".
	void retriveUserCredentials(in Uri uri);
	
	//API: To start fetching trending tweets
	int fetchStream(IStreamUpdateListener s);
	
	//API: To cancel a previously requested Stream. Stream either queued due to 
	//max downloads (3) in progress or currently downloading both cases will be
	//cancelled.
	boolean cancelStream(int StreamID);
	
    
}