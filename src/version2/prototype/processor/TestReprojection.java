package version2.prototype.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;

import version2.prototype.Projection;
import version2.prototype.Projection.Datum;
import version2.prototype.Projection.ProjectionType;
import version2.prototype.Projection.ResamplingType;
import version2.prototype.util.GdalUtils;

public class TestReprojection
{
    public static void main(String [] args)
    {
        //        GdalUtils.register();
        gdal.AllRegister();

        synchronized (GdalUtils.lockObject)
        {

            Projection p = new Projection(ProjectionType.TRANSVERSE_MERCATOR, ResamplingType.BILINEAR,
                    Datum.WGS84, 1000, 0.0, 0.0, 0.9996, 39.0, 500000.0, 0.0, 0.0);

            projection("D:\\project\\band_JM.tif",
                    //"D:\\testProjects\\TW\\settings\\shapefiles\\TW_DIS_F_P_Dis_REGION\\TW_DIS_F_P_Dis_REGION.shp",
                    "D:\\testProjects\\Amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp",
                    p, new File("D:\\project\\band1_JMP.tif"));

            //extract NOAH
            //            String noah = "D:\\project\\download\\NOAH\\noah_2015_0604.grb";
            //            String outFile = "D:\\project\\Noah_b19.tif";
            //            GdalUtils.register();
            //            synchronized (GdalUtils.lockObject)
            //            {
            //                Dataset inputDS = gdal.Open(noah);
            //                int xSize = inputDS.getRasterXSize();
            //                int ySize = inputDS.getRasterYSize();
            //                Dataset outputDS = gdal.GetDriverByName("GTiff").
            //                        Create(
            //                                outFile,
            //                                xSize, ySize,
            //                                1,
            //                                gdalconst.GDT_Float32
            //                                );
            //
            //                Band b20 = inputDS.GetRasterBand(19);
            //
            //                double[] arr = new double[xSize * ySize];
            //                b20.ReadRaster(0,  0 , xSize, ySize, arr);
            //
            //                System.out.println("original prj ref: " + inputDS.GetProjection());
            //                String outputProjStr = "+proj=longlat +datum=WGS84 +no_defs";
            //                //String wktStr = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\"],SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
            //                SpatialReference output = new SpatialReference();
            //                output.ImportFromProj4(outputProjStr);
            //
            //                outputDS.SetProjection(output.ExportToWkt());
            //
            //                //outputDS.SetProjection(wktStr);
            //                System.out.println(outputDS.GetProjection());
            //                double [] geoTrans = inputDS.GetGeoTransform();
            //                System.out.println(geoTrans[0] + " : " + geoTrans[1] + " : " + geoTrans[2] + " : " + geoTrans[3] + " : " + geoTrans[4] + " : " + geoTrans[5]);
            //
            //                outputDS.SetGeoTransform(inputDS.GetGeoTransform());
            //
            //                outputDS.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize, arr);
            //                outputDS.delete();
            //                inputDS.delete();
            //            }
            //
            //            projection("D:\\project\\Noah_b19.tif",
            //                    //"D:\\project\\band20.tif",
            //                    //"D:\\testProjects\\TW\\settings\\shapefiles\\TW_DIS_F_P_Dis_REGION\\TW_DIS_F_P_Dis_REGION.shp",
            //                    "D:\\testProjects\\Amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp",
            //                    p, new File("D:\\project\\Noah_b19p.tif"));
        }
    }

    private static void projection(String input, String masterShapeFile, Projection projection, File output)
    {
        //String wktStr = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\"],SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
        String wktStr = " ";
        assert (masterShapeFile != null);
        GdalUtils.register();
        synchronized (GdalUtils.lockObject)
        {
            Dataset inputDS = gdal.Open(input);
            wktStr = inputDS.GetProjection();
            //  SpatialReference inputRef = new SpatialReference();

            //  inputRef.ImportFromWkt(wktStr);
            //inputRef.ImportFromWkt("GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433],AUTHORITY[\"EPSG\",4326]]");

            //inputRef.ImportFromWkt("GEOGCS[\"GCS_Undefined\",DATUM[\"Undefined\",SPHEROID[\"User_Defined_Spheroid\",6371007.181,0.0]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Sinusoidal\"],PARAMETER[\"False_Easting\",0.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",0.0],UNIT[\"Meter\",1.0]");
            // FIXME: abstract it somehow?
            // inputRef.ImportFromWkt("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\"],SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");

            // inputDS.SetProjection(inputRef.ExportToWkt());

            DataSource feature = ogr.Open(masterShapeFile);

            // Find union of extents
            double[] extent = null;
            try{
                extent = feature.GetLayer(0).GetExtent(); // Ordered: left, right, bottom, top
            }catch(Exception e)
            {
                System.out.println(e.toString());
                for(StackTraceElement el : e.getStackTrace())
                {
                    System.out.println(el.toString());
                }
            }

            double left = extent[0];
            double right = extent[1];
            double bottom = extent[2];
            double top = extent[3];


            Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                    output.getPath(),
                    (int) Math.ceil((right-left)/projection.getPixelSize()),
                    (int) Math.ceil((top-bottom)/projection.getPixelSize()),
                    1,
                    gdalconst.GDT_Float32
                    );


            SpatialReference outputRef = new SpatialReference();
            outputRef.ImportFromProj4("+proj=utm +zone=37 +datum=WGS84 +units=m +no_defs");

            //String outputProjection = feature.GetLayer(0).GetSpatialRef().ExportToWkt();
            String outputProjection = outputRef.ExportToWkt();
            // String outputProjection ="PROJCS[\"WGS_1984_UTM_Zone_14N\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",39.0],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]";
            //String outputProjection = "+proj=utm +zone=37 +datum=WGS84 +units=m +no_defs";

            // System.out.println("Reproject: input : " + inputRef.ExportToWkt());
            System.out.println("Reproject: output : " + outputProjection);
            System.out.println("Reproject: GeoTransform: " + left + " : " + right + " : " + bottom + " : " + top);

            outputDS.SetProjection(outputProjection);
            //outputDS.SetProjection(outputProjection);
            outputDS.SetGeoTransform(new double[] { left, (projection.getPixelSize()), 0, top, 0, -(double)(projection.getPixelSize()) });

            // get resample argument
            int resampleAlg = -1;
            ResamplingType resample = projection.getResamplingType();
            switch (resample) {
            case NEAREST_NEIGHBOR:
                resampleAlg = gdalconst.GRA_NearestNeighbour;
                break;
            case BILINEAR:
                resampleAlg = gdalconst.GRA_Bilinear;
                break;
            case CUBIC_CONVOLUTION:
                resampleAlg = gdalconst.GRA_CubicSpline;
            }

            System.out.println("Reproject image return : " + gdal.ReprojectImage(inputDS, outputDS, wktStr, outputProjection, resampleAlg));
            outputDS.GetRasterBand(1).ComputeStatistics(false);
            outputDS.delete();
            inputDS.delete();

        }
    }

}
