package com.example.test.ui.main


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.test.R
import com.example.test.application.SharedManager
import com.example.test.databinding.ActivityMainBinding
import com.example.test.databinding.NaviHeaderBinding
import com.example.test.ui.main.children.BookmarkFragment
import com.example.test.ui.main.children.HomeFragment
import com.example.test.ui.main.children.MyInfoFragment
import com.example.test.ui.main.children.PostFragment
import com.example.test.ui.main.children.TipFragment
import com.example.test.ui.splash.SplashActivity
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val sharedManager by lazy { return@lazy SharedManager.getInstance() }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.layoutDrawer.isOpen) {
                binding.layoutDrawer.closeDrawers()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(backPressedCallback)

        with(binding) {
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.action_menu) {
                    binding.layoutDrawer.openDrawer(GravityCompat.START)
                }

                return@setOnMenuItemClickListener true
            }

            viewPager.adapter =
                ViewPagerAdapter(supportFragmentManager, this@MainActivity.lifecycle)
            viewPager.isUserInputEnabled = false
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    val id = when (position) {
                        0 -> R.id.action_home
                        1 -> R.id.action_near
                        2 -> R.id.action_board
                        3 -> R.id.action_weather
                        else -> R.id.action_my_profile
                    }

                    if (bottomView.selectedItemId != id) {
                        bottomView.selectedItemId = id
                    }
                }
            })

            bottomView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.action_home -> viewPager.currentItem = 0
                    R.id.action_near -> viewPager.currentItem = 1
                    R.id.action_board -> viewPager.currentItem = 2
                    R.id.action_weather -> viewPager.currentItem = 3
                    else -> viewPager.currentItem = 4
                }

                return@setOnItemSelectedListener true
            }

            naviview.setNavigationItemSelectedListener(this@MainActivity)

            logout.setOnClickListener {
                sharedManager.clear()

                val intent = Intent(this@MainActivity, SplashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
                finish()
            }

            val navHeaderBinding = NaviHeaderBinding.bind(naviview.getHeaderView(0))
            navHeaderBinding.close.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        }

        binding.layoutDrawer.closeDrawers()
        return false
    }

    private class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount() = 5

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> TipFragment()
                2 -> PostFragment()
                3 -> BookmarkFragment()
                else -> MyInfoFragment()
            }
        }
    }
}
