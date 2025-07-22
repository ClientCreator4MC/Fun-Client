package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public NoFall() {
        super("NoFall", Category.MOVEMENT, "Prevents fall damage.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        if (mc.player.fallDistance > 2.5f) {
            // Send safe on-ground spoof packet
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
    }
}
