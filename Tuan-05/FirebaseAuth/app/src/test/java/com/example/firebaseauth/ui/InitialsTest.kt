package com.example.firebaseauth.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialsTest {

    @Test
    fun initials_of_two_words() {
        assertEquals("TD", initialsOf("Tu Doan"))
    }

    @Test
    fun initials_of_three_words() {
        assertEquals("NA", initialsOf("Nguyễn Văn An"))
    }

    @Test
    fun initials_of_single_word() {
        assertEquals("DO", initialsOf("Doan"))
    }

    @Test
    fun initials_of_empty_name() {
        assertEquals("UT", initialsOf("   "))
    }
}
