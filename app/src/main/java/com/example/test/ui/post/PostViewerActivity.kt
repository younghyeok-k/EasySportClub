package com.example.test.ui.post

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.application.SharedManager
import com.example.test.databinding.ActivityPostViewerBinding
import com.example.test.databinding.DialogCommentEditorBinding
import com.example.test.databinding.ItemCommentInPostViewerBinding
import com.example.test.databinding.ItemPostInPostViewerBinding
import com.example.test.model.post.Comment
import com.example.test.model.post.Post
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PostViewerActivity : AppCompatActivity() {
    companion object {
        private const val ARG_POST = "ARG_POST"

        fun getIntent(context: Context, post: Post): Intent {
            return Intent(context, PostViewerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(ARG_POST, post)
            }
        }
    }

    private var _post: Post? = null
    private val post get() = _post!!

    private val binding by lazy { ActivityPostViewerBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PostViewerViewModel>()

    private val postAdapter = PostAdapter()
    private val commentAdapter = CommentAdapter()

    private val launchEditor =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    viewModel.refresh()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _post = intent.getParcelableExtra(ARG_POST)

        if (savedInstanceState != null) {
            _post = savedInstanceState.getParcelable(ARG_POST)
        }

        if (_post == null) {
            finish()
            return
        }

        setContentView(binding.root)

        initUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ARG_POST, post)
        super.onSaveInstanceState(outState)
    }

    private fun initUi() = with(binding) {
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val isMyPost = post.user.username == SharedManager.getCurrentUser()?.username
        if (isMyPost) {
            toolbar.inflateMenu(R.menu.post_menu)
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.action_edit) {
                    launchEditor.launch(PostEditorActivity.getIntent(root.context, post))
                } else {
                    showRemoveDialog(post)
                }

                return@setOnMenuItemClickListener true
            }
        }

        commentAdapter.onEditButtonClicked = {
            showCommentEditDialog(it)
        }

        commentAdapter.onRemoveButtonClicked = {
            showRemoveDialog(it)
        }

        val adapter = ConcatAdapter(postAdapter, commentAdapter)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                root.context,
                LinearLayoutManager.VERTICAL
            )
        )

        textField.editText?.addTextChangedListener {
            sendButton.isEnabled = it?.toString()?.isNotBlank() ?: false
        }

        sendButton.setOnClickListener { v ->
            lifecycleScope.launch {
                send()
            }
        }

        viewModel.fetch(post)

        lifecycleScope.launch {
            viewModel.post.collectLatest {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra("old", this@PostViewerActivity.post)
                    putExtra("new", it)
                })

                if (it == null) {
                    Toast.makeText(
                        this@PostViewerActivity,
                        if (isMyPost) "삭제되었습니다." else "글 작성자에 의해 삭제되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                } else {
                    postAdapter.submitList(listOf(it))
                }
            }
        }

        lifecycleScope.launch {
            viewModel.comments.collectLatest {
                commentAdapter.submitList(it)
            }
        }
    }

    private fun hideKeyboard() {
        currentFocus?.let {
            with(it) {
                clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(windowToken, 0)
            }
        }
    }

    private fun showCommentEditDialog(comment: Comment) {
        val dialogBinding = DialogCommentEditorBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        with(dialogBinding) {
            textField.editText?.addTextChangedListener {
                positiveButton.isEnabled = it?.toString()?.isNotBlank() ?: false
            }

            textField.editText!!.setText("")
            textField.editText!!.append(comment.content)

            positiveButton.setOnClickListener {
                dialog.dismiss()

                lifecycleScope.launch {
                    val message = textField.editText!!.text.toString().trim()
                    viewModel.updateComment(comment.id, message)
                }
            }

            negativeButton.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showRemoveDialog(obj: Any) {
        AlertDialog.Builder(binding.root.context)
            .setIcon(R.drawable.baseline_delete_24)
            .setMessage("해당 ${if (obj is Post) "글" else "댓글"}을 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                lifecycleScope.launch {
                    if (obj is Post) {
                        viewModel.deletePost()
                    } else if (obj is Comment) {
                        viewModel.deleteComment(obj.id)
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private suspend fun send() {
        hideKeyboard()

        val message = binding.textField.editText!!.text.toString().trim()
        viewModel.createComment(message)

        binding.textField.editText!!.setText("")
    }


    class PostAdapter : ListAdapter<Post, PostAdapter.PostItemViewHolder>(object :
        DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return true
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.title == newItem.title &&
                    oldItem.content == newItem.content &&
                    oldItem.commentSize == newItem.commentSize &&
                    oldItem.user.username == newItem.user.username
        }
    }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPostInPostViewerBinding.inflate(inflater, parent, false)
            return PostItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PostItemViewHolder, position: Int) {
            val post = currentList[position]

            with(holder.binding) {
                nameTextView.text = post.user.username
                datetimeTextView.text = post.createdAt
                datetimeTextView.isVisible = datetimeTextView.text.toString().isNotBlank()

                titleTextView.text = post.title
                contentTextView.text = post.content
                commentCountTextView.text = "${post.commentSize}"
            }
        }

        class PostItemViewHolder(val binding: ItemPostInPostViewerBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentItemViewHolder>(object :
        DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return true
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.content == newItem.content &&
                    oldItem.user.username == newItem.user.username
        }
    }) {
        var onEditButtonClicked: ((Comment) -> Unit)? = null
        var onRemoveButtonClicked: ((Comment) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemCommentInPostViewerBinding.inflate(inflater, parent, false)
            return CommentItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CommentItemViewHolder, position: Int) {
            val comment = currentList[position]

            with(holder.binding) {
                nameTextView.text = comment.user.username
                datetimeTextView.text = comment.createdAt
                datetimeTextView.isVisible = datetimeTextView.text.toString().isNotBlank()

                contentTextView.text = comment.content

                val isMyComment = comment.user.username == SharedManager.getCurrentUser()?.username
                if (isMyComment) {
                    overflowButton.isInvisible = false
                    overflowButton.setOnClickListener {
                        PopupMenu(it.context, it).apply {
                            inflate(R.menu.post_menu)
                            setOnMenuItemClickListener {
                                if (it.itemId == R.id.action_edit) {
                                    onEditButtonClicked?.invoke(comment)
                                } else if (it.itemId == R.id.action_remove) {
                                    onRemoveButtonClicked?.invoke(comment)
                                }

                                return@setOnMenuItemClickListener true
                            }
                        }.show()
                    }
                } else {
                    overflowButton.isInvisible = true
                    overflowButton.setOnClickListener(null)
                }
            }
        }

        class CommentItemViewHolder(val binding: ItemCommentInPostViewerBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}