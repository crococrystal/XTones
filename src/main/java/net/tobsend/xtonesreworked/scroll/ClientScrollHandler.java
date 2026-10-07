package net.tobsend.xtonesreworked.scroll;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tobsend.xtonesreworked.XtonesReworkedMod;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = XtonesReworkedMod.MODID, value = Dist.CLIENT)
public final class ClientScrollHandler {
    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null || !minecraft.player.isAlive()
                || minecraft.player.isSpectator() || minecraft.player.isUsingItem() || minecraft.getConnection() == null
                || !minecraft.getConnection().hasChannel(ScrollPayload.TYPE)
                || !BlockScrollCatalog.contains(minecraft.player.getMainHandItem().getItem())) {
            return;
        }

        long window = minecraft.getWindow().getWindow();
        if (!Screen.hasAltDown()
                && !InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SUPER)
                && !InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SUPER)) {
            return;
        }

        double delta = event.getScrollDeltaY() != 0 ? event.getScrollDeltaY() : -event.getScrollDeltaX();
        if (delta == 0) {
            return;
        }
        // Minecraft already applies wheel sensitivity and accumulates fractional trackpad movement.
        PacketDistributor.sendToServer(new ScrollPayload(delta > 0 ? 1 : -1,
                Screen.hasShiftDown(), minecraft.player.getInventory().selected));
        event.setCanceled(true);
    }
}
