package dev.zoenetic.brokenpromises.survival.neoforge.gametest

import com.mojang.serialization.MapCodec
import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.gametest.SurvivalTest
import dev.zoenetic.brokenpromises.survival.gametest.SurvivalTests
import net.minecraft.core.Holder
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.gametest.framework.GameTestInstance
import net.minecraft.gametest.framework.TestData
import net.minecraft.gametest.framework.TestEnvironmentDefinition
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Rotation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.RegisterGameTestsEvent

@EventBusSubscriber(modid = Survival.MOD_ID)
public object SurvivalGameTests {

    private val DEFAULT_ENVIRONMENT: Holder<TestEnvironmentDefinition<*>> =
        Holder.direct<TestEnvironmentDefinition<*>>(
            TestEnvironmentDefinition.AllOf(emptyList<Holder<TestEnvironmentDefinition<*>>>())
        )

    private val EMPTY_STRUCTURE: Identifier = Identifier.withDefaultNamespace("empty")

    @SubscribeEvent
    @JvmStatic
    public fun register(event: RegisterGameTestsEvent) {
        for (test in SurvivalTests.ALL) {
            event.registerTest(
                Identifier.fromNamespaceAndPath(Survival.NAMESPACE, test.name),
                SharedGameTest(
                    test,
                    TestData(
                        DEFAULT_ENVIRONMENT,
                        EMPTY_STRUCTURE,
                        test.maxTicks,
                        0,
                        true,
                        Rotation.NONE,
                    ),
                ),
            )
        }
    }
}

private class SharedGameTest(
    private val test: SurvivalTest,
    data: TestData<Holder<TestEnvironmentDefinition<*>>>,
) : GameTestInstance(data) {

    override fun run(helper: GameTestHelper) {
        test.run(helper)
    }

    override fun codec(): MapCodec<out GameTestInstance> = MapCodec.unit(this)

    override fun typeDescription(): MutableComponent = Component.literal(test.name)
}
