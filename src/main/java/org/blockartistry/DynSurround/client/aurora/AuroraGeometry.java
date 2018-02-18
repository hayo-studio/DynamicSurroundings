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

package org.blockartistry.DynSurround.client.aurora;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

/**
 * Preset geometry of an Aurora. A preset is selected by the server when an
 * Aurora spawns.
 */
public final class AuroraGeometry {

	public final int length;
	public final float nodeLength;
	public final float nodeWidth;
	public final int bandOffset;
	public final int alphaLimit;

	private static final List<AuroraGeometry> PRESET = new ArrayList<AuroraGeometry>();

	static {
		// 10/5; 90/45
		PRESET.add(new AuroraGeometry(128, 30.0F, 2.0F, 45, 96));
		PRESET.add(new AuroraGeometry(128, 15.0F, 2.0F, 27, 96));
		PRESET.add(new AuroraGeometry(64, 30.0F, 2.0F, 45, 96));
		PRESET.add(new AuroraGeometry(64, 15.0F, 2.0F, 27, 96));

		PRESET.add(new AuroraGeometry(128, 30.0F, 2.0F, 45, 80));
		PRESET.add(new AuroraGeometry(128, 15.0F, 2.0F, 27, 80));
		PRESET.add(new AuroraGeometry(64, 30.0F, 2.0F, 45, 80));
		PRESET.add(new AuroraGeometry(64, 15.0F, 2.0F, 27, 80));

		PRESET.add(new AuroraGeometry(128, 30.0F, 2.0F, 45, 64));
		PRESET.add(new AuroraGeometry(128, 15.0F, 2.0F, 27, 64));
		PRESET.add(new AuroraGeometry(64, 30.0F, 2.0F, 45, 64));
		PRESET.add(new AuroraGeometry(64, 15.0F, 2.0F, 27, 64));
	}

	private AuroraGeometry(final int length, final float nodeLength, final float nodeWidth, final int bandOffset,
			final int alphaLimit) {
		this.length = length;
		this.nodeLength = nodeLength;
		this.nodeWidth = nodeWidth;
		this.bandOffset = bandOffset;
		this.alphaLimit = alphaLimit;
	}

	@Nonnull
	public static AuroraGeometry get(@Nonnull final Random random) {
		final int idx = random.nextInt(PRESET.size());
		return PRESET.get(idx);
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("bandLength:").append(this.length);
		builder.append(";nodeLength:").append(this.nodeLength);
		builder.append(";nodeWidth:").append(this.nodeWidth);
		builder.append(";bandOffset:").append(this.bandOffset);
		builder.append(";alphaLimit:").append(this.alphaLimit);
		return builder.toString();
	}
}