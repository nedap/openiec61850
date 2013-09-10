/**
 * This class file was automatically generated by jASN1 (http://www.openmuc.org)
 */

package org.openmuc.openiec61850.internal.presentation.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.BerIdentifier;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.BerObjectIdentifier;

public final class Result_list {

	public final static class SubSeq {

		public final static BerIdentifier identifier = new BerIdentifier(BerIdentifier.UNIVERSAL_CLASS,
				BerIdentifier.CONSTRUCTED, 16);
		protected BerIdentifier id;

		public byte[] code = null;
		public BerInteger result = null;

		public BerObjectIdentifier transfer_syntax_name = null;

		public BerInteger provider_reason = null;

		public SubSeq() {
			id = identifier;
		}

		public SubSeq(byte[] code) {
			id = identifier;
			this.code = code;
		}

		public SubSeq(BerInteger result, BerObjectIdentifier transfer_syntax_name, BerInteger provider_reason) {
			id = identifier;
			this.result = result;
			this.transfer_syntax_name = transfer_syntax_name;
			this.provider_reason = provider_reason;
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
				if (provider_reason != null) {
					codeLength += provider_reason.encode(berOStream, false);
					codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 2))
							.encode(berOStream);
				}

				if (transfer_syntax_name != null) {
					codeLength += transfer_syntax_name.encode(berOStream, false);
					codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 1))
							.encode(berOStream);
				}

				codeLength += result.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 0))
						.encode(berOStream);

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
					result = new BerInteger();
					subCodeLength += result.decode(iStream, false);
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
					transfer_syntax_name = new BerObjectIdentifier();
					subCodeLength += transfer_syntax_name.decode(iStream, false);
					decodedIdentifier = false;
				}
			}
			if (subCodeLength < length.val) {
				if (decodedIdentifier == false) {
					subCodeLength += berIdentifier.decode(iStream);
					decodedIdentifier = true;
				}
				if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 2)) {
					provider_reason = new BerInteger();
					subCodeLength += provider_reason.decode(iStream, false);
					decodedIdentifier = false;
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

	public final static BerIdentifier identifier = new BerIdentifier(BerIdentifier.UNIVERSAL_CLASS,
			BerIdentifier.CONSTRUCTED, 16);
	protected BerIdentifier id;

	public byte[] code = null;
	public List<SubSeq> seqOf = null;

	public Result_list() {
		id = identifier;
	}

	public Result_list(byte[] code) {
		id = identifier;
		this.code = code;
	}

	public Result_list(List<SubSeq> seqOf) {
		id = identifier;
		this.seqOf = seqOf;
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
			for (int i = (seqOf.size() - 1); i >= 0; i--) {
				codeLength += seqOf.get(i).encode(berOStream, true);
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
		seqOf = new LinkedList<SubSeq>();

		if (explicit) {
			codeLength += id.decodeAndCheck(iStream);
		}

		BerLength length = new BerLength();
		codeLength += length.decode(iStream);

		while (subCodeLength < length.val) {
			SubSeq element = new SubSeq();
			subCodeLength += element.decode(iStream, true);
			seqOf.add(element);
		}
		if (subCodeLength != length.val) {
			throw new IOException("Decoded SequenceOf or SetOf has wrong length tag");

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