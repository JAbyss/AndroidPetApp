package com.foggyskies.petapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.room.Room
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.foggyskies.petapp.MainActivity.Companion.IDUSER
import com.foggyskies.petapp.MainActivity.Companion.TOKEN
import com.foggyskies.petapp.MainActivity.Companion.USERNAME
import com.foggyskies.petapp.MainActivity.Companion.isNetworkAvailable
import com.foggyskies.petapp.PushNotificationService.Companion.ISAPPLIFE
import com.foggyskies.petapp.PushNotificationService.Companion.notificationsList
import com.foggyskies.petapp.domain.db.ChatDB
import com.foggyskies.petapp.domain.repository.RepositoryChatDB
import com.foggyskies.petapp.network.ConnectionLiveData
import com.foggyskies.petapp.presentation.ui.adhomeless.AdsHomelessScreen
import com.foggyskies.petapp.presentation.ui.adhomeless.AdsHomelessViewModel
import com.foggyskies.petapp.presentation.ui.chat.ChatScreen
import com.foggyskies.petapp.presentation.ui.chat.ChatViewModel
import com.foggyskies.petapp.presentation.ui.globalviews.FormattedChatDC
import com.foggyskies.petapp.presentation.ui.home.HomeMVIModel
import com.foggyskies.petapp.presentation.ui.home.HomeScreen
import com.foggyskies.petapp.presentation.ui.navigationtree.NavTree
import com.foggyskies.petapp.presentation.ui.profile.human.ProfileScreen
import com.foggyskies.petapp.presentation.ui.profile.human.ProfileViewModel
import com.foggyskies.petapp.presentation.ui.registation.AuthorizationViewModel
import com.foggyskies.petapp.presentation.ui.splash.SplashScreen
import com.foggyskies.testingscrollcompose.presentation.ui.registation.AuthorizationScreen
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

enum class DBs {
    Chat
}

val mainModule = module {
    single(named(DBs.Chat)) {
        Room.databaseBuilder(
            androidContext(),
            ChatDB::class.java,
            "chat-db"
        ).build()
    }
    single {
        RepositoryChatDB(get(named(DBs.Chat)))
    }
}

