package kr.co.daparm.collector

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

private fun isOrder(text: String): Boolean {
    val t = text.trim()
    if (t.contains("?") || t.contains("？")) return false
    return Regex("""[가-힣A-Za-z][가-힣A-Za-z0-9\s]*?\s*\d+\s*(개|봉|박스|팩|세트|통|병|kg|주문|$)""").containsMatchIn(t)
}

class KakaoListener: NotificationListenerService() {
 override fun onNotificationPosted(sbn:StatusBarNotification?) {
  if(sbn==null || sbn.packageName!="com.kakao.talk") return
  android.util.Log.i(
    "DaparmDiag",
    "notification received: package=" + sbn.packageName +
    ", configuredRoom=" + Store.room(this).isNotBlank()
)

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
    if(sender.isNotBlank() && body.isNotBlank()) if(room == "다팜몰 평택점 공동구매" && isOrder(body)) Store.add(this,sender,body,room,m.timestamp,sbn.key)
   }
  } else {
   val body=e.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
   if(title.isNotBlank() && body.isNotBlank() && title!=room) if(room == "다팜몰 평택점 공동구매" && isOrder(body)) Store.add(this,title,body,room,sbn.postTime,sbn.key)
  }
 }
}
