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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.handlers.effects.CraftingSoundEffect;
import org.blockartistry.DynSurround.client.handlers.effects.EntityBowSoundEffect;
import org.blockartistry.DynSurround.client.handlers.effects.EntityChatEffect;
import org.blockartistry.DynSurround.client.handlers.effects.EntityFootprintEffect;
import org.blockartistry.DynSurround.client.handlers.effects.EntityHealthPopoffEffect;
import org.blockartistry.DynSurround.client.handlers.effects.EntitySwingEffect;
import org.blockartistry.DynSurround.client.handlers.effects.FrostBreathEffect;
import org.blockartistry.DynSurround.client.handlers.effects.PlayerToolBarSoundEffect;
import org.blockartistry.DynSurround.client.handlers.effects.VillagerChatEffect;
import org.blockartistry.lib.effects.ISoundHelper;
import org.blockartistry.lib.gfx.ParticleHelper;
import org.blockartistry.DynSurround.event.DiagnosticEvent;
import org.blockartistry.DynSurround.event.ReloadEvent;
import org.blockartistry.lib.effects.EntityEffectHandler;
import org.blockartistry.lib.effects.EntityEffectLibrary;
import org.blockartistry.lib.effects.EventEffectLibrary;
import org.blockartistry.lib.effects.IParticleHelper;
import org.blockartistry.lib.math.TimerEMA;
import org.blockartistry.lib.sound.BasicSound;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FxHandler extends EffectHandlerBase {

	private static final IParticleHelper PARTICLE_HELPER = (p) -> ParticleHelper.addParticle(p);
	private static final ISoundHelper SOUND_HELPER = new ISoundHelper() {
		@Override
		public String playSound(@Nonnull final BasicSound<?> sound) {
			return SoundEffectHandler.INSTANCE.playSound(sound);
		}

		@Override
		public void stopSound(@Nonnull final String id) {
			SoundEffectHandler.INSTANCE.stopSound(id);
		}
	};

	// Used to process handler entries during the client tick
	private static final Predicate<EntityEffectHandler> HANDLER_UPDATE_REMOVE = handler -> {
		handler.update();
		return !handler.isAlive();
	};

	private static final EntityEffectLibrary library = new EntityEffectLibrary(PARTICLE_HELPER, SOUND_HELPER);

	public static FxHandler INSTANCE;

	static {
		library.register(FrostBreathEffect.DEFAULT_FILTER, new FrostBreathEffect.Factory());
		library.register(EntityChatEffect.DEFAULT_FILTER, new EntityChatEffect.Factory());
		library.register(VillagerChatEffect.DEFAULT_FILTER, new VillagerChatEffect.Factory());
		library.register(PlayerToolBarSoundEffect.DEFAULT_FILTER, new PlayerToolBarSoundEffect.Factory());
		library.register(EntityFootprintEffect.DEFAULT_FILTER, new EntityFootprintEffect.Factory());
		library.register(EntitySwingEffect.DEFAULT_FILTER, new EntitySwingEffect.Factory());
		library.register(EntityBowSoundEffect.DEFAULT_FILTER, new EntityBowSoundEffect.Factory());
		library.register(EntityHealthPopoffEffect.DEFAULT_FILTER, new EntityHealthPopoffEffect.Factory());
	}

	private final Map<UUID, EntityEffectHandler> handlers = new HashMap<>(256);
	private final EventEffectLibrary eventLibrary = new EventEffectLibrary(PARTICLE_HELPER, SOUND_HELPER);

	private TimerEMA compute = new TimerEMA("FxHandler Updates");
	private long nanos;

	public FxHandler() {
		super("Special Effects");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		this.handlers.values().removeIf(HANDLER_UPDATE_REMOVE);
		this.compute.update(this.nanos);
		this.nanos = 0;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		final StringBuilder builder = new StringBuilder();
		builder.append("EffectHandlers: ").append(this.handlers.size());
		event.output.add(builder.toString());
	}

	/**
	 * Used for diagnostics to get data about an Entity.
	 * 
	 * @param entity
	 *            Entity to get information on
	 * @return A list of EntityEffects, if any
	 */
	public List<String> getEffects(@Nonnull final Entity entity) {
		final EntityEffectHandler eh = this.handlers.get(entity.getUniqueID());
		if (eh != null) {
			return eh.getAttachedEffects();
		}
		return ImmutableList.of();
	}

	/**
	 * Whenever an Entity updates make sure we have an appropriate handler, and
	 * update it's state if necessary.
	 */
	@SubscribeEvent(receiveCanceled = true)
	public void onLivingUpdate(@Nonnull final LivingUpdateEvent event) {
		final Entity entity = event.getEntity();
		if (entity == null || !entity.getEntityWorld().isRemote)
			return;

		final long start = System.nanoTime();

		final double distanceThreshold = ModOptions.general.specialEffectRange * ModOptions.general.specialEffectRange;
		final boolean inRange = entity.getDistanceSq(EnvironState.getPlayer()) <= distanceThreshold
				&& entity.dimension == EnvironState.getDimensionId();

		final EntityEffectHandler handler = this.handlers.get(entity.getUniqueID());
		if (handler != null && !inRange) {
			handler.die();
		} else if (handler == null && inRange && entity.isEntityAlive()) {
			this.handlers.put(entity.getUniqueID(), library.create(entity).get());
		}

		this.nanos += (System.nanoTime() - start);
	}

	protected void clearHandlers() {
		this.handlers.values().forEach(EntityEffectHandler::die);
		this.handlers.clear();
	}

	/**
	 * Check if the player joining the world is the one sitting at the keyboard. If
	 * so we need to wipe out the existing handler list because the dimension
	 * changed.
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityJoin(@Nonnull final EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerSP)
			this.clearHandlers();
	}

	/**
	 * Wipe out the effect handlers on a registry reload. Possible something changed
	 * in the effect configuration.
	 */
	@SubscribeEvent
	public void registryReload(@Nonnull final ReloadEvent.Registry event) {
		this.clearHandlers();
	}

	@Override
	public void onConnect() {
		this.clearHandlers();
		this.eventLibrary.register(new CraftingSoundEffect());
		INSTANCE = this;
		DiagnosticHandler.INSTANCE.addTimer(this.compute);
	}

	@Override
	public void onDisconnect() {
		this.clearHandlers();
		this.eventLibrary.cleanup();
		INSTANCE = null;
	}
}