class MainActivity : ComponentActivity() {
    private val MY_PERMISSIONS_REQUEST = 1234
    private val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        arrayOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        )
    } else {
        arrayOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
//    private val PERMISSIONS_OLD_API =

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ISAPPLIFE = true

        requestPermissions(PERMISSIONS, MY_PERMISSIONS_REQUEST)
        try{

            startKoin {
                androidContext(this@MainActivity)
                modules(listOf(mainModule))
            }
        }catch (_: java.lang.Exception){

        }
        loader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .crossfade(true)
            .diskCache {
                DiskCache.Builder()
                    .directory(File("/storage/emulated/0/RusLan/Images/"))
                    .maxSizePercent(0.10)
                    .build()
            }
            .build()
//        if (!LIFESERVICE){
//            Intent(this, PushNotificationService::class.java).also {
//                startService(it)
//                super.onPause()
//            }
//            Log.e("SERVISE STARTED", "dsssssssssssssss $LIFESERVICE")
//        }
//        val splashviewModel = SplashScreenViewModel()
//
//        installSplashScreen()
//            .setKeepOnScreenCondition{
//            !splashviewModel.isLoading
//        }


//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        val downloadDir =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//
//        downloadDir.path
//        if (downloadDir.canWrite()) {
//            val dbFileName =
//            val src = FileInputStream(applicationContext.getDatabasePath(DbName.DATABASE_NAME))
//            val dst = FileOutputStream(File(downloadDir, dbFileName))
//            src.copyTo(dst)
//            src.close()
//            dst.close()
//        }
//        MediaStore.Downloads.
//        var directory = "/storage/emulated/0"
//        val originString = "$directory/RusLan"
//        val path = Paths.get(originString)
//        val file = File(originString)
//        if (!Files.exists(path)) {
//            file.mkdirs()
//        }
//        val readyPath = "$originString/image_12_123.png"
//        File(readyPath).writeText("awfawfawf")

        setContent {
            val context = LocalContext.current

            val connection_live_data = ConnectionLiveData(context)
            isNetworkAvailable = connection_live_data.observeAsState(false)
            Surface(color = MaterialTheme.colors.background) {
                LoadingApp()
            }
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    fun onClick() {
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ISAPPLIFE = false
        Log.e("FLAGGGGGGG", "SETTTTTTTTTTTTTTTTTTTTTTT $ISAPPLIFE")
    }


    companion object {
        var USERNAME = ""
        var TOKEN = ""
        var IDUSER = ""
        lateinit var loader: ImageLoader

        /**        194.67.93.244:8089
        192.168.0.28

        26.228.47.11

        192.168.0.88:2525
         */

        val MAINENDPOINT = "192.168.0.88:2525"
        lateinit var isNetworkAvailable: State<Boolean>
    }
}

@SuppressLint("SuspiciousIndentation")
@ExperimentalMaterialApi
@Composable
fun LoadingApp() {
    val uri = "https://www.example.com"
    val nav_controller = rememberNavController()

    val context = LocalContext.current

    val window: Window = (context as Activity).window
    val statusBarColor: Int = Color(0xFFC2C8CC).toArgb()
    window.statusBarColor = statusBarColor


    val db = Room.databaseBuilder(
        context.applicationContext,
        ChatDB::class.java,
        "chat-db"
    ).build()
    val chatDao = db.chatDao()


    val viewModelProvider = ViewModelProvider(context as ComponentActivity)

    val mainSocketViewModel =
        viewModelProvider["MainSocketViewModel", (MainSocketViewModel::class.java)]

    mainSocketViewModel.chatDao = chatDao

    SideEffect {
        Log.e("УТЕЧКА", "УРОВЕНЬ АКТИВИТИ")
    }

    var isHomeLoaded by remember {
        mutableStateOf(false)
    }

    NavHost(navController = nav_controller, startDestination = NavTree.Splash.name) {
        composable(NavTree.Splash.name) {
            SplashScreen(nav_controller = nav_controller)
        }
        composable(NavTree.Authorization.name) {
            val viewModel =
                viewModelProvider["AuthorizationViewModel", (AuthorizationViewModel::class.java)]

            AuthorizationScreen(nav_controller, viewModel)
        }
        composable(NavTree.Home.name) {
//            val viewModel = viewModelProvider["HomeViewModel", (HomeViewModel::class.java)]
            val viewModel = viewModelProvider["HomeViewModel", (HomeMVIModel::class.java)]
//            val viewModel: HomeMVIModel = HomeMVIModel()

            SideEffect {
                Log.e(
                    "УТЕЧКА",
                    "УРОВЕНЬ НАВИГАЦИЯ АКТИВИТ\n\n${mainSocketViewModel.mainSocket.toString()}\n\n"
//                        "\n${viewModel.selectedPost.toString()}" +
//                        "\n${viewModel.listContents.toString()}"
                )

            }

            if (mainSocketViewModel.mainSocket == null && isNetworkAvailable.value)
                mainSocketViewModel.createMainSocket()
//            if (!isHomeLoaded){
//                isHomeLoaded = true
                viewModel.HomeScreen(nav_controller, mainSocketViewModel)
//            }
        }
        composable("AdsHomeless") {
            val viewModel =
                viewModelProvider["AdsHomelessViewModel", (AdsHomelessViewModel::class.java)]

            AdsHomelessScreen(nav_controller, viewModel = viewModel)
        }
        composable(
            NavTree.ChatSec.name
        ) {

            val str = it.arguments?.getString("itemChat") as String
            val item = Json.decodeFromString<FormattedChatDC>(str)

            val viewModel = viewModelProvider["ChatViewModel", (ChatViewModel::class.java)]
            viewModel.db = db
            ChatScreen(viewModel, item)
        }
        composable(
            "Chat/{itemChat}",
            arguments = listOf(navArgument("itemChat") {
                type = NavType.StringType
            }),
            deepLinks = listOf(navDeepLink { uriPattern = "$uri/itemChat={itemChat}token={token}" })
        ) {
            val token = it.arguments?.getString("token")
            if (token != null) {
                TOKEN = token
                USERNAME = context.getSharedPreferences(
                    "User",
                    Context.MODE_PRIVATE
                ).getString("username", "").toString()
                mainSocketViewModel.sendAction("deleteAllSentNotifications|")
                notificationsList = mutableListOf()
            }

            val str = it.arguments?.getString("itemChat") as String
            val item = Json.decodeFromString<FormattedChatDC>(str)

            val viewModel = viewModelProvider["ChatViewModel", (ChatViewModel::class.java)]
            ChatScreen(viewModel, item)
        }
        composable("Profile") {
            val viewModel =
                viewModelProvider["ProfileViewModel", (ProfileViewModel::class.java)]

            LaunchedEffect(key1 = Unit) {
                val isOwnerMode = it.arguments?.getBoolean("mode", true) ?: true
                val username = it.arguments?.getString("username", USERNAME)!!
                val image = it.arguments?.getString("image", "")!!
                val idUser = it.arguments?.getString("idUser", IDUSER)!!
                viewModel.stateUserProfile(username, image, idUser, isOwnerMode)
            }
            ProfileScreen(
                nav_controller = nav_controller,
                viewModel,
                mainSocketViewModel
            )
        }
    }
}
