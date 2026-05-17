package com.artem.artemmod.entity;

import com.artem.artemmod.ArtemMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    private static final ResourceLocation ZEUS_WATCHER_ID = ResourceLocation.fromNamespaceAndPath(
            ArtemMod.MOD_ID,
            "zeus_watcher"
    );

    private static final ResourceKey<EntityType<?>> ZEUS_WATCHER_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ZEUS_WATCHER_ID
    );

    public static final EntityType<ZeusWatcherEntity> ZEUS_WATCHER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ZEUS_WATCHER_KEY,
            EntityType.Builder.of(ZeusWatcherEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ZEUS_WATCHER_KEY)
    );

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(ZEUS_WATCHER, ZeusWatcherEntity.createAttributes());
    }
}
