package pas.tmdb.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.WebKit.WKAudiovisualMediaTypeNone
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

private const val EMBED_ERROR_HANDLER = "onEmbedError"

// IFrame Player API error codes signalling embedding is disabled by the video owner.
private val EMBED_NOT_ALLOWED_CODES = setOf(101, 150)

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun YoutubeEmbedPlayer(
    videoKey: String,
    modifier: Modifier,
    onEmbedNotAllowed: () -> Unit,
) {
    val html = remember(videoKey) { youtubeEmbedHtml(videoKey) }
    val currentOnEmbedNotAllowed by rememberUpdatedState(onEmbedNotAllowed)

    val messageHandler = remember {
        object : NSObject(), WKScriptMessageHandlerProtocol {
            override fun userContentController(
                userContentController: WKUserContentController,
                didReceiveScriptMessage: WKScriptMessage,
            ) {
                val code = (didReceiveScriptMessage.body as? NSNumber)?.intValue
                if (code != null && code in EMBED_NOT_ALLOWED_CODES) {
                    currentOnEmbedNotAllowed()
                }
            }
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val contentController = WKUserContentController().apply {
                addScriptMessageHandler(messageHandler, EMBED_ERROR_HANDLER)
            }
            val configuration = WKWebViewConfiguration().apply {
                allowsInlineMediaPlayback = true
                mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone
                this.userContentController = contentController
            }
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = configuration,
            ).apply {
                loadHTMLString(html, baseURL = NSURL.URLWithString("https://www.youtube.com"))
            }
        },
    )
}
