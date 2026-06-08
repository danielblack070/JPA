package com.ace.jp.japanesepractice.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ace.jp.japanesepractice.ui.collection.CollectionScreen
import com.ace.jp.japanesepractice.ui.collection.VocabularyViewModel
import com.ace.jp.japanesepractice.ui.collection.ConjugationViewModel
import com.ace.jp.japanesepractice.ui.collection.GrammarViewModel
import com.ace.jp.japanesepractice.ui.practice.PracticeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "collection") {
        composable("collection") {
            // Instantiate our local SQLite DB instance
            val database = androidx.room.Room.databaseBuilder(
                LocalContext.current,
                com.ace.jp.japanesepractice.data.AppDatabase::class.java,
                "app-database"
            ).build()

            val repository = com.ace.jp.japanesepractice.data.Repository(database.mainDao())

            // Initialize three decoupled ViewModels for separate tabs
            val vocabularyViewModel = VocabularyViewModel(repository)
            val conjugationViewModel = ConjugationViewModel(repository)
            val grammarViewModel = GrammarViewModel(repository)

            CollectionScreen(
                vocabularyViewModel = vocabularyViewModel,
                conjugationViewModel = conjugationViewModel,
                grammarViewModel = grammarViewModel
            )
        }
        composable("practice") { PracticeScreen() }
    }
}