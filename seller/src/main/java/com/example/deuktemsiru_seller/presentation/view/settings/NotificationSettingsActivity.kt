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

        listOf(
            binding.switchNewOrder to "new_order",
            binding.switchPickupComplete to "pickup_complete",
            binding.switchSaleComplete to "sale_complete",
        ).forEach { (switch, key) ->
            switch.isChecked = preferences.isEnabled(key)
            switch.setOnCheckedChangeListener { _, checked -> preferences.setEnabled(key, checked) }
        }
    }

}
