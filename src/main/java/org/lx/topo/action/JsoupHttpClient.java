package org.lx.topo.action;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.List;

/**
 * @author yuntao
 * @date 2022/8/2
 */
public class JsoupHttpClient {
    
    private static class innerWebClient{
        private static final WebClient webClient = new WebClient();
    }
    
    /**
     * 获取指定网页实体
     * @param url
     * @return
     */
    public static HtmlPage getHtmlPage(String url){
        //调用此方法时加载WebClient
        WebClient webClient = innerWebClient.webClient;
        /*// 取消 JS 支持
        webClient.setJavaScriptEnabled(false);
        // 取消 CSS 支持
        webClient.setCssEnabled(false);*/
        
        HtmlPage page=null;
        try{
            // 获取指定网页实体
            page = webClient.getPage(url);
        } catch (IOException e){
            e.printStackTrace();
        }
        return page;
    }
    
    
    public static void main(String[] args) throws Exception {
        // 获取指定网页实体
        HtmlPage page = getHtmlPage("https://www.baidu.com/");
        //System.out.println(page.asText());  //asText()是以文本格式显示
        System.out.println(page.asXml());   //asXml()是以xml格式显示
        // 获取搜索输入框
        HtmlInput input = page.getHtmlElementById("kw");
        // 往输入框 “填值”
        input.setValueAttribute("绿林寻猫");
        // 获取搜索按钮
        HtmlInput btn = page.getHtmlElementById("su");
        // “点击” 搜索
        HtmlPage page2 = btn.click();
        // 选择元素
        List<HtmlElement> spanList=page2.getByXPath("//h3[@class='t']/a");
        for(int i=0;i<spanList.size();i++) {
            // 输出新页面的文本
            System.out.println(i+1+"、"+spanList.get(i));
        }
    }
    
}
