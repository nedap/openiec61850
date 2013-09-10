/*
 * Copyright 2011-13 Fraunhofer ISE, energy & meteo Systems GmbH and other contributors
 *
 * This file is part of OpenIEC61850.
 * For more information visit http://www.openmuc.org
 *
 * OpenIEC61850 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * OpenIEC61850 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenIEC61850.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openmuc.openiec61850.internal.acse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Simple InputStream wrapper around a {@link ByteBuffer} object
 * 
 * @author Karsten Mueller-Bier
 */
public final class ByteBufferInputStream extends InputStream {

	private final ByteBuffer buf;

	public ByteBufferInputStream(ByteBuffer buf) {
		this.buf = buf;
	}

	@Override
	public int read() throws IOException {
		if (buf.hasRemaining() == false) {
			return -1;
		}
		return buf.get() & 0xFF;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (buf.hasRemaining() == false) {
			return -1;
		}
		int size = Math.min(len, available());

		buf.get(b, off, size);
		return size;
	}

	@Override
	public int available() throws IOException {
		return buf.limit() - buf.position();
	}
}
