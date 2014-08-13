package com.dnastack.beacon.rest;

import com.dnastack.beacon.core.Beacon;
import com.dnastack.beacon.core.BeaconProvider;
import com.dnastack.beacon.core.BeaconResponse;
import com.dnastack.beacon.core.Query;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * Query rest resource.
 *
 * @author mcupak
 */
@Path("/query")
public class BeaconResource {

    @Inject
    private BeaconProvider beaconProvider;

    private boolean isQueryValid(Query q) {
        return q != null && q.getChromosome() != null && q.getPosition() != null && q.getAllele() != null;
    }

    @GET
    @Produces("application/json")
    @Path("/bob")
    public BeaconResponse queryAll(@QueryParam("chrom") String chrom, @QueryParam("pos") Long pos, @QueryParam("allele") String allele) {
        Query q = new Query(chrom, pos, allele);
        BeaconResponse br = new BeaconResponse(new Beacon("all", "beacon of beacons"), q, null);

        if (!isQueryValid(q)) {
            return br;
        }
        br.setResponse(false);

        for (Beacon b : beaconProvider.getBeacons()) {
            Boolean res = beaconProvider.getService(b).executeQuery(b, q).getResponse();
            if (res != null && res) {
                br.setResponse(true);
            }
        }

        return br;
    }

    @GET
    @Produces("application/json")
    @Path("/{beaconId}")
    public BeaconResponse queryBeacon(@PathParam("beaconId") String beaconId, @QueryParam("chrom") String chrom, @QueryParam("pos") Long pos, @QueryParam("allele") String allele) {
        Query q = new Query(chrom, pos, allele);

        Beacon b = beaconProvider.getBeacon(beaconId);
        if (b == null) {
            // nonexisting beaconId param specified
            return new BeaconResponse(new Beacon(null, "invalid beacon"), q, null);
        }

        return beaconProvider.getService(b).executeQuery(b, q);
    }
}
