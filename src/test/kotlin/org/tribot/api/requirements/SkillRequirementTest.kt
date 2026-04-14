package org.tribot.api.requirements

import io.mockk.every
import net.runelite.api.Skill
import org.tribot.api.ApiContext
import org.tribot.api.testing.fakeContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkillRequirementTest {

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
    }

    @Test
    fun `satisfied when level meets requirement`() {
        ApiContext.init(fakeContext {
            every { skills.getLevel(Skill.WOODCUTTING) } returns 60
        })
        val req = SkillRequirement(skill = Skill.WOODCUTTING, level = 60)
        assertTrue(req.check())
    }

    @Test
    fun `satisfied when level exceeds requirement`() {
        ApiContext.init(fakeContext {
            every { skills.getLevel(Skill.WOODCUTTING) } returns 75
        })
        val req = SkillRequirement(skill = Skill.WOODCUTTING, level = 60)
        assertTrue(req.check())
    }

    @Test
    fun `not satisfied when level is too low`() {
        ApiContext.init(fakeContext {
            every { skills.getLevel(Skill.WOODCUTTING) } returns 40
        })
        val req = SkillRequirement(skill = Skill.WOODCUTTING, level = 60)
        assertFalse(req.check())
    }

    @Test
    fun `boostable checks boosted level`() {
        ApiContext.init(fakeContext {
            every { skills.getLevel(Skill.COOKING) } returns 65
            every { skills.getBoostedLevel(Skill.COOKING) } returns 70
        })
        val req = SkillRequirement(skill = Skill.COOKING, level = 70, boostable = true)
        assertTrue(req.check())
    }

    @Test
    fun `non-boostable ignores boosted level`() {
        ApiContext.init(fakeContext {
            every { skills.getLevel(Skill.COOKING) } returns 65
            every { skills.getBoostedLevel(Skill.COOKING) } returns 70
        })
        val req = SkillRequirement(skill = Skill.COOKING, level = 70, boostable = false)
        assertFalse(req.check())
    }

    @Test
    fun `displayText without boost`() {
        val req = SkillRequirement(skill = Skill.WOODCUTTING, level = 60)
        assertEquals("60 Woodcutting", req.displayText)
    }

    @Test
    fun `displayText with boost`() {
        val req = SkillRequirement(skill = Skill.COOKING, level = 70, boostable = true)
        assertEquals("70 Cooking (boostable)", req.displayText)
    }
}
