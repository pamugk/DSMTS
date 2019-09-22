import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StarUMLImporter implements VisualModelImporter {
    @Override
    public String getSupportedFormat() {
        return "MDJ";
    }

    @Override
    public VisualModel ImportModel(String filePaths)
    {
        String mdj = null;
        try {
            mdj = FileUtils.readFileToString(FileUtils.getFile(filePaths), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ReadModel(mdj);
    }

    @Override
    public VisualModel ImportModel(InputStream inputStream) {
        String mdj = null;
        try {
            mdj =  IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ReadModel(mdj);
    }

    @Override
    public VisualModel ImportModel(VisualModel model, Reader reader) {
        String mdj = null;
        try {
            mdj = IOUtils.toString(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ReadModel(mdj);
    }

    private VisualModel ReadModel(String mdj){
        VisualModel result = new VisualModel();
        PolarHypergraph struct = new PolarHypergraph();
        result.setStructure(struct);
        ModelView view = new ModelView();
        result.setView(view);
        if (mdj != null)
            struct.tryAddNode(ParseNode(new JsonParser().parse(mdj).getAsJsonObject(), struct, view));
        return result;
    }

    private Node dereferenceNode(JsonObject nodeRef, PolarHypergraph associatedStructure){
        String nodeRefVal = nodeRef.get("$ref").getAsString();
        Node derefencedNode = associatedStructure.tryGetNode(nodeRefVal);
        if (derefencedNode == null){
            derefencedNode = new Node(nodeRefVal);
            associatedStructure.tryAddNode(derefencedNode);
        }
        return derefencedNode;
    }

    private View formView(JsonObject mainObj, List<String> propNames){
        View view = new View();
        view.setAssociatedElementID(mainObj.get("_id").getAsString());
        if (mainObj.get("name") != null)
            view.setName(mainObj.get("name").getAsString());
        JsonObject props = new JsonObject();
        for (String prop: propNames) {
            JsonElement element = mainObj.get(prop);
            if (element != null)
                props.add(prop, element);
        }
        view.setContent(new Gson().toJson(props));
        return view;
    }

    private static final List<String> nodeProps = Arrays.asList(
            "_type", "ownedViews", "stereotype", "visibility", "isAbstract",
            "isFinalSpecialization", "isLeaf", "defaultDiagram",
            "name", "author", "company", "copyright", "version",
            "documentation", "viewpoint", "importedElements"
    );

    private Node ParseNode(JsonObject nodeObj, PolarHypergraph associatedStruct, ModelView mainView) {
        Node node = new Node(nodeObj.get("_id").getAsString());
        JsonElement innerStruct = nodeObj.get("ownedElements");
        View nodeView = formView(nodeObj, nodeProps);
        mainView.NodeViews.put(node.getId(), nodeView);

        if (innerStruct != null) {
            PolarHypergraph struct = new PolarHypergraph();
            node.setInnerStructure(struct);
            for (JsonElement elem: innerStruct.getAsJsonArray()) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.get("source") != null || obj.get("end1") != null)
                    associatedStruct.tryAddHyperedge(parseHyperedge(obj, associatedStruct, mainView));
                else struct.tryAddNode(ParseNode(obj, struct, mainView));
            }
        }
        return node;
    }

    private static final List<String> hyperedgeProps = Arrays.asList(
            "_type", "stereotype", "visibility", "discriminator", "isDerived",
            "documentation"
    );

    private Hyperedge parseHyperedge(JsonObject hyperedgeObj, PolarHypergraph associatedStruct, ModelView mainView){
        Hyperedge parsedHyperedge = new Hyperedge(hyperedgeObj.get("_id").getAsString());
        Node sourceNode, targetNode;
        ArrayList<Pole> poles = new ArrayList<>();
        if (hyperedgeObj.get("_type").getAsString().equals("UMLAssociation")) {
            poles.add((parsePole(hyperedgeObj.get("end1").getAsJsonObject(), associatedStruct, mainView)));
            sourceNode = poles.get(0).getParent();
            poles.add(parsePole(hyperedgeObj.get("end2").getAsJsonObject(), associatedStruct, mainView));
            targetNode = poles.get(1).getParent();
        }
        else {
            sourceNode = dereferenceNode(hyperedgeObj.get("source").getAsJsonObject(), associatedStruct);
            String fictionalID = sourceNode.getId()+"p";
            Pole sourcePole = associatedStruct.tryGetPole(fictionalID);
            if (sourcePole == null)
                sourcePole = new Pole(sourceNode, fictionalID);
            poles.add(sourcePole);

            targetNode = dereferenceNode(hyperedgeObj.get("target").getAsJsonObject(), associatedStruct);
            poles.add(new Pole(targetNode, targetNode.getId()+"p"));
        }
        sourceNode.tryAddOutputPole(poles.get(0));
        targetNode.tryAddInputPole(poles.get(1));
        for (Pole pole:poles){
            parsedHyperedge.tryAddPole(pole);
            associatedStruct.tryAddPole(pole);
        }

        View hyperedgeView = formView(hyperedgeObj, hyperedgeProps);
        mainView.LinkViews.put(hyperedgeView.getAssociatedElementId(), hyperedgeView);
        return parsedHyperedge;
    }

    private static final List<String> poleProps = Arrays.asList(
            "_type", "documentation", "visibility", "navigable", "aggregation",
            "multiplicity", "defaultValue", "isReadOnly", "isOrdered",
            "isUnique", "isDerived", "isID"
    );

    private Pole parsePole(JsonObject poleObj, PolarHypergraph associatedStructure, ModelView mainView){
        Node associatedNode = dereferenceNode(poleObj.get("reference").getAsJsonObject(), associatedStructure);
        Pole parsedPole = new Pole(associatedNode, poleObj.get("_id").getAsString());
        View poleView = formView(poleObj, poleProps);
        mainView.PoleViews.put(poleView.getAssociatedElementId(), poleView);
        return parsedPole;
    }
}
