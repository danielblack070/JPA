package com.ace.jp.app.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ace.jp.app.data.model.Meaning
import com.google.gson.Gson
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create the new temporary table matching your new Room entity exactly
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `GrammarRule_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `isEnabled` INTEGER NOT NULL,
                `confidence` INTEGER NOT NULL,
                `lastPracticed` INTEGER,
                `englishRule` TEXT NOT NULL,
                `japaneseRule` TEXT NOT NULL,
                `readingRule` TEXT,
                `notes` TEXT
            )
        """.trimIndent())

        // 2. Transfer data. We merge 'description' and 'details' into 'notes'.
        // We handle potential NULL values gracefully using COALESCE so the whole string doesn't turn null.
        // We supply empty strings ("") for your brand-new properties (englishRule, japaneseRule) so they don't crash.
        db.execSQL("""
            INSERT INTO `GrammarRule_new` (
                `id`, `name`, `isEnabled`, `confidence`, `lastPracticed`, 
                `englishRule`, `japaneseRule`, `readingRule`, `notes`
            )
            SELECT 
                `id`, 
                `name`, 
                `isEnabled`, 
                `confidence`, 
                `lastPracticed`,
                '', -- Default empty string for the new non-null englishRule property
                '', -- Default empty string for the new non-null japaneseRule property
                NULL, -- Default null for optional readingRule
                COALESCE(`description`, '') || CHAR(10) || COALESCE(`details`, '') AS `notes`
            FROM `GrammarRule`
        """.trimIndent())

        // 3. Drop the old table
        db.execSQL("DROP TABLE `GrammarRule`")

        // 4. Rename the new table to the original name
        db.execSQL("ALTER TABLE `GrammarRule_new` RENAME TO `GrammarRule`")

        // 1. If you are rebuilding the table, you drop the old one first
        db.execSQL("DROP TABLE IF EXISTS `ExampleSentence`")

        // 2. Re-create it matching Room's EXPECTED schema perfectly
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `ExampleSentence` (
                `id` INTEGER NOT NULL, 
                `grammarRuleId` INTEGER NOT NULL, 
                `english` TEXT NOT NULL, 
                `japanese` TEXT NOT NULL, 
                `reading` TEXT, 
                PRIMARY KEY(`id`),
                FOREIGN KEY(`grammarRuleId`) REFERENCES `GrammarRule`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """)

        // 3. Re-create the index Room is expecting
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ExampleSentence_grammarRuleId` ON `ExampleSentence` (`grammarRuleId`)")
    }
}
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val gson = Gson()

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `Word_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `wordListId` INTEGER NOT NULL,
                `japanese` TEXT NOT NULL,
                `english` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `isEnabled` INTEGER NOT NULL,
                `notes` TEXT,
                `confidence` INTEGER NOT NULL,
                `lastPracticed` INTEGER,
                FOREIGN KEY(`wordListId`) REFERENCES `WordList`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())

        db.query("SELECT id, wordListId, japanese, reading, english, type, isEnabled, notes, confidence, lastPracticed FROM Word").use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val wordListId = cursor.getInt(cursor.getColumnIndexOrThrow("wordListId"))
                val japaneseText = cursor.getString(cursor.getColumnIndexOrThrow("japanese"))
                val readingIdx = cursor.getColumnIndexOrThrow("reading")
                val readingText = if (cursor.isNull(readingIdx)) null else cursor.getString(readingIdx)
                val englishText = cursor.getString(cursor.getColumnIndexOrThrow("english"))
                val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
                val isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("isEnabled"))
                val notesIdx = cursor.getColumnIndexOrThrow("notes")
                val notes = if (cursor.isNull(notesIdx)) null else cursor.getString(notesIdx)
                val confidence = cursor.getInt(cursor.getColumnIndexOrThrow("confidence"))
                val lpIdx = cursor.getColumnIndexOrThrow("lastPracticed")
                val lastPracticed = if (cursor.isNull(lpIdx)) null else cursor.getLong(lpIdx)

                val japaneseJson = gson.toJson(listOf(Meaning(text = japaneseText, pronunciation = readingText)))
                val englishJson = gson.toJson(listOf(Meaning(text = englishText)))

                db.execSQL(
                    "INSERT INTO `Word_new` (`id`, `wordListId`, `japanese`, `english`, `type`, `isEnabled`, `notes`, `confidence`, `lastPracticed`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(id, wordListId, japaneseJson, englishJson, type, isEnabled, notes, confidence, lastPracticed)
                )
            }
        }

        db.execSQL("DROP TABLE `Word`")
        db.execSQL("ALTER TABLE `Word_new` RENAME TO `Word`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Word_wordListId` ON `Word` (`wordListId`)")
    }
}
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Folder` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `parentFolderId` INTEGER,
                `sortOrder` INTEGER NOT NULL,
                FOREIGN KEY(`parentFolderId`) REFERENCES `Folder`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Folder_parentFolderId` ON `Folder` (`parentFolderId`)")

        db.execSQL("ALTER TABLE `WordList` ADD COLUMN `folderId` INTEGER REFERENCES `Folder`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE")
        db.execSQL("ALTER TABLE `WordList` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_WordList_folderId` ON `WordList` (`folderId`)")

        db.execSQL("ALTER TABLE `Word` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")

        // Initialize sequential sortOrder for existing WordLists and Words
        val listIds = mutableListOf<Int>()
        db.query("SELECT id FROM WordList ORDER BY id ASC").use { cursor ->
            while (cursor.moveToNext()) {
                listIds.add(cursor.getInt(cursor.getColumnIndexOrThrow("id")))
            }
        }
        listIds.forEachIndexed { index, listId ->
            db.execSQL("UPDATE `WordList` SET `sortOrder` = ? WHERE `id` = ?", arrayOf<Any>(index, listId))
        }

        val wordsByList = mutableMapOf<Int, MutableList<Int>>()
        db.query("SELECT id, wordListId FROM Word ORDER BY wordListId ASC, id ASC").use { cursor ->
            while (cursor.moveToNext()) {
                val wordId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val listId = cursor.getInt(cursor.getColumnIndexOrThrow("wordListId"))
                wordsByList.getOrPut(listId) { mutableListOf() }.add(wordId)
            }
        }
        wordsByList.values.forEach { ids ->
            ids.forEachIndexed { index, wordId ->
                db.execSQL("UPDATE `Word` SET `sortOrder` = ? WHERE `id` = ?", arrayOf<Any>(index, wordId))
            }
        }
    }
}