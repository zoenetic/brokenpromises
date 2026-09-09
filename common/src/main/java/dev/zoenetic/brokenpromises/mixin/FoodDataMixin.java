package dev.zoenetic.brokenpromises.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FoodData.class)
public class FoodDataMixin {
    /**
     * @author zoenetic
     * @reason brokenpromises:survival replaces vanilla exhaustion
     */
    @Overwrite()
    public void tick(ServerPlayer player) {
        //no op
    }
}
