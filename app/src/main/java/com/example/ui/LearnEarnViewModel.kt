package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class LearnEarnViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val courseDao = database.courseDao()
    private val transactionDao = database.transactionDao()
    private val chatDao = database.chatDao()
    private val extraStatsDao = database.extraStatsDao()

    // Expose Flows to UI
    val userProfile: StateFlow<UserProfile?> = userDao.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val courses: StateFlow<List<Course>> = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionItem>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val extraStats: StateFlow<ExtraStats?> = extraStatsDao.getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI Local State Helpers (for loading indicators, toast messages, etc.)
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    init {
        // Prepopulate database with default content on startup if empty
        viewModelScope.launch(Dispatchers.IO) {
            prepopulateDatabase()
        }
    }

    private suspend fun prepopulateDatabase() {
        // Check if user profile already exists
        val existingUser = userDao.getUserProfileDirect()
        if (existingUser == null) {
            // Prepopulate a high-fidelity default user profile "Ishan"
            val defaultUser = UserProfile(
                name = "Ishan",
                mobile = "9876543210",
                email = "ishan@learnearn.com",
                referralCode = "EARN99",
                currentPackage = "Mini",
                walletBalance = 450.0,
                todayEarnings = 1200.0,
                totalEarnings = 14800.0,
                referralCount = 24,
                rank = "Silver Club",
                joinedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                isOtpVerified = true,
                isLoggedIn = true
            )
            userDao.insertOrUpdateUserProfile(defaultUser)

            // Prepopulate default high-fidelity courses
            val defaultCourses = listOf(
                Course(1, "Introduction to Digital Marketing", "Digital Marketing", false, 0.4f, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", "15:24"),
                Course(2, "Instagram Algorithm Secrets", "Instagram Growth", true, 1.0f, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", "12:10"),
                Course(3, "Facebook Ads Setup & Campaigning", "Facebook Ads", false, 0.0f, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", "24:45"),
                Course(4, "High Ticket Affiliate Marketing Pro", "Affiliate Marketing", false, 0.7f, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", "18:30"),
                Course(5, "Canva Graphics Design Masterclass", "Canva Design", false, 0.1f, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", "22:15"),
                Course(6, "CapCut Video Editing Mobile Hack", "CapCut Editing", true, 1.0f, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", "11:55"),
                Course(7, "AI Tools for Automation & Scale", "AI Marketing", false, 0.0f, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", "30:40"),
                Course(8, "YouTube Algorithm Growth Blueprint", "YouTube Growth", false, 0.5f, "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4", "14:50")
            )
            courseDao.insertCourses(defaultCourses)

            // Prepopulate default recent transactions
            val defaultTransactions = listOf(
                TransactionItem(type = "PURCHASE", details = "MINI PACKAGE", amount = 399.0, txIdOrUpi = "TXN92847192", status = "Verified"),
                TransactionItem(type = "WITHDRAWAL", details = "UPI: ishan@ybl", amount = 500.0, txIdOrUpi = "WDR827419", status = "Verified"),
                TransactionItem(type = "WITHDRAWAL", details = "UPI: ishan@ybl", amount = 250.0, txIdOrUpi = "WDR102948", status = "Pending")
            )
            for (tx in defaultTransactions) {
                transactionDao.insertTransaction(tx)
            }

            // Prepopulate initial stats
            val defaultStats = ExtraStats(
                dailyBonusClaimedToday = false,
                lastBonusClaimedDate = "",
                referralContestRank = 12,
                referralContestReferrals = 4,
                luckySpinsLeft = 3
            )
            extraStatsDao.insertStats(defaultStats)

            // Add welcome AI chat message
            chatDao.insertMessage(
                ChatMessage(
                    sender = "ai",
                    message = "Welcome to LEARN & EARN Support! 👋 How can I help you build your future and grow your passive income streams today?"
                )
            )
        }
    }

    // Clear message helper
    fun clearUiMessage() {
        _uiMessage.value = null
    }

    // --- Authentication Actions ---
    fun register(name: String, mobile: String, email: String, refCode: String, onVerificationRequired: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (name.isBlank() || mobile.length < 10 || !email.contains("@")) {
                _uiMessage.value = "Please fill in all details correctly."
                return@launch
            }
            val newUser = UserProfile(
                id = 1,
                name = name,
                mobile = mobile,
                email = email,
                referralCode = if (refCode.isBlank()) "LEARN${(1000..9999).random()}" else refCode,
                currentPackage = "None",
                walletBalance = 0.0,
                todayEarnings = 0.0,
                totalEarnings = 0.0,
                referralCount = 0,
                rank = "Beginner",
                joinedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                isOtpVerified = false,
                isLoggedIn = false
            )
            userDao.insertOrUpdateUserProfile(newUser)
            withContext(Dispatchers.Main) {
                onVerificationRequired()
            }
        }
    }

    fun verifyOtp(otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (otp == "123456" || otp == "1234" || otp.length >= 4) { // Let's accept any simple OTP for excellent prototype usability
                userDao.updateOtpVerified(true)
                userDao.updateLoggedIn(true)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                _uiMessage.value = "Invalid OTP. Enter 123456 to verify."
            }
        }
    }

    fun login(mobile: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getUserProfileDirect()
            if (user != null && (user.mobile == mobile || mobile == "9876543210")) {
                userDao.updateLoggedIn(true)
                userDao.updateOtpVerified(true)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                // If user doesn't exist, we auto-create so the user never gets blocked during test
                val autoCreatedUser = UserProfile(
                    name = "Ishan",
                    mobile = mobile,
                    email = "test@learnearn.com",
                    referralCode = "EARN99",
                    currentPackage = "None",
                    walletBalance = 0.0,
                    todayEarnings = 0.0,
                    totalEarnings = 0.0,
                    referralCount = 0,
                    rank = "Beginner",
                    joinedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                    isOtpVerified = true,
                    isLoggedIn = true
                )
                userDao.insertOrUpdateUserProfile(autoCreatedUser)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.updateLoggedIn(false)
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    // --- Package purchase and Activation ---
    fun submitPackagePayment(packageName: String, amount: Double, txId: String, onPendingState: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (txId.isBlank()) {
                _uiMessage.value = "Transaction ID is required to verify your payment."
                return@launch
            }
            val newTx = TransactionItem(
                type = "PURCHASE",
                details = packageName,
                amount = amount,
                txIdOrUpi = txId,
                status = "Pending"
            )
            transactionDao.insertTransaction(newTx)
            _uiMessage.value = "Payment receipt submitted successfully! Verification pending."
            withContext(Dispatchers.Main) {
                onPendingState()
            }
        }
    }

    // --- Withdraw Actions ---
    fun submitWithdrawal(amount: Double, upiOrBank: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getUserProfileDirect() ?: return@launch
            if (amount < 200.0) {
                _uiMessage.value = "Minimum withdrawal amount is ₹200."
                return@launch
            }
            if (amount > user.walletBalance) {
                _uiMessage.value = "Insufficient wallet balance."
                return@launch
            }
            if (upiOrBank.isBlank()) {
                _uiMessage.value = "Please provide valid payment details."
                return@launch
            }

            // Deduct from wallet & record transaction
            val updatedBalance = user.walletBalance - amount
            userDao.updateWalletBalance(updatedBalance)

            val newTx = TransactionItem(
                type = "WITHDRAWAL",
                details = upiOrBank,
                amount = amount,
                txIdOrUpi = "WDR${(100000..999999).random()}",
                status = "Pending"
            )
            transactionDao.insertTransaction(newTx)
            _uiMessage.value = "Withdrawal request of ₹$amount submitted successfully!"
        }
    }

    // --- Course Operations ---
    fun updateCourseProgress(courseId: Int, progress: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val isCompleted = progress >= 1.0f
            courseDao.updateCourseProgress(courseId, isCompleted, progress.coerceIn(0f, 1f))
        }
    }

    // --- Extra Features Actions ---
    fun claimDailyBonus() {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = extraStatsDao.getStatsDirect() ?: return@launch
            val user = userDao.getUserProfileDirect() ?: return@launch
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (stats.dailyBonusClaimedToday && stats.lastBonusClaimedDate == todayDateString) {
                _uiMessage.value = "Daily Bonus already claimed today!"
                return@launch
            }

            // Claim bonus of ₹20
            val bonusAmount = 20.0
            val newBalance = user.walletBalance + bonusAmount
            userDao.updateWalletBalance(newBalance)

            val newTx = TransactionItem(
                type = "BONUS",
                details = "Daily Login Bonus",
                amount = bonusAmount,
                txIdOrUpi = "BON${(1000..9999).random()}",
                status = "Verified"
            )
            transactionDao.insertTransaction(newTx)

            extraStatsDao.updateDailyBonus(true, todayDateString)
            _uiMessage.value = "Congratulations! ₹20.00 Daily Bonus added to your wallet."
        }
    }

    fun spinLuckyWheel(onResult: (String, Double) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = extraStatsDao.getStatsDirect() ?: return@launch
            val user = userDao.getUserProfileDirect() ?: return@launch

            if (stats.luckySpinsLeft <= 0) {
                _uiMessage.value = "No lucky spins remaining today!"
                return@launch
            }

            extraStatsDao.updateLuckySpins(stats.luckySpinsLeft - 1)

            val options = listOf(
                Pair("Better Luck Next Time", 0.0),
                Pair("Earned ₹10", 10.0),
                Pair("Earned ₹50", 50.0),
                Pair("Earned ₹100 Premium Voucher", 0.0),
                Pair("Earned ₹5 Login Reward", 5.0),
                Pair("Earned ₹150 Cashback", 150.0)
            )
            val win = options.random()

            if (win.second > 0.0) {
                userDao.updateWalletBalance(user.walletBalance + win.second)
                val newTx = TransactionItem(
                    type = "BONUS",
                    details = "Lucky Spin Reward: ${win.first}",
                    amount = win.second,
                    txIdOrUpi = "SPN${(10000..99999).random()}",
                    status = "Verified"
                )
                transactionDao.insertTransaction(newTx)
            }

            withContext(Dispatchers.Main) {
                onResult(win.first, win.second)
            }
        }
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getUserProfileDirect() ?: return@launch
            if (code.uppercase() == "EARN50") {
                val reward = 50.0
                userDao.updateWalletBalance(user.walletBalance + reward)
                val newTx = TransactionItem(
                    type = "BONUS",
                    details = "Promo Coupon Code: EARN50",
                    amount = reward,
                    txIdOrUpi = "CPN${(10000..99999).random()}",
                    status = "Verified"
                )
                transactionDao.insertTransaction(newTx)
                _uiMessage.value = "Promo Code Applied! ₹50.00 cash bonus added to your wallet."
            } else {
                _uiMessage.value = "Invalid or expired Coupon Code."
            }
        }
    }

    // --- AI Chat Actions ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            // Save user message
            val userMsg = ChatMessage(sender = "user", message = text)
            chatDao.insertMessage(userMsg)

            _isGeneratingAi.value = true

            // Gather past messages
            val history = chatDao.getAllMessages().first().takeLast(10)

            // Get response from Gemini
            val reply = GeminiService.generateSupportResponse(text, history)

            val aiMsg = ChatMessage(sender = "ai", message = reply)
            chatDao.insertMessage(aiMsg)

            _isGeneratingAi.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.clearChat()
            chatDao.insertMessage(
                ChatMessage(
                    sender = "ai",
                    message = "Chat history cleared. How can I help you support your educational and affiliate goals today?"
                )
            )
        }
    }

    // --- Admin Operations (MOCKED BUT FULLY ACTIVE AT RUNTIME!) ---
    fun adminApproveTransaction(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.updateTransactionStatus(id, "Verified")
            // If it's a package purchase, activate it for the user
            val tx = transactions.value.find { it.id == id }
            if (tx != null && tx.type == "PURCHASE") {
                val cleanPackageName = when (tx.details.uppercase()) {
                    "MINI PACKAGE" -> "Mini"
                    "PRO PACKAGE" -> "Pro"
                    "PREMIUM PACKAGE" -> "Premium"
                    "PREMIUM PLUS" -> "Premium Plus"
                    else -> "Pro"
                }
                userDao.updatePackage(cleanPackageName)

                // Give dynamic referral commission simulated flow!
                val user = userDao.getUserProfileDirect()
                if (user != null) {
                    val commissionAmount = when (cleanPackageName) {
                        "Mini" -> 150.0
                        "Pro" -> 300.0
                        "Premium" -> 500.0
                        "Premium Plus" -> 700.0
                        else -> 0.0
                    }
                    userDao.updateEarnings(
                        today = user.todayEarnings + commissionAmount,
                        total = user.totalEarnings + commissionAmount
                    )
                }
            }
            _uiMessage.value = "Transaction verified & approved successfully!"
        }
    }

    fun adminRejectTransaction(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.updateTransactionStatus(id, "Rejected")
            _uiMessage.value = "Transaction rejected!"
        }
    }

    fun adminAddCourse(title: String, category: String, duration: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = courses.value
            val nextId = (list.maxOfOrNull { it.id } ?: 0) + 1
            val newCourse = Course(
                id = nextId,
                title = title,
                category = category,
                isCompleted = false,
                progress = 0f,
                videoUrl = "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4",
                duration = duration
            )
            courseDao.insertCourses(listOf(newCourse))
            _uiMessage.value = "New course added successfully to database!"
        }
    }

    fun resetDemoData() {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.clearUser()
            transactionDao.clearTransactions()
            chatDao.clearChat()
            prepopulateDatabase()
            _uiMessage.value = "Demo database state reset successfully!"
        }
    }
}
