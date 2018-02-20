package edu.illinois.library.cantaloupe.processor.imageio;

import edu.illinois.library.cantaloupe.image.Compression;
import edu.illinois.library.cantaloupe.image.Format;
import edu.illinois.library.cantaloupe.operation.*;
import edu.illinois.library.cantaloupe.processor.ProcessorException;
import edu.illinois.library.cantaloupe.processor.UnsupportedSourceFormatException;
import edu.illinois.library.cantaloupe.resolver.StreamSource;
import edu.illinois.library.cantaloupe.util.SystemUtils;
import loci.formats.in.MRCReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

final class MRCImageReader extends AbstractImageReader {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MRCImageReader.class);

    private MRCReader mrcReader;
    /**
     * @param sourceFile Source file to read.
     */
    MRCImageReader(Path sourceFile) throws IOException {
        super(sourceFile, Format.MRC);
    }



    /**
     * @param inputStream Stream to read.
     */
    MRCImageReader(ImageInputStream inputStream) throws IOException {
        super(inputStream, Format.MRC);
    }

    /**
     * @param streamSource Source of streams to read.
     */
    MRCImageReader(StreamSource streamSource) throws IOException {
        super(streamSource, Format.MRC);
    }

    @Override
    Compression getCompression(int imageIndex) {
        return Compression.UNDEFINED;
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    @Override
    Metadata getMetadata(int imageIndex) {
        return new NullMetadata();
    }

    /**
     * @throws UnsupportedOperationException Always.
     */
    @Override
    BufferedImage read() {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException Always.
     */
    @Override
    BufferedImage read(OperationList ops,
                       Orientation orientation,
                       ReductionFactor reductionFactor,
                       Set<ImageReader.Hint> hints) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException Always.
     */
    @Override
    public RenderedImage readRendered(OperationList ops,
                                      Orientation orientation,
                                      ReductionFactor reductionFactor,
                                      Set<ImageReader.Hint> hints) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException Always.
     */
    @Override
    BufferedImageSequence readSequence() {
        throw new UnsupportedOperationException();
    }

    public String[] getMIMETypes(){
        return new String[]{"image/mrc"};
    }
}
