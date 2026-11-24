package dev.rezipped.zipline.network;

import dev.rezipped.zipline.api.CableReference;
import dev.rezipped.zipline.server.ZiplineServerHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageZiplineControl implements IMessage
{
    public static final byte ATTACH = 0;
    public static final byte RELEASE = 1;
    public static final byte JUMP = 2;

    private byte action;
    private CableReference reference;

    public MessageZiplineControl()
    {
    }

    private MessageZiplineControl(byte action, CableReference reference)
    {
        this.action = action;
        this.reference = reference;
    }

    public static MessageZiplineControl attach(CableReference reference)
    {
        return new MessageZiplineControl(ATTACH, reference);
    }

    public static MessageZiplineControl release()
    {
        return new MessageZiplineControl(RELEASE, null);
    }

    public static MessageZiplineControl jump()
    {
        return new MessageZiplineControl(JUMP, null);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.action = buffer.readByte();
        boolean hasReference = buffer.readBoolean();

        if (hasReference)
        {
            String providerId = ByteBufUtils.readUTF8String(buffer);
            NBTTagCompound data = ByteBufUtils.readTag(buffer);

            if (providerId != null && providerId.length() <= 128 && data != null)
            {
                this.reference = new CableReference(providerId, data);
            }
            else
            {
                this.reference = null;
            }
        }
        else
        {
            this.reference = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeByte(this.action);
        buffer.writeBoolean(this.reference != null);

        if (this.reference != null)
        {
            ByteBufUtils.writeUTF8String(buffer, this.reference.getProviderId());
            ByteBufUtils.writeTag(buffer, this.reference.getData());
        }
    }

    public static final class Handler implements IMessageHandler<MessageZiplineControl, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageZiplineControl message, MessageContext context)
        {
            final EntityPlayerMP player = context.getServerHandler().player;
            WorldServer world = player.getServerWorld();

            world.addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    ZiplineServerHandler.handleControl(player, message.action, message.reference);
                }
            });

            return null;
        }
    }
}
