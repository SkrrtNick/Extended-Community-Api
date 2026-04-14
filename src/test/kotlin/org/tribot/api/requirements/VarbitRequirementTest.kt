package org.tribot.api.requirements

import io.mockk.every
import org.tribot.api.ApiContext
import org.tribot.api.testing.fakeContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VarbitRequirementTest {

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
    }

    @Test
    fun `exact match satisfied`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 5
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.EQUAL)
        assertTrue(req.check())
    }

    @Test
    fun `exact match not satisfied`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 3
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.EQUAL)
        assertFalse(req.check())
    }

    @Test
    fun `GREATER_EQUAL satisfied when equal`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 5
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.GREATER_EQUAL)
        assertTrue(req.check())
    }

    @Test
    fun `GREATER_EQUAL satisfied when greater`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 10
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.GREATER_EQUAL)
        assertTrue(req.check())
    }

    @Test
    fun `GREATER_EQUAL not satisfied when less`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 3
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.GREATER_EQUAL)
        assertFalse(req.check())
    }

    @Test
    fun `LESS operation satisfied`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 3
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.LESS)
        assertTrue(req.check())
    }

    @Test
    fun `LESS operation not satisfied when equal`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 5
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.LESS)
        assertFalse(req.check())
    }

    @Test
    fun `GREATER operation satisfied`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 6
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.GREATER)
        assertTrue(req.check())
    }

    @Test
    fun `NOT_EQUAL operation satisfied`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 3
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.NOT_EQUAL)
        assertTrue(req.check())
    }

    @Test
    fun `LESS_EQUAL operation satisfied when equal`() {
        ApiContext.init(fakeContext {
            every { client.getVarbitValue(1234) } returns 5
        })
        val req = VarbitRequirement(varbitId = 1234, value = 5, operation = Operation.LESS_EQUAL)
        assertTrue(req.check())
    }
}
