package com.example.deuktemsiru_seller.presentation.view.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deuktemsiru_seller.R
import com.example.deuktemsiru_seller.data.repository.appSession
import com.example.deuktemsiru_seller.databinding.FragmentStoreBinding
import com.example.deuktemsiru_seller.domain.model.MenuItemApiResponse
import com.example.deuktemsiru_seller.presentation.view.store.confirmDeleteMenu
import com.example.deuktemsiru_seller.util.dp
import com.example.deuktemsiru_seller.domain.model.MenuItemUpdateRequest
import com.example.deuktemsiru_seller.data.remote.RetrofitClient
import com.example.deuktemsiru_seller.domain.model.UpdateStoreRequest
import com.example.deuktemsiru_seller.util.emptyTextView
import com.example.deuktemsiru_seller.util.loadMenuImage
import com.example.deuktemsiru_seller.util.renderChildren
import com.example.deuktemsiru_seller.util.toast
import com.example.deuktemsiru_seller.util.toClockMinutes
import com.example.deuktemsiru_seller.util.showTimePicker
import com.example.deuktemsiru_seller.util.toClockTime
import com.example.deuktemsiru_seller.util.toWon
import com.example.deuktemsiru_seller.util.visibleIf
import kotlinx.coroutines.launch

class StoreFragment : Fragment() {

    private val screenModel by lazy { com.example.deuktemsiru_seller.AppGraph.model(this) }

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = requireContext().appSession
        if (session.isLoggedIn()) {
            loadStore()
            loadMenus()
        }

