package nl.michielproce.cropscannedphotos;

import org.opencv.core.Core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        if(args.length != 2) {
            System.out.println("Please provide input directory and output directory");
            System.exit(-1);
        }

        Properties properties = new Properties();

        try (InputStream in = new FileInputStream("multicrop-scanned-photos.properties")) {
            properties.load(in);
        }

        Path baseIn = Paths.get(args[0]);
        Path baseOut = Paths.get(args[1]);

        System.out.println("Starting multicrop-scanned-photos");
        System.out.println("Input path:  " + baseIn);
        System.out.println("Output path: " + baseOut);
        System.out.println();

        List<Path> files = Files.walk(baseIn).filter(Files::isRegularFile).collect(Collectors.toList());

        for (Path in : files) {
            System.out.println("In:        " + in);


            Path out = baseOut.resolve(baseIn.relativize(in));

            System.out.println("Out:       " + out);

            Files.createDirectories(out.getParent());

            Extractor extractor = new Extractor(properties, in.toString(), out.toString());
            int count = extractor.start();
            System.out.println("Extracted: " + count + " picture(s)");
            System.out.println();

            System.gc();
        }

        System.out.println("");
        System.out.println("Done");
    }
}
