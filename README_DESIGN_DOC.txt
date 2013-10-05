Author: Chetan Ambekar
Application Name : ChetanTweetTest
Package Name : "com.example.chetantweettest"
Version Code: "1"
Version Name:"1.0"
Target SDK Version: 18
Permissions: "android.permission.INTERNET"
Libraries: signpost-commonshttp4-1.2.1.1.jar \
		    signpost-core-1.2.1.1.jar


How to:

1) Install ChetanTweetTest.apk on your device. 
2) The application has a launcher ICON.
3) Upon starting the application, click the Login to Twitter button.
4) Device's browser will pop up with Twitter page asking for login.
5) After successful login, grant permission to this application in the browser.
6) You will be redirected to the application which will proceed to fetch the trending tweets, parse it and display it.





Problem Description:

Implement a class that asynchronously downloads the trending tweets on twitter and passes the results back via interface.
Restrictions
	•	No more than three requests should ever execute in parallel, regardless of how many times the class has been instantiated or the retrieval method invoked
	•	All of the requests should execute eventually (bonus points for providing a mechanism to cancel one or more outstanding requests)
	•	Code should be executable both from UI-bound (Activities) and non-UI (Services) classes
	•	The use of libraries that are geared specifically for Twitter API, such as Twitter4J is not allowed. Basically think of the problem as if it were a generic REST service with OAuth2 authentication mechanism

Recommendations
It is recommended that some sort of OAuth library (just not Twitter-specific) be used for proper handling of the authentication flow.
Output
At a minimum the source code and an explanation of design, decisions made and choices considered (where applicable) should be provided. Ideally a functioning APK should be provided as well. Bonus given for automated tests.


Design Consideration and Class Description:

/**
 * @author chetanambekar
 * File: NetworkService.java
 * Description: NetworkService is the main background service which 
 * provides the following APIS:
 * API: Clients must call this API to retrive Request Token from the server. 
 * On success/failure Intent INTENT_REQUEST_TOKENS_RETRIEVED
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
 * and RetriveTask -> to retrieveAccessToken.
 * The FetchStream Operation is performed in the a seperate Runnable(FetchStreamRunnable)
 * and executed using a ExecutorService. Each time a request is made to get the new stream 
 * the runnable of type FetchStreamRunnable is created and submitted to ExecutorService. 
 * In return a Future is obtained from the ExecutorService. This future (Value) along with 
 * hashCode of the Thread (key) is stored in the HastTable activeOrPendingStreams.
 * The Key is removed from the HashTable if:
 * 1) The stream is successfully downloaded and job is completed
 * 2) A stream is cancelled using the cancelStream(int) API.
 *
 * The Service also sends out the following intents to update clients on it's progress.
 * Const.INTENT_REQUEST_TOKENS_RETRIEVED -> Once retriveRequestToken is performed and 
 * result is store as String Extra on the intent.
 * Const.INTENT_RETRIVE_USER_CREDENTIALS -> Once retrieveAccessToken is performed and 
 * result is store as String Extra on the intent.
 *
 */


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


Solution :

1) No more than three requests should ever execute in parallel, regardless of how many times the class has been instantiated or the retrieval method invoked
2) All of the requests should execute eventually (bonus points for providing a mechanism to cancel one or more outstanding requests)
1 & 2 Achieved using :
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
 * The FetchStream Operation is performed in the a seperate Runnable(FetchStreamRunnable)
 * and executed using a ExecutorService. Each time a request is made to get the new stream 
 * the runnable of type FetchStreamRunnable is created and submitted to ExecutorService. 
 * In return a Future is obtained from the ExecutorService. This future (Value) along with 
 * hashCode of the Thread (key) is stored in the HastTable activeOrPendingStreams.
 * The Key is removed from the HashTable if:
 * 1) The stream is successfully downloaded and job is completed
 * 2) A stream is cancelled using the cancelStream(int) API.

3) Code should be executable both from UI-bound (Activities) and non-UI (Services) classes

3 Achieved Using: 

NetworkService is an Android services which exposes INetworkService and IStreamUpdateListener. Since NetworkService is a Service both other Activities and Services can bind to it
to use the AIDL api's.

INetworkService.AIDL:

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

IStreamUpdateListener.AIDL:

package com.example.chetantweettest;


interface IStreamUpdateListener {

 //CB From Network Service: 
 //To be implemented by clients to receive Stream after successful download.
 void streamUpdate(String stream);

}

4) The use of libraries that are geared specifically for Twitter API, such as Twitter4J is not allowed. Basically think of the problem as if it were a generic REST service with OAuth2 authentication mechanism

4 Achieved using:
Libraries: signpost-commonshttp4-1.2.1.1.jar \
		    signpost-core-1.2.1.1.jar




