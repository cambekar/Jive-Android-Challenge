/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/chetanambekar/Documents/workspace/ChetanTweetTest/src/com/example/chetantweettest/INetworkService.aidl
 */
package com.example.chetantweettest;
public interface INetworkService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.chetantweettest.INetworkService
{
private static final java.lang.String DESCRIPTOR = "com.example.chetantweettest.INetworkService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.chetantweettest.INetworkService interface,
 * generating a proxy if needed.
 */
public static com.example.chetantweettest.INetworkService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.chetantweettest.INetworkService))) {
return ((com.example.chetantweettest.INetworkService)iin);
}
return new com.example.chetantweettest.INetworkService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_login:
{
data.enforceInterface(DESCRIPTOR);
this.login();
reply.writeNoException();
return true;
}
case TRANSACTION_retriveUserCredentials:
{
data.enforceInterface(DESCRIPTOR);
android.net.Uri _arg0;
if ((0!=data.readInt())) {
_arg0 = android.net.Uri.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.retriveUserCredentials(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_fetchStream:
{
data.enforceInterface(DESCRIPTOR);
com.example.chetantweettest.IStreamUpdateListener _arg0;
_arg0 = com.example.chetantweettest.IStreamUpdateListener.Stub.asInterface(data.readStrongBinder());
int _result = this.fetchStream(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_cancelStream:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
boolean _result = this.cancelStream(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.example.chetantweettest.INetworkService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
//API: Clients must call this API to retrive Request Token from the server.

@Override public void login() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_login, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//API: Clients must call this API to retrive user credentials before
//attempting to fetch streams. On success/failure Intent INTENT_RETRIVE_USER_CREDENTIALS
//will be sent out with String Extra "result" set to "true"/"false".

@Override public void retriveUserCredentials(android.net.Uri uri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((uri!=null)) {
_data.writeInt(1);
uri.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_retriveUserCredentials, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//API: To start fetching trending tweets

@Override public int fetchStream(com.example.chetantweettest.IStreamUpdateListener s) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((s!=null))?(s.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_fetchStream, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//API: To cancel a previously requested Stream. Stream either queued due to 
//max downloads (3) in progress or currently downloading both cases will be
//cancelled.

@Override public boolean cancelStream(int StreamID) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(StreamID);
mRemote.transact(Stub.TRANSACTION_cancelStream, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_login = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_retriveUserCredentials = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_fetchStream = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_cancelStream = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
//API: Clients must call this API to retrive Request Token from the server.

public void login() throws android.os.RemoteException;
//API: Clients must call this API to retrive user credentials before
//attempting to fetch streams. On success/failure Intent INTENT_RETRIVE_USER_CREDENTIALS
//will be sent out with String Extra "result" set to "true"/"false".

public void retriveUserCredentials(android.net.Uri uri) throws android.os.RemoteException;
//API: To start fetching trending tweets

public int fetchStream(com.example.chetantweettest.IStreamUpdateListener s) throws android.os.RemoteException;
//API: To cancel a previously requested Stream. Stream either queued due to 
//max downloads (3) in progress or currently downloading both cases will be
//cancelled.

public boolean cancelStream(int StreamID) throws android.os.RemoteException;
}
