package com.ace.jp.japanesepractice.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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