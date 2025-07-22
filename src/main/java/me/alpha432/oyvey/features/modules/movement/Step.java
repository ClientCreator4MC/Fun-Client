package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Step extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean stepping = false;

    public Step() {
        super("Step", Category.MOVEMENT, "Allows you to step up blocks safely.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround() || mc.player.isSneaking()) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.isClimbing()) return;

        Vec3d vel = mc.player.getVelocity();
        BlockPos pos = mc.player.getBlockPos();

        // Check for step opportunity
        if (mc.player.horizontalCollision && canStep(pos)) {
            doStep();
        }

        // Reset flag after stepping
        if (stepping && mc.player.getY() % 1 == 0) {
            stepping = false;
        }
    }

    private boolean canStep(BlockPos pos) {
        BlockPos head = pos.up();
        BlockPos aboveHead = pos.up(2);

        return mc.world.getBlockState(head).isAir() &&
               mc.world.getBlockState(aboveHead).isAir();
    }

    private void doStep() {
        // Simulate vanilla step (split into multiple Y motions)
        mc.player.setVelocity(mc.player.getVelocity().x, 0.42f, mc.player.getVelocity().z); // Stage 1
        stepping = true;
    }
}
