package edu.illinois.library.cantaloupe.processor;

import edu.illinois.library.cantaloupe.image.Format;
import edu.illinois.library.cantaloupe.image.Info;
import edu.illinois.library.cantaloupe.operation.*;
import edu.illinois.library.cantaloupe.operation.overlay.Overlay;
import edu.illinois.library.cantaloupe.operation.redaction.Redaction;
import edu.illinois.library.cantaloupe.resource.iiif.ProcessorFeature;
import edu.illinois.library.cantaloupe.processor.imageio.ImageReader;
import edu.illinois.library.cantaloupe.processor.imageio.ImageWriter;
import loci.common.ByteArrayHandle;
import loci.common.Location;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.*;
import loci.formats.gui.AWTImageTools;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.ome.OMEXMLMetadataImpl;
import loci.formats.out.JPEGWriter;
import loci.formats.services.OMEXMLService;
import ome.xml.model.enums.PixelType;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

/**
 * <p>Processor using the Java 2D and ImageIO libraries.</p>
 */
class BioFormatProcessor implements FileProcessor {

    protected Path sourceFile;
    private static final Set<ProcessorFeature> SUPPORTED_FEATURES =
            Collections.unmodifiableSet(EnumSet.of(
                    ProcessorFeature.SIZE_BY_WIDTH
            ));
    private static final Set<edu.illinois.library.cantaloupe.resource.iiif.v1.Quality>
            SUPPORTED_IIIF_1_1_QUALITIES = Collections.unmodifiableSet(EnumSet.of(
            edu.illinois.library.cantaloupe.resource.iiif.v1.Quality.NATIVE));
    private static final Set<edu.illinois.library.cantaloupe.resource.iiif.v2.Quality>
            SUPPORTED_IIIF_2_0_QUALITIES = Collections.unmodifiableSet(EnumSet.of(
            edu.illinois.library.cantaloupe.resource.iiif.v2.Quality.DEFAULT));

