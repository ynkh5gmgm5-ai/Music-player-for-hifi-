package com.yuandao.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.yuandao.music.ui.LibraryViewModel
import com.yuandao.music.ui.YuandaoMusicApp
import com.yuandao.music.ui.theme.YuandaoTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: LibraryViewModel

    private val audioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            if (grants.values.any { it }) {
                viewModel.scanMediaStore()
            }
        }

    private val safFolderLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            viewModel.scanSafFolder(uri, displayName = uri.lastPathSegment)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as YuandaoApp).container
        viewModel = ViewModelProvider(
            this,
            LibraryViewModel.Factory(container),
        )[LibraryViewModel::class.java]

        setContent {
            YuandaoTheme {
                YuandaoMusicApp(
                    viewModel = viewModel,
                    onRequestScan = ::requestAudioScan,
                    onRequestSafFolder = { safFolderLauncher.launch(null) },
                )
            }
        }
    }

    private fun requestAudioScan() {
        val permissions = audioPermissions()
        if (permissions.all { permission ->
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            viewModel.scanMediaStore()
        } else {
            audioPermissionLauncher.launch(permissions)
        }
    }

    private fun audioPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
}
