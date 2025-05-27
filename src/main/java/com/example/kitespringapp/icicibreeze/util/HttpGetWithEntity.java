package com.example.kitespringapp.icicibreeze.util;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import java.net.URI;

public class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "GET";

    public HttpGetWithEntity() {
        super();
    }

    public HttpGetWithEntity(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpGetWithEntity(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
} 