package com.example.headsup

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Surface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import org.json.JSONObject
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONArray
import java.net.URL

class MainActivity : AppCompatActivity() {
    lateinit var llTop: LinearLayout
    lateinit var llMain: LinearLayout
    lateinit var llCelebrity: LinearLayout

    lateinit var tvTime: TextView
    lateinit var tvName: TextView
    lateinit var tvTaboo1: TextView
    lateinit var tvTaboo2: TextView
    lateinit var tvTaboo3: TextView
    lateinit var tvMain: TextView

    lateinit var btStart: Button

    lateinit var celebrities: ArrayList<JSONObject>


    var gameActive = false
    var celeb = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        llTop = findViewById(R.id.llTop)
        llMain = findViewById(R.id.llMain)
        llCelebrity = findViewById(R.id.llCelebrity)

        tvTime = findViewById(R.id.tvTime)
        tvName = findViewById(R.id.tvName)
        tvTaboo1 = findViewById(R.id.tvTaboo1)
        tvTaboo2 = findViewById(R.id.tvTaboo2)
        tvTaboo3 = findViewById(R.id.tvTaboo3)
        tvMain = findViewById(R.id.tvMain)

        btStart = findViewById(R.id.btStart)
        btStart.setOnClickListener {
            requestAPI()
        }
        celebrities = arrayListOf()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val rotation = windowManager.defaultDisplay.rotation
        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180){
            if(gameActive){
                celeb++
                newCelebrity(celeb)
                updateStatus(false)
            }else{
                updateStatus(false)
            }
        }else{
            if(gameActive){
                updateStatus(true)
            }else{
                updateStatus(false)
            }
        }
    }
    fun newCelebrity(id: Int){
        if(id < celebrities.size){
            tvName.text = celebrities[id].getString("name")
            tvTaboo1.text = celebrities[id].getString("taboo1")
            tvTaboo2.text = celebrities[id].getString("taboo2")
            tvTaboo3.text = celebrities[id].getString("taboo3")
        }
    }
    fun updateStatus(showCelebrity: Boolean){
        if(showCelebrity){
            llCelebrity.isVisible = true
            llMain.isVisible = false
        }else{
            llCelebrity.isVisible = false
            llMain.isVisible = true
        }
    }

    fun getCelebrities(): String{
        var response = ""
        try {
            response = URL("https://dojo-recipes.herokuapp.com/celebrities/")
                .readText(Charsets.UTF_8)
        }catch (e: Exception){
            println("Error: $e")
        }
        return response
    }

    fun newTimer(){
        if(!gameActive){
            gameActive = true
            tvMain.text = "PLEASE Rotate Device"
            btStart.isVisible = false
            val rotation = windowManager.defaultDisplay.rotation
            if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180){
                updateStatus(false)
            }else{
                updateStatus(true)
            }

            object: CountDownTimer(60000, 1000){
                override fun onTick(millisUntilFinished: Long){
                    tvTime.text = "Time: ${millisUntilFinished / 1000}"
                }

                override fun onFinish() {
                    gameActive = false
                    tvTime.text = "Time : --"
                    tvMain.text = "Heads Up!"
                    btStart.isVisible = true
                    updateStatus(false)
                }
            }.start()
        }
    }

    fun requestAPI(){
        CoroutineScope(Dispatchers.IO).launch{
            val data = async { getCelebrities() }.await()
            if(data.isNotEmpty()){
                withContext(Main){
                    parseJSON(data)
                    celebrities.shuffle()
                    newCelebrity(0)
                    newTimer()
                }
            }else{

            }
        }
    }
    suspend fun parseJSON(result: String){
        withContext(Dispatchers.Main){
            celebrities.clear()
            val jsonArray = JSONArray(result)
            for(i in 0 until jsonArray.length()){
                celebrities.add(jsonArray.getJSONObject(i))
            }
        }
    }


}