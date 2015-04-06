/*
 * The MIT License
 *
 * Copyright 2015 DNAstack.
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
package com.dnastack.bob.service.util;

import com.dnastack.bob.persistence.api.BeaconDao;
import com.dnastack.bob.persistence.api.OrganizationDao;
import com.dnastack.bob.persistence.entity.Beacon;
import com.dnastack.bob.persistence.entity.Organization;
import com.dnastack.bob.persistence.enumerated.Reference;
import com.dnastack.bob.service.parser.impl.JsonCafeBeaconResponseParser;
import com.dnastack.bob.service.parser.impl.JsonExistsBeaconResponseParser;
import com.dnastack.bob.service.parser.impl.JsonExistsGtBeaconResponseParser;
import com.dnastack.bob.service.parser.impl.JsonResponseExistsBeaconResponseParser;
import com.dnastack.bob.service.parser.impl.JsonResponseExistsNullAsFalseBeaconResponseParser;
import com.dnastack.bob.service.parser.impl.StringFoundBeaconResponseParser;
import com.dnastack.bob.service.parser.impl.StringYesNoBeaconResponseParser;
import com.dnastack.bob.service.parser.impl.StringYesNoRefBeaconResponseParser;
import com.dnastack.bob.service.processor.impl.AmpLabBeaconProcessor;
import com.dnastack.bob.service.processor.impl.BeaconizerIntegerChromosomeBeaconProcessor;
import com.dnastack.bob.service.processor.impl.BeaconizerStringChromosomeBeaconProcessor;
import com.dnastack.bob.service.processor.impl.BroadInstituteBeaconProcessor;
import com.dnastack.bob.service.processor.impl.CafeVariomeBeaconProcessor;
import com.dnastack.bob.service.processor.impl.EbiBeaconProcessor;
import com.dnastack.bob.service.processor.impl.IcgcBeaconProcessor;
import com.dnastack.bob.service.processor.impl.KaviarBeaconProcessor;
import com.dnastack.bob.service.processor.impl.NcbiBeaconProcessor;
import com.dnastack.bob.service.processor.impl.UcscBeaconProcessor;
import com.dnastack.bob.service.processor.impl.UcscV2BeaconProcessor;
import com.dnastack.bob.service.processor.impl.WtsiBeaconProcessor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.jboss.logging.Logger;

/**
 *
 * @author Miroslav Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
@Singleton
@Startup
@Transactional
public class DatabaseInitializer {

    @Inject
    private OrganizationDao organizationDao;

    @Inject
    private BeaconDao beaconDao;

    @Inject
    private CdiBeanResolver resolver;

    @Inject
    private Logger logger;

    // TODO: remove this methods as it is dangerous and does tnot handle foreign keys well
    private void clean() {
        logger.debug("Cleaning DB...");
        List<Beacon> beacons = new ArrayList<>();
        do {
            beacons = beaconDao.findAll();
            for (Beacon b : beacons) {
                try {
                    beaconDao.delete(b.getId());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } while (!beacons.isEmpty());

        List<Organization> orgs = new ArrayList<>();
        do {
            orgs = organizationDao.findAll();
            for (Organization b : orgs) {
                try {
                    organizationDao.delete(b.getId());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } while (!orgs.isEmpty());
    }

    private void insertInitialData() {
        logger.debug("Initializing DB...");
        try {
            // set up regular beacons
            Organization ucsc = new Organization();
            ucsc.setId("ucsc");
            ucsc.setName("UCSC");
            organizationDao.save(ucsc);
            Beacon ucscBeacon = new Beacon();
            ucscBeacon.setId("ucsc");
            ucscBeacon.setName("UCSC");
            ucscBeacon.setOrganization(ucsc);
            ucscBeacon.setVisible(true);
            ucscBeacon.setProcessor(null);
            ucscBeacon.setEnabled(true);
            ucscBeacon.setAggregator(true);
            ucscBeacon.setUrl("http://hgwdev-max.cse.ucsc.edu/cgi-bin/beacon/query");
            ucscBeacon.setSupportedReferences(EnumSet.noneOf(Reference.class));
            ucscBeacon.setParser(null);
            beaconDao.save(ucscBeacon);
            Beacon clinvarBeacon = new Beacon();
            clinvarBeacon.setId("clinvar");
            clinvarBeacon.setName("ClinVar");
            clinvarBeacon.setOrganization(ucsc);
            clinvarBeacon.setVisible(true);
            clinvarBeacon.setAggregator(false);
            clinvarBeacon.setProcessor(resolver.getClassId(UcscBeaconProcessor.class));
            clinvarBeacon.setEnabled(true);
            clinvarBeacon.setUrl("http://hgwdev-max.cse.ucsc.edu/cgi-bin/beacon/query");
            clinvarBeacon.setSupportedReferences(EnumSet.of(Reference.HG19));
            clinvarBeacon.setParser(resolver.getClassId(StringYesNoBeaconResponseParser.class));
            beaconDao.save(clinvarBeacon);
            Beacon uniprotBeacon = new Beacon();
            uniprotBeacon.setId("uniprot");
            uniprotBeacon.setName("UniProt");
            uniprotBeacon.setOrganization(ucsc);
            uniprotBeacon.setVisible(true);
            uniprotBeacon.setAggregator(false);
            uniprotBeacon.setProcessor(resolver.getClassId(UcscBeaconProcessor.class));
            uniprotBeacon.setEnabled(true);
            uniprotBeacon.setUrl("http://hgwdev-max.cse.ucsc.edu/cgi-bin/beacon/query");
            uniprotBeacon.setSupportedReferences(EnumSet.of(Reference.HG19));
            uniprotBeacon.setParser(resolver.getClassId(StringYesNoBeaconResponseParser.class));
            beaconDao.save(uniprotBeacon);
            Beacon lovdBeacon = new Beacon();
            lovdBeacon.setId("lovd");
            lovdBeacon.setName("Leiden Open Variation");
            lovdBeacon.setOrganization(ucsc);
            lovdBeacon.setVisible(true);
            lovdBeacon.setAggregator(false);
            lovdBeacon.setProcessor(resolver.getClassId(UcscV2BeaconProcessor.class));
            lovdBeacon.setEnabled(true);
            lovdBeacon.setUrl("http://genome.ucsc.edu/cgi-bin/hgBeacon/query");
            lovdBeacon.setSupportedReferences(EnumSet.of(Reference.HG19));
            lovdBeacon.setParser(resolver.getClassId(JsonResponseExistsBeaconResponseParser.class));
            beaconDao.save(lovdBeacon);
            Beacon hgmdBeacon = new Beacon();
            hgmdBeacon.setId("hgmd");
            hgmdBeacon.setName("Biobase - HGMD");
            hgmdBeacon.setOrganization(ucsc);
            hgmdBeacon.setVisible(true);
            hgmdBeacon.setAggregator(false);
            hgmdBeacon.setProcessor(resolver.getClassId(UcscV2BeaconProcessor.class));
            hgmdBeacon.setEnabled(true);
            hgmdBeacon.setUrl("http://genome.ucsc.edu/cgi-bin/hgBeacon/query");
            hgmdBeacon.setDescription("HGMD gives out only positions and ignores alleles.");
            hgmdBeacon.setSupportedReferences(EnumSet.of(Reference.HG19));
            hgmdBeacon.setParser(resolver.getClassId(JsonResponseExistsBeaconResponseParser.class));
            beaconDao.save(hgmdBeacon);
            beaconDao.addRelationship(hgmdBeacon, ucscBeacon);
            beaconDao.addRelationship(clinvarBeacon, ucscBeacon);
            beaconDao.addRelationship(lovdBeacon, ucscBeacon);
            beaconDao.addRelationship(uniprotBeacon, ucscBeacon);

            Organization ebi = new Organization();
            ebi.setId("ebi");
            ebi.setName("EBI");
            organizationDao.save(ebi);
            Beacon ebiBeacon = new Beacon();
            ebiBeacon.setId("ebi");
            ebiBeacon.setName("EMBL-EBI");
            ebiBeacon.setOrganization(ebi);
            ebiBeacon.setVisible(true);
            ebiBeacon.setAggregator(false);
            ebiBeacon.setProcessor(resolver.getClassId(EbiBeaconProcessor.class));
            ebiBeacon.setEnabled(true);
            ebiBeacon.setUrl("http://wwwdev.ebi.ac.uk/eva/webservices/rest/v1/ga4gh/beacon");
            ebiBeacon.setSupportedReferences(EnumSet.of(Reference.HG19));
            ebiBeacon.setParser(resolver.getClassId(JsonExistsBeaconResponseParser.class));
            beaconDao.save(ebiBeacon);

            Organization ncbi = new Organization();
            ncbi.setId("ncbi");
            ncbi.setName("NCBI");
            organizationDao.save(ncbi);
            Beacon ncbiBeacon = new Beacon();
            ncbiBeacon.setId("ncbi");
            ncbiBeacon.setName("NCBI");
            ncbiBeacon.setOrganization(ncbi);
            ncbiBeacon.setVisible(true);
            ncbiBeacon.setAggregator(false);
            ncbiBeacon.setProcessor(resolver.getClassId(NcbiBeaconProcessor.class));
            ncbiBeacon.setEnabled(true);
            ncbiBeacon.setUrl("http://www.ncbi.nlm.nih.gov/projects/genome/beacon/beacon.cgi");
            ncbiBeacon.setSupportedReferences(EnumSet.of(Reference.HG18, Reference.HG19, Reference.HG38));
            ncbiBeacon.setParser(resolver.getClassId(JsonExistsGtBeaconResponseParser.class));
            beaconDao.save(ncbiBeacon);

            Organization wtsi = new Organization();
            wtsi.setId("wtsi");
            wtsi.setName("WTSI");
            organizationDao.save(wtsi);
            Beacon wtsiBeacon = new Beacon();
            wtsiBeacon.setId("wtsi");
            wtsiBeacon.setName("Wellcome Trust Sanger Institute");
            wtsiBeacon.setOrganization(wtsi);
            wtsiBeacon.setVisible(true);
            wtsiBeacon.setAggregator(false);
            wtsiBeacon.setProcessor(resolver.getClassId(WtsiBeaconProcessor.class));
            wtsiBeacon.setEnabled(true);
            wtsiBeacon.setUrl("http://www.sanger.ac.uk/sanger/GA4GH_Beacon");
            wtsiBeacon.setSupportedReferences(EnumSet.of(Reference.HG19));
            wtsiBeacon.setParser(resolver.getClassId(StringYesNoRefBeaconResponseParser.class));
            beaconDao.save(wtsiBeacon);

            Organization amplab = new Organization();
            amplab.setId("amplab");
            amplab.setName("AMPLab, University of California");
            organizationDao.save(amplab);
            Beacon amplabBeacon = new Beacon();
            amplabBeacon.setId("amplab");
            amplabBeacon.setName("AMPLab");
            amplabBeacon.setOrganization(amplab);
            amplabBeacon.setVisible(true);
            amplabBeacon.setAggregator(false);
            amplabBeacon.setProcessor(resolver.getClassId(AmpLabBeaconProcessor.class));
            amplabBeacon.setEnabled(true);
            amplabBeacon.setUrl("http://beacon.eecs.berkeley.edu/beacon.php");
            amplabBeacon.setSupportedReferences(EnumSet.of(Reference.HG18, Reference.HG19, Reference.HG38));
            amplabBeacon.setParser(resolver.getClassId(StringFoundBeaconResponseParser.class));
            beaconDao.save(amplabBeacon);

            Organization isb = new Organization();
            isb.setId("isb");
            isb.setName("Institute for Systems Biology");
            organizationDao.save(isb);
            Beacon kaviar = new Beacon();
            kaviar.setId("kaviar");
            kaviar.setName("Known VARiants");
            kaviar.setOrganization(isb);
            kaviar.setVisible(true);
            kaviar.setAggregator(false);
            kaviar.setProcessor(resolver.getClassId(KaviarBeaconProcessor.class));
            kaviar.setEnabled(true);
            kaviar.setUrl("http://db.systemsbiology.net/kaviar/cgi-pub/beacon");
            kaviar.setSupportedReferences(EnumSet.of(Reference.HG19, Reference.HG18));
            kaviar.setParser(resolver.getClassId(StringYesNoBeaconResponseParser.class));
            beaconDao.save(kaviar);

            Organization google = new Organization();
            google.setId("google");
            google.setName("Google");
            organizationDao.save(google);
            Beacon platinum = new Beacon();
            platinum.setId("platinum");
            platinum.setName("Illumina Platinum Genomes");
            platinum.setOrganization(google);
            platinum.setVisible(true);
            platinum.setAggregator(false);
            platinum.setProcessor(resolver.getClassId(BeaconizerStringChromosomeBeaconProcessor.class));
            platinum.setEnabled(true);
            platinum.setUrl("http://dnastack.com/p/beacon/");
            platinum.setSupportedReferences(EnumSet.of(Reference.HG19));
            platinum.setParser(resolver.getClassId(JsonExistsBeaconResponseParser.class));
            beaconDao.save(platinum);
            Beacon thousandGenomes = new Beacon();
            thousandGenomes.setId("thousandgenomes");
            thousandGenomes.setName("1000 Genomes Project");
            thousandGenomes.setOrganization(google);
            thousandGenomes.setVisible(true);
            thousandGenomes.setAggregator(false);
            thousandGenomes.setProcessor(resolver.getClassId(BeaconizerIntegerChromosomeBeaconProcessor.class));
            thousandGenomes.setEnabled(true);
            thousandGenomes.setUrl("http://dnastack.com/p/beacon/");
            thousandGenomes.setSupportedReferences(EnumSet.of(Reference.HG19));
            thousandGenomes.setParser(resolver.getClassId(JsonExistsBeaconResponseParser.class));
            beaconDao.save(thousandGenomes);
            Beacon thousandGenomesPhase3 = new Beacon();
            thousandGenomesPhase3.setId("thousandgenomes-phase3");
            thousandGenomesPhase3.setName("1000 Genomes Project - Phase 3");
            thousandGenomesPhase3.setOrganization(google);
            thousandGenomesPhase3.setVisible(true);
            thousandGenomesPhase3.setAggregator(false);
            thousandGenomesPhase3.setProcessor(resolver.getClassId(BeaconizerIntegerChromosomeBeaconProcessor.class));
            thousandGenomesPhase3.setEnabled(true);
            thousandGenomesPhase3.setUrl("http://dnastack.com/p/beacon/");
            thousandGenomesPhase3.setSupportedReferences(EnumSet.of(Reference.HG19));
            thousandGenomesPhase3.setParser(resolver.getClassId(JsonExistsBeaconResponseParser.class));
            beaconDao.save(thousandGenomesPhase3);

            Organization curoverse = new Organization();
            curoverse.setId("curoverse");
            curoverse.setName("Curoverse");
            organizationDao.save(curoverse);
            Beacon curoverseBeacon = new Beacon();
            curoverseBeacon.setId("curoverse");
            curoverseBeacon.setName("PGP");
            curoverseBeacon.setOrganization(curoverse);
            curoverseBeacon.setVisible(true);
            curoverseBeacon.setAggregator(false);
            curoverseBeacon.setProcessor(resolver.getClassId(BeaconizerIntegerChromosomeBeaconProcessor.class));
            curoverseBeacon.setEnabled(true);
            curoverseBeacon.setUrl(null);
            curoverseBeacon.setSupportedReferences(EnumSet.of(Reference.HG19));
            curoverseBeacon.setParser(resolver.getClassId(JsonExistsBeaconResponseParser.class));
            beaconDao.save(curoverseBeacon);
            Beacon curoverseRefBeacon = new Beacon();
            curoverseRefBeacon.setId("curoverse-ref");
            curoverseRefBeacon.setName("GA4GH Example Data");
            curoverseRefBeacon.setOrganization(curoverse);
            curoverseRefBeacon.setVisible(true);
            curoverseRefBeacon.setAggregator(false);
            curoverseRefBeacon.setProcessor(resolver.getClassId(BeaconizerIntegerChromosomeBeaconProcessor.class));
            curoverseRefBeacon.setEnabled(true);
            curoverseRefBeacon.setUrl("http://dnastack.com/p/beacon/");
            curoverseRefBeacon.setSupportedReferences(EnumSet.of(Reference.HG19));
            curoverseRefBeacon.setParser(resolver.getClassId(JsonExistsBeaconResponseParser.class));
            beaconDao.save(curoverseRefBeacon);

            Organization leicester = new Organization();
            leicester.setId("leicester");
            leicester.setName("University of Leicester");
            organizationDao.save(leicester);
            Beacon cafeVariomeCentral = new Beacon();
            cafeVariomeCentral.setId("cafe-central");
            cafeVariomeCentral.setName("Cafe Variome Central");
            cafeVariomeCentral.setOrganization(leicester);
            cafeVariomeCentral.setVisible(true);
            cafeVariomeCentral.setAggregator(false);
            cafeVariomeCentral.setProcessor(resolver.getClassId(CafeVariomeBeaconProcessor.class));
            cafeVariomeCentral.setEnabled(true);
            cafeVariomeCentral.setUrl("http://beacon.cafevariome.org/query");
            cafeVariomeCentral.setSupportedReferences(EnumSet.of(Reference.HG19));
            cafeVariomeCentral.setParser(resolver.getClassId(JsonCafeBeaconResponseParser.class));
            beaconDao.save(cafeVariomeCentral);
            Beacon cafeCardioKit = new Beacon();
            cafeCardioKit.setId("cafe-cardiokit");
            cafeCardioKit.setName("Cafe CardioKit");
            cafeCardioKit.setOrganization(leicester);
            cafeCardioKit.setVisible(true);
            cafeCardioKit.setAggregator(false);
            cafeCardioKit.setProcessor(resolver.getClassId(CafeVariomeBeaconProcessor.class));
            cafeCardioKit.setEnabled(true);
            cafeCardioKit.setUrl("http://beacon.cafevariome.org/query");
            cafeCardioKit.setSupportedReferences(EnumSet.of(Reference.HG19));
            cafeCardioKit.setParser(resolver.getClassId(JsonCafeBeaconResponseParser.class));
            beaconDao.save(cafeCardioKit);

            Organization broadInstitute = new Organization();
            broadInstitute.setId("broad");
            broadInstitute.setName("Broad Institute");
            organizationDao.save(broadInstitute);
            Beacon broad = new Beacon();
            broad.setId("broad");
            broad.setName("Broad Institute");
            broad.setOrganization(broadInstitute);
            broad.setVisible(true);
            broad.setAggregator(false);
            broad.setProcessor(resolver.getClassId(BroadInstituteBeaconProcessor.class));
            broad.setEnabled(true);
            broad.setUrl("http://broad-beacon.broadinstitute.org:8090/dev/beacon/query");
            broad.setSupportedReferences(EnumSet.of(Reference.HG19));
            broad.setParser(resolver.getClassId(StringYesNoBeaconResponseParser.class));
            beaconDao.save(broad);

            Organization icgc = new Organization();
            icgc.setId("icgc");
            icgc.setName("Ontario Institute for Cancer Research");
            organizationDao.save(icgc);
            Beacon icgcBeacon = new Beacon();
            icgcBeacon.setId("icgc");
            icgcBeacon.setName("ICGC");
            icgcBeacon.setOrganization(icgc);
            icgcBeacon.setVisible(true);
            icgcBeacon.setAggregator(false);
            icgcBeacon.setProcessor(resolver.getClassId(IcgcBeaconProcessor.class));
            icgcBeacon.setEnabled(true);
            icgcBeacon.setUrl("https://dcc.icgc.org/api/v1/beacon/query");
            icgcBeacon.setSupportedReferences(EnumSet.of(Reference.HG19));
            icgcBeacon.setParser(resolver.getClassId(JsonResponseExistsNullAsFalseBeaconResponseParser.class));
            beaconDao.save(icgcBeacon);

            Beacon googleBeacon = new Beacon();
            googleBeacon.setId("google");
            googleBeacon.setName("Google Genomics Public Data");
            googleBeacon.setOrganization(google);
            googleBeacon.setVisible(true);
            googleBeacon.setAggregator(true);
            googleBeacon.setProcessor(null);
            googleBeacon.setEnabled(true);
            googleBeacon.setUrl("http://dnastack.com/p/beacon/");
            googleBeacon.setSupportedReferences(EnumSet.noneOf(Reference.class));
            googleBeacon.setParser(null);

            beaconDao.save(googleBeacon);
            beaconDao.addRelationship(platinum, googleBeacon);
            beaconDao.update(platinum);
            beaconDao.addRelationship(thousandGenomes, googleBeacon);
            beaconDao.update(thousandGenomes);
            beaconDao.addRelationship(thousandGenomesPhase3, googleBeacon);
            beaconDao.update(thousandGenomesPhase3);

            Beacon cafeVariome = new Beacon();
            cafeVariome.setId("cafe-variome");
            cafeVariome.setName("Cafe Variome");
            cafeVariome.setOrganization(leicester);
            cafeVariome.setVisible(true);
            cafeVariome.setAggregator(true);
            cafeVariome.setProcessor(null);
            cafeVariome.setEnabled(true);
            cafeVariome.setUrl("http://beacon.cafevariome.org/query");
            cafeVariome.setSupportedReferences(EnumSet.noneOf(Reference.class));
            cafeVariome.setParser(null);
            beaconDao.save(cafeVariome);
            beaconDao.addRelationship(cafeVariomeCentral, cafeVariome);
            beaconDao.update(cafeVariomeCentral);
            beaconDao.addRelationship(cafeCardioKit, cafeVariome);
            beaconDao.update(cafeCardioKit);

            // set up bob
            Organization ga4gh = new Organization();
            ga4gh.setId("ga4gh");
            ga4gh.setName("Global Alliance for Genomics and Health");
            organizationDao.save(ga4gh);
            Beacon bob = new Beacon();
            bob.setId("bob");
            bob.setName("Beacon of Beacons");
            bob.setOrganization(ga4gh);
            bob.setVisible(true);
            bob.setAggregator(true);
            bob.setProcessor(null);
            bob.setEnabled(true);
            bob.setUrl("http://beacon-dnastack.rhcloud.com/");
            bob.setSupportedReferences(EnumSet.noneOf(Reference.class));
            bob.setParser(null);
            beaconDao.save(bob);

            // point all regular beacons to bob
            List<Beacon> beacons = beaconDao.findAll();
            for (Beacon b : beacons) {
                if (b.getProcessor() != null) {
                    beaconDao.addRelationship(b, bob);
                    beaconDao.update(b);
                }
            }
        } catch (Exception ex) {
            // failed to initialize, continue with an empty DB
            ex.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        clean();
        insertInitialData();
    }
}
