package com.example.deuktemsiru_buyer.ui.onboarding

import com.deuktemsiru.common.loginWithKakao
import com.deuktemsiru.common.isKakaoLoginCancelled
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.deuktemsiru_buyer.BuildConfig
import com.example.deuktemsiru_buyer.MainActivity
import com.example.deuktemsiru_buyer.R
import com.example.deuktemsiru_buyer.data.SessionManager
import com.example.deuktemsiru_buyer.data.appSession
import com.example.deuktemsiru_buyer.databinding.FragmentOnboardingBinding
import com.example.deuktemsiru_buyer.network.DebugLoginRequest
import com.example.deuktemsiru_buyer.network.KakaoLoginRequest
import com.example.deuktemsiru_buyer.network.RetrofitClient
import com.example.deuktemsiru_buyer.util.toast
import kotlinx.coroutines.launch

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private enum class LoginType {
        KAKAO,
        DEBUG,
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = requireContext().appSession

        if (session.isLoggedIn()) {
            navigateHome()
            return
        }

        binding.btnKakaoLogin.setOnClickListener {
            startKakaoLogin(session)
        }

        if (BuildConfig.DEBUG) {
            binding.btnDebugLogin.visibility = View.VISIBLE
            binding.btnDebugLogin.setOnClickListener {
                debugLogin(session)
            }
        }
    }

    private fun debugLogin(session: SessionManager) {
        setLoading(true, LoginType.DEBUG)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.debugLogin(DebugLoginRequest())
                val loginData = response.data
                if (loginData == null) {
                    toast(response.message.ifBlank { "디버그 로그인에 실패했어요" })
                    setLoading(false)
                    return@launch
                }

                session.saveLogin(loginData)
                (activity as? MainActivity)?.enablePush()
                navigateHome()
            } catch (e: Exception) {
                toast("디버그 로그인 서버 연결에 실패했어요.", Toast.LENGTH_LONG)
                setLoading(false)
            }
        }
    }

    private fun startKakaoLogin(session: SessionManager) {
        setLoading(true, LoginType.KAKAO)

        requireContext().loginWithKakao(onToken = { loginToBackend(it, session) }, onError = ::handleKakaoError)
    }

    private fun handleKakaoError(error: Throwable) {
        if (!error.isKakaoLoginCancelled()) {
            toast("카카오 로그인에 실패했어요")
        }
        setLoading(false)
    }

    private fun loginToBackend(kakaoAccessToken: String, session: SessionManager) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val loginData = RetrofitClient.api.kakaoLogin(
                    KakaoLoginRequest(kakaoAccessToken = kakaoAccessToken, role = "CONSUMER")
                ).data

                if (loginData == null) {
                    toast("로그인에 실패했어요")
                    setLoading(false)
                    return@launch
                }

                session.saveLogin(loginData)
                (activity as? MainActivity)?.enablePush()
                navigateHome()
            } catch (e: Exception) {
                toast("서버 로그인에 실패했어요. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG)
                setLoading(false)
            }
        }
    }

    private fun navigateHome() {
        findNavController().navigate(R.id.action_onboarding_to_home)
    }

    private fun setLoading(loading: Boolean, type: LoginType? = null) {
        binding.btnKakaoLogin.isEnabled = !loading
        binding.btnDebugLogin.isEnabled = !loading
        binding.btnKakaoLogin.text =
            if (loading && type == LoginType.KAKAO) "카카오 로그인 중..." else "카카오로 시작하기"
        binding.btnDebugLogin.text =
            if (loading && type == LoginType.DEBUG) "디버그 로그인 중..." else "디버그 로그인"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
