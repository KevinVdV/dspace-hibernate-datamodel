package org.dspace.xmlworkflow.storedcomponents;

import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 21/04/14
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="cwf_workflowitemrole", schema = "public")
public class WorkflowItemRole
{
    @Id
    @Column(name="workflowitemrole_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="cwf_workflowitemrole_seq")
    @SequenceGenerator(name="cwf_workflowitemrole_seq", sequenceName="cwf_workflowitemrole_seq", allocationSize = 1)
    private int id;

    @Column(name = "role_id")
    @Lob
    private String roleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowitem_id")
    private XmlWorkflowItem workflowItem;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson_id")
    private EPerson ePerson;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public int getId() {
        return id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public XmlWorkflowItem getWorkflowItem() {
        return workflowItem;
    }

    public void setWorkflowItem(XmlWorkflowItem workflowItem) {
        this.workflowItem = workflowItem;
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
