package com.artem.artemmod.client;

import com.artem.artemmod.client.render.ZeusWatcherRenderer;
import com.artem.artemmod.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ArtemModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.ZEUS_WATCHER, ZeusWatcherRenderer::new);
    }
}
