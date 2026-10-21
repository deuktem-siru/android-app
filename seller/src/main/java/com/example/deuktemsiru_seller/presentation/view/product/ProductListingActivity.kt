package com.example.deuktemsiru_seller.presentation.view.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.MainActivity
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.databinding.ActivityProductListingBinding
import com.example.deuktemsiru_seller.databinding.ItemChooseMenuBinding
import com.example.deuktemsiru_seller.domain.model.MenuItemApiResponse
import com.example.deuktemsiru_seller.data.remote.RetrofitClient
import com.example.deuktemsiru_seller.domain.model.SaleItemCreateRequest
import com.example.deuktemsiru_seller.presentation.view.registration.MenuRegistrationActivity
import com.example.deuktemsiru_seller.util.applySystemBarInsets
import com.example.deuktemsiru_seller.util.bindDiscountPrice
import com.example.deuktemsiru_seller.util.bindPickupTime
import com.example.deuktemsiru_seller.domain.product.discountPrice
import com.example.deuktemsiru_seller.presentation.view.store.confirmDeleteMenu
import com.example.deuktemsiru_seller.domain.product.PickupTimeState
import com.example.deuktemsiru_seller.util.failureMessage
import com.example.deuktemsiru_seller.util.emptyTextView
import com.example.deuktemsiru_seller.util.inflateInto
import com.example.deuktemsiru_seller.util.loadMenuImage
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.setSelectedChip
import com.example.deuktemsiru_seller.util.setupQuantityControls
import com.example.deuktemsiru_seller.util.toWon
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.visibleIf
import kotlinx.coroutines.launch
import java.time.LocalDate

class ProductListingActivity : AppCompatActivity() {

    private val screenModel by lazy { com.example.deuktemsiru_seller.AppGraph.model(this) }

