package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class KillAura extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Timer timer = new Timer();
    private Entity target;

    public KillAura() {
        super("KillAura", Category.COMBAT, "Attacks nearby entities automatically.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        target = findTarget(6.0); // 6-block reach max
        if (target != null && timer.passedMs(getCooldown())) {
            smoothLookAt(target);
            attack(target);
            timer.reset();
        }
    }

    private Entity findTarget(double range) {
        return mc.world.getEntities()
            .stream()
            .filter(e -> e instanceof LivingEntity && e.isAlive() && e != mc.player)
            .filter(e -> mc.player.squaredDistanceTo(e) < range * range)
            .filter(e -> !(e instanceof PlayerEntity p && p.isCreative()))
            .min((a, b) -> Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b)))
            .orElse(null);
    }

    private void smoothLookAt(Entity entity) {
        Vec3d targetPos = entity.getPos().add(0, entity.getHeight() / 2.0, 0);
        Vec3d playerEye = mc.player.getCameraPosVec(1.0f);

        double dx = targetPos.x - playerEye.x;
        double dy = targetPos.y - playerEye.y;
        double dz = targetPos.z - playerEye.z;

        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        mc.player.setYaw(smooth(mc.player.getYaw(), yaw, 35f));
        mc.player.setPitch(smooth(mc.player.getPitch(), pitch, 15f));
    }

    private float smooth(float current, float target, float speed) {
        float delta = wrapDegrees(target - current);
        return current + Math.max(Math.min(delta, speed), -speed);
    }

    private float wrapDegrees(float degrees) {
        degrees = degrees % 360.0F;
        if (degrees >= 180.0F) degrees -= 360.0F;
        if (degrees < -180.0F) degrees += 360.0F;
        return degrees;
    }

    private void attack(Entity target) {
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
    }

    private long getCooldown() {
        double base = 1000.0 / mc.player.getAttackSpeed(); // Adjust for attributes
        return (long) base;
    }
}
