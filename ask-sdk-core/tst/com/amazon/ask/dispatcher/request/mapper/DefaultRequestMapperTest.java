/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.dispatcher.request.mapper;

import com.amazon.ask.dispatcher.request.mapper.impl.DefaultRequestMapper;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.events.skillevents.SkillEnabledRequest;
import com.amazon.ask.dispatcher.request.handler.impl.DefaultRequestHandlerChain;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.dispatcher.request.handler.RequestHandlerChain;
import java.util.ArrayList;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultRequestMapperTest {

    @Test(expected = IllegalArgumentException.class)
    public void no_handler_list_throws_illegal_argument_exception() {
        DefaultRequestMapper.builder().build();
    }

    @Test
    public void empty_handler_list_does_not_throws_illegal_argument_exception() {
        try {
            DefaultRequestMapper.builder().withRequestHandlerChains(new ArrayList<>()).build();
        } catch(IllegalArgumentException ex) {
            fail("Exception should have not been thrown: " + ex.toString());
        }
    }

    @Test
    public void no_handler_registered_for_intent() {
        IntentRequest request = IntentRequest.builder().withIntent(Intent.builder().withName("FooIntent").build()).withRequestId(UUID.randomUUID().toString()).build();
        RequestEnvelope envelope = RequestEnvelope.<IntentRequest>builder().withRequest(request).build();
        HandlerInput input = HandlerInput.builder().withRequestEnvelope(envelope).build();
        ArgumentCaptor<HandlerInput> captor = ArgumentCaptor.forClass(HandlerInput.class);
        RequestHandler handler = mock(RequestHandler.class);
        when(handler.canHandle(captor.capture())).thenReturn(false);
        DefaultRequestMapper mapper = DefaultRequestMapper.builder()
                .addRequestHandlerChain(getHandlerChain(handler))
                .build();
        Optional<RequestHandlerChain> handlerOptional = mapper.getRequestHandlerChain(input);
        assertEquals(handlerOptional, Optional.empty());
        assertTrue(captor.getValue().getRequestEnvelope().getRequest() instanceof IntentRequest);
    }

    @Test
    public void no_handler_registered_for_event() {
        SkillEnabledRequest request = SkillEnabledRequest.builder().build();
        RequestEnvelope envelope = RequestEnvelope.<SkillEnabledRequest>builder().withRequest(request).build();
        HandlerInput input = HandlerInput.builder().withRequestEnvelope(envelope).build();
        ArgumentCaptor<HandlerInput> captor = ArgumentCaptor.forClass(HandlerInput.class);
        RequestHandler handler = mock(RequestHandler.class);
        when(handler.canHandle(captor.capture())).thenReturn(false);
        DefaultRequestMapper mapper = DefaultRequestMapper.builder()
                .addRequestHandlerChain(getHandlerChain(handler))
                .build();
        Optional<RequestHandlerChain> handlerOptional = mapper.getRequestHandlerChain(input);
        assertEquals(handlerOptional, Optional.empty());
        assertTrue(captor.getValue().getRequestEnvelope().getRequest() instanceof SkillEnabledRequest);
    }

    @Test
    public void handler_registered_for_intent() {
        IntentRequest request = IntentRequest.builder().withIntent(Intent.builder().withName("fooIntent").build()).withRequestId(UUID.randomUUID().toString()).build();
        RequestEnvelope envelope = RequestEnvelope.<IntentRequest>builder().withRequest(request).build();
        HandlerInput input = HandlerInput.builder().withRequestEnvelope(envelope).build();
        ArgumentCaptor<HandlerInput> captor = ArgumentCaptor.forClass(HandlerInput.class);
        RequestHandler handler = mock(RequestHandler.class);
        when(handler.canHandle(captor.capture())).thenReturn(true);
        DefaultRequestMapper mapper = DefaultRequestMapper.builder()
                .addRequestHandlerChain(getHandlerChain(handler))
                .build();
        Optional<RequestHandlerChain> handlerOptional = mapper.getRequestHandlerChain(input);
        assertEquals(handlerOptional.get().getRequestHandler(), handler);
        assertTrue(captor.getValue().getRequestEnvelope().getRequest() instanceof IntentRequest);
    }

    @Test
    public void handler_registered_for_event() {
        SkillEnabledRequest request = SkillEnabledRequest.builder().build();
        RequestEnvelope envelope = RequestEnvelope.<SkillEnabledRequest>builder().withRequest(request).build();
        HandlerInput input = HandlerInput.builder().withRequestEnvelope(envelope).build();
        ArgumentCaptor<HandlerInput> captor = ArgumentCaptor.forClass(HandlerInput.class);
        RequestHandler handler = mock(RequestHandler.class);
        when(handler.canHandle(captor.capture())).thenReturn(true);
        DefaultRequestMapper mapper = DefaultRequestMapper.builder()
                .addRequestHandlerChain(getHandlerChain(handler))
                .build();
        Optional<RequestHandlerChain> handlerOptional = mapper.getRequestHandlerChain(input);
        assertEquals(handlerOptional.get().getRequestHandler(), handler);
        assertTrue(captor.getValue().getRequestEnvelope().getRequest() instanceof SkillEnabledRequest);
    }

    @Test
    public void add_request_handler_chain() {
        RequestHandler handler = mock(RequestHandler.class);
        when(handler.canHandle(any())).thenReturn(true);
        DefaultRequestHandlerChain chain = getHandlerChain(handler);
        DefaultRequestMapper mapper = DefaultRequestMapper.builder()
                .addRequestHandlerChain(chain)
                .build();
        HandlerInput handlerInput = HandlerInput.builder()
                .withRequestEnvelope(RequestEnvelope.builder()
                        .withRequest(IntentRequest.builder()
                                .withIntent(Intent.builder()
                                        .withName("fooIntent").build())
                                .build())
                        .build())
                .build();
        Optional<RequestHandlerChain> result = mapper.getRequestHandlerChain(handlerInput);
        assertEquals(handler, result.get().getRequestHandler());
    }

    private DefaultRequestHandlerChain getHandlerChain(RequestHandler handler) {
        return DefaultRequestHandlerChain.builder()
                .withRequestHandler(handler)
                .build();
    }
    
}
