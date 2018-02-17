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

package org.blockartistry.DynSurround.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.api.events.SpeechTextEvent;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSpeechBubble implements IMessage {

	public static class PacketHandler implements IMessageHandler<PacketSpeechBubble, IMessage> {
		@Override
		@Nullable
		public IMessage onMessage(@Nonnull final PacketSpeechBubble message, @Nullable final MessageContext ctx) {
			
			// Reject mob chat messages since they are generated 100% client side.
			if(message.translate)
				return null;
			
			Network.postEvent(new SpeechTextEvent(message.entityId, message.message, message.translate));
			return null;
		}
	}

	protected int entityId;
	protected String message;
	protected boolean translate;

	public PacketSpeechBubble() {

	}

	public PacketSpeechBubble(@Nonnull final Entity player, @Nonnull final String message,
			final boolean translate) {
		this.entityId = player.getEntityId();
		this.message = message;
		this.translate = translate;
	}

	@Override
	public void fromBytes(@Nonnull final ByteBuf buf) {
		this.entityId = buf.readInt();
		this.message = ByteBufUtils.readUTF8String(buf);
		this.translate = buf.readBoolean();
	}

	@Override
	public void toBytes(@Nonnull final ByteBuf buf) {
		buf.writeInt(this.entityId);
		ByteBufUtils.writeUTF8String(buf, this.message);
		buf.writeBoolean(this.translate);
	}

}
