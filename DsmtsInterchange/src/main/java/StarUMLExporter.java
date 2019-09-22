import com.google.gson.*;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StarUMLExporter implements VisualModelExporter {
    @Override
    public String getSupportedFormat() {
        return "MDJ";
    }

    @Override
    public void ExportModel(VisualModel model, String filePath) {
        JsonObject serializedModel = serializeNode(model.getStructure().getNodes().get(0), null, model.getView());
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.disableHtmlEscaping();
        Gson serializer = builder.create();
        try {
            FileUtils.writeStringToFile(new File(filePath), serializer.toJson(serializedModel), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ExportModel(VisualModel model, OutputStream outputStream) {
        JsonObject serializedModel = serializeNode(model.getStructure().getNodes().get(0), null, model.getView());
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson serializer = builder.create();
        try {
            IOUtils.write(serializer.toJson(serializedModel), outputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ExportModel(VisualModel model, Writer writer) {
        JsonObject serializedModel = serializeNode(model.getStructure().getNodes().get(0), null, model.getView());
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson serializer = builder.create();
        try {
            IOUtils.write(serializer.toJson(serializedModel), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonObject referenceNode(Node node){
        JsonObject nodeReference = new JsonObject();
        nodeReference.addProperty("$ref", node.getId());
        return nodeReference;
    }

    private void readView(JsonObject props, JsonObject formedObj, List<String> propList){
        for (String propName: propList)
            if (props.get(propName) != null)
                formedObj.add(propName, props.get(propName));
    }

    private static final List<String> nodeProps = Arrays.asList(
            "_type", "ownedViews", "stereotype", "visibility", "isAbstract",
            "isFinalSpecialization", "isLeaf", "defaultDiagram",
            "name", "author", "company", "copyright", "version",
            "documentation", "viewpoint", "importedElements"
    );

    private JsonObject serializeNode(Node node, Node parent, ModelView mainView){
        JsonObject serializedNode = new JsonObject();
        View nodeView = mainView.NodeViews.get(node.getId());
        JsonObject props = new JsonParser().parse(nodeView.getContent()).getAsJsonObject();
        serializedNode.addProperty("_type", props.get("_type").getAsString());
        serializedNode.addProperty("_id", node.getId());

        if (parent != null) {
            JsonObject parentRef = new JsonObject();
            parentRef.addProperty("$ref", parent.getId());
            serializedNode.add("_parent", parentRef);
        }
        serializedNode.addProperty("name", nodeView.getName());

        JsonArray struct = new JsonArray();
        List<Pole> outputPoles = node.getOutputPoles();
        if (parent != null){
            ArrayList<Pair<Pole, Hyperedge>> links = new ArrayList<>();
            parent
                    .getInnerStructure()
                    .getHyperedges()
                    .stream()
                    .forEach(hyperedge -> {
                                for (Pole outputPole: outputPoles)
                                    if (hyperedge.getLinkedPoles().contains(outputPole))
                                        links.add(new Pair<>(outputPole, hyperedge));
                            }
                    );
            links.forEach(link -> struct.add(serializeHyperedge(link.getValue(), link.getKey(), mainView)));
        }


        if (node.getInnerStructure() !=  null)
            for (Node subnode : node.getInnerStructure().getNodes())
                struct.add(serializeNode(subnode, node, mainView));

        if (struct.size() > 0)
            serializedNode.add("ownedElements", struct);
        readView(props, serializedNode, nodeProps);

        return serializedNode;
    }

    private static final List<String> hyperedgeProps = Arrays.asList(
            "_type", "stereotype", "visibility", "discriminator", "isDerived",
            "documentation"
    );

    private JsonObject serializeHyperedge(Hyperedge hyperedge, Pole source, ModelView mainView){
        JsonObject serializedHyperedge = new JsonObject();
        serializedHyperedge.addProperty("_id", hyperedge.getId());
        View hyperedgeView = mainView.LinkViews.get(hyperedge.getId());
        JsonObject props = new JsonParser().parse(hyperedgeView.getContent()).getAsJsonObject();

        if (hyperedgeView.getName() != null && !hyperedgeView.getName().equals(""))
            serializedHyperedge.addProperty("name", hyperedgeView.getName());
        serializedHyperedge.add("_parent", referenceNode(source.getParent()));

        Pole target = hyperedge.getLinkedPoles().stream().filter(pole -> !pole.getId().equals(source.getId())).collect(Collectors.toList()).get(0);
        if ("UMLAssociation".equals(props.get("_type").getAsString())) {
            serializedHyperedge.add("end1", serializePole(source, hyperedge, mainView));
            serializedHyperedge.add("end2", serializePole(target, hyperedge, mainView));
        } else {
            serializedHyperedge.add("source", referenceNode(source.getParent()));
            serializedHyperedge.add("target", referenceNode(target.getParent()));
        }

        readView(props, serializedHyperedge, hyperedgeProps);
        return serializedHyperedge;
    }

    private static final List<String> poleProps = Arrays.asList(
            "_type", "documentation", "visibility", "navigable", "aggregation",
            "multiplicity", "defaultValue", "isReadOnly", "isOrdered",
            "isUnique", "isDerived", "isID"
    );

    private JsonObject serializePole(Pole pole, Hyperedge associatedHyperedge, ModelView mainView){
        JsonObject serializedPole = new JsonObject();
        serializedPole.addProperty("_id", pole.getId());
        View poleView = mainView.PoleViews.get(pole.getId());
        JsonObject props = new JsonParser().parse(poleView.getContent()).getAsJsonObject();

        JsonObject parentObj = new JsonObject();
        parentObj.addProperty("$ref", associatedHyperedge.getId());

        if (poleView.getName() != null && !poleView.getName().equals(""))
            serializedPole.addProperty("name", poleView.getName());
        serializedPole.add("_parent", parentObj);
        serializedPole.add("reference", referenceNode(pole.getParent()));
        readView(props, serializedPole, poleProps);
        return serializedPole;
    }
}
