package dev.zoenetic.brokenpromises.survival.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.zoenetic.brokenpromises.survival.block.CampfireKt.maybeLightCampfire;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends Block {
    private CampfireBlockMixin(Properties properties) {
        super(properties);
    }

    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos,
            @NonNull Player player, @NonNull BlockHitResult hit
    ) {
        return maybeLightCampfire(state, level, pos, player);
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