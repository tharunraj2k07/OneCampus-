package com.example.ui.components.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag

@Composable
fun OneCampusAppScaffold(
  modifier: Modifier = Modifier,
  topBar: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  snackbarHostState: SnackbarHostState? = null,
  floatingActionButton: @Composable () -> Unit = {},
  floatingActionButtonPosition: FabPosition = FabPosition.End,
  containerColor: Color = MaterialTheme.colorScheme.background,
  contentColor: Color = MaterialTheme.colorScheme.onBackground,
  contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
  testTag: String = "one_campus_app_scaffold",
  content: @Composable (PaddingValues) -> Unit
) {
  Scaffold(
    topBar = topBar,
    bottomBar = bottomBar,
    snackbarHost = {
      if (snackbarHostState != null) {
        SnackbarHost(hostState = snackbarHostState)
      }
    },
    floatingActionButton = floatingActionButton,
    floatingActionButtonPosition = floatingActionButtonPosition,
    containerColor = containerColor,
    contentColor = contentColor,
    contentWindowInsets = contentWindowInsets,
    modifier = modifier
      .fillMaxSize()
      .testTag(testTag)
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      content(innerPadding)
    }
  }
}

object ScaffoldDefaults {
  val contentWindowInsets: WindowInsets
    @Composable
    get() = androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
}
