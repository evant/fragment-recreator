package me.tatarka.fragmentrecreator.viewpager2

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.core.content.OnConfigurationChangedProvider
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 * A version of [androidx.viewpager2.adapter.FragmentStateAdapter] that invalidates items due to
 * configuration changes allowing them to be properly handled by the fragment.
 */
abstract class FragmentStateAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val configurationChangedProvider: OnConfigurationChangedProvider,
) : androidx.viewpager2.adapter.FragmentStateAdapter(fragmentManager, lifecycle) {

    constructor(fragmentActivity: FragmentActivity) : this(
        fragmentActivity.supportFragmentManager,
        fragmentActivity.lifecycle,
        fragmentActivity,
    )

    constructor(fragment: Fragment) : this(
        fragment.childFragmentManager,
        fragment.viewLifecycleOwner.lifecycle,
        fragment.requireActivity(),
    )

    init {
        lifecycle.addObserver(Listener())
    }

    private inner class Listener : DefaultLifecycleObserver, Consumer<Configuration> {

        override fun onCreate(owner: LifecycleOwner) {
            configurationChangedProvider.addOnConfigurationChangedListener(this)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            configurationChangedProvider.removeOnConfigurationChangedListener(this)
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun accept(configuration: Configuration) {
            notifyDataSetChanged()
        }
    }
}