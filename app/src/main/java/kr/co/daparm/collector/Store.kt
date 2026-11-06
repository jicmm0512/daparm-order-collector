package kr.co.daparm.collector

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

object Store {
 private const val PREF="daparm_capture"
 fun room(ctx: Context)=ctx.getSharedPreferences(PREF,0).getString("room", "") ?: ""
 fun setRoom(ctx: Context, s:String)=ctx.getSharedPreferences(PREF,0).edit().putString("room",s.trim()).apply()
 @Synchronized fun all(ctx:Context): JSONArray = try { JSONArray(ctx.getSharedPreferences(PREF,0).getString("records","[]")) } catch(_:Exception){ JSONArray() }
 @Synchronized fun add(ctx:Context, sender:String, message:String, room:String, timestamp:Long, key:String): Boolean {
  val arr=all(ctx)
  val fingerprint=sha("$room|$sender|$message|${timestamp/10000}|$key")
  for(i in 0 until arr.length()) if(arr.optJSONObject(i)?.optString("fingerprint")==fingerprint) return false
  val row=JSONObject().put("room",room).put("sender",sender).put("message",message).put("timestamp",timestamp).put("source","android_notification").put("status","review").put("fingerprint",fingerprint)
  arr.put(row)
  ctx.getSharedPreferences(PREF,0).edit().putString("records",arr.toString()).apply()
  return true
 }
 @Synchronized fun clear(ctx:Context){ctx.getSharedPreferences(PREF,0).edit().remove("records").apply()}
 private fun sha(s:String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString(""){"%02x".format(it)}
}
