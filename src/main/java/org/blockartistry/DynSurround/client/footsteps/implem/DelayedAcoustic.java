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
public class DelayedAcoustic extends BasicAcoustic implements IOptions {
	
	protected long delayMin = 0;
	protected long delayMax = 0;

	public DelayedAcoustic() {
		super();

		this.outputOptions = this;
	}

	@Override
	public long getDelayMin() {
		return this.delayMin;
	}

	@Override
	public long getDelayMax() {
		return this.delayMax;
	}

	public void setDelayMin(final long delay) {
		this.delayMin = delay;
	}

	public void setDelayMax(final long delay) {
		this.delayMax = delay;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(super.toString()).append(" (DELAYED min:").append(this.delayMin).append("/max:")
				.append(this.delayMax).append(')');
		return builder.toString();
	}
}
