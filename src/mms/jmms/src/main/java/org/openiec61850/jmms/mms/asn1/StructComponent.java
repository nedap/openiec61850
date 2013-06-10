/**
 * This class file was automatically generated by jASN1 (http://www.openmuc.org)
 */

package org.openiec61850.jmms.mms.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.LinkedList;
import org.openmuc.jasn1.ber.*;
import org.openmuc.jasn1.ber.types.*;
import org.openmuc.jasn1.ber.types.string.*;

public class StructComponent {

	public final static BerIdentifier identifier = new BerIdentifier(BerIdentifier.UNIVERSAL_CLASS, BerIdentifier.CONSTRUCTED, 16);
	protected BerIdentifier id;

	public byte[] code = null;
	public BerVisibleString componentName = null;

	public TypeSpecification componentType = null;

	public StructComponent() {
		id = identifier;
	}

	public StructComponent(byte[] code) {
		id = identifier;
		this.code = code;
	}

	public StructComponent(BerVisibleString componentName, TypeSpecification componentType) {
		id = identifier;
		this.componentName = componentName;
		this.componentType = componentType;
	}

	public int encode(BerByteArrayOutputStream berOStream, boolean explicit) throws IOException {

		int codeLength;

		if (code != null) {
			codeLength = code.length;
			for (int i = code.length - 1; i >= 0; i--) {
				berOStream.write(code[i]);
			}
		}
		else {
			codeLength = 0;
			int sublength;

			sublength = componentType.encode(berOStream, true);
			codeLength += sublength;
			codeLength += BerLength.encodeLength(berOStream, sublength);
			codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 1)).encode(berOStream);
			
			if (componentName != null) {
				codeLength += componentName.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 0)).encode(berOStream);
			}
			
			codeLength += BerLength.encodeLength(berOStream, codeLength);
		}

		if (explicit) {
			codeLength += id.encode(berOStream);
		}

		return codeLength;

	}

	public int decode(InputStream iStream, boolean explicit) throws IOException {
		int codeLength = 0;
		int subCodeLength = 0;
		int choiceDecodeLength = 0;
		BerIdentifier berIdentifier = new BerIdentifier();
		boolean decodedIdentifier = false;

		if (explicit) {
			codeLength += id.decodeAndCheck(iStream);
		}

		BerLength length = new BerLength();
		codeLength += length.decode(iStream);

		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 0)) {
				componentName = new BerVisibleString();
				subCodeLength += componentName.decode(iStream, false);
				decodedIdentifier = false;
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 1)) {
				subCodeLength += new BerLength().decode(iStream);
				componentType = new TypeSpecification();
				choiceDecodeLength = componentType.decode(iStream, null);
				if (choiceDecodeLength != 0) {
					decodedIdentifier = false;
					subCodeLength += choiceDecodeLength;
				}
			}
			else {
				throw new IOException("Identifier does not macht required sequence element identifer.");
			}
		}
		if (subCodeLength != length.val) {
			throw new IOException("Decoded sequence has wrong length tag");

		}
		codeLength += subCodeLength;

		return codeLength;
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		BerByteArrayOutputStream berOStream = new BerByteArrayOutputStream(encodingSizeGuess);
		encode(berOStream, false);
		code = berOStream.getArray();
	}
}

