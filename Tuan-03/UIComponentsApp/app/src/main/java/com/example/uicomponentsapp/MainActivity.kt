package com.example.uicomponentsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.uicomponentsapp.ui.theme.UIComponentsAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class UiComponent(
    val name: String,
    val description: String
)

data class UiComponentGroup(
    val name: String,
    val components: List<UiComponent>
)

val uiComponentGroups = listOf(
    UiComponentGroup(
        "Display",
        listOf(
            UiComponent("Text", "Displays text"),
            UiComponent("Image", "Displays an image")
        )
    ),
    UiComponentGroup(
        "Input",
        listOf(
            UiComponent("TextField", "Input field for text"),
            UiComponent("PasswordField", "Input field for passwords"),
            UiComponent("Toggle", "On/off switch"),
            UiComponent("Slider", "Selects a value from a range"),
            UiComponent("DatePicker", "Selects a date")
        )
    ),
    UiComponentGroup(
        "Selection",
        listOf(
            UiComponent("Button", "Triggers an action when tapped"),
            UiComponent("Checkbox", "Selects one or multiple options"),
            UiComponent("RadioButton", "Selects a single option"),
            UiComponent("Picker", "Selects a value from a list")
        )
    ),
    UiComponentGroup(
        "Navigation",
        listOf(
            UiComponent("NavigationView", "Navigation container with title and back button"),
            UiComponent("TabBar", "Switches between main sections"),
            UiComponent("Menu", "Shows a menu with multiple actions")
        )
    ),
    UiComponentGroup(
        "Content",
        listOf(
            UiComponent("List", "Displays a list of items"),
            UiComponent("Card", "Container with content and style"),
            UiComponent("Divider", "Separates content into sections")
        )
    ),
    UiComponentGroup(
        "Layout",
        listOf(
            UiComponent("Column", "Arranges elements vertically"),
            UiComponent("Row", "Arranges elements horizontally"),
            UiComponent("Box", "Places elements on top of each other")
        )
    )
)

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(timestamp))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UIComponentsAppTheme {
                var currentScreen by remember { mutableStateOf("welcome") }
                var selectedComponent by remember { mutableStateOf("Text") }
                val listState = rememberLazyListState()

                BackHandler(enabled = currentScreen != "welcome") {
                    currentScreen = when (currentScreen) {
                        "detail" -> "list"
                        else -> "welcome"
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when (currentScreen) {
                        "welcome" -> WelcomeScreen(
                            onReadyClick = { currentScreen = "list" },
                            modifier = Modifier.padding(innerPadding)
                        )

                        "list" -> ComponentListScreen(
                            listState = listState,
                            onSelectComponent = { componentName ->
                                selectedComponent = componentName
                                currentScreen = "detail"
                            },
                            modifier = Modifier.padding(innerPadding)
                        )

                        "detail" -> ComponentDetailScreen(
                            componentName = selectedComponent,
                            onBack = { currentScreen = "list" },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onReadyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.jetpack_compose_logo),
            contentDescription = "Jetpack Compose logo",
            modifier = Modifier.size(130.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Jetpack Compose",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Jetpack Compose is a modern UI toolkit for building native Android applications using a declarative programming approach.",
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF555555),
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1.2f))

        Button(
            onClick = onReadyClick,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Text(
                text = "I'm ready",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ComponentListScreen(
    onSelectComponent: (String) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "UI Components List",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF399BCB),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }

        uiComponentGroups.forEach { group ->
            item {
                Text(
                    text = group.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            items(
                items = group.components,
                key = { component -> "${group.name}_${component.name}" }
            ) { component ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectComponent(component.name) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFC3E2F8)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = component.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2D38)
                        )
                        Text(
                            text = component.description,
                            fontSize = 13.sp,
                            color = Color(0xFF37474F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentDetailScreen(
    componentName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenTitle = when (componentName) {
        "Text" -> "Text Detail"
        "Image" -> "Images"
        "Row" -> "Row Layout"
        "Column" -> "Column Layout"
        else -> componentName
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = "‹",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF2495D3)
                )
            }

            Text(
                text = screenTitle,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2495D3),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (componentName) {
                "Text" -> TextDetailExample()
                "Image" -> ImagesDetailExample()
                "TextField" -> TextFieldDetailExample()
                "PasswordField" -> PasswordFieldDetailExample()
                "Toggle" -> ToggleDetailExample()
                "Slider" -> SliderDetailExample()
                "DatePicker" -> DatePickerDetailExample()
                "Button" -> ButtonDetailExample()
                "Checkbox" -> CheckboxDetailExample()
                "RadioButton" -> RadioButtonDetailExample()
                "Picker" -> PickerDetailExample()
                "NavigationView" -> NavigationViewDetailExample()
                "TabBar" -> TabBarDetailExample()
                "Menu" -> MenuDetailExample()
                "List" -> ListDetailExample()
                "Card" -> CardDetailExample()
                "Divider" -> DividerDetailExample()
                "Column" -> ColumnDetailExample()
                "Row" -> RowLayoutDetailExample()
                "Box" -> BoxDetailExample()
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Component: $componentName")
                    }
                }
            }
        }
    }
}

