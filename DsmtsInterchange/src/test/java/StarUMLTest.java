import java.io.File;
import java.io.IOException;

public class StarUMLTest {
    public static void main(String[] args) {
        StarUMLImporter importer = new StarUMLImporter();
        VisualModel testModel = importer.ImportModel(StarUMLTest.class.getResourceAsStream("Test.mdj"));

        File resultFile = new File("TestResult.mdj");
        boolean created = resultFile.exists();
        if (!resultFile.exists()) {
            try {
                created = resultFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!created)
            return;

        StarUMLExporter exporter = new StarUMLExporter();
        exporter.ExportModel(testModel, resultFile.getPath());
    }
}
