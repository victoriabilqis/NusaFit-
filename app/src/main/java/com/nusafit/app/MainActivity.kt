package com.nusafit.app

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class MainActivity:AppCompatActivity(),OnMapReadyCallback{
    private lateinit var map:GoogleMap; private var routeLine:Polyline?=null; private var tracking=false; private var sessionName="Aktivitas"; private val handler=Handler(Looper.getMainLooper());
    private lateinit var status:TextView;private lateinit var distance:TextView;private lateinit var duration:TextView;private lateinit var profileText:TextView;private lateinit var list:LinearLayout
    private val mediaPicker=registerForActivityResult(ActivityResultContracts.GetMultipleContents()){uris->if(uris.isNotEmpty()){getSharedPreferences("nusafit",0).edit().putString("pending_media",uris.joinToString("|"){it.toString()}).apply();toast("${uris.size} media dipilih untuk sesi berikutnya")}}
    private val perm=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)startTracking()else toast("Izin lokasi diperlukan untuk tracking")}
    override fun onCreate(b:Bundle?){super.onCreate(b);AppContext.ctx=applicationContext;setContentView(build());val frag=supportFragmentManager.findFragmentById(1001) as SupportMapFragment?;frag?.getMapAsync(this);ContextCompat.registerReceiver(this,trackReceiver,IntentFilter("NUSAFIT_TRACK"),ContextCompat.RECEIVER_NOT_EXPORTED);ContextCompat.registerReceiver(this,stopReceiver,IntentFilter("NUSAFIT_STOPPED"),ContextCompat.RECEIVER_NOT_EXPORTED);refresh();}
    override fun onDestroy(){unregisterReceiver(trackReceiver);unregisterReceiver(stopReceiver);super.onDestroy()}
    private fun build():View{val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(245,247,250))};val title=TextView(this).apply{text="NusaFit";textSize=26f;setTextColor(Color.WHITE);setPadding(20,18,20,18);setBackgroundColor(Color.rgb(21,101,192));setTypeface(null,1)};root.addView(title);val scroll=ScrollView(this);val col=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,16,16,16)};scroll.addView(col);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        profileText=TextView(this);col.addView(card("PROFIL & BERAT BADAN",profileText));val profBtn=Button(this).apply{text="Edit profil";setOnClickListener{profileDialog()}};col.addView(profBtn)
        status=TextView(this).apply{text="● Tidak tracking";textSize=18f};distance=TextView(this).apply{text="Jarak: 0,00 km"};duration=TextView(this).apply{text="Durasi: 00:00:00"};col.addView(status);col.addView(distance);col.addView(duration)
        val name=EditText(this).apply{hint="Nama aktivitas, mis. Lari Pagi"};col.addView(name);val type=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Running","Walking","Cycling","Hiking","Other"))};col.addView(type)
        val start=Button(this).apply{text="▶ MULAI OLAHRAGA";setOnClickListener{sessionName=name.text.toString().ifBlank{"Aktivitas"};requestLocation(type.selectedItem.toString())}};col.addView(start);val stop=Button(this).apply{text="■ STOP & SIMPAN";setOnClickListener{stopTracking()}};col.addView(stop)
        val media=Button(this).apply{text="📷 Pilih Foto/Video";setOnClickListener{mediaPicker.launch("image/*")}};col.addView(media)
        val mapFrag=SupportMapFragment.newInstance();supportFragmentManager.beginTransaction().replace(1001,mapFrag).commit();root.addView(FrameLayout(this).apply{id=1001},LinearLayout.LayoutParams(-1,450));
        val schedule=Button(this).apply{text="⏰ Buat Pengingat Olahraga";setOnClickListener{scheduleDialog()}};col.addView(schedule);val calendar=Button(this).apply{text="📅 Kalender & Riwayat";setOnClickListener{historyDialog()}};col.addView(calendar);val stats=Button(this).apply{text="📊 Statistik Mingguan/Bulanan/Tahunan";setOnClickListener{statsDialog()}};col.addView(stats);return root}
    private fun card(t:String,v:TextView)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(14,14,14,14);addView(TextView(this@MainActivity).apply{text=t;textSize=16f;setTypeface(null,1)});addView(v)}
    private fun refresh(){val p=Store.profile(this);val bmi=if(p.heightCm>0)p.weightKg/((p.heightCm/100)*(p.heightCm/100))else 0.0;val range=if(p.heightCm>0)"${String.format("%.1f",18.5*(p.heightCm/100)*(p.heightCm/100))}–${String.format("%.1f",24.9*(p.heightCm/100)*(p.heightCm/100))} kg" else "-";profileText.text="${p.gender.ifBlank{"-"}} • ${p.age} th • ${p.heightCm} cm • ${p.weightKg} kg\nBMI: ${String.format("%.1f",bmi)} • Rentang berbasis BMI 18,5–24,9: $range"}
    private fun profileDialog(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,20,20,20)};val g=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Laki-laki","Perempuan"))};val a=EditText(this).apply{hint="Usia (tahun)";inputType=2};val h=EditText(this).apply{hint="Tinggi (cm)";inputType=2 or 8192};val w=EditText(this).apply{hint="Berat (kg)";inputType=2 or 8192};box.addView(g);box.addView(a);box.addView(h);box.addView(w);AlertDialog.Builder(this).setTitle("Profil NusaFit").setView(box).setPositiveButton("Simpan"){_, _ ->Store.saveProfile(this,Profile(g.selectedItem.toString(),a.text.toString().toIntOrNull()?:0,h.text.toString().toDoubleOrNull()?:0.0,w.text.toString().toDoubleOrNull()?:0.0));refresh()}.setNegativeButton("Batal",null).show()}
    private fun requestLocation(t:String){val need=mutableListOf<String>();if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)need.add(Manifest.permission.ACCESS_FINE_LOCATION);if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)need.add(Manifest.permission.POST_NOTIFICATIONS);if(need.isNotEmpty()){getSharedPreferences("nusafit",0).edit().putString("pending_type",t).apply();perm.launch(need.toTypedArray())}else startTracking()}
    private fun startTracking(){val t=getSharedPreferences("nusafit",0).getString("pending_type","Running")!!;tracking=true;status.text="● TRACKING AKTIF • $sessionName";ContextCompat.startForegroundService(this,Intent(this,TrackingService::class.java).apply{action="START";putExtra("name",sessionName);putExtra("type",t)})}
    private fun stopTracking(){if(!tracking){toast("Belum ada sesi aktif");return};startService(Intent(this,TrackingService::class.java).setAction("STOP"));tracking=false;status.text="● Menyimpan sesi..."}
    private val trackReceiver=object:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){distance.text=String.format(Locale.US,"Jarak: %.2f km",i.getDoubleExtra("distance",0.0));duration.text="Durasi: "+fmt(i.getLongExtra("duration",0));val la=i.getDoubleExtra("lat",0.0);val lo=i.getDoubleExtra("lng",0.0);if(la!=0.0 && ::map.isInitialized){val pt=LatLng(la,lo);if(routeLine==null)routeLine=map.addPolyline(PolylineOptions().add(pt)) else routeLine!!.points=routeLine!!.points+pt;map.animateCamera(CameraUpdateFactory.newLatLngZoom(pt,16f))}}}
    private val stopReceiver=object:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){status.text="● Sesi tersimpan";val id=i.getStringExtra("id");val a=Store.activities(this@MainActivity).firstOrNull{it.id==id};if(a!=null){val raw=getSharedPreferences("nusafit",0).getString("pending_media","")!!.split("|").filter{it.isNotBlank()};Store.addMedia(this@MainActivity,a.id,raw);if(raw.isNotEmpty())FirebaseSync.syncActivity(this@MainActivity,a,raw) else FirebaseSync.syncActivity(this@MainActivity,a)};toast("Aktivitas tersimpan di HP");refresh()}}
    override fun onMapReady(g:GoogleMap){map=g;if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){map.isMyLocationEnabled=true}}
    private fun scheduleDialog(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,20,20,20)};val n=EditText(this).apply{hint="Nama aktivitas"};val time=TimePicker(this).apply{setIs24HourView(true)};box.addView(n);box.addView(time);AlertDialog.Builder(this).setTitle("Jadwal olahraga").setView(box).setPositiveButton("Pasang"){_, _ ->val now=Calendar.getInstance();now.set(Calendar.HOUR_OF_DAY,time.hour);now.set(Calendar.MINUTE,time.minute);now.set(Calendar.SECOND,0);if(now.timeInMillis<System.currentTimeMillis())now.add(Calendar.DAY_OF_YEAR,1);val id=(System.currentTimeMillis()%100000).toInt();val pi=PendingIntent.getBroadcast(this,id,Intent(this,ReminderReceiver::class.java).putExtra("id",id).putExtra("name",n.text.toString().ifBlank{"Olahraga"}),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,now.timeInMillis,pi);toast("Pengingat ${SimpleDateFormat("HH:mm",Locale.getDefault()).format(now.time)} dipasang")}.setNegativeButton("Batal",null).show()}
    private fun historyDialog(){
        val cal=CalendarView(this)
        val selected=TextView(this).apply{setPadding(20,12,20,12)}
        fun update(ms:Long){val d=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date(ms));val rows=Store.activities(this).filter{SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date(it.start))==d};selected.text=if(rows.isEmpty())"Tidak ada aktivitas pada $d" else rows.joinToString("\n\n"){a->"${a.name} • ${a.type}\n${String.format("%.2f",a.distanceKm)} km • ${fmt(a.end-a.start)} • ${a.calories.toInt()} kcal"}}
        update(System.currentTimeMillis());cal.setOnDateChangeListener{_,y,m,day->val c=Calendar.getInstance();c.set(y,m,day);update(c.timeInMillis)}
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(cal);addView(selected)}
        AlertDialog.Builder(this).setTitle("Kalender Aktivitas").setView(box).setPositiveButton("Tutup",null).show()
    }
    private fun statsDialog(){val s=Store.activities(this);val now=System.currentTimeMillis();val week=s.filter{now-it.start<=7*86400000L};val month=s.filter{now-it.start<=30*86400000L};val year=s.filter{now-it.start<=365*86400000L};fun line(x:List<ActivityRecord>)="${x.size} sesi • ${String.format("%.1f",x.sumOf{it.distanceKm})} km • ${x.sumOf{it.calories}.toInt()} kcal";val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,10,20,10);addView(StatsChartView(this@MainActivity,s))};box.addView(TextView(this).apply{text="7 hari: ${line(week)}\n30 hari: ${line(month)}\n365 hari: ${line(year)}";setPadding(0,18,0,0)});AlertDialog.Builder(this).setTitle("Statistik & Capaian").setView(box).setPositiveButton("Tutup",null).show()}
    private fun fmt(ms:Long):String{val s=max(0,ms/1000);return String.format(Locale.US,"%02d:%02d:%02d",s/3600,(s%3600)/60,s%60)}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
}
