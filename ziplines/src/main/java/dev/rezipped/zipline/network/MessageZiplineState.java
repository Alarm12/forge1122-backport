package dev.rezipped.zipline.network;

import dev.rezipped.zipline.client.ClientZiplineState;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageZiplineState implements IMessage
{
    private boolean attached;

    public MessageZiplineState()
    {
    }

    public MessageZiplineState(boolean attached)
    {
        this.attached = attached;
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.attached = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeBoolean(this.attached);
    }

    public static final class Handler implements IMessageHandler<MessageZiplineState, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageZiplineState message, MessageContext context)
        {
            FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    ClientZiplineState.setAttached(message.attached);
                }
            });

            return null;
        }
    }
}
