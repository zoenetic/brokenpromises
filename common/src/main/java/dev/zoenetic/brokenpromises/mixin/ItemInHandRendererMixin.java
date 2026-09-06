package dev.zoenetic.brokenpromises.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.zoenetic.brokenpromises.effects.player.ShiveringKt;
import dev.zoenetic.brokenpromises.vitals.VitalsKt;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.zoenetic.brokenpromises.effects.player.ShiveringKt.SHIVER_AMPLITUDE_DEGREES;
import static dev.zoenetic.brokenpromises.effects.player.ShiveringKt.SHIVER_FREQUENCY;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void brokenpromises$shiverHands(float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                            LocalPlayer player, int lightCoords, CallbackInfo ci) {
        double shiver = ShiveringKt.shiverIntensity(VitalsKt.vitals(player).getTemperature().getValue());
        if (shiver <= 0.0) return;
        float phase = (float) ((player.tickCount + frameInterp) * SHIVER_FREQUENCY);
        float roll = (float) (Math.sin(phase) * SHIVER_AMPLITUDE_DEGREES * shiver);
        float yaw = (float) (Math.sin(phase * 1.37) * SHIVER_AMPLITUDE_DEGREES * 0.25 * shiver);
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
    }
}
