package dev.zoenetic.brokenpromises.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Player.class)
public class PlayerMixin {
    /**
     * @author zoenetic
     * @reason brokenpromises:survival replaces vanilla exhaustion
     */
    @Overwrite
    public void causeFoodExhaustion(float amount) {
        // no op
    }
}
