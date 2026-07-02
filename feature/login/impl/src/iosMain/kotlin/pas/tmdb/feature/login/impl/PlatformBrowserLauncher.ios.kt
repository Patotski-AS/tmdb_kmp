package pas.tmdb.feature.login.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberPlatformBrowserLauncher(): (String) -> Unit {
    val viewController = LocalUIViewController.current
    return remember(viewController) {
        val openUrl: (String) -> Unit = fun(url: String) {
            val nsUrl = NSURL.URLWithString(url) ?: return
            val safari = SFSafariViewController(nsUrl)
            topViewController(viewController)?.presentViewController(
                safari,
                animated = true,
                completion = null,
            )
        }
        openUrl
    }
}

private fun topViewController(controller: UIViewController): UIViewController? {
    var current: UIViewController? = controller
    while (current?.presentedViewController != null) {
        current = current?.presentedViewController
    }
    return current ?: UIApplication.sharedApplication.keyWindow?.rootViewController
}
