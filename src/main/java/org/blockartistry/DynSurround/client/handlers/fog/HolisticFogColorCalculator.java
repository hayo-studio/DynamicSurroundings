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
package org.blockartistry.DynSurround.client.handlers.fog;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.lib.Color;

import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HolisticFogColorCalculator implements IFogColorCalculator {

	protected List<IFogColorCalculator> calculators = new ArrayList<>();
	protected Color cached;

	public HolisticFogColorCalculator() {

	}

	public void add(@Nonnull final IFogColorCalculator calc) {
		this.calculators.add(calc);
	}

	@Override
	public Color calculate(@Nonnull final EntityViewRenderEvent.FogColors event) {
		Color result = null;
		for (final IFogColorCalculator calc : this.calculators) {
			final Color color = calc.calculate(event);
			if (result == null)
				result = color;
			else if (color != null)
				result = result.mix(color);
		}
		return this.cached = result;
	}

	@Override
	public void tick() {
		for (final IFogColorCalculator calc : this.calculators)
			calc.tick();
	}

	public String toString() {
		return this.cached != null ? this.cached.toString() : "<NOT SET>";
	}

}
