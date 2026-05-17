package com.artem.artemmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ZeusHandItem extends Item {
    private static final double RANGE = 64.0;
    private static final int COOLDOWN_TICKS = 40;

    public ZeusHandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

        BlockPos targetPos;
        if (hit.getType() == HitResult.Type.BLOCK) {
            targetPos = hit.getBlockPos().relative(hit.getDirection());
        } else {
            targetPos = player.blockPosition().relative(player.getDirection(), 10);
        }

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightning != null) {
            lightning.moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            lightning.setCause(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer ? serverPlayer : null);
            serverLevel.addFreshEntity(lightning);

            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            player.getItemInHand(hand).hurtAndBreak(
                    1,
                    player,
                    player.getEquipmentSlotForItem(player.getItemInHand(hand))
            );
        }

        return InteractionResult.SUCCESS;
    }
}
