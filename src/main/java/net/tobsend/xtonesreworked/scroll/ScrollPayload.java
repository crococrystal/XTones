package net.tobsend.xtonesreworked.scroll;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.tobsend.xtonesreworked.XtonesReworkedMod;

public record ScrollPayload(int direction, boolean families, int slot) implements CustomPacketPayload {
    public static final Type<ScrollPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(XtonesReworkedMod.MODID, "scroll_block"));
    public static final StreamCodec<ByteBuf, ScrollPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ScrollPayload::direction,
        ByteBufCodecs.BOOL, ScrollPayload::families,
        ByteBufCodecs.VAR_INT, ScrollPayload::slot,
        ScrollPayload::new
    );

    @Override
    public Type<ScrollPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("scroll-1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) apply(player, payload);
        });
    }

    static void apply(ServerPlayer player, ScrollPayload payload) {
        if ((payload.direction != -1 && payload.direction != 1) || payload.slot < 0 || payload.slot > 8
            || player.getInventory().selected != payload.slot || !player.isAlive() || player.isSpectator()
            || player.containerMenu != player.inventoryMenu || player.isUsingItem()) return;
        ItemStack held = player.getInventory().getItem(payload.slot);
        if (held.isEmpty() || !BlockScrollCatalog.contains(held.getItem())) return;
        ItemStack replacement = BlockScrollCatalog.convert(held, payload.direction, payload.families);
        if (replacement == held) return;
        player.getInventory().setItem(payload.slot, replacement);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
    }
}
