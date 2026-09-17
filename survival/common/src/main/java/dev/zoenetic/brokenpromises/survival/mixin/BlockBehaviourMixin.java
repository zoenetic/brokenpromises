package dev.zoenetic.brokenpromises.survival.mixin;

import dev.zoenetic.brokenpromises.survival.block.FuelledLightSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void brokenpromises$dynamicLight(BlockBehaviour.Properties properties, CallbackInfo ci) {
        if (this instanceof FuelledLightSource fuelled) {
            properties.lightLevel(fuelled::lightLevel);
        }
    }
}
