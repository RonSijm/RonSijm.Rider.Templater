package ronsijm.templater.script.evaluators

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ParserStateTest {


    @Test
    fun `initial state is at top level`() {
        val state = ParserState()
        assertTrue(state.isAtTopLevel())
    }

    @Test
    fun `initial state is not in quotes`() {
        val state = ParserState()
        assertTrue(state.isNotInQuotes())
    }

    @Test
    fun `initial state is at zero paren depth`() {
        val state = ParserState()
        assertTrue(state.isAtZeroParenDepth())
    }


    @Test
    fun `double quote enters quote mode`() {
        val state = ParserState()
        state.updateForChar('"')
        assertFalse(state.isNotInQuotes())
        assertFalse(state.isAtTopLevel())
    }

    @Test
    fun `matching double quote exits quote mode`() {
        val state = ParserState()
        state.updateForChar('"')
        state.updateForChar('"')
        assertTrue(state.isNotInQuotes())
        assertTrue(state.isAtTopLevel())
    }

    @Test
    fun `single quote enters quote mode`() {
        val state = ParserState()
        state.updateForChar('\'')
        assertFalse(state.isNotInQuotes())
    }

    @Test
    fun `backtick enters quote mode`() {
        val state = ParserState()
        state.updateForChar('`')
        assertFalse(state.isNotInQuotes())
    }

    @Test
    fun `mismatched quote does not exit quote mode`() {
        val state = ParserState()
        state.updateForChar('"')
        state.updateForChar('\'')
        assertFalse(state.isNotInQuotes())
    }


    @Test
    fun `open paren increases depth`() {
        val state = ParserState()
        state.updateForChar('(')
        assertFalse(state.isAtZeroParenDepth())
        assertFalse(state.isAtTopLevel())
    }

    @Test
    fun `close paren decreases depth`() {
        val state = ParserState()
        state.updateForChar('(')
        state.updateForChar(')')
        assertTrue(state.isAtZeroParenDepth())
        assertTrue(state.isAtTopLevel())
    }

    @Test
    fun `nested parens track depth correctly`() {
        val state = ParserState()
        state.updateForChar('(')
        state.updateForChar('(')
        assertFalse(state.isAtZeroParenDepth())
        state.updateForChar(')')
        assertFalse(state.isAtZeroParenDepth())
        state.updateForChar(')')
        assertTrue(state.isAtZeroParenDepth())
    }

    @Test
    fun `parens inside quotes are ignored`() {
        val state = ParserState()
        state.updateForChar('"')
        state.updateForChar('(')
        assertTrue(state.isAtZeroParenDepth())
    }


    @Test
    fun `open bracket affects top level`() {
        val state = ParserState()
        state.updateForChar('[')
        assertFalse(state.isAtTopLevel())
    }

    @Test
    fun `close bracket restores top level`() {
        val state = ParserState()
        state.updateForChar('[')
        state.updateForChar(']')
        assertTrue(state.isAtTopLevel())
    }


    @Test
    fun `open brace affects top level`() {
        val state = ParserState()
        state.updateForChar('{')
        assertFalse(state.isAtTopLevel())
    }

    @Test
    fun `close brace restores top level`() {
        val state = ParserState()
        state.updateForChar('{')
        state.updateForChar('}')
        assertTrue(state.isAtTopLevel())
    }


    @Test
    fun `reset restores initial state`() {
        val state = ParserState()
        state.updateForChar('"')
        state.updateForChar('(')
        state.updateForChar('[')
        state.updateForChar('{')

        state.reset()

        assertTrue(state.isAtTopLevel())
        assertTrue(state.isNotInQuotes())
        assertTrue(state.isAtZeroParenDepth())
    }


    @Test
    fun `complex expression tracking`() {
        val state = ParserState()

        state.updateForChar('f')
        state.updateForChar('u')
        state.updateForChar('n')
        state.updateForChar('c')
        state.updateForChar('(')
        assertFalse(state.isAtTopLevel())
        state.updateForChar('"')
        state.updateForChar('a')
        state.updateForChar('r')
        state.updateForChar('g')
        state.updateForChar('"')
        state.updateForChar(',')
        state.updateForChar(' ')
        state.updateForChar('[')
        state.updateForChar('1')
        state.updateForChar(',')
        state.updateForChar(' ')
        state.updateForChar('2')
        state.updateForChar(']')
        state.updateForChar(')')
        assertTrue(state.isAtTopLevel())
    }
}
