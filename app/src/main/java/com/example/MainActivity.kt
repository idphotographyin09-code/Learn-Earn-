package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.data.*
import com.example.ui.LearnEarnViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

// Global Nav Routes
object Routes {
    const val LANDING = "landing"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val OTP = "otp"
    const val DASHBOARD = "dashboard"
    const val BUY_PACKAGE = "buy_package"
    const val PAYMENT = "payment"
    const val LEARN = "learn"
    const val EARNINGS = "earnings"
    const val REFERRALS = "referrals"
    const val PROFILE = "profile"
    const val ADMIN = "admin"
    const val CHATBOT = "chatbot"
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val viewModel: LearnEarnViewModel = viewModel()
    val navController = rememberNavController()
    val userProfile by viewModel.userProfile.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    // Handle toast alerts from ViewModel
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUiMessage()
        }
    }

    // Determine currently selected navigation item
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.LANDING

    // Screen routes where the Bottom Navigation Bar should be shown
    val bottomNavRoutes = listOf(
        Routes.DASHBOARD,
        Routes.LEARN,
        Routes.EARNINGS,
        Routes.REFERRALS,
        Routes.PROFILE
    )
    val shouldShowBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        },
        floatingActionButton = {
            // Globally requested WhatsApp Support FAB at bottom right
            WhatsAppSupportFAB(context = context)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LANDING,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LANDING) {
                LandingScreen(navController, userProfile)
            }
            composable(Routes.LOGIN) {
                LoginScreen(navController, viewModel)
            }
            composable(Routes.REGISTER) {
                RegisterScreen(navController, viewModel)
            }
            composable(Routes.OTP) {
                OtpScreen(navController, viewModel)
            }
            composable(Routes.DASHBOARD) {
                DashboardScreen(navController, viewModel)
            }
            composable(Routes.BUY_PACKAGE) {
                BuyPackageScreen(navController)
            }
            composable(Routes.PAYMENT + "/{packageName}/{price}") { backStackEntry ->
                val pkgName = backStackEntry.arguments?.getString("packageName") ?: "PRO PACKAGE"
                val price = backStackEntry.arguments?.getString("price")?.toDoubleOrNull() ?: 699.0
                PaymentScreen(navController, viewModel, pkgName, price)
            }
            composable(Routes.LEARN) {
                LearningDashboardScreen(viewModel)
            }
            composable(Routes.EARNINGS) {
                EarningsDashboardScreen(navController, viewModel)
            }
            composable(Routes.REFERRALS) {
                AffiliateDashboardScreen(viewModel)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(navController, viewModel)
            }
            composable(Routes.ADMIN) {
                AdminPanelScreen(navController, viewModel)
            }
            composable(Routes.CHATBOT) {
                ChatbotScreen(viewModel)
            }
        }
    }
}

