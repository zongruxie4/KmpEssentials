package com.architect.kmpessentials.lifecycle

import com.architect.kmpessentials.aliases.DefaultAction
import com.architect.kmpessentials.aliases.DefaultActionAsync
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import platform.Foundation.*
import platform.UIKit.*
import kotlinx.atomicfu.atomic

private enum class AppState {
    FOREGROUND,
    BACKGROUND
}

actual class KmpLifecycle {

    actual companion object {
        private var currentState = atomic<AppState?>(null)

        private var backgroundAction: (() -> Unit)? = null
        private var foregroundAction: (() -> Unit)? = null
        private var destroyAction: DefaultAction? = null
        private var isInForeground: Boolean

        init {
            // Register for lifecycle notifications
            isInForeground = when (UIApplication.sharedApplication.applicationState) {
                UIApplicationState.UIApplicationStateActive -> true
                else -> false
            }

            setupLifecycleObservers()
        }

        /**
         *  Sets the action to run when the app enters the background state.
         */
        actual fun setAppLifecycleBackground(action: () -> Unit) {
            backgroundAction = action
        }

        /**
         *  Sets the action to run when the app enters the foreground state.
         */
        actual fun setAppLifecycleForeground(action: () -> Unit) {
            foregroundAction = action
        }

        actual fun setAppLifecycleDestroy(action: DefaultAction) {
            destroyAction = action
        }

        actual suspend fun waitForAppToReturnToForegroundWithTimeout(
            milliseconds: Long,
            action: DefaultActionAsync
        ) {
            val startTime = Clock.System.now().toEpochMilliseconds()
            withTimeoutOrNull(milliseconds) { // Enforce timeout
                while (!isInForeground) { // Platform-specific check
                    delay(100) // Check every second

                    // If the timeout is reached, exit the loop
                    if (Clock.System.now().toEpochMilliseconds() - startTime >= milliseconds) {
                        break
                    }
                }

                true
            }

            action()
        }

        actual suspend fun waitForAppToReturnToBackgroundWithTimeout(
            milliseconds: Long,
            action: DefaultActionAsync
        ) {
            val startTime = Clock.System.now().toEpochMilliseconds()

            withTimeoutOrNull(milliseconds) { // Enforce timeout
                while (isInForeground) { // Platform-specific check
                    delay(100) // Check every second

                    // If the timeout is reached, exit the loop
                    if (Clock.System.now().toEpochMilliseconds() - startTime >= milliseconds) {
                        break
                    }
                }

                true
            }

            action()
        }

        actual suspend fun waitForAppToReturnToForeground(action: DefaultActionAsync) {
            while (!isInForeground) { // checks if the app returns to the foreground
                delay(100)
            }

            action()
        }

        actual fun isCurrentlyInForeground(): Boolean {
            return isInForeground
        }

        /**
         *  Resets all lifecycle actions.
         */
        actual fun resetAppLifecycleActions() {
            backgroundAction = null
            foregroundAction = null
            destroyAction = null
        }

        /**
         *  Initializes the lifecycle observers.
         */
        private fun transitionTo(newState: AppState) {
            val previous = currentState.value
            if (previous != newState) {
                currentState.value = newState
                when (newState) {
                    AppState.FOREGROUND -> {
                        foregroundAction?.invoke()
                        isInForeground = true
                    }

                    AppState.BACKGROUND -> {
                        backgroundAction?.invoke()
                        isInForeground = false
                    }
                }
            }
        }

        private fun setupLifecycleObservers() {
            val notificationCenter = NSNotificationCenter.defaultCenter

            // Observe app entering background
            notificationCenter.addObserverForName(
                name = UIApplicationDidEnterBackgroundNotification,
                `object` = null,
                queue = null
            ) { _ ->
                transitionTo(AppState.BACKGROUND)
            }

            notificationCenter.addObserverForName(
                name = UIApplicationWillResignActiveNotification,
                `object` = null,
                queue = null
            ) { _ ->
                transitionTo(AppState.BACKGROUND)
            }

            // Observe app entering foreground
            notificationCenter.addObserverForName(
                name = UIApplicationDidBecomeActiveNotification,
                `object` = null,
                queue = null
            ) { _ ->
                transitionTo(AppState.FOREGROUND)
            }

            notificationCenter.addObserverForName(
                name = UIApplicationWillEnterForegroundNotification,
                `object` = null,
                queue = null
            ) { _ ->
                transitionTo(AppState.FOREGROUND)
            }

            // Observe app termination
            notificationCenter.addObserverForName(
                name = UIApplicationWillTerminateNotification,
                `object` = null,
                queue = null
            ) { _ ->
                destroyAction?.invoke()
                isInForeground = false
                currentState.value = null
            }
        }
    }
}