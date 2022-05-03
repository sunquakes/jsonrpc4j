package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcHttpServer;
import org.easymock.EasyMockRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(EasyMockRunner.class)
public class JsonRpcHttpServerTest {

    private JsonRpcHttpServer jsonRpcHttpServer;

    @Before
    public void setup() {
        jsonRpcHttpServer = new JsonRpcHttpServer();
    }

    @Test
    public void testMethod() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jsonRpcHttpServer.handleRequest(request, response);
        assertEquals(response.getStatus(), 400);

        request = new MockHttpServletRequest("POST", "");
        response = new MockHttpServletResponse();

        jsonRpcHttpServer.handleRequest(request, response);
        assertEquals(response.getStatus(), 200);
    }
}
