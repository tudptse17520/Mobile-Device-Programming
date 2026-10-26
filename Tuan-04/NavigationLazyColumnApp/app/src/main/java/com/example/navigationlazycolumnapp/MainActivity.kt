package com.example.navigationlazycolumnapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val ROOT_ROUTE = "root"
private const val CHOOSE_LIST_ROUTE = "choose_list"
private const val COLUMN_LIST_ROUTE = "column_list"
private const val LAZY_LIST_ROUTE = "lazy_list"
private const val DETAIL_ROUTE = "detail/{itemId}"
private const val ITEM_COUNT = 1_000_000

private const val QUOTE_TEXT = "The only way to do great work is to love what you do."
private const val QUOTE_AUTHOR = "Steve Jobs"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFBFF)
                ) {
                    NavigationLazyColumnApp()
                }
            }
        }
    }
}

@Composable
fun NavigationLazyColumnApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROOT_ROUTE
    ) {
        composable(ROOT_ROUTE) {
            RootScreen(
                onPushClick = {
                    navController.navigate(CHOOSE_LIST_ROUTE)
                }
            )
        }

        composable(CHOOSE_LIST_ROUTE) {
            ChooseListTypeScreen(
                onBack = { navController.popBackStack() },
                onColumnClick = {
                    navController.navigate(COLUMN_LIST_ROUTE) {
                        popUpTo(CHOOSE_LIST_ROUTE) { inclusive = true }
                    }
                },
                onLazyColumnClick = {
                    navController.navigate(LAZY_LIST_ROUTE) {
                        popUpTo(CHOOSE_LIST_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(COLUMN_LIST_ROUTE) {
            ColumnMillionItemsScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { itemId ->
                    navController.navigate("detail/$itemId")
                }
            )
        }

        composable(LAZY_LIST_ROUTE) {
            QuoteListScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { itemId ->
                    navController.navigate("detail/$itemId")
                }
            )
        }

        composable(
            route = DETAIL_ROUTE,
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 1

            QuoteDetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onBackToRoot = {
                    val popped = navController.popBackStack(
                        route = ROOT_ROUTE,
                        inclusive = false
                    )
                    if (!popped) {
                        navController.navigate(ROOT_ROUTE) {
                            popUpTo(ROOT_ROUTE) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun RootScreen(
    onPushClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        JetpackComposeLogo(modifier = Modifier.size(150.dp))

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Navigation",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "is a framework that simplifies the implementation of navigation between different UI components (activities, fragments, or composables) in an app",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF666666),
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.weight(1.3f))

        Button(
            onClick = onPushClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Text(
                text = "PUSH",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun JetpackComposeLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.jetpack_compose_logo),
        contentDescription = "Jetpack Compose Logo",
        modifier = modifier
    )
}

@Composable
fun ChooseListTypeScreen(
    onBack: () -> Unit,
    onColumnClick: () -> Unit,
    onLazyColumnClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBFF))
            .navigationBarsPadding()
    ) {
        BlueTopBar(title = "Choose List Type", onBack = onBack, showBack = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onColumnClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    text = "Column (1,000,000 items)",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLazyColumnClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    text = "LazyColumn (1,000,000 items)",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ColumnMillionItemsScreen(
    onBack: () -> Unit,
    onItemClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        BlueTopBar(title = "Column", onBack = onBack, showBack = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = "Column - 1,000,000 items",
                modifier = Modifier.padding(12.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            repeat(ITEM_COUNT) { index ->
                val itemId = index + 1
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onItemClick(itemId) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFC7E7F8)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = itemId.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = QUOTE_TEXT,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteListScreen(
    onBack: () -> Unit,
    onItemClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBFF))
            .navigationBarsPadding()
    ) {
        BlueTopBar(title = "LazyColumn", onBack = onBack, showBack = true)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
        ) {
            items(count = ITEM_COUNT) { index ->
                val itemId = index + 1
                QuoteListItem(
                    itemId = itemId,
                    onClick = { onItemClick(itemId) }
                )
            }
        }
    }
}

@Composable
private fun QuoteListItem(
    itemId: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F3FC)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatItemId(itemId),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = QUOTE_TEXT,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF82909A)
                )
            }

            Text(
                text = "➜",
                modifier = Modifier.padding(start = 8.dp),
                fontSize = 20.sp,
                color = Color(0xFF263238)
            )
        }
    }
}

@Composable
fun QuoteDetailScreen(
    itemId: Int,
    onBack: () -> Unit,
    onBackToRoot: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BlueTopBar(title = "Detail", onBack = onBack, showBack = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "\"$QUOTE_TEXT\"",
                modifier = Modifier.padding(horizontal = 12.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF222222)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = Color(0xFF8BD5F6),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "\"$QUOTE_TEXT\"",
                        fontSize = 25.sp,
                        lineHeight = 34.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF101820)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = QUOTE_AUTHOR,
                        fontSize = 15.sp,
                        color = Color(0xFF101820)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Item ID: ${formatItemId(itemId)}",
                        fontSize = 12.sp,
                        color = Color(0xFF101820)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onBackToRoot,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = "BACK TO ROOT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BlueTopBar(
    title: String,
    onBack: () -> Unit,
    showBack: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2196F3))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                Text(
                    text = "‹",
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(end = 16.dp),
                    fontSize = 32.sp,
                    color = Color.White
                )
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

private fun formatItemId(itemId: Int): String {
    return if (itemId == ITEM_COUNT) "1,000,000" else itemId.toString()
}
