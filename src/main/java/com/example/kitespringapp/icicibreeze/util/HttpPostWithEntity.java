package com.example.kitespringapp.icicibreeze.util;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import java.net.URI;

public class HttpPostWithEntity extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "POST";

    public HttpPostWithEntity() {
        super();
    }

    public HttpPostWithEntity(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpPostWithEntity(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
} 