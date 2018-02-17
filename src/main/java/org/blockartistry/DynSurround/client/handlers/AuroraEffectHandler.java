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

package org.blockartistry.DynSurround.client.handlers;

import javax.annotation.Nonnull;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.aurora.AuroraEngineClassic;
import org.blockartistry.DynSurround.client.aurora.AuroraEngineShader;
import org.blockartistry.DynSurround.client.aurora.AuroraUtils;
import org.blockartistry.DynSurround.client.aurora.IAurora;
import org.blockartistry.DynSurround.client.aurora.IAuroraEngine;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.shader.Shaders;
import org.blockartistry.DynSurround.event.DiagnosticEvent;
import org.blockartistry.lib.DiurnalUtils;
import org.blockartistry.lib.math.TimerEMA;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public final class AuroraEffectHandler extends EffectHandlerBase {

	private final IAuroraEngine auroraEngine;

	private IAurora current;
	private int dimensionId;

	private final TimerEMA timer = new TimerEMA("Aurora Render");
	private long nanos;

	public AuroraEffectHandler() {
		super("Aurora Effect");

		if (ModOptions.aurora.auroraUseShader && Shaders.areShadersSupported())
			this.auroraEngine = new AuroraEngineShader();
		else
			this.auroraEngine = new AuroraEngineClassic();
	}

	@Override
	public void onConnect() {
		this.current = null;
		DiagnosticHandler.INSTANCE.addTimer(this.timer);
	}

	@Override
	public void onDisconnect() {
		this.current = null;
	}

	private boolean spawnAurora(@Nonnull final World world) {
		if (!ModOptions.aurora.auroraEnable)
			return false;

		if (this.current != null || Minecraft.getMinecraft().gameSettings.renderDistanceChunks < 6
				|| DiurnalUtils.isAuroraInvisible(world))
			return false;
		return AuroraUtils.hasAuroras() && EnvironState.getPlayerBiome().getHasAurora();
	}

	private boolean canAuroraStay(@Nonnull final World world) {
		if (!ModOptions.aurora.auroraEnable)
			return false;

		return Minecraft.getMinecraft().gameSettings.renderDistanceChunks < 6
				|| DiurnalUtils.isAuroraVisible(world) && EnvironState.getPlayerBiome().getHasAurora();
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		// Process the current aurora
		if (this.current != null) {
			// If completed or the player changed dimensions we want to kill
			// outright
			if (this.current.isComplete() || this.dimensionId != EnvironState.getDimensionId()
					|| !ModOptions.aurora.auroraEnable) {
				this.current = null;
			} else {
				this.current.update();
				final boolean isDying = this.current.isDying();
				final boolean canStay = canAuroraStay(player.getEntityWorld());
				if (isDying && canStay) {
					DSurround.log().debug("Unfading aurora...");
					this.current.setFading(false);
				} else if (!isDying && !canStay) {
					DSurround.log().debug("Aurora fade...");
					this.current.setFading(true);
				}
			}
		}

		// If there isn't a current aurora see if it needs to spawn
		if (spawnAurora(player.getEntityWorld())) {
			this.current = this.auroraEngine.produce(AuroraUtils.getSeed());
			DSurround.log().debug("New aurora [%s]", this.current.toString());
		}

		// Set the dimension in case it changed
		this.dimensionId = EnvironState.getDimensionId();

		this.timer.update(this.nanos);
		this.nanos = 0;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void doRender(@Nonnull final RenderWorldLastEvent event) {

		final long start = System.nanoTime();

		if (this.current != null)
			this.current.render(event.getPartialTicks());

		this.nanos += System.nanoTime() - start;
	}

	@SubscribeEvent
	public void diagnostic(@Nonnull final DiagnosticEvent.Gather event) {
		event.output.add("Aurora: " + (this.current == null ? "NONE" : this.current.toString()));
	}

}
