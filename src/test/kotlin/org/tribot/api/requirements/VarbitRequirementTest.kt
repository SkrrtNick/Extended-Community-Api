package org.tribot.api.requirements

import io.mockk.every
import org.tribot.api.testing.fakeContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VarbitRequirementTest {

    @Test
    fun `exact match satisfied`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 5
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.EQUAL)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `exact match not satisfied`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 3
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.EQUAL)
        assertFalse(req.check(ctx))
    }

    @Test
    fun `GREATER_EQUAL satisfied when equal`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 5
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.GREATER_EQUAL)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `GREATER_EQUAL satisfied when greater`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 10
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.GREATER_EQUAL)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `GREATER_EQUAL not satisfied when less`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 3
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.GREATER_EQUAL)
        assertFalse(req.check(ctx))
    }

    @Test
    fun `LESS operation satisfied`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 3
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.LESS)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `LESS operation not satisfied when equal`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 5
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.LESS)
        assertFalse(req.check(ctx))
    }

    @Test
    fun `GREATER operation satisfied`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 6
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.GREATER)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `NOT_EQUAL operation satisfied`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 3
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.NOT_EQUAL)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `LESS_EQUAL operation satisfied when equal`() {
        val ctx = fakeContext {
            every { client.getVarbitValue(1234) } returns 5
        }
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.LESS_EQUAL)
        assertTrue(req.check(ctx))
    }
}
