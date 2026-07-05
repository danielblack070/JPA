package com.ace.jp.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.ace.jp.app.data.AppDatabase
import com.ace.jp.app.data.MIGRATION_2_3
import com.ace.jp.app.ui.collection.CollectionScreen
import com.ace.jp.app.ui.collection.VocabularyViewModel
import com.ace.jp.app.ui.collection.ConjugationViewModel
import com.ace.jp.app.ui.collection.GrammarViewModel
import com.ace.jp.app.ui.practice.PracticeScreen
import com.ace.jp.app.ui.practice.PracticeViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Shared Single Instance Room Database setup
    val database = Room.databaseBuilder(
        LocalContext.current,
        AppDatabase::class.java,
        "app-database"
    ).addMigrations(MIGRATION_2_3)
        .fallbackToDestructiveMigration(false).build()

    val repository = com.ace.jp.app.data.Repository(database.mainDao())

    NavHost(navController = navController, startDestination = "collection") {
        composable("collection") {
            // Decoupled ViewModels for each sub-tab
            val vocabularyViewModel: VocabularyViewModel = viewModel { VocabularyViewModel(repository) }
            val conjugationViewModel: ConjugationViewModel = viewModel { ConjugationViewModel(repository) }
            val grammarViewModel: GrammarViewModel = viewModel { GrammarViewModel(repository) }
            val practiceViewModel: PracticeViewModel = viewModel { PracticeViewModel(repository) }

            CollectionScreen(
                vocabularyViewModel = vocabularyViewModel,
                conjugationViewModel = conjugationViewModel,
                grammarViewModel = grammarViewModel,
                practiceViewModel = practiceViewModel
            )
        }
        composable("practice") {
            // Initialize the practice state vm using the shared single repository
            val practiceViewModel: PracticeViewModel = viewModel { PracticeViewModel(repository) }
            PracticeScreen(viewModel = practiceViewModel)
        }
    }
}
