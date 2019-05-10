import java.io.InputStream;
import java.io.Reader;

public class StarUMLImporter implements VisualModelImporter {
    @Override
    public String getSupportedFormat() {
        return "MDJ";
    }

    @Override
    public VisualModel ImportModel(String filePaths) {
        return ReadModel();
    }

    @Override
    public VisualModel ImportModel(InputStream inputStream) {
        return ReadModel();
    }

    @Override
    public VisualModel ImportModel(VisualModel model, Reader reader) {
        return ReadModel();
    }

    private VisualModel ReadModel(){
        VisualModel result = new VisualModel();
        return result;
    }
}
