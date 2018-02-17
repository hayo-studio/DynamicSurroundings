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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.facade.FacadeHelper;
import org.blockartistry.DynSurround.registry.BlockInfo;
import org.blockartistry.lib.BlockNameUtil;
import org.blockartistry.lib.BlockNameUtil.NameResult;
import org.blockartistry.lib.MCHelper;
import gnu.trove.map.hash.THashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockMap {

	private final AcousticsManager acousticsManager;
	private final BlockAcousticMap metaMap = new BlockAcousticMap();
	private Map<Substrate, BlockAcousticMap> substrateMap = new EnumMap<Substrate, BlockAcousticMap>(Substrate.class);

	private static class MacroEntry {
		public final int meta;
		public final String substrate;
		public final String value;

		public MacroEntry(@Nonnull final String substrate, final @Nonnull String value) {
			this(-1, substrate, value);
		}

		public MacroEntry(final int meta, @Nonnull final String substrate, @Nonnull final String value) {
			this.meta = meta;
			this.substrate = substrate;
			this.value = value;
		}
	}

	private static final THashMap<String, List<MacroEntry>> macros = new THashMap<String, List<MacroEntry>>();

	static {
		List<MacroEntry> entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry("foliage", "straw"));
		macros.put("#sapling", entries);
		macros.put("#reed", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "leaves"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry("foliage", "brush"));
		macros.put("#plant", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "leaves"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry("foliage", "brush_straw_transition"));
		macros.put("#bush", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry(0, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(1, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(2, "foliage", "brush"));
		entries.add(new MacroEntry(3, "foliage", "brush"));
		entries.add(new MacroEntry(4, "foliage", "brush_straw_transition"));
		entries.add(new MacroEntry(5, "foliage", "brush_straw_transition"));
		entries.add(new MacroEntry(6, "foliage", "straw"));
		entries.add(new MacroEntry(7, "foliage", "straw"));
		macros.put("#wheat", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry(0, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(1, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(2, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(3, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(4, "foliage", "brush"));
		entries.add(new MacroEntry(5, "foliage", "brush"));
		entries.add(new MacroEntry(6, "foliage", "brush"));
		entries.add(new MacroEntry(7, "foliage", "brush"));
		macros.put("#crop", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry(0, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(1, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(2, "foliage", "brush"));
		entries.add(new MacroEntry(3, "foliage", "brush"));
		macros.put("#beets", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("bigger", "bluntwood"));
		macros.put("#fence", entries);
	}

	public BlockMap(@Nonnull final AcousticsManager manager) {
		this.acousticsManager = manager;
		this.metaMap.put(new BlockInfo(Blocks.AIR), AcousticsManager.NOT_EMITTER);
	}

	@Nullable
	public boolean hasAcoustics(@Nonnull final IBlockState state) {
		final IAcoustic[] a = this.metaMap.getBlockAcoustics(state);
		return a != null && a != BlockAcousticMap.NO_ACOUSTICS;
	}

	@Nullable
	public IAcoustic[] getBlockAcoustics(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
		final IBlockState trueState = FacadeHelper.resolveState(state, world, pos, EnumFacing.UP);
		return this.metaMap.getBlockAcoustics(trueState);
	}

	@Nullable
	public IAcoustic[] getBlockSubstrateAcoustics(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos,
			@Nonnull final Substrate substrate) {
		final IBlockState trueState = FacadeHelper.resolveState(state, world, pos, EnumFacing.UP);
		final BlockAcousticMap sub = this.substrateMap.get(substrate);
		return sub != null ? sub.getBlockAcousticsWithSpecial(trueState) : null;
	}

	private void put(@Nonnull final Block block, final int meta, @Nonnull final String substrate,
			@Nonnull final String value) {

		final Substrate s = Substrate.get(substrate);
		final IAcoustic[] acoustics = this.acousticsManager.compileAcoustics(value);

		if (s == null) {
			this.metaMap.put(new BlockInfo(block, meta), acoustics);
		} else {
			BlockAcousticMap sub = this.substrateMap.get(s);
			if (sub == null)
				this.substrateMap.put(s, sub = new BlockAcousticMap());
			sub.put(new BlockInfo(block, meta), acoustics);
		}
	}

	private void expand(@Nonnull final Block block, @Nonnull final String value) {
		final List<MacroEntry> macro = macros.get(value);
		if (macro != null) {
			for (final MacroEntry entry : macro) {
				final int trueMeta = MCHelper.hasVariants(block) ? entry.meta : BlockInfo.NO_SUBTYPE;
				put(block, trueMeta, entry.substrate, entry.value);
			}
		} else {
			DSurround.log().debug("Unknown macro '%s'", value);
		}
	}

	public void register(@Nonnull final String key, @Nonnull final String value) {
		final NameResult result = BlockNameUtil.parseBlockName(key);
		if (result != null) {
			final String blockName = result.getBlockName();
			final Block block = MCHelper.getBlockByName(blockName);
			if (block == null) {
				DSurround.log().debug("Unable to locate block for blockMap '%s'", blockName);
			} else if (block == Blocks.AIR) {
				DSurround.log().warn("Attempt to insert AIR into blockMap '%s'", blockName);
			} else if (value.startsWith("#")) {
				expand(block, value);
			} else {
				final int meta;
				if (result.isGeneric())
					meta = BlockInfo.GENERIC;
				else if (result.noMetadataSpecified())
					meta = MCHelper.hasVariants(block) ? BlockInfo.GENERIC : BlockInfo.NO_SUBTYPE;
				else
					meta = result.getMetadata();
				final String substrate = result.getExtras();
				put(block, meta, substrate, value);
			}
		} else {
			DSurround.log().warn("Malformed key in blockMap '%s'", key);
		}
	}

	@Nonnull
	private static String combine(@Nonnull final IAcoustic[] acoustics) {
		final StringBuilder builder = new StringBuilder();
		boolean addComma = false;
		for (final IAcoustic a : acoustics) {
			if (addComma)
				builder.append(",");
			else
				addComma = true;
			builder.append(a.getAcousticName());
		}
		return builder.toString();
	}

	public void collectData(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos,
			@Nonnull final List<String> data) {

		final IAcoustic[] temp = getBlockAcoustics(world, state, pos);
		if (temp != null)
			data.add(combine(temp));

		for (final Entry<Substrate, BlockAcousticMap> e : this.substrateMap.entrySet()) {
			final IAcoustic[] acoustics = e.getValue().getBlockAcousticsWithSpecial(state);
			if (acoustics != null)
				data.add(e.getKey() + ":" + combine(acoustics));
		}
	}

	public void clear() {
		this.metaMap.clear();
		this.substrateMap = new EnumMap<Substrate, BlockAcousticMap>(Substrate.class);
		this.metaMap.put(new BlockInfo(Blocks.AIR), AcousticsManager.NOT_EMITTER);
	}

	public void freeze() {
		this.metaMap.freeze();
	}
}
