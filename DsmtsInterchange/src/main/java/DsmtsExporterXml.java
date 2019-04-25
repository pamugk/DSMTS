import java.awt.*;
import java.io.OutputStream;
import java.io.Writer;
import java.io.File;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.thoughtworks.xstream.XStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DsmtsExporterXml implements VisualModelExporter {
    //Метод, возвращающий поддерживаемый экспортером формат
    public String getSupportedFormat() {
        return "DSMTS";
    }

    //Метод, экспортирующий модель model в файл по указанному пути filePath
    public void ExportModel(VisualModel model, String filePath) {
        Document doc = CreateXmlDocument();
        WriteModel(doc, model);
        WriteXmlDocument(doc, new StreamResult(new File(filePath)));
    }

    //Метод, экспортирующий модель model в переданный поток outputStream
    public void ExportModel(VisualModel model, OutputStream outputStream) {
        Document doc = CreateXmlDocument();
        WriteModel(doc, model);
        WriteXmlDocument(doc, new StreamResult(outputStream));
    }

    //Метод, записывающий модель model переданным  writer
    public void ExportModel(VisualModel model, Writer writer) {
        Document doc = CreateXmlDocument();
        WriteModel(doc, model);
        WriteXmlDocument(doc, new StreamResult(writer));
    }

    //Метод, создающий объект, представляющий XML-документ в оперативной памяти
    private static Document CreateXmlDocument(){
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return doc;
    }

    //Метод, записывающий переданный Xml-документ в переданный выходной поток
    private static void WriteXmlDocument(Document doc, StreamResult stream) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return;
        }
        DOMSource source = new DOMSource(doc);
        transformer.setOutputProperty(OutputKeys.INDENT,"yes");
        try {
            transformer.transform(source, stream);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    //Метод, формирующий на основе объекта визуальной модели соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WriteModel(Document doc, VisualModel model)
    {
        Element rootElement = doc.createElement("VisualModel");
        WritePolarHypergraph(doc, rootElement, model.getStructure(), "Structure");
        WriteModelView(doc, rootElement, model.getView(), "ModelView");
        doc.appendChild(rootElement);
    }

    //Метод, формирующий на основе объекта гиперграфа с полюсами соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WritePolarHypergraph(Document doc, Element root, PolarHypergraph hypergraph, String header)
    {
        Element hypergraphEl = doc.createElement(header);
        root.appendChild(hypergraphEl);
        if (hypergraph == null)
            return;

        Element nodesRoot = doc.createElement("Nodes");
        hypergraph.getNodes().forEach(node -> WriteNode(doc, nodesRoot, node, "Node"));

        Element polesRoot = doc.createElement("Poles");
        hypergraph.getNodes().forEach(node -> WriteNode(doc, polesRoot, node, "Pole"));

        Element linksRoot = doc.createElement("Hyperedges");
        hypergraph.getNodes().forEach(node -> WriteNode(doc, linksRoot, node, "Hyperedge"));
    }

    //Метод, формирующий на основе объекта узла модели соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WriteNode(Document doc, Element root, Node node, String header)
    {
        Element nodeEl = doc.createElement(header);
        nodeEl.setAttribute("ID", node.getId());
        WritePolesHeadersXml(doc, nodeEl, node.getInputPoles(), "Input");
        WritePolesHeadersXml(doc, nodeEl,node.getOutputPoles(), "Output");
        WritePolarHypergraph(doc, nodeEl, node.getInnerStructure(), "InnerStructure");
        root.appendChild(nodeEl);
    }

    //Метод, формирующий на основе объекта полюса соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WritePole(Document doc, Element root, Pole pole, String header)
    {
        Element poleEl = doc.createElement(header);
        root.appendChild(poleEl);
        if (pole == null)
            return;
        poleEl.setAttribute("ID", pole.getId());
        poleEl.setAttribute("ParentID", pole.getParent().getId());
    }

    //Метод, формирующий на основе объекта гиперребра соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WriteHyperedge(Document doc, Element root, Hyperedge hyperedge)
    {
        Element hyperedgeEl = doc.createElement("Hyperedge");
        hyperedgeEl.setAttribute("ID", hyperedge.getId());
        WritePolesHeadersXml(doc, hyperedgeEl, hyperedge.getLinkedPoles(), "Linked");
        WritePolarHypergraph(doc, hyperedgeEl, hyperedge.getInnerStructure(), "InnerStructure");
    }

    //Метод, формирующий на основе визуального представления модели соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WriteModelView(Document doc, Element root, ModelView modelView, String header)
    {
        Element mvEl = doc.createElement(header);
        WriteViews(doc, mvEl, "NodeViews", "NodeView", modelView.NodeViews.values());
        WriteViews(doc, mvEl, "PoleViews", "PoleView", modelView.PoleViews.values());
        WriteViews(doc, mvEl, "LinkViews", "LinkView",modelView.LinkViews.values());
        root.appendChild(mvEl);
    }

    //Метод, формирующий на основе представлений элементов соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WriteViews(Document doc, Element root, String header, String elHeader, Collection<View> views){
        Element headerEl = doc.createElement(header);
        headerEl.setAttribute("Count", String.valueOf(views.size()));
        views.forEach(view -> WriteView(doc, headerEl, view, elHeader));
        root.appendChild(headerEl);
    }

    //Метод, формирующий на основе объекта представления элемента модели
    // соответствующий Xml-элемент и включающий его в Xml-документ
    private static void WriteView(Document doc, Element root, View view, String header)
    {
        Element viewEl = doc.createElement(header);
        root.appendChild(viewEl);
        if (view == null)
            return;
        viewEl.setAttribute("AssociatedElementID", view.getAssociatedElementId());
        WriteShape(doc, viewEl, view.getShape(), "Shape");
        WriteStroke(doc, viewEl, view.getStroke(), "Stroke");
        WriteRGBAColorXml(doc, viewEl, view.getBackColor(), "BackColor");
        WriteRGBAColorXml(doc, viewEl, view.getStrokeColor(), "StrokeColor");
        viewEl.appendChild(CreateTextNode(doc, "Name", view.getName()));
        viewEl.appendChild(CreateTextNode(doc, "Content", view.getContent()));
    }

    //Метод, формирующий на основе объекта формы соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WriteShape(Document doc, Element root, Shape shape, String header){
        Element shapeEl = doc.createElement(header);
        root.appendChild(shapeEl);
        if (shape == null)
            return;
        XStream stream = new XStream();
        shapeEl.setAttribute("Value", stream.toXML(shape));
    }

    //Метод, формирующий на основе объекта черты соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WriteStroke(Document doc, Element root, Stroke stroke, String header){
        Element strokeEl = doc.createElement(header);
        root.appendChild(strokeEl);
        if (stroke == null)
            return;
        XStream stream = new XStream();
        strokeEl.setAttribute("Value", stream.toXML(stroke));
    }

    //Метод, формирующий текстовый элемент в XMl для переданных заголовка и значеия
    private static Element CreateTextNode(Document doc, String header, String value){
        Element el = doc.createElement(header);
        el.appendChild(doc.createTextNode(value));
        return el;
    }

    //Метод, формирующий на основе объекта цвета соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WritePolesHeadersXml(Document doc, Element root, Collection<Pole> set, String adj)
    {
        Element polesHeaderEl = doc.createElement(adj+"Poles");
        polesHeaderEl.setAttribute(adj+"PoleCount", String.valueOf(set.size()));
        set.forEach(pole -> polesHeaderEl.appendChild(CreateTextNode(doc, "PoleID", String.valueOf(pole.getId()))));
        root.appendChild(polesHeaderEl);
    }

    //Метод, формирующий на основе объекта цвета соответствующий Xml-элемент и включающий его
    //в Xml-документ
    private static void WriteRGBAColorXml(Document doc, Element root, Color color, String start)
    {
        Element colorEl = doc.createElement(start);
        colorEl.setAttribute("R", String.valueOf(color.getRed()));
        colorEl.setAttribute("G", String.valueOf(color.getGreen()));
        colorEl.setAttribute("B", String.valueOf(color.getBlue()));
        colorEl.setAttribute("A", String.valueOf(color.getAlpha()));
        root.appendChild(colorEl);
    }
}
