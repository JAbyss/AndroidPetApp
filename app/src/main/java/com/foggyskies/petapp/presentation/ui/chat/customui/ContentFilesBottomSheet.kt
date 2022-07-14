package com.foggyskies.petapp.presentation.ui.chat.customui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.foggyskies.petapp.R
import com.foggyskies.petapp.presentation.ui.chat.ChatViewModel
import com.foggyskies.petapp.workers.UploadWorker
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContentFilesBottomSheet(viewModel: ChatViewModel, bottomSheetState: ModalBottomSheetState) {

    fun getSizeFile(size: Long): String {
        if (size > 8) {
            val bytes = size
            if (bytes > 1024) {
                val kBytes = bytes / 1024f
                if (kBytes > 1024) {
                    val mBytes = kBytes / 1024f
                    if (mBytes > 1024)
                        return "${String.format("%.1f", mBytes / 1024f)} GB"
                    else
                        return "${String.format("%.1f", mBytes)} MB"
                } else
                    return "${String.format("%.1f", kBytes)} KB"
            } else
                return "${String.format("%.1f", bytes)} B"
        } else if (size == 0L)
            return "0 Bit"
        else
            return "${String.format("%.1f", size)} Bit"
    }

    @Composable
    fun OneItemFile(item: String) {
        val file = File("${viewModel.selectedPath}/$item")

        val context = LocalContext.current.applicationContext

        val scope = rememberCoroutineScope()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(7.dp)
                .fillMaxWidth(0.9f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (file.isDirectory) {
                            viewModel.selectedPath += "/$item"
                            viewModel.listFiles =
                                File(viewModel.selectedPath)
                                    .list()
                                    ?.toList() ?: emptyList()
//                                    .reversed()
                        }
                        if (item == "...") {
                            viewModel.selectedPath =
                                viewModel.selectedPath.replace("\\/[^\\/]+\$".toRegex(), "")
                            viewModel.listFiles =
                                File(viewModel.selectedPath)
                                    .list()
                                    ?.toList() ?: emptyList()
                        }
                        if (file.isFile) {

                            val taskData = Data
                                .Builder()
                                .putString("nameFile", item)
                                .putString("dirFile", "${viewModel.selectedPath}/")
                                .putString("idChat", viewModel.chatEntity?.id!!)
                                .build()
                            val uploadWorkRequest: WorkRequest =
                                OneTimeWorkRequestBuilder<UploadWorker>()
                                    .setInputData(taskData)
                                    .build()
                            val man = WorkManager
                                .getInstance(context.applicationContext)
                                .enqueue(uploadWorkRequest)
                            if (man.result.isDone) {
                                Log.e("MANAGERRRR", "DOMEEEEEEEEEEEE")
                            }

                            scope.launch {
                                bottomSheetState.hide()
                                viewModel.listFiles = emptyList()
                            }
                        }
                    }
                )
        ) {
            Icon(
                painter = painterResource(id = if (file.isFile) R.drawable.ic_file else R.drawable.ic_dir),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp),
                Color(0xFFDAE0E4)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Column {
                Text(
                    text = item,
                    fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
//                val sizeFile = File("${Routes.FILE.ANDROID_DIR + DOWNLOAD_DIR}/$item").length()

                Text(
                    text = if (item == "...") "Назад" else getSizeFile(file.length()),
                    fontSize = 14.sp,
                    maxLines = 1,
                )
            }
        }
    }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(bottom = 50.dp)
            .fillMaxWidth()
            .requiredHeightIn(min = 1.dp)
            .wrapContentWidth(unbounded = false)
    ) {
        if (viewModel.selectedPath != "/storage/emulated/0")
            item {
                OneItemFile("...")
            }
        if (viewModel.listFiles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Файлы не найдены",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .padding(vertical = 30.dp)
                            .align(Center)
                    )
                }
            }
        }
//        if (viewModel.selectedPath != "${Routes.FILE.ANDROID_DIR + Routes.FILE.DOWNLOAD_DIR}"){

//        }
        items(viewModel.listFiles) { item ->
                OneItemFile(item)
        }
    }

}