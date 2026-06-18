package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.SandboxProfile
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SandboxWebView(
    profile: SandboxProfile,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Backup gesture triggers
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    // Intercept back actions
    BackHandler {
        val webView = webViewRef
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            showExitDialog = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing // Respect dynamic system bars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black) // Dark slate background to avoid white flashes
                // Multi-gesture listeners for sandbox escape (5 rapid taps OR multi-finger gestures)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 450) {
                                tapCount++
                                if (tapCount >= 5) {
                                    showExitDialog = true
                                    tapCount = 0
                                }
                            } else {
                                tapCount = 1
                            }
                            lastTapTime = now
                        }
                    )
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            // If 3 or more touch contacts are present simultaneously
                            if (event.changes.size >= 3) {
                                val allPressed = event.changes.all { it.pressed }
                                if (allPressed) {
                                    var timerInterrupted = false
                                    val startTime = System.currentTimeMillis()
                                    // Monitor touch states for 1.3 seconds
                                    while (System.currentTimeMillis() - startTime < 1300) {
                                        val nextEvent = awaitPointerEvent()
                                        val activeContacts = nextEvent.changes.filter { it.pressed }
                                        if (activeContacts.size < 3) {
                                            timerInterrupted = true
                                            break
                                        }
                                    }
                                    if (!timerInterrupted) {
                                        showExitDialog = true
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            // Loading Overlay that fades out
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        // Set standard styling
                        overScrollMode = View.OVER_SCROLL_NEVER
                        isVerticalScrollBarEnabled = false
                        isHorizontalScrollBarEnabled = false
                        
                        // Modern, native-ready sandbox settings
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            
                            // Desktop Mode overrides
                            if (profile.isDesktopMode) {
                                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            }
                        }

                        // Sandbox cookies configuration
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                webViewRef = view

                                if (view != null) {
                                    val injectCss = profile.customCss
                                    val hideSelectors = profile.cssHideSelectors
                                    val injectJs = profile.customJs

                                    val injectionCode = StringBuilder()

                                    // 1. Inject Custom CSS Stylesheet
                                    if (injectCss.isNotEmpty()) {
                                        val processedCss = injectCss
                                            .replace("`", "\\`")
                                            .replace("$", "\\$")
                                            .replace("\n", " ")
                                        injectionCode.append("""
                                            var style = document.createElement('style');
                                            style.type = 'text/css';
                                            style.innerHTML = `$processedCss`;
                                            document.head.appendChild(style);
                                        """.trimIndent())
                                    }

                                    // 2. Hide Designated Selectors & Setup Observer to lock hide state (handles Lazy-loaded panels!)
                                    if (hideSelectors.isNotEmpty()) {
                                        val selectorsEscaped = hideSelectors.replace("'", "\\'")
                                        injectionCode.append("""
                                            var selectors = '$selectorsEscaped';
                                            var arr = selectors.split(',');
                                            arr.forEach(function(sel) {
                                                var trimmed = sel.trim();
                                                if (trimmed) {
                                                    function hideAll() {
                                                        var elms = document.querySelectorAll(trimmed);
                                                        elms.forEach(function(elm) {
                                                            elm.style.setProperty('display', 'none', 'important');
                                                        });
                                                    }
                                                    hideAll();
                                                    // Dynamic observer
                                                    if (window.MutationObserver) {
                                                        var obs = new MutationObserver(hideAll);
                                                        obs.observe(document.body, { childList: true, subtree: true });
                                                    }
                                                }
                                            });
                                        """.trimIndent())
                                    }

                                    // Run the combined UI injection package
                                    if (injectionCode.isNotEmpty()) {
                                        view.evaluateJavascript("javascript:(function() { $injectionCode })()", null)
                                    }

                                    // 3. Inject arbitrary custom scripts
                                    if (injectJs.isNotEmpty()) {
                                        view.evaluateJavascript("javascript:(function() { $injectJs })()", null)
                                    }
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val destinationUri = request?.url ?: return false
                                val host = destinationUri.host ?: return false
                                val initialHost = Uri.parse(profile.url).host ?: ""

                                // Forward special protocols (tel/mailto/sms etc.) natively to device handler
                                val scheme = destinationUri.scheme
                                if (scheme != null && scheme != "http" && scheme != "https") {
                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, destinationUri)
                                        context.startActivity(intent)
                                        true
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No app found to handle this link", Toast.LENGTH_SHORT).show()
                                        true
                                    }
                                }

                                // Domain Sandbox Lockdown Check
                                if (profile.blockExternalPages && initialHost.isNotEmpty()) {
                                    val isInternal = host.contains(initialHost) || initialHost.contains(host)
                                    if (!isInternal) {
                                        Toast.makeText(context, "External navigation blocked inside Sandbox", Toast.LENGTH_SHORT).show()
                                        return true // Intercept & Block
                                    }
                                }

                                return false // Load locally in WebView
                            }
                        }

                        loadUrl(profile.url)
                    }
                },
                update = { webView ->
                    webViewRef = webView
                }
            )

            // Progress Loading Indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }

            // Discreet gesture guidelines (visible briefly on screen load)
            var showHelperText by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(4000)
                showHelperText = false
            }
            if (showHelperText) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.75f)
                    )
                ) {
                    Text(
                        text = "Gesture Unlocked: Hold screen with 3 fingers or tap 5 times rapidly to exit sandbox.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Hidden Sandbox exit configuration dialog
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Web Sandbox Control") },
                    text = {
                        Text("You are inside a sandboxed display of '${profile.name}'. What would you like to do?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExitDialog = false
                                onExit()
                            }
                        ) {
                            Text("Exit to Sandbox Cabin")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showExitDialog = false
                                webViewRef?.reload()
                            }
                        ) {
                            Text("Refresh Page")
                        }
                    }
                )
            }
        }
    }
}
