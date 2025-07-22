package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class ReverseStep extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public ReverseStep() {
        super("ReverseStep", Category.MOVEMENT, "Faster falling down ledges safely.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isInLava() || mc.player.isInWater() || mc.player.isClimbing()) return;

        BlockPos below = mc.player.getBlockPos().down();
        double yDiff = mc.player.getY() - below.getY();

        // Check for actual ledge drop
        if (yDiff <= 1.0 && !mc.world.getBlockState(below).isAir()) return;

        // Apply fast fall (limited to -0.8 to be safe from Grim motion flags)
        mc.player.setVelocity(mc.player.getVelocity().x, -0.8, mc.player.getVelocity().z);
    }
}
