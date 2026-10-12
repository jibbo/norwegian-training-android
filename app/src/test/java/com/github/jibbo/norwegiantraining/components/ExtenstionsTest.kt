package com.github.jibbo.norwegiantraining.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtenstionsTest {
    @Test
    fun emojiRemover(){
        var s = "\uD83D\uDC3E First steps"
        assertEquals(" First steps", s.removeEmoji())

        s = "\uD83E\uDDB6\uD83E\uDDB6 Beginner 3"
        assertEquals(" Beginner 3", s.removeEmoji())
    }
}