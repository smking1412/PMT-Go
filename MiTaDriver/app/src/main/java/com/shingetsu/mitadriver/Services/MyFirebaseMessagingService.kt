package com.shingetsu.mitadriver.Services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shingetsu.mitadriver.Utils.Common
import com.shingetsu.mitadriver.Utils.UserUtils
import kotlin.random.Random

/**
 * Created by Phạm Minh Tân - Shin on 5/9/2021.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token : String) {
        super.onNewToken(token)
        if (FirebaseAuth.getInstance().currentUser!= null){
            UserUtils.updateToken(this,token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        if  (data!= null){
            Common.showNotificaion(this, Random.nextInt(), data[Common.NOTI_TITLE], data[Common.NOTI_BODY], null)
        }
    }
}