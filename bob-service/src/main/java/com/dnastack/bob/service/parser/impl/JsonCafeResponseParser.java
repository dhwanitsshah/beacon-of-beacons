/*
 * The MIT License
 *
 * Copyright 2014 Miroslav Cupak (mirocupak@gmail.com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dnastack.bob.service.parser.impl;

import com.dnastack.bob.persistence.entity.Beacon;
import com.dnastack.bob.service.parser.api.ResponseParser;
import com.dnastack.bob.service.parser.util.ParseUtils;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import static com.dnastack.bob.service.util.Constants.REQUEST_TIMEOUT;

/**
 * Parses cafe-prefixed responses.
 *
 * @author Miroslav Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
@Stateless
@Named
@Dependent
@Local(ResponseParser.class)
public class JsonCafeResponseParser implements ResponseParser, Serializable {

    private static final long serialVersionUID = 6472531100065834529L;
    private static final String BEACON_PREFIX = "cafe-";
    private static final String RESPONSE_FIELD = "response";
    @Inject
    private ParseUtils parseUtils;

    private String getJsonFieldName(Beacon b) {
        String res;
        if (b.getId().startsWith(BEACON_PREFIX)) {
            res = b.getId().substring(BEACON_PREFIX.length()) + "_" + RESPONSE_FIELD;
        } else {
            res = b.getId() + "_" + RESPONSE_FIELD;
        }
        return res;
    }

    @Asynchronous
    @Override
    public Future<Boolean> parseQueryResponse(Beacon b, Future<String> response) {
        Boolean res = null;
        try {
            res = parseUtils.parseBooleanFromJson(response.get(REQUEST_TIMEOUT, TimeUnit.SECONDS), RESPONSE_FIELD, getJsonFieldName(b));
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            // ignore
        }

        return new AsyncResult<>(res);
    }
}
