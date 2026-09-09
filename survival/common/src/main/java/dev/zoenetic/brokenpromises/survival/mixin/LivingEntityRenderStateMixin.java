package dev.zoenetic.brokenpromises.survival.mixin;

import dev.zoenetic.brokenpromises.survival.client.ShiverState;
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
