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

package org.blockartistry.DynSurround.client.weather;

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.gfx.ParticleHelper;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NetherSplashRenderer extends StormSplashRenderer {

	@Override
	protected SoundEvent getBlockSoundFX(final Block block, final boolean hasDust, final World world) {
		return hasDust ? Weather.getWeatherProperties().getDustSound() : null;
	}

	@Override
	protected void spawnBlockParticle(final IBlockState state, final boolean dust, final World world, final double x,
			final double y, final double z) {

		if (dust)
			ParticleHelper.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z);
	}

	@Override
	protected BlockPos getPrecipitationHeight(final World world, final int range, final BlockPos pos) {
		final int y = EnvironState.getPlayerPosition().getY();
		boolean airBlockFound = false;
		for (int i = range; i >= -range; i--) {
			final IBlockState state = WorldUtils.getBlockState(world, pos.getX(), y + i, pos.getZ());
			final Material material = state.getMaterial();
			if (airBlockFound && material != Material.AIR && material.isSolid())
				return new BlockPos(pos.getX(), y + i + 1, pos.getZ());
			if (material == Material.AIR)
				airBlockFound = true;
		}

		return new BlockPos(pos.getX(), 128, pos.getZ());
	}
}
