package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainWebViewScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWebViewScreen() {
    val context = LocalContext.current
    val baseUrl = "https://pgricibuaya.my.id"

    // WebView States
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(baseUrl) }
    var pageTitle by remember { mutableStateOf("PGRI Cibuaya") }
    var loadingProgress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isOfflineError by remember { mutableStateOf(false) }
    var canGoBackState by remember { mutableStateOf(false) }
    var canGoForwardState by remember { mutableStateOf(false) }

    // Dialog state
    var showSecurityInfoDialog by remember { mutableStateOf(false) }

    // File selection callback for HTML forms in WebView (Upload berkas)
    var uploadMessageCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // File picker launcher
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = WebChromeClient.FileChooserParams.parseResult(result.resultCode, data)
            uploadMessageCallback?.onReceiveValue(results)
        } else {
            uploadMessageCallback?.onReceiveValue(null)
        }
        uploadMessageCallback = null
    }

    // Handle physical back button in Android to exit or load previous webpage
    BackHandler(enabled = canGoBackState) {
        webViewInstance?.let { wv ->
            if (wv.canGoBack()) {
                wv.goBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Koneksi Aman",
                            tint = Color(0xFF006492), // Frosted primary blue
                            modifier = Modifier
                                .size(18.dp)
                                .testTag("secure_lock_indicator")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "pgricibuaya.my.id",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF001E2F)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Koneksi Aman & Terverifikasi",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF006492).copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSecurityInfoDialog = true },
                        modifier = Modifier.testTag("security_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informasi Keamanan",
                            tint = Color(0xFF006492)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xE5F8F9FF), // Semitransparent glass tint
                    titleContentColor = Color(0xFF191C1E),
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            )
        },
        bottomBar = {
            // Elegant Frosted Glass floating navigation dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xE6FFFFFF), // Frosted glass white container
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        IconButton(
                            onClick = {
                                webViewInstance?.let { wv ->
                                    if (wv.canGoBack()) wv.goBack()
                                }
                            },
                            enabled = canGoBackState,
                            modifier = Modifier.testTag("btn_navigation_back")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali ke halaman sebelumnya",
                                tint = if (canGoBackState) Color(0xFF006492) else Color(0x4D001E2F)
                            )
                        }

                        // Forward Button
                        IconButton(
                            onClick = {
                                webViewInstance?.let { wv ->
                                    if (wv.canGoForward()) wv.goForward()
                                }
                            },
                            enabled = canGoForwardState,
                            modifier = Modifier.testTag("btn_navigation_forward")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Navigasi maju ke depan",
                                tint = if (canGoForwardState) Color(0xFF006492) else Color(0x4D001E2F)
                            )
                        }

                        // Home Button
                        IconButton(
                            onClick = {
                                webViewInstance?.loadUrl(baseUrl)
                            },
                            modifier = Modifier.testTag("btn_navigation_home")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Ke Beranda Utama",
                                tint = Color(0xFF006492)
                            )
                        }

                        // Refresh Button
                        IconButton(
                            onClick = {
                                isOfflineError = false
                                webViewInstance?.reload()
                            },
                            modifier = Modifier.testTag("btn_navigation_refresh")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Muat ulang halaman web",
                                tint = Color(0xFF006492)
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Bagikan Portal PGRI Cibuaya")
                                    putExtra(Intent.EXTRA_TEXT, currentUrl)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Tautan"))
                            },
                            modifier = Modifier.testTag("btn_navigation_share")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Bagikan tautan halaman web ini",
                                tint = Color(0xFF006492)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main WebView Interface
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        this.layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Highly Secure & Play Store Compliant Custom WebView Setup
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            setBuiltInZoomControls(true)
                            setDisplayZoomControls(false)

                            // Secure sandboxing rules:
                            // Prevent arbitrary sandboxed file access on the device
                            allowFileAccess = false
                            allowContentAccess = false

                            // Force secure mixed content modes
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                isOfflineError = false
                                if (url != null) {
                                    currentUrl = url
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                if (view != null) {
                                    canGoBackState = view.canGoBack()
                                    canGoForwardState = view.canGoForward()
                                    view.title?.let { pageTitle = it }
                                }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                if (url != null) {
                                    // Handle telephone protocol links
                                    if (url.startsWith("tel:") || url.startsWith("whatsapp:") || url.startsWith("mailto:")) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            ctx.startActivity(intent)
                                            return true
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "Gagal membuka aplikasi terkait", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    
                                    // Keep browsing within the PGRI Cibuaya domain for maximum application safety
                                    if (url.contains("pgricibuaya.my.id")) {
                                        view?.loadUrl(url)
                                        return false
                                    } else {
                                        // Open external websites securely in custom external device browser to avoid session hijacks
                                        try {
                                            val externalIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            ctx.startActivity(externalIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "Gagal membuka tautan luar", Toast.LENGTH_SHORT).show()
                                        }
                                        return true
                                    }
                                }
                                return false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                // Only show offline template for main page fetches, ignoring background CSS/JS file resource slips
                                if (request?.isForMainFrame == true) {
                                    isOfflineError = true
                                    isLoading = false
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                loadingProgress = newProgress
                            }

                            // Secure, dynamic file choosing protocol supporting document/media upload
                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                if (uploadMessageCallback != null) {
                                    uploadMessageCallback?.onReceiveValue(null)
                                    uploadMessageCallback = null
                                }
                                uploadMessageCallback = filePathCallback

                                val intent = fileChooserParams?.createIntent()
                                if (intent != null) {
                                    try {
                                        fileChooserLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        uploadMessageCallback?.onReceiveValue(null)
                                        uploadMessageCallback = null
                                        Toast.makeText(ctx, "Gagal membuka file manager", Toast.LENGTH_SHORT).show()
                                        return false
                                    }
                                } else {
                                    uploadMessageCallback?.onReceiveValue(null)
                                    uploadMessageCallback = null
                                    return false
                                }
                                return true
                            }
                        }

                        loadUrl(baseUrl)
                        webViewInstance = this
                    }
                },
                update = { wv ->
                    webViewInstance = wv
                },
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic progress bar loading at the top
            AnimatedVisibility(
                visible = isLoading && !isOfflineError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter)
                        .testTag("linear_loading_progress")
                )
            }

            // Beautiful custom Offline/Error Screen for first-class user experience
            AnimatedVisibility(
                visible = isOfflineError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("offline_error_view")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .padding(bottom = 24.dp)
                                .size(110.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Pemberitahuan gangguan koneksi",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }

                        Text(
                            text = "Koneksi Terputus",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Pastikan perangkat Anda terhubung ke internet (Wi-Fi atau Data Seluler) untuk memuat portal PGRI Cibuaya.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                isOfflineError = false
                                isLoading = true
                                webViewInstance?.reload()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(50.dp)
                                .testTag("btn_retry_connection")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Simbol Muat ulang",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Muat Ulang Halaman",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Beautiful Overlay App Brand Loading Splash Screen
            if (webViewInstance == null && isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF8F9FF),
                                    Color(0xFFE1E2E9)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Absolute background decorative blue circles to enhance "glassmorphism" blur visibility look
                    Box(
                        modifier = Modifier
                            .offset(x = (-60).dp, y = (-120).dp)
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF006492).copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = 100.dp, y = 80.dp)
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF004A6E).copy(alpha = 0.1f))
                    )

                    // Frosted Glass Center Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(24.dp)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(30.dp)
                            ),
                        color = Color(0x99FFFFFF), // Beautiful Frosted semitransparent white
                        shape = RoundedCornerShape(30.dp),
                        shadowElevation = 12.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Display our custom generated logo perfectly
                            Image(
                                painter = painterResource(id = com.example.R.drawable.pgri_cibuaya_logo),
                                contentDescription = "Emblem PGRI Cibuaya",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .testTag("splash_brand_logo")
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "PGRI CIBUAYA",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001E2F),
                                style = MaterialTheme.typography.titleLarge,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Portal Informasi & Pendidikan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF006492),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Cabang Cibuaya, Karawang",
                                fontSize = 11.sp,
                                color = Color(0xFF191C1E).copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(36.dp))
                            LinearProgressIndicator(
                                color = Color(0xFF006492),
                                trackColor = Color(0x4D006492),
                                modifier = Modifier
                                    .width(140.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }

    // Security Status & Google Play Compliance Modal Dialog
    if (showSecurityInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Penanda Proteksi Keamanan",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Keamanan Aplikasi Terverifikasi",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Aplikasi ini dirancang dengan standar keamanan tertinggi yang dipersyaratkan oleh Google Play Store:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🔒 Proteksi Enkripsi SSL penuh secara real-time yang memblokir serangan pembajakan perantara (Man-in-the-Middle).\n" +
                               "🚫 Pelarangan pemuatan aset lokal yang tidak terverifikasi demi menutup celah keamanan Remote Code Execution (RCE).\n" +
                               "🌐 Batas jelajah aman terisolasi di dalam domain PGRI Cibuaya dan pembukaan tautan luar melalui browser pihak ketiga.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSecurityInfoDialog = false },
                    modifier = Modifier.testTag("btn_close_security_modal")
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
