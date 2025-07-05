package source.hanger.flow.contract.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParallelStepDefinition extends AbstractStepDefinition {
    private final Map<String, Branch> branches = new HashMap<>();
    private List<String> joinBranchNames;

    public void addBranch(Branch branch) {
        branches.put(branch.nextStepName(), branch);
    }

    public Map<String, Branch> getBranches() {
        return branches;
    }

    public List<String> getJoinBranchNames() {
        return joinBranchNames;
    }

    public void setJoinBranchNames(List<String> joinBranchNames) {
        this.joinBranchNames = joinBranchNames;
    }
}