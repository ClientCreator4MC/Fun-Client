package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;

public class Velocity extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public Velocity() {
        super("Velocity", Category.COMBAT, "Reduces or cancels knockback.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.hurtTime > 0) {
            // Modify velocity after being hit
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * 0.05, vel.y * 0.0, vel.z * 0.05);
        }
    }
}
