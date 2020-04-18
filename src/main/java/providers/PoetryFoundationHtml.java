package providers;

import net.bellew.Poem;
import net.bellew.PoetryDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PoetryFoundationHtml
{
    public void find(PoetryDatabase db) throws IOException
    {
        List<String> urls = findNewPoems(db);
        for (String url : urls)
        {
            try
            {
                Poem p = readPoem(url);
                if (null!=p)
                    db.addPoem(p, false);
            }
            catch (IOException x)
            {
                x.printStackTrace();
            }
        }
    }

    List<String> findNewPoems(PoetryDatabase db) throws IOException
    {
        Document doc = Jsoup.connect("https://www.poetryfoundation.org/poems/poem-of-the-day").get();
        Elements poemLinks = doc.select("DIV.c-feature H2 A");
        List<String> list = poemLinks.stream()
                .map(a -> a.attr("href"))
                .filter(s ->
                {
                    return !db.poemExists(idFromUrl(s));
                })
                .collect(Collectors.toList());
        for (var s : list)
            System.out.println(s);
        return list;
    }

    String idFromUrl(String s)
    {
        if (s.contains("?"))
            s = s.substring(0,s.indexOf("?"));
        return "pf_" + s.substring(s.lastIndexOf('/')+1);
    }

    Poem readPoem(String url) throws IOException
    {
        Document doc = Jsoup.connect(url).get();
        Element e = doc.selectFirst("DIV.o-article-bd");
        if (null == e)
            return null;
        String id = idFromUrl(url);
        String author = e.selectFirst("SPAN.c-txt_attribution").text();
        String title  = e.selectFirst("DIV.c-feature-hd").text();
        Element body   = e.selectFirst("DIV.o-poem");
        StringBuilder text = new StringBuilder();
        body.select("DIV").stream()
                .map(Element::text)
                .forEach(line -> text.append(line).append("\n"));
        return new Poem(id,url,title,author,text.toString());
    }
}
