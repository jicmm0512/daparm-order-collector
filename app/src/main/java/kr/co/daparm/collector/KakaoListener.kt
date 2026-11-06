package kr.co.daparm.collector

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class KakaoListener: NotificationListenerService() {
 override fun onNotificationPosted(sbn:StatusBarNotification?) {
  if(sbn==null || sbn.packageName!="com.kakao.talk") return
  val room=Store.room(this)
  if(room.isBlank()) return // opt-in: no capture until exact room configured
  val e=sbn.notification.extras ?: return
  val title=e.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
  val sub=e.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim().orEmpty()
  val conversation=e.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()?.trim().orEmpty()
  // Exact room match only. Never assume sender title is a room.
  if(conversation!=room && sub!=room) return
  val messages=e.getParcelableArray(Notification.EXTRA_MESSAGES)
  if(!messages.isNullOrEmpty()) {
   val parsed=Notification.MessagingStyle.Message.getMessagesFromBundleArray(messages)
   for(m in parsed){
    val sender=m.senderPerson?.name?.toString()?.trim() ?: m.sender?.toString()?.trim() ?: title
    val body=m.text?.toString()?.trim().orEmpty()
    if(sender.isNotBlank() && body.isNotBlank()) Store.add(this,sender,body,room,m.timestamp,sbn.key)
   }
  } else {
   val body=e.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
   if(title.isNotBlank() && body.isNotBlank() && title!=room) Store.add(this,title,body,room,sbn.postTime,sbn.key)
  }
 }
}
