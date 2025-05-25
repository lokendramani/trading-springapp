package com.example.kitespringapp.icicibreeze.util;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import java.net.URI;

public class HttpDeleteWithEntity extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";

    public HttpDeleteWithEntity() {
        super();
    }

    public HttpDeleteWithEntity(final URI uri) {
        super();
        setURI(uri);
    }

    public HttpDeleteWithEntity(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
} 