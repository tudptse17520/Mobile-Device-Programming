package com.example.dataflownavigationapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dataflownavigationapp.model.PasswordResetFlowData
import com.example.dataflownavigationapp.ui.theme.DataFlowNavigationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DataFlowNavigationAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DataFlowNavigationAppNavGraph()
                }
            }
        }
    }
}

@Composable
fun DataFlowNavigationAppNavGraph() {
    val navController = rememberNavController()
    var returnedResultData by remember { mutableStateOf<PasswordResetFlowData?>(null) }

    NavHost(
        navController = navController,
        startDestination = "forget_password"
    ) {
        composable("forget_password") {
            ForgetPasswordScreen(
                returnedData = returnedResultData,
                onNext = { email ->
                    val encodedEmail = Uri.encode(email)
                    navController.navigate("verify_code/$encodedEmail")
                }
            )
        }

        composable(
            route = "verify_code/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            VerifyCodeScreen(
                email = email,
                onBack = { navController.popBackStack() },
                onNext = { otp ->
                    val encodedEmail = Uri.encode(email)
                    val encodedOtp = Uri.encode(otp)
                    navController.navigate("reset_password/$encodedEmail/$encodedOtp")
                }
            )
        }

        composable(
            route = "reset_password/{email}/{otp}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("otp") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            val otp = Uri.decode(backStackEntry.arguments?.getString("otp") ?: "")

            ResetPasswordScreen(
                email = email,
                otp = otp,
                onBack = { navController.popBackStack() },
                onNext = { password ->
                    val encodedEmail = Uri.encode(email)
                    val encodedOtp = Uri.encode(otp)
                    val encodedPassword = Uri.encode(password)
                    navController.navigate("confirm/$encodedEmail/$encodedOtp/$encodedPassword")
                }
            )
        }

        composable(
            route = "confirm/{email}/{otp}/{password}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("otp") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            val otp = Uri.decode(backStackEntry.arguments?.getString("otp") ?: "")
            val password = Uri.decode(backStackEntry.arguments?.getString("password") ?: "")

            ConfirmScreen(
                email = email,
                otp = otp,
                password = password,
                onBack = { navController.popBackStack() },
                onSubmit = {
                    returnedResultData = PasswordResetFlowData(
                        email = email,
                        otp = otp,
                        password = password
                    )
                    navController.navigate("forget_password") {
                        popUpTo("forget_password") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun SmartTasksHeader(
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            if (showBackButton) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF29B6F6))
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "UTH",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF00695C),
            letterSpacing = 1.sp
        )

        Text(
            text = "UNIVERSITY\nOF TRANSPORT\nHOCHIMINH CITY",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC62828),
            textAlign = TextAlign.Center,
            lineHeight = 10.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SmartTasks",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF29B6F6)
        )
    }
}

@Composable
fun ForgetPasswordScreen(
    returnedData: PasswordResetFlowData? = null,
    onNext: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf(returnedData?.email ?: "uth@gmail.com") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmartTasksHeader(showBackButton = false)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Forget Password?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your Email, we will send you a verification code.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                placeholder = { Text("Your Email", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF29B6F6),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(err, color = Color(0xFFE53935), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isBlank() || !email.contains("@")) {
                        errorMessage = "Vui lòng nhập email hợp lệ!"
                    } else {
                        onNext(email)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6))
            ) {
                Text(
                    text = "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            if (returnedData != null && returnedData.isComplete()) {
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF81D4FA)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "email: ${returnedData.email}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "otp: ${returnedData.otp}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "pass: ${returnedData.password}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VerifyCodeScreen(
    email: String,
    onBack: () -> Unit = {},
    onNext: (String) -> Unit = {}
) {
    var otpDigits by remember { mutableStateOf(listOf("1", "2", "3", "4", "5")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequesters = remember { List(5) { FocusRequester() } }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmartTasksHeader(
                showBackButton = true,
                onBackClick = onBack
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verify Code",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the code\nwe just sent you on your registered Email",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF29B6F6)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (index in 0 until 5) {
                    val digit = otpDigits.getOrElse(index) { "" }
                    OutlinedTextField(
                        value = digit,
                        onValueChange = { newVal ->
                            if (newVal.length <= 1) {
                                val newDigits = otpDigits.toMutableList()
                                if (index < newDigits.size) {
                                    newDigits[index] = newVal
                                } else {
                                    newDigits.add(newVal)
                                }
                                otpDigits = newDigits
                                errorMessage = null
                                if (newVal.isNotEmpty() && index < 4) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }
                        },
                        modifier = Modifier
                            .width(52.dp)
                            .height(56.dp)
                            .focusRequester(focusRequesters[index]),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E293B)
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedBorderColor = Color(0xFF29B6F6),
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(err, color = Color(0xFFE53935), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val enteredOtp = otpDigits.joinToString("")
                    if (enteredOtp.length < 5) {
                        errorMessage = "Vui lòng nhập đủ 5 chữ số mã xác thực!"
                    } else {
                        onNext(enteredOtp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6))
            ) {
                Text(
                    text = "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(
    email: String,
    otp: String,
    onBack: () -> Unit = {},
    onNext: (String) -> Unit = {}
) {
    var password by remember { mutableStateOf("12345678") }
    var confirmPassword by remember { mutableStateOf("12345678") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmartTasksHeader(
                showBackButton = true,
                onBackClick = onBack
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create new password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your new password must be different form\npreviously used password",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = { Text("Password", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF29B6F6),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                placeholder = { Text("Confirm Password", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF29B6F6),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(err, color = Color(0xFFE53935), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (password.length < 6) {
                        errorMessage = "Mật khẩu phải có tối thiểu 6 ký tự!"
                    } else if (password != confirmPassword) {
                        errorMessage = "Mật khẩu xác nhận không khớp!"
                    } else {
                        onNext(password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6))
            ) {
                Text(
                    text = "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ConfirmScreen(
    email: String,
    otp: String,
    password: String,
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmartTasksHeader(
                showBackButton = true,
                onBackClick = onBack
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Confirm",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We are here to help you!",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = "•".repeat(password.length.coerceAtLeast(8)),
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6))
            ) {
                Text(
                    text = "Summit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "1. Forget Password")
@Composable
fun ForgetPasswordScreenPreview() {
    DataFlowNavigationAppTheme {
        ForgetPasswordScreen()
    }
}

@Preview(showBackground = true, name = "1b. Forget Password (Nhận kết quả từ màn 4)")
@Composable
fun ForgetPasswordWithReturnedDataPreview() {
    DataFlowNavigationAppTheme {
        ForgetPasswordScreen(
            returnedData = PasswordResetFlowData(
                email = "uth@gmail.com",
                otp = "12345",
                password = "************"
            )
        )
    }
}

@Preview(showBackground = true, name = "2. Verification (Verify Code)")
@Composable
fun VerifyCodeScreenPreview() {
    DataFlowNavigationAppTheme {
        VerifyCodeScreen(email = "uth@gmail.com")
    }
}

@Preview(showBackground = true, name = "3. Reset Password")
@Composable
fun ResetPasswordScreenPreview() {
    DataFlowNavigationAppTheme {
        ResetPasswordScreen(email = "uth@gmail.com", otp = "12345")
    }
}

@Preview(showBackground = true, name = "4. Confirm")
@Composable
fun ConfirmScreenPreview() {
    DataFlowNavigationAppTheme {
        ConfirmScreen(
            email = "uth@gmail.com",
            otp = "12345",
            password = "************"
        )
    }
}
