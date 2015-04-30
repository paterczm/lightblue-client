package com.redhat.lightblue.client.http.transport;

import com.redhat.lightblue.client.LightblueClientConfiguration;
import com.redhat.lightblue.client.http.auth.ApacheHttpClients;
import com.redhat.lightblue.client.request.LightblueRequest;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class LightblueApacheHttpClient implements HttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(LightblueApacheHttpClient.class);

    private final LightblueClientConfiguration config;

    public LightblueApacheHttpClient(LightblueClientConfiguration config) {
        // Defensive copy because mutability...
        this.config = new LightblueClientConfiguration(config);
    }

    @Override
    public String executeRequest(LightblueRequest request, String baseUri) throws IOException {
        HttpUriRequest httpOperation = makeHttpUriRequest(request, baseUri);

        LOGGER.debug("Calling " + httpOperation);

        try (CloseableHttpClient httpClient = getClient()) {
            httpOperation.setHeader("Content-Type", "application/json");

            if (LOGGER.isDebugEnabled()) {
                try {
                    if (httpOperation instanceof HttpEntityEnclosingRequest) {
                        LOGGER.debug("Request body: " + request.getBody());
                    }
                } catch (ClassCastException e) {
                    LOGGER.debug("Request body: None");
                }
            }

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpOperation)) {
                HttpEntity entity = httpResponse.getEntity();
                return EntityUtils.toString(entity);
            }
        }
    }

    private HttpUriRequest makeHttpUriRequest(LightblueRequest request, String baseUri) throws UnsupportedEncodingException {
        String uri = request.getRestURI(baseUri);
        HttpUriRequest httpRequest = null;

        switch (request.getHttpMethod()) {
            case GET:
                httpRequest = new HttpGet(uri);
                break;
            case POST:
                httpRequest = new HttpPost(uri);
                break;
            case PUT:
                httpRequest = new HttpPost(uri);
                break;
            case DELETE:
                httpRequest = new HttpDelete(uri);
                break;
        }

        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = new StringEntity(request.getBody(), Consts.UTF_8);
            ((HttpEntityEnclosingRequest) httpRequest).setEntity(entity);
        }

        return httpRequest;
    }

    private CloseableHttpClient getClient() {
        try {
            return ApacheHttpClients.fromLightblueClientConfiguration(config);
        } catch (GeneralSecurityException | IOException e) {
            LOGGER.error("Error creating HTTP client: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        // Doesn't need to be closed to keep compatibility with legacy behavior.
    }
}