@Composable
fun TextDetailExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        val annotatedText = buildAnnotatedString {
            append("The ")
            withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                append("quick")
            }
            append(" ")
            withStyle(
                style = SpanStyle(
                    fontSize = 32.sp,
                    color = Color(0xFFC76918),
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append("Brown")
            }
            append("\n")
            append("fox ")
            withStyle(style = SpanStyle(letterSpacing = 6.sp)) {
                append("jumps")
            }
            append(" ")
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            ) {
                append("over")
            }
            append("\n")
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                append("the")
            }
            append(" ")
            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                append("lazy")
            }
            append(" dog.")
        }

        Text(
            text = annotatedText,
            fontSize = 24.sp,
            lineHeight = 44.sp,
            textAlign = TextAlign.Center,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        HorizontalDivider(
            color = Color(0xFFE5E5E5),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ImagesDetailExample() {
    val imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS17Cnv2pYRVsp1Q0IkstxSAgtbesWdFGL0RFANVS62Uw&s=10"
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Remote web image",
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = Color(0xFF2495D3)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unable to load image",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        )

        Text(
            text = imageUrl,
            fontSize = 11.sp,
            color = Color(0xFF1E88E5),
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 18.dp)
                .clickable {
                    try {
                        uriHandler.openUri(imageUrl)
                    } catch (_: Exception) {
                    }
                }
        )

        Image(
            painter = painterResource(id = R.drawable.cat_picture),
            contentDescription = "Local cat picture",
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "In app",
            fontSize = 13.sp,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun TextFieldDetailExample() {
    var textValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            placeholder = {
                Text(
                    text = "Thông tin nhập",
                    color = Color(0xFF9E9E9E),
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFBDBDBD),
                unfocusedBorderColor = Color(0xFFD0D0D0),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Tự động cập nhật dữ liệu theo textfield",
            color = Color(0xFFE53935),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp, start = 12.dp)
        )

        if (textValue.isNotEmpty()) {
            Text(
                text = textValue,
                color = Color(0xFF212121),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 12.dp, start = 12.dp)
            )
        }
    }
}

@Composable
fun RowLayoutDetailExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            Color(0xFFA5C8F7),
                            RoundedCornerShape(8.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            Color(0xFF4F83E4),
                            RoundedCornerShape(8.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            Color(0xFFA5C8F7),
                            RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun PasswordFieldDetailExample() {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Enter password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show", fontSize = 12.sp)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Password length: ${password.length} characters",
            fontSize = 14.sp,
            color = Color(0xFF555555)
        )
    }
}

@Composable
fun ToggleDetailExample() {
    var isEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable feature", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Switch(
                checked = isEnabled,
                onCheckedChange = { isEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF2196F3)
                )
            )
        }

        Text(
            text = "Status: ${if (isEnabled) "ON" else "OFF"}",
            fontSize = 14.sp,
            color = Color(0xFF555555)
        )
    }
}

@Composable
fun SliderDetailExample() {
    var sliderValue by remember { mutableStateOf(40f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Value: ${sliderValue.toInt()}%",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF2196F3),
                activeTrackColor = Color(0xFF2196F3)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDetailExample() {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val formattedDate = selectedDate?.let {
        formatDate(it)
    } ?: "No date selected"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Selected Date: $formattedDate",
            fontSize = 18.sp
        )

        Button(
            onClick = { showDialog = true }
        ) {
            Text("Open DatePicker Dialog")
        }

        if (showDialog) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
            )

            DatePickerDialog(
                onDismissRequest = {
                    showDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDate =
                                datePickerState.selectedDateMillis
                            showDialog = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState
                )
            }
        }
    }
}

@Composable
fun ButtonDetailExample() {
    var count by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Clicked: $count times",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )

        Button(
            onClick = { count++ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Click Me")
        }
    }
}

