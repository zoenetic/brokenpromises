package dev.zoenetic.brokenpromises.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.zoenetic.brokenpromises.effects.player.ShiverState;
import dev.zoenetic.brokenpromises.vitals.VitalsKt;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.zoenetic.brokenpromises.effects.player.ShiveringKt.SHIVER_AMPLITUDE_DEGREES;
import static dev.zoenetic.brokenpromises.effects.player.ShiveringKt.SHIVER_FREQUENCY;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void brokenpromises$afterExtractRenderState(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof Player player) {
            var vitals = VitalsKt.vitals(player);
            ((ShiverState) state).brokenpromises$setShiver(vitals);
        }
    }

    @ModifyVariable(method = "setupRotations", at = @At("HEAD"), argsOnly = true, name = "bodyRot")
    private float brokenPromises$shiver(float bodyRot, @Local(argsOnly = true) LivingEntityRenderState state) {
        double shiver = ((ShiverState) state).brokenpromises$getShiver();
        if (shiver <= 0.0) return bodyRot;
        return bodyRot + (float) (shiver * SHIVER_AMPLITUDE_DEGREES * Math.sin(state.ageInTicks * SHIVER_FREQUENCY));
    }
}
