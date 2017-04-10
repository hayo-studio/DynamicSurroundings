/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.blockartistry.mod.DynSurround.util.gui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModLog;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ShaderUtils {

	@SideOnly(Side.CLIENT)
	public static class ParameterBindings {

		private final int shaderId;

		ParameterBindings(final int shader) {
			this.shaderId = shader;
		}

		private static int getIdx(final int shaderId, @Nonnull final String name) {
			return ARBShaderObjects.glGetUniformLocationARB(shaderId, name);
		}

		@Nonnull
		public ParameterBindings bind(@Nonnull final String name, @Nonnull final int... v) {
			assert v.length != 0;

			final int idx = getIdx(this.shaderId, name);
			switch (v.length) {
			case 1:
				ARBShaderObjects.glUniform1iARB(idx, v[0]);
				break;
			case 2:
				ARBShaderObjects.glUniform2iARB(idx, v[0], v[1]);
				break;
			case 3:
				ARBShaderObjects.glUniform3iARB(idx, v[0], v[1], v[2]);
				break;
			case 4:
				ARBShaderObjects.glUniform4iARB(idx, v[0], v[1], v[2], v[3]);
				break;
			}

			return this;
		}

		@Nonnull
		public ParameterBindings bind(@Nonnull final String name, @Nonnull final IntBuffer buf) {
			final int idx = getIdx(this.shaderId, name);
			ARBShaderObjects.glUniform1ARB(idx, buf);
			return this;
		}

		@Nonnull
		public ParameterBindings bind(@Nonnull final String name, @Nonnull final float... v) {
			assert v.length != 0;

			final int idx = getIdx(this.shaderId, name);
			switch (v.length) {
			case 1:
				ARBShaderObjects.glUniform1fARB(idx, v[0]);
				break;
			case 2:
				ARBShaderObjects.glUniform2fARB(idx, v[0], v[1]);
				break;
			case 3:
				ARBShaderObjects.glUniform3fARB(idx, v[0], v[1], v[2]);
				break;
			case 4:
				ARBShaderObjects.glUniform4fARB(idx, v[0], v[1], v[2], v[3]);
				break;
			}

			return this;
		}

		@Nonnull
		public ParameterBindings bind(@Nonnull final String name, @Nonnull final FloatBuffer buf) {
			final int idx = getIdx(this.shaderId, name);
			ARBShaderObjects.glUniform1ARB(idx, buf);
			return this;
		}
	}

	private static final ParameterBindings NULL_BINDINGS = new ParameterBindings(0) {
		@Override
		@Nonnull
		public ParameterBindings bind(@Nonnull final String name, @Nonnull final int... v) {
			return this;
		}

		@Override
		@Nonnull
		public ParameterBindings bind(@Nonnull final String name, @Nonnull final IntBuffer buf) {
			return this;
		}

		@Override
		@Nonnull
		public ParameterBindings bind(@Nonnull final String name, @Nonnull final float... v) {
			return this;
		}

		@Override
		@Nonnull
		public ParameterBindings bind(@Nonnull final String name, @Nonnull final FloatBuffer buf) {
			return this;
		}
	};

	private ShaderUtils() {

	}

	public static boolean useShaders() {
		// TODO: Option to turn off shaders
		return false; // OpenGlHelper.shadersSupported;
	}

	@Nonnull
	public static ParameterBindings useShader(final int shader) {

		if (useShaders() && shader != 0) {
			ARBShaderObjects.glUseProgramObjectARB(shader);
			return new ParameterBindings(shader);
		}

		return NULL_BINDINGS;
	}

	public static void closeShader() {
		if (useShaders())
			ARBShaderObjects.glUseProgramObjectARB(0);
	}

	// Most of the code taken from the LWJGL wiki
	// http://wiki.lwjgl.org/wiki/GLSL_Shaders_with_LWJGL.html

	public static int createProgram(@Nullable final String vert, @Nullable final String frag) {
		int vertId = 0, fragId = 0, program;
		if (vert != null)
			vertId = createShader(vert, ARBVertexShader.GL_VERTEX_SHADER_ARB);
		if (frag != null)
			fragId = createShader(frag, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

		program = ARBShaderObjects.glCreateProgramObjectARB();
		if (program == 0)
			return 0;

		if (vert != null)
			ARBShaderObjects.glAttachObjectARB(program, vertId);
		if (frag != null)
			ARBShaderObjects.glAttachObjectARB(program, fragId);

		ARBShaderObjects.glLinkProgramARB(program);
		if (ARBShaderObjects.glGetObjectParameteriARB(program,
				ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
			ModLog.error(getLogInfo(program), null);
			return 0;
		}

		ARBShaderObjects.glValidateProgramARB(program);
		if (ARBShaderObjects.glGetObjectParameteriARB(program,
				ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
			ModLog.error(getLogInfo(program), null);
			return 0;
		}

		return program;
	}

	private static int createShader(@Nullable final String filename, final int shaderType) {
		int shader = 0;
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

			if (shader == 0)
				return 0;

			ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
			ARBShaderObjects.glCompileShaderARB(shader);

			if (ARBShaderObjects.glGetObjectParameteriARB(shader,
					ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
				throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

			return shader;
		} catch (Exception e) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			e.printStackTrace();
			return -1;
		}
	}

	private static String getLogInfo(final int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj,
				ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	private static String readFileAsString(@Nonnull final String filename) throws Exception {
		final InputStream in = ShaderUtils.class.getResourceAsStream(filename);

		if (in == null)
			return "";

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
			final StringBuilder builder = new StringBuilder();
			String s = null;
			while ((s = reader.readLine()) != null)
				builder.append(s).append('\n');
			return builder.toString();
		}
	}

}
