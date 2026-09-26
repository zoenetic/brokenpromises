package dev.zoenetic.unbidden.survival.datagen.models

import dev.zoenetic.unbidden.survival.registry.UnbiddenItems
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.MultiVariant
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.*
import net.minecraft.client.renderer.block.dispatch.Variant
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.util.random.WeightedList
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING
import java.util.function.BiConsumer
import java.util.function.Consumer

fun createTorch(
    ground: Block,
    wall: Block,
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
) {
    val material = Material(Identifier.withDefaultNamespace("block/torch"))
    val particleMaterial = Material(Identifier.withDefaultNamespace("block/oak_log"))
    val textures = TextureMapping()
        .put(TextureSlot.TORCH, material)
        .putForced(TextureSlot.PARTICLE, particleMaterial)

    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(
            ground, MultiVariant(
                WeightedList.of(
                    Variant(
                        ModelTemplates.TORCH.create(
                            ground, textures, modelOutput
                        )
                    )
                )
            )
        )
    )

    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(
            wall, MultiVariant(
                WeightedList.of(
                    Variant(
                        ModelTemplates.WALL_TORCH.create(
                            wall, textures, modelOutput
                        )
                    )
                )
            )
        ).with(
            PropertyDispatch.modify(HORIZONTAL_FACING)
                .select(Direction.EAST, BlockModelGenerators.NOP)
                .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_90)
                .select(Direction.WEST, BlockModelGenerators.Y_ROT_180)
                .select(Direction.NORTH, BlockModelGenerators.Y_ROT_270)
        )
    )

    itemModelOutput.accept(
        UnbiddenItems.TORCH.asItem(),
        ItemModelUtils.plainModel(
            ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(ground.asItem()),
                TextureMapping.layer0(material),
                modelOutput
            )
        )
    )
}