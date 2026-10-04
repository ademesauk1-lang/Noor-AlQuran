package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppTab
import com.example.data.model.DarkModeOption
import com.example.ui.screens.MainContainerScreen
import com.example.ui.state.QuranViewModel
import com.example.ui.theme.QuranAppTheme

class MainActivity : ComponentActivity() {

  private val viewModel: QuranViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    handleIntent(intent)

    setContent {
      // Isolated read: only a change of the dark-mode setting invalidates the
      // whole content tree (previously every state change — including playback
      // updates — recomposed the entire app).
      val uiStateState = viewModel.uiState.collectAsStateWithLifecycle()
      val darkModeOption by remember(uiStateState) {
        derivedStateOf { uiStateState.value.darkModeOption }
      }
      val systemInDark = isSystemInDarkTheme()
      val isDark = when (darkModeOption) {
        DarkModeOption.DARK -> true
        DarkModeOption.LIGHT -> false
        DarkModeOption.SYSTEM -> systemInDark
      }

      QuranAppTheme(darkTheme = isDark) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainContainerScreen(viewModel = viewModel)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    when (intent?.getStringExtra(EXTRA_OPEN_TAB)) {
      OPEN_TAB_INSPIRATION -> viewModel.selectTab(AppTab.INSPIRATION)
      null -> Unit
    }
  }

  companion object {
    const val EXTRA_OPEN_TAB = "OPEN_TAB"
    const val OPEN_TAB_INSPIRATION = "INSPIRATION"
  }
}
