package org.tribot.api.requirements

import io.mockk.every
import net.runelite.api.Skill
import org.tribot.api.ApiContext
import org.tribot.api.testing.fakeContext
import org.tribot.automation.script.core.tabs.InventoryItem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompositeRequirementTest {

    @BeforeTest
    fun setUp() {
        ApiContext.init(fakeContext {
            every { skills.getLevel(Skill.WOODCUTTING) } returns 60
            every { skills.getLevel(Skill.COOKING) } returns 30
            every { inventory.getItems() } returns listOf(
                InventoryItem(995, 500, 0)
            )
        })
    }

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
    }

    // A requirement that always passes in our test context
    private val passingSkillReq = SkillRequirement(Skill.WOODCUTTING, 50)

    // A requirement that always fails in our test context
    private val failingSkillReq = SkillRequirement(Skill.COOKING, 50)

    // Another passing requirement
    private val passingItemReq = ItemRequirement(itemId = 995, quantity = 100, displayName = "Coins")

    @Test
    fun `AND passes when all requirements pass`() {
        val composite = CompositeRequirement(
            LogicType.AND,
            listOf(passingSkillReq, passingItemReq)
        )
        assertTrue(composite.check())
    }

    @Test
    fun `AND fails when one requirement fails`() {
        val composite = CompositeRequirement(
            LogicType.AND,
            listOf(passingSkillReq, failingSkillReq)
        )
        assertFalse(composite.check())
    }

    @Test
    fun `AND fails when all requirements fail`() {
        val composite = CompositeRequirement(
            LogicType.AND,
            listOf(failingSkillReq, SkillRequirement(Skill.COOKING, 99))
        )
        assertFalse(composite.check())
    }

    @Test
    fun `OR passes when any requirement passes`() {
        val composite = CompositeRequirement(
            LogicType.OR,
            listOf(passingSkillReq, failingSkillReq)
        )
        assertTrue(composite.check())
    }

    @Test
    fun `OR passes when all requirements pass`() {
        val composite = CompositeRequirement(
            LogicType.OR,
            listOf(passingSkillReq, passingItemReq)
        )
        assertTrue(composite.check())
    }

    @Test
    fun `OR fails when all requirements fail`() {
        val composite = CompositeRequirement(
            LogicType.OR,
            listOf(failingSkillReq, SkillRequirement(Skill.COOKING, 99))
        )
        assertFalse(composite.check())
    }

    @Test
    fun `NOR passes when none pass`() {
        val composite = CompositeRequirement(
            LogicType.NOR,
            listOf(failingSkillReq, SkillRequirement(Skill.COOKING, 99))
        )
        assertTrue(composite.check())
    }

    @Test
    fun `NOR fails when any passes`() {
        val composite = CompositeRequirement(
            LogicType.NOR,
            listOf(passingSkillReq, failingSkillReq)
        )
        assertFalse(composite.check())
    }

    @Test
    fun `NAND passes when at least one fails`() {
        val composite = CompositeRequirement(
            LogicType.NAND,
            listOf(passingSkillReq, failingSkillReq)
        )
        assertTrue(composite.check())
    }

    @Test
    fun `NAND fails when all pass`() {
        val composite = CompositeRequirement(
            LogicType.NAND,
            listOf(passingSkillReq, passingItemReq)
        )
        assertFalse(composite.check())
    }

    @Test
    fun `XOR passes when exactly one passes`() {
        val composite = CompositeRequirement(
            LogicType.XOR,
            listOf(passingSkillReq, failingSkillReq)
        )
        assertTrue(composite.check())
    }

    @Test
    fun `XOR fails when all pass`() {
        val composite = CompositeRequirement(
            LogicType.XOR,
            listOf(passingSkillReq, passingItemReq)
        )
        assertFalse(composite.check())
    }

    @Test
    fun `XOR fails when none pass`() {
        val composite = CompositeRequirement(
            LogicType.XOR,
            listOf(failingSkillReq, SkillRequirement(Skill.COOKING, 99))
        )
        assertFalse(composite.check())
    }

    @Test
    fun `Requirements all helper creates AND composite`() {
        val composite = Requirements.all(passingSkillReq, passingItemReq)
        assertTrue(composite.check())

        val composite2 = Requirements.all(passingSkillReq, failingSkillReq)
        assertFalse(composite2.check())
    }

    @Test
    fun `Requirements any helper creates OR composite`() {
        val composite = Requirements.any(passingSkillReq, failingSkillReq)
        assertTrue(composite.check())

        val composite2 = Requirements.any(failingSkillReq, SkillRequirement(Skill.COOKING, 99))
        assertFalse(composite2.check())
    }

    @Test
    fun `Requirements none helper creates NOR composite`() {
        val composite = Requirements.none(failingSkillReq)
        assertTrue(composite.check())

        val composite2 = Requirements.none(passingSkillReq)
        assertFalse(composite2.check())
    }

    @Test
    fun `Requirements not helper creates NOR with single requirement`() {
        val composite = Requirements.not(failingSkillReq)
        assertTrue(composite.check())

        val composite2 = Requirements.not(passingSkillReq)
        assertFalse(composite2.check())
    }

    @Test
    fun `displayText uses custom name when provided`() {
        val composite = CompositeRequirement(
            LogicType.AND,
            listOf(passingSkillReq, passingItemReq),
            name = "All combat requirements"
        )
        assertEquals("All combat requirements", composite.displayText)
    }

    @Test
    fun `displayText joins requirement text when no custom name`() {
        val composite = CompositeRequirement(
            LogicType.AND,
            listOf(passingSkillReq, passingItemReq)
        )
        val expected = "${passingSkillReq.displayText} AND ${passingItemReq.displayText}"
        assertEquals(expected, composite.displayText)
    }

    @Test
    fun `nested composite requirements work correctly`() {
        val inner = Requirements.all(passingSkillReq, passingItemReq)
        val outer = Requirements.any(inner, failingSkillReq)
        assertTrue(outer.check())
    }
}
