package com.lilypuree.connectiblechains.network;

import com.lilypuree.connectiblechains.chain.ChainConnection;
import com.lilypuree.connectiblechains.chain.ClientChainStore;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageSyncChains implements IMessage
{
    private int dimension;
    private List<ChainConnection> connections;

    public MessageSyncChains()
    {
        this.connections = new ArrayList<ChainConnection>();
    }

    public MessageSyncChains(int dimension, List<ChainConnection> connections)
    {
        this.dimension = dimension;
        this.connections = new ArrayList<ChainConnection>(connections);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        this.dimension = buffer.readInt();
        int count = buffer.readInt();
        this.connections = new ArrayList<ChainConnection>(count);

        for (int index = 0; index < count; index++)
        {
            BlockPos first = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
            BlockPos second = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
            double slack = buffer.readDouble();
            this.connections.add(new ChainConnection(first, second, slack));
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeInt(this.dimension);
        buffer.writeInt(this.connections.size());

        for (ChainConnection connection : this.connections)
        {
            buffer.writeInt(connection.getFirst().getX());
            buffer.writeInt(connection.getFirst().getY());
            buffer.writeInt(connection.getFirst().getZ());
            buffer.writeInt(connection.getSecond().getX());
            buffer.writeInt(connection.getSecond().getY());
            buffer.writeInt(connection.getSecond().getZ());
            buffer.writeDouble(connection.getSlack());
        }
    }

    public static final class Handler implements IMessageHandler<MessageSyncChains, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageSyncChains message, MessageContext context)
        {
            FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    ClientChainStore.replace(message.dimension, message.connections);
                }
            });

            return null;
        }
    }
}
