package edu.illinois.library.cantaloupe.processor;

import edu.illinois.library.cantaloupe.config.Configuration;
import edu.illinois.library.cantaloupe.config.Key;
import edu.illinois.library.cantaloupe.image.Format;
import edu.illinois.library.cantaloupe.image.Info;
import edu.illinois.library.cantaloupe.operation.Normalize;
import edu.illinois.library.cantaloupe.operation.OperationList;
import edu.illinois.library.cantaloupe.operation.Orientation;
import edu.illinois.library.cantaloupe.operation.ReductionFactor;
import edu.illinois.library.cantaloupe.processor.imageio.BufferedImageSequence;
import edu.illinois.library.cantaloupe.processor.imageio.ImageReader;
import edu.illinois.library.cantaloupe.resolver.StreamSource;
import edu.illinois.library.cantaloupe.resource.iiif.ProcessorFeature;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

/**
 * <p>Processor using the Java 2D and ImageIO libraries.</p>
 */
class BioFormatProcessor extends AbstractProcessor
        implements FileProcessor {

    private ImageReader reader;
    protected Path sourceFile;
    private static final Set<ProcessorFeature> SUPPORTED_FEATURES =
            Collections.unmodifiableSet(EnumSet.of(
                    ProcessorFeature.REGION_BY_PIXELS,
                    ProcessorFeature.SIZE_BY_HEIGHT,
                    ProcessorFeature.SIZE_BY_PERCENT,
                    ProcessorFeature.SIZE_BY_WIDTH,
                    ProcessorFeature.SIZE_BY_WIDTH_HEIGHT));
    private static final Set<edu.illinois.library.cantaloupe.resource.iiif.v1.Quality>
            SUPPORTED_IIIF_1_1_QUALITIES = Collections.unmodifiableSet(EnumSet.of(
            edu.illinois.library.cantaloupe.resource.iiif.v1.Quality.NATIVE));
    private static final Set<edu.illinois.library.cantaloupe.resource.iiif.v2.Quality>
            SUPPORTED_IIIF_2_0_QUALITIES = Collections.unmodifiableSet(EnumSet.of(
            edu.illinois.library.cantaloupe.resource.iiif.v2.Quality.DEFAULT));

    public Set<ProcessorFeature> getSupportedFeatures() {
        Set<ProcessorFeature> features;
        if (!getAvailableOutputFormats().isEmpty()) {
            features = SUPPORTED_FEATURES;
        } else {
            features = Collections.unmodifiableSet(Collections.emptySet());
        }
        return features;
    }

    public Set<edu.illinois.library.cantaloupe.resource.iiif.v1.Quality>
    getSupportedIIIF1Qualities() {
        Set<edu.illinois.library.cantaloupe.resource.iiif.v1.Quality>
                qualities;
        if (!getAvailableOutputFormats().isEmpty()) {
            qualities = SUPPORTED_IIIF_1_1_QUALITIES;
        } else {
            qualities = Collections.unmodifiableSet(Collections.emptySet());
        }
        return qualities;
    }

    public Set<edu.illinois.library.cantaloupe.resource.iiif.v2.Quality>
    getSupportedIIIF2Qualities() {
        Set<edu.illinois.library.cantaloupe.resource.iiif.v2.Quality>
                qualities;
        if (!getAvailableOutputFormats().isEmpty()) {
            qualities = SUPPORTED_IIIF_2_0_QUALITIES;
        } else {
            qualities = Collections.unmodifiableSet(Collections.emptySet());
        }
        return qualities;
    }

    public Path getSourceFile() {
        return sourceFile;
    }

    @Override
    public void process(final OperationList ops,
                        final Info imageInfo,
                        final OutputStream outputStream)
            throws ProcessorException {

        ImageReader reader = null;

    }

    public void setSourceFile(Path sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Info readImageInfo() throws IOException {
        final Info info = new Info();
        info.setSourceFormat(getSourceFormat());

        final ImageReader reader = getReader();
        final Orientation orientation = getEffectiveOrientation();
        for (int i = 0, numImages = reader.getNumImages(); i < numImages; i++) {
            Info.Image image = new Info.Image();
            image.setOrientation(orientation);
            image.setSize(reader.getSize(i));
            image.setTileSize(reader.getTileSize(i));
            // JP2 tile dimensions are inverted, so swap them
            if ((image.width > image.height && image.tileWidth < image.tileHeight) ||
                    (image.width < image.height && image.tileWidth > image.tileHeight)) {
                int tmp = image.tileWidth;
                image.tileWidth = image.tileHeight;
                image.tileHeight = tmp;
            }
            info.getImages().add(image);
        }
        return info;
    }

    Orientation getEffectiveOrientation() throws IOException {
        Orientation orientation = null;
        if (Configuration.getInstance().
                getBoolean(Key.PROCESSOR_RESPECT_ORIENTATION, false)) {
            orientation = getReader().getMetadata(0).getOrientation();
        }
        if (orientation == null) {
            orientation = Orientation.ROTATE_0;
        }
        return orientation;
    }

    protected ImageReader getReader() throws IOException {
        if (reader == null) {
            reader = new ImageReader(sourceFile, getSourceFormat());
        }
        return reader;
    }

    public Set<Format> getAvailableOutputFormats() {
        Set<Format> formats = null;
        if(format == Format.MRC){
            formats = Collections.unmodifiableSet(EnumSet.of(Format.TIF));
        }
        if (formats == null) {
            formats = Collections.unmodifiableSet(Collections.emptySet());
        }
        return formats;
    }
}
