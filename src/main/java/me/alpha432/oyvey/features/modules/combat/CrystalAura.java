package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

public class CrystalAura extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();

    public CrystalAura() {
        super("CrystalAura", Category.COMBAT, "Places and breaks crystals automatically.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (breakTimer.passedMs(150)) {
            breakNearbyCrystals();
            breakTimer.reset();
        }

        if (placeTimer.passedMs(300)) {
            placeCrystal();
            placeTimer.reset();
        }
    }

    private void breakNearbyCrystals() {
        for (EndCrystalEntity crystal : mc.world.getEntitiesByClass(EndCrystalEntity.class, mc.player.getBoundingBox().expand(4), e -> true)) {
            if (mc.player.squaredDistanceTo(crystal) > 25) continue;

            faceEntity(crystal);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
            return;
        }
    }

    private void placeCrystal() {
        if (!mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL)) return;

        BlockPos playerPos = mc.player.getBlockPos();
        for (BlockPos offset : BlockPos.iterateOutwards(playerPos, 4, 2, 4)) {
            if (!canPlaceCrystal(offset)) continue;

            Vec3d placeVec = Vec3d.ofCenter(offset);
            faceVector(placeVec);

            BlockHitResult hit = new BlockHitResult(placeVec, Direction.UP, offset, false);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        // Obsidian or bedrock base
        if (!mc.world.getBlockState(pos).isOf(Blocks.OBSIDIAN) && !mc.world.getBlockState(pos).isOf(Blocks.BEDROCK)) return false;
        // Two air blocks above
        return mc.world.getBlockState(pos.up()).isAir() && mc.world.getBlockState(pos.up(2)).isAir();
    }

    private void faceEntity(EndCrystalEntity entity) {
        Vec3d eyes = mc.player.getCameraPosVec(1.0f);
        Vec3d target = entity.getPos();

        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        mc.player.setYaw(smooth(mc.player.getYaw(), yaw, 30f));
        mc.player.setPitch(smooth(mc.player.getPitch(), pitch, 15f));
    }

    private void faceVector(Vec3d vec) {
        Vec3d eyes = mc.player.getCameraPosVec(1.0f);

        double dx = vec.x - eyes.x;
        double dy = vec.y - eyes.y;
        double dz = vec.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        mc.player.setYaw(smooth(mc.player.getYaw(), yaw, 20f));
        mc.player.setPitch(smooth(mc.player.getPitch(), pitch, 10f));
    }

    private float smooth(float current, float target, float maxDelta) {
        float delta = wrap(target - current);
        return current + Math.max(Math.min(delta, maxDelta), -maxDelta);
    }

    private float wrap(float angle) {
        angle %= 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}
