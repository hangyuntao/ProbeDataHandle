package org.lx.http;

import java.util.Collection;
import java.util.Iterator;

public class AnalysisPage {

//	public static boolean compare(String url, String html, BlackLinks sits, KeyWords keyWords) {
//		return (sits != null && sits.contails(url)) || (keyWords != null && keyWords.textContains(html));
//	}

	public static String compareLink(String url, Collection<String> links) {
		Iterator<String> iterator = links.iterator();
		while (iterator.hasNext()) {
			String link = iterator.next();
			String p = link.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
			if (url.matches(p)) {
				return link;
			}
		}
		return null;
	}

	public static String compareKeyWords(String html, Collection<String> keyWords) {
		Iterator<String> iterator = keyWords.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();

			String[] ss = key.split("&");
			boolean compare = true;
			for (String s : ss) {
				if (!html.contains(s)) {
					compare = false;
				}
			}
			if (compare) {
				return key;
			}
//			if (html.contains(key)) {
//				return key;
//			}
		}

		return null;
	}

	public static String compareBasicKeyWord(HttpBanner banner, Collection<String> keyWords) {

		HtmlBasicInfo basicInfo = HttpBannerTool.getHtmlBasicInfo(banner);
		if (basicInfo != null) {
			String basicStr = basicInfo.getTitle()+basicInfo.getKeywords()+basicInfo.getDescription();
			Iterator<String> iterator = keyWords.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();

				String[] ss = key.split("&");
				boolean compare = true;
				for (String s : ss) {
					if (!basicStr.contains(s)) {
						compare = false;
					}
				}
				if (compare) {
					return key;
				}
//				if (html.contains(key)) {
//					return key;
//				}
			}
		}

		return null;
	}
}
