package org.dspace.workflow;

import org.dspace.eperson.EPerson;

import javax.persistence.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/14
 * Time: 09:06
 */
@Entity
@Table(name = "tasklistitem", schema = "public")
public class TaskListItem {

    @Id
    @Column(name = "tasklist_id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="tasklistitem_seq")
    @SequenceGenerator(name="tasklistitem_seq", sequenceName="tasklistitem_seq")
    private int taskListItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson_id")
    private EPerson ePerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private WorkflowItem workflowItem;

    public int getTaskListItemId() {
        return taskListItemId;
    }

    public EPerson getEPerson() {
        return ePerson;
    }

    public WorkflowItem getWorkflowItem() {
        return workflowItem;
    }

    void setEPerson(EPerson ePerson) {
        this.ePerson = ePerson;
    }

    void setWorkflowItem(WorkflowItem workflowItem) {
        this.workflowItem = workflowItem;
    }
}
