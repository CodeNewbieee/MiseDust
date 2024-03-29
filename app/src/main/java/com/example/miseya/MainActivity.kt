package com.example.miseya

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.miseya.databinding.ActivityMainBinding
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.skydoves.powerspinner.IconSpinnerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var items = mutableListOf<DustItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            spinnerViewSido.setOnSpinnerItemSelectedListener<String>{  _, _, _, text ->
                communicateNetWork(setUpDustParameter(text))
            }

         spinnerViewGoo.setOnSpinnerItemSelectedListener<String> { _, _, _, text ->
             Log.d("miseya", "selectedItem: spinnerViewGoo selected >  $text")
             var selectedItem = items.filter { f -> f.stationName == text }
             Log.d("miseya", "selectedItem: sidoName > " + selectedItem[0].sidoName)
             Log.d("miseya", "selectedItem: stationName > " + selectedItem[0].stationName)
             Log.d("miseya", "selectedItem: pm10Value > " + selectedItem[0].pm10Value)

             tvCityname.text = selectedItem[0].sidoName + " " + selectedItem[0].stationName
             tvDate.text = selectedItem[0].dataTime
             tvP10value.text = selectedItem[0].pm10Value + " ㎍/㎥"

             when(getGrade(selectedItem[0].pm10Value)) {
                 1 -> {
                     mainBg.setBackgroundColor(Color.parseColor("#9ED2EC"))
                     ivFace.setImageResource(R.drawable.ic_mise1)
                     tvP10grade.text = "좋음"
                 }
                 2 -> {
                     mainBg.setBackgroundColor(Color.parseColor("#D6A478"))
                     ivFace.setImageResource(R.drawable.ic_mise2)
                     tvP10grade.text = "보통"
                 }
                 3 -> {
                     mainBg.setBackgroundColor(Color.parseColor("#DF7766"))
                     ivFace.setImageResource(R.drawable.ic_mise3)
                     tvP10grade.text = "나쁨"
                 }
                 4 -> {
                     mainBg.setBackgroundColor(Color.parseColor("#BB3320"))
                     ivFace.setImageResource(R.drawable.ic_mise4)
                     tvP10grade.text = "매우나쁨"
                 }
             }
         }
        }
    }

    // 코루틴
    private fun communicateNetWork(param: HashMap<String,String>)= lifecycleScope.launch() {
        val responseData = NetWorkClient.dustNetWork.getDust(param)
        Log.d("Parsing Dust ::", responseData.toString())

        val adapter = IconSpinnerAdapter(binding.spinnerViewGoo)
        items = responseData.response.dustBody.dustItem!!

        val goo = ArrayList<String>()
        items.forEach {
            Log.d("add Item :", it.stationName)
            goo.add(it.stationName)
        }

        runOnUiThread {
            binding.spinnerViewGoo.setItems(goo)
        }

    }

    private fun setUpDustParameter(sido:String) : HashMap<String,String> {
        val authKey =
            "s+8vQZwKsq3In9+YslrrQFNZ05GLafKDkhYK9oD9ttlMlY09Jof7XyjzxIoKVE59LADLk4Hc0oFp5zc8loyhBA=="

        return hashMapOf(
            "serviceKey" to authKey,
            "returnType" to "json",
            "numOfRows" to "100",
            "pageNo" to "1",
            "sidoName" to sido,
            "ver" to "1.0"
        )
    }

    fun getGrade (value : String) : Int {
        val mValue = value.toInt()
        var grade = when(mValue) {
            in 0..30 ->  1
            in 31..80 -> 2
            in 81..100 -> 3
            else -> 4
        }
        return grade
    }

}