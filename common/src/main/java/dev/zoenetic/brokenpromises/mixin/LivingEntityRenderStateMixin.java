package dev.zoenetic.brokenpromises.mixin;

import dev.zoenetic.brokenpromises.effects.player.ShiverState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ShiverState {
    @Unique
    private double brokenpromises$shiver;

    @Override
    public double brokenpromises$getShiver() {
        return brokenpromises$shiver;
    }

    @Override
    public void brokenpromises$setShiver(double shiver) {
        brokenpromises$shiver = shiver;
    }
}
