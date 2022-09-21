# fragment-recreator

A utility to handle fragment view-recreation when used in an Activity that handles configuration
changes.

## Why?

Unlike android's view system, jetpack compose is able to handle configuration changes gracefully.
This opens up new possibilities like simplifying app architecture and doing fun things like 
animations on rotation. Unfortunately you can't really take advantage of that in an hybrid app that
still uses views. This library lets you set the option for view recreation per-fragment, allowing 
you slowly migrate one fragment at a time.

## Usage

First make sure your activity is handling configuration changes.

```xml
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
    android:exported="true"
    android:windowSoftInputMode="adjustResize">
    <intent-filter>
        <category android:name="android.intent.category.LAUNCHER" />
        <action android:name="android.intent.action.MAIN" />
    </intent-filter>
</activity>
```

Then, for any fragments you want to recreate their views, set `recreateOnConfigurationChange` to 
true.

```kotlin
class MyLegacyFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recreateOnConfigurationChange = true
    }
}
```

You could also automatically set this for all fragments on an activity by registering fragment
lifecycle callbacks. Note: doing this explicitly per-fragment is preferred as it calls out all the
places where you are opting into the behavior, but this may be necessary if for example you have
3rd-party fragments you need to control.

```kotlin
supportFragmentManager.registerFragmentLifecycleCallbacks(object :
    FragmentLifecycleCallbacks() {
    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        f.recreateOnConfigurationChange = true
    }
}, true)
```