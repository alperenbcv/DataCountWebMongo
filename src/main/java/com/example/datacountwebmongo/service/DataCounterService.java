package com.example.datacountwebmongo.service;

import com.example.datacountwebmongo.dto.DataCounterGroupResult;
import com.example.datacountwebmongo.dto.DataCounterResult;
import com.example.datacountwebmongo.repository.JurisprudenceCounterRepository;
import com.example.datacountwebmongo.repository.YargitayRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCounterService {

    private final WebClient webClient;
    private final JurisprudenceCounterRepository repository;
    private final YargitayRepository yargitayRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ================== ANAYASA MAHKEMESİ ==================
    // Python: anayasa_data_counter.py

    public DataCounterGroupResult getAnayasaCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("anayasa", "bireyselbasvuru", "Bireysel Başvuru Kararları",
                fetchAnayasaWebCount("https://kararlarbilgibankasi.anayasa.gov.tr"),
                repository.countByDocTypeAndAcType("anayasa", "bireyselbasvuru")));

        results.add(createResult("anayasa", "norm", "Norm Denetimi Kararları",
                fetchAnayasaWebCount("https://normkararlarbilgibankasi.anayasa.gov.tr"),
                repository.countByDocTypeAndAcType("anayasa", "norm")));

        results.add(createResult("anayasa", "siyasiparti", "Siyasi Parti Kararları",
                fetchAnayasaWebCount("https://siyasipartikararlar.anayasa.gov.tr"),
                repository.countByDocTypeAndAcType("anayasa", "siyasiparti")));

        results.add(createResult("anayasa", "yasamaDokunulmazligi", "Yasama Dokunulmazlığı Kararları",
                40L, repository.countByDocTypeAndAcType("anayasa", "yasamaDokunulmazligi")));

        results.add(createResult("anayasa", "yucedivan", "Yüce Divan Kararları",
                15L, repository.countByDocTypeAndAcType("anayasa", "yucedivan")));

        return DataCounterGroupResult.of("anayasa", "Anayasa Mahkemesi", results);
    }

    private Long fetchAnayasaWebCount(String url) {
        try {
            String html = fetchHtml(url);
            if (html == null) return null;

            Document doc = Jsoup.parse(html);
            Element found = doc.selectFirst("div.bulunankararsayisi.col-xs-12.col-sm-3");
            if (found == null) return null;

            return extractNumber(found.text());
        } catch (Exception e) {
            log.error("Anayasa fetch error: {} - {}", url, e.getMessage());
            return null;
        }
    }

    // ================== BAM (Bölge Adliye Mahkemesi) ==================
    // Python: bam_data_counter.py

    public DataCounterGroupResult getBamCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchBamWebCount();
        results.add(createResult("regionalcourt", null, "BAM Emsal Kararları",
                webCount, repository.countByDocType("regionalcourt")));

        return DataCounterGroupResult.of("bam", "Bölge Adliye Mahkemesi (BAM)", results);
    }

    private Long fetchBamWebCount() {
        try {
            String html = fetchHtml("https://emsal.uyap.gov.tr/");
            if (html == null) return null;

            Document doc = Jsoup.parse(html);
            Element span = doc.selectFirst("span.timer.number-count[data-to]");
            if (span != null) {
                return Long.parseLong(span.attr("data-to"));
            }

            Pattern p = Pattern.compile("data-to=[\"'](\\d+)[\"']");
            Matcher m = p.matcher(html);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
            return null;
        } catch (Exception e) {
            log.error("BAM fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== BRSA (BDDK) ==================
    // Python: brsa_data_counter.py

    public DataCounterGroupResult getBrsaCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("brsa", "resmiGazete", "Resmi Gazetede Yayımlanan Kurul Kararları",
                fetchBrsaWebCount("https://www.bddk.org.tr/Mevzuat/Liste/55"),
                repository.countByDocTypeAndCategory("brsa", "Resmi Gazetede Yayımlanan Kurul Kararları")));

        results.add(createResult("brsa", "digerKararlar", "Resmi Gazetede Yayımlanmayan Kurul Kararları",
                fetchBrsaWebCount("https://www.bddk.org.tr/Mevzuat/Liste/56"),
                repository.countByDocTypeAndCategory("brsa", "Resmi Gazetede Yayımlanmayan Kurul Kararları")));

        return DataCounterGroupResult.of("brsa", "BDDK (Bankacılık Düzenleme ve Denetleme Kurumu)", results);
    }

    private Long fetchBrsaWebCount(String url) {
        try {
            String html = fetchHtml(url);
            if (html == null) return null;

            Document doc = Jsoup.parse(html);
            Elements links = doc.select("a.mevzuatBaslik.baslikContainer");
            return (long) links.size();
        } catch (Exception e) {
            log.error("BRSA fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== CA (Rekabet Kurumu) ==================
    // Python: ca_data_counter.py

    public DataCounterGroupResult getCaCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("ca", "kararlar", "Rekabet Kurulu Kararları",
                fetchCaWebCount("https://www.rekabet.gov.tr/tr/Kararlar?page=1"),
                repository.countByDocTypeAndSafahatIdNotExists("ca")));

        results.add(createResult("ca", "safahatlar", "Safahatlar",
                fetchCaWebCount("https://www.rekabet.gov.tr/tr/Safahatlar"),
                repository.countByDocTypeAndSafahatIdExists("ca")));

        return DataCounterGroupResult.of("ca", "Rekabet Kurumu", results);
    }

    private Long fetchCaWebCount(String url) {
        try {
            String html = fetchHtml(url);
            if (html == null) return null;

            Document doc = Jsoup.parse(html);
            Element div = doc.selectFirst("div.yazi01");
            if (div != null) {
                Pattern p = Pattern.compile("Toplam\\s*:\\s*(\\d+)");
                Matcher m = p.matcher(div.text());
                if (m.find()) {
                    return Long.parseLong(m.group(1));
                }
            }
            return null;
        } catch (Exception e) {
            log.error("CA fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== CBRT (TCMB PPK) ==================
    // Python: cbrt_data_counter.py

    public DataCounterGroupResult getCbrtCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchCbrtTotalWebCount();
        results.add(createResult("cbrt", null, "TCMB Para Politikası Kurulu Kararları",
                webCount, repository.countByDocType("cbrt")));

        return DataCounterGroupResult.of("cbrt", "Türkiye Cumhuriyet Merkez Bankası (TCMB)", results);
    }

    private Long fetchCbrtTotalWebCount() {
        long total = 0;
        String baseUrl = "https://www.tcmb.gov.tr/wps/wcm/connect/TR/TCMB+TR/Main+Menu/Temel+Faaliyetler/Para+Politikasi/PPK/";

        for (int year = 2006; year <= 2014; year++) {
            try {
                String html = fetchHtml(baseUrl + year);
                if (html == null) continue;
                Document doc = Jsoup.parse(html);
                total += doc.select("div.block-collection-box").size();
            } catch (Exception e) {
                log.warn("CBRT {} error: {}", year, e.getMessage());
            }
        }

        for (int year = 2015; year <= 2025; year++) {
            try {
                String html = fetchHtml(baseUrl + year);
                if (html == null) continue;
                Document doc = Jsoup.parse(html);

                Elements links = doc.select("a[href*=/duyurular/basin/]");
                int count = links.size();

                if (count == 0) {
                    Element table = doc.selectFirst("table");
                    if (table != null) {
                        count = Math.max(0, table.select("tr").size() - 1);
                    }
                }
                total += count;
            } catch (Exception e) {
                log.warn("CBRT {} error: {}", year, e.getMessage());
            }
        }

        return total;
    }

    // ================== DANIŞTAY ==================
    // Python: danistay_data_counter.py

    public DataCounterGroupResult getDanistayCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchDanistayWebCount();
        results.add(createResult("danistay", null, "Danıştay Kararları",
                webCount, repository.countByDocType("danistay")));

        return DataCounterGroupResult.of("danistay", "Danıştay", results);
    }

    private Long fetchDanistayWebCount() {
        try {
            String html = fetchHtml("https://karararama.danistay.gov.tr/");
            if (html == null) return null;

            Document doc = Jsoup.parse(html);
            Element span = doc.selectFirst("span#istatistik span");
            if (span != null) {
                return extractNumber(span.text());
            }
            return null;
        } catch (Exception e) {
            log.error("Danıştay fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== UYUŞMAZLIK MAHKEMESİ ==================
    // Python: dispute_data_counter.py

    public DataCounterGroupResult getDisputeCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("dispute", null, "Uyuşmazlık Mahkemesi Kararları",
                null, repository.countByDocType("dispute")));

        return DataCounterGroupResult.of("dispute", "Uyuşmazlık Mahkemesi", results);
    }

    // ================== ECHR (AİHM) ==================
    // Python: echr_data_counter.py

    public DataCounterGroupResult getEchrCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("echr", "all", "ECHR Tüm Kararlar",
                fetchEchrWebCount(null), repository.countByDocType("echr")));

        results.add(createResult("echr", "turkish", "ECHR Türkçe Kararlar",
                fetchEchrWebCount("TUR"), null));

        return DataCounterGroupResult.of("echr", "Avrupa İnsan Hakları Mahkemesi (AİHM)", results);
    }

    private Long fetchEchrWebCount(String languageCode) {
        try {
            final String query = "contentsitename:ECHR AND (NOT (doctype=PR OR doctype=HFCOMOLD OR doctype=HECOMOLD))"
                    + (languageCode != null ? " AND (languageisocode=" + languageCode + ")" : "");

            String uri = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("hudoc.echr.coe.int")
                    .path("/app/query/results")
                    .queryParam("query", query)
                    .queryParam("select", "itemid")
                    .queryParam("start", "0")
                    .queryParam("length", "1")
                    .queryParam("rankingModelId", "11111111-0000-0000-0000-000000000000")
                    .toUriString();

            String response = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode json = objectMapper.readTree(response);
                return json.path("resultcount").asLong();
            }
            return null;
        } catch (Exception e) {
            log.error("ECHR fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== EMRA (EPDK) ==================
    // Python: emra_data_counter.py

    public DataCounterGroupResult getEmraCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("emra", null, "EPDK Kurul Kararları",
                null, repository.countByDocType("emra")));

        return DataCounterGroupResult.of("emra", "Enerji Piyasası Düzenleme Kurumu (EPDK)", results);
    }

    // ================== GİB (Gelir İdaresi) ==================
    // Python: gib_data_counter.py

    public DataCounterGroupResult getGibCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchGibWebCount();
        results.add(createResult("gib", null, "GİB Mevzuat",
                webCount, repository.countByDocType("gib")));

        return DataCounterGroupResult.of("gib", "Gelir İdaresi Başkanlığı (GİB)", results);
    }

    private Long fetchGibWebCount() {
        try {
            String payload = "{\"ktype\":99,\"kanunIds\":[],\"status\":2,\"deleted\":false}";

            String response = webClient.post()
                    .uri("https://gib.gov.tr/api/gibportal/mevzuat/mevzuatSearchAll?page=0&sortFieldName=id&sortType=ASC")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Referer", "https://gib.gov.tr/mevzuat/arama")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode json = objectMapper.readTree(response);
                JsonNode rc = json.path("resultContainer");
                return rc.path("SIRKULER").asLong() +
                        rc.path("OZELGE").asLong() +
                        rc.path("GEREKCE").asLong() +
                        rc.path("ICGENELGE").asLong() +
                        rc.path("GENELYAZILAR").asLong();
            }
            return null;
        } catch (Exception e) {
            log.error("GİB fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== IPPRSA (SEDDK) ==================
    // Python: ipprsa_data_counter.py

    public DataCounterGroupResult getIpprsaCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchIpprsaWebCount();
        results.add(createResult("ipprsa", null, "SEDDK Kurul Kararları",
                webCount, repository.countByDocType("ipprsa")));

        return DataCounterGroupResult.of("ipprsa", "SEDDK", results);
    }

    private Long fetchIpprsaWebCount() {
        try {
            String html = fetchHtml("https://www.seddk.gov.tr/tr/mevzuat/kurul-kararlari");
            if (html == null) return null;

            Document doc = Jsoup.parse(html);
            long count = 0;
            Elements tables = doc.select("table.table");
            for (Element table : tables) {
                for (Element row : table.select("tr")) {
                    if (row.selectFirst("th") == null && row.selectFirst("a") != null) {
                        count++;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            log.error("IPPRSA fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== KAP ==================
    // Python: kapd_data_counter.py

    public DataCounterGroupResult getKapdCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("kapd", null, "KAP Bildirimleri",
                null, repository.countByDocType("kapd")));

        return DataCounterGroupResult.of("kapd", "Kamuyu Aydınlatma Platformu (KAP)", results);
    }

    // ================== KİK (Kamu İhale Kurumu) ==================
    // Python: kik_data_counter.py

    public DataCounterGroupResult getKikCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("kik", "uk", "Uyuşmazlık Kararları",
                calculateKikUkCount(), repository.countByDocTypeAndAcType("kik", "uk")));

        results.add(createResult("kik", "mk", "Makbuz Kararları",
                calculateKikMkCount(), repository.countByDocTypeAndAcType("kik", "mk")));

        results.add(createResult("kik", "dk", "Düzenleyici Kararlar",
                114L, repository.countByDocTypeAndAcType("kik", "dk")));

        return DataCounterGroupResult.of("kik", "Kamu İhale Kurumu (KİK)", results);
    }

    private Long calculateKikUkCount() {
        long count = 237;
        for (int year = 2007; year <= 2026; year++) count += 500;
        return count;
    }

    private Long calculateKikMkCount() {
        long count = 144 + 142 + 167 + 208 + 276 + 397 + 395 - 210 - 160 - 144 - 250 - 367 - 265;
        for (int year = 2014; year <= 2026; year++) count += 500;
        return count;
    }

    // ================== OMI (Ombudsman) ==================
    // Python: omi_data_counter.py

    public DataCounterGroupResult getOmiCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchOmiWebCount();
        results.add(createResult("omi", null, "Ombudsman Kararları",
                webCount, repository.countByDocType("omi")));

        return DataCounterGroupResult.of("omi", "Kamu Denetçiliği Kurumu (Ombudsman)", results);
    }

    private Long fetchOmiWebCount() {
        try {
            String response = webClient.post()
                    .uri("https://kararlar.ombudsman.gov.tr/Arama/IndexPaging")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("draw=1&start=0&length=1&search[value]=&search[regex]=false")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode json = objectMapper.readTree(response);
                return json.path("recordsTotal").asLong();
            }
            return null;
        } catch (Exception e) {
            log.error("OMI fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== RTÜK ==================
    // Python: rthc_data_counter.py

    public DataCounterGroupResult getRtukCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchRtukWebCount();
        results.add(createResult("rthc", null, "RTÜK Üst Kurul Kararları",
                webCount, repository.countByDocType("rthc")));

        return DataCounterGroupResult.of("rthc", "Radyo ve Televizyon Üst Kurulu (RTÜK)", results);
    }

    private Long fetchRtukWebCount() {
        try {
            String baseUrl = "https://www.rtuk.gov.tr/ust-kurul-kararlari";

            String html = fetchHtml(baseUrl + "?page=1");
            if (html == null) return null;

            Document doc = Jsoup.parse(html);
            Element lastPageLink = doc.selectFirst("li.PagedList-skipToLast a");
            if (lastPageLink == null) return null;

            String href = lastPageLink.attr("href");
            int lastPageNum = Integer.parseInt(href.split("page=")[1]);

            Elements kararDivs = doc.select("div.row[style*=border]");
            int perPage = kararDivs.size();

            String lastHtml = fetchHtml(baseUrl + "?page=" + lastPageNum);
            if (lastHtml == null) return null;

            Document lastDoc = Jsoup.parse(lastHtml);
            int lastPageCount = lastDoc.select("div.row[style*=border]").size();

            return (long) ((lastPageNum - 1) * perPage + lastPageCount);
        } catch (Exception e) {
            log.error("RTÜK fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== SAYIŞTAY ==================
    // Python: sayistay_data_counter.py

    public DataCounterGroupResult getSayistayCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("sayistay", "daire", "Sayıştay Daire Kararları",
                null, repository.countByDocTypeAndAcType("sayistay", "daire")));

        results.add(createResult("sayistay", "temyiz", "Sayıştay Temyiz Kararları",
                null, repository.countByDocTypeAndAcType("sayistay", "temyiz")));

        return DataCounterGroupResult.of("sayistay", "Sayıştay", results);
    }

    // ================== YSK (SEB) ==================
    // Python: seb_data_counter.py

    public DataCounterGroupResult getSebCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("seb", "kararlar", "YSK Kararları",
                null, repository.countByDocTypeAndCategory("seb", "YSK Kararları")));

        results.add(createResult("seb", "ilke", "İlke Kararları",
                fetchYskJsonCount("https://www.ysk.gov.tr/doc/ilkeKarar/ilkeKararlar.txt"),
                repository.countByDocTypeAndCategory("seb", "İlke Kararları")));

        results.add(createResult("seb", "genelge", "Genelgeler",
                fetchYskJsonCount("https://www.ysk.gov.tr/doc/genelge/genelgeler.txt"),
                repository.countByDocTypeAndCategory("seb", "Genelgeler")));

        return DataCounterGroupResult.of("seb", "Yüksek Seçim Kurulu (YSK)", results);
    }

    private Long fetchYskJsonCount(String url) {
        try {
            String response = fetchHtml(url);
            if (response != null) {
                JsonNode json = objectMapper.readTree(response);
                return (long) json.size();
            }
            return null;
        } catch (Exception e) {
            log.error("YSK fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== SPK ==================
    // Python: spk_data_counter.py

    public DataCounterGroupResult getSpkCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchSpkWebCount();
        results.add(createResult("spk", null, "SPK Bültenleri",
                webCount, repository.countByDocType("spk")));

        return DataCounterGroupResult.of("spk", "Sermaye Piyasası Kurulu (SPK)", results);
    }

    private Long fetchSpkWebCount() {
        long total = 0;

        for (int year = 2004; year <= 2022; year++) {
            try {
                String html = fetchHtml("https://spk.gov.tr/spk-bultenleri/gecmis-yillara-ait-bultenler/" + year + "-yili-spk-bultenleri");
                if (html == null) continue;
                Document doc = Jsoup.parse(html);
                total += doc.select("a[href*=.pdf]").size();
            } catch (Exception e) {
                log.warn("SPK {} error: {}", year, e.getMessage());
            }
        }

        for (int year = 2023; year <= 2025; year++) {
            try {
                String html = fetchHtml("https://spk.gov.tr/spk-bultenleri/gecmis-yillara-ait-bultenler/" + year + "-yili-spk-bultenleri");
                if (html == null) continue;
                Document doc = Jsoup.parse(html);
                total += doc.select("div.liste-item").size();
            } catch (Exception e) {
                log.warn("SPK {} error: {}", year, e.getMessage());
            }
        }

        return total;
    }

    // ================== TİHEK ==================
    // Python: tihre_data_counter.py

    public DataCounterGroupResult getTihreCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("tihre", null, "TİHEK Kararları",
                null, repository.countByDocType("tihre")));

        return DataCounterGroupResult.of("tihre", "Türkiye İnsan Hakları ve Eşitlik Kurumu (TİHEK)", results);
    }

    // ================== TBB (UTBADB) ==================
    // Python: utbadb_data_counter.py

    public DataCounterGroupResult getUtbadbCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchUtbadbWebCount();
        results.add(createResult("utbadb", null, "TBB Disiplin Kararları",
                webCount, repository.countByDocType("utbadb")));

        return DataCounterGroupResult.of("utbadb", "Türkiye Barolar Birliği (TBB)", results);
    }

    private Long fetchUtbadbWebCount() {
        long total = 0;
        Pattern pattern = Pattern.compile("(\\d+)\\s*adet");

        for (int year = 2005; year <= 2026; year++) {
            try {
                String html = fetchHtml("https://www.barobirlik.org.tr/DisiplinKararlari?yil=" + year + "&arama=");
                if (html == null) continue;

                Document doc = Jsoup.parse(html);
                Element span = doc.selectFirst("span[style*=color:#ff0000]");
                if (span != null) {
                    Matcher m = pattern.matcher(span.text());
                    if (m.find()) {
                        total += Long.parseLong(m.group(1));
                    }
                }
            } catch (Exception e) {
                log.warn("TBB {} error: {}", year, e.getMessage());
            }
        }

        return total;
    }

    // ================== YARGITAY ==================
    // Python: yargitay_data_counter.py

    public DataCounterGroupResult getYargitayCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        Long webCount = fetchYargitayWebCount();
        Long dbCount = yargitayRepository.count();

        results.add(createResult("yargitay", null, "Yargıtay Kararları",
                webCount, dbCount));

        return DataCounterGroupResult.of("yargitay", "Yargıtay", results);
    }

    private Long fetchYargitayWebCount() {
        try {
            String html = fetchHtml("https://karararama.yargitay.gov.tr/");
            if (html == null) return null;

            Pattern p = Pattern.compile("data-to=[\"'](\\d+)[\"']");
            Matcher m = p.matcher(html);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
            return null;
        } catch (Exception e) {
            log.error("Yargıtay fetch error: {}", e.getMessage());
            return null;
        }
    }

    // ================== YÖK TEZ ==================
    // Python: yoktez_data_counter.py

    public DataCounterGroupResult getYoktezCounts() {
        List<DataCounterResult> results = new ArrayList<>();

        results.add(createResult("yoktez", null, "YÖK Tez Merkezi",
                null, repository.countByDocType("yoktez")));

        return DataCounterGroupResult.of("yoktez", "YÖK Ulusal Tez Merkezi", results);
    }

    // ================== TÜM VERİLER ==================

    public List<DataCounterGroupResult> getAllCounts() {
        List<DataCounterGroupResult> allResults = new ArrayList<>();

        allResults.add(getAnayasaCounts());
        allResults.add(getBamCounts());
        allResults.add(getBrsaCounts());
        allResults.add(getCaCounts());
        allResults.add(getCbrtCounts());
        allResults.add(getDanistayCounts());
        allResults.add(getDisputeCounts());
        allResults.add(getEchrCounts());
        allResults.add(getEmraCounts());
        allResults.add(getGibCounts());
        allResults.add(getIpprsaCounts());
        allResults.add(getKapdCounts());
        allResults.add(getKikCounts());
        allResults.add(getOmiCounts());
        allResults.add(getRtukCounts());
        allResults.add(getSayistayCounts());
        allResults.add(getSebCounts());
        allResults.add(getSpkCounts());
        allResults.add(getTihreCounts());
        allResults.add(getUtbadbCounts());
        allResults.add(getYargitayCounts());
        allResults.add(getYoktezCounts());

        return allResults;
    }

    // ================== HELPER METODLAR ==================

    private String fetchHtml(String url) {
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("HTML fetch error: {} - {}", url, e.getMessage());
            return null;
        }
    }

    private Long extractNumber(String text) {
        if (text == null) return null;
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(text.replace(".", "").replace(",", ""));
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        return null;
    }

    private DataCounterResult createResult(String docType, String subType, String description,
                                           Long webCount, Long dbCount) {
        return DataCounterResult.of(docType, subType, description, webCount, dbCount);
    }
}
