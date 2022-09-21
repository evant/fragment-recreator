package me.tatarka.fragmentrecreator.sample

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import me.tatarka.fragmentrecreator.recreateOnConfigurationChange

val Pages = arrayOf(
    LegacyViewFragment::class,
    ComposeFragment::class,
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentLifecycleCallbacks() {
            override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                f.recreateOnConfigurationChange = true
            }
        }, true)

        val viewPager: ViewPager2 = findViewById(R.id.pager)
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                val fragmentClass = Pages[position]
                return supportFragmentManager.fragmentFactory.instantiate(
                    fragmentClass.java.classLoader!!,
                    fragmentClass.java.name
                )
            }

            override fun getItemCount(): Int = 2
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}

const val TAG = "FragmentRecreator"

class LegacyViewFragment : Fragment(R.layout.legacy) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recreateOnConfigurationChange = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "legacy view: onViewCreated()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "legacy view: onDestroyView()")
    }
}

class ComposeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Box(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // This is a simple way to show we are surviving configuration changes,
                        // in a real app you would probably want to save this to survive process death.
                        var count by remember { mutableStateOf(0) }
                        val orientation = LocalConfiguration.current.orientation
                        Text("Compose  ${if (orientation == Configuration.ORIENTATION_LANDSCAPE) "Landscape" else "Portrait"} ($count)")

                        Button(onClick = { count++ }) {
                            Text("Click Me")
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "compose: onViewCreated()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "compose: onDestroyView()")
    }
}