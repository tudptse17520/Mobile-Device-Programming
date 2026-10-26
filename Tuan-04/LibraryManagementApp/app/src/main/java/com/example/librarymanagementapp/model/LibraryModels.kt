package com.example.librarymanagementapp.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface Borrowable {
    fun borrow(student: Student): Boolean
    fun returnItem(): Boolean
    fun isAvailable(): Boolean
    fun getLoanDurationDays(): Int
    fun calculateLateFee(daysLate: Int): Long
}

abstract class Person(
    val id: String,
    val name: String
) {
    abstract fun getRole(): String
}

class Student(
    id: String,
    name: String,
    val major: String = "Công nghệ Thông tin"
) : Person(id, name) {
    override fun getRole(): String = "Sinh viên"

    override fun toString(): String = "$name ($id - $major)"
}

abstract class Book(
    val id: String,
    val title: String,
    val author: String
) : Borrowable {

    private var _borrowedByStudentId by mutableStateOf<String?>(null)
    private var _borrowedByStudentName by mutableStateOf<String?>(null)

    val borrowedByStudentId: String? get() = _borrowedByStudentId
    val borrowedByStudentName: String? get() = _borrowedByStudentName

    override fun isAvailable(): Boolean = _borrowedByStudentId == null

    override fun borrow(student: Student): Boolean {
        if (!isAvailable()) {
            return false
        }
        _borrowedByStudentId = student.id
        _borrowedByStudentName = student.name
        return true
    }

    override fun returnItem(): Boolean {
        if (isAvailable()) {
            return false
        }
        _borrowedByStudentId = null
        _borrowedByStudentName = null
        return true
    }

    abstract fun getBookTypeLabel(): String
    abstract fun getSpecialAttribute(): String
}

class TextBook(
    id: String,
    title: String,
    author: String,
    val subject: String,
    val semester: Int
) : Book(id, title, author) {

    override fun getLoanDurationDays(): Int = 14

    override fun calculateLateFee(daysLate: Int): Long = daysLate * 2000L

    override fun getBookTypeLabel(): String = "Giáo trình"

    override fun getSpecialAttribute(): String = "Môn học: $subject (HK $semester)"
}

class ReferenceBook(
    id: String,
    title: String,
    author: String,
    val field: String,
    val publisher: String
) : Book(id, title, author) {

    override fun getLoanDurationDays(): Int = 7

    override fun calculateLateFee(daysLate: Int): Long = daysLate * 5000L

    override fun getBookTypeLabel(): String = "Sách tham khảo"

    override fun getSpecialAttribute(): String = "Lĩnh vực: $field - NXB: $publisher"
}

object SampleData {
    val students = listOf(
        Student("SV01", "Nguyen Van A", "Công nghệ Thông tin"),
        Student("SV02", "Nguyen Thi B", "Hệ thống Thông tin"),
        Student("SV03", "Nguyen Van C", "Khoa học Máy tính")
    )

    fun createInitialBooks(students: List<Student>): List<Book> {
        val s1 = TextBook("B01", "Sách 01", "TS. Trần Văn Hào", "Lập trình Di động", 4)
        val s2 = TextBook("B02", "Sách 02", "PGS. Lê Minh", "Thuật toán", 3)
        val s3 = ReferenceBook("B03", "Sách 03", "Gang of Four", "Kiến trúc phần mềm", "O'Reilly")
        val s4 = ReferenceBook("B04", "Sách 04", "Robert C. Martin", "Kỹ thuật lập trình", "Prentice Hall")
        val s5 = TextBook("B05", "Sách 05", "ThS. Hoàng Lan", "Hệ quản trị CSDL", 2)

        s1.borrow(students[0])
        s2.borrow(students[0])

        return listOf(s1, s2, s3, s4, s5)
    }
}
