/*
 * nimbus-jose-jwt
 *
 * Copyright 2012-2022, Connect2id Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.nimbusds.jose.jwk.source;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * This JWK set source implements a workaround for transient network problems. <br>
 * <br>
 * It retries getting the list of JWKS if the wrapped source throws a
 * {@linkplain JWKSetUnavailableException}.
 */

public class RetryingJWKSetSource<C extends SecurityContext> extends BaseJWKSetSource<C> {

	public static interface Listener<C extends SecurityContext> extends JWKSetSourceListener<C> {
		void onRetrying(Exception e, C context);
	}
	
	private final Listener<C> listener;
	
	public RetryingJWKSetSource(JWKSetSource<C> source, Listener<C> listener) {
		super(source);
		this.listener = listener;
	}

	@Override
	public JWKSet getJWKSet(long time, boolean forceUpdate, C context) throws KeySourceException {
		try {
			return source.getJWKSet(time, forceUpdate, context);
		} catch (JWKSetUnavailableException e) {
			// assume transient network issue, retry once
			listener.onRetrying(e, context);

			return source.getJWKSet(time, forceUpdate, context);
		}
	}

}
