#!/usr/bin/env python3
"""Compile and run the real backport's dependency-light Java core against offline test doubles.

This is NOT a ForgeGradle build. The test doubles do not replace Forge's real API checks.
"""
from pathlib import Path
import subprocess
import tempfile
import textwrap

ROOT = Path(__file__).resolve().parents[1]
STUBS = {
'net/minecraft/util/math/BlockPos.java': '''
package net.minecraft.util.math;
public final class BlockPos {
    private final int x;
    private final int y;
    private final int z;
    public BlockPos(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public int getZ() { return this.z; }
    public BlockPos toImmutable() { return this; }
    public long toLong() {
        return ((long) this.x & 0x3FFFFFFL) << 38
             | ((long) this.y & 0xFFFL) << 26
             | ((long) this.z & 0x3FFFFFFL);
    }
    public static BlockPos fromLong(long value) {
        return new BlockPos((int) (value >> 38), (int) (value << 26 >> 52), (int) (value << 38 >> 38));
    }
    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockPos)) { return false; }
        BlockPos p = (BlockPos) other;
        return this.x == p.x && this.y == p.y && this.z == p.z;
    }
    @Override public int hashCode() { return (this.x * 31 + this.y) * 31 + this.z; }
}
''',
'net/minecraft/util/math/Vec3d.java': '''
package net.minecraft.util.math;
public final class Vec3d {
    public final double x;
    public final double y;
    public final double z;
    public Vec3d(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    public Vec3d add(Vec3d p) { return new Vec3d(this.x + p.x, this.y + p.y, this.z + p.z); }
    public Vec3d subtract(Vec3d p) { return new Vec3d(this.x - p.x, this.y - p.y, this.z - p.z); }
    public Vec3d scale(double d) { return new Vec3d(this.x * d, this.y * d, this.z * d); }
    public double dotProduct(Vec3d p) { return this.x * p.x + this.y * p.y + this.z * p.z; }
    public double lengthSquared() { return this.dotProduct(this); }
    public Vec3d normalize() {
        double length = Math.sqrt(this.lengthSquared());
        if (length < 1.0E-8D) { return new Vec3d(0, 0, 0); }
        return this.scale(1.0D / length);
    }
    public double squareDistanceTo(Vec3d p) { return this.subtract(p).lengthSquared(); }
    public double distanceTo(Vec3d p) { return Math.sqrt(this.squareDistanceTo(p)); }
}
''',
'net/minecraft/util/math/MathHelper.java': '''
package net.minecraft.util.math;
public final class MathHelper {
    public static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
''',
'net/minecraft/nbt/NBTTagCompound.java': '''
package net.minecraft.nbt;
import java.util.HashMap;
import java.util.Map;
public final class NBTTagCompound {
    private final Map<String, Object> values = new HashMap<String, Object>();
    public void setLong(String key, long value) { this.values.put(key, Long.valueOf(value)); }
    public long getLong(String key) { return ((Long) this.values.get(key)).longValue(); }
    public void setDouble(String key, double value) { this.values.put(key, Double.valueOf(value)); }
    public double getDouble(String key) { return ((Double) this.values.get(key)).doubleValue(); }
    public void setTag(String key, NBTTagList list) { this.values.put(key, list); }
    public NBTTagList getTagList(String key, int type) { return (NBTTagList) this.values.get(key); }
    public boolean hasKey(String key) { return this.values.containsKey(key); }
    public NBTTagCompound copy() {
        NBTTagCompound result = new NBTTagCompound();
        result.values.putAll(this.values);
        return result;
    }
    @Override public boolean equals(Object other) {
        return other instanceof NBTTagCompound
            && this.values.equals(((NBTTagCompound) other).values);
    }
}
''',
'net/minecraft/nbt/NBTTagList.java': '''
package net.minecraft.nbt;
import java.util.ArrayList;
import java.util.List;
public final class NBTTagList {
    private final List<NBTTagCompound> values = new ArrayList<NBTTagCompound>();
    public void appendTag(NBTTagCompound tag) { this.values.add(tag); }
    public int tagCount() { return this.values.size(); }
    public NBTTagCompound getCompoundTagAt(int index) { return this.values.get(index); }
}
''',
'net/minecraft/world/storage/WorldSavedData.java': '''
package net.minecraft.world.storage;
import net.minecraft.nbt.NBTTagCompound;
public abstract class WorldSavedData {
    private boolean dirty;
    public WorldSavedData(String name) { }
    public abstract void readFromNBT(NBTTagCompound tag);
    public abstract NBTTagCompound writeToNBT(NBTTagCompound tag);
    public void markDirty() { this.dirty = true; }
    public boolean isDirty() { return this.dirty; }
}
''',
'net/minecraft/world/storage/MapStorage.java': '''
package net.minecraft.world.storage;
import java.util.HashMap;
import java.util.Map;
public final class MapStorage {
    private final Map<String, WorldSavedData> values = new HashMap<String, WorldSavedData>();
    public WorldSavedData getOrLoadData(Class<? extends WorldSavedData> type, String key) {
        return this.values.get(key);
    }
    public void setData(String key, WorldSavedData data) { this.values.put(key, data); }
}
''',
'net/minecraft/world/WorldProvider.java': '''
package net.minecraft.world;
public final class WorldProvider {
    public int getDimension() { return 0; }
}
''',
'net/minecraft/world/World.java': '''
package net.minecraft.world;
import net.minecraft.world.storage.MapStorage;
public final class World {
    public final boolean isRemote;
    public final WorldProvider provider = new WorldProvider();
    private final MapStorage storage = new MapStorage();
    public World(boolean isRemote) { this.isRemote = isRemote; }
    public MapStorage getPerWorldStorage() { return this.storage; }
}
''',
'OfflineCoreTest.java': '''
import com.lilypuree.connectiblechains.chain.ChainConnection;
import com.lilypuree.connectiblechains.chain.ChainMath;
import com.lilypuree.connectiblechains.chain.ChainNetworkData;
import com.lilypuree.connectiblechains.api.ChainApi;
import com.lilypuree.connectiblechains.chain.ClientChainStore;
import dev.rezipped.zipline.api.Cable;
import dev.rezipped.zipline.api.CableHit;
import dev.rezipped.zipline.api.Cables;
import dev.rezipped.zipline.api.CableReference;
import dev.rezipped.zipline.integration.ReconnectibleChainProvider;
import dev.rezipped.zipline.server.ZiplineState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;

public final class OfflineCoreTest {
    public static void main(String[] args) {
        BlockPos start = new BlockPos(-10, 80, -20);
        BlockPos end = new BlockPos(10, 80, -20);
        ChainConnection connection = new ChainConnection(end, start, 0.2D);
        assertTrue(connection.getFirst().equals(start), "endpoint order");
        assertTrue(connection.getSecond().equals(end), "endpoint order 2");
        assertTrue(connection.matches(start, end), "symmetric match");
        assertNear(ChainMath.getPoint(connection, 0.0D).y, 80.625D, "start height");
        assertNear(ChainMath.getPoint(connection, 1.0D).y, 80.625D, "end height");
        assertNear(ChainMath.getPoint(connection, 0.5D).y, 76.625D, "midpoint sag");
        assertTrue(ChainMath.approximateLength(connection, 100) > connection.getStraightLength(), "curved length");
        assertNear(ChainMath.getTangent(connection, 0.5D).lengthSquared(), 1.0D, "normalized tangent");
        assertTrue(ChainConnection.readFromNbt(connection.writeToNbt()).matches(start, end), "NBT roundtrip");
        World server = new World(false);
        ChainNetworkData data = ChainNetworkData.get(server);
        assertTrue(data.add(connection), "first connection is added");
        assertTrue(!data.add(connection), "duplicate rejected");
        assertTrue(data.isDirty(), "data marked dirty");
        assertTrue(ChainApi.getConnections(server).size() == 1, "server API");
        NBTTagCompound saved = data.writeToNBT(new NBTTagCompound());
        ChainNetworkData restored = new ChainNetworkData();
        restored.readFromNBT(saved);
        assertTrue(restored.getConnections().size() == 1, "persistence");
        World client = new World(true);
        List<ChainConnection> list = new ArrayList<ChainConnection>();
        list.add(connection);
        ClientChainStore.replace(0, list);
        assertTrue(ChainApi.getConnections(client).size() == 1, "client API");
        Cables.registerProvider(new ReconnectibleChainProvider());
        Vec3d eye = new Vec3d(-9.0D, 80.625D, -20.0D);
        CableHit hit = Cables.findLookedAt(server, eye, new Vec3d(1.0D, 0.0D, 0.0D), 6.0D, 2.0D);
        assertTrue(hit != null, "look ray should find cable");
        Cable cable = Cables.resolve(server, hit.getCable().getReference());
        assertTrue(cable != null, "cable re-resolves by reference");
        CableReference reference = cable.getReference();
        ZiplineState state = new ZiplineState(reference, 0.25D, 0.3D);
        assertNear(state.getT(), 0.25D, "state parameter");
        assertTrue(state.incrementDurabilityTicks() == 1, "durability increments");
        state.resetDurabilityTicks();
        assertTrue(state.incrementDurabilityTicks() == 1, "durability resets");
        assertTrue(data.remove(connection), "connection removal");
        assertTrue(ChainApi.getConnections(server).size() == 0, "removed from API");
        System.out.println("PASS: Java 8 bytecode, chain math, NBT, storage, provider lookup, zipline state");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }

    private static void assertNear(double actual, double expected, String message) {
        if (Math.abs(actual - expected) > 0.001D) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
''',
}

