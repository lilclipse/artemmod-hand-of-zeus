package com.artem.artemmod.client.render;

import com.artem.artemmod.ArtemMod;
import com.artem.artemmod.entity.ZeusWatcherEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ZeusWatcherRenderer extends MobRenderer<ZeusWatcherEntity, HumanoidModel<ZeusWatcherEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ArtemMod.MOD_ID,
            "textures/entity/zeus.png"
    );

    public ZeusWatcherRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ZeusWatcherEntity entity) {
        return TEXTURE;
    }
}
