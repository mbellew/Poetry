package net.bellew.providers;

import net.bellew.Poem;
import net.bellew.PoetryDatabase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PoetsOrg
{
    public void find(PoetryDatabase db) throws IOException
    {
        List<String> urls = findNewPoems(db);

        for (String url : urls)
        {
            try
            {
                db.addPoem(readPoem(url), false);
            }
            catch (IOException x)
            {
                x.printStackTrace();
            }
        }
    }


    String jsonResponse = "{\"rows\":[" +
            "{\"field_poem_of_the_day_date\":\"04\\/18\\/2020\",\"view_node\":\"\\/poem\\/two-0\",\"title\":\"Two\",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/helen-hunt-jackson\\u0022 hreflang=\\u0022und\\u0022\\u003EHelen Hunt Jackson\\u003C\\/a\\u003E\"}" +
            ",{\"field_poem_of_the_day_date\":\"04\\/17\\/2020\",\"view_node\":\"\\/poem\\/waiting-number\",\"title\":\"Waiting for a Number\",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/peter-balakian\\u0022 hreflang=\\u0022und\\u0022\\u003EPeter Balakian\\u003C\\/a\\u003E\"},{\"field_poem_of_the_day_date\":\"04\\/16\\/2020\",\"view_node\":\"\\/poem\\/now-hes-etching\",\"title\":\"Now He\\u2019s an Etching\",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/patricia-smith\\u0022 hreflang=\\u0022und\\u0022\\u003EPatricia Smith\\u003C\\/a\\u003E\"},{\"field_poem_of_the_day_date\":\"04\\/15\\/2020\",\"view_node\":\"\\/poem\\/green-flame\",\"title\":\"Green Flame\",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/pamela-uschuk\\u0022 hreflang=\\u0022en\\u0022\\u003EPamela Uschuk\\u003C\\/a\\u003E\"},{\"field_poem_of_the_day_date\":\"04\\/14\\/2020\",\"view_node\":\"\\/poem\\/doubt-and-bad-reviews\",\"title\":\"On Doubt and Bad Reviews\",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/naomi-shihab-nye\\u0022 hreflang=\\u0022und\\u0022\\u003ENaomi Shihab Nye\\u003C\\/a\\u003E\"},{\"field_poem_of_the_day_date\":\"04\\/13\\/2020\",\"view_node\":\"\\/poem\\/wonder\",\"title\":\"Wonder\",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/mei-mei-berssenbrugge\\u0022 hreflang=\\u0022und\\u0022\\u003EMei-mei Berssenbrugge\\u003C\\/a\\u003E\"},{\"field_poem_of_the_day_date\":\"04\\/12\\/2020\",\"view_node\":\"\\/poem\\/monotone\",\"title\":\"Monotone\",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/carl-sandburg\\u0022 hreflang=\\u0022und\\u0022\\u003ECarl Sandburg\\u003C\\/a\\u003E\"},{\"field_poem_of_the_day_date\":\"04\\/11\\/2020\",\"view_node\":\"\\/poem\\/rainbow\",\"title\":\"The Rainbow \",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/effie-waller-smith\\u0022 hreflang=\\u0022en\\u0022\\u003EEffie Waller Smith\\u003C\\/a\\u003E\"},{\"field_poem_of_the_day_date\":\"04\\/10\\/2020\",\"view_node\":\"\\/poem\\/beggar\",\"title\":\"Beggar\",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/marilyn-kallet\\u0022 hreflang=\\u0022en\\u0022\\u003EMarilyn Kallet\\u003C\\/a\\u003E\"},{\"field_poem_of_the_day_date\":\"04\\/09\\/2020\",\"view_node\":\"\\/poem\\/ghazal-ya-ltyf-ya-lateef\",\"title\":\"Ghazal !\\u064a\\u0627 \\u0644\\u0637\\u064a\\u0641 (Ya Lateef!)\",\"field_author\":\"\\u003Ca href=\\u0022\\/poet\\/marilyn-hacker\\u0022 hreflang=\\u0022und\\u0022\\u003EMarilyn Hacker\\u003C\\/a\\u003E\"}],\"pager\":{\"current_page\":0,\"total_items\":\"3754\",\"total_pages\":376,\"items_per_page\":10}}";

    List<String> findNewPoems(PoetryDatabase db) throws IOException
    {
        //String jsonResponse2 = Jsoup.connect("https://api.poets.org/api/previous-poems?page=0").ignoreContentType(true).execute().body();
        JSONObject json = new JSONObject(jsonResponse);
        JSONArray results = json.getJSONArray("rows");
        ArrayList<String> list = new ArrayList<>();
        for (Object o : results) {
            if (!(o instanceof JSONObject))
                continue;
            JSONObject row = (JSONObject) o;
            String node = row.has("view_node") ? row.getString("view_node") : null;
            if (null == node)
                continue;
            String href = "https://poets.org" + node;
            if (!db.poemExists(idFromUrl(href)))
                list.add(href);
        }
        return list;
    }

    String idFromUrl(String s)
    {
        if (s.contains("?"))
            s = s.substring(0,s.indexOf("?"));
        return "poets_" + s.substring(s.lastIndexOf('/')+1);
    }

    Poem readPoem(String url) throws IOException
    {
        Document doc = Jsoup.connect(url).get();
        Element e = doc.selectFirst("MAIN.card--main");
        if (null == e)
            return null;
        String id = idFromUrl(url);
        String title  = e.selectFirst("DIV.card-header H1[itemprop='name']").text();
        String author = e.selectFirst("DIV.card-header A[itemprop='author']").text();
        Element body   = e.selectFirst("DIV.poem__body ");
        StringBuilder text = new StringBuilder();
        body.select("P").forEach(p -> {
            p.children().forEach(el ->
            {
                switch (el.tagName().toUpperCase())
                {
                    case "SPAN": text.append(el.text()); break;
                    case "BR": text.append("\n"); break;
                    default:
                        //System.out.println(el.tagName());
                        text.append(el.text());
                }
            });
            text.append("\n");
        });
        return new Poem(id,url,title,author,text.toString());
    }
}