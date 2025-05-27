package com.example.kitespringapp.icicibreeze.util;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import java.net.URI;

public class HttpPutWithEntity extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "PUT";

    public HttpPutWithEntity() {
        super();
    }

    public HttpPutWithEntity(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpPutWithEntity(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
} 