package com.inkysea.vmware.vra.jenkins.plugin.util;

import java.io.*;
import java.util.Collection;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


public class zip {


    private static File INPUT_DIRECTORY = null;
    private static String ZIP_FILE = null;
    private static String OUTPUT_DIRECTORY = null;
    private static File ZIP_FILE_ABSOLUTE_PATH = null;


    public zip(String zipFile ,String inDirectoryPath, String outDirectoryPath)  {


        this.ZIP_FILE = zipFile;
        this.INPUT_DIRECTORY = new File(inDirectoryPath);
        this.OUTPUT_DIRECTORY = outDirectoryPath;
        this.ZIP_FILE_ABSOLUTE_PATH = new File (outDirectoryPath+"/"+zipFile);

    }


    public boolean Create() throws IOException, ArchiveException {

        System.out.println("Input Directory "+ INPUT_DIRECTORY);


        OutputStream archiveStream = new FileOutputStream(ZIP_FILE_ABSOLUTE_PATH);
        ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);

        Collection<File> fileList = FileUtils.listFiles(INPUT_DIRECTORY , null, true);

        for (File file : fileList) {

            String relative = INPUT_DIRECTORY.toURI().relativize(file.toURI()).toString();

           // String entryName = getEntryName(INPUT_DIRECTORY, file);


            if ( ! relative.startsWith(".") &! relative.equalsIgnoreCase(ZIP_FILE.toString())) {

                System.out.println("Adding file name "+ relative + " to "+ZIP_FILE_ABSOLUTE_PATH);


                ZipArchiveEntry entry = new ZipArchiveEntry(relative);
                archive.putArchiveEntry(entry);

                BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

                IOUtils.copy(input, archive);
                input.close();
                archive.closeArchiveEntry();
            }
        }

        archive.finish();
        archiveStream.close();

        return true;
    }

    private String getEntryName(File source, File file) throws IOException {
        int index = source.getAbsolutePath().length();
        String path = file.getCanonicalPath();

        return path.substring(index);
    }



    public boolean Delete() {

        return true;
    }
}


