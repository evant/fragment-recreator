package me.tatarka.fragmentrecreator

import android.content.res.Configuration
import androidx.annotation.MainThread
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.util.*

private val FragmentConfigurationChangeMap =
    IdentityHashMap<Fragment, FragmentConfigurationChangeListener>()

/**
 * Tells the fragment to recreate it's views on a configuration change even when the activity is
 * handling it. This is implemented by detaching and immediately re-attaching the fragment. While
 * it's ok to call this method at any time, best practice is to do on or before [Fragment.onCreate].
 * This ensures it's lifecycle stays consistent.
 */
var Fragment.recreateOnConfigurationChange: Boolean
    @MainThread
    get() = FragmentConfigurationChangeMap.contains(this)
    @MainThread
    set(value) {
        if (value) {
            FragmentConfigurationChangeMap.getOrPut(this) {
                FragmentConfigurationChangeListener().also { lifecycle.addObserver(it) }
            }
        } else {
            FragmentConfigurationChangeMap.remove(this)?.let {
                lifecycle.removeObserver(it)
                // manually clean up as removeObserver won't trigger this for us.
                it.dispose(this)
            }
        }
    }

private class FragmentConfigurationChangeListener
    : DefaultLifecycleObserver, Consumer<Configuration> {

    private var fragment: Fragment? = null

    override fun onCreate(owner: LifecycleOwner) {
        require(owner is Fragment)
        fragment = owner
        val activity = owner.requireActivity()
        activity.addOnConfigurationChangedListener(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        require(owner is Fragment)
        FragmentConfigurationChangeMap.remove(owner)
        dispose(owner)
    }

    fun dispose(owner: Fragment) {
        val activity = owner.requireActivity()
        activity.removeOnConfigurationChangedListener(this)
    }

    override fun accept(newConfiguration: Configuration) {
        // re-create views by brining the fragment up to the created state then back down to it's
        // current state.

        // ignore if the fragment has not yet been created
        val fragment = fragment ?: return

        // ignore if the fragment's views are not created, this is what we are recreating for.
        if (fragment.view == null
            || !fragment.viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
        ) {
            return
        }

        val fm = fragment.parentFragmentManager

        // immediately tear down views
        fm.commitNow {
            detach(fragment)
        }
        // now move back to the current state
        fm.commit {
            attach(fragment)
        }
    }
}