package com.example.uthsmarttasks

import com.example.uthsmarttasks.model.*
import com.example.uthsmarttasks.network.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class TaskContractTest {
    private val gson = Gson()
    private val original = Task(2, "Original", date = "2026-10-20", time = "14:00",
        subtasks = listOf(Subtask("First"), Subtask("Second", true)))

    @Test fun serializedRequestsAndSessionOverlaySurviveFixedSamples() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            val repository = TaskRepository(ApiClient.create(server.url("/mock/teach/").toString(), "test-only"))
            fun reply(value: Any, status: Int = 200) {
                server.enqueue(MockResponse().setResponseCode(status).setBody(gson.toJson(value)))
            }
            fun request(method: String, path: String, keys: Set<String>): com.google.gson.JsonObject {
                val r = server.takeRequest(2, TimeUnit.SECONDS)!!
                assertEquals(method, r.method)
                assertEquals(path, r.path)
                assertEquals("Bearer test-only", r.getHeader("Authorization"))
                return JsonParser.parseString(r.body.readUtf8()).asJsonObject.also { assertEquals(keys, it.keySet()) }
            }
            reply(listOf(original))
            assertEquals(listOf(original), repository.loadTasks())
            assertEquals("GET", server.takeRequest().method)

            val form = CreateTaskRequest("Test create", "", "life", "2026-11-17", "19:00", "high", false)
            reply(original.copy(id = 7), 201)
            val created = repository.create(form)
            val post = request("POST", "/mock/teach/tasks", setOf("title","note","category","date","time","priority","remind"))
            assertEquals("Test create", post["title"].asString)
            assertFalse(post["remind"].asBoolean)
            assertEquals(form.title, created.title)
            reply(original.copy(id = 7), 201)
            val duplicate = repository.create(form.copy(title = "Second create"))
            request("POST", "/mock/teach/tasks", post.keySet())
            assertTrue(duplicate.id < 0)
            assertNotEquals(created.id, duplicate.id)
            reply(original)
            repository.patch(duplicate.id, PatchTaskRequest(done = false))
            assertFalse(request("PATCH", "/mock/teach/tasks/7", setOf("done"))["done"].asBoolean)

            reply(original.copy(id = 999))
            assertTrue(repository.patch(2, PatchTaskRequest(done = true)).done)
            val done = request("PATCH", "/mock/teach/tasks/2", setOf("done"))
            assertTrue(done["done"].asBoolean)
            assertEquals("Original", repository.snapshot().first { it.id == 2 }.title)

            reply(original.copy(id = 999))
            repository.patch(2, form.copy(title = "Edited").changesFrom(
                original.copy(title = form.title, note = form.note, category = form.category,
                    date = form.date, time = form.time, priority = form.priority, remind = form.remind)))
            request("PATCH", "/mock/teach/tasks/2", setOf("title"))

            val subtasks = listOf(Subtask("Renamed", true), Subtask("New", false))
            reply(original)
            assertEquals(subtasks, repository.patch(2, PatchTaskRequest(subtasks = subtasks)).subtasks)
            val sub = request("PATCH", "/mock/teach/tasks/2", setOf("subtasks"))
            assertEquals(gson.toJsonTree(subtasks), sub["subtasks"])

            reply(listOf(original))
            val reloaded = repository.loadTasks()
            server.takeRequest()
            assertTrue(reloaded.first { it.id == 2 }.done)
            assertEquals("Edited", reloaded.first { it.id == 2 }.title)
            assertEquals(subtasks, reloaded.first { it.id == 2 }.subtasks)
            assertEquals(3, reloaded.size)
            reply(original)
            assertEquals("Edited", repository.loadTask(2).title)
            server.takeRequest()

            server.enqueue(MockResponse().setResponseCode(204))
            repository.delete(2)
            val deletion = server.takeRequest()
            assertEquals("DELETE", deletion.method)
            assertEquals("/mock/teach/tasks/2", deletion.path)
            assertEquals(0L, deletion.bodySize)
            reply(listOf(original))
            assertFalse(repository.loadTasks().any { it.id == 2 })
            server.takeRequest()

            val freshSession = TaskRepository(ApiClient.create(server.url("/").toString()))
            reply(listOf(original))
            assertEquals(listOf(original), freshSession.loadTasks())
        } finally { server.shutdown() }
    }

    @Test fun apiMessageFallbackAndConnectionFailureDoNotChangeState() = runBlocking {
        val server = MockWebServer()
        server.start()
        val repo = TaskRepository(ApiClient.create(server.url("/").toString()))
        try {
            server.enqueue(MockResponse().setBody(gson.toJson(listOf(original))))
            repo.loadTasks()
            server.enqueue(MockResponse().setResponseCode(400).setBody("""{"message":"Tên công việc không được để trống."}"""))
            try {
                repo.patch(2, PatchTaskRequest(title = ""))
                fail("Expected API error")
            } catch (e: TaskApiException) { assertEquals("Tên công việc không được để trống.", e.message) }
            assertEquals(original, repo.snapshot().single())
            server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
            try { repo.loadTasks(); fail("Expected 500") }
            catch (e: TaskApiException) { assertTrue(e.message!!.contains("HTTP 500")) }
            server.shutdown()
            try { repo.patch(2, PatchTaskRequest(done = true)); fail("Expected disconnected socket") }
            catch (e: IOException) { assertEquals(original, repo.snapshot().single()) }
        } finally { runCatching { server.shutdown() } }
    }

    @Test fun homeProgressCountsOnlyToday() {
        val tasks = listOf(original.copy(done = true), original.copy(id = 3, date = "2026-11-17"),
            original.copy(id = 4, date = "2026-11-17", done = true))
        val today = tasksForToday(tasks, "2026-11-17")
        assertEquals(2, today.size)
        assertEquals(1, today.count { it.done })
        assertTrue(tasksForToday(tasks, "2026-11-18").isEmpty())
    }
}
