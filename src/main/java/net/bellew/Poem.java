package net.bellew;

/**
 * Created with IntelliJ IDEA.
 * User: matthewb
 * Date: 2/19/13
 * Time: 9:04 PM
 */
public class Poem
{
    final String id;
    final String link;
    final String author;
    final String title;
    final String text;

    public Poem(String id, String link, String title, String author, String text)
    {
        this.id = id;
        this.link = link;
        this.title = title;
        this.author = author;
        this.text = text;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (null != title)
            sb.append(title).append("\n");
        if (null != author)
            sb.append(author).append("\n");
        if (0 < sb.length())
            sb.append("\n");
        sb.append(text);
        if (!text.endsWith("\n"))
            sb.append("\n");
        if (null != link)
        {
            sb.append("\n");
            sb.append(link);
        }
        return sb.toString();
    }
}
