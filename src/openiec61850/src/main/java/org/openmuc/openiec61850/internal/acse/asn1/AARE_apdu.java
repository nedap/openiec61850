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

public final class AARE_apdu {

	public final static BerIdentifier identifier = new BerIdentifier(BerIdentifier.APPLICATION_CLASS, BerIdentifier.CONSTRUCTED, 1);
	protected BerIdentifier id;

	public byte[] code = null;
	public BerBitString protocol_version = null;

	public BerObjectIdentifier application_context_name = null;

	public BerInteger result = null;

	public Associate_source_diagnostic result_source_diagnostic = null;

	public AP_title responding_AP_title = null;

	public AE_qualifier responding_AE_qualifier = null;

	public BerInteger responding_AP_invocation_identifier = null;

	public BerInteger responding_AE_invocation_identifier = null;

	public BerBitString responder_acse_requirements = null;

	public BerObjectIdentifier mechanism_name = null;

	public Authentication_value responding_authentication_value = null;

	public Application_context_name_list application_context_name_list = null;

	public BerGraphicString implementation_information = null;

	public Association_information user_information = null;

	public AARE_apdu() {
		id = identifier;
	}

	public AARE_apdu(byte[] code) {
		id = identifier;
		this.code = code;
	}

	public AARE_apdu(BerBitString protocol_version, BerObjectIdentifier application_context_name, BerInteger result,
            Associate_source_diagnostic result_source_diagnostic, AP_title responding_AP_title,
            AE_qualifier responding_AE_qualifier, BerInteger responding_AP_invocation_identifier,
            BerInteger responding_AE_invocation_identifier, BerBitString responder_acse_requirements,
            BerObjectIdentifier mechanism_name, Authentication_value responding_authentication_value,
            Application_context_name_list application_context_name_list, BerGraphicString implementation_information,
            Association_information user_information) {
		id = identifier;
		this.protocol_version = protocol_version;
		this.application_context_name = application_context_name;
		this.result = result;
		this.result_source_diagnostic = result_source_diagnostic;
		this.responding_AP_title = responding_AP_title;
		this.responding_AE_qualifier = responding_AE_qualifier;
		this.responding_AP_invocation_identifier = responding_AP_invocation_identifier;
		this.responding_AE_invocation_identifier = responding_AE_invocation_identifier;
		this.responder_acse_requirements = responder_acse_requirements;
		this.mechanism_name = mechanism_name;
		this.responding_authentication_value = responding_authentication_value;
		this.application_context_name_list = application_context_name_list;
		this.implementation_information = implementation_information;
		this.user_information = user_information;
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

			if (user_information != null) {
				codeLength += user_information.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 30)).encode(berOStream);
			}

			if (implementation_information != null) {
				codeLength += implementation_information.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 29)).encode(berOStream);
			}

			if (application_context_name_list != null) {
				codeLength += application_context_name_list.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 11)).encode(berOStream);
			}

			if (responding_authentication_value != null) {
				sublength = responding_authentication_value.encode(berOStream, true);
				codeLength += sublength;
				codeLength += BerLength.encodeLength(berOStream, sublength);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 10)).encode(berOStream);
			}

			if (mechanism_name != null) {
				codeLength += mechanism_name.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 9)).encode(berOStream);
			}

			if (responder_acse_requirements != null) {
				codeLength += responder_acse_requirements.encode(berOStream, false);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 8)).encode(berOStream);
			}

			if (responding_AE_invocation_identifier != null) {
				sublength = responding_AE_invocation_identifier.encode(berOStream, true);
				codeLength += sublength;
				codeLength += BerLength.encodeLength(berOStream, sublength);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 7)).encode(berOStream);
			}

			if (responding_AP_invocation_identifier != null) {
				sublength = responding_AP_invocation_identifier.encode(berOStream, true);
				codeLength += sublength;
				codeLength += BerLength.encodeLength(berOStream, sublength);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 6)).encode(berOStream);
			}

			if (responding_AE_qualifier != null) {
				sublength = responding_AE_qualifier.encode(berOStream, true);
				codeLength += sublength;
				codeLength += BerLength.encodeLength(berOStream, sublength);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 5)).encode(berOStream);
			}

			if (responding_AP_title != null) {
				sublength = responding_AP_title.encode(berOStream, true);
				codeLength += sublength;
				codeLength += BerLength.encodeLength(berOStream, sublength);
				codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 4)).encode(berOStream);
			}

			sublength = result_source_diagnostic.encode(berOStream, true);
			codeLength += sublength;
			codeLength += BerLength.encodeLength(berOStream, sublength);
			codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 3)).encode(berOStream);

			sublength = result.encode(berOStream, true);
			codeLength += sublength;
			codeLength += BerLength.encodeLength(berOStream, sublength);
			codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 2)).encode(berOStream);

			sublength = application_context_name.encode(berOStream, true);
			codeLength += sublength;
			codeLength += BerLength.encodeLength(berOStream, sublength);
			codeLength += (new BerIdentifier(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 1)).encode(berOStream);

			if (protocol_version != null) {
				codeLength += protocol_version.encode(berOStream, false);
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
				protocol_version = new BerBitString();
				subCodeLength += protocol_version.decode(iStream, false);
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
				application_context_name = new BerObjectIdentifier();
				subCodeLength += application_context_name.decode(iStream, true);
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
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 2)) {
				subCodeLength += new BerLength().decode(iStream);
				result = new BerInteger();
				subCodeLength += result.decode(iStream, true);
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
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 3)) {
				subCodeLength += new BerLength().decode(iStream);
				result_source_diagnostic = new Associate_source_diagnostic();
				choiceDecodeLength = result_source_diagnostic.decode(iStream, null);
				if (choiceDecodeLength != 0) {
					decodedIdentifier = false;
					subCodeLength += choiceDecodeLength;
				}
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
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 4)) {
				subCodeLength += new BerLength().decode(iStream);
				responding_AP_title = new AP_title();
				choiceDecodeLength = responding_AP_title.decode(iStream, null);
				if (choiceDecodeLength != 0) {
					decodedIdentifier = false;
					subCodeLength += choiceDecodeLength;
				}
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 5)) {
				subCodeLength += new BerLength().decode(iStream);
				responding_AE_qualifier = new AE_qualifier();
				choiceDecodeLength = responding_AE_qualifier.decode(iStream, null);
				if (choiceDecodeLength != 0) {
					decodedIdentifier = false;
					subCodeLength += choiceDecodeLength;
				}
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 6)) {
				subCodeLength += new BerLength().decode(iStream);
				responding_AP_invocation_identifier = new BerInteger();
				subCodeLength += responding_AP_invocation_identifier.decode(iStream, true);
				decodedIdentifier = false;
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 7)) {
				subCodeLength += new BerLength().decode(iStream);
				responding_AE_invocation_identifier = new BerInteger();
				subCodeLength += responding_AE_invocation_identifier.decode(iStream, true);
				decodedIdentifier = false;
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 8)) {
				responder_acse_requirements = new BerBitString();
				subCodeLength += responder_acse_requirements.decode(iStream, false);
				decodedIdentifier = false;
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 9)) {
				mechanism_name = new BerObjectIdentifier();
				subCodeLength += mechanism_name.decode(iStream, false);
				decodedIdentifier = false;
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 10)) {
				subCodeLength += new BerLength().decode(iStream);
				responding_authentication_value = new Authentication_value();
				choiceDecodeLength = responding_authentication_value.decode(iStream, null);
				if (choiceDecodeLength != 0) {
					decodedIdentifier = false;
					subCodeLength += choiceDecodeLength;
				}
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 11)) {
				application_context_name_list = new Application_context_name_list();
				subCodeLength += application_context_name_list.decode(iStream, false);
				decodedIdentifier = false;
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.PRIMITIVE, 29)) {
				implementation_information = new BerGraphicString();
				subCodeLength += implementation_information.decode(iStream, false);
				decodedIdentifier = false;
			}
		}
		if (subCodeLength < length.val) {
			if (decodedIdentifier == false) {
				subCodeLength += berIdentifier.decode(iStream);
				decodedIdentifier = true;
			}
			if (berIdentifier.equals(BerIdentifier.CONTEXT_CLASS, BerIdentifier.CONSTRUCTED, 30)) {
				user_information = new Association_information();
				subCodeLength += user_information.decode(iStream, false);
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

