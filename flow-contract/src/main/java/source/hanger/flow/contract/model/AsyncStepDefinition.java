package source.hanger.flow.contract.model;

import java.util.ArrayList;
import java.util.List;

public class AsyncStepDefinition extends AbstractStepDefinition {
    private final List<String> branchNames = new ArrayList<>();

    public void addBranchName(String s) {
        branchNames.add(s);
    }

    public List<String> getBranchNames() {
        return branchNames;
    }
}