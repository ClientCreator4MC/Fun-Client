package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class XRayESP extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Set<Block> targets = new HashSet<>();
    private boolean active = false;

    // Toggle keybind (press X)
    private final KeyBinding toggleKey = new KeyBinding(
        "key.xray.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "XRay");

    public XRayESP() {
        super("XRayESP", Category.RENDER, "Highlights ores, water, and lava with outlines.");
        registerEvents();
    }

    private void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                active = !active;
                client.player.sendMessage(Text.of("XRay: " + (active ? "ON" : "OFF")), true);
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            if (!active || mc.player == null || mc.world == null) return;

            Vec3d camPos = context.camera().getPos();
            RenderLayer layer = RenderLayer.getLines();
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            RenderSystem.lineWidth(1.5F);

            BufferBuilder.BuiltBuffer built = null;
            buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

            BlockPos playerPos = mc.player.getBlockPos();
            int radius = 12;

            for (BlockPos pos : BlockPos.iterateOutwards(playerPos, radius, radius, radius)) {
                Block block = mc.world.getBlockState(pos).getBlock();
                if (targets.contains(block)) {
                    Box box = new Box(pos).offset(-camPos.x, -camPos.y, -camPos.z);
                    drawBox(buffer, box, block == Blocks.LAVA ? 1f : 0f, block == Blocks.WATER ? 0f : 1f, 0f, 1f);
                }
            }

            Tessellator.getInstance().draw();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        });

        // Add target blocks
        targets.add(Blocks.DIAMOND_ORE);
        targets.add(Blocks.IRON_ORE);
        targets.add(Blocks.GOLD_ORE);
        targets.add(Blocks.COAL_ORE);
        targets.add(Blocks.REDSTONE_ORE);
        targets.add(Blocks.EMERALD_ORE);
        targets.add(Blocks.WATER);
        targets.add(Blocks.LAVA);
    }

    private void drawBox(BufferBuilder buffer, Box box, float r, float g, float b, float a) {
        // 12 lines = outline of a box
        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        addLine(buffer, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(buffer, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(buffer, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(buffer, x1, y1, z2, x1, y1, z1, r, g, b, a);

        addLine(buffer, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(buffer, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(buffer, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(buffer, x1, y2, z2, x1, y2, z1, r, g, b, a);

        addLine(buffer, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(buffer, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(buffer, x2, y1, z2, x2, y2, z2, r, g, b, a);
        addLine(buffer, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private void addLine(BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        buffer.vertex(x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(x2, y2, z2).color(r, g, b, a).next();
    }
}
