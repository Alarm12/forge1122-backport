package dev.rezipped.zipline.client;

public final class ClientZiplineState
{
    private static volatile boolean attached;
    private static volatile boolean pendingAttach;

    private ClientZiplineState()
    {
    }

    public static boolean isAttached()
    {
        return attached;
    }

    public static void setAttached(boolean value)
    {
        attached = value;
        pendingAttach = false;
    }

    public static boolean isPendingAttach()
    {
        return pendingAttach;
    }

    public static void setPendingAttach(boolean value)
    {
        pendingAttach = value;
    }
}
