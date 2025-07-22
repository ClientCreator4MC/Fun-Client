package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;

public class FastPlace extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public FastPlace() {
        super("FastPlace", Category.COMBAT, "Places blocks/items faster.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Safe method: resets delay only if holding usable item
        if (mc.player.getMainHandStack().getItem().getMaxUseTime() <= 0) {
            mc.itemUseCooldown = 0; // resets delay between uses (safe method)
        }
    }
}
