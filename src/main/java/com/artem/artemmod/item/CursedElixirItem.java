package com.artem.artemmod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CursedElixirItem extends Item {
    private static final int DARKNESS_TICKS = 20 * 7;
    private static final int WITHER_TICKS = 20 * 8;

    public CursedElixirItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ZeusHandItem.resetDebt(serverPlayer);
        }

        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_TICKS, 0));
        player.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, 0));
        player.displayClientMessage(Component.literal("Зря ты это сделал."), false);

        ServerLevel serverLevel = (ServerLevel) level;
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 0.9F, 0.55F);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.45F, 0.7F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}