@Composable
fun CheckboxDetailExample() {
    var checked1 by remember {
        mutableStateOf(false)
    }

    var checked2 by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { checked1 = !checked1 },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked1,
                onCheckedChange = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Jetpack Compose", fontSize = 15.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { checked2 = !checked2 },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked2,
                onCheckedChange = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kotlin Coroutines", fontSize = 15.sp)
        }

        val selectedCount = listOf(checked1, checked2).count { it }
        Text(
            text = "Selected options: $selectedCount of 2",
            fontSize = 14.sp,
            color = Color(0xFF555555)
        )
    }
}

@Composable
fun RadioButtonDetailExample() {
    val options = listOf("Kotlin", "Java", "Python")
    var selectedOption by remember { mutableStateOf(options[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Select a programming language:", fontSize = 15.sp, fontWeight = FontWeight.Medium)

        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedOption = option }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2196F3))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option, fontSize = 15.sp)
            }
        }

        Text(
            text = "Selected: $selectedOption",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1976D2),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun PickerDetailExample() {
    val languages = listOf("Kotlin", "Java", "Python", "C++")
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(languages[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Select an item:", fontSize = 15.sp, fontWeight = FontWeight.Medium)

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedItem, fontSize = 15.sp, color = Color.Black)
                    Text("▼", fontSize = 12.sp, color = Color.Gray)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            selectedItem = language
                            expanded = false
                        }
                    )
                }
            }
        }

        Text(
            text = "Picked value: $selectedItem",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
fun NavigationViewDetailExample() {
    var selectedScreen by remember { mutableStateOf("Home") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { selectedScreen = "Home" }) {
                        Text("‹ Back", color = Color(0xFF2196F3))
                    }
                    Text(
                        text = "Header: $selectedScreen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                HorizontalDivider()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Viewing: $selectedScreen",
                        fontSize = 16.sp,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Home", "Profile", "Settings").forEach { dest ->
                Button(
                    onClick = { selectedScreen = dest },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedScreen == dest) Color(0xFF2196F3) else Color(0xFFE0E0E0),
                        contentColor = if (selectedScreen == dest) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(dest, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TabBarDetailExample() {
    val tabs = listOf("Overview", "Details", "Settings")
    var selectedIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color(0xFFF5F5F5),
            contentColor = Color(0xFF2196F3)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Content for \"${tabs[selectedIndex]}\" tab",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
fun MenuDetailExample() {
    var expanded by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf("None") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box {
            Button(
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Open Menu ⋮")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("Edit", "Share", "Delete").forEach { action ->
                    DropdownMenuItem(
                        text = { Text(action) },
                        onClick = {
                            selectedAction = action
                            expanded = false
                        }
                    )
                }
            }
        }

        Text(
            text = "Selected action: $selectedAction",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2196F3)
        )
    }
}

@Composable
fun ListDetailExample() {
    val items = listOf(
        "Jetpack Compose",
        "Kotlin Coroutines",
        "Material 3 Components",
        "Declarative Layouts"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(items) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CardDetailExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sample Card",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                Text(
                    text = "A Card groups related information and content into a single container.",
                    fontSize = 14.sp,
                    color = Color(0xFF333333)
                )
            }
        }
    }
}

@Composable
fun DividerDetailExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Section 1: Above Divider", fontWeight = FontWeight.Medium, fontSize = 15.sp)

        HorizontalDivider(thickness = 1.dp, color = Color(0xFFBDBDBD))

        Text("Section 2: Below Divider", fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
fun ColumnDetailExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFAFAFA), shape = RoundedCornerShape(12.dp))
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.55f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFFA5C8F7), shape = RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFF4F83E4), shape = RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFFA5C8F7), shape = RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
fun BoxDetailExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Box layers elements on top of each other:", fontSize = 15.sp, fontWeight = FontWeight.Medium)

        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color(0xFFC3E2F8), shape = RoundedCornerShape(14.dp))
            )

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .offset(x = (-12).dp, y = (-12).dp)
                    .background(Color(0xFF64B5F6), shape = RoundedCornerShape(12.dp))
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = 16.dp, y = 16.dp)
                    .background(Color(0xFF1E88E5), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Top", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    UIComponentsAppTheme {
        WelcomeScreen(onReadyClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ComponentListScreenPreview() {
    UIComponentsAppTheme {
        ComponentListScreen(onSelectComponent = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ComponentDetailScreenPreview() {
    UIComponentsAppTheme {
        ComponentDetailScreen(
            componentName = "Text",
            onBack = {}
        )
    }
}
