package net.bellew;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: matthewb
 * Date: 2/23/13
 * Time: 12:43 PM
 */
public class ThermalPrinter
{
    OutputStream out;
    FontSize currentSize = null;

    final int BAUDRATE=19200;
    final int PAUSE_PER_CHAR=10;

    // Number of microseconds to issue one byte to the printer.  11 bits
    // (not 8) to accommodate idle, start and stop bits.  Idle time might
    // be unnecessary, but erring on side of caution here.

    enum FontSize
    {
        SMALL(0,24,32),
        MEDIUM(1,48,32),
        LARGE(2,48,16);

        final int size, charHeight, maxColumn;
        FontSize(int size, int charHeight, int maxColumn)
        {
            this.size = size;
            this.charHeight=charHeight;
            this.maxColumn=maxColumn;
        }
    }


    public ThermalPrinter(String dev) throws IOException
    {
        out = new FileOutputStream(dev);
    }


    public boolean hasPaper()
    {
        return true;
    }


    public void printPoem(Poem p) throws IOException
    {
        writeLarge("\n\n");
        if (null != p.title)
        {
            writeLarge(p.title);
        }
        if (null != p.author)
        {
            // if fontsize is different a \n will be added automatically
            if (currentSize == FontSize.MEDIUM)
                writeMedium("\n");
            writeMedium(p.author);
        }
        writeSmall("\n");
        String[] lines = p.text.split("\n");
        for (String line : lines)
        {
            writeSmall(line);
            writeSmall("\n");
            sleep(100);
        }
        writeSmall("\n\n\n\n\n\n");
    }


    void writeLarge(String s) throws IOException
    {
        FontSize sz = s.length() > FontSize.LARGE.maxColumn ? FontSize.MEDIUM : FontSize.LARGE;
        if (sz != currentSize)
            setSize(sz);
        writeString(s);
    }


    void writeMedium(String s) throws IOException
    {
        if (FontSize.MEDIUM != currentSize)
            setSize(FontSize.MEDIUM);
        writeString(s);
    }


    void writeSmall(String s) throws IOException
    {
        if (FontSize.SMALL != currentSize)
            setSize(FontSize.SMALL);
        writeString(s);
    }


    public void setSize(FontSize size) throws IOException
    {
        currentSize = size;
        writeBytes((byte) 29, (byte) 33, (byte) size.size, (byte) 10);
    }


    private void writeBytes(byte... bytes) throws IOException
    {
        out.write(bytes);
    }


    private void writeString(String s) throws IOException
    {
        for (int i=0 ; i<s.length() ; i++)
        {
            byte b = (byte)s.charAt(i);
            out.write(b);
            sleep(PAUSE_PER_CHAR);
        }
    }


    private void sleep(int ms)
    {
        try {Thread.sleep(ms);}catch (InterruptedException x) {}
    }
}
