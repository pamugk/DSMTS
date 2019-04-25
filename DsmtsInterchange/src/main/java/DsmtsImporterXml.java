import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.thoughtworks.xstream.XStream;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DsmtsImporterXml implements VisualModelImporter {
    //Метод, возвращающий поддерживаемый импортером формат
    public String getSupportedFormat() {
        return "DSMTS";
    }

    //Метод, импортирующий из файла по указанному пути filePath
    public VisualModel ImportModel(String filePaths) {
        Document doc;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.parse(new File(filePaths));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
        return ReadVisualModel(doc.getDocumentElement());
    }

    //Метод, импортирующий модель из переданного потока inputStream
    public VisualModel ImportModel(InputStream inputStream) {
        Document doc;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.parse(inputStream);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
        return ReadVisualModel(doc.getDocumentElement());
    }

    //Метод, читающий модель из переданного reader
    public VisualModel ImportModel(VisualModel model, Reader reader) {
        Document doc;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.parse(reader.toString());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
        return ReadVisualModel(doc.getDocumentElement());
    }

    //Метод, читающий из XML-элемента визуальную модельф
    private static VisualModel ReadVisualModel(Element docEl)
    {
        VisualModel model = new VisualModel();
        Element vmEl = (Element)docEl.getElementsByTagName("VisualModel").item(0);
        model.setStructure(ReadHypergraph(vmEl));
        model.setView(ReadModelView(vmEl));
        return model;
    }

    //Метод, читающий из XML-элемента гиперграф
    private static PolarHypergraph ReadHypergraph(Element node)
    {
        PolarHypergraph hypergraph = new PolarHypergraph();
        HashMap<String, Pair<HashSet<String>, HashSet<String>>> nodesPoles = new HashMap<>();
        ReadNodes((Element)node.getElementsByTagName("Nodes").item(0), hypergraph, nodesPoles);
        ReadPoles((Element)node.getElementsByTagName("Poles").item(0), hypergraph, nodesPoles);
        ReadLinks((Element)node.getElementsByTagName("Hyperedges").item(0), hypergraph);
        return hypergraph;
    }

    //Метод, читающий из XML-элемента визуальное представление визуальной модели
    private static ModelView ReadModelView(Element node){
        ModelView mv = new ModelView();
        mv.NodeViews = ReadViews((Element)node.getElementsByTagName("NodeViews").item(0), "NodeView");
        mv.NodeViews = ReadViews((Element)node.getElementsByTagName("PoleViews").item(0), "PoleView");
        mv.NodeViews = ReadViews((Element)node.getElementsByTagName("LinkViews").item(0), "LinkView");
        return mv;
    }

    //Метод, читающий из XML-элемента визуальные представдения гиперграфа
    private static HashMap<String, View> ReadViews(Element node, String singleViewName){
        int N = Integer.parseInt(node.getAttribute("Count"));
        NodeList viewList = node.getElementsByTagName(singleViewName);
        return IntStream.range(0, N)
                .mapToObj(i -> ReadView((Element) viewList.item(i)))
                .collect(Collectors.toMap(View::getAssociatedElementId, view -> view, (a, b) -> b, HashMap::new));
    }

    //Метод, читающий из XML-элемента вершины гиперграфа
    private static void ReadNodes(Element element, PolarHypergraph hypergraph,
                                  HashMap<String, Pair<HashSet<String>, HashSet<String>>> nodesPoles)
    {
        int N = Integer.parseInt(element.getAttribute("NodeCount"));
        NodeList nodeList = element.getElementsByTagName("Node");
        IntStream.range(0, N).forEach(i -> {
            Pair<HashSet<String>, HashSet<String>> poles = new Pair(new HashSet<String>(), new HashSet<String>());
            Node node = ReadNode((Element) nodeList.item(i), poles);
            nodesPoles.put(node.getId(), poles);
            hypergraph.tryAddNode(node);
        });
    }

    //Метод, читающий из XML-элемента полюса гиперграфа
    private static void ReadPoles(Element polesEl, PolarHypergraph model,
                                  HashMap<String, Pair<HashSet<String>, HashSet<String>>> nodesPoles)
    {
        int N = Integer.parseInt(polesEl.getAttribute("PoleCount"));
        NodeList poles = polesEl.getElementsByTagName("Pole");
        IntStream.range(0, N).mapToObj(i -> ReadPole((Element) poles.item(i), model)).forEach(pole -> {
            if (nodesPoles.get(pole.getParent().getId()).getKey().contains(pole.getId()))
                pole.getParent().tryAddInputPole(pole);
            if (nodesPoles.get(pole.getParent().getId()).getValue().contains(pole.getId()))
                pole.getParent().tryAddOutputPole(pole);
        });
    }

    //Метод, читающий из XML-элемента гиперребра гиперграфа
    private static void ReadLinks(Element linksEl, PolarHypergraph model)
    {
        int N = Integer.parseInt(linksEl.getAttribute("HyperedgeCount"));
        NodeList hyperedges = linksEl.getElementsByTagName("Hyperedge");
        IntStream.range(0, N).forEach(i -> model.tryAddHyperedge(ReadLink((Element) hyperedges.item(i), model)));
    }

    //Метод, читающий из XML-элемента вершину гиперграфа
    private static Node ReadNode(Element nodeEl, Pair<HashSet<String>, HashSet<String>> poles)
    {
        Node node = new Node(nodeEl.getAttribute("ID"));
        ReadPolesIDs((Element)nodeEl.getElementsByTagName("InputPoles").item(0), poles.getKey());
        ReadPolesIDs((Element)nodeEl.getElementsByTagName("OutputPoles").item(0), poles.getValue());
        node.setInnerStructure(ReadHypergraph((Element)nodeEl.getElementsByTagName("InnerStructure").item(0)));
        return node;
    }

    //Метод, читающий из XML-элемента полюс
    private static Pole ReadPole(Element poleEl, PolarHypergraph model)
    {
        String id = poleEl.getAttribute("ID");
        String pID = poleEl.getAttribute("ParentID");
        Node parent = model.tryGetNode(pID);
        return new Pole(parent, id);
    }

    //Метод, читающий из XML-элемента гиперребро гиперграфа
    private static Hyperedge ReadLink(Element hyperedgeEl, PolarHypergraph hypergraph)
    {
        Hyperedge hyperedge = new Hyperedge(hyperedgeEl.getAttribute("ID"));
        ArrayList<String> linkedPoles = new ArrayList<>();
        ReadPolesIDs((Element)hyperedgeEl.getElementsByTagName("LinkedPoles").item(0), linkedPoles);
        linkedPoles.forEach(poleID -> hyperedge.tryAddPole(hypergraph.tryGetPole(poleID)));
        hyperedge.setInnerStructure(ReadHypergraph((Element)hyperedgeEl.getElementsByTagName("InnerStructure").item(0)));
        return hyperedge;
    }

    //Метод, преобразующий читающий из XML-элемента визуальное представление элемента
    private static View ReadView(Element viewEl)
    {
        View view = new View();
        view.setAssociatedElementID(viewEl.getAttribute("AssociatedElementID"));
        XStream stream = new XStream();
        view.setShape((Shape)stream.fromXML(((Element)viewEl.getElementsByTagName("Shape").item(0)).getAttribute("Value")));
        view.setStroke((Stroke)stream.fromXML(((Element)viewEl.getElementsByTagName("Stroke").item(0)).getAttribute("Value")));
        view.setBackColor(ReadRGBAColor((Element) viewEl.getElementsByTagName("BackColor").item(0)));
        view.setStrokeColor(ReadRGBAColor((Element) viewEl.getElementsByTagName("StrokeColor").item(0)));
        view.setName(viewEl.getElementsByTagName("Name").item(0).getNodeValue());
        view.setContent(viewEl.getElementsByTagName("Content").item(0).getNodeValue());
        return view;
    }

    //Метод, читающий из XML-элемента ID полюсов
    private static void ReadPolesIDs(Element el, Collection<String> poleIDs)
    {
        int N = Integer.parseInt(el.getAttributes().item(0).getNodeValue());
        NodeList ids = el.getElementsByTagName("PoleID");
        for (int i = 0; i < N; i++)
            poleIDs.add(ids.item(i).getNodeValue());
    }

    //Метод, читающий из XML-элемента цвет в формате RGBA
    private static Color ReadRGBAColor(Element colorEl)
    {
        int R = Integer.parseInt(colorEl.getAttribute("R"));
        int G = Integer.parseInt(colorEl.getAttribute("G"));
        int B = Integer.parseInt(colorEl.getAttribute("B"));
        int A = Integer.parseInt(colorEl.getAttribute("A"));
        return new Color(R, G, B, A);
    }
}
