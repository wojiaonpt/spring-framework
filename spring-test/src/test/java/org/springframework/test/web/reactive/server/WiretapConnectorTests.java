/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.web.reactive.server;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpResponse;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeFunctions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for {@link WiretapConnector}.
 *
 * @author Rossen Stoyanchev
 */
public class WiretapConnectorTests {

	@Test
	public void listener() throws Exception {

		ClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, "/test");
		ClientHttpResponse response = new MockClientHttpResponse(HttpStatus.OK);
		ClientHttpConnector connector = (method, uri, fn) -> fn.apply(request).then(Mono.just(response));

		AtomicReference<WiretapConnector.Info> infoRef = new AtomicReference<>();
		WiretapConnector wiretapConnector = new WiretapConnector(connector);
		wiretapConnector.addListener(infoRef::set);

		ExchangeFunction exchangeFn = ExchangeFunctions.create(wiretapConnector);
		ClientRequest clientRequest = ClientRequest.method(HttpMethod.GET, URI.create("/test")).build();
		exchangeFn.exchange(clientRequest).blockMillis(0);

		WiretapConnector.Info info = infoRef.get();
		assertNotNull(info);
		assertEquals(HttpMethod.GET, info.getMethod());
		assertEquals("/test", info.getUrl().toString());
		assertSame(response, info.getResponse());
	}

}
