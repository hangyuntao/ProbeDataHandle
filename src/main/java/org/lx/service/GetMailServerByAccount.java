package org.lx.service;


import com.alibaba.fastjson.JSON;
import org.xbill.DNS.*;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;

//通过邮件地址获取邮件服务器地址
public class GetMailServerByAccount {

    private static final List<String> dnsList = Arrays.asList("1.1.1.1","114.114.114.114", "8.8.8.8");


    public static List<String> resolver(String domain, String dnsServer) {
        List<String> ips = new ArrayList<>();
        try {
            SimpleResolver resolver = new SimpleResolver(dnsServer);
//            System.out.println(dnsServer);
            resolver.setTimeout(Duration.ofSeconds(5));

            Lookup lookup = new Lookup(domain, Type.A);
            lookup.setResolver(resolver);
            lookup.setCache(null);
            lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL) {
//                System.out.println("ERROR: " + lookup.getErrorString());
                return ips;
            }

            Record[] answers = lookup.getAnswers();
            for (Record rec : answers) {
//                System.out.println(rec.rdataToString());
                ips.add(rec.rdataToString());
            }

        } catch (UnknownHostException e) {
//			e.printStackTrace();
        } catch (TextParseException e) {
//			e.printStackTrace();
        }
        return ips;
    }

    public static List<String> resolver(String domain) {
        Set<String> ips = new HashSet<>();
        for (String dns : dnsList) {
            ips.addAll(resolver(domain, dns));
        }
        return new ArrayList<>(ips);
    }

    public static List<String> resolverMx(String domain) {
        List<String> list = new ArrayList<>();
        try {
            SimpleResolver resolver = new SimpleResolver(dnsList.get(0));
            resolver.setTimeout(Duration.ofSeconds(5));
            Lookup lookup = new Lookup(domain, Type.MX);
            lookup.setResolver(resolver);
            lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL) {
//                System.out.println("ERROR: " + lookup.getErrorString());
                return list;
            }

            Record[] answers = lookup.getAnswers();
            for (Record rec : answers) {
//                System.out.println(rec.rdataToString());
                list.add(rec.rdataToString());
            }

        } catch (UnknownHostException e) {
//			e.printStackTrace();
        } catch (TextParseException e) {
//			e.printStackTrace();
        }
        return list;
    }


    //pop、smtp、imap
    public static void main(String[] args) {
//        String mailAccount = "inquire@cqa.guam.gov";
//
//        String domain = mailAccount.substring(mailAccount.indexOf("@") + 1);
//        System.out.println(domain);
//
//        String popDomain = "pop." + domain;
//        String smtpDomain = "smtp." + domain;
//        String imapDomain = "imap." + domain;
//
//        System.out.println(popDomain);
//        System.out.println(JSON.toJSONString(resolver(popDomain)));
//        System.out.println("*********************");
//        System.out.println(smtpDomain);
//        System.out.println(JSON.toJSONString(resolver(smtpDomain)));
//        System.out.println("*********************");
//        System.out.println(imapDomain);
//        System.out.println(JSON.toJSONString(resolver(imapDomain)));
//        System.out.println("*********************");
//        System.out.println("MX");
//        System.out.println(JSON.toJSONString(resolverMx(domain)));

        System.out.println(JSON.toJSONString(resolver("guamshark.com")));
    }


}
