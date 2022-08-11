package org.lx.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class HttpBannerTool {

    static ThreadLocal<List<String>> keyWords = ThreadLocal.withInitial(new Supplier<List<String>>() {

        @Override
        public List<String> get() {
            return Arrays.asList("赌博", "赌场", "娱乐&棋牌", "炸金花&提现", "威尼斯人", "体育&投注", "在线发牌", "新葡京", "真人&电游", "博彩", "真人视讯",
                    "真人百家乐");
        }

    });

    public static boolean contailsKey(String text) {
        return AnalysisPage.compareKeyWords(text, keyWords.get()) != null;
    }

    public static HttpBanner getBanner(File file) throws IOException {
        HttpBanner banner = new HttpBanner();
        String text = "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = null;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (firstLine) {
                    banner.setUrl(line);
                    firstLine = false;
                    continue;
                }

                if (line.equals("<--***###html###***-->")) {
                    banner.setHeaders(text);
                    text = "";
                    continue;
                }
                if (line.equals("<--***###endHtml###***-->")) {
                    banner.setHtml(text);
                    text = "";
                    continue;
                }
                if (line.equals("<--***###cert###***-->")) {
                    continue;
                }

                if (line.equals("<--***###endCert###***-->")) {
                    banner.setCert(text);
                    text = "";
                    continue;
                }
                text = text + line + "\n";

            }
        }
        return banner;
    }

    public static HttpBanner getBanner(String fileText) throws IOException {
        HttpBanner banner = new HttpBanner();
        String text = "";
        String[] ss = fileText.split("\n");
        boolean firstLine = true;
        for (String line : ss) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (firstLine) {
                banner.setUrl(line);
                firstLine = false;
                continue;
            }

            if (line.equals("<--***###html###***-->")) {
                banner.setHeaders(text);
                text = "";
                continue;
            }
            if (line.equals("<--***###endHtml###***-->")) {
                banner.setHtml(text);
                text = "";
                continue;
            }
            if (line.equals("<--***###cert###***-->")) {
                continue;
            }

            if (line.equals("<--***###endCert###***-->")) {
                banner.setCert(text);
                text = "";
                continue;
            }
            text = text + line + "\n";

        }
        return banner;
    }

    public static HtmlBasicInfo getHtmlBasicInfo(HttpBanner banner) {
        if (banner.getHtml() == null || banner.getHtml().isEmpty()) {
            return null;
        }

        try {
            HtmlBasicInfo pageInfo = new HtmlBasicInfo();
            Document document = Jsoup.parse(banner.getHtml());
            Elements titleElements = document.getElementsByTag("title");
            List<String> titlesList = titleElements.eachText();
            if (!titlesList.isEmpty()) {
                pageInfo.setTitle(titlesList.get(0));
            }
            Element keywordEle = document.select("meta[name=Keywords]").first();
            if (keywordEle != null) {
                pageInfo.setKeywords(keywordEle.attr("content"));
            }

            Element descEle = document.select("meta[name=Description]").first();
            if (descEle != null) {
                pageInfo.setDescription(descEle.attr("content"));
            }
            if (pageInfo.getDescription() == null) {
                pageInfo.setDescription("");
            }
            if (pageInfo.getTitle() == null) {
                pageInfo.setTitle("");
            }
            if (pageInfo.getKeywords() == null) {
                pageInfo.setKeywords("");
            }
            return pageInfo;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getBannerCertDomain(HttpBanner banner) {
        String cert = banner.getCert();
        if (cert != null && cert.length() > 0) {
            String[] certs = cert.split("\n");

            for (String ce : certs) {
                if (ce.startsWith("Subject:")) {
                    ce = ce.replace("Subject:", "");
                    String[] ss = ce.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
                    String domain = null;

                    for (String s : ss) {
                        s = s.trim();
                        if (s.startsWith("CN=")) {
                            domain = s.replace("CN=", "").trim();
                            return domain;
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }
//	private static void print(String msg, Object... args) {
//		System.out.println(String.format(msg, args));
//	}
//
//	private static String trim(String s, int width) {
//		if (s.length() > width)
//			return s.substring(0, width - 1) + ".";
//		else
//			return s;
//	}

}
