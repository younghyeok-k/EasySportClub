package com.example.test.ui.main.children

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.example.CenterResponse
import com.example.example.Content
import com.example.test.api.CenterApi
import com.example.test.api.RetrofitInstance
import com.example.test.application.SharedManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections

class HomeViewModel : ViewModel() {
    private val apiService = RetrofitInstance.retrofit.create(CenterApi::class.java)
    private val sharedManager = SharedManager.getInstance()

    private val _contents = MutableLiveData<List<Content>>()
    val contents: LiveData<List<Content>> get() = _contents

    val _markers = MutableLiveData<List<Marker>>()
    val markers: LiveData<List<Marker>> get() = _markers

    init {
        apiService.getall().enqueue(object : Callback<CenterResponse> {
            override fun onResponse(
                call: Call<CenterResponse>,
                response: Response<CenterResponse>
            ) {
                val result = response.body()?.content ?: Collections.emptyList()

                _contents.postValue(result)

                markers.value?.forEach {
                    it.map = null
                    it.onClickListener = null
                }

                _markers.postValue(result.map {
                    Marker().apply {
                        position = LatLng(it.lat!!, it.lnt!!)
                        tag = it.centerId
                        icon = MarkerIcons.BLACK
                        iconTintColor = Color.GREEN
                        width = 50
                        height = 80
                    }
                })
            }

            override fun onFailure(call: Call<CenterResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })

    }

}