        binding.btnEdit.setOnClickListener { enterEditMode() }
        binding.btnSave.setOnClickListener { if (session.isLoggedIn()) saveStore() }
        binding.containerClosingTime.setOnClickListener { if (isEditMode) showTimePicker() }
    }

    // ── 가게 정보 ─────────────────────────────────────────────

    private fun loadStore() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val store = screenModel.getMyStore(RetrofitClient.memberId).data ?: return@runCatching
                binding.tvStoreEmoji.text = "🏪"
                binding.tvStoreName.text = store.name
                binding.tvCategoryStatus.text = "영업 중  •  ${categoryLabel(store.category ?: "")}"
                binding.tvRating.text = "—"
                binding.etAddress.setText(store.address ?: "")
                binding.etPhone.setText(store.phone ?: "")
                binding.tvClosingTime.text = store.closingTime ?: ""
            }.onFailure {
                toast("가게 정보를 불러올 수 없어요.")
            }
        }
    }

    private fun saveStore() {
        val address = binding.etAddress.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        val closingTime = binding.tvClosingTime.text?.toString() ?: ""

        if (address.isEmpty()) {
            binding.etAddress.error = "주소를 입력해주세요"
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                binding.btnSave.isEnabled = false
                val store = screenModel.updateStore(
                    RetrofitClient.memberId,
                    UpdateStoreRequest(address = address, phone = phone, closingTime = closingTime)
                ).data ?: return@runCatching
                binding.tvStoreName.text = store.name
                binding.etAddress.setText(store.address)
                binding.etPhone.setText(store.phone)
                binding.tvClosingTime.text = store.closingTime
                exitEditMode()
                toast("가게 정보가 저장되었어요.")
            }.onFailure {
                toast("저장에 실패했어요.")
            }
            binding.btnSave.isEnabled = true
        }
    }

    // ── 메뉴 목록 ─────────────────────────────────────────────

    private fun loadMenus() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val menus = screenModel.getMenus(RetrofitClient.memberId).data ?: emptyList()
                renderMenus(menus)
            }.onFailure {
                renderMenus(emptyList())
            }
        }
    }

    private fun renderMenus(menus: List<MenuItemApiResponse>) {
        binding.menuContainer.renderChildren(
            items = menus,
            emptyView = { requireContext().emptyTextView("등록된 메뉴가 없어요") },
            itemView = ::createMenuRow,
        )
    }

    private fun createMenuRow(menu: MenuItemApiResponse): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 12.dp, 0, 12.dp)
        }
        val emoji = TextView(requireContext()).apply {
            text = menu.displayEmoji
            textSize = 24f
            gravity = android.view.Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }
        val image = ImageView(requireContext()).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }
        val thumbnail = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(40.dp, 40.dp)
            addView(emoji)
            addView(image)
        }
        val summary = TextView(requireContext()).apply {
            text = "${menu.name}\n${menu.originalPrice.toWon()}"
            textSize = 13f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 12.dp
            }
        }
        image.loadMenuImage(menu.imageUrl, emoji, viewLifecycleOwner)
        val edit = TextView(requireContext()).apply {
            text = "수정"
            textSize = 13f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
            setPadding(12.dp, 8.dp, 12.dp, 8.dp)
            setOnClickListener { showMenuEditDialog(menu) }
        }
        val delete = TextView(requireContext()).apply {
            text = "삭제"
            textSize = 13f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.danger))
            setPadding(12.dp, 8.dp, 0, 8.dp)
            setOnClickListener { confirmDeleteMenu(menu) }
        }
        row.addView(thumbnail)
        row.addView(summary)
        row.addView(edit)
        row.addView(delete)
        return row
    }

    private fun showMenuEditDialog(menu: MenuItemApiResponse) {
        val form = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 8.dp, 16.dp, 0)
        }
        val nameField = editText("메뉴명", menu.name)
        val priceField = editText("정상가", menu.originalPrice.toString(), numeric = true)
        form.addView(nameField)
        form.addView(priceField)

        AlertDialog.Builder(requireContext())
            .setTitle(menu.name)
            .setView(form)
            .setPositiveButton("저장") { _, _ ->
                val name = nameField.text?.toString()?.trim().orEmpty().ifBlank { menu.name }
                val price = priceField.text?.toString()?.toIntOrNull() ?: menu.originalPrice
                updateMenu(menu.id, MenuItemUpdateRequest(name = name, originalPrice = price))
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun editText(hint: String, value: String, numeric: Boolean = false): EditText =
        EditText(requireContext()).apply {
            this.hint = hint
            setText(value)
            textSize = 14f
            inputType = if (numeric) android.text.InputType.TYPE_CLASS_NUMBER
                        else android.text.InputType.TYPE_CLASS_TEXT
        }

    private fun updateMenu(menuId: Long, req: MenuItemUpdateRequest) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                screenModel.updateMenu(menuId, RetrofitClient.memberId, req)
                toast("메뉴가 수정됐어요.")
                loadMenus()
            }.onFailure {
                toast("메뉴 수정에 실패했어요.")
            }
        }
    }

    private fun confirmDeleteMenu(menu: MenuItemApiResponse) {
        requireContext().confirmDeleteMenu(menu, viewLifecycleOwner, delete = { screenModel.deleteMenu(menu.id, RetrofitClient.memberId) },
            onDeleted = ::loadMenus,
            onFailure = { toast("메뉴 삭제에 실패했어요.") },
        )
    }

    // ── 편집 모드 ─────────────────────────────────────────────

    private fun enterEditMode() {
        setEditMode(true)
        binding.etAddress.requestFocus()
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(binding.etAddress, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun exitEditMode() {
        setEditMode(false)
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        listOf(binding.etAddress, binding.etPhone).forEach { field ->
            field.isEnabled = enabled
            field.setTextColor(requireContext().getColor(R.color.text))
        }
        binding.tvClosingTimeArrow.visibleIf(enabled)
        binding.btnEdit.visibleIf(!enabled)
        binding.btnSave.visibleIf(enabled)
    }

    private fun showTimePicker() {
        val current = binding.tvClosingTime.text?.toString() ?: "21:00"
        val minutes = current.toClockMinutes(defaultHour = 21)
        requireContext().showTimePicker(minutes) {
            binding.tvClosingTime.text = it.toClockTime()
        }
    }

    // ── 유틸 ──────────────────────────────────────────────────

    private fun categoryLabel(category: String) = when (category.uppercase()) {
        "BAKERY" -> "베이커리"
        "RESTAURANT" -> "음식점"
        "CAFE" -> "카페"
        else -> category
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
