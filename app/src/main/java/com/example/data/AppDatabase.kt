package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(user: UserProfile)

    @Query("UPDATE users SET currentPackage = :newPackage WHERE id = 1")
    suspend fun updatePackage(newPackage: String)

    @Query("UPDATE users SET walletBalance = :newBalance WHERE id = 1")
    suspend fun updateWalletBalance(newBalance: Double)

    @Query("UPDATE users SET todayEarnings = :today, totalEarnings = :total WHERE id = 1")
    suspend fun updateEarnings(today: Double, total: Double)

    @Query("UPDATE users SET referralCount = :count WHERE id = 1")
    suspend fun updateReferralCount(count: Int)

    @Query("UPDATE users SET isOtpVerified = :verified WHERE id = 1")
    suspend fun updateOtpVerified(verified: Boolean)

    @Query("UPDATE users SET isLoggedIn = :loggedIn WHERE id = 1")
    suspend fun updateLoggedIn(loggedIn: Boolean)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY id ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Query("UPDATE courses SET isCompleted = :isCompleted, progress = :progress WHERE id = :courseId")
    suspend fun updateCourseProgress(courseId: Int, isCompleted: Boolean, progress: Float)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionItem)

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateTransactionStatus(id: Int, status: String)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

@Dao
interface ExtraStatsDao {
    @Query("SELECT * FROM extra_stats WHERE id = 1 LIMIT 1")
    fun getStats(): Flow<ExtraStats?>

    @Query("SELECT * FROM extra_stats WHERE id = 1 LIMIT 1")
    suspend fun getStatsDirect(): ExtraStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: ExtraStats)

    @Query("UPDATE extra_stats SET dailyBonusClaimedToday = :claimed, lastBonusClaimedDate = :date WHERE id = 1")
    suspend fun updateDailyBonus(claimed: Boolean, date: String)

    @Query("UPDATE extra_stats SET luckySpinsLeft = :spins WHERE id = 1")
    suspend fun updateLuckySpins(spins: Int)
}

@Database(
    entities = [
        UserProfile::class,
        Course::class,
        TransactionItem::class,
        ChatMessage::class,
        ExtraStats::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun transactionDao(): TransactionDao
    abstract fun chatDao(): ChatDao
    abstract fun extraStatsDao(): ExtraStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "learn_earn_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
