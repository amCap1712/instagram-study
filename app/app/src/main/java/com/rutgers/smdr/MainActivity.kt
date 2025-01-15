package com.rutgers.smdr

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.rutgers.smdr.datastore.Request
import com.rutgers.smdr.logging.DeviceDetails
import com.rutgers.smdr.logging.TimberRemoteTree
import com.rutgers.smdr.ui.theme.WebViewDataCollectorTheme
import com.rutgers.smdr.usage.UsageCollector
import com.rutgers.smdr.usage.UsageWorker
import com.rutgers.smdr.webview.datastore.requestDataStore
import kotlinx.coroutines.delay
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import timber.log.Timber
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firestore = FirebaseFirestore.getInstance()

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val deviceDetails = DeviceDetails(deviceId)
        val remoteTree = TimberRemoteTree(firestore, deviceDetails)

        Timber.plant(remoteTree)

        val networkClient = NetworkClient()
        val userPreferencesRepository = UserPreferencesRepository(preferencesDataStore)
        val usageCollector = UsageCollector(userPreferencesRepository, networkClient)
        val geckoRuntime = GeckoRuntime.create(
            this,
            GeckoRuntimeSettings.Builder().consoleOutput(true).build()
        )

        val viewModel: MainViewModel by viewModels {
            MainViewModelFactory(
                application,
                networkClient,
                userPreferencesRepository
            )
        }

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                "recordUsage",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<UsageWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            )

        setContent {
            val counter = remember { mutableStateOf(1) }
            val lifecycleEvent = rememberLifecycleEvent()
            LaunchedEffect(lifecycleEvent) {
                if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
                    counter.value += 1
                }
            }
            LaunchedEffect(null) {
                usageCollector.recordInstagramUsage(this@MainActivity)
            }
            WebViewDataCollectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (counter.value > 0) {
                        MainScreen(
                            usageCollector = usageCollector,
                            requestDataStore = requestDataStore,
                            userPreferencesRepository = userPreferencesRepository,
                            viewModel = viewModel,
                            geckoRuntime = geckoRuntime
                        )
                    }
                }
            }
        }
    }

}


@Composable
fun MainScreen(
    usageCollector: UsageCollector,
    requestDataStore: DataStore<Request>,
    userPreferencesRepository: UserPreferencesRepository,
    viewModel: MainViewModel,
    geckoRuntime: GeckoRuntime
) {
    val context = LocalContext.current as Activity
    val onboardingStatus = viewModel.onboardingFlow().collectAsStateWithLifecycle()
    if (onboardingStatus.value == OnboardingStatus.LOADING) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Loading...", fontSize = 24.sp)
        }
    } else {
        if (usageCollector.checkPermission(context)) {
            DataCollectorNavHost(
                requestDataStore = requestDataStore,
                userPreferencesRepository = userPreferencesRepository,
                onboardingStatus = onboardingStatus.value,
                viewModel = viewModel,
                geckoRuntime = geckoRuntime
            )
            CompletionDialog(usageCollector)
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.95f)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Grant Permissions", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(text = stringResource(R.string.grant_permissions_description))
                    Text("1. Clicking on the Grant Permissions button will open the following screen listing various applications, find Social Media Data Research and click on its name.")
                    AsyncImage(
                        model = R.drawable.step1,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                    Text("2. On the next screen, press the toggle button to grant permissions.")
                    AsyncImage(
                        model = R.drawable.step2,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                    Text("3. Click on the back button on top of the screen.")
                    AsyncImage(
                        model = R.drawable.step3,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                    Text("4. Click on the back button on top of the screen again.")
                    AsyncImage(
                        model = R.drawable.step4,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                }
                Row (modifier = Modifier.fillMaxSize()) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(4.dp),
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                    ) {
                        Text(text = "Grant Permissions")
                    }
                }
            }
        }
    }
}


@Composable
fun CompletionDialog(usageCollector: UsageCollector) {
    val startTime = remember { mutableStateOf(System.currentTimeMillis()) }
    val durationElapsed = remember { mutableStateOf(0L) }
    val openAlertDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleEvent = rememberLifecycleEvent()
    LaunchedEffect(lifecycleEvent) {
        if (lifecycleEvent == Lifecycle.Event.ON_RESUME || lifecycleEvent == Lifecycle.Event.ON_PAUSE) {
            startTime.value = System.currentTimeMillis()
        }
    }
    LaunchedEffect(null) {
        while (true) {
            val usage1 = usageCollector.getCurrentAppUsage(context)
            val usage2 = durationElapsed.value + System.currentTimeMillis() - startTime.value
            val totalUsage = usage1 + usage2
            Timber.tag("UsageChecker")
                .i("CurrentUsage: %s + %s = %s", usage1, usage2, totalUsage)
            if (totalUsage <= 20 * 60 * 1000) {
                delay(1000 * 60)
            } else {
                break
            }
        }
        openAlertDialog.value = true
    }
    when {
        openAlertDialog.value -> {
            AlertDialog(
                icon = {
                    Icon(Icons.Default.Info, contentDescription = "Info")
                },
                title = {
                    Text(text = "Task Completed")
                },
                text = {
                    Text(text = "Thank you for participating in our survey. Please click on confirm to complete it.")
                },
                onDismissRequest = {},
                dismissButton = {
                    TextButton(onClick = {
                        openAlertDialog.value = false
                    }) {
                        Text("Dismiss")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://app.prolific.com/submissions/complete?cc=CAKPHYU5"))
                            context.startActivity(intent)
                            openAlertDialog.value = false
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            )
        }
    }
}

@Composable
fun rememberLifecycleEvent(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current): Lifecycle.Event {
    var state by remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            state = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    return state
}
