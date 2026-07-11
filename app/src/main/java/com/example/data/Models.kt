package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Only 1 active user profile for client persistence
    val name: String,
    val mobile: String,
    val email: String,
    val referralCode: String,
    val currentPackage: String, // "None", "Mini", "Pro", "Premium", "Premium Plus"
    val walletBalance: Double,
    val todayEarnings: Double,
    val totalEarnings: Double,
    val referralCount: Int,
    val rank: String,
    val joinedDate: String,
    val isOtpVerified: Boolean = false,
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val isCompleted: Boolean,
    val progress: Float, // 0f to 1f
    val videoUrl: String,
    val duration: String
)

@Entity(tableName = "transactions")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "PURCHASE" or "WITHDRAWAL" or "BONUS"
    val details: String, // package name or UPI ID / Bank
    val amount: Double,
    val txIdOrUpi: String,
    val status: String, // "Pending", "Verified", "Rejected"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "extra_stats")
data class ExtraStats(
    @PrimaryKey val id: Int = 1,
    val dailyBonusClaimedToday: Boolean = false,
    val lastBonusClaimedDate: String = "",
    val referralContestRank: Int = 12,
    val referralContestReferrals: Int = 4,
    val luckySpinsLeft: Int = 3,
    val activePromoCode: String = ""
)
