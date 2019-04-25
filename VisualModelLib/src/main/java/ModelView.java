import java.util.HashMap;

//Класс представления визуальной модели
public class ModelView {
    //Множество представлений вершин
    //Представлено в виде ассоциативной коллекции,
    //где ключ - ID связанной вершины,
    // а значение - представление вершины
    public HashMap<String, View> NodeViews;
    //Множество представлений полюсов
    //Представлено в виде ассоциативной коллекции,
    //где ключ - ID связанного полюса,
    // а значение - представление полюса
    public HashMap<String, View> PoleViews;
    //Множество представлений гиперребер
    //Представлено в виде ассоциативной коллекции,
    //где ключ - ID связанного гиперребра,
    // а значение - представление гиперребра
    public HashMap<String, View> LinkViews;

    public ModelView()
    {
        NodeViews = new HashMap<>();
        PoleViews = new HashMap<>();
        LinkViews = new HashMap<>();
    }
}
