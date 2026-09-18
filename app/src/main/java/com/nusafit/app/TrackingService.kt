package com.nusafit.app

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import android.location.Location
import kotlin.math.max

class TrackingService:Service(){
    private lateinit var fused:FusedLocationProviderClient
    private val points=mutableListOf<Point>(); private var last:Location?=null; private var distance=0.0; private var start=0L; private var profile=Profile(); private var name="Aktivitas"; private var type="Running"
    private val callback=object:LocationCallback(){override fun onLocationResult(r:LocationResult){r.locations.forEach{loc->val now=System.currentTimeMillis(); last?.let{distance += it.distanceTo(loc).toDouble()/1000.0};last=loc;points.add(Point(loc.latitude,loc.longitude,now));broadcast()}}}
    override fun onCreate(){super.onCreate();fused=LocationServices.getFusedLocationProviderClient(this);profile=Store.profile(this);createChannel()}
    override fun onStartCommand(i:Intent?,flags:Int,startId:Int):Int{when(i?.action){"START"->start(i.getStringExtra("name")? :"Aktivitas",i.getStringExtra("type")?:"Running");"STOP"->stop()};return START_STICKY}
    private fun start(n:String,t:String){name=n;type=t;start=System.currentTimeMillis();points.clear();distance=0.0;last=null;startForeground(7,notification());val req=LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,2000).setMinUpdateDistanceMeters(3f).build();try{fused.requestLocationUpdates(req,callback,mainLooper)}catch(_:SecurityException){stopSelf()}}
    private fun stop(){fused.removeLocationUpdates(callback);val end=System.currentTimeMillis();val hours=max(1L,end-start)/3600000.0;val speed=if(hours>0)distance/hours else 0.0;val met=when(type.lowercase()){"cycling"->8.0;"walking"->3.5;"hiking"->6.0;else->8.0};val kcal=met*profile.weightKg*hours;val fat=kcal*0.11/9.0;val a=ActivityRecord(java.util.UUID.randomUUID().toString(),name,type,start,end,distance,kcal,fat,points.toList(),emptyList());Store.addActivity(this,a);sendBroadcast(Intent("NUSAFIT_STOPPED").putExtra("id",a.id));stopForeground(STOP_FOREGROUND_REMOVE);stopSelf()}
    private fun broadcast(){sendBroadcast(Intent("NUSAFIT_TRACK").apply{putExtra("distance",distance);putExtra("duration",System.currentTimeMillis()-start);putExtra("lat",last?.latitude?:0.0);putExtra("lng",last?.longitude?:0.0);putExtra("points",points.size)})}
    private fun notification()=NotificationCompat.Builder(this,"tracking").setContentTitle("NusaFit • Tracking aktif").setContentText("Tekan STOP di aplikasi untuk mengakhiri sesi").setSmallIcon(android.R.drawable.ic_menu_mylocation).setOngoing(true).build()
    private fun createChannel(){getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("tracking","NusaFit Tracking",NotificationManager.IMPORTANCE_LOW))}
    override fun onBind(i:Intent?):IBinder?=null
}
