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

package org.blockartistry.DynSurround.client.fx.particle;

import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleBlockDust;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDust extends ParticleBlockDust {

	private final float f, f1, f2, f3, f4;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private int slX16, blX16;

	public ParticleDust(final World world, final double x, final double y, final double z, final IBlockState block) {
		super(world, x, y, z, 0, 0, 0, block);

		this.canCollide = false;
		
		this.rand = XorShiftRandom.current();

		this.multipleParticleScaleBy((float) (0.3F + this.rand.nextGaussian() / 30.0F));
		this.setPosition(this.posX, this.posY, this.posZ);

		f = this.particleTexture.getInterpolatedU((double) (this.particleTextureJitterX / 4.0F * 16.0F));
		f1 = this.particleTexture.getInterpolatedU((double) ((this.particleTextureJitterX + 1.0F) / 4.0F * 16.0F));
		f2 = this.particleTexture.getInterpolatedV((double) (this.particleTextureJitterY / 4.0F * 16.0F));
		f3 = this.particleTexture.getInterpolatedV((double) ((this.particleTextureJitterY + 1.0F) / 4.0F * 16.0F));
		f4 = 0.1F * this.particleScale;
	}

	@Override
	public void move(final double dX, final double dY, final double dZ) {
		this.posX += dX;
		this.posY += dY;
		this.posZ += dZ;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.04D * (double) this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		this.pos.setPos(this.posX, this.posY, this.posZ);

		if (this.particleMaxAge-- <= 0) {
			this.setExpired();
		} else if (WorldUtils.isSolidBlock(this.world, this.pos)) {
			this.setExpired();
		}

		final int combinedLight = this.getBrightnessForRender(0);
		this.slX16 = combinedLight >> 16 & 65535;
		this.blX16 = combinedLight & 65535;
	}

	@Override
	public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
		float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
		float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
		worldRendererIn
				.pos((double) (f5 - rotationX * f4 - rotationXY * f4), (double) (f6 - rotationZ * f4),
						(double) (f7 - rotationYZ * f4 - rotationXZ * f4))
				.tex((double) f, (double) f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F)
				.lightmap(slX16, blX16).endVertex();
		worldRendererIn
				.pos((double) (f5 - rotationX * f4 + rotationXY * f4), (double) (f6 + rotationZ * f4),
						(double) (f7 - rotationYZ * f4 + rotationXZ * f4))
				.tex((double) f, (double) f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F)
				.lightmap(slX16, blX16).endVertex();
		worldRendererIn
				.pos((double) (f5 + rotationX * f4 + rotationXY * f4), (double) (f6 + rotationZ * f4),
						(double) (f7 + rotationYZ * f4 + rotationXZ * f4))
				.tex((double) f1, (double) f2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F)
				.lightmap(slX16, blX16).endVertex();
		worldRendererIn
				.pos((double) (f5 + rotationX * f4 - rotationXY * f4), (double) (f6 - rotationZ * f4),
						(double) (f7 + rotationYZ * f4 - rotationXZ * f4))
				.tex((double) f1, (double) f3).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F)
				.lightmap(slX16, blX16).endVertex();
	}

	@Override
	public int getBrightnessForRender(final float partialTicks) {
		return this.world.getCombinedLight(this.pos, 0);
	}

}