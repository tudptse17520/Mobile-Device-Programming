package com.example.firebaseauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firebaseauth.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditor(
    task: TaskItem, onDismiss: () -> Unit, onSave: (TaskItem) -> Unit,
    onDelete: () -> Unit, onShare: () -> Unit, modifier: Modifier = Modifier
) {
    var title by rememberSaveable(task.id) { mutableStateOf(task.title) }
    var category by rememberSaveable(task.id) { mutableStateOf(task.category) }
    var time by rememberSaveable(task.id) { mutableStateOf(task.time) }
    var date by rememberSaveable(task.id) { mutableStateOf(task.date) }
    var note by rememberSaveable(task.id) { mutableStateOf(task.note) }
    val valid = validTask(title, time, date) && note.length <= 2000
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (task.id.isBlank()) "Thêm công việc" else "Chi tiết công việc", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(title, { title = it.take(120) }, label = { Text("Tên công việc") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Học tập", "Cuộc sống").forEach { label -> FilterChip(category == label, { category = label }, { Text(label) }) }
            }
            OutlinedTextField(date, { date = it.take(10) }, label = { Text("Ngày (yyyy-MM-dd)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(time, { time = it.take(5) }, label = { Text("Giờ (HH:mm)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(note, { note = it.take(2000) }, label = { Text("Ghi chú") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
            Button(onClick = { onSave(task.copy(title = title, category = category, time = time, date = date, note = note)) },
                enabled = valid, modifier = Modifier.fillMaxWidth()) { Text("Lưu công việc") }
            if (task.id.isNotBlank()) Row {
                TextButton(onClick = onShare) { Text("Chia sẻ bản đã lưu") }
                TextButton(onClick = onDelete) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
