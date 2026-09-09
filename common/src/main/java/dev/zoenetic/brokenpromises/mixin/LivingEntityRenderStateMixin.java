package dev.zoenetic.brokenpromises.mixin;

import dev.zoenetic.brokenpromises.effects.player.ShiverState;
import dev.zoenetic.brokenpromises.effects.player.ShiveringKt;
import dev.zoenetic.brokenpromises.vitals.Vitals;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.NonNull;
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
    public void brokenpromises$setShiver(@NonNull Vitals vitals) {
        brokenpromises$shiver = ShiveringKt.shiverIntensity(vitals);
    }
}
