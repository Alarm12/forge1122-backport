package com.lilypuree.connectiblechains.client;

import com.lilypuree.connectiblechains.chain.ChainConnection;
import com.lilypuree.connectiblechains.chain.ChainMath;
import com.lilypuree.connectiblechains.chain.ClientChainStore;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public final class ClientChainRenderer
{
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.world == null)
        {
            return;
        }

        List<ChainConnection> connections = ClientChainStore.get(minecraft.world.provider.getDimension());

        if (connections.isEmpty())
        {
            return;
        }

        Entity camera = minecraft.getRenderViewEntity();

        if (camera == null)
        {
            return;
        }

        float partialTicks = event.getPartialTicks();
        double cameraX = camera.lastTickPosX + ((camera.posX - camera.lastTickPosX) * partialTicks);
        double cameraY = camera.lastTickPosY + ((camera.posY - camera.lastTickPosY) * partialTicks);
        double cameraZ = camera.lastTickPosZ + ((camera.posZ - camera.lastTickPosZ) * partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-cameraX, -cameraY, -cameraZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GL11.glLineWidth(2.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (ChainConnection connection : connections)
        {
            int segments = Math.max(12, Math.min(96, (int) Math.ceil(connection.getStraightLength() * 2.0D)));
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

            for (int index = 0; index <= segments; index++)
            {
                double t = (double) index / (double) segments;
                Vec3d point = ChainMath.getPoint(connection, t);
                buffer.pos(point.x, point.y, point.z).color(72, 75, 82, 255).endVertex();
            }

            tessellator.draw();
        }

        GL11.glLineWidth(1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
