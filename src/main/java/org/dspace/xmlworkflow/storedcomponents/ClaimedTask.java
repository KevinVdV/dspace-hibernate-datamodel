package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.eperson.EPerson;
import org.dspace.workflow.WorkflowItem;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 19/04/14
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="cwf_claimtask", schema = "public")
public class ClaimedTask {

    @Id
    @Column(name="claimtask_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="cwf_claimtask_seq")
    @SequenceGenerator(name="cwf_claimtask_seq", sequenceName="cwf_claimtask_seq", allocationSize = 1)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowitem_id")
    private XmlWorkflowItem workflowItem;

    @Column(name = "workflow_id")
    private String workflowId;

    @Column(name = "step_id")
    private String stepId;

    @Column(name = "action_id")
    private String actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private EPerson owner;

    public int getId() {
        return id;
    }

    public void setWorkflowItem(XmlWorkflowItem workflowItem) {
        this.workflowItem = workflowItem;
    }

    void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    void setStepId(String stepId) {
        this.stepId = stepId;
    }

    void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public void setOwner(EPerson owner) {
        this.owner = owner;
    }

    public XmlWorkflowItem getWorkflowItem() {
        return workflowItem;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getStepId() {
        return stepId;
    }

    public String getActionId() {
        return actionId;
    }

    public EPerson getOwner() {
        return owner;
    }
}
