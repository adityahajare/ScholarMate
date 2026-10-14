package com.scholarmate.config;

import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.repository.ScholarshipSourceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ScholarshipSourceInitializer {

    @Bean
    CommandLineRunner initializeScholarshipSources(
            ScholarshipSourceRepository repository) {

        return args -> {
                List<SourceSeed> seeds = List.of(
                    new SourceSeed("National Scholarship Portal (NSP)", "https://scholarships.gov.in", "GOVERNMENT", "NSP", "DAILY"),
                    new SourceSeed("AICTE Fellowship & Scholarships", "https://www.aicte-india.org/schemes", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Ministry of Education Scholarships", "https://www.education.gov.in/scholarships", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Ministry of Social Justice and Empowerment", "https://socialjustice.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Department of Science and Technology (DST)", "https://dst.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("University Grants Commission (UGC)", "https://www.ugc.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Prime Minister's Research Fellowship (PMRF)", "https://pmrf.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Indian Council of Cultural Relations (ICCR)", "https://a2ascholarships.iccr.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Ministry of Minority Affairs (MOMA)", "https://minorityaffairs.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Ministry of Tribal Affairs", "https://tribal.nic.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Ministry of Corporate Affairs (MCA)", "https://www.mca.gov.in", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("Ministry of Home Affairs - WARB / PMSS", "https://www.mha.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("KVPY / IISc Archives", "https://www.iisc.ac.in", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("Department of Biotechnology (DBT)", "https://dbtindia.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("CSIR Human Resource Development Group", "https://csirhrdg.res.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Indian Council of Medical Research (ICMR)", "https://www.icmr.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Board of Apprenticeship Training (BOAT)", "https://boat-srp.com", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("National Institutes of Health (NIH)", "https://www.nih.gov", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("ISRO Space Science Grants", "https://www.isro.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Defence Research and Development Organisation (DRDO)", "https://www.drdo.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),

                    new SourceSeed("MahaDBT (Maharashtra)", "https://mahadbt.maharashtra.gov.in", "STATE", "MAHADBT", "DAILY"),
                    new SourceSeed("UP Scholarship Portal", "https://scholarship.up.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("State Scholarship Portal - SSP (Karnataka)", "https://ssp.postmatric.karnataka.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("OASIS Portal (West Bengal)", "https://oasis.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("SVMCM Portal (West Bengal)", "https://svmcm.wbhed.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("MP Scholarship Portal / MPTAAS", "https://scholarshipportal.mp.nic.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("State Scholarship Portal (Odisha)", "https://scholarship.odisha.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Digital Gujarat", "https://www.digitalgujarat.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Bihar Post Matric Portal", "https://pmsonline.bihar.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Telangana ePASS", "https://telanganaepass.cgg.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Andhra Pradesh Jnanabhumi", "https://jnanabhumi.ap.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Har-Chhatravratti (Haryana)", "https://harchhatravratti.highereduhry.ac.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Dr. Ambedkar Portal (Punjab)", "https://scholarships.punjab.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("HP e-Pass", "https://hpepass.cgg.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("DCE Scholarship Portal (Kerala)", "https://dcescholarship.kerala.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Tamil Nadu e-District Scholarship Portal", "https://escholarship.tn.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Jharkhand e-Kalyan Portal", "https://ekalyan.cgg.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Rajasthan Social Justice SJMS", "https://sje.rajasthan.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Chhattisgarh e-Kalyan Portal", "https://postmatric-scholarship.cg.nic.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Uttarakhand Scholarship Portal", "https://scholarship.uk.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Assam State Scholarship Portal", "https://directorateofhighereducation.assam.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Goa Directorate of Higher Education", "https://dhe.goa.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Delhi E-District Portal", "https://edistrict.delhigovt.nic.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Tripura Scholarship Portal", "https://highereducation.tripura.gov.in", "STATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Jammu & Kashmir Higher Education Portal", "https://jkhighereducation.nic.in", "STATE", "GENERIC", "WEEKLY"),

                    new SourceSeed("Buddy4Study", "https://www.buddy4study.com", "PRIVATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Vidyasaarathi", "https://www.vidyasaarathi.co.in", "PRIVATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("ScholarsBox", "https://scholarsbox.in", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Vidyaasaadhan", "https://vidyaasaadhan.org", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Sitaram Jindal Foundation", "https://www.sitaramjindalfoundation.org", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Tata Trusts Educational Grants", "https://www.tatatrusts.org", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Azim Premji Foundation", "https://azimpremjifoundation.org", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Aditya Birla Scholarships", "https://www.adityabirlascholars.net", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("L&T Build-India", "https://www.lntecc.com", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Glow & Lovely Careers", "https://www.glowandlovelycareers.in", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("HDFC Bank Parivartan", "https://www.hdfcbank.com/personal/about-us/csr", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("LIC Golden Jubilee Foundation", "https://licindia.in", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Keep India Smiling (Colgate-Palmolive)", "https://www.colgate.com/en-in/bright-smiles-bright-futures", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("L'Oreal India For Young Women in Science", "https://www.loreal.com/en/india/", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("WeMakeScholars", "https://www.wemakescholars.com", "PRIVATE", "GENERIC", "WEEKLY"),
                    new SourceSeed("Infosys Foundation Grants", "https://www.infosys.com/infosys-foundation", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Cognizant Foundation India", "https://www.cognizant.com/in/en/about-cognizant/corporate-citizenship", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Jindal Foundation Education Portals", "https://jsplfoundation.com", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Aga Khan Foundation India", "https://akdn.org/our-agencies/aga-khan-foundation", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Inlaks Shivdasani Foundation", "https://www.inlaksfoundation.org", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Mahindra Scholarships / KC Mahindra Education Trust", "https://www.kcmet.org", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Narotam Sekhsaria Foundation", "https://www.narotam.com", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Fair & Lovely Foundation", "https://www.glowandlovelycareers.in", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("DLF Foundation Education Program", "https://www.dlffoundation.in", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("JK Lakshmi Vidya Scholarship", "https://www.jklakshmi.com", "PRIVATE", "GENERIC", "MONTHLY"),

                    new SourceSeed("Chevening Scholarships", "https://www.chevening.org", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Shiksha Study Abroad", "https://www.shiksha.com/studyabroad", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("USIEF / Fulbright-Nehru Fellowships", "https://www.usief.org.in", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("British Council India", "https://www.britishcouncil.in", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("DAAD India", "https://www.daad.in", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Campus France India", "https://www.inde.campusfrance.org", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Rhodes Trust (Oxford)", "https://www.rhodeshouse.ox.ac.uk", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("J.N. Tata Endowment", "https://jntataendowment.org", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("Erasmus+ Programme", "https://erasmus-plus.ec.europa.eu", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Scholarshipportal.com", "https://www.scholarshipportal.com", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Commonwealth Scholarship Commission", "https://cscuk.fcdo.gov.uk", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("IDP Education India Abroad", "https://www.idp.com/india", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Australia Awards", "https://www.dfat.gov.au/people-to-people/australia-awards", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("MEXT Scholarship Portal (Japan)", "https://www.studyinjapan.go.jp/en/", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Chegg Scholarships", "https://www.chegg.com/scholarships", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Fastweb Scholarships", "https://www.fastweb.com", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Scholarships.com", "https://www.scholarships.com", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Fulbright Foreign Student Program", "https://foreign.fulbrightonline.org", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Gates Cambridge Scholarships", "https://www.gatescambridge.org", "INTERNATIONAL", "GENERIC", "MONTHLY"),
                    new SourceSeed("Clarendon Fund (Oxford)", "https://www.ox.ac.uk/clarendon", "INTERNATIONAL", "GENERIC", "MONTHLY"),

                    new SourceSeed("Science and Engineering Research Board (SERB)", "https://serb.gov.in", "GOVERNMENT", "GENERIC", "WEEKLY"),
                    new SourceSeed("Lady Tata Memorial Trust", "https://www.ladytatatrust.org", "PRIVATE", "GENERIC", "MONTHLY"),
                    new SourceSeed("ONGC India Sports Scholarships", "https://ongcindia.com", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("Airports Authority of India (AAI) Sports", "https://www.aai.aero", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("Indian Council of Historical Research (ICHR)", "https://ichr.ac.in", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("Indian Council of Social Science Research (ICSSR)", "https://www.icssr.org", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("Ministry of Earth Sciences (MoES)", "https://moes.gov.in", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("Bhabha Atomic Research Centre (BARC)", "https://www.barc.gov.in", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("National Mineral Development Corporation (NMDC)", "https://www.nmdc.co.in", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("NTPC Utkarsh Scholarships", "https://www.ntpc.co.in", "GOVERNMENT", "GENERIC", "MONTHLY"),

                    new SourceSeed("IIT Bombay Scholarships", "https://www.iitb.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IIT Delhi Scholarships", "https://home.iitd.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IIT Madras Scholarships", "https://www.iitm.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IIT Kanpur Scholarships", "https://www.iitk.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IIT Kharagpur Scholarships", "https://www.iitkgp.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IIT Roorkee Scholarships", "https://www.iitr.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IIT Guwahati Scholarships", "https://www.iitg.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IIT Hyderabad Scholarships", "https://www.iith.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IIT Indore Scholarships", "https://www.iiti.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IIT Gandhinagar Scholarships", "https://www.iitgn.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("NIT Tiruchirappalli Scholarships", "https://www.nitt.edu", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("NIT Warangal Scholarships", "https://www.nitw.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("NIT Rourkela Scholarships", "https://www.nitrkl.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("NIT Calicut Scholarships", "https://nitc.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("NIT Karnataka Scholarships", "https://www.nitk.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("Jawaharlal Nehru University Scholarships", "https://www.jnu.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("University of Delhi Scholarships", "https://www.du.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("Banaras Hindu University Scholarships", "https://www.bhu.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("Jamia Millia Islamia Scholarships", "https://www.jmi.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("Aligarh Muslim University Scholarships", "https://www.amu.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("Anna University Scholarships", "https://www.annauniv.edu", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("University of Hyderabad Scholarships", "https://uohyd.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("Tata Institute of Social Sciences Scholarships", "https://www.tiss.edu", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IGNOU Scholarships and Financial Aid", "https://www.ignou.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("Indian Council of Agricultural Research Scholarships", "https://icar.org.in", "GOVERNMENT", "GENERIC", "MONTHLY"),
                    new SourceSeed("ICAR Indian Agricultural Research Institute Fellowships", "https://www.iari.res.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IISER Pune Scholarships", "https://www.iiserpune.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("IISER Kolkata Scholarships", "https://www.iiserkol.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("National Institute of Fashion Technology Scholarships", "https://www.nift.ac.in", "INSTITUTION", "GENERIC", "MONTHLY"),
                    new SourceSeed("National Institute of Design Scholarships", "https://www.nid.edu", "INSTITUTION", "GENERIC", "MONTHLY")
                );

            for (SourceSeed seed : seeds) {
                addSource(
                        repository,
                        seed.name,
                        seed.baseUrl,
                        seed.sourceType,
                        "HTML",
                        seed.scraperKey,
                        seed.crawlFrequency
                );
            }

            repository.findByName("National Scholarship Portal (NSP)")
                    .ifPresent(nspSource -> repository.findByName("National Scholarship Portal")
                            .filter(legacySource -> legacySource.getBaseUrl().equalsIgnoreCase(nspSource.getBaseUrl()))
                            .ifPresent(legacySource -> {
                                legacySource.setActive(false);
                                legacySource.setScrapingAllowed(false);
                                repository.save(legacySource);
                            }));
        };
    }

    private void addSource(
            ScholarshipSourceRepository repository,
            String name,
            String baseUrl,
            String sourceType,
            String scraperType,
            String scraperKey,
            String crawlFrequency) {

        ScholarshipSource source = repository.findByName(name).orElse(new ScholarshipSource());

        source.setName(name);
        source.setBaseUrl(baseUrl);
        source.setSourceType(sourceType);
        source.setScraperType(scraperType);
        source.setScraperKey(scraperKey);
        source.setScrapingAllowed(true);
        source.setActive(true);
        source.setCrawlFrequency(crawlFrequency);

        repository.save(source);
        System.out.println("Scholarship source registered/updated: " + name);
    }

    private static final class SourceSeed {
        private final String name;
        private final String baseUrl;
        private final String sourceType;
        private final String scraperKey;
        private final String crawlFrequency;

        private SourceSeed(String name, String baseUrl, String sourceType, String scraperKey, String crawlFrequency) {
            this.name = name;
            this.baseUrl = baseUrl;
            this.sourceType = sourceType;
            this.scraperKey = scraperKey;
            this.crawlFrequency = crawlFrequency;
        }
    }
}