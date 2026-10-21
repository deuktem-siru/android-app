package com.example.deuktemsiru_seller.presentation.view.settings

import com.example.deuktemsiru_seller.data.repository.NotificationPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.deuktemsiru_seller.util.applySystemBarInsets
import com.example.deuktemsiru_seller.databinding.ActivityNotificationSettingsBinding

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private val preferences by lazy { NotificationPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        binding.btnBack.setOnClickListener { finish() }

        notificationSettings().forEach { setting ->
            setting.switch.isChecked = preferences.isEnabled(setting.key)
            setting.switch.setOnCheckedChangeListener { _, checked ->
                preferences.setEnabled(setting.key, checked)
            }
        }
    }

    private data class NotificationSetting(
        val switch: android.widget.CompoundButton,
        val key: String,
    )

    private fun notificationSettings() = listOf(
        NotificationSetting(binding.switchNewOrder, "new_order"),
        NotificationSetting(binding.switchPickupComplete, "pickup_complete"),
        NotificationSetting(binding.switchSaleComplete, "sale_complete"),
    )

}
