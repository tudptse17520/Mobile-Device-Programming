package com.example.firebaseauth.data

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseTaskRepository(url: String, uid: String) {
    private val database = synchronized(databases) {
        databases.getOrPut(url) { FirebaseDatabase.getInstance(url).apply { setPersistenceEnabled(true) } }
    }
    private val tasks = database.getReference("users").child(uid).child("tasks")
    val changes = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.children.mapNotNull { child ->
                    child.getValue(TaskItem::class.java)?.copy(id = child.key.orEmpty())
                }.sortedWith(compareBy({ it.date }, { it.time })))
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        tasks.addValueEventListener(listener)
        awaitClose { tasks.removeEventListener(listener) }
    }
    val connection = callbackFlow {
        val ref = database.getReference(".info/connected")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { trySend(snapshot.getValue(Boolean::class.java) == true) }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
    fun save(task: TaskItem, onResult: (Exception?) -> Unit) {
        val key = task.id.ifBlank { tasks.push().key ?: error("Không tạo được mã công việc") }
        tasks.child(key).setValue(task.copy(id = key)).addOnCompleteListener { onResult(it.exception) }
    }
    fun toggle(task: TaskItem, onResult: (Exception?) -> Unit) {
        tasks.child(task.id).child("completed").setValue(!task.completed).addOnCompleteListener { onResult(it.exception) }
    }
    fun delete(task: TaskItem, onResult: (Exception?) -> Unit) {
        tasks.child(task.id).removeValue().addOnCompleteListener { onResult(it.exception) }
    }
    companion object { private val databases = mutableMapOf<String, FirebaseDatabase>() }
}
