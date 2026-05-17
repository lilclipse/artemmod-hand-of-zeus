package com.artem.artemmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ZeusHandItem extends Item {
    private static final int COOLDOWN_TICKS = 40;

    public ZeusHandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

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

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (lightning != null) {
            lightning.setPos(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);

            if (player instanceof ServerPlayer serverPlayer) {
                lightning.setCause(serverPlayer);
            }

            serverLevel.addFreshEntity(lightning);

            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            stack.hurtAndBreak(
                    1,
                    player,
                    player.getEquipmentSlotForItem(stack)
            );
        }

        return InteractionResult.SUCCESS;
    }
}
