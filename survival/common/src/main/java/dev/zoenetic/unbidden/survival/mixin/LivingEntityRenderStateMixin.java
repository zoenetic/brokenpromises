package dev.zoenetic.unbidden.survival.mixin;

import dev.zoenetic.unbidden.survival.fire.client.LightingCampfireState;
import dev.zoenetic.unbidden.survival.vitals.client.ShiverState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ShiverState, LightingCampfireState {
    @Unique
    private double unbidden$shiver;
    @Unique
    private boolean unbidden$lightingCampfire;

    @Override
    public double unbidden$getShiver() {
        return unbidden$shiver;
    }

    @Override
    public void unbidden$setShiver(double shiver) {
        unbidden$shiver = shiver;
    }

    @Override
    public boolean unbidden$getLightingCampfire() {
        return unbidden$lightingCampfire;
    }

    @Override
    public void unbidden$setLightingCampfire(boolean state) {
        unbidden$lightingCampfire = state;
    }
}
