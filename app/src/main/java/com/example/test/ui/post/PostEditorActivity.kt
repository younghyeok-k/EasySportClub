package com.example.test.ui.post

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.test.databinding.ActivityPostEditorBinding
import com.example.test.model.post.Post
import kotlinx.coroutines.launch

class PostEditorActivity : AppCompatActivity() {
    companion object {
        private const val ARG_POST = "ARG_POST"

        fun getIntent(context: Context, post: Post? = null): Intent {
            return Intent(context, PostEditorActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(ARG_POST, post)
            }
        }
    }

    private val binding by lazy { ActivityPostEditorBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<PostEditorViewModel>()

    private var post: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        post = intent.getParcelableExtra(ARG_POST)

        if (savedInstanceState != null) {
            post = savedInstanceState.getParcelable(ARG_POST)
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

        titleField.editText?.addTextChangedListener {
            doneButton.isEnabled = titleField.editText!!.text.toString().isNotBlank() &&
                    contentField.editText!!.text.toString().isNotBlank()
        }

        contentField.editText?.addTextChangedListener {
            doneButton.isEnabled = titleField.editText!!.text.toString().isNotBlank() &&
                    contentField.editText!!.text.toString().isNotBlank()
        }

        titleField.editText?.setText(post?.title)
        contentField.editText?.setText(post?.content)

        doneButton.setOnClickListener {
            hideKeyboard()
            createOrUpdate()
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

    private fun createOrUpdate() {
        lifecycleScope.launch {
            if (binding.progressView.isVisible) return@launch

            binding.progressView.isVisible = true

            val title = binding.titleField.editText!!.text.toString().trim()
            val content = binding.contentField.editText!!.text.toString().trim()

            try {
                val result = if (post == null) {
                    viewModel.create(title, content)
                } else {
                    viewModel.update(post!!.id, title, content)
                }

                if (result == "null") throw Exception()

                binding.progressView.isVisible = false

                setResult(Activity.RESULT_OK)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()

                binding.progressView.isVisible = false

                Toast.makeText(
                    this@PostEditorActivity,
                    "오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}