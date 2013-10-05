package com.example.chetantweettest;


interface IStreamUpdateListener {

 //CB From Network Service: 
 //To be implemented by clients to receive Stream after successful download.
 void streamUpdate(String stream);

}