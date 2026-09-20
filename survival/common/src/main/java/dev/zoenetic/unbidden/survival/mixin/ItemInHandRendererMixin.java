package dev.zoenetic.unbidden.survival.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.zoenetic.unbidden.survival.fire.client.LightingCampfireRenderer;
import dev.zoenetic.unbidden.survival.vitals.client.ShiverRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void unbidden$shiverHands(float frameInterp, PoseStack poseStack,
                                            SubmitNodeCollector submitNodeCollector,
                                            LocalPlayer player, int lightCoords, CallbackInfo ci) {
        ShiverRenderer.INSTANCE.applyToHeldItems(player, frameInterp, poseStack);
    }

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void unbidden$beginLightingFrame(float frameInterp, PoseStack poseStack,
                                                   SubmitNodeCollector submitNodeCollector,
                                                   LocalPlayer player, int lightCoords, CallbackInfo ci) {
        LightingCampfireRenderer.INSTANCE.beginFrame(player, frameInterp);
    }

    @Inject(method = "renderPlayerArm", at = @At("HEAD"))
    private void unbidden$animateArmLightingCampfire(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                                           int lightCoords, float inverseArmHeight, float attackValue,
                                                           HumanoidArm arm, CallbackInfo ci) {
        LightingCampfireRenderer.INSTANCE.applyToArm(poseStack, arm, inverseArmHeight);
    }

    @Inject(method = "submitHandsWithItems", at = @At("TAIL"))
    private void unbidden$drawOffHandLightingCampfire(float frameInterp, PoseStack poseStack,
                                                            SubmitNodeCollector submitNodeCollector,
                                                            LocalPlayer player, int lightCoords, CallbackInfo ci) {
        if (player.isInvisible() || player.isScoping()) return;
        if (!player.getOffhandItem().isEmpty()) return;
        if (!LightingCampfireRenderer.INSTANCE.isLighting(player)) return;
        poseStack.pushPose();
        ((ItemInHandRendererInvoker) this).unbidden$renderPlayerArm(
                poseStack, submitNodeCollector, lightCoords, 0.0F, 0.0F, player.getMainArm().getOpposite());
        poseStack.popPose();
    }
}
