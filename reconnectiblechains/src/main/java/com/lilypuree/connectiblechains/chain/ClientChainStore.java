package com.lilypuree.connectiblechains.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientChainStore
{
    private static final Map<Integer, List<ChainConnection>> CONNECTIONS = new HashMap<Integer, List<ChainConnection>>();

    private ClientChainStore()
    {
    }

    public static synchronized void replace(int dimension, List<ChainConnection> connections)
    {
        List<ChainConnection> copy = new ArrayList<ChainConnection>();

        for (ChainConnection connection : connections)
        {
            copy.add(connection.copy());
        }

        CONNECTIONS.put(Integer.valueOf(dimension), copy);
    }

    public static synchronized List<ChainConnection> get(int dimension)
    {
        List<ChainConnection> connections = CONNECTIONS.get(Integer.valueOf(dimension));

        if (connections == null)
        {
            return Collections.emptyList();
        }

        List<ChainConnection> copy = new ArrayList<ChainConnection>();

        for (ChainConnection connection : connections)
        {
            copy.add(connection.copy());
        }

        return copy;
    }

    public static synchronized void clear()
    {
        CONNECTIONS.clear();
    }
}
