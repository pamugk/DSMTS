//Класс, представляющий визуальную модель
public class VisualModel {
    //Гиперграф с полюсами, отражающий структуру модели
    private PolarHypergraph structure;
    //Представление модели, отражающее визуальное отображение модели
    private ModelView view;

    public VisualModel()
    {
        structure = new PolarHypergraph();
        view = new ModelView();
    }

    public PolarHypergraph getStructure() {
        return structure;
    }

    public void setStructure(PolarHypergraph structure) {
        this.structure = structure;
    }

    public ModelView getView() {
        return view;
    }

    public void setView(ModelView view) {
        this.view = view;
    }
}
