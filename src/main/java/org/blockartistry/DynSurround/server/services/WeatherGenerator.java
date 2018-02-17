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

package org.blockartistry.DynSurround.server.services;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.data.DimensionEffectData;
import org.blockartistry.DynSurround.network.Network;
import org.blockartistry.DynSurround.network.PacketThunder;
import org.blockartistry.DynSurround.network.PacketWeatherUpdate;
import org.blockartistry.DynSurround.registry.DimensionInfo;
import org.blockartistry.lib.PlayerUtils;
import org.blockartistry.lib.random.XorShiftRandom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public class WeatherGenerator {

	protected final Random RANDOM = XorShiftRandom.current();
	protected final DimensionInfo dimension;
	protected final World world;
	protected final WorldInfo info;
	protected final DimensionEffectData data;

	public WeatherGenerator(@Nonnull final World world) {
		this.world = world;
		this.info = world.getWorldInfo();
		this.data = DimensionEffectData.get(world);
		this.dimension = ServerRegistry.DIMENSION.getData(world);
	}

	protected int nextRainInterval(final boolean isRaining) {
		final int base = isRaining ? ModOptions.rain.rainActiveTimeConst : ModOptions.rain.rainInactiveTimeConst;
		return base + this.RANDOM
				.nextInt(isRaining ? ModOptions.rain.rainActiveTimeVariable : ModOptions.rain.rainInactiveTimeVariable);
	}

	protected int nextThunderInterval(final boolean isThundering) {
		final int base = isThundering ? ModOptions.rain.stormActiveTimeConst : ModOptions.rain.stormInactiveTimeConst;
		return base + this.RANDOM
				.nextInt(isThundering ? ModOptions.rain.stormActiveTimeVariable : ModOptions.rain.stormInactiveTimeVariable);
	}

	protected int nextThunderEvent(final float rainIntensity) {
		final float scale = 2.0F - rainIntensity;
		return this.RANDOM.nextInt((int) (450 * scale)) + 300;
	}

	protected boolean doFlash(final float rainIntensity) {
		final int randee = (int) (rainIntensity * 100.0F);
		return this.RANDOM.nextInt(150) <= randee;
	}

	protected void preProcess() {

	}

	protected void doRain() {
		final int rain = this.info.getRainTime();

		if (rain == 2) {
			this.info.setRaining(!this.info.isRaining());
			this.info.setRainTime(nextRainInterval(this.info.isRaining()));
		}

		// Track our rain intensity values
		if (this.info.isRaining()) {
			// If our intensity is 0 it means that we need to establish
			// a strength.
			if (this.data.getRainIntensity() == 0.0F) {
				this.data.randomizeRain();
				this.info.setRainTime(nextRainInterval(true));
				DSurround.log().debug("dim %d rain intensity set to %f, duration %d ticks", this.data.getDimensionId(),
						this.data.getRainIntensity(), this.info.getRainTime());
			}
			this.data.setCurrentRainIntensity(this.world.getRainStrength(1.0F));
		} else {

			if (this.world.getRainStrength(1.0F) > 0.0F) {
				// It's not raining and the world has strength. Means
				// that it is on the way out so reflect the worlds strength.
				this.data.setCurrentRainIntensity(this.world.getRainStrength(1.0F));
			} else if (this.data.getCurrentRainIntensity() > 0) {
				// We get here the world is not raining and there is no
				// strength, but our record indicates something. Means we
				// stopped.
				this.data.setRainIntensity(0);
				this.data.setCurrentRainIntensity(0);
				this.info.setRainTime(nextRainInterval(false));
				DSurround.log().debug("dim %d rain has stopped, next rain %d ticks", this.data.getDimensionId(),
						this.info.getRainTime());
			} else if (this.data.getRainIntensity() > 0) {
				this.data.setRainIntensity(0);
			}
		}

	}

	protected void doThunder() {
		final int thunder = this.info.getThunderTime();

		if (thunder == 2) {
			this.info.setThundering(!this.info.isThundering());
			this.info.setThunderTime(nextThunderInterval(this.info.isThundering()));
		}

	}

	protected void doAmbientThunder() {

		// If not enabled, return
		if (!ModOptions.rain.allowBackgroundThunder)
			return;

		// Gather the intensity for rain
		final float intensity = this.data.getCurrentRainIntensity();

		// If it is thundering and the intensity exceeds our threshold...
		if (this.info.isThundering() && intensity >= ModOptions.rain.stormThunderThreshold) {
			int time = this.data.getThunderTimer() - 1;
			if (time <= 0) {
				// If it is 0 we just counted down to this. If it were
				// the first time through it would be -1.
				if (time == 0) {
					// Get a random player in the dimension - they will be the
					// locus of the event. Center it at build height above
					// their head.
					final EntityPlayer player = PlayerUtils.getRandomPlayer(this.world);
					final float theY = this.dimension.getSkyHeight();
					if (player != null) {
						final PacketThunder packet = new PacketThunder(this.data.getDimensionId(), doFlash(intensity),
								new BlockPos(player.posX, theY, player.posZ));
						Network.sendToDimension(this.data.getDimensionId(), packet);
					}
				}
				// set new time
				time = nextThunderEvent(intensity);
			}
			this.data.setThunderTimer(time);

		} else {
			// Clear out the timer data for the next storm
			this.data.setThunderTimer(0);
		}
	}

	protected void postProcess() {

	}

	public final void update() {
		this.process();
		this.sendUpdate();
	}

	protected void process() {
		this.preProcess();
		this.doRain();
		this.doThunder();
		this.doAmbientThunder();
		this.postProcess();
	}

	protected void sendUpdate() {
		// Send the weather update to all players in the dimension.
		if (this.world.playerEntities.size() > 0) {
			final PacketWeatherUpdate packet = new PacketWeatherUpdate(this.data.getDimensionId(),
					this.data.getCurrentRainIntensity(), this.data.getRainIntensity(), this.info.getRainTime(),
					this.world.getThunderStrength(1.0F), this.info.getThunderTime(), this.data.getThunderTimer());
			Network.sendToDimension(this.data.getDimensionId(), packet);
		}
	}
}
