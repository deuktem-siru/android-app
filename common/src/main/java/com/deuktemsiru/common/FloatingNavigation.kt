package com.deuktemsiru.common

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

/** Keep the screen background behind the capsule while reserving room for its controls. */
fun AppCompatActivity.setupFloatingNavigation(
    root: View,
    host: View,
    bottomNav: BottomNavigationView,
    bottomGap: Int,
) {
    val contentPadding = mutableMapOf<Fragment, Pair<View, Int>>()
    var systemBottom = 0
    fun updateContentPadding() {
        val clearance = if (bottomNav.visibility == View.VISIBLE) {
            bottomNav.height + bottomGap * 2 + systemBottom
        } else {
            systemBottom
        }
        contentPadding.values.forEach { (view, initialBottom) ->
            view.updatePadding(bottom = initialBottom + clearance)
        }
    }
    fun trackContent(fragment: Fragment, view: View) {
        if (view is FragmentContainerView) return
        if (fragment.id != host.id && fragment.parentFragment?.id != host.id) return
        contentPadding[fragment] = view to view.paddingBottom
        updateContentPadding()
    }
    fun trackRestoredContent(manager: FragmentManager) {
        manager.fragments.forEach { fragment ->
            fragment.view?.let { trackContent(fragment, it) }
            trackRestoredContent(fragment.childFragmentManager)
        }
    }
    trackRestoredContent(supportFragmentManager)
    supportFragmentManager.registerFragmentLifecycleCallbacks(
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, state: Bundle?) {
                trackContent(f, v)
            }

            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                contentPadding.remove(f)
            }
        }, true,
    )
    // Material's default inset handler puts the system area inside the capsule.
    ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { _, insets -> insets }
    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        root.updatePadding(left = bars.left, top = bars.top, right = bars.right)
        systemBottom = bars.bottom
        bottomNav.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = bottomGap + systemBottom
        }
        updateContentPadding()
        insets
    }
    bottomNav.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateContentPadding() }
    ViewCompat.requestApplyInsets(root)
}
