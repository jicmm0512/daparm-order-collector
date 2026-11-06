package kr.co.daparm.collector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.graphics.Typeface
import android.widget.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity:Activity(){
 private lateinit var output:TextView
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,32,24,16)}
  val heading=TextView(this).apply{text="다팜몰 알림수집 v0.4 (테스트)";textSize=21f;setTypeface(null,Typeface.BOLD)}
  root.addView(heading)
  val note=TextView(this).apply{text="카카오톡 알림 중 정확히 지정한 방 이름이 확인된 메시지만 저장합니다. 자동 주문 확정은 하지 않습니다. 알림 형식에 따라 수집이 되지 않을 수 있습니다.\n";textSize=14f}
  root.addView(note)
  val field=EditText(this).apply{hint="오픈채팅방 이름을 정확히 입력";setSingleLine(true);setText(Store.room(this@MainActivity))}
  root.addView(field)
  fun btn(label:String, action:()->Unit){root.addView(Button(this).apply{text=label;setOnClickListener{action()}})}
  btn("수집할 방 이름 저장"){Store.setRoom(this,field.text.toString());Toast.makeText(this,"방 이름 저장됨",Toast.LENGTH_SHORT).show();refresh()}
  btn("알림 접근 권한 설정 열기"){startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))}
  btn("수집 내역 새로고침"){refresh()}
  btn("수집 내역 JSON 공유/저장"){val a=Store.all(this).toString(2);val intent=Intent(Intent.ACTION_SEND).apply{type="application/json";putExtra(Intent.EXTRA_TEXT,a)};startActivity(Intent.createChooser(intent,"수집 내역 내보내기"))}
  btn("수집 내역 CSV 공유/저장"){val arr=Store.all(this);fun esc(v:String)=34.toChar().toString()+v.replace(34.toChar().toString(),34.toChar().toString().repeat(2))+34.toChar();val lines=mutableListOf("시간,방,회원,메시지,상태")
   for(i in 0 until arr.length()){val x=arr.getJSONObject(i);lines.add(listOf(x.optString("timestamp"),x.optString("room"),x.optString("sender"),x.optString("message"),x.optString("status")).joinToString(","){esc(it)})}
   val intent=Intent(Intent.ACTION_SEND).apply{type="text/csv";putExtra(Intent.EXTRA_TEXT,"\uFEFF"+lines.joinToString("\r\n"))};startActivity(Intent.createChooser(intent,"CSV 내용 공유"))}
  btn("수집 기록 전체 삭제"){android.app.AlertDialog.Builder(this).setTitle("기록 삭제").setMessage("수집된 모든 메시지를 삭제할까요?").setNegativeButton("취소",null).setPositiveButton("삭제"){_,_->Store.clear(this);refresh()}.show()}
  output=TextView(this).apply{textSize=14f;setPadding(0,16,0,0)}
  val scroll=ScrollView(this);scroll.addView(output);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));setContentView(root);refresh()
 }
 override fun onResume(){super.onResume();if(::output.isInitialized)refresh()}
 private fun refresh(){val arr=Store.all(this);val fmt=SimpleDateFormat("MM-dd HH:mm:ss",Locale.KOREA);val sb=StringBuilder("수집된 알림: ${arr.length()}건\n수집 대상: ${Store.room(this).ifBlank{"미설정"}}\n\n")
  for(i in arr.length()-1 downTo maxOf(0,arr.length()-100)){val x=arr.optJSONObject(i)?:continue;sb.append(fmt.format(Date(x.optLong("timestamp")))).append("  ").append(x.optString("sender")).append("\n").append(x.optString("message")).append("\n\n")}
  output.text=sb.toString()
 }
}
