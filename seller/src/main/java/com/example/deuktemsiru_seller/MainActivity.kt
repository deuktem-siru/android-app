package com.example.deuktemsiru_seller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.data.repository.SessionManager
import com.example.deuktemsiru_seller.data.repository.appSession
import com.example.deuktemsiru_seller.databinding.ActivityMainBinding
import com.example.deuktemsiru_seller.domain.model.OrderStatus
import com.example.deuktemsiru_seller.data.remote.Push
import com.example.deuktemsiru_seller.data.remote.RetrofitClient
import com.example.deuktemsiru_seller.presentation.view.auth.LoginActivity
import com.example.deuktemsiru_seller.presentation.view.home.HomeFragment
import com.example.deuktemsiru_seller.presentation.view.mypage.MyPageFragment
import com.example.deuktemsiru_seller.presentation.view.notification.NotificationFragment
import com.example.deuktemsiru_seller.presentation.view.order.OrderFragment
import com.example.deuktemsiru_seller.presentation.view.product.ProductFragment
import com.example.deuktemsiru_seller.presentation.view.sales.SalesFragment
import com.example.deuktemsiru_seller.util.LocalNotificationHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val screenModel by lazy { com.example.deuktemsiru_seller.AppGraph.model(this) }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private var orderBadgeCount = 0
    var pendingOrderTab: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.bottomNav.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        session = this.appSession
        LocalNotificationHelper.createChannel(this)

        // 로그인 상태 확인 — 미로그인 시 LoginActivity로 이동
        if (!session.isLoggedIn()) {
            navigateToLogin()
            return
        }

        RetrofitClient.onSessionExpired = {
            runOnUiThread {
                session.clear()
                navigateToLogin()
            }
        }
        enablePush()

        setupBottomNav()

        if (savedInstanceState == null) {
            if (intent.getBooleanExtra("navigate_to_product", false)) {
                binding.bottomNav.selectedItemId = R.id.nav_product
            } else {
                loadFragment(HomeFragment())
            }
        }
    }

    private fun enablePush() {
        Push.registerCurrentToken()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        if (session.isLoggedIn()) refreshOrderBadge()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("navigate_to_product", false)) {
            binding.bottomNav.selectedItemId = R.id.nav_product
        }
    }

    override fun onDestroy() {
        RetrofitClient.onSessionExpired = null
        super.onDestroy()
    }

    // ── 로그아웃 (MyPageFragment에서 호출) ──────────────────────
    fun logout() {
        lifecycleScope.launch {
            runCatching { screenModel.logout(RetrofitClient.memberId) }
            session.clear()
            navigateToLogin()
        }
    }

    // ── 화면 이동 ─────────────────────────────────────────────
    fun navigateToOrder() = navigateTo(R.id.nav_order)

    fun navigateToOrderTab(tabIndex: Int) {
        pendingOrderTab = tabIndex
        navigateTo(R.id.nav_order)
    }

    fun navigateToOrderCompleted() = navigateToOrderTab(3)

    fun navigateToSales() = navigateTo(R.id.nav_sales)

    fun navigateToProduct() = navigateTo(R.id.nav_product)

    fun navigateToNotification() = loadFragment(NotificationFragment())

    private fun navigateTo(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }

    // ── 내부 유틸 ─────────────────────────────────────────────
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    fun refreshOrderBadge() {
        lifecycleScope.launch {
            runCatching {
                val response = screenModel.getOrders(RetrofitClient.memberId)
                orderBadgeCount = response.data?.count { it.orderStatus == OrderStatus.Pending } ?: 0
                updateOrderBadge()
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_order -> {
                    orderBadgeCount = 0
                    updateOrderBadge()
                    OrderFragment()
                }
                R.id.nav_sales -> SalesFragment()
                R.id.nav_product -> ProductFragment()
                R.id.nav_my -> MyPageFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateOrderBadge() {
        val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_order)
        if (orderBadgeCount > 0) {
            badge.isVisible = true
            badge.number = orderBadgeCount
            badge.backgroundColor = getColor(R.color.danger)
            badge.badgeTextColor = getColor(R.color.white)
        } else {
            badge.isVisible = false
        }
    }
}
