package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.eperson.EPerson;

import javax.persistence.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 22/04/14
 * Time: 10:23
 */
@Entity
@Table(name="cwf_in_progress_user", schema = "public")
public class InProgressUser {

    @Id
    @Column(name="in_progress_user_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="cwf_in_progress_user_seq")
    @SequenceGenerator(name="cwf_in_progress_user_seq", sequenceName="cwf_in_progress_user_seq", allocationSize = 1)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name="user_id")
    private EPerson ePerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name="workflowitem_id")
    private XmlWorkflowItem workflowItem;

    @Column(name ="finished")
    private boolean finished = false;

    public int getId() {
        return id;
    }

    public EPerson getePerson() {
        return ePerson;
    }

    public void setePerson(EPerson ePerson) {
        this.ePerson = ePerson;
    }

    public XmlWorkflowItem getWorkflowItem() {
        return workflowItem;
    }

    public void setWorkflowItem(XmlWorkflowItem workflowItem) {
        this.workflowItem = workflowItem;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