JAVA_SOURCES = [
    'reconnectiblechains/src/main/java/com/lilypuree/connectiblechains/chain/ChainConnection.java',
    'reconnectiblechains/src/main/java/com/lilypuree/connectiblechains/chain/ChainMath.java',
    'reconnectiblechains/src/main/java/com/lilypuree/connectiblechains/chain/ClientChainStore.java',
    'reconnectiblechains/src/main/java/com/lilypuree/connectiblechains/chain/ChainNetworkData.java',
    'reconnectiblechains/src/main/java/com/lilypuree/connectiblechains/api/ChainApi.java',
    'ziplines/src/main/java/dev/rezipped/zipline/api/Cable.java',
    'ziplines/src/main/java/dev/rezipped/zipline/api/CableReference.java',
    'ziplines/src/main/java/dev/rezipped/zipline/api/CableHit.java',
    'ziplines/src/main/java/dev/rezipped/zipline/api/CableProvider.java',
    'ziplines/src/main/java/dev/rezipped/zipline/api/Cables.java',
    'ziplines/src/main/java/dev/rezipped/zipline/integration/ReconnectibleChainCable.java',
    'ziplines/src/main/java/dev/rezipped/zipline/integration/ReconnectibleChainProvider.java',
    'ziplines/src/main/java/dev/rezipped/zipline/server/ZiplineState.java',
]

with tempfile.TemporaryDirectory(prefix='forge1122-core-') as temp:
    location = Path(temp)
    source_files = []

    for filename, source in STUBS.items():
        path = location / filename
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(textwrap.dedent(source).strip() + '\n', encoding='utf8')
        source_files.append(str(path))

    for filename in JAVA_SOURCES:
        source_files.append(str(ROOT / filename))

    output = location / 'classes'
    output.mkdir()
    result = subprocess.run(
        ['javac', '--release', '8', '-d', str(output)] + source_files,
        capture_output=True, text=True
    )
    if result.returncode != 0:
        raise SystemExit('Core source compile FAILED:\n' + result.stdout + result.stderr)

    result = subprocess.run(['java', '-cp', str(output), 'OfflineCoreTest'], capture_output=True, text=True)
    if result.returncode != 0:
        raise SystemExit('Core test FAILED:\n' + result.stdout + result.stderr)

    print(result.stdout.strip())
    print('Compiled %d real Java files against offline test doubles.' % len(JAVA_SOURCES))
    print('Note: full ForgeGradle compilation was not possible without Forge libraries.')
