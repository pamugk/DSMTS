import java.io.OutputStream;
import java.io.Writer;

public class StarUMLExporter implements VisualModelExporter {
    @Override
    public String getSupportedFormat() {
        return "MDJ";
    }

    @Override
    public void ExportModel(VisualModel model, String filePath) {

    }

    @Override
    public void ExportModel(VisualModel model, OutputStream outputStream) {

    }

    @Override
    public void ExportModel(VisualModel model, Writer writer) {

    }
}
