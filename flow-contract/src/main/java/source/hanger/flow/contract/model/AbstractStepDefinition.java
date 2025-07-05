package source.hanger.flow.contract.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fuhangbo.hanger.uhfun
 **/
public abstract class AbstractStepDefinition implements StepDefinition {
    protected String name;
    protected String description;
    protected List<Transition> transition = new ArrayList<>();
    protected Transition errorTransition;

    @Override
    public void addTransition(Transition transition) {
        this.transition.add(transition);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Transition getErrorTransition() {
        return errorTransition;
    }

    @Override
    public void setErrorTransition(Transition errorTransition) {
        this.errorTransition = errorTransition;
    }
}
