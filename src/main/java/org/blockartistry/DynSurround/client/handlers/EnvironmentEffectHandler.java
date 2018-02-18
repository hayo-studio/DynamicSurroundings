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

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.handlers.fog.BedrockFogRangeCalculator;
import org.blockartistry.DynSurround.client.handlers.fog.BiomeFogColorCalculator;
import org.blockartistry.DynSurround.client.handlers.fog.BiomeFogRangeCalculator;
import org.blockartistry.DynSurround.client.handlers.fog.FixedFogRangeCalculator;
import org.blockartistry.DynSurround.client.handlers.fog.FogResult;
import org.blockartistry.DynSurround.client.handlers.fog.HazeFogRangeCalculator;
import org.blockartistry.DynSurround.client.handlers.fog.HolisticFogColorCalculator;
import org.blockartistry.DynSurround.client.handlers.fog.HolisticFogRangeCalculator;
import org.blockartistry.DynSurround.client.handlers.fog.MorningFogRangeCalculator;
import org.blockartistry.DynSurround.client.handlers.fog.WeatherFogRangeCalculator;
import org.blockartistry.DynSurround.event.DiagnosticEvent;
import org.blockartistry.DynSurround.event.ReloadEvent;
import org.blockartistry.DynSurround.registry.EffectRegistry;
import org.blockartistry.DynSurround.registry.ThemeInfo;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.math.TimerEMA;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class EnvironmentEffectHandler extends EffectHandlerBase {

	private final TimerEMA timer = new TimerEMA("Fog Render");
	private long nanos;

	private ThemeInfo theme;

	public EnvironmentEffectHandler() {
		super("Environment");
	}

	private boolean doFog() {
		return ModOptions.fog.enableFogProcessing && EnvironState.getDimensionInfo().getHasFog();
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {

		if (doFog()) {
			this.fogRange.tick();
			this.fogColor.tick();
		}

		this.timer.update(this.nanos);
		this.nanos = 0;
	}

	protected HolisticFogColorCalculator fogColor = new HolisticFogColorCalculator();
	protected HolisticFogRangeCalculator fogRange = new HolisticFogRangeCalculator();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void fogColorEvent(final EntityViewRenderEvent.FogColors event) {
		if (doFog()) {
			final long start = System.nanoTime();
			final Material material = event.getState().getMaterial();
			if (material != Material.LAVA && material != Material.WATER) {
				final Color color = this.fogColor.calculate(event);
				if (color != null) {
					event.setRed(color.red);
					event.setGreen(color.green);
					event.setBlue(color.blue);
				}
			}
			this.nanos += System.nanoTime() - start;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void fogRenderEvent(final EntityViewRenderEvent.RenderFogEvent event) {
		if (doFog()) {
			final long start = System.nanoTime();
			final Material material = event.getState().getMaterial();
			if (material != Material.LAVA && material != Material.WATER) {
				final FogResult result = this.fogRange.calculate(event);
				if (result != null) {
					GlStateManager.setFogStart(result.getStart());
					GlStateManager.setFogEnd(result.getEnd());
				}
			}
			this.nanos += System.nanoTime() - start;
		}
	}

	@SubscribeEvent
	public void diagnostics(final DiagnosticEvent.Gather event) {
		event.output.add("Theme: " + this.theme.name());
		if (doFog()) {
			event.output.add("Fog Range: " + this.fogRange.toString());
			event.output.add("Fog Color: " + this.fogColor.toString());
		} else
			event.output.add("FOG: IGNORED");
	}

	@Override
	public void onConnect() {
		DiagnosticHandler.INSTANCE.addTimer(this.timer);
	}

	@SubscribeEvent(receiveCanceled = false, priority = EventPriority.LOWEST)
	public void onWorldLoad(@Nonnull final WorldEvent.Load event) {
		// Only want client side world things
		if (!event.getWorld().isRemote)
			return;

		setupTheme(event.getWorld());
	}

	@SubscribeEvent
	public void onConfigurationChanged(@Nonnull final ReloadEvent.Configuration event) {
		setupTheme(EnvironState.getWorld());
	}

	protected void setupTheme(@Nonnull final World world) {

		this.theme = ClientRegistry.EFFECTS.setTheme(EffectRegistry.DEFAULT_THEME);
		//this.theme = ClientRegistry.EFFECTS.setTheme(new ResourceLocation("dsurround:gloamwood"));

		this.fogColor = new HolisticFogColorCalculator();
		this.fogRange = new HolisticFogRangeCalculator();

		if (this.theme.doBiomeFog()) {
			this.fogColor.add(new BiomeFogColorCalculator());
			this.fogRange.add(new BiomeFogRangeCalculator());
		}

		if (this.theme.doElevationHaze())
			this.fogRange.add(new HazeFogRangeCalculator());

		if (this.theme.doMorningFog())
			this.fogRange.add(new MorningFogRangeCalculator());

		if (this.theme.doBedrockFog())
			this.fogRange.add(new BedrockFogRangeCalculator());

		if (this.theme.doWeatherFog())
			this.fogRange.add(new WeatherFogRangeCalculator());

		if (this.theme.doFixedFog())
			this.fogRange
					.add(new FixedFogRangeCalculator(this.theme.getMinFogDistance(), this.theme.getMaxFogDistance()));
	}

}