// Bottom Navigation Component
@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String) {
    NavigationBar(
        containerColor = Color(0xFF18243C),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.DASHBOARD,
            onClick = { if (currentRoute != Routes.DASHBOARD) navController.navigate(Routes.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color(0xFFB7C1D1),
                unselectedTextColor = Color(0xFFB7C1D1),
                indicatorColor = Color(0xFF0B5FFF)
            ),
            modifier = Modifier.testTag("nav_home")
        )
        NavigationBarItem(
            selected = currentRoute == Routes.LEARN,
            onClick = { if (currentRoute != Routes.LEARN) navController.navigate(Routes.LEARN) },
            icon = { Icon(Icons.Default.School, contentDescription = "Learn") },
            label = { Text("Learn", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color(0xFFB7C1D1),
                unselectedTextColor = Color(0xFFB7C1D1),
                indicatorColor = Color(0xFF0B5FFF)
            ),
            modifier = Modifier.testTag("nav_learn")
        )
        NavigationBarItem(
            selected = currentRoute == Routes.EARNINGS,
            onClick = { if (currentRoute != Routes.EARNINGS) navController.navigate(Routes.EARNINGS) },
            icon = { Icon(Icons.Default.Payments, contentDescription = "Earnings") },
            label = { Text("Earnings", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color(0xFFB7C1D1),
                unselectedTextColor = Color(0xFFB7C1D1),
                indicatorColor = Color(0xFF0B5FFF)
            ),
            modifier = Modifier.testTag("nav_earnings")
        )
        NavigationBarItem(
            selected = currentRoute == Routes.REFERRALS,
            onClick = { if (currentRoute != Routes.REFERRALS) navController.navigate(Routes.REFERRALS) },
            icon = { Icon(Icons.Default.Share, contentDescription = "Referrals") },
            label = { Text("Refer", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color(0xFFB7C1D1),
                unselectedTextColor = Color(0xFFB7C1D1),
                indicatorColor = Color(0xFF0B5FFF)
            ),
            modifier = Modifier.testTag("nav_refer")
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = { if (currentRoute != Routes.PROFILE) navController.navigate(Routes.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFC107),
                selectedTextColor = Color(0xFFFFC107),
                unselectedIconColor = Color(0xFFB7C1D1),
                unselectedTextColor = Color(0xFFB7C1D1),
                indicatorColor = Color(0xFF0B5FFF)
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}

// Globally requested WhatsApp Support FAB pointing to +917384091833
@Composable
fun WhatsAppSupportFAB(context: Context) {
    FloatingActionButton(
        onClick = {
            try {
                val url = "https://api.whatsapp.com/send?phone=+917384091833&text=Hello%20Learn%20and%20Earn%20Support%20Team!%20Need%20some%20help."
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp Support: +917384091833", Toast.LENGTH_LONG).show()
            }
        },
        containerColor = Color(0xFF25D366),
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
            .padding(bottom = 16.dp, end = 12.dp)
            .testTag("whatsapp_support_fab")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "WhatsApp Chat",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Support", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

// ------------------ SCREEN: LANDING SCREEN ------------------
@Composable
fun LandingScreen(navController: NavHostController, userProfile: UserProfile?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F)),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Section with dynamic background
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0B5FFF), Color(0xFF0A0F1F))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "OFFICIAL PLATFORM",
                            color = Color(0xFF0A0F1F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "LEARN & EARN",
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Learn Skills. Build Income. Grow Your Future.",
                        color = Color(0xFFB7C1D1),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                if (userProfile?.isLoggedIn == true) {
                                    navController.navigate(Routes.DASHBOARD)
                                } else {
                                    navController.navigate(Routes.LOGIN)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5FFF)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_start_learning")
                        ) {
                            Text("Start Learning", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { navController.navigate(Routes.BUY_PACKAGE) },
                            border = BorderStroke(1.5.dp, Color(0xFFFFC107)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_buy_package")
                        ) {
                            Text("Buy Package", fontWeight = FontWeight.Bold, color = Color(0xFFFFC107))
                        }
                    }
                }
            }
        }

        // Demo Watch Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Watch Demo Presentation",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "See how everyday learners are building robust passive income channels inside 30 days.",
                        color = Color(0xFFB7C1D1),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Mock player card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A0F1F))
                            .clickable {
                                // Launch interactive video simulation toast
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play Video",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Click to Watch Presentation (3:45)", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: Why Choose Learn & Earn
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Why Choose Learn & Earn?",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                val items = listOf(
                    Triple(Icons.Default.Verified, "Trusted Platform", "100% verified payouts and safe digital course delivery to over 50,000+ active affiliates globally."),
                    Triple(Icons.Default.School, "Digital Marketing Course", "Industry-grade courses designed by real experts to teach Instagram growth, Canva designs, and ads."),
                    Triple(Icons.Default.CurrencyExchange, "Affiliate Income Blueprint", "High commission margins. Get up to 70% direct affiliate commission instantly credited to your wallet."),
                    Triple(Icons.Default.SupportAgent, "Live Mentorship & Support", "Direct support channels, premium video guides, and automated AI assistance 24/7.")
                )

                items.forEach { (icon, title, desc) ->
                    Row(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B5FFF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF0B5FFF))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(desc, color = Color(0xFFB7C1D1), fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }

        // Section: Packages Comparison Table
        item {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                Text(
                    "Package Comparison",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0B5FFF).copy(alpha = 0.2f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Package", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Price", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                            Text("Commissions", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                        }
                        Divider(color = Color(0xFF233554))
                        listOf(
                            Triple("MINI", "₹399", "₹150 Per Sale"),
                            Triple("PRO", "₹699", "₹300 Per Sale"),
                            Triple("PREMIUM", "₹999", "₹500 Per Sale"),
                            Triple("PREMIUM PLUS", "₹1299", "₹700 Per Sale")
                        ).forEach { (name, price, commission) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(price, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                                Text(commission, color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                            }
                            Divider(color = Color(0xFF233554).copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Section: Success Stories
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Success Stories",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        listOf(
                            Triple("Rahul S.", "Earning ₹45,000+", "Learn & Earn changed my career! The Instagram Growth course has premium videos that really work."),
                            Triple("Anjali Sharma", "Earning ₹80,000+", "Highly support-oriented platform. Direct support on WhatsApp helped me verify my packages in 5 minutes."),
                            Triple("Vikram Patel", "Earning ₹1,20,000+", "I am in the Gold Rank and withdrawing commission to my UPI. Highly trusted platform!")
                        )
                    ) { (name, earnings, review) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                            modifier = Modifier
                                .width(260.dp)
                                .height(160.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(earnings, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(review, color = Color(0xFFB7C1D1), fontSize = 11.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // Section: FAQs
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("Frequently Asked Questions", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    "Is this platform safe for beginners?" to "Absolutely! We focus on basic learning with simple, step-by-step videos and dynamic mock support chat to help you get started.",
                    "What is the minimum withdrawal amount?" to "The minimum withdrawal is ₹200. You can request direct payouts to UPI or Bank Account.",
                    "How fast is payment verification?" to "Payment verification takes between 5 to 30 minutes. Once verified, your course and affiliate panels are fully activated."
                ).forEach { (q, a) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF11192E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(q, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(a, color = Color(0xFFB7C1D1), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section: Contact Support Details
        item {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(color = Color(0xFF233554))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Need Help?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Contact our official support lines:", color = Color(0xFFB7C1D1), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = Color(0xFF0B5FFF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("support@learnearn.com", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+91 7384091833", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ------------------ SCREEN: AUTH LOGIN SCREEN ------------------
@Composable
fun LoginScreen(navController: NavHostController, viewModel: LearnEarnViewModel) {
    val context = LocalContext.current
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = "Logo",
            tint = Color(0xFF0B5FFF),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Welcome Back", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("Login to your Learn & Earn Account", color = Color(0xFFB7C1D1), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B5FFF),
                unfocusedBorderColor = Color(0xFF233554),
                focusedLabelColor = Color(0xFF0B5FFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedLabelColor = Color(0xFFB7C1D1)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("username_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B5FFF),
                unfocusedBorderColor = Color(0xFF233554),
                focusedLabelColor = Color(0xFF0B5FFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedLabelColor = Color(0xFFB7C1D1)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0B5FFF))
                )
                Text("Remember Me", color = Color(0xFFB7C1D1), fontSize = 13.sp)
            }
            Text(
                "Forgot Password?",
                color = Color(0xFFFFC107),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Password reset link sent to mobile number.", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.login(mobile, password) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5FFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("login_button")
        ) {
            Text("Login", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google sign-in design integration
        OutlinedButton(
            onClick = {
                // Mock success Google signup for fast testing
                viewModel.login("9876543210", "123456") {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                }
            },
            border = BorderStroke(1.dp, Color(0xFF233554)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Login, contentDescription = "Google", tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Login with Google", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Text("Don't have an account? ", color = Color(0xFFB7C1D1), fontSize = 14.sp)
            Text(
                "Create Account",
                color = Color(0xFFFFC107),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { navController.navigate(Routes.REGISTER) }
            )
        }
    }
}

// ------------------ SCREEN: AUTH REGISTER SCREEN ------------------
@Composable
fun RegisterScreen(navController: NavHostController, viewModel: LearnEarnViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("Start your Learning & Earning journey today", color = Color(0xFFB7C1D1), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B5FFF),
                unfocusedBorderColor = Color(0xFF233554),
                focusedLabelColor = Color(0xFF0B5FFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedLabelColor = Color(0xFFB7C1D1)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B5FFF),
                unfocusedBorderColor = Color(0xFF233554),
                focusedLabelColor = Color(0xFF0B5FFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedLabelColor = Color(0xFFB7C1D1)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B5FFF),
                unfocusedBorderColor = Color(0xFF233554),
                focusedLabelColor = Color(0xFF0B5FFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedLabelColor = Color(0xFFB7C1D1)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B5FFF),
                unfocusedBorderColor = Color(0xFF233554),
                focusedLabelColor = Color(0xFF0B5FFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedLabelColor = Color(0xFFB7C1D1)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B5FFF),
                unfocusedBorderColor = Color(0xFF233554),
                focusedLabelColor = Color(0xFF0B5FFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedLabelColor = Color(0xFFB7C1D1)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = referralCode,
            onValueChange = { referralCode = it },
            label = { Text("Referral Code (Optional)") },
            leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = "Referral") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B5FFF),
                unfocusedBorderColor = Color(0xFF233554),
                focusedLabelColor = Color(0xFF0B5FFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedLabelColor = Color(0xFFB7C1D1)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = acceptTerms,
                onCheckedChange = { acceptTerms = it },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0B5FFF))
            )
            Text(
                "I accept the terms and community rules.",
                color = Color(0xFFB7C1D1),
                fontSize = 12.sp,
                modifier = Modifier.clickable { acceptTerms = !acceptTerms }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!acceptTerms) {
                    Toast.makeText(context, "Please accept the terms.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.register(name, mobile, email, referralCode) {
                    navController.navigate(Routes.OTP)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5FFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text("Already have an account? ", color = Color(0xFFB7C1D1), fontSize = 14.sp)
            Text(
                "Login",
                color = Color(0xFFFFC107),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { navController.navigate(Routes.LOGIN) }
            )
        }
    }
}

// ------------------ SCREEN: AUTH OTP VERIFICATION SCREEN ------------------
@Composable
fun OtpScreen(navController: NavHostController, viewModel: LearnEarnViewModel) {
    val context = LocalContext.current
    var otp by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Sms,
            contentDescription = "OTP",
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("OTP Verification", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We have sent a 4-digit verification code to your mobile number. Enter code below to activate your profile.",
            color = Color(0xFFB7C1D1),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it },
            label = { Text("Enter OTP Code") },
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                textAlign = TextAlign.Center,
                color = Color.White,
                letterSpacing = 8.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B5FFF),
                unfocusedBorderColor = Color(0xFF233554),
                focusedLabelColor = Color(0xFF0B5FFF),
                unfocusedLabelColor = Color(0xFFB7C1D1)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .width(200.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Hint: Enter 1234 to verify", color = Color(0xFFFFC107), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.verifyOtp(otp) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LANDING) { inclusive = true }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5FFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Verify & Continue", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Resend Code",
            color = Color(0xFFB7C1D1),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                Toast.makeText(context, "OTP code resent successfully.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ------------------ SCREEN: DASHBOARD ------------------
@Composable
fun DashboardScreen(navController: NavHostController, viewModel: LearnEarnViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val extraStats by viewModel.extraStats.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showSpinModal by remember { mutableStateOf(false) }
    var spinResultMsg by remember { mutableStateOf("") }
    var spinRotation by remember { mutableStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F)),
        contentPadding = PaddingValues(18.dp)
    ) {
        // Welcome Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome ${userProfile?.name ?: "Ishan"} 👋",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Your custom learning paths are ready",
                        color = Color(0xFFB7C1D1),
                        fontSize = 13.sp
                    )
                }

                // AI Chatbot Quick Icon Entry
                IconButton(
                    onClick = { navController.navigate(Routes.CHATBOT) },
                    modifier = Modifier
                        .background(Color(0xFF0B5FFF), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Support Chat",
                        tint = Color.White
                    )
                }
            }
        }

        // Stats Card Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Wallet row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Wallet Balance", color = Color(0xFFB7C1D1), fontSize = 12.sp)
                            Text(
                                "₹${String.format("%.2f", userProfile?.walletBalance ?: 450.0)}",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { navController.navigate(Routes.EARNINGS) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Withdraw", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider(color = Color(0xFF233554))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Three columns of earnings stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Today's", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text(
                                "₹${String.format("%.0f", userProfile?.todayEarnings ?: 1200.0)}",
                                color = Color(0xFF00C853),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Total", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text(
                                "₹${String.format("%.0f", userProfile?.totalEarnings ?: 14800.0)}",
                                color = Color(0xFF0B5FFF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Referrals", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text(
                                "${userProfile?.referralCount ?: 24} Joins",
                                color = Color(0xFFFFC107),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Rank", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text(
                                userProfile?.rank ?: "Silver Club",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Package Highlight
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11192E)),
                border = BorderStroke(1.dp, Color(0xFF233554)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "Pkg", tint = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Active Plan", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text(
                                text = (userProfile?.currentPackage?.uppercase() ?: "MINI") + " PACKAGE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { navController.navigate(Routes.BUY_PACKAGE) },
                        border = BorderStroke(1.dp, Color(0xFF0B5FFF)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Upgrade", fontSize = 11.sp, color = Color(0xFF0B5FFF))
                    }
                }
            }
        }

        // Promo Code Applied Quick Action
        item {
            var promoCodeInput by remember { mutableStateOf("") }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promoCodeInput,
                        onValueChange = { promoCodeInput = it },
                        placeholder = { Text("Coupon EARN50", fontSize = 12.sp, color = Color(0xFFB7C1D1)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0B5FFF),
                            unfocusedBorderColor = Color(0xFF233554),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.applyCoupon(promoCodeInput)
                            promoCodeInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Apply", color = Color(0xFF0A0F1F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Section: Quick Cards Matrix (Requested Items)
        item {
            Text(
                "Quick Hub",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 14.dp, bottom = 10.dp)
            )
        }

        // Matrix Grid of Quick cards
        item {
            Column {
                val itemsRow1 = listOf(
                    Triple("My Courses", Icons.Default.School, Routes.LEARN),
                    Triple("Buy Package", Icons.Default.Storefront, Routes.BUY_PACKAGE)
                )
                val itemsRow2 = listOf(
                    Triple("Invite Friends", Icons.Default.Share, Routes.REFERRALS),
                    Triple("Daily Bonus", Icons.Default.Celebration, "BONUS")
                )
                val itemsRow3 = listOf(
                    Triple("Withdraw Fund", Icons.Default.AccountBalance, Routes.EARNINGS),
                    Triple("Lucky Wheel", Icons.Default.Games, "SPIN")
                )

                listOf(itemsRow1, itemsRow2, itemsRow3).forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (title, icon, action) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clickable {
                                        when (action) {
                                            "BONUS" -> viewModel.claimDailyBonus()
                                            "SPIN" -> showSpinModal = true
                                            else -> navController.navigate(action)
                                        }
                                    }
                                    .testTag("quick_card_${title.lowercase().replace(" ", "_")}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        tint = if (title == "Daily Bonus" || title == "Lucky Wheel") Color(0xFFFFC107) else Color(0xFF0B5FFF),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Referrals Contest Showcase
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11192E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Contest", tint = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active Referral Contest", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Your current contest rank is #${extraStats?.referralContestRank ?: 12} with ${extraStats?.referralContestReferrals ?: 4} package sales this week.",
                        color = Color(0xFFB7C1D1),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    // Interactive Lucky Spin Modal overlay
    if (showSpinModal) {
        AlertDialog(
            onDismissRequest = { if (!isSpinning) showSpinModal = false },
            containerColor = Color(0xFF18243C),
            title = {
                Text(
                    "Lucky Spin Wheel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Spins left today: ${extraStats?.luckySpinsLeft ?: 3}",
                        color = Color(0xFFB7C1D1),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Simulated wheel drawing with rotation animation
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .rotate(spinRotation)
                            .background(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF0B5FFF),
                                        Color(0xFFFFC107),
                                        Color(0xFF00C853),
                                        Color(0xFFFF4D4D),
                                        Color(0xFF0B5FFF)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing divisions
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color.White, radius = 8f, center = center)
                        }
                        Text(
                            "★ WIN ★",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    if (spinResultMsg.isNotEmpty()) {
                        Text(
                            spinResultMsg,
                            color = Color(0xFFFFC107),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isSpinning && (extraStats?.luckySpinsLeft ?: 0) > 0) {
                            coroutineScope.launch {
                                isSpinning = true
                                spinResultMsg = "Spinning the Wheel..."
                                // Spin animation loop
                                repeat(30) { index ->
                                    spinRotation += (30 - index) * 5
                                    delay(50)
                                }
                                viewModel.spinLuckyWheel { won, cash ->
                                    spinResultMsg = "Result: $won!"
                                    isSpinning = false
                                }
                            }
                        } else if ((extraStats?.luckySpinsLeft ?: 0) <= 0) {
                            Toast.makeText(context, "No spins left for today!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5FFF)),
                    enabled = !isSpinning && (extraStats?.luckySpinsLeft ?: 0) > 0
                ) {
                    Text("SPIN NOW")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSpinModal = false },
                    enabled = !isSpinning
                ) {
                    Text("CLOSE", color = Color(0xFFB7C1D1))
                }
            }
        )
    }
}

// ------------------ SCREEN: BUY PACKAGE (SHOP) ------------------
@Composable
fun BuyPackageScreen(navController: NavHostController) {
    val packages = listOf(
        Quadruple("MINI PACKAGE", "₹399", listOf("Basic Learning Access", "Beginner Digital Marketing Course", "Limited affiliate earnings"), 399.0),
        Quadruple("PRO PACKAGE", "₹699", listOf("Advanced learning videos", "Better income payouts", "Premium CapCut and Canva guides"), 699.0),
        Quadruple("PREMIUM PACKAGE", "₹999", listOf("Premium high-ticket videos", "Maximum affiliate commissions", "Priority WhatsApp Support link"), 999.0),
        Quadruple("PREMIUM PLUS", "₹1299", listOf("VIP Course Vault Access", "Highest commission slab", "Personal support line priority"), 1299.0)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F)),
        contentPadding = PaddingValues(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upgrade / Buy Packages", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        items(packages) { (title, priceStr, features, price) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, if (title == "PREMIUM PLUS") Color(0xFFFFC107) else Color(0xFF233554)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(priceStr, color = Color(0xFFFFC107), fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF233554))
                    Spacer(modifier = Modifier.height(12.dp))

                    features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Yes", tint = Color(0xFF00C853), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(feature, color = Color(0xFFB7C1D1), fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { navController.navigate(Routes.PAYMENT + "/$title/$price") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (title == "PREMIUM PLUS") Color(0xFFFFC107) else Color(0xFF0B5FFF)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Buy Now",
                            fontWeight = FontWeight.Bold,
                            color = if (title == "PREMIUM PLUS") Color(0xFF0A0F1F) else Color.White
                        )
                    }
                }
            }
        }
    }
}

// ------------------ SCREEN: PAYMENT SCREEN ------------------
@Composable
fun PaymentScreen(navController: NavHostController, viewModel: LearnEarnViewModel, pkgName: String, price: Double) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf("UPI QR") }
    var transactionIdInput by remember { mutableStateOf("") }
    var mockScreenshotUploaded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F)),
        contentPadding = PaddingValues(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select Payment", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Bill Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11192E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Selected Package", color = Color(0xFFB7C1D1), fontSize = 12.sp)
                        Text(pkgName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Amount", color = Color(0xFFB7C1D1), fontSize = 12.sp)
                        Text("₹$price", color = Color(0xFFFFC107), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            }
        }

        // Payment Methods LazyRow
        item {
            Text("Select Payment Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
            val methods = listOf("UPI QR", "PhonePe", "Google Pay", "Paytm", "Bank Transfer")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(methods) { method ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMethod == method) Color(0xFF0B5FFF) else Color(0xFF18243C)
                        ),
                        modifier = Modifier
                            .clickable { selectedMethod = method }
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = method,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // Dynamic QR and UPI instructions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedMethod == "UPI QR" || selectedMethod == "PhonePe" || selectedMethod == "Google Pay" || selectedMethod == "Paytm") {
                        Text("Scan QR Code to pay ₹$price", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Simulated QR Code
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw realistic QR code mock blocks
                                for (x in 0..4) {
                                    for (y in 0..4) {
                                        if ((x + y) % 2 == 0 || (x == 0 && y == 0) || (x == 4 && y == 0) || (x == 0 && y == 4) || (x == 4 && y == 4)) {
                                            drawRect(
                                                color = Color(0xFF0A0F1F),
                                                topLeft = Offset(x * 30f, y * 30f),
                                                size = androidx.compose.ui.geometry.Size(24f, 24f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("UPI ID: payment@learnandearn", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        // Bank Transfer Info
                        Text("Bank Account Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            "Bank Name" to "State Bank of India",
                            "Account Holder" to "LEARN AND EARN PVT LTD",
                            "Account Number" to "30294810294",
                            "IFSC Code" to "SBIN000184"
                        ).forEach { (lbl, valStr) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(lbl, color = Color(0xFFB7C1D1), fontSize = 12.sp)
                                Text(valStr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Transaction Inputs & Screenshot
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Proof of Payment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = transactionIdInput,
                        onValueChange = { transactionIdInput = it },
                        label = { Text("Transaction ID / Ref No.") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0B5FFF),
                            unfocusedBorderColor = Color(0xFF233554),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated Screenshot Upload Button
                    OutlinedButton(
                        onClick = {
                            mockScreenshotUploaded = true
                            Toast.makeText(context, "Screenshot Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                        },
                        border = BorderStroke(1.dp, if (mockScreenshotUploaded) Color(0xFF00C853) else Color(0xFFB7C1D1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (mockScreenshotUploaded) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                tint = if (mockScreenshotUploaded) Color(0xFF00C853) else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (mockScreenshotUploaded) "Screenshot Verified" else "Upload Screenshot Receipt",
                                color = if (mockScreenshotUploaded) Color(0xFF00C853) else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Submit Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (transactionIdInput.isBlank()) {
                        Toast.makeText(context, "Please enter Transaction ID.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.submitPackagePayment(pkgName, price, transactionIdInput) {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5FFF)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Submit Verification Request", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ------------------ SCREEN: LEARNING DASHBOARD ------------------
@Composable
fun LearningDashboardScreen(viewModel: LearnEarnViewModel) {
    val courses by viewModel.courses.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var activeCourseForVideo by remember { mutableStateOf<Course?>(null) }
    var currentVideoProgress by remember { mutableStateOf(0f) }

    val categories = listOf("All", "Digital Marketing", "Instagram Growth", "Facebook Ads", "Affiliate Marketing", "Canva Design", "CapCut Editing", "AI Marketing", "YouTube Growth")

    val filteredCourses = if (selectedCategory == "All") {
        courses
    } else {
        courses.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F)),
        contentPadding = PaddingValues(18.dp)
    ) {
        item {
            Text("My Learning Space", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            Text("Acquire top high-income skills at your own pace.", color = Color(0xFFB7C1D1), fontSize = 13.sp)
        }

        // Categories LazyRow
        item {
            Spacer(modifier = Modifier.height(14.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCategory == category) Color(0xFF0B5FFF) else Color(0xFF18243C)
                        ),
                        modifier = Modifier.clickable { selectedCategory = category },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = category,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active playing video panel
        activeCourseForVideo?.let { course ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFC107)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NOW PLAYING", color = Color(0xFFFFC107), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { activeCourseForVideo = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                        Text(course.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Simulated Video Player
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Streaming Premium High-Quality Video...", color = Color.White, fontSize = 11.sp)
                            }

                            // Progress tracker overlay
                            LinearProgressIndicator(
                                progress = currentVideoProgress,
                                color = Color(0xFF0B5FFF),
                                trackColor = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .height(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val nextProg = (currentVideoProgress + 0.25f).coerceAtMost(1.0f)
                                    currentVideoProgress = nextProg
                                    viewModel.updateCourseProgress(course.id, nextProg)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (currentVideoProgress >= 1.0f) "Completed" else "Watch Progress +25%", fontSize = 12.sp)
                            }
                            Text("Length: ${course.duration}", color = Color(0xFFB7C1D1), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // List of filtered courses
        if (filteredCourses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No premium courses available in this category yet.", color = Color(0xFFB7C1D1), textAlign = TextAlign.Center)
                }
            }
        } else {
            items(filteredCourses) { course ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            activeCourseForVideo = course
                            currentVideoProgress = course.progress
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0B5FFF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (course.isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircleOutline,
                                contentDescription = "Status",
                                tint = if (course.isCompleted) Color(0xFF00C853) else Color(0xFF0B5FFF),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(course.category.uppercase(), color = Color(0xFFFFC107), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(course.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))

                            // Show progress bar
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = course.progress,
                                    color = Color(0xFF0B5FFF),
                                    trackColor = Color(0xFF233554),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${(course.progress * 100).toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (course.isCompleted) {
                            IconButton(onClick = {
                                Toast.makeText(viewModel.getApplication(), "Certificate Downloaded: ${course.title} Certificate!", Toast.LENGTH_LONG).show()
                            }) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = "Download Certificate", tint = Color(0xFF00C853))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------ SCREEN: EARNINGS & WITHDRAW SCREEN ------------------
@Composable
fun EarningsDashboardScreen(navController: NavHostController, viewModel: LearnEarnViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var withdrawAmountInput by remember { mutableStateOf("") }
    var upiOrBankInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F)),
        contentPadding = PaddingValues(18.dp)
    ) {
        item {
            Text("My Wallet & Withdrawals", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
        }

        // Wallet Balance highlight
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Available Balance", color = Color(0xFFB7C1D1), fontSize = 13.sp)
                    Text(
                        "₹${String.format("%.2f", userProfile?.walletBalance ?: 450.0)}",
                        color = Color(0xFF00C853),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Weekly Earning", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text("₹3,400", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Monthly Earning", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text("₹12,800", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Submit Withdrawal form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Request Payout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = withdrawAmountInput,
                        onValueChange = { withdrawAmountInput = it },
                        label = { Text("Amount to Withdraw (Min ₹200)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0B5FFF),
                            unfocusedBorderColor = Color(0xFF233554),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = upiOrBankInput,
                        onValueChange = { upiOrBankInput = it },
                        label = { Text("UPI ID or Bank Account Details") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0B5FFF),
                            unfocusedBorderColor = Color(0xFF233554),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amt = withdrawAmountInput.toDoubleOrNull()
                            if (amt == null) {
                                Toast.makeText(context, "Enter a valid amount.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.submitWithdrawal(amt, upiOrBankInput)
                            withdrawAmountInput = ""
                            upiOrBankInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5FFF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Withdrawal", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Payout and Purchase Logs History
        item {
            Text("Transaction Logs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Text("No recent transaction activity found.", color = Color(0xFFB7C1D1), fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
        } else {
            items(transactions) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11192E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (tx.type == "PURCHASE") Icons.Default.ShoppingCart else Icons.Default.AccountBalance,
                                    contentDescription = "TxType",
                                    tint = if (tx.type == "PURCHASE") Color(0xFF0B5FFF) else Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tx.type, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(tx.details, color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text("ID: ${tx.txIdOrUpi}", color = Color(0xFFB7C1D1), fontSize = 10.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${tx.amount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when (tx.status) {
                                        "Verified", "Completed" -> Color(0xFF00C853).copy(alpha = 0.15f)
                                        "Pending" -> Color(0xFFFFC107).copy(alpha = 0.15f)
                                        else -> Color(0xFFFF4D4D).copy(alpha = 0.15f)
                                    }
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tx.status,
                                    color = when (tx.status) {
                                        "Verified", "Completed" -> Color(0xFF00C853)
                                        "Pending" -> Color(0xFFFFC107)
                                        else -> Color(0xFFFF4D4D)
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------ SCREEN: AFFILIATE REFERRAL DASHBOARD ------------------
@Composable
fun AffiliateDashboardScreen(viewModel: LearnEarnViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    val referralLink = "https://learnearn.com/join?ref=${userProfile?.referralCode ?: "EARN99"}"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F)),
        contentPadding = PaddingValues(18.dp)
    ) {
        item {
            Text("Affiliate Dashboard", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            Text("Share your unique referral link to build direct passive income.", color = Color(0xFFB7C1D1), fontSize = 13.sp)
        }

        // Earnings statistics card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Affiliate Commissions Earned", color = Color(0xFFB7C1D1), fontSize = 12.sp)
                    Text(
                        "₹${String.format("%.2f", userProfile?.totalEarnings ?: 14800.0)}",
                        color = Color(0xFFFFC107),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Clicks", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text("1,420", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Registrations", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text("184", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Purchases", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Text("24", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Sharing tools
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Your Referral Link", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0F1F), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = referralLink,
                            color = Color(0xFFB7C1D1),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Referral Link", referralLink)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Link Copied!", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Social Share channels
                    Text("Share Directly On", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(
                            Triple("WhatsApp", Color(0xFF25D366), "whatsapp"),
                            Triple("Telegram", Color(0xFF0088CC), "telegram"),
                            Triple("Facebook", Color(0xFF1877F2), "facebook")
                        ).forEach { (name, color, channel) ->
                            Button(
                                onClick = {
                                    val text = "Hey! Join LEARN & EARN today and start building dynamic passive income. Use my link to join: $referralLink"
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Link via"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = color),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(name, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section: Visual Graph Chart of Earnings (Using Jetpack Compose Canvas)
        item {
            Text("Earning Trend This Week", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Earnings Growth (₹)", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Stunning Canvas based custom line graph
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val points = listOf(200f, 600f, 1500f, 1200f, 2400f, 3200f, 4500f)
                        val maxVal = 5000f
                        val width = size.width
                        val height = size.height

                        // Draw Grid lines
                        for (i in 1..4) {
                            val y = height * (i / 5f)
                            drawLine(
                                color = Color(0xFF233554),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        val stepX = width / (points.size - 1)
                        val path = Path()

                        points.forEachIndexed { index, p ->
                            val x = index * stepX
                            val y = height - (p / maxVal) * height
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        // Draw line
                        drawPath(
                            path = path,
                            color = Color(0xFF0B5FFF),
                            style = Stroke(width = 5f)
                        )

                        // Draw filled gradient area below path
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF0B5FFF).copy(alpha = 0.35f), Color.Transparent)
                            )
                        )

                        // Draw coordinate points
                        points.forEachIndexed { index, p ->
                            val x = index * stepX
                            val y = height - (p / maxVal) * height
                            drawCircle(
                                color = Color(0xFFFFC107),
                                radius = 7f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(day, color = Color(0xFFB7C1D1), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ------------------ SCREEN: PROFILE & SETTINGS SCREEN ------------------
@Composable
fun ProfileScreen(navController: NavHostController, viewModel: LearnEarnViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    var notificationEnabled by remember { mutableStateOf(true) }
    var kycVerified by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F)),
        contentPadding = PaddingValues(18.dp)
    ) {
        item {
            Text("My Account", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
        }

        // Profile details Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFF0B5FFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userProfile?.name ?: "I").take(1).uppercase(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(userProfile?.name ?: "Ishan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Mobile: ${userProfile?.mobile ?: "9876543210"}", color = Color(0xFFB7C1D1), fontSize = 12.sp)
                        Text("Ref Code: ${userProfile?.referralCode ?: "EARN99"}", color = Color(0xFFFFC107), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Admin Panel Switch
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11192E)),
                border = BorderStroke(1.dp, Color(0xFFFFC107)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Control/Admin Panel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Manage course DB & verify payments", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                        }
                    }
                    Button(
                        onClick = { navController.navigate(Routes.ADMIN) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                    ) {
                        Text("Open", color = Color(0xFF0A0F1F), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Section Settings
        item {
            Text("Settings & KYC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    // KYC status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("KYC Document Verification", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("PAN / Aadhaar for earnings withdrawal", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                        }
                        Switch(
                            checked = kycVerified,
                            onCheckedChange = {
                                kycVerified = it
                                Toast.makeText(context, if (it) "KYC Status: Verified!" else "KYC Reset", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00C853))
                        )
                    }
                    Divider(color = Color(0xFF233554))

                    // Notifications Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Instant Push Notifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Get instant alerts for affiliate sales", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                        }
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { notificationEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0B5FFF))
                        )
                    }
                    Divider(color = Color(0xFF233554))

                    // Reset Data Helper (for easy presentation testing)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.resetDemoData()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFFFF4D4D))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Reset Application Data", color = Color(0xFFFF4D4D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Restore prepopulated courses & earnings database", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Logout
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.logout {
                        navController.navigate(Routes.LANDING) {
                            popUpTo(Routes.LANDING) { inclusive = true }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text("Logout Account", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ------------------ SCREEN: ADMIN CONTROL PANEL SCREEN ------------------
@Composable
fun AdminPanelScreen(navController: NavHostController, viewModel: LearnEarnViewModel) {
    val courses by viewModel.courses.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var activeTab by remember { mutableStateOf("PAYMENTS") }

    // Course Creation Form State
    var courseTitleInput by remember { mutableStateOf("") }
    var courseCategoryInput by remember { mutableStateOf("Digital Marketing") }
    var courseDurationInput by remember { mutableStateOf("12:30") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F)),
        contentPadding = PaddingValues(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Control Room Panel", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tab Selector Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF18243C), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("PAYMENTS", "MANAGE COURSES").forEach { tab ->
                    Button(
                        onClick = { activeTab = tab },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == tab) Color(0xFF0B5FFF) else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(tab, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (activeTab == "PAYMENTS") {
            // Transaction verification log list
            val pendingTx = transactions.filter { it.status == "Pending" }
            if (pendingTx.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No pending payout or package purchase approvals found.", color = Color(0xFFB7C1D1), textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(pendingTx) { tx ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                        border = BorderStroke(1.dp, Color(0xFF233554)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tx.type, color = if (tx.type == "PURCHASE") Color(0xFF0B5FFF) else Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("₹${tx.amount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Details: ${tx.details}", color = Color.White, fontSize = 12.sp)
                            Text("TX ID/Ref: ${tx.txIdOrUpi}", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.adminRejectTransaction(tx.id) }
                                ) {
                                    Text("REJECT", color = Color(0xFFFF4D4D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.adminApproveTransaction(tx.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                                ) {
                                    Text("APPROVE & ACTIVATE", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Manage Courses form + display
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Add New Course Entry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = courseTitleInput,
                            onValueChange = { courseTitleInput = it },
                            label = { Text("Course Title") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0B5FFF),
                                unfocusedBorderColor = Color(0xFF233554),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = courseCategoryInput,
                            onValueChange = { courseCategoryInput = it },
                            label = { Text("Category (e.g. Canva Design)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0B5FFF),
                                unfocusedBorderColor = Color(0xFF233554),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = courseDurationInput,
                            onValueChange = { courseDurationInput = it },
                            label = { Text("Duration (e.g. 15:45)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0B5FFF),
                                unfocusedBorderColor = Color(0xFF233554),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (courseTitleInput.isBlank()) {
                                    return@Button
                                }
                                viewModel.adminAddCourse(courseTitleInput, courseCategoryInput, courseDurationInput)
                                courseTitleInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5FFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE COURSE TO DB", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            item {
                Text("Active Courses (${courses.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
            }

            items(courses) { course ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11192E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(course.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Category: ${course.category} | Progress: ${(course.progress * 100).toInt()}%", color = Color(0xFFB7C1D1), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ------------------ SCREEN: AI SUPPORT CHATBOT SCREEN ------------------
@Composable
fun ChatbotScreen(viewModel: LearnEarnViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsState()
    var promptInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1F))
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF18243C))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI Support",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Learn & Earn AI Assistant", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Instant responses on earnings & payouts", color = Color(0xFF00C853), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(onClick = { viewModel.clearChatHistory() }) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = Color.White)
            }
        }

        // Message stream
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) Color(0xFF0B5FFF) else Color(0xFF18243C)
                            ),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isUser) 12.dp else 0.dp,
                                bottomEnd = if (isUser) 0.dp else 12.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.message,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                if (isGeneratingAi) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF18243C)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "AI support is typing...",
                                    color = Color(0xFFFFC107),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF11192E))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Ask anything about payouts...", fontSize = 13.sp, color = Color(0xFFB7C1D1)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0B5FFF),
                    unfocusedBorderColor = Color(0xFF233554),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 3,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (promptInput.isNotBlank()) {
                        viewModel.sendChatMessage(promptInput)
                        promptInput = ""
                    }
                },
                modifier = Modifier
                    .background(Color(0xFF0B5FFF), CircleShape)
                    .size(44.dp)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// Simple Quadruple / Triple generic helpers for layout
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
