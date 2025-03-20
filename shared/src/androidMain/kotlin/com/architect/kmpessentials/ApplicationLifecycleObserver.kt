import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.architect.kmpessentials.lifecycle.KmpLifecycle
import com.architect.kmpessentials.logging.KmpLogging

class ApplicationLifecycleObserver : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        KmpLogging.writeInfo("KMP_ESSENTIALS_APP_STATE", "App is now running the background")

        KmpLifecycle.isInForeground = false
        if(KmpLifecycle.backgroundAction != null) {
            KmpLifecycle.backgroundAction?.invoke()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        KmpLogging.writeInfo("KMP_ESSENTIALS_APP_STATE", "App is now running in the foreground")

        KmpLifecycle.isInForeground = true
        if(KmpLifecycle.foregroundAction != null) {
            KmpLifecycle.foregroundAction?.invoke()
        }
    }
}