package com.example.test.ui.main.children

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.test.databinding.FragmentPostBinding
import com.example.test.databinding.ItemPostBinding
import com.example.test.model.post.Post
import com.example.test.ui.post.PostEditorActivity
import com.example.test.ui.post.PostViewerActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PostFragment : Fragment() {
    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<PostViewModel>()
    private val adapter = PostAdapter()

    private val launchEditor =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    viewModel.refresh()
                }
            }
        }

    private val launchViewer =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val old: Post? = result.data?.getParcelableExtra("old")
                val new: Post? = result.data?.getParcelableExtra("new")

                if (old == null) return@registerForActivityResult

                val posts = ArrayList(viewModel.posts.value)
                val index = posts.indexOfFirst { it.id == old.id }
                if (index >= 0) {
                    if (new != null) {
                        posts[index] = new;
                    } else {
                        posts.removeAt(index)
                    }

                    viewModel.posts.tryEmit(posts)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    root.context,
                    LinearLayoutManager.VERTICAL
                )
            )

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
                        lifecycleScope.launch {
                            viewModel.loadMore()
                        }
                    }
                }
            })

            recyclerView.adapter = adapter.apply {
                onItemClickListener = {
                    launchViewer.launch(PostViewerActivity.getIntent(view.context, it))
                }
            }

            searchField.editText?.addTextChangedListener {
                viewModel.setQuery(it?.toString() ?: "")
            }

            searchField.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                    return@OnEditorActionListener true
                }

                false
            })

            refreshLayout.setOnRefreshListener {
                lifecycleScope.launch {
                    viewModel.refresh()
                }
            }

            editButton.setOnClickListener {
                launchEditor.launch(PostEditorActivity.getIntent(it.context))
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest {
                binding.refreshLayout.isRefreshing = it
            }
        }

        lifecycleScope.launch {
            viewModel.posts.collectLatest {
                adapter.submitList(it)
            }
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

    private class PostAdapter :
        ListAdapter<Post, PostAdapter.PostItemViewHolder>(object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.title == newItem.title &&
                        oldItem.content == newItem.content &&
                        oldItem.commentSize == newItem.commentSize &&
                        oldItem.user.username == newItem.user.username &&
                        oldItem.user.nickname == newItem.user.nickname

            }
        }) {
        var onItemClickListener: ((Post) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPostBinding.inflate(inflater, parent, false)
            return PostItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PostItemViewHolder, position: Int) {
            val post = currentList[position]

            with(holder.binding) {
                root.setOnClickListener {
                    onItemClickListener?.invoke(post)
                }

                titleTextView.text = post.title
                contentTextView.text = post.content
                commentCountTextView.text = "${post.commentSize}"
Log.d("사이즈 ","${post.commentSize} 개")
                if (post.commentSize > 0) {
                    commentCountTextView.isVisible = true
                    verticalDivider1.isVisible = true
                } else {
                    commentCountTextView.isVisible = false
                    verticalDivider1.isVisible = false
                }

                if (post.createdAt != null) {
                    datetimeTextView.text = post.createdAt
                    verticalDivider2.isVisible = true
                } else {
                    datetimeTextView.text = null
                    verticalDivider2.isVisible = false
                }

                nameTextView.text = post.user.nickname
            }
        }

        class PostItemViewHolder(val binding: ItemPostBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}