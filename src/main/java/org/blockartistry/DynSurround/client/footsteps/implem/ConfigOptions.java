/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.blockartistry.DynSurround.client.footsteps.implem;

import org.blockartistry.DynSurround.client.footsteps.interfaces.IOptions;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigOptions implements IOptions {

	protected long delayMin = 0;
	protected long delayMax = 0;
	protected float glidingVolume = 0;
	protected float glidingPitch = 0;
	protected float volumeScale = 1F;
	protected float pitchScale = 1F;

	public ConfigOptions() {

	}

	@Override
	public long getDelayMin() {
		return this.delayMin;
	}

	public void setDelayMin(final long v) {
		this.delayMin = v;
	}

	@Override
	public long getDelayMax() {
		return this.delayMax;
	}

	public void setDelayMax(final long v) {
		this.delayMax = v;
	}

	@Override
	public float getGlidingVolume() {
		return this.glidingVolume;
	}

	public void setGlidingVolume(final float v) {
		this.glidingVolume = v;
	}

	@Override
	public float getGlidingPitch() {
		return this.glidingPitch;
	}

	public void setGlidingPitch(final float v) {
		this.glidingPitch = v;
	}
	
	@Override
	public float getVolumeScale() {
		return this.volumeScale;
	}
	
	public void setVolumeScale(final float s) {
		this.volumeScale = s;
	}
	
	@Override
	public float getPitchScale() {
		return this.pitchScale;
	}
	
	public void setPitchScale(final float p) {
		this.pitchScale = p;
	}

}
