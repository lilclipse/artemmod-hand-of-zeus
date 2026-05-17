package com.artem.artemmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ZeusGuideBookItem extends Item {
    private static final int DANGEROUS_KARMA = 30;
    private static final float PUNISHMENT_CHANCE = 0.50F;

    public ZeusGuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        showGuide(serverPlayer);

        int karma = ZeusHandItem.getKarma(serverPlayer);
        ServerLevel serverLevel = (ServerLevel) level;
        if (karma > DANGEROUS_KARMA && serverLevel.random.nextFloat() < PUNISHMENT_CHANCE) {
            punishReader(serverLevel, serverPlayer, stack);
        }

        return InteractionResult.SUCCESS;
    }

    private void showGuide(ServerPlayer player) {
        player.displayClientMessage(Component.literal("§6Книга грома"), false);
        player.displayClientMessage(Component.literal("§7Рука отвечает на ПКМ. Иногда не только молнией."), false);
        player.displayClientMessage(Component.literal("§7Некоторые поступки оставляют след. Его можно стереть, но не бесплатно."), false);
        player.displayClientMessage(Component.literal("§7Если увидишь того, кто смотрит, не задерживай взгляд."), false);
        player.displayClientMessage(Component.literal("§8Текущая карма: " + ZeusHandItem.getKarma(player)), false);
    }

    private void punishReader(ServerLevel level, ServerPlayer player, ItemStack stack) {
        BlockPos pos = player.blockPosition();
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (lightning != null) {
            lightning.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            lightning.setCause(player);
            level.addFreshEntity(lightning);
        }

        player.displayClientMessage(Component.literal("§5Ты слишком много знаешь."), false);
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8F, 0.35F);
        level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0F, 0.8F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
