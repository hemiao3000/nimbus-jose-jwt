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

import org.junit.Test;

import com.nimbusds.jose.proc.SecurityContext;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class JWKSourceBuilderTest extends AbstractDelegateSourceTest {

	@Test
	public void testShouldCreateCachedProvider() {
		JWKSource<SecurityContext> provider = builder().rateLimited(false).cached(true).health(false).build();
		assertNotNull(provider);

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(2, jwksProviders.size());

		assertTrue(jwksProviders.get(0) instanceof CachedJWKSetSource);
		assertTrue(jwksProviders.get(1) instanceof JWKSetSource);
	}

	@Test
	public void testShouldCreateCachedProviderWithCustomValues() {
		JWKSource<SecurityContext> provider = builder().rateLimited(false).cached(24 * 3600 * 1000, 15 * 1000).health(false).build();

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(2, jwksProviders.size());

		CachedJWKSetSource<SecurityContext, CachedJWKSetSource.Listener<SecurityContext>> cachedJwksProvider = (CachedJWKSetSource<SecurityContext, CachedJWKSetSource.Listener<SecurityContext>>) jwksProviders.get(0);

		assertEquals(cachedJwksProvider.getTimeToLive(), TimeUnit.HOURS.toMillis(24));
	}

	@Test
	public void testShouldCreateRateLimitedProvider() {
		JWKSource<SecurityContext> provider = builder().rateLimited(true).build();

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(4, jwksProviders.size());

		assertTrue(jwksProviders.get(1) instanceof RateLimitedJWKSetSource);
	}

	@Test
	public void testShouldCreateRateLimitedProviderWithCustomValues() {
		JWKSource<SecurityContext> provider = builder().rateLimited(30 * 1000).build();

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(4, jwksProviders.size());

		assertTrue(jwksProviders.get(1) instanceof RateLimitedJWKSetSource);
	}

	@Test
	public void testShouldCreateCachedAndRateLimitedProvider() {
		JWKSource<SecurityContext> provider = builder().cached(true).rateLimited(true).build();

		assertNotNull(provider);

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(4, jwksProviders.size());

		assertTrue(jwksProviders.get(0) instanceof CachedJWKSetSource);
		assertTrue(jwksProviders.get(1) instanceof RateLimitedJWKSetSource);
		assertTrue(jwksProviders.get(2) instanceof DefaultHealthJWKSetSource);
		assertTrue(jwksProviders.get(3) instanceof JWKSetSource);
	}

	@Test
	public void testShouldCreateCachedAndRateLimitedProviderWithCustomValues() {
		JWKSource<SecurityContext> provider = builder().cached(24 * 3600 * 1000, 15 * 1000).rateLimited(30 * 1000).build();

		assertNotNull(provider);

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(4, jwksProviders.size());

		assertTrue(jwksProviders.get(0) instanceof CachedJWKSetSource);
		assertTrue(jwksProviders.get(1) instanceof RateLimitedJWKSetSource);
		assertTrue(jwksProviders.get(2) instanceof DefaultHealthJWKSetSource);
		assertTrue(jwksProviders.get(3) instanceof JWKSetSource);
	}

	@Test
	public void testShouldCreateCachedAndRateLimitedProviderByDefault() {
		JWKSource<SecurityContext> provider = builder().build();
		assertNotNull(provider);

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(4, jwksProviders.size());

		assertTrue(jwksProviders.get(0) instanceof CachedJWKSetSource);
		assertTrue(jwksProviders.get(1) instanceof RateLimitedJWKSetSource);
		assertTrue(jwksProviders.get(2) instanceof DefaultHealthJWKSetSource);
		assertTrue(jwksProviders.get(3) instanceof JWKSetSource);
	}

	// peek into the jwk source and get the underlying set providers
	@SuppressWarnings("resource")
	private List<JWKSetSource<SecurityContext>> jwksProviders(JWKSource<SecurityContext> jwkSource) {
		UrlJWKSource<SecurityContext> remoteJWKSet = (UrlJWKSource<SecurityContext>) jwkSource;

		JWKSetSource<SecurityContext> jwksProvider = remoteJWKSet.getSource();

		List<JWKSetSource<SecurityContext>> list = new ArrayList<>();

		list.add(jwksProvider);

		while (jwksProvider instanceof BaseJWKSetSource) {
			BaseJWKSetSource<SecurityContext> baseJwksProvider = (BaseJWKSetSource<SecurityContext>) jwksProvider;

			jwksProvider = baseJwksProvider.getSource();

			list.add(jwksProvider);
		}

		return list;
	}

	@Test
	public void testShouldCreateRetryingProvider() {
		JWKSource<SecurityContext> provider = builder().rateLimited(false).cached(false).preemptiveCacheRefresh(false).retrying(true).health(false).build();
		assertNotNull(provider);

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(2, jwksProviders.size());

		assertTrue(jwksProviders.get(0) instanceof RetryingJWKSetSource);
		assertTrue(jwksProviders.get(1) instanceof JWKSetSource);
	}

	@Test
	public void testShouldCreateOutageCachedProvider() {
		JWKSource<SecurityContext> provider = builder().rateLimited(false).cached(false).preemptiveCacheRefresh(false).outageCached(true).health(false).build();
		assertNotNull(provider);

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(2, jwksProviders.size());

		assertTrue(jwksProviders.get(0) instanceof OutageCachedJWKSetSource);
		assertTrue(jwksProviders.get(1) instanceof JWKSetSource);
	}

	@Test
	public void testShouldCreateOutageCachedProviderWithCustomValues() {
		JWKSource<SecurityContext> provider = builder().rateLimited(false).cached(false).health(false).preemptiveCacheRefresh(false).outageCached(24 * 3600 * 1000).build();

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(2, jwksProviders.size());

		OutageCachedJWKSetSource<SecurityContext> cachedJwksProvider = (OutageCachedJWKSetSource<SecurityContext>) jwksProviders.get(0);

		assertEquals(cachedJwksProvider.getTimeToLive(), TimeUnit.HOURS.toMillis(24));
	}

	@Test
	public void testShouldCreateCachedAndRateLimitedAndOutageAndRetryingProvider() {
		JWKSource<SecurityContext> provider = builder().cached(true).rateLimited(true).retrying(true).outageCached(true).health(true).build();

		assertNotNull(provider);

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(6, jwksProviders.size());

		assertTrue(jwksProviders.get(0) instanceof CachedJWKSetSource);
		assertTrue(jwksProviders.get(1) instanceof RateLimitedJWKSetSource);
		assertTrue(jwksProviders.get(2) instanceof DefaultHealthJWKSetSource);
		assertTrue(jwksProviders.get(3) instanceof OutageCachedJWKSetSource);
		assertTrue(jwksProviders.get(4) instanceof RetryingJWKSetSource);
		assertTrue(jwksProviders.get(5) instanceof JWKSetSource);
	}

	@Test
	public void testShouldCreateWithCustomJwksProvider() {
		JWKSetSource<SecurityContext> customJwksProvider = mock(JWKSetSource.class);

		@SuppressWarnings("unchecked")
		JWKSource<SecurityContext> provider = new JWKSourceBuilder<>(customJwksProvider).build();

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(4, jwksProviders.size());

		assertSame(jwksProviders.get(jwksProviders.size() - 1), customJwksProvider);
	}

	@Test
	public void testShouldCreatePreemptiveCachedProvider() {
		JWKSource<SecurityContext> provider = builder().rateLimited(false).preemptiveCacheRefresh(10 * 1000, true).health(false).build();
		assertNotNull(provider);

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(2, jwksProviders.size());

		assertTrue(jwksProviders.get(0) instanceof PreemptiveCachedJWKSetSource);
		assertTrue(jwksProviders.get(1) instanceof JWKSetSource);
	}

	@Test
	public void testShouldFailWhenRatelimitingWithoutCaching() {
		try {
			builder().cached(false).rateLimited(true).build();
			fail();
		} catch (IllegalStateException e) {
			// pass
		}
	}

	@Test
	public void testShouldEnableCacheWhenPreemptiveCaching() {
		JWKSource<SecurityContext> provider = builder().rateLimited(false).cached(false).health(false).preemptiveCacheRefresh(true).build();

		assertNotNull(provider);

		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(provider);
		assertEquals(2, jwksProviders.size());

		assertTrue(jwksProviders.get(0) instanceof PreemptiveCachedJWKSetSource);
		assertTrue(jwksProviders.get(1) instanceof JWKSetSource);
	}

	@Test
	public void testShouldLocalProviderForFileURL() throws MalformedURLException {
		File file = new File("test");
		URL url = file.toURI().toURL();
		JWKSource<SecurityContext> source = JWKSourceBuilder.newBuilder(url).build();
		
		List<JWKSetSource<SecurityContext>> jwksProviders = jwksProviders(source);

		assertTrue(jwksProviders.get(jwksProviders.size() - 1) instanceof LocalUrlJWKSetSource);
	}

}