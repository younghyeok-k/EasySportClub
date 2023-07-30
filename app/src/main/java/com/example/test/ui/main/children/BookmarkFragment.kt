package com.example.test.ui.main.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.test.R
import com.example.test.databinding.FragmentBookmarkBinding

class BookmarkFragment : Fragment() {
    private lateinit var binding: FragmentBookmarkBinding
    private val TAG = BookmarkFragment::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bookmark, container, false)
        return binding.root
    }
}