/**
 * This class file was automatically generated by jASN1 (http://www.openmuc.org)
 */

package org.openmuc.openiec61850.internal.acse.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.LinkedList;
import org.openmuc.jasn1.ber.*;
import org.openmuc.jasn1.ber.types.*;
import org.openmuc.jasn1.ber.types.string.*;

public final class MMS_TLS_Authentication_value {

	public byte[] code = null;
	public final static class SubSeq_certificate_based {

		public final static BerIdentifier identifier = new BerIdentifier(BerIdentifier.UNIVERSAL_CLASS, BerIdentifier.CONSTRUCTED, 16);
		protected BerIdentifier id;

		public byte[] code = null;
		public BerOctetString authentication_Certificate = null;

		public BerGeneralizedTime time = null;

		public BerOctetString signature = null;

		public SubSeq_certificate_based() {
			id = identifier;
		}

		public SubSeq_certificate_based(byte[] code) {
			id = identifier;
			this.code = code;
		}

		public SubSeq_certificate_based(BerOctetString authentication_Certificate, BerGeneralizedTime time, BerOctetString signature) {
			id = identifier;
			this.authentication_Certificate = authentication_Certificate;
			this.time = time;
			this.signature = signature;
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
				codeLength += signature.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 2)).encode(berOStream);
				
				codeLength += time.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 1)).encode(berOStream);
				
				codeLength += authentication_Certificate.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 0)).encode(berOStream);
				
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
					authentication_Certificate = new BerOctetString();
					subCodeLength += authentication_Certificate.decode(iStream, false);
					decodedIdentifier = false;
				}
				else {
					throw new IOException("Identifier does not macht required sequence element identifer.");
				}
			}
			if (subCodeLength < length.val) {
				if (decodedIdentifier == false) {
					subCodeLength += berIdentifier.decode(iStream);
					decodedIdentifier = true;
				}
				if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 1)) {
					time = new BerGeneralizedTime();
					subCodeLength += time.decode(iStream, false);
					decodedIdentifier = false;
				}
				else {
					throw new IOException("Identifier does not macht required sequence element identifer.");
				}
			}
			if (subCodeLength < length.val) {
				if (decodedIdentifier == false) {
					subCodeLength += berIdentifier.decode(iStream);
					decodedIdentifier = true;
				}
				if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 2)) {
					signature = new BerOctetString();
					subCodeLength += signature.decode(iStream, false);
					decodedIdentifier = false;
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

	public SubSeq_certificate_based certificate_based = null;

	public MMS_TLS_Authentication_value() {
	}

	public MMS_TLS_Authentication_value(byte[] code) {
		this.code = code;
	}

	public MMS_TLS_Authentication_value(SubSeq_certificate_based certificate_based) {
		this.certificate_based = certificate_based;
	}

	public int encode(BerByteArrayOutputStream berOStream, boolean explicit) throws IOException {
		if (code != null) {
			for (int i = code.length - 1; i >= 0; i--) {
				berOStream.write(code[i]);
			}
			return code.length;

		}
		int codeLength = 0;
		if (certificate_based != null) {
			codeLength += certificate_based.encode(berOStream, false);
			codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 0)).encode(berOStream);
			return codeLength;

		}
		
		throw new IOException("Error encoding BerChoice: No item in choice was selected.");
	}

	public int decode(InputStream iStream, BerIdentifier berIdentifier) throws IOException {
		int codeLength = 0;
		int choiceDecodeLength = 0;
		BerIdentifier passedIdentifier = berIdentifier;
		if (berIdentifier == null) {
			berIdentifier = new BerIdentifier();
			codeLength += berIdentifier.decode(iStream);
		}
		if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 0)) {
			certificate_based = new SubSeq_certificate_based();
			codeLength += certificate_based.decode(iStream, false);
			return codeLength;
		}

		if (passedIdentifier != null) {
			return 0;
		}
		throw new IOException("Error decoding BerChoice: Identifier matched to no item.");
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		BerByteArrayOutputStream berOStream = new BerByteArrayOutputStream(encodingSizeGuess);
		encode(berOStream, false);
		code = berOStream.getArray();
	}
}
