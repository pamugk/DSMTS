import java.awt.*;

//Класс визуального представления элемента модели
public class View {
    //Атрибут формы элемента, инкапсулирующий и всю информацию
    //о метоположении элемента
    private Shape shape;
    //Атрибут границы элемента, инкапсулирующий информацию о
    // толщине границ элемента, их виде и так далее
    private Stroke stroke;

    //Фоновый цвет элемента
    private Color backColor;
    //Цвет границ элемента
    private Color strokeColor;

    //Название элемента
    private String name;
    //Содержимое элемента
    private String content;

    //ID элемента, к которому относится данное представление
    private String associatedElementId;

    public View() {
        backColor = Color.WHITE;
        strokeColor = Color.BLACK;
        name = "";
        content = "";
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Color getBackColor() {
        return backColor;
    }

    public void setBackColor(Color backColor) {
        this.backColor = backColor;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAssociatedElementId() {
        return associatedElementId;
    }

    public void setAssociatedElementID(String associatedElementID) {
        this.associatedElementId = associatedElementID;
    }
}
