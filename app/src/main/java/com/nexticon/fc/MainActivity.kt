package com.nexticon.fc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var statusView: View
    private var pendingCameraRequest: PermissionRequest? = null

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val request = pendingCameraRequest
        pendingCameraRequest = null
        if (request == null) return@registerForActivityResult
        if (granted) {
            request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
        } else {
            request.deny()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        statusView = findViewById(R.id.status)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        if (!hasBundledGame()) {
            statusView.visibility = View.VISIBLE
            webView.visibility = View.GONE
            return
        }

        configureWebView()
        statusView.visibility = View.VISIBLE
        webView.visibility = View.INVISIBLE
        loadFromIntent(intent)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        loadFromIntent(intent)
    }

    private fun configureWebView() {
        val assetLoader = WebViewAssetLoader.Builder()
            .setDomain(VIRTUAL_HOST)
            .addPathHandler("/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = false
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(true)
            builtInZoomControls = false
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                if (!request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    request.deny()
                    return
                }
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                    return
                }
                pendingCameraRequest = request
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        webView.webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest,
            ): WebResourceResponse? {
                val url = request.url
                if (!isVirtualHost(url.host)) return null
                val path = url.path ?: "/"
                if (isSpaFallbackPath(path)) {
                    return assetLoader.shouldInterceptRequest(
                        url.buildUpon().encodedPath("/index.html").build(),
                    )
                }
                return assetLoader.shouldInterceptRequest(url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val uri = request.url
                if (shouldStayInApp(uri)) return false
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                statusView.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }

            @Deprecated("Deprecated in API 24+ bridge")
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String?,
            ) {
                statusView.visibility = View.VISIBLE
                webView.visibility = View.GONE
            }
        }
    }

    private fun loadFromIntent(intent: Intent?) {
        val join = parseJoinUri(intent?.data)
        if (join != null && isDeviceOnline()) {
            webView.loadUrl(join.toString())
            return
        }
        webView.loadUrl("https://$VIRTUAL_HOST/index.html")
    }

    private fun isDeviceOnline(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = manager.activeNetwork ?: return false
        val caps = manager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun hasBundledGame(): Boolean {
        return try {
            assets.open("index.html").use { true }
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        const val VIRTUAL_HOST = "app.local"

        val JOIN_HOSTS = setOf(
            "nexticon-fc.onrender.com",
            "nexticon-fc-staging.onrender.com",
            VIRTUAL_HOST,
        )

        fun isVirtualHost(host: String?): Boolean {
            return host.equals(VIRTUAL_HOST, ignoreCase = true)
        }

        fun isSpaFallbackPath(path: String): Boolean {
            if (path.isEmpty() || path == "/") return true
            if (path.startsWith("/j/")) return true
            val last = path.substringAfterLast('/')
            return last.isNotEmpty() && !last.contains('.')
        }

        fun shouldStayInApp(uri: Uri): Boolean {
            val host = uri.host?.lowercase() ?: return false
            if (isVirtualHost(host)) return true
            if (host.endsWith("onrender.com")) return true
            if (host.endsWith("supabase.co") || host.endsWith("supabase.in")) return true
            return false
        }

        fun parseJoinUri(uri: Uri?): Uri? {
            if (uri == null) return null
            val host = uri.host?.lowercase() ?: return null
            if (host !in JOIN_HOSTS) return null
            val path = uri.path ?: return null
            if (!path.matches(Regex("^/j/[A-Za-z0-9-]+/?$"))) return null
            return uri
        }
    }
}
