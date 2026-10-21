package com.example.deuktemsiru_seller.ui.registration

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.appSession
import com.example.deuktemsiru_seller.databinding.ActivityMenuRegistrationBinding
import com.example.deuktemsiru_seller.network.RetrofitClient
import com.example.deuktemsiru_seller.util.bindDiscountPrice
import com.example.deuktemsiru_seller.util.bindPickupTime
import com.example.deuktemsiru_seller.util.discountPrice
import com.example.deuktemsiru_seller.util.PickupTimeState
import com.example.deuktemsiru_seller.util.applySystemBarInsets
import com.example.deuktemsiru_seller.util.inflateInto
import com.example.deuktemsiru_seller.util.setupQuantityControls
import com.example.deuktemsiru_seller.util.toWon
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.visibleIf
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class MenuRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuRegistrationBinding
    private var currentStep = 1
    private var totalSteps = 4
    private var menuOnlyMode = false

    private var selectedMenuName = ""
    private var selectedImageUri: Uri? = null
    private var selectedEmoji = "🍽️"
    private var selectedOriginalPrice = 0
    private var selectedCostPrice: Int? = null
    private var selectedAllergyInfo: String? = null
    private var selectedDiscountRate = 0
    private var selectedQuantity = 5
    private val pickupTime = PickupTimeState()
    private var currentStepView: View? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        currentStepView?.let { refreshStep1Image(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        menuOnlyMode = intent.getBooleanExtra(EXTRA_MENU_ONLY, false)
        totalSteps = if (menuOnlyMode) 1 else 4
        if (menuOnlyMode) {
            binding.tvTitle.text = "새 메뉴 만들기"
            binding.progressContainer.visibility = View.GONE
        }

        binding.root.applySystemBarInsets()

        loadStep(1)

        binding.btnBack.setOnClickListener {
            if (currentStep > 1) loadStep(currentStep - 1) else finish()
        }

        binding.btnNext.setOnClickListener {
            if (currentStep == 1 && !validateStep1()) {
                return@setOnClickListener
            }
            if (currentStep == 2 && !validateStep2()) {
                return@setOnClickListener
            }

            if (currentStep < totalSteps) {
                loadStep(currentStep + 1)
            } else {
                registerMenu()
            }
        }

    }

    private fun registerMenu() {
        val session = this.appSession
        if (!session.isLoggedIn()) {
            toast("로그인이 필요해요.")
            return
        }

        binding.btnNext.isEnabled = false
        binding.btnNext.text = "등록 중..."

        lifecycleScope.launch {
            try {
                RetrofitClient.api.addMenuWithImage(
                    sellerId = RetrofitClient.memberId,
                    name = selectedMenuName.toTextPart(),
                    originalPrice = selectedOriginalPrice.toString().toTextPart(),
                    description = null,
                    allergenInfo = selectedAllergyInfo?.toTextPart(),
                    image = createImagePart("image"),
                )
                toast("메뉴가 등록됐어요! 판매 상품으로 등록해보세요.", Toast.LENGTH_LONG)
                finish()
            } catch (e: Exception) {
                toast("등록에 실패했어요.")
                binding.btnNext.isEnabled = true
                binding.btnNext.text = "등록하기"
            }
        }
    }

    private fun loadStep(step: Int) {
        currentStep = step
        updateProgressBar()
        updateStepLabel()
        updateBottomButtons()

        val layoutRes = when (step) {
            1 -> if (menuOnlyMode) R.layout.step_quick_menu else R.layout.step1_menu_select
            2 -> R.layout.step2_price_setting
            3 -> R.layout.step3_quantity_time
            4 -> R.layout.step4_preview
            else -> R.layout.step1_menu_select
        }

        binding.stepContainer.removeAllViews()
        val stepView = layoutInflater.inflateInto(layoutRes, binding.stepContainer)
        currentStepView = stepView

        when (step) {
            1 -> setupStep1(stepView)
            2 -> setupStep2(stepView)
            3 -> setupStep3(stepView)
            4 -> setupStep4(stepView)
        }
    }

    private fun setupStep1(view: View) {
        val etName = view.findViewById<EditText>(R.id.et_menu_name)
        val etEmoji = view.findViewById<EditText?>(R.id.et_emoji)
        val etPrice = view.findViewById<EditText>(R.id.et_original_price)
        val etCostPrice = view.findViewById<EditText?>(R.id.et_cost_price)
        val etAllergyInfo = view.findViewById<EditText?>(R.id.et_allergy_info)
        val tvSummary = view.findViewById<TextView>(R.id.tv_selected_menu_summary)
        val imageUpload = view.findViewById<View>(R.id.container_image_upload)

        etName.setText(selectedMenuName)
        etEmoji?.setText(if (selectedEmoji == "🍽️") "" else selectedEmoji)
        if (selectedOriginalPrice > 0) etPrice.setText(selectedOriginalPrice.toString())
        selectedCostPrice?.let { etCostPrice?.setText(it.toString()) }
        selectedAllergyInfo?.let { etAllergyInfo?.setText(it) }
        refreshStep1Image(view)

        imageUpload.setOnClickListener {
            imagePicker.launch("image/*")
        }

        fun refreshSummary() {
            val name = etName.text?.toString()?.trim().orEmpty()
            val price = etPrice.text?.toString()?.toIntOrNull() ?: 0
            tvSummary.text = if (name.isBlank() || price <= 0) {
                "메뉴 정보를 입력해주세요"
            } else {
                "$name  ·  ${price.toWon()}"
            }
        }

        etName.doOnTextChanged { _, _, _, _ -> refreshSummary() }
        etPrice.doOnTextChanged { _, _, _, _ -> refreshSummary() }
        refreshSummary()
    }

    private fun refreshStep1Image(view: View) {
        val imageView = view.findViewById<ImageView>(R.id.iv_menu_image)
        val hintView = view.findViewById<TextView>(R.id.tv_image_upload_hint)
        val uri = selectedImageUri
        imageView.visibleIf(uri != null)
        hintView.visibleIf(uri == null)
        uri?.let(imageView::setImageURI)
    }

    private fun validateStep1(): Boolean {
        val view = binding.stepContainer.getChildAt(0) ?: return true
        val etName = view.findViewById<EditText>(R.id.et_menu_name)
        val etPrice = view.findViewById<EditText>(R.id.et_original_price)

        val name = etName.text?.toString()?.trim().orEmpty()
        val price = etPrice.text?.toString()?.toIntOrNull() ?: 0

        if (name.isBlank()) {
            etName.error = "메뉴명을 입력해주세요"
            etName.requestFocus()
            return false
        }
        if (price <= 0) {
            etPrice.error = "정상가를 입력해주세요"
            etPrice.requestFocus()
            return false
        }

        selectedMenuName = name
        selectedOriginalPrice = price
        val emojiInput = view.findViewById<EditText?>(R.id.et_emoji)?.text?.toString()?.trim()
        if (!emojiInput.isNullOrBlank()) selectedEmoji = emojiInput
        selectedCostPrice = view.findViewById<EditText?>(R.id.et_cost_price)?.text?.toString()?.toIntOrNull()
        selectedAllergyInfo = view.findViewById<EditText?>(R.id.et_allergy_info)?.text?.toString()?.trim()?.takeIf { it.isNotBlank() }
        return true
    }

    private fun setupStep4(view: View) {
        val session = this.appSession
        view.findViewById<TextView>(R.id.tv_preview_store_name).text =
            session.nickname.ifBlank { "내 가게" }
        val previewImage = view.findViewById<ImageView>(R.id.iv_preview_menu_image)
        selectedImageUri?.let { previewImage.setImageURI(it) }
        view.findViewById<TextView>(R.id.tv_preview_menu_name).text = selectedMenuName
        view.findViewById<TextView>(R.id.tv_preview_discount_price).text = discountedPrice().toWon()
        view.findViewById<TextView>(R.id.tv_preview_discount_rate).text = " ${selectedDiscountRate}%↓"
        view.findViewById<TextView>(R.id.tv_preview_quantity_time).text =
            "잔여 ${selectedQuantity}개 · 픽업 ${pickupTimeLabel()}"
        view.findViewById<TextView>(R.id.tv_summary_menu_name).text = selectedMenuName
        view.findViewById<TextView>(R.id.tv_summary_original_price).text = selectedOriginalPrice.toWon()
        view.findViewById<TextView>(R.id.tv_summary_discount_price).text =
            "${discountedPrice().toWon()} (${selectedDiscountRate}% 할인)"
        view.findViewById<TextView>(R.id.tv_summary_quantity).text = "${selectedQuantity}개"
        view.findViewById<TextView>(R.id.tv_summary_pickup_time).text = pickupTimeLabel()
    }

    private fun setupStep2(view: View) {
        view.bindDiscountPrice(selectedOriginalPrice, selectedDiscountRate,
            onRateChanged = { selectedDiscountRate = it },
            onPriceChanged = { price ->
                view.findViewById<TextView>(R.id.tv_original_price).text = selectedOriginalPrice.toWon()
                view.findViewById<TextView>(R.id.tv_discount_price).text = price.toWon()
                view.findViewById<TextView>(R.id.tv_customer_saving).text = (selectedOriginalPrice - price).toWon()
                view.findViewById<TextView>(R.id.tv_seller_recovery).text = price.toWon()
            },
        )
    }

    private fun validateStep2(): Boolean {
        val view = binding.stepContainer.getChildAt(0) ?: return true
        val etDiscountRate = view.findViewById<EditText>(R.id.et_discount_rate)
        val discountRate = etDiscountRate.text?.toString()?.toIntOrNull() ?: 0
        if (discountRate !in 1..99) {
            etDiscountRate.error = "1~99 사이의 할인율을 입력해주세요"
            etDiscountRate.requestFocus()
            return false
        }

        selectedDiscountRate = discountRate
        return true
    }

    private fun setupStep3(view: View) {
        setupQuantityControls(view, selectedQuantity) { selectedQuantity = it }
        view.bindPickupTime(pickupTime, minDurationMinutes = 30)
    }

    private fun discountedPrice(): Int = discountPrice(selectedOriginalPrice, selectedDiscountRate)

    private fun pickupTimeLabel(): String =
        "${pickupTime.startLabel} – ${pickupTime.endLabel}"
    private fun String.toTextPart(): RequestBody =
        this.toRequestBody("text/plain".toMediaType())

    private fun createImagePart(partName: String = "image"): MultipartBody.Part? {
        val uri = selectedImageUri ?: return null
        val mimeType = contentResolver.getType(uri) ?: "image/*"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val filename = getDisplayName(uri) ?: "menu-image"
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, filename, body)
    }

    private fun getDisplayName(uri: Uri): String? =
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else null
        }

    private fun updateProgressBar() {
        val progressViews = listOf(
            binding.progress1, binding.progress2, binding.progress3, binding.progress4
        )
        progressViews.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index < currentStep) R.drawable.bg_progress_active
                else R.drawable.bg_progress_inactive
            )
        }
    }

    private fun updateStepLabel() {
        binding.tvStepLabel.text = "$currentStep/$totalSteps"
    }

    private fun updateBottomButtons() {
        binding.btnNext.text = when {
            currentStep != totalSteps -> "다음 ($currentStep/${totalSteps})"
            menuOnlyMode -> "메뉴 등록"
            else -> "등록하기"
        }
    }

    companion object {
        const val EXTRA_MENU_ONLY = "extra_menu_only"
    }
}
