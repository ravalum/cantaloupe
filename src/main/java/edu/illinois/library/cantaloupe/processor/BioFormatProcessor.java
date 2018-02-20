package edu.illinois.library.cantaloupe.processor;

import edu.illinois.library.cantaloupe.image.Format;
import edu.illinois.library.cantaloupe.image.Info;
import edu.illinois.library.cantaloupe.operation.Normalize;
import edu.illinois.library.cantaloupe.operation.OperationList;
import edu.illinois.library.cantaloupe.operation.ReductionFactor;
import edu.illinois.library.cantaloupe.processor.imageio.BufferedImageSequence;
import edu.illinois.library.cantaloupe.processor.imageio.ImageReader;
import edu.illinois.library.cantaloupe.resource.iiif.ProcessorFeature;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * <p>Processor using the Java 2D and ImageIO libraries.</p>
 */
class BioFormatProcessor extends AbstractImageIOProcessor
        implements StreamProcessor, FileProcessor {

    private static final Set<ProcessorFeature> SUPPORTED_FEATURES =
            Collections.unmodifiableSet(EnumSet.of(
                    ProcessorFeature.MIRRORING,
                    ProcessorFeature.REGION_BY_PERCENT,
                    ProcessorFeature.REGION_BY_PIXELS,
                    ProcessorFeature.REGION_SQUARE,
                    ProcessorFeature.ROTATION_ARBITRARY,
                    ProcessorFeature.ROTATION_BY_90S,
                    ProcessorFeature.SIZE_ABOVE_FULL,
                    ProcessorFeature.SIZE_BY_DISTORTED_WIDTH_HEIGHT,
                    ProcessorFeature.SIZE_BY_FORCED_WIDTH_HEIGHT,
                    ProcessorFeature.SIZE_BY_HEIGHT,
                    ProcessorFeature.SIZE_BY_PERCENT,
                    ProcessorFeature.SIZE_BY_WIDTH,
                    ProcessorFeature.SIZE_BY_WIDTH_HEIGHT));
    private static final Set<edu.illinois.library.cantaloupe.resource.iiif.v1.Quality>
            SUPPORTED_IIIF_1_1_QUALITIES = Collections.unmodifiableSet(EnumSet.of(
            edu.illinois.library.cantaloupe.resource.iiif.v1.Quality.BITONAL,
            edu.illinois.library.cantaloupe.resource.iiif.v1.Quality.COLOR,
            edu.illinois.library.cantaloupe.resource.iiif.v1.Quality.GRAY,
            edu.illinois.library.cantaloupe.resource.iiif.v1.Quality.NATIVE));
    private static final Set<edu.illinois.library.cantaloupe.resource.iiif.v2.Quality>
            SUPPORTED_IIIF_2_0_QUALITIES = Collections.unmodifiableSet(EnumSet.of(
            edu.illinois.library.cantaloupe.resource.iiif.v2.Quality.BITONAL,
            edu.illinois.library.cantaloupe.resource.iiif.v2.Quality.COLOR,
            edu.illinois.library.cantaloupe.resource.iiif.v2.Quality.DEFAULT,
            edu.illinois.library.cantaloupe.resource.iiif.v2.Quality.GRAY));

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

    @Override
    public void process(final OperationList ops,
                        final Info imageInfo,
                        final OutputStream outputStream)
            throws ProcessorException {

        ImageReader reader = null;

    }

}
