# Forge 1.12.2 API audit

Checked against Forge 1.12.2 / 14.23.5.2854 Javadocs, which is API-compatible with the 14.23.5.2860 target for the inspected signatures.

Validated usages include:

- `RegistryEvent.Register<Item>`
- `PlayerInteractEvent#getWorld`, `getPos`, `getHand`, `getItemStack`
- cancellable `LeftClickBlock`
- `SimpleNetworkWrapper`
- `MessageContext#getServerHandler`
- `NetHandlerPlayServer#player`
- `NetHandlerPlayServer#setPlayerLocation`
- `ByteBufUtils#readTag`, `writeTag`, `readUTF8String`, `writeUTF8String`
- `World#getPerWorldStorage`
- `MapStorage#getOrLoadData`, `setData`
- `WorldSavedData#readFromNBT`, `writeToNBT`, `markDirty`
- `BlockPos#toLong`, `fromLong`, inherited `distanceSq`
- `Vec3d#add`, `subtract`, `scale`, `dotProduct`, `normalize`, `lengthSquared`
- `MathHelper#clamp(double, double, double)`
- `ItemStack#isItemStackDamageable`, `damageItem`

The remaining unverified layer is the actual ForgeGradle compilation/reobfuscation run, which needs Java 8 and the Forge/Minecraft dependency set.
