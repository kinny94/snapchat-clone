package com.example.kinny.snapchat_clone;

/**
 * Created by kinny on 9/27/2017.
 */

public class Images {

    String imageUrl;
    String senderName;
    String recipientName;

    Images(){}

    Images(String url, String sender, String receiver){
        imageUrl = url;
        senderName = sender;
        recipientName = receiver;
    }
}
