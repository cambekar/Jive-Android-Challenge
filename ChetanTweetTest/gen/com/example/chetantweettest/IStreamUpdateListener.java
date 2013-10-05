/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/chetanambekar/Documents/workspace/ChetanTweetTest/src/com/example/chetantweettest/IStreamUpdateListener.aidl
 */
package com.example.chetantweettest;
public interface IStreamUpdateListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.chetantweettest.IStreamUpdateListener
{
private static final java.lang.String DESCRIPTOR = "com.example.chetantweettest.IStreamUpdateListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.chetantweettest.IStreamUpdateListener interface,
 * generating a proxy if needed.
 */
public static com.example.chetantweettest.IStreamUpdateListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.chetantweettest.IStreamUpdateListener))) {
return ((com.example.chetantweettest.IStreamUpdateListener)iin);
}
return new com.example.chetantweettest.IStreamUpdateListener.Stub.Proxy(obj);
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
case TRANSACTION_streamUpdate:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.streamUpdate(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.example.chetantweettest.IStreamUpdateListener
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
//CB From Network Service: 
//To be implemented by clients to receive Stream after successful download.

@Override public void streamUpdate(java.lang.String stream) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(stream);
mRemote.transact(Stub.TRANSACTION_streamUpdate, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_streamUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
//CB From Network Service: 
//To be implemented by clients to receive Stream after successful download.

public void streamUpdate(java.lang.String stream) throws android.os.RemoteException;
}
