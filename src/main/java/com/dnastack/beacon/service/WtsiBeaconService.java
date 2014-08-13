/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnastack.beacon.service;

import com.dnastack.beacon.core.Beacon;
import com.dnastack.beacon.core.Query;
import com.dnastack.beacon.util.HttpUtils;
import com.dnastack.beacon.util.ParsingUtils;
import java.net.MalformedURLException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.http.client.methods.HttpGet;

/**
 * WTSI beacon service.
 *
 * @author Miroslav Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
@Named
@ApplicationScoped
public class WtsiBeaconService extends GenomeUnawareBeaconService {

    private static final String BASE_URL = "http://www.sanger.ac.uk/sanger/GA4GH_Beacon";
    private static final String PARAM_TEMPLATE = "?src=all&chr=%s&pos=%d&all=%s";

    @Inject
    private HttpUtils httpUtils;

    @Inject
    private ParsingUtils parsingUtils;

    private String getQueryUrl(String chrom, Long pos, String allele) throws MalformedURLException {
        String params = String.format(PARAM_TEMPLATE, chrom, pos, allele);

        return BASE_URL + params;
    }

    @Override
    public String getQueryResponse(Beacon beacon, Query query, String ref) {
        String response = null;

        HttpGet httpGet;
        try {
            httpGet = new HttpGet(getQueryUrl(query.getChromosome(), query.getPosition(), query.getAllele()));
        } catch (MalformedURLException ex) {
            return response;
        }

        return httpUtils.executeRequest(httpGet);
    }

    @Override
    public Boolean parseQueryResponse(String response) {
        return parsingUtils.parseYesNoCaseInsensitive(response);
    }

}
