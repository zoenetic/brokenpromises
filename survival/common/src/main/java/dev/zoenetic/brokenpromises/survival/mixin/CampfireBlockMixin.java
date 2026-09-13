package dev.zoenetic.brokenpromises.survival.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends Block {
    private CampfireBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void brokenpromises$defaultUnlit(CallbackInfo ci) {
        registerDefaultState(defaultBlockState().setValue(CampfireBlock.LIT, false));
    }

    @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
    private BlockState brokenpromises$placeUnlit(BlockState original, BlockPlaceContext context) {
        return original.setValue(CampfireBlock.LIT, false);
    }
}