    private Format format;
    private loci.formats.ImageReader reader;
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
        try{
            byte[] bytes;
            int w = imageInfo.getSize().width;
            int h = imageInfo.getSize().height;
            int x = 0;
            int y = 0;

            if(reader == null){
                reader = new loci.formats.ImageReader();
                reader.setId(getSourceFile().toString());
            }
            bytes = reader.openBytes(0,x,y,w,h);

            IMetadata meta;
            try {
                ServiceFactory factory = new ServiceFactory();
                OMEXMLService service = factory.getInstance(OMEXMLService.class);
                meta = service.createOMEXMLMetadata();
            }
            catch (DependencyException exc) {
                throw new FormatException("Could not create OME-XML store.", exc);
            }
            catch (ServiceException exc) {
                throw new FormatException("Could not create OME-XML store.", exc);
            }

            String outId = "temp" + ".png";//+ops.getOutputFormat().getPreferredExtension();
            ByteArrayHandle outputFile = new ByteArrayHandle();
            Location.mapFile(outId, outputFile);

            MetadataTools.populatePixels(meta,reader);
            loci.formats.ImageWriter writer = new loci.formats.ImageWriter();
            writer.setMetadataRetrieve(meta);
            //writer.setCompression("Uncompressed");
            writer.setId(outId);

            writer.saveBytes(0, bytes);
            reader.close();
            writer.close();
/*
            outputStream.write(outputFile.getBytes());
            outputFile.close();
*/

            ByteArrayInputStream bais = new ByteArrayInputStream(outputFile.getBytes());
            ImageInputStream iis = ImageIO.createImageInputStream(bais);
            ImageReader imageReader = new ImageReader(iis,Format.PNG);

            final ReductionFactor rf = new ReductionFactor();
            final Set<ImageReader.Hint> hints =
                    EnumSet.noneOf(ImageReader.Hint.class);

            if (ops.getFirst(Normalize.class) != null) {
                // When normalizing, the reader needs to read the entire image
                // so that its histogram can be sampled accurately, which will
                // preserve the luminance across tiles.
                hints.add(ImageReader.Hint.IGNORE_CROP);
            }

            BufferedImage image = imageReader.read(ops, imageInfo.getOrientation(), rf, hints);
            image = doPostProcess(image, hints, ops, imageInfo, rf);
            new edu.illinois.library.cantaloupe.processor.imageio.ImageWriter(ops).write(image, outputStream);

/*
            BufferedImage image = new BufferedImage( imageInfo.getSize().width, imageInfo.getSize().height, BufferedImage.TYPE_BYTE_GRAY);
            byte[] array = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(outputFile.getBytes(), 0, array, 0, array.length);

            edu.illinois.library.cantaloupe.processor.imageio.ImageWriter writer2 = new edu.illinois.library.cantaloupe.processor.imageio.ImageWriter(ops);
            writer2.write(image,outputStream);
*/

        }catch(IOException e){
            throw new ProcessorException(e.getMessage());
        }catch(FormatException e){
            throw new ProcessorException(e.getMessage());
        }
    }

    public void setSourceFile(Path sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Info readImageInfo() throws IOException {
        reader = new loci.formats.ImageReader();
        try{
            reader.setId(getSourceFile().toString());
        }catch(FormatException e){
            throw new IOException();
        }

        return new Info(reader.getSizeX(), reader.getSizeY(),
                reader.getSizeX(), reader.getSizeY(), getSourceFormat());

    }


    public Set<Format> getAvailableOutputFormats() {
        Set<Format> formats = null;
        if(format == Format.MRC || format == Format.TIF){
            formats = Collections.unmodifiableSet(EnumSet.of(Format.JPG,Format.TIF,Format.PNG, Format.JP2));
        }
        if (formats == null) {
            formats = Collections.unmodifiableSet(Collections.emptySet());
        }
        return formats;
    }

    public void setSourceFormat(Format format) throws UnsupportedSourceFormatException {
        if(format == Format.MRC  || format == Format.TIF){
            this.format = format;
        }else{
            throw new UnsupportedSourceFormatException();
        }
    }

    public Format getSourceFormat(){
        return format;
    }

    private BufferedImage doPostProcess(BufferedImage image,
                                        Set<edu.illinois.library.cantaloupe.processor.imageio.ImageReader.Hint> readerHints,
                                        final OperationList opList,
                                        final Info imageInfo,
                                        ReductionFactor reductionFactor) throws IOException {
        final Format outputFormat = opList.getOutputFormat();

        if (reductionFactor == null) {
            reductionFactor = new ReductionFactor();
        }
        if (readerHints == null) {
            readerHints = EnumSet.noneOf(edu.illinois.library.cantaloupe.processor.imageio.ImageReader.Hint.class);
        }
        if (opList.getFirst(Normalize.class) != null) {
            image = Java2DUtil.stretchContrast(image);
        }

        // If the Encode operation specifies a max sample size of 8 bits, or if
        // the output format's max sample size is 8 bits, we will need to
        // clamp the image's sample size to 8 bits. HOWEVER, if the output
        // format's max sample size is LESS THAN 8 bits (e.g. GIF), don't do
        // anything and let the writer handle it.
        //
        // The writer could actually do this itself regardless, but doing it
        // here could make subsequent processing steps more efficient as they
        // will have less data to deal with.
        Encode encode = (Encode) opList.getFirst(Encode.class);
        if (((encode != null && encode.getMaxSampleSize() != null && encode.getMaxSampleSize() <= 8)
                || outputFormat.getMaxSampleSize() <= 8)
                && !Format.GIF.equals(outputFormat)) {
            image = Java2DUtil.reduceTo8Bits(image);
        }

        final Dimension fullSize = imageInfo.getSize();

        // Apply the crop operation, if present, and retain a reference
        // to it for subsequent operations to refer to.
        Crop crop = new Crop(0, 0, image.getWidth(), image.getHeight(),
                imageInfo.getOrientation(), imageInfo.getSize());
        for (Operation op : opList) {
            if (op instanceof Crop) {
                crop = (Crop) op;
                if (crop.hasEffect(fullSize, opList) &&
                        !readerHints.contains(edu.illinois.library.cantaloupe.processor.imageio.ImageReader.Hint.ALREADY_CROPPED)) {
                    image = Java2DUtil.crop(image, crop, reductionFactor);
                }
            }
        }

        // Redactions happen immediately after cropping.
        List<Redaction> redactions = new ArrayList<>();
        for (Operation op : opList) {
            if (op instanceof Redaction) {
                if (op.hasEffect(fullSize, opList)) {
                    redactions.add((Redaction) op);
                }
            }
        }
        image = Java2DUtil.applyRedactions(image, crop, reductionFactor,
                redactions);

        // Apply remaining operations.
        for (Operation op : opList) {
            if (op.hasEffect(fullSize, opList)) {
                if (op instanceof Scale &&
                        !readerHints.contains(edu.illinois.library.cantaloupe.processor.imageio.ImageReader.Hint.IGNORE_SCALE)) {
                    image = Java2DUtil.scale(image, (Scale) op,
                            reductionFactor);
                } else if (op instanceof Transpose) {
                    image = Java2DUtil.transpose(image, (Transpose) op);
                } else if (op instanceof Rotate) {
                    image = Java2DUtil.rotate(image, (Rotate) op);
                } else if (op instanceof ColorTransform) {
                    image = Java2DUtil.transformColor(image, (ColorTransform) op);
                } else if (op instanceof Sharpen) {
                    image = Java2DUtil.sharpen(image, (Sharpen) op);
                } else if (op instanceof Overlay) {
                    image = Java2DUtil.applyOverlay(image, (Overlay) op);
                }
            }
        }

        return image;
    }
}