    private lateinit var binding: ActivityProductListingBinding
    private var currentStep = 1
    private var menus: List<MenuItemApiResponse> = emptyList()
    private var selectedMenu: MenuItemApiResponse? = null
    private var selectedDiscountRate = 20
    private var selectedPriceMode: String = "rate"
    private var selectedQuantity = 5
    private val pickupTime = PickupTimeState()
    private var reloadMenusOnResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnNext.setOnClickListener { onNextClicked() }
        binding.btnPrev.setOnClickListener { loadStep(1) }
        loadMenus()
    }

    override fun onResume() {
        super.onResume()
        if (reloadMenusOnResume) {
            reloadMenusOnResume = false
            loadMenus()
        }
    }

    private fun loadMenus() {
        lifecycleScope.launch {
            try {
                menus = screenModel.getMenus(RetrofitClient.memberId).data ?: emptyList()
                loadStep(1)
            } catch (e: Exception) {
                toast("메뉴를 불러올 수 없어요")
                finish()
            }
        }
    }

    private fun loadStep(step: Int) {
        currentStep = step
        listOf(binding.progress1, binding.progress2).forEachIndexed { index, progress ->
            progress.setBackgroundResource(if (step > index) R.drawable.bg_progress_active else R.drawable.bg_progress_inactive)
        }
        binding.tvStepLabel.text = "$step/2"
        binding.stepContainer.removeAllViews()

        val (layoutRes, nextText, setup) = when (step) {
            1 -> Triple(R.layout.step_choose_menu, "다음", ::setupStep1)
            else -> Triple(R.layout.step_sale_details, "상품 등록", ::setupStep2)
        }
        binding.btnPrev.visibleIf(step > 1)
        binding.btnNext.text = nextText
        setup(layoutInflater.inflateInto(layoutRes, binding.stepContainer))
    }

    private fun setupStep1(view: View) {
        val container = view.findViewById<android.widget.LinearLayout>(R.id.menu_list_container)
        container.renderChildren(
            items = menus,
            emptyView = { emptyTextView("등록된 메뉴가 없어요. 먼저 메뉴를 추가해주세요.") },
        ) { menu ->
                val itemBinding = ItemChooseMenuBinding.inflate(LayoutInflater.from(this), container, false)
                itemBinding.tvMenuEmoji.text = menu.displayEmoji
                itemBinding.ivMenuImage.loadMenuImage(menu.imageUrl, itemBinding.tvMenuEmoji, this)
                itemBinding.tvMenuName.text = menu.name
                itemBinding.tvMenuPrice.text = menu.originalPrice.toWon()
                updateMenuSelection(itemBinding, menu == selectedMenu)
                itemBinding.root.setOnClickListener {
                    selectedMenu = if (selectedMenu?.id == menu.id) null else menu
                    menus.forEachIndexed { i, _ ->
                        val child = container.getChildAt(i)
                        val childBinding = ItemChooseMenuBinding.bind(child)
                        updateMenuSelection(childBinding, selectedMenu?.id == menus[i].id)
                    }
                }
                itemBinding.root.setOnLongClickListener {
                    confirmDeleteMenu(menu)
                    true
                }
                itemBinding.btnDeleteMenu.setOnClickListener {
                    confirmDeleteMenu(menu)
                }
                itemBinding.root
        }

        view.findViewById<TextView>(R.id.btn_new_menu).setOnClickListener {
            reloadMenusOnResume = true
            startActivity(
                Intent(this, MenuRegistrationActivity::class.java)
                    .putExtra(MenuRegistrationActivity.EXTRA_MENU_ONLY, true)
            )
        }
    }

    private fun updateMenuSelection(itemBinding: ItemChooseMenuBinding, selected: Boolean) {
        itemBinding.tvCheck.text = if (selected) "●" else "○"
        itemBinding.tvCheck.setTextColor(getColor(if (selected) R.color.primary else R.color.text_muted))
        itemBinding.root.setBackgroundResource(if (selected) R.drawable.bg_menu_card_selected else R.drawable.bg_menu_card_normal)
    }

    private fun setupStep2(view: View) {
        val menu = selectedMenu ?: return
        view.findViewById<TextView>(R.id.tv_selected_emoji).text = menu.displayEmoji
        view.findViewById<TextView>(R.id.tv_selected_name).text = menu.name
        view.findViewById<TextView>(R.id.tv_selected_price).text = "정상가 ${menu.originalPrice.toWon()}"

        val layoutRateMode = view.findViewById<android.widget.LinearLayout>(R.id.layout_rate_mode)
        val layoutPriceMode = view.findViewById<android.widget.LinearLayout>(R.id.layout_price_mode)
        val btnModeRate = view.findViewById<TextView>(R.id.btn_mode_rate)
        val btnModePrice = view.findViewById<TextView>(R.id.btn_mode_price)

        fun updateModeUI() {
            val rateMode = selectedPriceMode == "rate"
            layoutRateMode.visibleIf(rateMode)
            layoutPriceMode.visibleIf(!rateMode)
            btnModeRate.setSelectedChip(rateMode)
            btnModePrice.setSelectedChip(!rateMode)
        }

        updateModeUI()
        btnModeRate.setOnClickListener { selectedPriceMode = "rate"; updateModeUI() }
        btnModePrice.setOnClickListener { selectedPriceMode = "price"; updateModeUI() }

        view.bindDiscountPrice(menu.originalPrice, selectedDiscountRate,
            onRateChanged = { selectedDiscountRate = it },
            onPriceChanged = { price ->
                view.findViewById<TextView>(R.id.tv_discounted_price).text = price.toWon()
            },
        )
        setupQuantityControls(view, selectedQuantity) { selectedQuantity = it }
        view.bindPickupTime(pickupTime, minDurationMinutes = 60, highlightSelection = true)
    }

    private fun onNextClicked() {
        when {
            currentStep == 1 && selectedMenu == null -> toast("메뉴를 선택해주세요")
            currentStep == 1 -> loadStep(2)
            validateSaleDetails() -> registerProduct()
        }
    }

    private fun validateSaleDetails(): Boolean {
        if (selectedPriceMode == "rate" && selectedDiscountRate !in 1..99) {
            toast("1~99 사이의 할인율을 입력해주세요")
            return false
        }
        if (pickupTime.endMinutes <= pickupTime.startMinutes) {
            toast("픽업 종료 시간이 시작 시간보다 늦어야 해요.")
            return false
        }
        return true
    }

    private fun registerProduct() {
        val menu = selectedMenu ?: return
        setSubmitting(true)
        lifecycleScope.launch {
            try {
                val discountPrice = resolveDiscountPrice(menu) ?: return@launch
                val response = screenModel.createSaleItem(
                    RetrofitClient.memberId,
                    SaleItemCreateRequest(
                        menuItemId = menu.id,
                        name = menu.name,
                        discountPrice = discountPrice,
                        originalPrice = menu.originalPrice,
                        quantityTotal = selectedQuantity,
                        pickupStart = pickupTime.startLabel,
                        pickupEnd = pickupTime.endLabel,
                        availableDate = LocalDate.now().toString(),
                    ),
                )
                if (response.code !in 200..299 || response.data == null) {
                    throw IllegalStateException(response.message)
                }
                toast("상품이 등록됐어요!")
                startActivity(
                    Intent(this@ProductListingActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("navigate_to_product", true)
                    }
                )
                finish()
            } catch (e: Exception) {
                toast("등록에 실패했어요")
                setSubmitting(false)
            }
        }
    }

    private fun resolveDiscountPrice(menu: MenuItemApiResponse): Int? {
        if (selectedPriceMode != "price") {
            return discountPrice(menu.originalPrice, selectedDiscountRate)
        }
        val stepView = binding.stepContainer.getChildAt(0) ?: return null
        val price = stepView.findViewById<android.widget.EditText>(R.id.et_direct_price)
            ?.text?.toString()?.toIntOrNull()
        return when {
            price == null -> {
                toast("할인가를 입력해주세요")
                setSubmitting(false)
                null
            }
            price <= 0 || price >= menu.originalPrice -> {
                toast("할인가는 정가보다 낮은 1원 이상이어야 해요.")
                setSubmitting(false)
                null
            }
            else -> price
        }
    }

    private fun setSubmitting(submitting: Boolean) {
        binding.btnNext.isEnabled = !submitting
        binding.btnNext.text = if (submitting) "등록 중..." else "상품 등록"
    }

    private fun confirmDeleteMenu(menu: MenuItemApiResponse) {
        confirmDeleteMenu(menu, this, delete = { screenModel.deleteMenu(menu.id, RetrofitClient.memberId) }, onDeleted = {
            if (selectedMenu?.id == menu.id) selectedMenu = null
            loadMenus()
        }, onFailure = { error ->
            Log.e("ProductListingActivity", "Failed to delete menu id=${menu.id}", error)
            toast(error.failureMessage("메뉴 삭제"))
        })
    }
}
