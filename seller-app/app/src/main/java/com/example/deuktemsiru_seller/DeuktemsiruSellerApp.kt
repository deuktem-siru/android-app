package com.example.deuktemsiru_seller

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.deuktemsiru_seller.network.Push
import com.kakao.sdk.common.KakaoSdk

class DeuktemsiruSellerApp : Application() {


    override fun onCreate() {
        super.onCreate()
        // ponytail: 다크 팔레트가 없어 라이트 고정. values-night/ 팔레트를 만들면 이 줄 삭제.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        Push.ensureChannel(this)
    }
}
