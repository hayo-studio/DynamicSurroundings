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
package org.blockartistry.DynSurround.expression;

import java.util.List;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.lib.expression.ExpressionCache;
import org.blockartistry.lib.expression.IDynamicVariant;
import org.blockartistry.lib.expression.Variant;

public final class ExpressionEngine {

	private static ExpressionEngine instance;

	public static ExpressionEngine instance() {
		if (instance == null)
			instance = new ExpressionEngine();
		return instance;
	}

	private final ExpressionCache cache = new ExpressionCache(DSurround.log());

	private ExpressionEngine() {
		this.cache.add(new BiomeTypeVariables());
		this.cache.add(new BiomeVariables());
		this.cache.add(new PlayerVariables());
		this.cache.add(new WeatherVariables());
		this.cache.add(new BattleVariables());
		this.cache.add(new MiscVariables());
	}

	public void update() {
		this.cache.update();
	}
	
	public List<IDynamicVariant<?>> getVariables() {
		return this.cache.getVariantList();
	}

	public Variant eval(final String exp) {
		return this.cache.eval(exp);
	}

	public List<String> getNaughtyList() {
		return this.cache.getNaughtyList();
	}

	public boolean check(final String exp) {
		return this.cache.check(exp);
	}

}
