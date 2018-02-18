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

package org.blockartistry.DynSurround.registry;

import javax.annotation.Nonnull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Special fake biome to prevent strange stack issues if
 * a bogus biome comes into the registry.
 */
@SideOnly(Side.CLIENT)
public class WTFFakeBiome extends FakeBiome {

	public WTFFakeBiome() {
		super("WTFJustHappened");
	}

	@Override
	public boolean canRain() {
		return false;
	}

	@Override
	public boolean getEnableSnow() {
		return false;
	}

	@Override
	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return 0F;
	}

	@Override
	public float getTemperature() {
		return 0F;
	}

	@Override
	public TempCategory getTempCategory() {
		return TempCategory.COLD;
	}

	@Override
	public boolean isHighHumidity() {
		return false;
	}

	@Override
	public float getRainfall() {
		return 0F;
	}
}
