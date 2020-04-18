package net.bellew;

import java.io.*;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: matthewb
 * Date: 2/19/13
 * Time: 6:17 PM
 */

public class PoetryDatabase
{
    private final File priorityDir;
    private final File inboxDir;
    private final File readDir;


    public PoetryDatabase(File f) throws IOException
    {
        if (!f.isDirectory())
            throw new IOException(f.getPath() + " is not a directory");
        inboxDir = new File(f,"inbox");
        if (!inboxDir.exists())
            inboxDir.mkdir();
        readDir = new File(f,"read");
        if (!readDir.exists())
            readDir.mkdir();
        priorityDir = new File(f,"priority");
        if (!priorityDir.exists())
            priorityDir.mkdir();
    }


    public boolean poemExists(String identifier)
    {
        if ((new File(priorityDir, identifier)).exists())
            return true;
        if ((new File(inboxDir, identifier)).exists())
            return true;
        if ((new File(readDir, identifier)).exists())
            return true;
        return false;
    }


    final static Random r = new Random();
    final static FileFilter ff = new FileFilter(){
        public boolean accept(File f) {
            return f.isFile();
        }
    };


    public Poem getPoem() throws IOException
    {
        File[] list = priorityDir.listFiles(ff);
        if (null == list || 0 == list.length)
            list = inboxDir.listFiles(ff);
        if (null == list || 0 == list.length)
            return null;
        int i = r.nextInt(list.length);
        return parseFile(list[i]);
    }


    public void addPoem(Poem p, boolean priority) throws IOException
    {
        if (null == p.text)
            return;

        if (poemExists(p.id))
            return;

        File f;
        if (priority)
            f = new File(priorityDir, p.id);
        else
            f = new File(inboxDir, p.id);

        PrintWriter pw = new PrintWriter(f);
        try
        {
            if (null != p.title)
                pw.println("title:" + p.title);
            if (null != p.author)
                pw.println("author:" + p.author);
            if (null != p.link)
                pw.println("link:" + p.link);
            pw.println(p.text);
        }
        finally
        {
            pw.close();
        }
    }


    public void markRead(Poem p)
    {
        File from = new File(priorityDir,p.id);
        if (!from.isFile())
            from = new File(inboxDir,p.id);
        File dest = new File(readDir, p.id);
        if (!from.isFile() || dest.exists())
            return;
        from.renameTo(dest);
    }


    public Poem parseFile(File f) throws IOException
    {
        BufferedReader r = new BufferedReader(new FileReader(f));
        try
        {
            return parse(f.getName(), r);
        }
        finally
        {
            if (null != r)
                r.close();
        }
    }


    public Poem parse(String id, BufferedReader r) throws IOException
    {
        String title="", author="", link="";
        String s;
        StringBuilder text = new StringBuilder();
        while (null != (s = r.readLine()))
        {
            if (s.startsWith("title:"))
                title = s.substring("title:".length()).trim();
            else if (s.startsWith("author:"))
                author = s.substring("author:".length()).trim();
            else if (s.startsWith("link:"))
                link = s.substring("link:".length()).trim();
            else
            {
                text.append(s);
                text.append("\n");
            }
        }
        return new Poem(id, link, title, author, text.toString());
    }
}