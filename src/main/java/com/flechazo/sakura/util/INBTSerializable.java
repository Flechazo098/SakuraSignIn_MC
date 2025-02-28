package com.flechazo.sakura.util;

import net.minecraft.nbt.NbtElement;

public interface INBTSerializable <T extends NbtElement >{
    T serializeNBT();

    void deserializeNBT(T var1);
}
