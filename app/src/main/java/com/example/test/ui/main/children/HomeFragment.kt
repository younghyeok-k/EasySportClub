package com.example.test.ui.main.children

import android.R
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.map
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.test.adapter.SportViewPagerAdapter
import com.example.test.databinding.FragmentHomeBinding
import com.example.test.dialog.ReservationDialog
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons


private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

class HomeFragment : Fragment(), OnMapReadyCallback, Overlay.OnClickListener {
    private val viewModel by viewModels<HomeViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var _naverMap: NaverMap? = null
    private val naverMap get() = _naverMap!!

    private lateinit var locationSource: FusedLocationSource

    private val viewPagerAdapter = SportViewPagerAdapter(itemClicked = {
        ReservationDialog.show(requireActivity().supportFragmentManager, it)
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding?.mapView?.onDestroy()
        _binding = null
        _naverMap = null

        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationSource = FusedLocationSource(this@HomeFragment, LOCATION_PERMISSION_REQUEST_CODE)

        with(binding) {
            mapView.getMapAsync(this@HomeFragment)

            with(viewPager) {
                adapter = viewPagerAdapter
                offscreenPageLimit = 3
                setPageTransformer(MarginPageTransformer((resources.displayMetrics.density * 16).toInt()))
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        if (_naverMap == null) return

                        val selectedHouseModel = viewPagerAdapter.currentList[position]
                        val cameraUpdate =
                            selectedHouseModel.lat?.let {
                                selectedHouseModel.lnt?.let { it1 ->
                                    LatLng(
                                        it,
                                        it1
                                    )
                                }
                            }?.let {
                                CameraUpdate.scrollTo(it)
                                    .animate(CameraAnimation.Easing)
                            }




                        if (cameraUpdate != null) {
                            naverMap.moveCamera(cameraUpdate)



                        }
                    }
                })
            }

            (searchField.editText as MaterialAutoCompleteTextView).run {
                setOnItemClickListener { _, _, position, _ ->
                    hideKeyboard()

                    val name = (adapter as ArrayAdapter<String>).getItem(position)
                        ?: return@setOnItemClickListener

                    val index = viewPagerAdapter.currentList.indexOfFirst { it.name == name }
                    if (index >= 0) {
                        viewPager.setCurrentItem(index, false)
                    }
                }
            }

            searchField.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                    return@OnEditorActionListener true
                }

                false
            })
        }

        viewModel.contents.observe(viewLifecycleOwner) {
            viewPagerAdapter.submitList(it)

            (binding.searchField.editText as MaterialAutoCompleteTextView).setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.select_dialog_item,
                    it.mapNotNull { it.name }
                )
            )
        }

        viewModel.markers.observe(viewLifecycleOwner) {
            it.forEach {
                it.map = _naverMap
                it.onClickListener = this
            }
        }
    }


    override fun onMapReady(map: NaverMap) {
        with(map) {
            _naverMap = this
            binding.currentLocationButton.map = this

            maxZoom = 18.0
            minZoom = 10.0
            moveCamera(
                CameraUpdate.toCameraPosition(
                    CameraPosition(
                        LatLng(35.180277, 128.091565), // 대상 지점
                        16.0
                    )
                )
            )

            locationSource = this@HomeFragment.locationSource
            locationTrackingMode = LocationTrackingMode.NoFollow

            viewModel.markers.value?.let {
                it.forEach {
                    it.map = this
                    it.onClickListener = this@HomeFragment
                }
            }
        }
    }

    // Marker click listener
    override fun onClick(overly: Overlay): Boolean {
        val selectedModel = viewPagerAdapter.currentList.firstOrNull {
            it.centerId == overly.tag
        }

        selectedModel?.let {
            val position = viewPagerAdapter.currentList.indexOf(it)
            if (position >= 0) {
                binding.viewPager.currentItem = position
            }
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }

            return
        }
    }

    private fun hideKeyboard() {
        with(binding.searchField.editText!!) {
            clearFocus()
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding?.mapView?.onDestroy()
        locationSource.deactivate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}