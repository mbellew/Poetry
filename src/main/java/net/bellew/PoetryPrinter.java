package net.bellew;

import net.bellew.providers.PoetryFoundationHtml;
import net.bellew.providers.PoetsOrg;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: matthewb
 * Date: 2/19/13
 * Time: 6:44 PM
 */
public class PoetryPrinter
{
    public static void main(String[] args) throws IOException
    {
        String command = "find";
        if (args.length> 0)
            command = args[0];
        String path = "/home/pi/Poetry/database";
        if (args.length > 1)
            path = args[1];

        if (!(new File(path)).isDirectory())
        {
            usage();
            return;
        }

        if (!"find".equals(command) && !"print".equals(command))
        {
            usage();
            return;
        }

        PoetryDatabase db = new PoetryDatabase(new File(path));

        if ("find".equals(command))
        {
            try
            {
                new PoetryFoundationHtml().find(db);
            }
            catch (IOException x)
            {
                System.err.println(x.getMessage());
            }

            try
            {
                new PoetsOrg().find(db);
            }
            catch (IOException x)
            {
                System.err.println(x.getMessage());
            }

//            try
//            {
//                Gmail gm = new Gmail();
//                gm.find(db);
//            }
//            catch (IOException x)
//            {
//                System.err.println(x.getMessage());
//            }

            return;
        }

        if ("print".equals(command))
        {
            Poem poem = db.getPoem();
            ThermalPrinter printer = new ThermalPrinter("/dev/ttyAMA0");
            if (null != poem)
            {
                if (printer.hasPaper())
                {
                    printer.printPoem(poem);
                    db.markRead(poem);
                }
            }
            else
            {
                printer.writeLarge("OUT OF POEMS\n\n\n\n");
            }
        }
    }


    static void usage()
    {
        System.out.println("java -Dgoogle.password={pwd} -jar Poetry.jar find|print database");
    }
}
