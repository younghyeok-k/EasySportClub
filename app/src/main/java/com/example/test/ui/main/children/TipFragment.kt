package com.example.test.ui.main.children

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.example.CenterResponse
import com.example.test.R
import com.example.test.adapter.SportListAdapter
import com.example.test.api.CenterApi
import com.example.test.api.RetrofitInstance
import com.example.test.databinding.FragmentTipBinding
import com.example.test.dialog.ReservationDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [TipFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TipFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private var _binding: FragmentTipBinding? = null
    private val binding get() = _binding!!
    lateinit var recyclerView: RecyclerView

    private val recyclerAdapter = SportListAdapter(itemClicked = {
        Log.d("test", "${it.name} ${it.price}")

        ReservationDialog.show(requireActivity().supportFragmentManager, it)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tip, container, false)

        recyclerView = binding.recyclerView


        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerviewadd()
        recyclerView.adapter?.notifyDataSetChanged()
        return binding.root
    }

    private fun recyclerviewadd() {
        RetrofitInstance.retrofit.create(CenterApi::class.java)
            .getall()
            .enqueue(object : Callback<CenterResponse> {
                override fun onResponse(
                    call: Call<CenterResponse>,
                    response: Response<CenterResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d("MainActivity", "success");
                        response.body()?.let { dto ->
                            recyclerAdapter.submitList(dto.content) // 새 리스트로 갱신
                            Log.d("retrofit", "통신 성공")
                            binding.bottomSheetTitleTextView!!.text = "${dto.content.size}개의 시설"
                        }

                    }
                }

                override fun onFailure(call: Call<CenterResponse>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }
}