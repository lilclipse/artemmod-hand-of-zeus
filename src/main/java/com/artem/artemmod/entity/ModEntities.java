package com.artem.artemmod.entity;

import com.artem.artemmod.ArtemMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final EntityType<ZeusWatcherEntity> ZEUS_WATCHER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ArtemMod.MOD_ID, "zeus_watcher"),
            FabricEntityType.Builder.create(MobCategory.MONSTER, ZeusWatcherEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(1)
                    .build()
    );

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(ZEUS_WATCHER, ZeusWatcherEntity.createAttributes());
    }
}
