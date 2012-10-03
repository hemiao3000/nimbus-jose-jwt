package com.nimbusds.jose.crypto;


import java.security.Signature;
import java.security.InvalidKeyException;
import java.security.SignatureException;

import java.security.interfaces.RSAPublicKey;

import com.nimbusds.jose.sdk.JOSEException;
import com.nimbusds.jose.sdk.JWSValidator;
import com.nimbusds.jose.sdk.ReadOnlyJWSHeader;

import com.nimbusds.jose.sdk.util.Base64URL;



/**
 * RSA Signature-Scheme-with-Appendix (RSASSA) validator of 
 * {@link com.nimbusds.jose.sdk.JWSObject JWS objects}.
 *
 * <p>Supports the following JSON Web Algorithms (JWAs):
 *
 * <ul>
 *     <li>{@link com.nimbusds.jose.sdk.JWSAlgorithm#RS256}
 *     <li>{@link com.nimbusds.jose.sdk.JWSAlgorithm#RS384}
 *     <li>{@link com.nimbusds.jose.sdk.JWSAlgorithm#RS512}
 * </ul>
 * 
 * @author Vladimir Dzhuvinov
 * @version $version$ (2012-10-03)
 */
public class RSASSAValidator extends RSASSAProvider implements JWSValidator {


	/**
	 * The public RSA key.
	 */
	private final RSAPublicKey publicKey;
	
	
	/**
	 * Creates a new RSA Signature-Scheme-with-Appendix (RSASSA) validator.
	 *
	 * @param publicKey The public RSA key. Must not be {@code null}.
	 */
	public RSASSAValidator(final RSAPublicKey publicKey) {

		if (publicKey == null)
			throw new IllegalArgumentException("The public RSA key must not be null");
		
		this.publicKey = publicKey;
	}
	
	
	/**
	 * Gets the public RSA key.
	 *
	 * @return The public RSA key.
	 */
	public RSAPublicKey getPublicKey() {
	
		return publicKey;
	}


	@Override
	public boolean validate(final ReadOnlyJWSHeader header, 
	                        final byte[] signedContent, 
			        final Base64URL signature)
		throws JOSEException {
		
		ensureAcceptedAlgorithm(header.getAlgorithm());
		
		Signature validator = getRSASignerAndValidator(header.getAlgorithm());
		
		try {
			validator.initVerify(publicKey);
			validator.update(signedContent);
			return validator.verify(signature.decode());
			
		} catch (InvalidKeyException e) {
		
			throw new JOSEException("Invalid public RSA key: " + e.getMessage(), e);
		
		} catch (SignatureException e) {
		
			throw new JOSEException("RSA signature exception: " + e.getMessage(), e);
		}
	}
}
