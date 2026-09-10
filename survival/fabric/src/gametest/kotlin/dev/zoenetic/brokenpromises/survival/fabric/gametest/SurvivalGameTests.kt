package dev.zoenetic.brokenpromises.survival.fabric.gametest

import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestHelper

class SurvivalGameTests {

    @GameTest
    fun aRealServerLevelIsAvailable(helper: GameTestHelper) {
        val level = helper.level
        check(!level.isClientSide) { "expected a server level" }
        check(level.server != null) { "expected a running server" }
        helper.succeed()
    }
}
