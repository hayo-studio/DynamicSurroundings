/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.DynSurround.client.sound;

import javax.annotation.Nonnull;

import org.blockartistry.lib.compat.PositionedSoundUtil;
import org.blockartistry.lib.sound.BasicSound;

import net.minecraft.client.audio.PositionedSound;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AdhocSound extends BasicSound<AdhocSound> {

	// Used for sound routing
	public AdhocSound(@Nonnull final NBTTagCompound nbt) {
		super((ResourceLocation) null, null);
		this.deserializeNBT(nbt);
	}

	public AdhocSound(@Nonnull final SoundEvent event, @Nonnull final PositionedSound sound) {
		super(event, sound.getCategory());

		this.setPosition(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
		this.setAttenuationType(sound.getAttenuationType());
		this.setVolume(PositionedSoundUtil.getVolume(sound));
		this.setPitch(PositionedSoundUtil.getPitch(sound));
	}

	public AdhocSound(@Nonnull final SoundEvent event, @Nonnull final SoundCategory cat) {
		super(event, cat);
	}

}
