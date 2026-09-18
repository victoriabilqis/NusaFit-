package com.nusafit.app

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseSync {
    fun available(): Boolean = FirebaseApp.getApps(AppContext.ctx).isNotEmpty()
    fun syncActivity(c: Context, a: ActivityRecord, media: List<String> = emptyList(), done: (Boolean)->Unit = {}) {
        if (!available()) { done(false); return }
        val auth = FirebaseAuth.getInstance(); val work: (String)->Unit = { uid ->
            val data = hashMapOf("name" to a.name, "type" to a.type, "start" to a.start, "end" to a.end, "distanceKm" to a.distanceKm, "calories" to a.calories, "fatG" to a.fatG,
                "route" to a.points.map { mapOf("lat" to it.lat, "lng" to it.lng, "time" to it.time) })
            FirebaseFirestore.getInstance().collection("users").document(uid).collection("activities").document(a.id).set(data).addOnSuccessListener {
                if (media.isEmpty()) { done(true); return@addOnSuccessListener }
                val storage=FirebaseStorage.getInstance(); var left=media.size; var ok=true
                media.forEachIndexed { idx, raw -> val ref=storage.reference.child("users/$uid/activities/${a.id}/media_$idx");ref.putFile(Uri.parse(raw)).addOnFailureListener{ok=false}.addOnCompleteListener{left--;if(left==0)done(ok)} }
            }.addOnFailureListener{done(false)}
        }
        if (auth.currentUser != null) work(auth.currentUser!!.uid) else auth.signInAnonymously().addOnSuccessListener{work(it.user!!.uid)}.addOnFailureListener{done(false)}
    }
}
object AppContext { lateinit var ctx: Context }
