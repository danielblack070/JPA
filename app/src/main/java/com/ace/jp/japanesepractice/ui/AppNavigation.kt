package com.ace.jp.japanesepractice.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ace.jp.japanesepractice.ui.collection.CollectionScreen
import com.ace.jp.japanesepractice.ui.practice.PracticeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "collection") {
        composable("collection") {
            // Fetch database and viewmodel instances normally
            // This is a minimal example of passing the viewmodel
            val database = androidx.room.Room.databaseBuilder(
                androidx.compose.ui.platform.LocalContext.current,
                com.ace.jp.japanesepractice.data.AppDatabase::class.java,
                "app-database"
            ).build()
            val repository = com.ace.jp.japanesepractice.data.Repository(database.mainDao())
            val viewModel = com.ace.jp.japanesepractice.ui.collection.CollectionViewModel(repository)
            CollectionScreen(viewModel)
        }
        composable("practice") { PracticeScreen() }
    }
}
