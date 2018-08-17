package version2.prototype.processor;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Gdalinfo {
    static public void main(String [ ] args)
    {
        Process p;

        String fileName = "/home/wmmm6262/Desktop/h21v07.hdf";

        try {
            // call the gdalinfo to list the HDF info including the sdsNames
            String command = "./lib/gdal/gdalinfo " + fileName;
            p = Runtime.getRuntime().exec(command);
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            int j = 0;

            while ((line = reader.readLine())!= null) {
                /* Pattern pattern = Pattern.compile(bandpattern);
            Matcher matcher = pattern.matcher(line);

            if (matcher.find())
            {
                if (bandNames[j] == null) {
                    bandNames[j] = " ";
                }
                // add bands to the string
                bandNames[j++] += line.substring(line.indexOf('=') + 1) + " ";
            }*/
                System.out.println(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
