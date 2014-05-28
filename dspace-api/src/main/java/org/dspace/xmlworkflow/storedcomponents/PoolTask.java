package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="cwf_pooltask", schema = "public")
public class PoolTask {

    @Id
    @Column(name="pooltask_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="cwf_pooltask_seq")
    @SequenceGenerator(name="cwf_pooltask_seq", sequenceName="cwf_pooltask_seq", allocationSize = 1)
    private int id;

    @OneToOne
    @JoinColumn(name = "workflowitem_id")
    private XmlWorkflowItem workflowItem;

    @Column(name = "workflow_id")
    @Lob
    private String workflowId;

    @Column(name = "step_id")
    @Lob
    private String stepId;

    @Column(name = "action_id")
    @Lob
    private String actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name="eperson_id")
    private EPerson ePerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "group_id")
    private Group group;

    public int getId() {
        return id;
    }

    public XmlWorkflowItem getWorkflowItem() {
        return workflowItem;
    }

    public void setWorkflowItem(XmlWorkflowItem workflowItem) {
        this.workflowItem = workflowItem;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public EPerson getePerson() {
        return ePerson;
    }

    public void setePerson(EPerson ePerson) {
        this.ePerson = ePerson;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
